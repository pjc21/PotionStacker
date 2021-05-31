package pjc21.mods.potionstacker.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Collection;
import java.util.function.Supplier;

public class SendEffectsInfo {

    public static void encode(final SendEffectsInfo msg, final PacketBuffer packetBuffer) { }

    public static SendEffectsInfo decode(final PacketBuffer packetBuffer) { return new SendEffectsInfo(); }

    public static void handle(final SendEffectsInfo msg, Supplier<NetworkEvent.Context> context) {

        final NetworkEvent.Context contextObj = context.get();
        contextObj.enqueueWork(() -> {
            final ServerPlayerEntity sender = contextObj.getSender();
            if (sender != null) {
                Collection<EffectInstance> effects = sender.getActiveEffects();

                if (!effects.isEmpty()) {
                    effects.forEach(effectInstance -> {
                        Effect effect = effectInstance.getEffect();
                        int amplifier = effectInstance.getAmplifier();
                        int duration = effectInstance.getDuration();
                        ITextComponent effectsInfo = new TranslationTextComponent(String.valueOf(effect.getRegistryName()).toUpperCase()).withStyle(TextFormatting.GREEN)
                                .append(new TranslationTextComponent(" - ").withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent("Amplifier: ").withStyle(TextFormatting.DARK_AQUA)).append(new TranslationTextComponent(String.valueOf(amplifier + 1)).withStyle(TextFormatting.GOLD))
                                .append(new TranslationTextComponent(", ").withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent("Duration: ").withStyle(TextFormatting.DARK_AQUA)).append(new TranslationTextComponent(String.valueOf(formatTickDuration(duration))).withStyle(TextFormatting.GOLD));

                        sender.sendMessage(effectsInfo,sender.getUUID());
                    });
                }
            }
        });
        contextObj.setPacketHandled(true);
    }

    private static String formatTickDuration(int dur) {

        int durFloor = MathHelper.floor((float) dur);
        int i = durFloor / 20;
        i = i % (24 * 3600);
        int hour = i / 3600;
        i %= 3600;
        int minutes = i / 60 ;
        i %= 60;
        int seconds = i;

        return String.format("%02d:%02d:%02d",hour,minutes,seconds);
    }
}
