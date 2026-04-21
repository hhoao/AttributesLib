package dev.shadowsoffire.attributeslib.mixin.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.util.text.IFormattableTextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ContainerScreen.class)
public class ContainerScreenMixin extends Screen {

    protected ContainerScreenMixin(IFormattableTextComponent pTitle) {
        super(pTitle);
    }

    @Inject(at = @At("RETURN"), method = "mouseDragged(DDIDD)Z", cancellable = true, require = 1)
    public void apoth_superMouseDragged(
            double pMouseX,
            double pMouseY,
            int pButton,
            double pDragX,
            double pDragY,
            CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof InventoryScreen)
            cir.setReturnValue(super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY));
    }
}
