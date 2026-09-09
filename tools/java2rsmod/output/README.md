# java2rsmod Output

Converted Kotlin files and conversion notes from the Elvarg-to-RSMod converter.

## Structure

```
output/
├── 2026-09-08_1330/          # Timestamped runs (one per conversion batch)
│   ├── Slayer.kt
│   └── Slayer_notes.md
├── by-type/                  # Organized by conversion type (manual)
│   ├── enums/                # Converted enum classes
│   ├── data-classes/         # Converted data classes
│   └── plugins/              # Converted PluginScripts
└── reviewed/                 # Files that have been manually reviewed
```

## File Types

| File | Contents |
|------|----------|
| `{Name}.kt` | Kotlin skeleton — correct structure, `// TODO:` where review needed |
| `{Name}_notes.md` | Conversion notes: kept, replaced, needs manual work |

## Workflow

1. Run `python java2rsmod.py` to generate timestamped output
2. Review `.kt` files for correctness
3. Move reviewed files to `reviewed/`
4. Categorize by type in `by-type/` for organization
