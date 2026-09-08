"""
java2rsmod.py — Convert Elvarg/RSPS Java files to rsmod Kotlin skeletons.

Usage (PowerShell):
    cd C:\Users\bob\Desktop\gpt-rsmod\tools\java2rsmod
    python java2rsmod.py                  # process everything in input/
    python java2rsmod.py MyFile.java      # process a single file
    python java2rsmod.py --model mistral  # override model for this run

Outputs per file (inside output/<timestamp>/ by default):
    {Name}.kt          — Kotlin skeleton ready to review
    {Name}_notes.md    — conversion notes: what was kept / replaced / needs work
"""

from __future__ import annotations

import argparse
import json
import logging
import re
import sys
import textwrap
from datetime import datetime
from logging.handlers import RotatingFileHandler
from pathlib import Path
from typing import NamedTuple

import httpx

import config


# ── Logging setup ──────────────────────────────────────────────────────────────

def _setup_logging() -> logging.Logger:
    """Configure console + rotating-file logging from config values."""
    config.LOG_FILE.parent.mkdir(parents=True, exist_ok=True)

    log_level = getattr(logging, config.LOG_LEVEL.upper(), logging.INFO)

    formatter = logging.Formatter(
        fmt="%(asctime)s [%(levelname)-8s] %(name)s: %(message)s",
        datefmt="%Y-%m-%d %H:%M:%S",
    )

    # Console handler
    console_handler = logging.StreamHandler(sys.stdout)
    console_handler.setLevel(log_level)
    console_handler.setFormatter(formatter)

    # Rotating file handler
    file_handler = RotatingFileHandler(
        config.LOG_FILE,
        maxBytes=config.LOG_MAX_BYTES,
        backupCount=config.LOG_BACKUP_COUNT,
        encoding="utf-8",
    )
    file_handler.setLevel(logging.DEBUG)  # always capture full detail to file
    file_handler.setFormatter(formatter)

    root_logger = logging.getLogger()
    root_logger.setLevel(logging.DEBUG)
    root_logger.addHandler(console_handler)
    root_logger.addHandler(file_handler)

    return logging.getLogger("java2rsmod")


logger = _setup_logging()


# ── Data types ─────────────────────────────────────────────────────────────────

class FileAnalysis(NamedTuple):
    """Result of the fast rule-based pre-analysis step."""
    filename: str
    is_enum: bool
    is_data_holder: bool        # simple POJO / data holder class
    has_npc_interaction: bool
    has_item_interaction: bool
    has_loc_interaction: bool
    has_death_handler: bool
    elvarg_apis_found: list[str]  # Java API calls with no rsmod equivalent
    suggested_output_type: str    # e.g. "PluginScript", "enum class", "data class"


# ── Pre-analysis (rule-based, no AI) ──────────────────────────────────────────

# Elvarg API calls that have NO direct rsmod equivalent — must be flagged
_ELVARG_API_PATTERNS: dict[str, str] = {
    r"getPacketSender\(\)": "getPacketSender() → use mes() or similar rsmod player message API",
    r"getSkillManager\(\)": "getSkillManager() → use stat() / statAdvance() / statRandom()",
    r"getInventory\(\)": "getInventory() → use inv / invAdd / invDel",
    r"Misc\.getRandom": "Misc.getRandom() → use rsmod random utilities",
    r"Misc\.inclusive": "Misc.inclusive() → use rsmod range helpers",
    r"Misc\.ucFirst": "Misc.ucFirst() → use Kotlin string extensions",
    r"com\.elvarg\.":  "com.elvarg.* import → remove entirely, no rsmod equivalent",
    r"sendMessage\(": "sendMessage() → use mes()",
    r"sendInterfaceRemoval\(": "sendInterfaceRemoval() → check rsmod interface API",
    r"getHitpoints\(\)": "getHitpoints() → use stat(stats.hitpoints)",
}


def analyse_file(java_source: str, filename: str) -> FileAnalysis:
    """
    Perform a fast keyword scan to classify the Java file before sending
    it to the AI.  No network calls — instant and free.
    """
    logger.debug("Pre-analysing %s", filename)

    is_enum = bool(re.search(r"\bpublic\s+enum\b", java_source))

    # A data holder has only private fields + getters/setters, no logic
    field_count = len(re.findall(r"private\s+\w+\s+\w+;", java_source))
    method_count = len(re.findall(r"(public|private|protected)\s+\w[\w<>\[\]]*\s+\w+\s*\(", java_source))
    is_data_holder = (
        not is_enum
        and field_count >= 1
        and method_count <= field_count * 3  # mostly getters/setters
    )

    has_npc_interaction = bool(
        re.search(r"onOpNpc|NpcClick|NpcInteract|pickpocket|getOpNpc", java_source, re.IGNORECASE)
    )
    has_item_interaction = bool(
        re.search(r"onOpHeldU|ItemOnItem|UseWith|useOn", java_source, re.IGNORECASE)
    )
    has_loc_interaction = bool(
        re.search(r"onOpLoc|LocClick|LocInteract|ObjectClick", java_source, re.IGNORECASE)
    )
    has_death_handler = bool(
        re.search(r"onDeath|killed\s*\(|npcDeath|playerDeath", java_source, re.IGNORECASE)
    )

    elvarg_apis_found: list[str] = []
    for pattern, description in _ELVARG_API_PATTERNS.items():
        if re.search(pattern, java_source):
            elvarg_apis_found.append(description)

    # Determine suggested output type
    if is_enum:
        suggested = "enum class"
    elif is_data_holder:
        suggested = "data class"
    elif has_npc_interaction or has_item_interaction or has_loc_interaction or has_death_handler:
        suggested = "PluginScript"
    else:
        suggested = "PluginScript (review required)"

    analysis = FileAnalysis(
        filename=filename,
        is_enum=is_enum,
        is_data_holder=is_data_holder,
        has_npc_interaction=has_npc_interaction,
        has_item_interaction=has_item_interaction,
        has_loc_interaction=has_loc_interaction,
        has_death_handler=has_death_handler,
        elvarg_apis_found=elvarg_apis_found,
        suggested_output_type=suggested,
    )

    logger.info(
        "  Analysis complete: type=%s | npc=%s item=%s loc=%s death=%s | elvarg APIs=%d",
        suggested,
        has_npc_interaction,
        has_item_interaction,
        has_loc_interaction,
        has_death_handler,
        len(elvarg_apis_found),
    )
    return analysis


# ── Prompt builder ─────────────────────────────────────────────────────────────

def build_prompt(
    java_source: str,
    analysis: FileAnalysis,
    knowledge_base: str,
    template: str,
) -> str:
    """Assemble the full prompt by filling in the template placeholders."""
    analysis_summary = textwrap.dedent(f"""
        File: {analysis.filename}
        Suggested output type: {analysis.suggested_output_type}
        Is enum: {analysis.is_enum}
        Is data holder: {analysis.is_data_holder}
        Has NPC interaction: {analysis.has_npc_interaction}
        Has item-on-item interaction: {analysis.has_item_interaction}
        Has LOC interaction: {analysis.has_loc_interaction}
        Has death handler: {analysis.has_death_handler}
        Elvarg APIs detected (must be replaced or removed):
    """).strip()

    if analysis.elvarg_apis_found:
        for api in analysis.elvarg_apis_found:
            analysis_summary += f"\n  - {api}"
    else:
        analysis_summary += "\n  (none detected)"

    return (
        template
        .replace("{{KNOWLEDGE_BASE}}", knowledge_base)
        .replace("{{ANALYSIS_SUMMARY}}", analysis_summary)
        .replace("{{JAVA_SOURCE}}", java_source)
    )


# ── Ollama client ──────────────────────────────────────────────────────────────

def call_ollama(prompt: str, model: str) -> str:
    """
    Send a prompt to a locally running Ollama instance and return the
    full response text.

    Raises:
        ConnectionError: if Ollama is not reachable.
        RuntimeError:    if Ollama returns an unexpected status code.
        TimeoutError:    if the request exceeds OLLAMA_TIMEOUT_SECONDS.
    """
    url = f"{config.OLLAMA_BASE_URL}/api/generate"
    payload = {
        "model": model,
        "prompt": prompt,
        "stream": False,
    }

    logger.debug("Calling Ollama at %s with model '%s'", url, model)

    try:
        response = httpx.post(
            url,
            json=payload,
            timeout=config.OLLAMA_TIMEOUT_SECONDS,
        )
    except httpx.ConnectError as exc:
        raise ConnectionError(
            f"Could not connect to Ollama at {config.OLLAMA_BASE_URL}.\n"
            "Make sure Ollama is running: open a terminal and run 'ollama serve'."
        ) from exc
    except httpx.TimeoutException as exc:
        raise TimeoutError(
            f"Ollama did not respond within {config.OLLAMA_TIMEOUT_SECONDS}s. "
            "Try a smaller model or increase OLLAMA_TIMEOUT_SECONDS in config.py."
        ) from exc

    if response.status_code != 200:
        raise RuntimeError(
            f"Ollama returned HTTP {response.status_code}: {response.text[:200]}"
        )

    try:
        data = response.json()
    except json.JSONDecodeError as exc:
        raise RuntimeError(
            f"Ollama response was not valid JSON: {response.text[:200]}"
        ) from exc

    return data.get("response", "").strip()


# ── Output writers ─────────────────────────────────────────────────────────────

def _split_ollama_output(raw: str) -> tuple[str, str]:
    """
    The AI is asked to return two sections separated by a sentinel line:
        ---NOTES---
    Everything before that line is the Kotlin skeleton.
    Everything after is the conversion notes.
    If the sentinel is missing, the whole response becomes the Kotlin file
    and the notes file will contain a fallback message.
    """
    sentinel = "---NOTES---"
    if sentinel in raw:
        parts = raw.split(sentinel, maxsplit=1)
        return parts[0].strip(), parts[1].strip()
    return raw.strip(), "(No structured notes returned by model — review .kt file manually.)"


def write_outputs(raw_response: str, stem: str, output_dir: Path) -> None:
    """Write the .kt skeleton and _notes.md file to output_dir."""
    output_dir.mkdir(parents=True, exist_ok=True)

    kt_content, notes_content = _split_ollama_output(raw_response)

    kt_path = output_dir / f"{stem}.kt"
    notes_path = output_dir / f"{stem}_notes.md"

    kt_path.write_text(kt_content, encoding="utf-8")
    notes_path.write_text(notes_content, encoding="utf-8")

    logger.info("  Written: %s", kt_path.name)
    logger.info("  Written: %s", notes_path.name)


# ── Core pipeline ──────────────────────────────────────────────────────────────

def convert_file(
    java_path: Path,
    output_dir: Path,
    knowledge_base: str,
    prompt_template: str,
    model: str,
) -> bool:
    """
    Full pipeline for a single Java file.
    Returns True on success, False on any handled error.
    """
    logger.info("Processing: %s", java_path.name)

    # 1. Read source
    try:
        java_source = java_path.read_text(encoding="utf-8", errors="replace")
    except OSError as exc:
        logger.error("Could not read %s: %s", java_path, exc)
        return False

    if not java_source.strip():
        logger.warning("Skipping %s — file is empty.", java_path.name)
        return False

    # 2. Pre-analyse
    analysis = analyse_file(java_source, java_path.name)

    # 3. Build prompt
    prompt = build_prompt(java_source, analysis, knowledge_base, prompt_template)
    logger.debug("Prompt length: %d characters", len(prompt))

    # 4. Call Ollama
    try:
        raw_response = call_ollama(prompt, model)
    except (ConnectionError, TimeoutError, RuntimeError) as exc:
        logger.error("Ollama error for %s: %s", java_path.name, exc)
        return False

    if not raw_response:
        logger.warning("Ollama returned an empty response for %s.", java_path.name)
        return False

    # 5. Write outputs
    write_outputs(raw_response, java_path.stem, output_dir)
    return True


# ── Entry point ────────────────────────────────────────────────────────────────

def _resolve_output_dir() -> Path:
    """Return the output directory for this run (timestamped or flat)."""
    if config.TIMESTAMPED_OUTPUT:
        timestamp = datetime.now().strftime("%Y-%m-%d_%H%M")
        return config.OUTPUT_DIR / timestamp
    return config.OUTPUT_DIR


def _load_text_file(path: Path, label: str) -> str:
    """Read a required text file, exiting with a clear message if missing."""
    if not path.exists():
        logger.critical("%s not found: %s", label, path)
        sys.exit(1)
    try:
        return path.read_text(encoding="utf-8")
    except OSError as exc:
        logger.critical("Could not read %s (%s): %s", label, path, exc)
        sys.exit(1)


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Convert Elvarg Java files to rsmod Kotlin skeletons using Ollama.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=textwrap.dedent("""
            Examples:
              python java2rsmod.py                     # convert all files in input/
              python java2rsmod.py Slayer.java         # convert a single file
              python java2rsmod.py --model codellama   # override the AI model
        """),
    )
    parser.add_argument(
        "files",
        nargs="*",
        metavar="FILE",
        help="Java file(s) to convert (default: all .java files in input/).",
    )
    parser.add_argument(
        "--model",
        default=config.OLLAMA_MODEL,
        help=f"Ollama model to use (default: {config.OLLAMA_MODEL}).",
    )
    parser.add_argument(
        "--log-level",
        default=config.LOG_LEVEL,
        choices=["DEBUG", "INFO", "WARNING", "ERROR"],
        help="Override log verbosity for this run.",
    )
    args = parser.parse_args()

    # Apply CLI log level override
    logging.getLogger().setLevel(getattr(logging, args.log_level))

    logger.info("=" * 60)
    logger.info("java2rsmod starting — model: %s", args.model)
    logger.info("=" * 60)

    # Load shared resources
    knowledge_base = _load_text_file(config.KNOWLEDGE_BASE_FILE, "Knowledge base")
    prompt_template = _load_text_file(config.PROMPT_TEMPLATE_FILE, "Prompt template")
    logger.debug("Knowledge base: %d chars", len(knowledge_base))
    logger.debug("Prompt template: %d chars", len(prompt_template))

    # Resolve input files
    if args.files:
        java_files: list[Path] = []
        for f in args.files:
            p = Path(f)
            # Accept bare names relative to input/ for convenience
            if not p.is_absolute() and not p.exists():
                p = config.INPUT_DIR / p
            if not p.exists():
                logger.error("File not found: %s", p)
                continue
            java_files.append(p)
    else:
        config.INPUT_DIR.mkdir(parents=True, exist_ok=True)
        java_files = [
            f for f in config.INPUT_DIR.iterdir()
            if f.suffix in config.ACCEPTED_EXTENSIONS
        ]

    if not java_files:
        logger.warning(
            "No Java files found.  Drop .java files into: %s", config.INPUT_DIR
        )
        sys.exit(0)

    logger.info("Files to convert: %d", len(java_files))

    output_dir = _resolve_output_dir()
    logger.info("Output directory: %s", output_dir)

    # Process each file
    success_count = 0
    fail_count = 0
    for java_path in sorted(java_files):
        ok = convert_file(
            java_path=java_path,
            output_dir=output_dir,
            knowledge_base=knowledge_base,
            prompt_template=prompt_template,
            model=args.model,
        )
        if ok:
            success_count += 1
        else:
            fail_count += 1

    logger.info("=" * 60)
    logger.info(
        "Done. Success: %d  Failed: %d  Output: %s",
        success_count,
        fail_count,
        output_dir,
    )
    if fail_count > 0:
        logger.warning("%d file(s) had errors — check logs above.", fail_count)
    logger.info("=" * 60)


if __name__ == "__main__":
    main()
