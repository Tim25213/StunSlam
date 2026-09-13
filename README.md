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

**Automatic** — the full axe, sword, mace attribute swap. The axe phase runs as above.
When the shield drops the mod swap-attacks to a sword, waits for the sword's attack
cooldown to charge past the strong-attack threshold, then swap-attacks to the mace and
restores the original hotbar slot. The stun-tail settings are not used in this mode.

### How the attribute swap works

`Minecraft.tick()` runs `handleKeybinds()` before `tickEntities()`. The hotbar key and
the attack key are both consumed inside that one `handleKeybinds()` call, and inside it
`Inventory.setSelectedSlot` runs before `startAttack()`. Attack damage is scaled by
`getCurrentItemAttackStrengthDelay()`, which is `20.0 / getAttributeValue(ATTACK_SPEED)`
— a cached attribute that is only refreshed later, by `detectEquipmentUpdates()` during
`tickEntities()`.

So an attack issued in the same tick as the slot change is resolved with the previous
item's attack speed and the new item's stack: the mace hit lands on the sword's much
shorter cooldown. `MultiPlayerGameMode.attack()` calls `ensureHasSentCarriedItem()`
before sending the attack packet, so the server sees the same ordering.

The mod presses the hotbar key and the attack key in the same client tick to hit that
window. Pressing them a tick apart is an ordinary swap and hit, because `Player.tick()`
calls `resetAttackStrengthTicker()` as soon as it notices the held item changed.

### Version warning

Attribute swapping was removed in 26.2-pre2 and re-added in 26.2-pre3 "with the
intention of fixing it in a future version". It works on 26.2 release. Expect it to stop
working on a later version, at which point automatic mode degrades to a plain swap and
hit while manual mode is unaffected.

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
