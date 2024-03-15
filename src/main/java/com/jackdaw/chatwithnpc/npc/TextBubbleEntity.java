package com.jackdaw.chatwithnpc.npc;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;




public class TextBubbleEntity extends TextDisplayEntity {

    private final Entity speaker;
    private final double textSizeY = 0.55D;
    private long lastUpdateTime = 0L;
    private final long displayTimeOut = 10000L;
    
    public TextBubbleEntity(@NotNull Entity speaker) {
        super(EntityType.TEXT_DISPLAY, speaker.world);
        this.speaker = speaker;
        this.setPosition(speaker.getX(), speaker.getY() + speaker.getHeight() + textSizeY, speaker.getZ());
        speaker.world.spawnEntity(this);
    }

    @Override
    public void tick() {
        super.tick();
        this.setPosition(speaker.getX(), speaker.getY() + speaker.getHeight() + textSizeY, speaker.getZ());
        if (System.currentTimeMillis() - lastUpdateTime > displayTimeOut) {
            NbtCompound nbtData = this.writeNbt(new NbtCompound());
            nbtData.putByte("text_opacity", (byte) 10);
            nbtData.putInt("background", 0);
            this.readNbt(nbtData);
        }
    }

    public void update(String message) {
        NbtCompound nbtData = this.writeNbt(new NbtCompound());
        nbtData.putByte("text_opacity", (byte) -1);
        nbtData.putString("text", Text.Serializer.toJson(Text.of(message)));
        nbtData.putString("billboard", "center");
        nbtData.putBoolean("see_through", true);
        nbtData.putInt("background", 1073741824);
        this.readNbt(nbtData);
        lastUpdateTime = System.currentTimeMillis();
    }

}
