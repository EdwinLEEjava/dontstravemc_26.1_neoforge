# Creature animation remake plan (2026-11-09)

Scope: butterfly, rabbit (ds_rabbit), mole (mole_worm), spider (monster_spider), pigman.
References: `E:\饥荒模组项目规划和内容\生物参考\<蝴蝶|兔子|鼹鼠|蜘蛛|猪人>\*.gif` (Don't Starve originals).

## Shared rules
1. **Minecraft logic, DST flavour.** Read each GIF frame-by-frame for timing, poses and character
   (squash, anticipation, follow-through), then express it as 3D bone rotation/position on the
   GeckoLib model. DST is a 2D side-view sprite: do not copy sprite flipping, scale-popping or
   camera-facing tricks. Walk/run loops must read from any angle, loop seamlessly (first key ==
   last key), and match in-game movement speed (no foot sliding). Head look (if code-driven) must
   not be overridden by keyframes on the same bone.
2. **Skeleton may change slightly** to serve the animation: add/re-pivot bones, split a part into
   a joint chain, move pivots to real joints. Keep cube geometry, UVs and texture unchanged unless
   a split strictly needs it; keep bone names that Java code references (grep before renaming).
3. **Delete every `hit` animation and its trigger logic**: the `hit` entry in animation.json,
   `HIT_ANIM` constants, synced hit-tick data, hurt-time branches in the controller and NBT for
   them. Keep hurt sounds, flee/aggro AI and vanilla red hurt flash.
4. **Keep animation names Java uses** (or update Java in the same change). `loop` values:
   loops `true`, one-shots `hold_on_last_frame` where the controller holds them.
5. Files are the GeckoLib JSON under `src/main/resources/assets/dontstravemc/geckolib/`.
   Edit them directly (scripted, validated JSON). **Do not drive Blockbench and do not run
   Gradle** — five agents run in parallel; the coordinator imports each result into Blockbench
   and builds once at the end.
6. Each agent only touches its own creature's files (butterfly agent additionally owns the
   registry/renderer registration edits it needs).
