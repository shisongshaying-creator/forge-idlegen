package net.minecraft.client.gui.components;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractButton extends AbstractWidget {
    protected static final int TEXT_MARGIN = 2;
    protected static final WidgetSprites SPRITES = new WidgetSprites(
        ResourceLocation.withDefaultNamespace("widget/button"),
        ResourceLocation.withDefaultNamespace("widget/button_disabled"),
        ResourceLocation.withDefaultNamespace("widget/button_highlighted")
    );

    public AbstractButton(int p_93365_, int p_93366_, int p_93367_, int p_93368_, Component p_93369_) {
        super(p_93365_, p_93366_, p_93367_, p_93368_, p_93369_);
    }

    public abstract void onPress(InputWithModifiers p_428560_);

    @Override
    protected void renderWidget(GuiGraphics p_281670_, int p_282682_, int p_281714_, float p_282542_) {
        Minecraft minecraft = Minecraft.getInstance();
        p_281670_.blitSprite(
            RenderPipelines.GUI_TEXTURED,
            SPRITES.get(this.active, this.isHoveredOrFocused()),
            this.getX(),
            this.getY(),
            this.getWidth(),
            this.getHeight(),
            ARGB.white(this.alpha)
        );
        int i = getFGColor();
        this.renderString(p_281670_, minecraft.font, i);
        if (this.isHovered()) {
            p_281670_.requestCursor(this.isActive() ? CursorTypes.POINTING_HAND : CursorTypes.NOT_ALLOWED);
        }
    }

    public void renderString(GuiGraphics p_283366_, Font p_283054_, int p_281656_) {
        this.renderScrollingString(p_283366_, p_283054_, 2, p_281656_);
    }

    @Override
    public void onClick(MouseButtonEvent p_426095_, boolean p_428686_) {
        this.onPress(p_426095_);
    }

    @Override
    public boolean keyPressed(KeyEvent p_427564_) {
        if (!this.isActive()) {
            return false;
        } else if (p_427564_.isSelection()) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.onPress(p_427564_);
            return true;
        } else {
            return false;
        }
    }
}
