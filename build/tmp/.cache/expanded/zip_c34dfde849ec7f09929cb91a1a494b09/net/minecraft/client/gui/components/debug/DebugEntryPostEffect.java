package net.minecraft.client.gui.components.debug;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DebugEntryPostEffect implements DebugScreenEntry {
    @Override
    public void display(DebugScreenDisplayer p_424990_, @Nullable Level p_426587_, @Nullable LevelChunk p_431361_, @Nullable LevelChunk p_426455_) {
        Minecraft minecraft = Minecraft.getInstance();
        ResourceLocation resourcelocation = minecraft.gameRenderer.currentPostEffect();
        if (resourcelocation != null) {
            p_424990_.addLine("Post: " + resourcelocation);
        }
    }
}