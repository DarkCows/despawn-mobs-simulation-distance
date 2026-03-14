package darkcows.despawndistance.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;
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

    /**
     * Discard hostile mob entity
     */
    @Inject(
            method = "checkDespawn",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/SpawnGroup;getDespawnStartRange()I")
    )
    private void despawnPastSimulationDistance(CallbackInfo ci, @Local Entity entity) {
        // get the simulation distance
        final MinecraftServer minecraftServer = entity.getEntityWorld().getServer();
        if (minecraftServer == null) return;
        final int simulationDistance = minecraftServer.getPlayerManager().getSimulationDistance();

        // calculate despawn radius based on simulation distance
        final int maxDespawnDistance = simulationDistance * 16;

        // get the distance between player and mob
        final double groundSquareDistance = groundSquareDistanceTo(this.getEntityPos(), entity.getEntityPos());

        // remove mob if it is outside of simulation distance radius
        if (groundSquareDistance > maxDespawnDistance * maxDespawnDistance && this.canImmediatelyDespawn(groundSquareDistance)) this.discard();
    }

    /**
     * gets the distance squared between two entities not counting elevation/y distance
     */
    @Unique
    private double groundSquareDistanceTo(Vec3d entityPos1, Vec3d entityPos2) {
        final double x = entityPos2.x - entityPos1.x;
        final double z = entityPos2.z - entityPos1.z;
        return x * x + z * z;
    }
}
