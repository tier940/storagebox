# StorageBox

A portable, single-slot storage item that holds large quantities of one item type.

## Features

- **Storage**
  - Holds up to 65,536 items of a single type per box
  - Item count and LC (Large Chest equivalent) displayed in the GUI and tooltip
- **Right-click (air)** — Opens the GUI
  - IN slot: insert items into the box
  - OUT slot: withdraw one max-stack at a time
- **Sneak + Right-click** — Toggles auto-collect on/off
  - When enabled, the box automatically picks up matching items from the ground
- **Right-click (inventory block)** — Dumps all contents into the target inventory (chest, barrel, etc.)
- **Right-click (ground, block template)** — Places one block directly from the box
- **Sneak + Q** — Ejects one stack onto the ground; the box remains in hand

## Visuals

- When filled, the item icon changes to show the stored template item
- A small storage box icon is overlaid in the top-right corner of the slot as an indicator
- Enchantment glint is shown while the box is non-empty
- Tooltip shows the stored item, count, auto-collect state, and usage hints (hold Shift for details)
