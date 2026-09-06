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

## [0.1.0] - 2026-09-06

### Added
- Initial RSMod server setup (rev 233)
- RSProx client proxy integration
- Cache extraction and XTEA configuration
- Development documentation (`DEVELOPMENT.md`, `SETUP.md`)
- Content module templates (skills, bosses, NPCs)
- Git repository initialization with comprehensive `.gitignore`
