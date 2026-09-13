# ShieldBreaker

Client-side Fabric mod for Minecraft 26.2.

While the local player is airborne and carrying a mace, ShieldBreaker presses the
hotbar key bound to an axe and drives the attack key at a randomized CPS. When the
target's shield drops out of a block, it keeps clicking briefly at a lower rate and
then restores the original hotbar slot.

The mod never touches look direction, camera, rotation or target selection, and never
selects the mace slot. It only reacts to the local player's own movement state.

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
| `/shieldbreaker cps <min> <max>` | Attack CPS range while swapped to the axe |
| `/shieldbreaker stuncps <min> <max>` | Attack CPS range during the post-stun tail |
| `/shieldbreaker stunduration <seconds>` | Length of the post-stun tail |
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
| Swap CPS | 10.0 – 15.0 |
| Stun-tail CPS | 8.0 – 10.0 |
| Stun-tail duration | 1.0s |
| Left counter | on |
| Right counter | off |
| Position | 4, 4 |
| Size | 5 |
