package eu.zunix.ore_and_alloy.registry;

import com.mojang.serialization.Codec;
import eu.zunix.ore_and_alloy.OreAndAlloy;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, OreAndAlloy.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> VEIN_WORLDGEN_PROCESSED =
            ATTACHMENT_TYPES.register(
                    "vein_worldgen_processed",
                    () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).build()
            );

    private ModAttachments() {}
}
