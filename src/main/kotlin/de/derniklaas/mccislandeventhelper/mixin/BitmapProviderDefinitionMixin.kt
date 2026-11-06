package de.derniklaas.mccislandeventhelper.mixin

import de.derniklaas.mccislandeventhelper.glyphs.IconGlyphs
import net.minecraft.client.gui.font.providers.BitmapProvider
import net.minecraft.resources.Identifier
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(BitmapProvider.Definition::class)
class BitmapProviderDefinitionMixin {

    @Inject(method = ["<init>"], at = [At("TAIL")])
    private fun mccislandeventhelper_captureIconGlyphs(
        file: Identifier,
        height: Int,
        ascent: Int,
        codepointGrid: Array<IntArray>,
        ci: CallbackInfo,
    ) {
        IconGlyphs.capture(file, codepointGrid)
    }
}
