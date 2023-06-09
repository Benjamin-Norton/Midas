package com.bawnorton.midas.mixin.client;

import com.bawnorton.midas.api.MidasApi;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.block.entity.ChestBlockEntityRenderer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChestBlockEntityRenderer.class)
public abstract class ChestBlockEntityRendererMixin {
    @Redirect(method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/TexturedRenderLayers;getChestTextureId(Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/block/enums/ChestType;Z)Lnet/minecraft/client/util/SpriteIdentifier;"))
    private SpriteIdentifier getChestTextureId(BlockEntity blockEntity, ChestType chestType, boolean bl) {
        if (MidasApi.isGold(blockEntity)) {
            return new SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE, switch(chestType) {
                case SINGLE -> new Identifier("midas", "entity/chest/gold");
                case LEFT -> new Identifier("midas", "entity/chest/gold_left");
                case RIGHT -> new Identifier("midas", "entity/chest/gold_right");
            });
        }
        return TexturedRenderLayers.getChestTextureId(blockEntity, chestType, bl);
    }
}
