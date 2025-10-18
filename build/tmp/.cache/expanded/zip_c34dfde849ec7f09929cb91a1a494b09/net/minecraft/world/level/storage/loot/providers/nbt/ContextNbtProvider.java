package net.minecraft.world.level.storage.loot.providers.nbt;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;

public class ContextNbtProvider implements NbtProvider {
    private static final ExtraCodecs.LateBoundIdMapper<String, ContextNbtProvider.Source<?>> SOURCES = new ExtraCodecs.LateBoundIdMapper<>();
    private static final Codec<ContextNbtProvider.Source<?>> GETTER_CODEC;
    public static final MapCodec<ContextNbtProvider> MAP_CODEC;
    public static final Codec<ContextNbtProvider> INLINE_CODEC;
    private final ContextNbtProvider.Source<?> source;

    private ContextNbtProvider(ContextNbtProvider.Source<?> p_424229_) {
        this.source = p_424229_;
    }

    @Override
    public LootNbtProviderType getType() {
        return NbtProviders.CONTEXT;
    }

    @Nullable
    @Override
    public Tag get(LootContext p_165573_) {
        return this.source.get(p_165573_);
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(this.source.contextParam());
    }

    public static NbtProvider forContextEntity(LootContext.EntityTarget p_165571_) {
        return new ContextNbtProvider(new ContextNbtProvider.EntitySource(p_165571_.getParam()));
    }

    static {
        for (LootContext.EntityTarget lootcontext$entitytarget : LootContext.EntityTarget.values()) {
            SOURCES.put(lootcontext$entitytarget.getSerializedName(), new ContextNbtProvider.EntitySource(lootcontext$entitytarget.getParam()));
        }

        for (LootContext.BlockEntityTarget lootcontext$blockentitytarget : LootContext.BlockEntityTarget.values()) {
            SOURCES.put(lootcontext$blockentitytarget.getSerializedName(), new ContextNbtProvider.BlockEntitySource(lootcontext$blockentitytarget.getParam()));
        }

        GETTER_CODEC = SOURCES.codec(Codec.STRING);
        MAP_CODEC = RecordCodecBuilder.mapCodec(
            p_300408_ -> p_300408_.group(GETTER_CODEC.fieldOf("target").forGetter(p_422275_ -> p_422275_.source)).apply(p_300408_, ContextNbtProvider::new)
        );
        INLINE_CODEC = GETTER_CODEC.xmap(ContextNbtProvider::new, p_422276_ -> p_422276_.source);
    }

    record BlockEntitySource(ContextKey<? extends BlockEntity> contextParam) implements ContextNbtProvider.Source<BlockEntity> {
        public Tag get(BlockEntity p_428056_) {
            return p_428056_.saveWithFullMetadata(p_428056_.getLevel().registryAccess());
        }

        @Override
        public ContextKey<? extends BlockEntity> contextParam() {
            return this.contextParam;
        }
    }

    record EntitySource(ContextKey<? extends Entity> contextParam) implements ContextNbtProvider.Source<Entity> {
        public Tag get(Entity p_426254_) {
            return NbtPredicate.getEntityTagToCompare(p_426254_);
        }

        @Override
        public ContextKey<? extends Entity> contextParam() {
            return this.contextParam;
        }
    }

    interface Source<T> {
        ContextKey<? extends T> contextParam();

        @Nullable
        Tag get(T p_424741_);

        @Nullable
        default Tag get(LootContext p_423251_) {
            T t = p_423251_.getOptionalParameter((ContextKey<T>)this.contextParam());
            return t != null ? this.get(t) : null;
        }
    }
}