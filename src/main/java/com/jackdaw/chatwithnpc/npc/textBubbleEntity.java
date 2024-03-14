package com.jackdaw.chatwithnpc.npc;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.minecraft.entity.data.TrackedData;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.text.Text.Serializer;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;



public class textBubbleEntity extends TextDisplayEntity {

    private boolean DEBUG = true;
    
    public textBubbleEntity(EntityType<? extends TextDisplayEntity> entityType, World world) {
        super(entityType, world);
    }

    protected boolean canStartRiding(Entity entity) {
        return true;
    }

    public void display(Entity speaker, String message) {

        this.refreshPositionAndAngles(speaker.getX(), speaker.getY() + speaker.getHeight() * 1.25f, speaker.getZ(), 0, 0);
        speaker.world.spawnEntity(this);
        this.startRiding(speaker, true);
        NbtCompound nbtData = this.writeNbt(new NbtCompound());
        nbtData.putString("text", Text.Serializer.toJson(Text.of(message)));
        nbtData.putString("billboard", "center");
        this.readNbt(nbtData);
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(() -> {
            this.discard();
            executorService.shutdown();
        }, 5, TimeUnit.SECONDS);
        if (DEBUG) {
            // Debug print this's nbt in plain text
            System.out.println(nbtData.toString());
        }
    }
    
}
