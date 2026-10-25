package dev.celerbi.polymorphicdelight;

import dev.celerbi.polymorphicdelight.client.PolymorphicDelightClient;
import dev.celerbi.polymorphicdelight.compat.CuttingBoardSelectionHandler;
import dev.celerbi.polymorphicdelight.compat.HeatDeviceSelectionHandler;
import dev.celerbi.polymorphicdelight.compat.PolymorphCompat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(PolymorphicDelight.MOD_ID)
public final class PolymorphicDelight {
    public static final String MOD_ID = "polymorphic_delight";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public PolymorphicDelight() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, CuttingBoardSelectionHandler::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(HeatDeviceSelectionHandler::onRightClickBlock);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(PolymorphicDelightClient::clientSetup);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            PolymorphCompat.register();
            LOGGER.info("Farmer's Delight conflict-selection integration enabled with Polymorph.");
        });
    }
}
