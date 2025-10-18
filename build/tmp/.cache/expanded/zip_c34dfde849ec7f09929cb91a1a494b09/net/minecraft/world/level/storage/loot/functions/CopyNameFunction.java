package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class CopyNameFunction extends LootItemConditionalFunction {
    private static final ExtraCodecs.LateBoundIdMapper<String, CopyNameFunction.Source> SOURCES = new ExtraCodecs.LateBoundIdMapper<>();
    public static final MapCodec<CopyNameFunction> CODEC;
    private final CopyNameFunction.Source source;

    private CopyNameFunction(List<LootItemCondition> p_300985_, CopyNameFunction.Source p_422979_) {
        super(p_300985_);
        this.source = p_422979_;
    }

    @Override
    public LootItemFunctionType<CopyNameFunction> getType() {
        return LootItemFunctions.COPY_NAME;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(this.source.param);
    }

    @Override
    public ItemStack run(ItemStack p_80185_, LootContext p_80186_) {
        if (p_80186_.getOptionalParameter(this.source.param) instanceof Nameable nameable) {
            p_80185_.set(DataComponents.CUSTOM_NAME, nameable.getCustomName());
        }

        return p_80185_;
    }

    public static LootItemConditionalFunction.Builder<?> copyName(CopyNameFunction.Source p_423205_) {
        return simpleBuilder(p_422270_ -> new CopyNameFunction(p_422270_, p_423205_));
    }

    static {
        for (LootContext.EntityTarget lootcontext$entitytarget : LootContext.EntityTarget.values()) {
            SOURCES.put(lootcontext$entitytarget.getSerializedName(), new CopyNameFunction.Source(lootcontext$entitytarget.getParam()));
        }

        for (LootContext.BlockEntityTarget lootcontext$blockentitytarget : LootContext.BlockEntityTarget.values()) {
            SOURCES.put(lootcontext$blockentitytarget.getSerializedName(), new CopyNameFunction.Source(lootcontext$blockentitytarget.getParam()));
        }

        CODEC = RecordCodecBuilder.mapCodec(
            p_422268_ -> commonFields(p_422268_)
                .and(SOURCES.codec(Codec.STRING).fieldOf("source").forGetter(p_422271_ -> p_422271_.source))
                .apply(p_422268_, CopyNameFunction::new)
        );
    }

    public record Source(ContextKey<?> param) {
    }
}