"""
config.py — Central configuration for java2rsmod.

Edit this file to change paths, model settings, or logging behaviour.
All paths are resolved relative to this file's location so the tool
works regardless of where you run it from.
"""

from pathlib import Path

# ── Paths ──────────────────────────────────────────────────────────────────────

# Root of this tool (the java2rsmod/ folder)
TOOL_ROOT = Path(__file__).parent.resolve()

# Java files to convert go here
INPUT_DIR = TOOL_ROOT / "input"

# Generated .kt and _notes.md files land here (one sub-folder per run)
OUTPUT_DIR = TOOL_ROOT / "output"

# Prompt template used when calling Ollama
PROMPT_TEMPLATE_FILE = TOOL_ROOT / "prompts" / "conversion.txt"

# RSMod API reference injected into every prompt
KNOWLEDGE_BASE_FILE = TOOL_ROOT / "context" / "AI_KNOWLEDGE_BASE.md"

# Log file (rotated automatically; see logging setup in java2rsmod.py)
LOG_FILE = TOOL_ROOT / "logs" / "java2rsmod.log"

# ── Ollama settings ────────────────────────────────────────────────────────────

# Model to use.  Recommended options and approximate RAM requirements:
#   mistral          ~4-5 GB   ← default, good quality
#   llama3.2:3b      ~2-3 GB   ← lighter, faster, less accurate
#   codellama        ~4-5 GB   ← code-focused alternative
OLLAMA_MODEL: str = "hf.co/unsloth/Qwen3-4B-Instruct-2507-GGUF:Q4_K_M"

# Ollama server URL (default local install)
OLLAMA_BASE_URL: str = "http://localhost:11434"

# Request timeout in seconds (large files may need more time)
OLLAMA_TIMEOUT_SECONDS: int = 120

# ── Conversion settings ────────────────────────────────────────────────────────

# File extensions the tool will pick up from INPUT_DIR
ACCEPTED_EXTENSIONS: tuple[str, ...] = (".java",)

# If True, each run gets its own timestamped sub-folder under OUTPUT_DIR.
# If False, all output lands directly in OUTPUT_DIR (older files may be overwritten).
TIMESTAMPED_OUTPUT: bool = True

# ── Logging ────────────────────────────────────────────────────────────────────

# "DEBUG" | "INFO" | "WARNING" | "ERROR"
LOG_LEVEL: str = "INFO"

# Maximum size of a single log file before rotation (bytes)
LOG_MAX_BYTES: int = 1_000_000  # 1 MB

# Number of rotated log files to keep
LOG_BACKUP_COUNT: int = 3
