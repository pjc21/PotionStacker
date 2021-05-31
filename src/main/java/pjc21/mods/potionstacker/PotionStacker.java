package pjc21.mods.potionstacker;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pjc21.mods.potionstacker.client.KeyBindings;
import pjc21.mods.potionstacker.config.PotionStackerConfig;
import pjc21.mods.potionstacker.network.NetworkHandler;

@Mod(PotionStacker.MODID)
public class PotionStacker
{
    public static final String MODID = "potionstacker";
    public static final String NAME = "Potion Stacker";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public PotionStacker() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::setup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PotionStackerConfig.COMMON_SPEC);
    }

    private void setup(FMLCommonSetupEvent event) {
        NetworkHandler.init();
        event.enqueueWork(KeyBindings::init);
    }
}
