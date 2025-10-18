package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class CopyComponentsFunction extends LootItemConditionalFunction {
    private static final ExtraCodecs.LateBoundIdMapper<String, CopyComponentsFunction.Source<?>> SOURCES = new ExtraCodecs.LateBoundIdMapper<>();
    public static final MapCodec<CopyComponentsFunction> CODEC;
    private final CopyComponentsFunction.Source<?> source;
    private final Optional<List<DataComponentType<?>>> include;
    private final Optional<List<DataComponentType<?>>> exclude;
    private final Predicate<DataComponentType<?>> bakedPredicate;

    CopyComponentsFunction(
        List<LootItemCondition> p_332739_,
        CopyComponentsFunction.Source<?> p_333486_,
        Optional<List<DataComponentType<?>>> p_332029_,
        Optional<List<DataComponentType<?>>> p_329656_
    ) {
        super(p_332739_);
        this.source = p_333486_;
        this.include = p_332029_.map(List::copyOf);
        this.exclude = p_329656_.map(List::copyOf);
        List<Predicate<DataComponentType<?>>> list = new ArrayList<>(2);
        p_329656_.ifPresent(p_329848_ -> list.add(p_331276_ -> !p_329848_.contains(p_331276_)));
        p_332029_.ifPresent(p_331486_ -> list.add(p_331486_::contains));
        this.bakedPredicate = Util.allOf(list);
    }

    @Override
    public LootItemFunctionType<CopyComponentsFunction> getType() {
        return LootItemFunctions.COPY_COMPONENTS;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(this.source.contextParam());
    }

    @Override
    public ItemStack run(ItemStack p_329465_, LootContext p_328771_) {
        DataComponentGetter datacomponentgetter = this.source.get(p_328771_);
        if (datacomponentgetter != null) {
            if (datacomponentgetter instanceof DataComponentMap datacomponentmap) {
                p_329465_.applyComponents(datacomponentmap.filter(this.bakedPredicate));
            } else {
                Collection<DataComponentType<?>> collection = this.exclude.orElse(List.of());
                this.include.map(Collection::stream).orElse(BuiltInRegistries.DATA_COMPONENT_TYPE.listElements().map(Holder::value)).forEach(p_422267_ -> {
                    if (!collection.contains(p_422267_)) {
                        TypedDataComponent<?> typeddatacomponent = datacomponentgetter.getTyped(p_422267_);
                        if (typeddatacomponent != null) {
                            p_329465_.set(typeddatacomponent);
                        }
                    }
                });
            }
        }

        return p_329465_;
    }

    public static CopyComponentsFunction.Builder copyComponentsFromEntity(ContextKey<? extends Entity> p_422628_) {
        return new CopyComponentsFunction.Builder(new CopyComponentsFunction.EntitySource(p_422628_));
    }

    public static CopyComponentsFunction.Builder copyComponentsFromBlockEntity(ContextKey<? extends BlockEntity> p_429680_) {
        return new CopyComponentsFunction.Builder(new CopyComponentsFunction.BlockEntitySource(p_429680_));
    }

    static {
        for (LootContext.EntityTarget lootcontext$entitytarget : LootContext.EntityTarget.values()) {
            SOURCES.put(lootcontext$entitytarget.getSerializedName(), new CopyComponentsFunction.EntitySource(lootcontext$entitytarget.getParam()));
        }

        for (LootContext.BlockEntityTarget lootcontext$blockentitytarget : LootContext.BlockEntityTarget.values()) {
            SOURCES.put(
                lootcontext$blockentitytarget.getSerializedName(), new CopyComponentsFunction.BlockEntitySource(lootcontext$blockentitytarget.getParam())
            );
        }

        for (LootContext.ItemStackTarget lootcontext$itemstacktarget : LootContext.ItemStackTarget.values()) {
            SOURCES.put(lootcontext$itemstacktarget.getSerializedName(), new CopyComponentsFunction.ItemStackSource(lootcontext$itemstacktarget.getParam()));
        }

        CODEC = RecordCodecBuilder.mapCodec(
            p_422263_ -> commonFields(p_422263_)
                .and(
                    p_422263_.group(
                        SOURCES.codec(Codec.STRING).fieldOf("source").forGetter(p_329984_ -> p_329984_.source),
                        DataComponentType.CODEC.listOf().optionalFieldOf("include").forGetter(p_330902_ -> p_330902_.include),
                        DataComponentType.CODEC.listOf().optionalFieldOf("exclude").forGetter(p_331318_ -> p_331318_.exclude)
                    )
                )
                .apply(p_422263_, CopyComponentsFunction::new)
        );
    }

    record BlockEntitySource(ContextKey<? extends BlockEntity> contextParam) implements CopyComponentsFunction.Source<BlockEntity> {
        public DataComponentGetter get(BlockEntity p_425299_) {
            return p_425299_.collectComponents();
        }

        @Override
        public ContextKey<? extends BlockEntity> contextParam() {
            return this.contextParam;
        }
    }

    public static class Builder extends LootItemConditionalFunction.Builder<CopyComponentsFunction.Builder> {
        private final CopyComponentsFunction.Source<?> source;
        private Optional<ImmutableList.Builder<DataComponentType<?>>> include = Optional.empty();
        private Optional<ImmutableList.Builder<DataComponentType<?>>> exclude = Optional.empty();

        Builder(CopyComponentsFunction.Source<?> p_336396_) {
            this.source = p_336396_;
        }

        public CopyComponentsFunction.Builder include(DataComponentType<?> p_329871_) {
            if (this.include.isEmpty()) {
                this.include = Optional.of(ImmutableList.builder());
            }

            this.include.get().add(p_329871_);
            return this;
        }

        public CopyComponentsFunction.Builder exclude(DataComponentType<?> p_332922_) {
            if (this.exclude.isEmpty()) {
                this.exclude = Optional.of(ImmutableList.builder());
            }

            this.exclude.get().add(p_332922_);
            return this;
        }

        protected CopyComponentsFunction.Builder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new CopyComponentsFunction(
                this.getConditions(), this.source, this.include.map(ImmutableList.Builder::build), this.exclude.map(ImmutableList.Builder::build)
            );
        }
    }

    record EntitySource(ContextKey<? extends Entity> contextParam) implements CopyComponentsFunction.Source<Entity> {
        public DataComponentGetter get(Entity p_426319_) {
            return p_426319_;
        }

        @Override
        public ContextKey<? extends Entity> contextParam() {
            return this.contextParam;
        }
    }

    record ItemStackSource(ContextKey<? extends ItemStack> contextParam) implements CopyComponentsFunction.Source<ItemStack> {
        public DataComponentGetter get(ItemStack p_430559_) {
            return p_430559_.getComponents();
        }

        @Override
        public ContextKey<? extends ItemStack> contextParam() {
            return this.contextParam;
        }
    }

    public interface Source<T> {
        ContextKey<? extends T> contextParam();

        DataComponentGetter get(T p_426452_);

        @Nullable
        default DataComponentGetter get(LootContext p_429205_) {
            T t = p_429205_.getOptionalParameter((ContextKey<T>)this.contextParam());
            return t != null ? this.get(t) : null;
        }
    }
}