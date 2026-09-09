"""
output.py — Write conversion outputs and post-process results.
"""

from __future__ import annotations

import logging
import re
from pathlib import Path


logger = logging.getLogger("java2rsmod")


def split_response(raw: str) -> tuple[str, str]:
    """
    Split Ollama response into Kotlin code and conversion notes.
    Uses the ---NOTES--- sentinel line as delimiter.
    """
    sentinel = "---NOTES---"
    if sentinel in raw:
        parts = raw.split(sentinel, maxsplit=1)
        return parts[0].strip(), parts[1].strip()
    return raw.strip(), "(No structured notes returned by model — review .kt file manually.)"


def clean_kotlin(raw_kt: str) -> str:
    """
    Post-process Kotlin output to strip markdown fences and embedded notes.
    Makes the output more robust even if the model doesn't follow format exactly.
    """
    # Strip markdown code fences
    raw_kt = re.sub(r"^```kotlin\s*\n?", "", raw_kt)
    raw_kt = re.sub(r"\n?```\s*$", "", raw_kt)

    # Strip leading/trailing prose (lines before 'package' or 'import' or 'class')
    lines = raw_kt.split("\n")
    start = 0
    for i, line in enumerate(lines):
        stripped = line.strip()
        if stripped.startswith(("package ", "import ", "class ", "enum ", "data ", "object ", "interface ", "fun ")):
            start = i
            break
    lines = lines[start:]

    # Strip trailing notes section if embedded
    for i, line in enumerate(lines):
        if line.strip().startswith("### Conversion Summary"):
            lines = lines[:i]
            break
        if line.strip().startswith("---NOTES---"):
            lines = lines[:i]
            break

    return "\n".join(lines).strip()


def validate_output(kt_path: Path, notes_path: Path) -> list[str]:
    """
    Validate conversion output files. Returns list of issues found.
    Empty list means all checks passed.
    """
    issues = []

    if not kt_path.exists():
        issues.append(f"Missing .kt file: {kt_path.name}")
        return issues

    kt_content = kt_path.read_text(encoding="utf-8")

    if not kt_content.strip():
        issues.append(f"Empty .kt file: {kt_path.name}")
    elif "com.elvarg" in kt_content:
        issues.append(f"Elvarg imports remain in: {kt_path.name}")
    elif not any(kw in kt_content for kw in ("package ", "class ", "enum ", "object ", "fun ")):
        issues.append(f"No Kotlin structure found in: {kt_path.name}")

    if notes_path.exists():
        notes_content = notes_path.read_text(encoding="utf-8")
        if notes_content.strip() == "(No structured notes returned by model — review .kt file manually.)":
            issues.append(f"No structured notes in: {notes_path.name}")

    return issues


def write_outputs(raw_response: str, stem: str, output_dir: Path) -> None:
    """Write the .kt skeleton and _notes.md file to output_dir."""
    output_dir.mkdir(parents=True, exist_ok=True)

    kt_content, notes_content = split_response(raw_response)
    kt_content = clean_kotlin(kt_content)

    kt_path = output_dir / f"{stem}.kt"
    notes_path = output_dir / f"{stem}_notes.md"

    kt_path.write_text(kt_content, encoding="utf-8")
    notes_path.write_text(notes_content, encoding="utf-8")

    logger.info("  Written: %s", kt_path.name)
    logger.info("  Written: %s", notes_path.name)
