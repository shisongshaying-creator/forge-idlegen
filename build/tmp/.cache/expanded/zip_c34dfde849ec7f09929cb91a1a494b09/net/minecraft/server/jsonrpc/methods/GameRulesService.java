package net.minecraft.server.jsonrpc.methods;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.GameRules;

public class GameRulesService {
    public static List<GameRulesService.TypedRule> get(MinecraftApi p_426026_) {
        List<? extends GameRules.Key<?>> list = p_426026_.gameRuleService().getAvailableGameRules().map(Entry::getKey).toList();
        List<GameRulesService.TypedRule> list1 = new ArrayList<>();

        for (GameRules.Key<?> key : list) {
            GameRules.Value<?> value = p_426026_.gameRuleService().getRule((GameRules.Key)key);
            list1.add(getTypedRule(p_426026_, key.getId(), value));
        }

        return list1;
    }

    public static GameRulesService.TypedRule getTypedRule(MinecraftApi p_423755_, String p_431250_, GameRules.Value<?> p_430637_) {
        return p_423755_.gameRuleService().getTypedRule(p_431250_, p_430637_);
    }

    public static GameRulesService.TypedRule update(MinecraftApi p_426148_, GameRulesService.UntypedRule p_426936_, ClientInfo p_430040_) {
        return p_426148_.gameRuleService().updateGameRule(p_426936_, p_430040_);
    }

    public static enum RuleType implements StringRepresentable {
        INT("integer"),
        BOOL("boolean");

        private final String name;

        private RuleType(final String p_431431_) {
            this.name = p_431431_;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public record TypedRule(String key, String value, GameRulesService.RuleType type) {
        public static final MapCodec<GameRulesService.TypedRule> CODEC = RecordCodecBuilder.mapCodec(
            p_423056_ -> p_423056_.group(
                    Codec.STRING.fieldOf("key").forGetter(GameRulesService.TypedRule::key),
                    Codec.STRING.fieldOf("value").forGetter(GameRulesService.TypedRule::value),
                    StringRepresentable.fromEnum(GameRulesService.RuleType::values).fieldOf("type").forGetter(GameRulesService.TypedRule::type)
                )
                .apply(p_423056_, GameRulesService.TypedRule::new)
        );
    }

    public record UntypedRule(String key, String value) {
        public static final MapCodec<GameRulesService.UntypedRule> CODEC = RecordCodecBuilder.mapCodec(
            p_423672_ -> p_423672_.group(
                    Codec.STRING.fieldOf("key").forGetter(GameRulesService.UntypedRule::key),
                    Codec.STRING.fieldOf("value").forGetter(GameRulesService.UntypedRule::value)
                )
                .apply(p_423672_, GameRulesService.UntypedRule::new)
        );
    }
}