"""
ollama_client.py — HTTP client for the local Ollama API.
"""

from __future__ import annotations

import json
import logging

import httpx

import config


logger = logging.getLogger("java2rsmod")


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
