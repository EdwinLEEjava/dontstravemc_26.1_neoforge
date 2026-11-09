package com.theconstant.dontstravemc.attachment;

import com.theconstant.dontstravemc.TheConstant;
import com.theconstant.dontstravemc.status.sanity.SanityComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * 玩家数据附件。理智通过 {@link AttachmentType.Builder#sync} 与
 * {@link SanityAttachmentData#STREAM_CODEC} 在逻辑服与客户端之间同步。
 * {@link AttachmentType.Builder#copyOnDeath()} 对齐旧 CCA 的 {@code RespawnCopyStrategy.ALWAYS_COPY}。
 */
public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, TheConstant.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SanityAttachmentData>> SANITY =
            ATTACHMENT_TYPES.register(
                    "sanity",
                    () -> AttachmentType.serializable(SanityAttachmentData::new)
                            .copyOnDeath()
                            .sync((holder, to) -> holder == to, SanityAttachmentData.STREAM_CODEC)
                            .build());

    private ModAttachments() {}

    public static SanityComponents getSanity(Player player) {
        return player.getData(SANITY.get());
    }

    public static void syncSanity(ServerPlayer player) {
        player.syncData(SANITY.get());
    }
}
