# java2rsmod

Converts Elvarg/RSPS Java files into rsmod Kotlin skeletons using a local Ollama model.

## Requirements

- Python 3.11+
- [Ollama](https://ollama.com) installed and running locally
- At least one model pulled (see below)

## Setup

### 1. Install Python dependencies

From the `tools/java2rsmod/` folder in PowerShell:

```powershell
pip install -r requirements.txt
```

### 2. Pull an Ollama model

Open a separate PowerShell window and run:

```powershell
ollama serve
```

Then pull a model (pick one based on your available RAM):

```powershell
ollama pull mistral        # ~4-5 GB RAM — recommended
ollama pull llama3.2:3b    # ~2-3 GB RAM — lighter and faster
ollama pull codellama      # ~4-5 GB RAM — code-focused
```

### 3. Configure paths and model (optional)

Open `config.py` and check:
- `OLLAMA_MODEL` — change if you pulled a different model
- `OLLAMA_BASE_URL` — only change if Ollama runs on a different port
- `TIMESTAMPED_OUTPUT` — set to `False` if you prefer a flat output folder

## Usage

### Convert all files in input/

```powershell
cd C:\Users\bob\Desktop\gpt-rsmod\tools\java2rsmod
python java2rsmod.py
```

### Convert a specific file

```powershell
python java2rsmod.py Slayer.java
python java2rsmod.py input\PrayerHandler.java
```

### Override model for one run

```powershell
python java2rsmod.py --model codellama
```

### Verbose debug output

```powershell
python java2rsmod.py --log-level DEBUG
```

## Output

For every input file the tool creates two files inside `output/<timestamp>/`:

| File | Contents |
|------|----------|
| `{Name}.kt` | Kotlin skeleton — correct structure, `// TODO:` where review is needed |
| `{Name}_notes.md` | Conversion notes: what was kept, replaced, and needs manual work |

## Folder Structure

```
tools/java2rsmod/
├── java2rsmod.py          # Main script
├── config.py              # All configurable settings
├── requirements.txt       # Python dependencies
├── README.md              # This file
├── prompts/
│   └── conversion.txt     # Ollama prompt template (edit to tune output)
├── context/
│   └── AI_KNOWLEDGE_BASE.md  # RSMod API reference used in every prompt
├── input/
│   └── (drop .java files here)
├── output/
│   └── 2026-09-08_1330/   # One timestamped folder per run
│       ├── Slayer.kt
│       └── Slayer_notes.md
└── logs/
    └── java2rsmod.log     # Rotating log file
```

## Keeping the Knowledge Base Up to Date

The `context/AI_KNOWLEDGE_BASE.md` file is a copy of the repo-level
`AI_KNOWLEDGE_BASE.md`. When you update the main knowledge base, copy the new
version into `tools/java2rsmod/context/` so the tool stays in sync.

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `Could not connect to Ollama` | Run `ollama serve` in a separate terminal |
| `model not found` | Run `ollama pull <model>` to download it first |
| Empty `.kt` output | Try `--log-level DEBUG` and check `logs/java2rsmod.log` |
| Timeout error | Increase `OLLAMA_TIMEOUT_SECONDS` in `config.py`, or use a smaller model |
| Wrong output structure | Edit `prompts/conversion.txt` to tune the AI instructions |
