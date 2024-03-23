package com.jackdaw.chatwithnpc.npc;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.SettingManager.TextBackgroundColor;



public class TextBubbleEntity extends TextDisplayEntity {

    private final Entity speaker;
    private final double textSizeY = 0.55D;
    private final TextBackgroundColor defaultColor = TextBackgroundColor.DEFAULT;
    private final long defaultTimePerChar = 500L;
    private long lastUpdateTime = 0L;
    private long timeLastingPerChar; // In milliseconds.
    private long bubbleLastingTime;
    private TextBackgroundColor textBackgroundColor;

    private List<String> defaultText = List.of("os: 今天是个上分的好日子", "os: 这个天气，真不戳", "os: 烧鸡翼，我中意食~~~");

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
        if (System.currentTimeMillis() - lastUpdateTime > bubbleLastingTime) {
            NbtCompound nbtData = this.writeNbt(new NbtCompound());
            String randomThough = defaultText.get(random.nextInt(defaultText.size()));
            nbtData.putString("text", Text.Serializer.toJson(
                textBuilder(randomThough, textBackgroundColor)));
            this.readNbt(nbtData);
            bubbleLastingTime = bubbleLastingTime(defaultText.get(random.nextInt(defaultText.size())));
            lastUpdateTime = System.currentTimeMillis();
        }
    }

    protected boolean glowing = true;

    public void update(String message) {
        this.update(message, defaultColor);
    }

    public void update(String message, TextBackgroundColor textBackgroundColor) {
        this.update(message, textBackgroundColor, defaultTimePerChar);
    }

    public void update(String message, TextBackgroundColor textBackgroundColor, long timeLastingPerChar) {
        NbtCompound nbtData = this.writeNbt(new NbtCompound());
        nbtData.putByte("text_opacity", (byte) -1);
        nbtData.putString("text", Text.Serializer.toJson(textBuilder(message, textBackgroundColor)));
        nbtData.putString("billboard", "center");
        nbtData.putBoolean("see_through", true);
        nbtData.putLong("background", textBackgroundColor.getBackgroundARGBAsLong());
        this.readNbt(nbtData);
        this.textBackgroundColor = textBackgroundColor;
        this.timeLastingPerChar = timeLastingPerChar;
        bubbleLastingTime = bubbleLastingTime(message);
        lastUpdateTime = System.currentTimeMillis();
    }

    private Text textBuilder(String message, TextBackgroundColor textBackgroundColor) {
        MutableText replyText = Text.of(message).copy();
        Style textStyle = Style.EMPTY.withColor(textBackgroundColor.getTextRGBAsInt());
        replyText.setStyle(textStyle);
        return replyText;
    }

    private long bubbleLastingTime(String message){
        return message.length() * this.timeLastingPerChar;
    }

}