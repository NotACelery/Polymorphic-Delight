package dev.celerbi.polymorphicdelight;

import dev.celerbi.polymorphicdelight.compat.InteractionHandlers;
import dev.celerbi.polymorphicdelight.compat.PolymorphCompat;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

public final class PolymorphicDelight implements ModInitializer {
    public static final String MOD_ID = "polymorphic_delight";

    @Override
    public void onInitialize() {
        PolymorphCompat.register();
        UseBlockCallback.EVENT.register(InteractionHandlers::onUseBlock);
    }
}
