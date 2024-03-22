package com.jackdaw.chatwithnpc.npc.npcentity;

import com.jackdaw.chatwithnpc.npc.NPCEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LivingNPCEntity extends NPCEntity {
    /**
     * This is a constructor used to initialize the NPC with the entity.
     *
     * @param entity The entity of the NPC.
     */
    public LivingNPCEntity(@NotNull LivingEntity entity) {
        super(entity);
    }

    @Override
    public void doAction(@NotNull Actions action, PlayerEntity player) {
        switch (action) {
            case WALK_TO_PLAYER -> walkToPlayer(player);
            case ESCAPE -> escape();
            case FEEL_HAPPY -> feelHappy();
            default -> {}
        }
    }

    protected void feelHappy() {
        LivingEntity livingEntity = (LivingEntity) this.entity;
        ServerWorld world = (ServerWorld) livingEntity.world;
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            double x = livingEntity.getX();
            double y = livingEntity.getY() + livingEntity.getHeight() + 0.5;
            double z = livingEntity.getZ();
            world.spawnParticles(ParticleTypes.HEART, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }, 0, 500, TimeUnit.MILLISECONDS);
        ScheduledExecutorService scheduledExecutorService1 = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService1.schedule(() -> {
            scheduledExecutorService.shutdown();
            scheduledExecutorService1.shutdown();
        }, 5, TimeUnit.SECONDS);
    }

    protected void escape() {
        PathAwareEntity mobEntity = (PathAwareEntity) entity;
        Random random = mobEntity.getRandom();
        BlockPos blockPos = mobEntity.getBlockPos();
        Vec3d vec3d = null;
        for(int i = 0; i < 10; ++i) {
            BlockPos blockPos2 = blockPos.add(random.nextInt(20) - 10, random.nextInt(6) - 3, random.nextInt(20) - 10);
            if (mobEntity.getPathfindingFavor(blockPos2) < 0.0F) {
                vec3d = Vec3d.ofBottomCenter(blockPos2);
                break;
            }
        }
        if (vec3d == null) return;
        double targetX = vec3d.x;
        double targetY = vec3d.y;
        double targetZ = vec3d.z;
        mobEntity.getNavigation().startMovingTo(targetX, targetY, targetZ, mobEntity.getMovementSpeed() + 1);
    }

    protected void walkToPlayer(PlayerEntity player) {
        MobEntity mobEntity = (MobEntity) this.entity;
        mobEntity.getNavigation().startMovingTo(player, mobEntity.getMovementSpeed() + 1);
    }

}
