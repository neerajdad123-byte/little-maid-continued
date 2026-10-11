# Community Wishlist

Collected on 2026-10-11 from the 340 top-level comments on the original
[CurseForge page](https://www.curseforge.com/minecraft/mc-mods/touhou-little-maid/comments)
and the open [upstream GitHub issues](https://github.com/TartaricAcid/TouhouLittleMaid/issues).
Ordered by how often people ask for it. Status is for Little Maid Continued.

## Versions and platforms

| Request | How often | Status |
|---|---|---|
| Newer Minecraft (1.21.8, 1.21.10, 1.21.11, latest) on NeoForge | Very common | 🚧 Porting to MC 26.2 (NeoForge) |
| Fabric version | Very common (10+) | Planned after the NeoForge port works |
| Bedrock / Pocket Edition | Common | ❌ Not possible (Java mods don't run on Bedrock) |
| Old versions (1.16.5, 1.12.2, 1.7.10) | Common, mostly old comments | ❌ Out of scope |

## Bugs players hit

| Bug | Source | Status |
|---|---|---|
| Item duplication: items in maid hands + armor, then summon scroll and release | CurseForge | To fix |
| Maids disappear / become "ghosts" after placing or re-using the soul scroll | CurseForge, GitHub #1081, #1169, #1219 | To fix |
| Maid stuck "in unloaded chunks" even when standing next to the player | CurseForge | To investigate |
| Maid spams opening and closing doors | CurseForge | To fix |
| Maid attacks neutral mobs (endermen etc.) first and dies | CurseForge, GitHub #1065 | To fix (config option) |
| Maid attacks the player in snow biomes / shoots the player in the back | CurseForge | To investigate |
| Grave duplicates inventory when maid is saved in another mod's pet storage | CurseForge, GitHub #1188 | Config: disable graves |
| Maids only farm when the player is nearby, even in chunk-loaded areas | CurseForge | To investigate |
| Maids sit at the kotatsu forever | CurseForge | To fix |
| Eating food at full inventory throws away items / food | GitHub #986, #1177 | To fix |
| Non-default color maid bed corrupts the save | GitHub #1206 | To fix |
| Crash when player leaves the End while maid stays | GitHub #1197 | To fix |
| Server-side crash: mixin loads client `RenderType` class | GitHub #1174, #1202 | To fix |
| Invalid regex in blacklist crashes the game | GitHub #1135 | To fix |
| Gemini models: missing `thought_signature` on tool calls | GitHub #1140, #1220 | To fix |
| Black text after downloading packs from the maid menu | CurseForge | To investigate |

## Features people want

| Feature | How often | Status |
|---|---|---|
| Mining task (mine ores nearby) | Common | Planned |
| Tree chopping / lumberjack task | Common | Planned |
| Option to disable friendly fire (can't hit your own maid) | Common | Planned |
| Option: don't attack neutral mobs first; adjustable aggro range | Common | Planned |
| Make the AI chat optional / lighter; one switch to turn it off | Common, often frustrated | Planned |
| Global attack list instead of per-maid lists | Some | Planned |
| Maid sneaks and stays quiet when the player sneaks (Warden) | Some | Planned |
| Volume slider for voice packs | Some | Planned |
| Let maids sleep in normal beds | Some | Planned |
| Dynamic lights: torch in maid's hand gives light | Some | Planned (compat) |
| English voice lines / English chat bubbles by default | Some | Investigate |
| Better performance, faster load with many models | Some | Planned |
| Toggle for fire extinguisher requirement | Few | Planned |
| Tame / pacify fairy maids | Few | Idea |
| Maids play with each other, head pats, feeding treats | Few | Idea |
| Player-vs-player board games | GitHub #1028, #1134 | Idea |
