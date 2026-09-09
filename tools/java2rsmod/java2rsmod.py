r"""
java2rsmod.py — Convert Elvarg/RSPS Java files to rsmod Kotlin skeletons.

Usage (PowerShell):
    cd C:\Users\bob\Desktop\gpt-rsmod\tools\java2rsmod
    python java2rsmod.py                  # process everything in input/
    python java2rsmod.py MyFile.java      # process a single file
    python java2rsmod.py --model mistral  # override model for this run
    python java2rsmod.py --dry-run        # pre-analysis only, skip Ollama
    python java2rsmod.py --validate       # validate existing output files
    python java2rsmod.py --clean          # remove old timestamped output folders

Outputs per file (inside output/<timestamp>/ by default):
    {Name}.kt          — Kotlin skeleton ready to review
    {Name}_notes.md    — conversion notes: what was kept / replaced / needs work
"""

from __future__ import annotations

import argparse
import logging
import shutil
import sys
import textwrap
from datetime import datetime
from pathlib import Path

import config
from logging_setup import setup_logging
from pipeline import convert_file
from analysis import analyse_file
from output import validate_output


logger = setup_logging()


def _load_text_file(path: Path, label: str) -> str:
    """Read a required text file, exiting with a clear message if missing."""
    if not path.exists():
        logger.critical("%s not found: %s", label, path)
        sys.exit(1)
    try:
        return path.read_text(encoding="utf-8")
    except OSError as exc:
        logger.critical("Could not read %s (%s): %s", label, exc)
        sys.exit(1)


def _resolve_output_dir() -> Path:
    """Return the output directory for this run (timestamped or flat)."""
    if config.TIMESTAMPED_OUTPUT:
        timestamp = datetime.now().strftime("%Y-%m-%d_%H%M")
        return config.OUTPUT_DIR / timestamp
    return config.OUTPUT_DIR


def discover_files(paths: list[str] | None) -> list[Path]:
    """
    Resolve input files from CLI args or scan INPUT_DIR recursively.
    Accepts bare names relative to input/ for convenience.
    """
    if paths:
        java_files: list[Path] = []
        for f in paths:
            p = Path(f)
            if not p.is_absolute() and not p.exists():
                p = config.INPUT_DIR / p
            if not p.exists():
                logger.error("File not found: %s", p)
                continue
            java_files.append(p)
        return java_files

    config.INPUT_DIR.mkdir(parents=True, exist_ok=True)
    return sorted(config.INPUT_DIR.rglob("*.java"))


def dry_run(files: list[Path]) -> None:
    """Run pre-analysis only — no Ollama calls."""
    logger.info("DRY RUN — pre-analysis only")
    for java_path in files:
        try:
            source = java_path.read_text(encoding="utf-8", errors="replace")
        except OSError as exc:
            logger.error("Could not read %s: %s", java_path, exc)
            continue
        if not source.strip():
            logger.warning("Skipping %s — empty.", java_path.name)
            continue
        analyse_file(source, java_path.name)


def clean_output() -> None:
    """Remove old timestamped output folders, keep by-type/ and reviewed/."""
    if not config.OUTPUT_DIR.exists():
        return
    removed = 0
    for child in config.OUTPUT_DIR.iterdir():
        if child.is_dir() and child.name[0:4].isdigit():
            shutil.rmtree(child)
            removed += 1
    logger.info("Cleaned %d old output folders", removed)


def validate_all() -> None:
    """Validate all existing output files."""
    if not config.OUTPUT_DIR.exists():
        logger.warning("No output directory found")
        return
    total_issues = 0
    for kt_path in sorted(config.OUTPUT_DIR.rglob("*.kt")):
        notes_path = kt_path.with_name(kt_path.stem + "_notes.md")
        issues = validate_output(kt_path, notes_path)
        if issues:
            for issue in issues:
                logger.warning("  %s", issue)
            total_issues += len(issues)
        else:
            logger.info("  OK: %s", kt_path.name)
    logger.info("Validation complete — %d issues found", total_issues)


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Convert Elvarg Java files to rsmod Kotlin skeletons using Ollama.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=textwrap.dedent("""
            Examples:
              python java2rsmod.py                     # convert all files in input/
              python java2rsmod.py Slayer.java         # convert a single file
              python java2rsmod.py --model codellama   # override the AI model
              python java2rsmod.py --dry-run           # preview analysis only
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
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Run pre-analysis only, skip Ollama.",
    )
    parser.add_argument(
        "--validate",
        action="store_true",
        help="Validate existing output files.",
    )
    parser.add_argument(
        "--clean",
        action="store_true",
        help="Remove old timestamped output folders.",
    )
    args = parser.parse_args()

    logging.getLogger().setLevel(getattr(logging, args.log_level))

    if args.clean:
        clean_output()
        return

    if args.validate:
        validate_all()
        return

    logger.info("=" * 60)
    logger.info("java2rsmod starting — model: %s", args.model)
    logger.info("=" * 60)

    java_files = discover_files(args.files if args.files else None)

    if not java_files:
        logger.warning(
            "No Java files found.  Drop .java files into: %s", config.INPUT_DIR
        )
        sys.exit(0)

    logger.info("Files to convert: %d", len(java_files))

    if args.dry_run:
        dry_run(java_files)
        return

    knowledge_base = _load_text_file(config.KNOWLEDGE_BASE_FILE, "Knowledge base")
    prompt_template = _load_text_file(config.PROMPT_TEMPLATE_FILE, "Prompt template")
    logger.debug("Knowledge base: %d chars", len(knowledge_base))
    logger.debug("Prompt template: %d chars", len(prompt_template))

    output_dir = _resolve_output_dir()
    logger.info("Output directory: %s", output_dir)

    success_count = 0
    fail_count = 0
    for java_path in java_files:
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
