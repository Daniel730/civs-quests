package dev.daniel730.rpgserver.quest.objective;

import dev.daniel730.rpgserver.quest.Quest;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;

public final class ObjectiveTypeRegistry {

    @FunctionalInterface
    public interface ObjectiveParser extends BiFunction<String, Map<?, ?>, Quest.Objective> {
    }

    private final Map<String, ObjectiveParser> parsers = new LinkedHashMap<>();

    public void register(String typeId, ObjectiveParser parser) {
        parsers.put(typeId.toLowerCase(Locale.ROOT), parser);
    }

    public Quest.Objective parseObjective(Map<?, ?> raw) {
        Object idObj = raw.get("id");
        if (idObj == null) {
            throw new IllegalArgumentException("Objetivo sem 'id'");
        }
        String id = String.valueOf(idObj);
        Object typeObj = raw.get("type");
        if (typeObj == null) {
            throw new IllegalArgumentException("Objetivo '" + id + "' sem 'type'");
        }
        String typeId = String.valueOf(typeObj).toLowerCase(Locale.ROOT);
        ObjectiveParser parser = parsers.get(typeId);
        if (parser == null) {
            throw new IllegalArgumentException("Tipo de objetivo desconhecido: " + typeId);
        }
        return parser.apply(id, raw);
    }

    public Map<String, ObjectiveParser> getRegisteredTypes() {
        return Collections.unmodifiableMap(parsers);
    }

    public void registerDefaults() {
        register(ObjectiveTypes.BUILD_REGION, ObjectiveTypeRegistry::parseBuildRegion);
        register(ObjectiveTypes.SKILL_LEVEL, ObjectiveTypeRegistry::parseSkillLevel);
        register(ObjectiveTypes.KILL_MOB, ObjectiveTypeRegistry::parseKillMob);
        register(ObjectiveTypes.MINE_BLOCK, ObjectiveTypeRegistry::parseMineBlock);
        register(ObjectiveTypes.EARN_MONEY, ObjectiveTypeRegistry::parseEarnMoney);
        register(ObjectiveTypes.BALANCE_MIN, ObjectiveTypeRegistry::parseBalanceMin);
        register(ObjectiveTypes.SHOP_BUY, ObjectiveTypeRegistry::parseShopBuy);
        register(ObjectiveTypes.SHOP_SELL, ObjectiveTypeRegistry::parseShopSell);
        register(ObjectiveTypes.SHOP_REVENUE, ObjectiveTypeRegistry::parseShopRevenue);
    }

    private static Quest.Objective parseBuildRegion(String id, Map<?, ?> raw) {
        String description = stringOrDefault(raw, "description", id);
        String region = requiredString(raw, "region", id);
        return new Quest.Objective(id, ObjectiveTypes.BUILD_REGION, description, region, null, 0,
                null, null, 1, false);
    }

    private static Quest.Objective parseSkillLevel(String id, Map<?, ?> raw) {
        String description = stringOrDefault(raw, "description", id);
        String skill = requiredString(raw, "skill", id);
        int level = intValue(raw, "level", 1);
        return new Quest.Objective(id, ObjectiveTypes.SKILL_LEVEL, description, null, skill, level,
                null, null, level, false);
    }

    private static Quest.Objective parseKillMob(String id, Map<?, ?> raw) {
        String description = stringOrDefault(raw, "description", id);
        String mob = requiredString(raw, "mob", id);
        int amount = intValue(raw, "amount", 1);
        return new Quest.Objective(id, ObjectiveTypes.KILL_MOB, description, null, null, 0,
                mob, null, amount, true);
    }

    private static Quest.Objective parseMineBlock(String id, Map<?, ?> raw) {
        String description = stringOrDefault(raw, "description", id);
        String block = requiredString(raw, "block", id);
        int amount = intValue(raw, "amount", 1);
        return new Quest.Objective(id, ObjectiveTypes.MINE_BLOCK, description, null, null, 0,
                null, block, amount, true);
    }

    private static Quest.Objective parseEarnMoney(String id, Map<?, ?> raw) {
        String description = stringOrDefault(raw, "description", id);
        int amount = intValue(raw, "amount", 1);
        return new Quest.Objective(id, ObjectiveTypes.EARN_MONEY, description, null, null, 0,
                null, null, amount, true);
    }

    private static Quest.Objective parseBalanceMin(String id, Map<?, ?> raw) {
        String description = stringOrDefault(raw, "description", id);
        int amount = intValue(raw, "amount", 1);
        return new Quest.Objective(id, ObjectiveTypes.BALANCE_MIN, description, null, null, amount,
                null, null, amount, false);
    }

    private static Quest.Objective parseShopBuy(String id, Map<?, ?> raw) {
        String description = stringOrDefault(raw, "description", id);
        int amount = intValue(raw, "amount", 1);
        return new Quest.Objective(id, ObjectiveTypes.SHOP_BUY, description, null, null, 0,
                null, null, amount, true);
    }

    private static Quest.Objective parseShopSell(String id, Map<?, ?> raw) {
        String description = stringOrDefault(raw, "description", id);
        int amount = intValue(raw, "amount", 1);
        return new Quest.Objective(id, ObjectiveTypes.SHOP_SELL, description, null, null, 0,
                null, null, amount, true);
    }

    private static Quest.Objective parseShopRevenue(String id, Map<?, ?> raw) {
        String description = stringOrDefault(raw, "description", id);
        int amount = intValue(raw, "amount", 1);
        return new Quest.Objective(id, ObjectiveTypes.SHOP_REVENUE, description, null, null, 0,
                null, null, amount, true);
    }

    private static String stringOrDefault(Map<?, ?> raw, String key, String defaultValue) {
        Object value = raw.get(key);
        return value == null ? defaultValue : String.valueOf(value);
    }

    private static String requiredString(Map<?, ?> raw, String key, String objectiveId) {
        Object value = raw.get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            throw new IllegalArgumentException("Objetivo '" + objectiveId + "' requer campo '" + key + "'");
        }
        return String.valueOf(value);
    }

    private static int intValue(Map<?, ?> raw, String key, int defaultValue) {
        Object value = raw.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value != null) {
            return Integer.parseInt(String.valueOf(value));
        }
        return defaultValue;
    }
}
