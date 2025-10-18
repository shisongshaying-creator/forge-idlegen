package net.minecraft.client.gui.components.debug;

import java.util.Collection;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface DebugScreenDisplayer {
    void addPriorityLine(String p_429600_);

    void addLine(String p_425268_);

    void addToGroup(ResourceLocation p_425311_, Collection<String> p_428004_);

    void addToGroup(ResourceLocation p_426696_, String p_427760_);
}