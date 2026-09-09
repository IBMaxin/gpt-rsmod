"""
prompt.py — Assemble the full prompt for the AI model.
"""

from __future__ import annotations

import textwrap

from analysis import FileAnalysis


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
