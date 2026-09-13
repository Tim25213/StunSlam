# ShieldBreaker

Client-side Fabric mod for Minecraft 26.2.

While the local player is airborne and carrying a mace, ShieldBreaker presses the
hotbar key bound to an axe and drives the attack key at a randomized CPS until the
target's shield drops out of a block. What happens next depends on the mode.

The mod never touches look direction, camera, rotation or target selection. It only
reacts to the local player's own movement state.

## Modes

**Manual** (default) — axe only. Once the shield is disabled the mod keeps clicking for
the stun-tail duration at the reduced stun CPS, then restores the original hotbar slot.
The mace slot is never selected.

**Automatic** — the full axe, sword, mace attribute swap. The axe phase runs as above;
the moment the shield drops the mod swaps to a sword and lands one attack, then swaps
to the mace and lands one attack, then restores the original hotbar slot. The stun-tail
settings are not used in this mode.

Either mode stops immediately, with no trailing clicks, if the player lands or stops
carrying a mace. Automatic mode needs the sword and mace in the hotbar to swap to them;
a missing sword is skipped and a missing mace ends the sequence.

## Requirements

- Minecraft 26.2
- Fabric Loader 0.19.5+
- Fabric API 0.160.0+26.2
- Java 25

## Building

```
./gradlew build
```

The mod jar lands in `build/libs/`.

## Commands

| Command | Effect |
| --- | --- |
| `/shieldbreaker toggle` | Enable or disable the mod |
| `/shieldbreaker mode <manual\|automatic>` | Axe only, or the axe-sword-mace combo |
| `/shieldbreaker cps <min> <max>` | Attack CPS range while swapped to the axe |
| `/shieldbreaker stuncps <min> <max>` | Attack CPS range during the post-stun tail (manual mode) |
| `/shieldbreaker stunduration <seconds>` | Length of the post-stun tail (manual mode) |
| `/shieldbreaker counter left <on\|off>` | Show or hide the left-click CPS readout |
| `/shieldbreaker counter right <on\|off>` | Show or hide the right-click CPS readout |
| `/shieldbreaker counter pos <x> <y>` | Absolute screen position |
| `/shieldbreaker counter move <up\|down\|left\|right> <pixels>` | Nudge the counter |
| `/shieldbreaker counter size <1-10>` | Counter scale, 1 smallest, 10 largest |

Settings are written to `config/shieldbreaker.json` on every change.

## CPS counter

Manual and automated clicks are tracked separately for both mouse buttons. The counter
shows the mod's rate while the mod is clicking and the player's own rate otherwise.
The active source is marked by a tag and a colour:

- `YOU` in green — the player's own clicks
- `SB` in orange — the mod's clicks

## Defaults

| Setting | Default |
| --- | --- |
| Mode | manual |
| Swap CPS | 10.0 – 15.0 |
| Stun-tail CPS | 8.0 – 10.0 |
| Stun-tail duration | 1.0s |
| Left counter | on |
| Right counter | off |
| Position | 4, 4 |
| Size | 5 |
