package pjc21.mods.potionstacker.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import pjc21.mods.potionstacker.PotionStacker;
import pjc21.mods.potionstacker.network.NetworkHandler;
import pjc21.mods.potionstacker.network.SendEffectsInfo;

@Mod.EventBusSubscriber(modid = PotionStacker.MODID, value = Dist.CLIENT)
public class KeyBindings {

    private static final KeyBinding effectInfo = new KeyBinding("Show Effects Info", KeyConflictContext.UNIVERSAL, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_N, PotionStacker.NAME);

    public static void init() {
        ClientRegistry.registerKeyBinding(effectInfo);
    }

    @SubscribeEvent
    public static void onKeyInput(TickEvent.ClientTickEvent event) {

        if (event.phase != TickEvent.Phase.END) return;

        if (effectInfo.isDown()) {
            NetworkHandler.HANDLER.sendToServer(new SendEffectsInfo());
        }
    }
}
