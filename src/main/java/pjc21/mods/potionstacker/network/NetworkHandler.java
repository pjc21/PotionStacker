package pjc21.mods.potionstacker.network;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import pjc21.mods.potionstacker.PotionStacker;

public class NetworkHandler {

    private static int id = 420;
    public static final SimpleChannel HANDLER = NetworkRegistry.newSimpleChannel(new ResourceLocation(PotionStacker.MODID, "main"), () -> "1.0", (s) -> true, (s) -> true);

    public static void init() {
        HANDLER.registerMessage(id(), SendEffectsInfo.class, SendEffectsInfo::encode, SendEffectsInfo::decode, SendEffectsInfo::handle);
    }

    private static int id() {
        return id++;
    }
}
