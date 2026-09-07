# Changelog

All notable changes to this project will be documented in this file.

Format based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added
- Thieving skill: pickpocketing men/women in Lumbridge (`content/skills/thieving/`)
  - Pickpocket via opNpc3 on `content.person` NPCs (man, man2, man3, man_indoor, woman, woman2, woman3)
  - Level 1 requirement, 8 XP per pickpocket
  - OSRS-accurate success rates (`low=180, high=240`, ~70.7% at level 1, ~94.1% at level 99)
  - 3-9 coins loot per successful pickpocket
  - Stun + 1 HP damage on failure (1 tick stun)
  - Full inventory check
  - Module: `ThievingModule`, `ThievingLevelBoosts`, `PickpocketNpcEditor`, `Pickpocket`
  - Tests: 10/10 passing (`ThievingConfigTest`, `PickpocketTest`)

### Fixed
- Removed `build.gradle.kts` from `_template` directories (bosses, npcs, skills) to fix Gradle project naming convention errors

### Known Issues
- ALL integration tests fail with `RuntimeException` at `GameServer.kt:229` due to `content.fletching_knife` reference in `BaseContent.kt` that is not a valid item name in the `.sym` file
  - Error: "The following references use names that are not defined in a .sym file (1 found) - Name: fletching_knife"
  - This is a type verifier issue: `ContentReferences.find()` creates a `ContentGroupType` but the verifier validates it against the item `.sym` file
  - Blocks ALL skills' integration tests (fletching, thieving, woodcutting, cooking, firemaking, fishing)

## [0.1.0] - 2026-09-06

### Added
- Initial RSMod server setup (rev 233)
- RSProx client proxy integration
- Cache extraction and XTEA configuration
- Development documentation (`DEVELOPMENT.md`, `SETUP.md`)
- Content module templates (skills, bosses, NPCs)
- Git repository initialization with comprehensive `.gitignore`
