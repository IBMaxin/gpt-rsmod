"""
analysis.py — Rule-based pre-analysis of Java files.

Scans Elvarg/RSPS Java source for known API patterns and classifies
the file type. No network calls — instant and free.
"""

from __future__ import annotations

import re
import logging
from typing import NamedTuple


logger = logging.getLogger("java2rsmod")


class FileAnalysis(NamedTuple):
    """Result of the fast rule-based pre-analysis step."""
    filename: str
    is_enum: bool
    is_data_holder: bool
    has_npc_interaction: bool
    has_item_interaction: bool
    has_loc_interaction: bool
    has_death_handler: bool
    elvarg_apis_found: list[str]
    suggested_output_type: str


# Elvarg API calls that have NO direct rsmod equivalent — must be flagged
# Ordered by frequency across the 228-file input codebase (most common first)
ELVARG_API_PATTERNS: dict[str, str] = {
    # ── Top 10 (highest frequency) ─────────────────────────────────────────
    r"performAnimation\(": "performAnimation() → use anim(seqs.name)",
    r"CombatSpecial\.": "CombatSpecial.* → use SpecialAttackMap + MeleeSpecialAttack",
    r"performGraphic\(": "performGraphic() → use spotanim(spotanims.name, height = 96)",
    r"getAsPlayer\(\)": "getAsPlayer() → use Kotlin 'as Player' type cast",
    r"getPacketSender\(\)": "getPacketSender() → use mes() / ifSetText() / ifClose()",
    r"getLocation\(\)": "getLocation() → use player.coords (CoordGrid)",
    r"isPlayer\(\)": "isPlayer() → use Kotlin 'is Player' type check",
    r"sendMessage\(": "sendMessage() → use mes('text')",
    r"getSkillManager\(\)": "getSkillManager() → use stat(stats.*) / statAdvance(stats.*) / statRandom(stats.*)",
    r"getInventory\(\)": "getInventory() → use invAdd(inv, objs.*) / invDel(inv, objs.*) / invTotal(inv, objs.*)",
    # ── Next 10 (high frequency) ───────────────────────────────────────────
    r"isNpc\(\)": "isNpc() → use Kotlin 'is Npc' type check",
    r"getTimers\(\)": "getTimers() → use timer(timers.*, cycles) or softTimer(timers.*, cycles)",
    r"TimerKey\.": "TimerKey.* → use timers.* (TimerType references)",
    r"Boundary": "Boundary → use isWithinArea(CoordGrid(...), CoordGrid(...))",
    r"getAsNpc\(\)": "getAsNpc() → use Kotlin 'as Npc' type cast",
    r"sendString\(": "sendString(id, text) → use ifSetText(component, text)",
    r"getHitpoints\(\)": "getHitpoints() → use stat(stats.hitpoints)",
    r"\.contains\(": ".contains() → use invTotal(inv, objs.*) > 0",
    r"SoundManager\.sendSound": "SoundManager.sendSound() → use soundSynth(synths.*)",
    # ── Supporting patterns ─────────────────────────────────────────────────
    r"\.delete\(": ".delete() → use invDel(inv, objs.*, count)",
    r"sendInterfaceRemoval\(": "sendInterfaceRemoval() → use ifClose()",
    r"getArea\(\)": "getArea() → use isWithinArea() for position checks",
    r"sendWalkableInterface\(": "sendWalkableInterface() → use ifSetWalkable(component)",
    r"PlayerRights": "PlayerRights → check player rights via existing RSMod system",
    r"addExperience\(": "addExperience() → use statAdvance(stats.*, xp)",
    r"Misc\.getRandom": "Misc.getRandom() → use GameRandom or Random.Default",
    r"Misc\.inclusive": "Misc.inclusive() → use (low..high).random()",
    r"Misc\.ucFirst": "Misc.ucFirst() → use Kotlin .replaceFirstChar { it.uppercase() }",
    r"com\.elvarg\.":  "com.elvarg.* import → remove entirely, no rsmod equivalent",
    r"setHitpoints\(": "setHitpoints() → use stat(stats.hitpoints) with statAdvance or direct set",
    r"getDialogueManager\(\)": "getDialogueManager() → use rsmod dialogue system",
    r"stopSkillable\(\)": "stopSkillable() → cancel current action (context-dependent)",
    r"NpcIdentifiers\.": "NpcIdentifiers.* → use rsmod NPC refs via find()",
}


def analyse_file(java_source: str, filename: str) -> FileAnalysis:
    """Perform a fast keyword scan to classify the Java file."""
    logger.debug("Pre-analysing %s", filename)

    is_enum = bool(re.search(r"\bpublic\s+enum\b", java_source))

    field_count = len(re.findall(r"private\s+\w+\s+\w+;", java_source))
    method_count = len(re.findall(r"(public|private|protected)\s+\w[\w<>\[\]]*\s+\w+\s*\(", java_source))
    is_data_holder = (
        not is_enum
        and field_count >= 1
        and method_count <= field_count * 3
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
    for pattern, description in ELVARG_API_PATTERNS.items():
        if re.search(pattern, java_source):
            elvarg_apis_found.append(description)

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
