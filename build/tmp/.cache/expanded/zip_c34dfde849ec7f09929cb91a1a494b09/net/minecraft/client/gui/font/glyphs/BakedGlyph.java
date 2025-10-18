package net.minecraft.client.gui.font.glyphs;

import com.mojang.blaze3d.font.GlyphInfo;
import javax.annotation.Nullable;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.network.chat.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface BakedGlyph {
    GlyphInfo info();

    @Nullable
    TextRenderable createGlyph(float p_422607_, float p_429324_, int p_424269_, int p_430880_, Style p_430213_, float p_423403_, float p_428283_);
}