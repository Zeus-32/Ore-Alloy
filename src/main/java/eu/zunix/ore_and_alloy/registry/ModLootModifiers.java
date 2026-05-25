package eu.zunix.ore_and_alloy.registry;

import com.mojang.serialization.MapCodec;
import eu.zunix.ore_and_alloy.OreAndAlloy;
import eu.zunix.ore_and_alloy.integration.loot.RedstoneLootReplacementModifier;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, OreAndAlloy.MODID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<RedstoneLootReplacementModifier>>
            REPLACE_REDSTONE_DROPS = LOOT_MODIFIER_SERIALIZERS.register(
            "replace_redstone_drops",
            () -> RedstoneLootReplacementModifier.CODEC
    );

    private ModLootModifiers() {}
}
