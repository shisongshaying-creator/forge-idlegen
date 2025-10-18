package net.minecraft.world.entity.animal.coppergolem;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.InteractWithDoor;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.TransportItemsBetweenContainers;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CopperGolemAi {
    private static final float SPEED_MULTIPLIER_WHEN_PANICKING = 1.5F;
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 1.0F;
    private static final int TRANSPORT_ITEM_HORIZONTAL_SEARCH_RADIUS = 32;
    private static final int TRANSPORT_ITEM_VERTICAL_SEARCH_RADIUS = 8;
    private static final int TICK_TO_START_ON_REACHED_INTERACTION = 1;
    private static final int TICK_TO_PLAY_ON_REACHED_SOUND = 9;
    private static final Predicate<BlockState> TRANSPORT_ITEM_SOURCE_BLOCK = p_428358_ -> p_428358_.is(BlockTags.COPPER_CHESTS);
    private static final Predicate<BlockState> TRANSPORT_ITEM_DESTINATION_BLOCK = p_426042_ -> p_426042_.is(Blocks.CHEST) || p_426042_.is(Blocks.TRAPPED_CHEST);
    private static final ImmutableList<SensorType<? extends Sensor<? super CopperGolem>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY);
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
        MemoryModuleType.IS_PANICKING,
        MemoryModuleType.HURT_BY,
        MemoryModuleType.HURT_BY_ENTITY,
        MemoryModuleType.NEAREST_LIVING_ENTITIES,
        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
        MemoryModuleType.WALK_TARGET,
        MemoryModuleType.LOOK_TARGET,
        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
        MemoryModuleType.PATH,
        MemoryModuleType.GAZE_COOLDOWN_TICKS,
        MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS,
        MemoryModuleType.VISITED_BLOCK_POSITIONS,
        MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS,
        MemoryModuleType.DOORS_TO_CLOSE
    );

    public static Brain.Provider<CopperGolem> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    protected static Brain<?> makeBrain(Brain<CopperGolem> p_423371_) {
        initCoreActivity(p_423371_);
        initIdleActivity(p_423371_);
        p_423371_.setCoreActivities(Set.of(Activity.CORE));
        p_423371_.setDefaultActivity(Activity.IDLE);
        p_423371_.useDefaultActivity();
        return p_423371_;
    }

    public static void updateActivity(CopperGolem p_431523_) {
        p_431523_.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.IDLE));
    }

    private static void initCoreActivity(Brain<CopperGolem> p_426859_) {
        p_426859_.addActivity(
            Activity.CORE,
            0,
            ImmutableList.of(
                new AnimalPanic<>(1.5F),
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink(),
                InteractWithDoor.create(),
                new CountDownCooldownTicks(MemoryModuleType.GAZE_COOLDOWN_TICKS),
                new CountDownCooldownTicks(MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS)
            )
        );
    }

    private static void initIdleActivity(Brain<CopperGolem> p_428622_) {
        p_428622_.addActivity(
            Activity.IDLE,
            ImmutableList.of(
                Pair.of(0, new TransportItemsBetweenContainers(1.0F, TRANSPORT_ITEM_SOURCE_BLOCK, TRANSPORT_ITEM_DESTINATION_BLOCK, 32, 8, getTargetReachedInteractions(), onTravelling(), shouldQueueForTarget())),
                Pair.of(1, SetEntityLookTargetSometimes.create(EntityType.PLAYER, 6.0F, UniformInt.of(40, 80))),
                Pair.of(
                    2,
                    new RunOne<>(
                        ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS, MemoryStatus.VALUE_PRESENT),
                        ImmutableList.of(Pair.of(RandomStroll.stroll(1.0F, 2, 2), 1), Pair.of(new DoNothing(30, 60), 1))
                    )
                )
            )
        );
    }

    private static Map<TransportItemsBetweenContainers.ContainerInteractionState, TransportItemsBetweenContainers.OnTargetReachedInteraction> getTargetReachedInteractions() {
        return Map.of(
            TransportItemsBetweenContainers.ContainerInteractionState.PICKUP_ITEM,
            onReachedTargetInteraction(CopperGolemState.GETTING_ITEM, SoundEvents.COPPER_GOLEM_ITEM_GET),
            TransportItemsBetweenContainers.ContainerInteractionState.PICKUP_NO_ITEM,
            onReachedTargetInteraction(CopperGolemState.GETTING_NO_ITEM, SoundEvents.COPPER_GOLEM_ITEM_NO_GET),
            TransportItemsBetweenContainers.ContainerInteractionState.PLACE_ITEM,
            onReachedTargetInteraction(CopperGolemState.DROPPING_ITEM, SoundEvents.COPPER_GOLEM_ITEM_DROP),
            TransportItemsBetweenContainers.ContainerInteractionState.PLACE_NO_ITEM,
            onReachedTargetInteraction(CopperGolemState.DROPPING_NO_ITEM, SoundEvents.COPPER_GOLEM_ITEM_NO_DROP)
        );
    }

    private static TransportItemsBetweenContainers.OnTargetReachedInteraction onReachedTargetInteraction(CopperGolemState p_429539_, @Nullable SoundEvent p_426594_) {
        return (p_425723_, p_428413_, p_430305_) -> {
            if (p_425723_ instanceof CopperGolem coppergolem) {
                Container container = p_428413_.container();
                if (p_430305_ == 1) {
                    container.startOpen(coppergolem);
                    coppergolem.setOpenedChestPos(p_428413_.pos());
                    coppergolem.setState(p_429539_);
                }

                if (p_430305_ == 9 && p_426594_ != null) {
                    coppergolem.playSound(p_426594_);
                }

                if (p_430305_ == 60) {
                    if (container.getEntitiesWithContainerOpen().contains(p_425723_)) {
                        container.stopOpen(coppergolem);
                    }

                    coppergolem.clearOpenedChestPos();
                }
            }
        };
    }

    private static Consumer<PathfinderMob> onTravelling() {
        return p_425830_ -> {
            if (p_425830_ instanceof CopperGolem coppergolem) {
                coppergolem.clearOpenedChestPos();
                coppergolem.setState(CopperGolemState.IDLE);
            }
        };
    }

    private static Predicate<TransportItemsBetweenContainers.TransportItemTarget> shouldQueueForTarget() {
        return p_424241_ -> p_424241_.blockEntity() instanceof ChestBlockEntity chestblockentity ? !chestblockentity.getEntitiesWithContainerOpen().isEmpty() : false;
    }
}