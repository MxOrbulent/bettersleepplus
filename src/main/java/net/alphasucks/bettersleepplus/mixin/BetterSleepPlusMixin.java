package net.alphasucks.bettersleepplus.mixin;

import net.alphasucks.bettersleepplus.BetterSleepPlus;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.SleepManager;
import net.minecraft.world.level.ServerWorldProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public class BetterSleepPlusMixin {

    @Shadow
    @Final
    List<ServerPlayerEntity> players;
    @Shadow
    @Final
    private ServerWorldProperties worldProperties;


    @Shadow @Final private SleepManager sleepManager;

    @Inject(method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(value = "INVOKE", shift = At.Shift.BEFORE,
                    target = "Lnet/minecraft/world/GameRules;getInt(Lnet/minecraft/world/GameRules$Key;)I", ordinal = 0))
    private void customSleepingMechanic(final BooleanSupplier shouldKeepTicking, final CallbackInfo ci) {
        sleepManager.update(players);
        SleepManagerAccessor access = (SleepManagerAccessor) sleepManager;
        BetterSleepPlus.customSleepTick(access.getTotal(),access.getSleeping(),(ServerWorld) (Object) this, players, worldProperties);
    }
}
