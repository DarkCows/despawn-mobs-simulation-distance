# Despawn Mobs Simulation Distance
Allows mobs to despawn with simulation distances under 8.

## Explanation
Mobs despawn when they are 128 blocks away from the player. However, with
simulation distances below 8, the mobs are unloaded before they are further
than the despawn range. This causes mobs to accumulate in unloaded chunks
and fill up the mob cap

This mod causes despawn before being unloaded for simulation distances below 8.
It does not affect the vertical 128 block despawn range, meaning that mob farms
will still work as intended. You can think of the despawn radius with this mod
shaped like a capsule.