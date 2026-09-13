# ShieldBreaker

Client-side Fabric mod for Minecraft 26.2.

ShieldBreaker reacts to the local player's own state. It engages only while the player
is **airborne, moving downward, and already aiming at a living entity within a set
distance** — so a plain jump, a wind charge or eating a gapple never triggers it.

It never touches look direction, camera, rotation or target selection. The target is read
from the crosshair the player is already pointing; the mod never picks or changes it.

## Modes

**Manual** (default) — axe only. It swaps to an axe and attacks until the target's shield
drops, then restores the original hotbar slot immediately so the mace hit is yours to
land. Set `stunduration` above 0 if you want it to keep clicking after the shield breaks.

**Automatic** — the full attribute-swap chain. Axe until the shield drops, then a
same-tick swap-attack to the sword, then to the mace, then the original slot is restored.
With `spear on` and a spear in the hotbar, a spear swap-attack opens the chain when the
target is more than 3 blocks away.

Either mode stops immediately if the player lands or stops carrying a mace. Attacks are
only issued while the crosshair is on the target — see below for why that matters.

### Why it never swings at air

`Minecraft.startAttack()` sets `missTime = 10` whenever the attack lands on nothing. For
those 10 ticks every further attack returns immediately, so clicking into empty air does
not just waste a click, it blocks the hits that follow. The mod therefore issues an
attack only when `crosshairPickEntity` is a living entity inside `distance`.

### How the attribute swap works

`Minecraft.tick()` runs `handleKeybinds()` before `tickEntities()`. The hotbar key and
the attack key are both consumed inside that one `handleKeybinds()` call, and inside it
`Inventory.setSelectedSlot` runs before `startAttack()`. Attack damage is scaled by
`getCurrentItemAttackStrengthDelay()`, which is `20.0 / getAttributeValue(ATTACK_SPEED)`
— a cached attribute that is only refreshed later, by `detectEquipmentUpdates()` during
`tickEntities()`.

So an attack issued in the same tick as the slot change is resolved with the previous
item's attack speed and the new item's stack. `MultiPlayerGameMode.attack()` calls
`ensureHasSentCarriedItem()` before sending the attack packet, so the server sees the
same ordering. The Attribute Swap Indicator mod judges a swap successful with exactly
this rule: `hotbarTick() == addTick() && attackTick() == addTick()`.

`charge on` makes the mace step wait until the sword's cooldown passes the strong-attack
threshold, which hits harder but costs about half a second — usually longer than a fall
allows, so it is off by default.

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
- Fabric Loader 0.18.4+
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
| `/shieldbreaker distance <blocks>` | How close the target must be to engage |
| `/shieldbreaker spear <on\|off>` | Open the automatic chain with a spear |
| `/shieldbreaker charge <on\|off>` | Wait for full charge before the mace hit |
| `/shieldbreaker counter left <on\|off>` | Show or hide the left-click CPS readout |
| `/shieldbreaker counter right <on\|off>` | Show or hide the right-click CPS readout |
| `/shieldbreaker counter pos <x> <y>` | Absolute screen position |
| `/shieldbreaker counter move <up\|down\|left\|right> <pixels>` | Nudge the counter |
| `/shieldbreaker counter size <1-10>` | Counter scale, 1 smallest, 10 largest |

Settings are written to `config/shieldbreaker.json` on every change.

## CPS counter

Plain clicks per second over the last second, counting every click whatever its source.
With both buttons enabled it reads `3|2 cps`, left then right; with one enabled it reads
`3 cps`.

## Defaults

| Setting | Default |
| --- | --- |
| Mode | manual |
| Trigger distance | 5.0 blocks |
| Spear opener | off |
| Charge before mace | off |
| Swap CPS | 10.0 – 15.0 |
| Stun-tail CPS | 8.0 – 10.0 |
| Stun-tail duration | 0.0s |
| Left counter | on |
| Right counter | off |
| Position | 4, 4 |
| Size | 5 |
