package net.minecraft.client.input;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record KeyEvent(int key, int scancode, int modifiers) implements InputWithModifiers {
    @Override
    public int input() {
        return this.key;
    }

    @Override
    public int modifiers() {
        return this.modifiers;
    }
}