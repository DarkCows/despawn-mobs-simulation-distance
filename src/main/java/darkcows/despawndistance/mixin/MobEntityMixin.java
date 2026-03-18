package darkcows.despawndistance.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends Entity {

    @Shadow
    public abstract boolean canImmediatelyDespawn(double distanceSquared);

    public MobEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    // idk why
    @Unique
    private static int cachedMaxDistanceSquared = -1;

    @Unique
    private static int cachedSimulationDistance = -1;

    /**
     * Discard hostile mob entity if it is outside of simulation distance radius
     */
    @Inject(
        method = "checkDespawn",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/SpawnGroup;getDespawnStartRange()I"
        )
    )
    private final void despawnPastSimulationDistance(
        CallbackInfo ci,
        @Local Entity entity
    ) {
        // get minecraft server
        final MinecraftServer minecraftServer = entity
            .getEntityWorld()
            .getServer();
        if (minecraftServer == null) return;

        // calculate difference of coords between player and mob
        final double dx = entity.getX() - this.getX();
        final double dz = entity.getZ() - this.getZ();

        // get the simulation distance
        final int simulationDistance = minecraftServer
            .getPlayerManager()
            .getSimulationDistance();

        // if simulation distance has changed, recalculate despawn radius
        if (simulationDistance != cachedSimulationDistance) {
            // calculate despawn radius based on simulation distance
            final int maxDespawnDistance = simulationDistance << 4;
            cachedSimulationDistance = simulationDistance;
            cachedMaxDistanceSquared = maxDespawnDistance * maxDespawnDistance;
        }

        // calculate the squared distance between player and mob
        final double groundSquareDistance = dx * dx + dz * dz;

        // remove mob if it is outside of simulation distance radius
        if (
            groundSquareDistance > cachedMaxDistanceSquared &&
            this.canImmediatelyDespawn(groundSquareDistance)
        ) this.discard();
    }
}
