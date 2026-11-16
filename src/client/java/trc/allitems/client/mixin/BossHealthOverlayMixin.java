package trc.allitems.client.mixin;

import net.minecraft.client.gui.components.BossHealthOverlay;

import trc.allitems.client.ChallengeHud;
import trc.allitems.client.ClientChallenge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(BossHealthOverlay.class)
public class BossHealthOverlayMixin {
    @ModifyConstant(method = "extractRenderState", constant = @Constant(intValue = 12))
    private int allitems$shiftBelowChallengeBar(int original) {
        return ClientChallenge.barVisible() ? original + ChallengeHud.HEIGHT : original;
    }
}