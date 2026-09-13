# ShieldBreaker

Client-side Fabric mod for Minecraft 26.2.

ShieldBreaker never clicks for you and never touches your aim. It hooks the attack you
already made and swaps your hotbar slot inside that same attack, so the hit lands with a
different item than the one you are holding.

When you attack a living entity:

- if the target is **blocking**, the mod swaps to an axe for that hit and swaps straight
  back, so your own click breaks the shield while you keep the mace selected
- in **automatic** mode, when the target is not blocking, it swaps to the mace (or a
  spear with `spear on`) for the hit and swaps back

You aim, you click, you decide when. The mod only chooses which item the hit resolves
with.

## How it works

`MultiPlayerGameMode.attack(Player, Entity)` runs only when your attack actually lands on
an entity. The mod injects at its head, cancels the original call, selects the swap slot
(`Inventory.setSelectedSlot` plus a `ServerboundSetCarriedItemPacket` so the server sees
the change first), re-invokes the real attack, then restores the slot — all synchronously
inside that one call, so the swap and the attack are guaranteed to share a tick.

That tick equality is what makes it an attribute swap. Attack damage is scaled by
`getCurrentItemAttackStrengthDelay()`, which is `20.0 / getAttributeValue(ATTACK_SPEED)`
— a cached attribute refreshed only later, by `detectEquipmentUpdates()` during
`tickEntities()`. So the hit resolves with the held item's attack speed and the swapped
item's stack. The Attribute Swap Indicator mod judges a swap successful with exactly this
rule: `hotbarTick() == addTick() && attackTick() == addTick()`.

Because the mod rides a real attack it never swings at air, which matters:
`Minecraft.startAttack()` sets `missTime = 10` on a miss, and for those ten ticks every
further attack returns immediately.

### Spear

`spear on` makes the damage swap use a spear instead of the mace. It does **not** extend
your reach: vanilla has already chosen the target before `attack` runs, so swapping at
that point changes the item the hit resolves with, not how far you can reach.

### Version warning

Attribute swapping was removed in 26.2-pre2 and re-added in 26.2-pre3 "with the intention
of fixing it in a future version". It works on 26.2 release. Expect it to stop working on
a later version, at which point the swap degrades to an ordinary item switch.

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
| `/shieldbreaker mode <manual\|automatic>` | Axe on blocking targets only, or also swap for damage |
| `/shieldbreaker spear <on\|off>` | Damage swap uses a spear instead of the mace |
| `/shieldbreaker counter left <on\|off>` | Show or hide the left-click CPS readout |
| `/shieldbreaker counter right <on\|off>` | Show or hide the right-click CPS readout |
| `/shieldbreaker counter pos <x> <y>` | Absolute screen position |
| `/shieldbreaker counter move <up\|down\|left\|right> <pixels>` | Nudge the counter |
| `/shieldbreaker counter size <1-10>` | Counter scale, 1 smallest, 10 largest |

Settings are written to `config/shieldbreaker.json` on every change.

## CPS counter

Plain clicks per second over the last second. With both buttons enabled it reads
`3|2 cps`, left then right; with one enabled it reads `3 cps`.

## Defaults

| Setting | Default |
| --- | --- |
| Mode | manual |
| Spear damage swap | off |
| Left counter | on |
| Right counter | off |
| Position | 4, 4 |
| Size | 5 |
