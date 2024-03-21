package com.jackdaw.chatwithnpc.npc;

import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VillagerNPCEntity extends NPCEntity {

    /**
     * This is a constructor used to initialize the NPC with the entity.
     *
     * @param entity The entity of the NPC.
     */
    public VillagerNPCEntity(@NotNull VillagerEntity entity) {
        super(entity);
    }

    @Override
    public void doAction(@NotNull Actions action, PlayerEntity player) {
        switch (action) {
            case WALK_TO_PLAYER -> walkToPlayer(player);
            case ESCAPE -> escape();
            case SHAKE_HEAD -> shakeHead();
            case FEEL_HAPPY -> feelHappy();
        }
    }

    private void feelHappy() {
        VillagerEntity villager = (VillagerEntity) this.entity;
        ServerWorld world = (ServerWorld) villager.world;
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            double x = villager.getX();
            double y = villager.getY() + villager.getHeight() + 0.5;
            double z = villager.getZ();
            world.spawnParticles(ParticleTypes.HEART, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }, 0, 500, TimeUnit.MILLISECONDS);
        ScheduledExecutorService scheduledExecutorService1 = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService1.schedule(() -> {
            scheduledExecutorService.shutdown();
            scheduledExecutorService1.shutdown();
        }, 5, TimeUnit.SECONDS);
    }


    private void shakeHead() {
        VillagerEntity villager = (VillagerEntity) this.entity;
        villager.setHeadRollingTimeLeft(60);
    }

    private void escape() {
        MobEntity mobEntity = (MobEntity) this.entity;
        Vec3d vec3d = NoPenaltyTargeting.find((PathAwareEntity) this.entity, 5, 4);
        if (vec3d == null) return;
        double targetX = vec3d.x;
        double targetY = vec3d.y;
        double targetZ = vec3d.z;
        mobEntity.getNavigation().startMovingTo(targetX, targetY, targetZ, mobEntity.getMovementSpeed() + 1);
    }

    private void walkToPlayer(PlayerEntity player) {
        MobEntity mobEntity = (MobEntity) this.entity;
        mobEntity.getNavigation().startMovingTo(player, mobEntity.getMovementSpeed());
    }
}
