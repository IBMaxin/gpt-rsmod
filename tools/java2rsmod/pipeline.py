"""
pipeline.py — Core conversion pipeline.
"""

from __future__ import annotations

import logging
from pathlib import Path

from analysis import analyse_file
from prompt import build_prompt
from ollama_client import call_ollama
from output import write_outputs


logger = logging.getLogger("java2rsmod")


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
