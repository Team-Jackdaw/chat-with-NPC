package com.jackdaw.chatwithnpc.npc;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;



public class TextBubbleEntity extends TextDisplayEntity {

    private final Entity speaker;
    private final double heightOffset = 0.55D;
    private long lastUpdateTime;        // In milliseconds.
    private long timeLastingPerChar;    // In milliseconds.
    private long bubbleLastingTime;     // In milliseconds.
    private TextBackgroundColor textBackgroundColor;

    public TextBubbleEntity(@NotNull Entity speaker) {
        super(EntityType.TEXT_DISPLAY, speaker.getWorld());
        this.speaker = speaker;
        this.setPosition(speaker.getX(), speaker.getY() + speaker.getHeight() + heightOffset, speaker.getZ());
        this.lastUpdateTime = System.currentTimeMillis();
        this.timeLastingPerChar = 500L;
        this.textBackgroundColor = TextBackgroundColor.DEFAULT;
        this.bubbleLastingTime = 0;
        speaker.getWorld().spawnEntity(this);
        ServerChunkEvents.CHUNK_UNLOAD.register(this::onChunkUnload);
    }

    @Override
    public void tick() {
        super.tick();
        this.setPosition(speaker.getX(), speaker.getY() + speaker.getHeight() + heightOffset, speaker.getZ());
        updateNbtSeeThrough();
        if (System.currentTimeMillis() - lastUpdateTime > bubbleLastingTime) {
            this.remove(RemovalReason.DISCARDED);
        }
        if (this.speaker.isRemoved()) {
            this.remove(RemovalReason.DISCARDED);
        }
    }

    private void updateNbtSeeThrough() {
        NbtWriteView writeView = NbtWriteView.create(ErrorReporter.EMPTY);
        this.writeData(writeView);
        writeView.putBoolean("see_through", this.isSeeThroughBlock());
        this.readData((ReadView) writeView);
    }

    private void onChunkUnload(ServerWorld world, WorldChunk chunk) {
        if (chunk.getPos().equals(this.getChunkPos()) && world.equals(this.getWorld())) {
            this.remove(RemovalReason.DISCARDED);
        }
    }

    public void setTimeLastingPerChar(long timeLastingPerChar) {
        this.timeLastingPerChar = timeLastingPerChar;
    }

    public void setTextBackgroundColor(TextBackgroundColor textBackgroundColor) {
        this.textBackgroundColor = textBackgroundColor;
    }

    public void update(String message) {
        updateAllNbt(message);
        bubbleLastingTime = bubbleLastingTime(message);
        lastUpdateTime = System.currentTimeMillis();
    }

    private void updateAllNbt(String message) {
        NbtWriteView writeView = NbtWriteView.create(ErrorReporter.EMPTY);
        this.writeData(writeView);
        writeView.putByte("text_opacity", (byte) -1);
        writeView.putString("text", textBuilder(message, textBackgroundColor).getString());

        writeView.putString("billboard", "center");
        writeView.putBoolean("see_through", this.isSeeThroughBlock());
        writeView.putLong("background", textBackgroundColor.getBackgroundARGBAsLong());
        this.readData((ReadView) writeView);
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

    private boolean isSeeThroughBlock() {
        int checkingRadius = 1;
        World world = this.getWorld();
        BlockPos pos = this.getBlockPos();
        for (int x = -checkingRadius; x <= checkingRadius; x++) {
            for (int y = 0; y <= checkingRadius; y++) {
                for (int z = -checkingRadius; z <= checkingRadius; z++) {
                    if(world.getBlockState(pos.add(x, y, z)).isOpaque()) return true;
                }
            }
        }
        return false;
    }

    public enum TextBackgroundColor{

        /**
         * RGB can be represented by Integer.
         * ARGB must represent by Long.
         */
        DEFAULT ("69C8FF", "FF160C0E"),
        DAY ("000000", "FFe0e0e0"),
        NIGHT("FFFFFF", "FF202020"),
        MATRIX("00d643", "FF0a0a0a"),
        FERN ("784884", "FF201f22"),
        ABBA ("091972", "FF0ABBA0"),
        SAKURANIGHT ("FEACAD", "FF1A153D"),
        SAKURADAY ("f9316d", "FFfed9d5"),
        MISTYBLUE ("001532", "FFa0afb7"),
        UCL ("FF9933", "FF000000"),
        TUB ("FFFFFF", "FFC61521"),
        KTH ("FFFFFF", "FF2258A5");
    
        private final String textRGB;
        private final String backgroundARGB;
    
        TextBackgroundColor(String textRGB, String backgroundARGB){
            this.textRGB = textRGB;
            this.backgroundARGB = backgroundARGB;
        }
        
        public int getTextRGBAsInt(){
            return Integer.parseInt(textRGB, 16);
        }
    
        public long getBackgroundARGBAsLong(){
            return Long.parseLong(backgroundARGB, 16);
        }
    
    }

}