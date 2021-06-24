package net.alphasucks.bettersleepplus.mixin;

import net.minecraft.server.world.SleepManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SleepManager.class)
public class SleepManagerMixin {

    @Inject(method = "canSkipNight", at = @At(value = "HEAD"), cancellable = true)
    private void canSkipNightInject(int percentage, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
