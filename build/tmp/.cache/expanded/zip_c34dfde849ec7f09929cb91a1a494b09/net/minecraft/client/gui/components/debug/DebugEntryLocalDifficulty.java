package net.minecraft.client.gui.components.debug;

import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DebugEntryLocalDifficulty implements DebugScreenEntry {
    @Override
    public void display(DebugScreenDisplayer p_422323_, @Nullable Level p_426366_, @Nullable LevelChunk p_423491_, @Nullable LevelChunk p_431288_) {
        Minecraft minecraft = Minecraft.getInstance();
        Entity entity = minecraft.getCameraEntity();
        if (entity != null && minecraft.level != null && p_431288_ != null && p_426366_ != null) {
            BlockPos blockpos = entity.blockPosition();
            if (minecraft.level.isInsideBuildHeight(blockpos.getY())) {
                float f = p_426366_.getMoonBrightness();
                long i = p_431288_.getInhabitedTime();
                DifficultyInstance difficultyinstance = new DifficultyInstance(p_426366_.getDifficulty(), p_426366_.getDayTime(), i, f);
                p_422323_.addLine(
                    String.format(
                        Locale.ROOT,
                        "Local Difficulty: %.2f // %.2f (Day %d)",
                        difficultyinstance.getEffectiveDifficulty(),
                        difficultyinstance.getSpecialMultiplier(),
                        minecraft.level.getDayTime() / 24000L
                    )
                );
            }
        }
    }
}