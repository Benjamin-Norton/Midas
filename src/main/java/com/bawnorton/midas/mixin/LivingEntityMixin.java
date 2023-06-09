package com.bawnorton.midas.mixin;

import com.bawnorton.midas.access.DataSaverAccess;
import com.bawnorton.midas.api.MidasApi;
import com.bawnorton.midas.liquid.PactolusFluid;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements DataSaverAccess {
    @Shadow public abstract Box getVisibilityBoundingBox();

    private static final TrackedData<Boolean> IS_GOLD = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initDataTracker(CallbackInfo ci) {
        ((LivingEntity) (Object) this).getDataTracker().startTracking(IS_GOLD, false);
    }

    @Override
    public boolean isGold() {
        return ((LivingEntity) (Object) this).getDataTracker().get(IS_GOLD);
    }

    @Override
    public void setGold(boolean gold) {
        ((LivingEntity) (Object) this).getDataTracker().set(IS_GOLD, gold);
        if(!gold && ((LivingEntity) (Object) this) instanceof MobEntity mobEntity) {
            mobEntity.setAiDisabled(false);
        }
    }

    @Inject(method = "dropLoot", at = @At("HEAD"), cancellable = true)
    private void dropLoot(DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        if(MidasApi.isGold(this)) {
            Box box = this.getVisibilityBoundingBox();
            double volume = box.getXLength() * box.getYLength() * box.getZLength();

            int i;
            for(i = (int) Math.max(Math.floor(volume * 9.0 * 9.0), 1); i >= 81; i -= 81) {
                ((LivingEntity) (Object) this).dropItem(Items.GOLD_BLOCK);
            }

            while(i >= 9) {
                ((LivingEntity) (Object) this).dropItem(Items.GOLD_INGOT);
                i -= 9;
            }

            while(i > 0) {
                ((LivingEntity) (Object) this).dropItem(Items.GOLD_NUGGET);
                --i;
            }
            ci.cancel();
        }
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void baseTick(CallbackInfo ci) {
        Box box = ((LivingEntity) (Object) this).getBoundingBox().contract(0.001);
        int minX = (int) Math.floor(box.minX);
        int minY = (int) Math.floor(box.minY);
        int minZ = (int) Math.floor(box.minZ);
        int maxX = (int) Math.ceil(box.maxX);
        int maxY = (int) Math.ceil(box.maxY);
        int maxZ = (int) Math.ceil(box.maxZ);
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for(int x = minX; x < maxX; x++) {
            for(int y = minY; y < maxY; y++) {
                for(int z = minZ; z < maxZ; z++) {
                    mutable.set(x, y, z);
                    FluidState fluidState = ((LivingEntity) (Object) this).world.getFluidState(mutable);
                    Fluid fluid = fluidState.getFluid();
                    if(fluid instanceof PactolusFluid) {
                        MidasApi.cleanse((LivingEntity) (Object) this);
                        return;
                    }
                }
            }
        }
    }
}
