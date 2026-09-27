package dev.celerbi.polymorphicdelight;

import dev.celerbi.polymorphicdelight.client.PolymorphicDelightClient;
import dev.celerbi.polymorphicdelight.compat.CuttingBoardSelectionHandler;
import dev.celerbi.polymorphicdelight.compat.HeatDeviceSelectionHandler;
import dev.celerbi.polymorphicdelight.compat.PolymorphCompat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(PolymorphicDelight.MOD_ID)
public final class PolymorphicDelight {
    public static final String MOD_ID = "polymorphic_delight";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final String POLYMORPH_PLUS_MOD_ID = "polymorph_plus";

    public PolymorphicDelight(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.addListener(CuttingBoardSelectionHandler::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(HeatDeviceSelectionHandler::onRightClickBlock);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(PolymorphicDelightClient::clientSetup);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            PolymorphCompat.register();
            LOGGER.info(
                    "Farmer's Delight conflict-selection integration enabled with {}.",
                    providerName()
            );
        });
    }

    private static String providerName() {
        return ModList.get().isLoaded(POLYMORPH_PLUS_MOD_ID) ? "Polymorph+" : "Polymorph";
    }
}
