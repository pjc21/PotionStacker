package pjc21.mods.potionstacker.events;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pjc21.mods.potionstacker.PotionStacker;
import pjc21.mods.potionstacker.config.PotionStackerConfig;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = PotionStacker.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {

    @SubscribeEvent
    public static void onPotionAdded(PotionEvent.PotionAddedEvent event) {

        final LivingEntity entityLiving = event.getEntityLiving();
        EffectInstance newPotionEffect = event.getPotionEffect();
        EffectInstance oldPotionEffect = event.getOldPotionEffect();

        if (PotionStackerConfig.allowAllEffectSources) {
            if ((!event.getEntity().level.isClientSide && !PotionStackerConfig.effectsBlacklist.contains(newPotionEffect.getEffect())) && (oldPotionEffect != null) && (newPotionEffect.getDuration() > PotionStackerConfig.minDurationStackable)) {

                Effect newEffect = newPotionEffect.getEffect();
                final int newAmp = newPotionEffect.getAmplifier();
                final int newDur = newPotionEffect.getDuration();
                final boolean isAmbient = newPotionEffect.isAmbient();

                Effect oldEffect = oldPotionEffect.getEffect();
                final int oldAmp = oldPotionEffect.getAmplifier();
                final int oldDur = oldPotionEffect.getDuration();

                int ampCap = 50;
                int durCap = 30000;

                if (PotionStackerConfig.effectCaps.containsKey(newEffect)) {
                    ampCap = PotionStackerConfig.effectCaps.get(newEffect).getAmpMax();
                    durCap = PotionStackerConfig.effectCaps.get(newEffect).getDurMax();
                }

                int increaseAmp = oldAmp + (newAmp + 1);
                int increaseDur = newDur + oldDur;

                if (PotionStackerConfig.allowAmbient) {
                    if (PotionStackerConfig.allowAllEntities) {
                        updateAddedPotionEffect(oldEffect, newEffect, oldAmp, newAmp, ampCap, oldDur, newDur, durCap, increaseDur, increaseAmp, newPotionEffect);
                    } else if ((!PotionStackerConfig.allowAllEntities) && entityLiving instanceof PlayerEntity) {
                        updateAddedPotionEffect(oldEffect, newEffect, oldAmp, newAmp, ampCap, oldDur, newDur, durCap, increaseDur, increaseAmp, newPotionEffect);
                    }
                } else if (!PotionStackerConfig.allowAmbient && !isAmbient) {
                    if (PotionStackerConfig.allowAllEntities) {
                        updateAddedPotionEffect(oldEffect, newEffect, oldAmp, newAmp, ampCap, oldDur, newDur, durCap, increaseDur, increaseAmp, newPotionEffect);
                    } else if ((!PotionStackerConfig.allowAllEntities) && entityLiving instanceof PlayerEntity) {
                        updateAddedPotionEffect(oldEffect, newEffect, oldAmp, newAmp, ampCap, oldDur, newDur, durCap, increaseDur, increaseAmp, newPotionEffect);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingEntityUseItemEvent(LivingEntityUseItemEvent.Tick event) {

        if (!PotionStackerConfig.allowAllEffectSources) {
            if (!event.getEntity().level.isClientSide && event.getEntity() instanceof PlayerEntity) {
                PlayerEntity playerEntity = (PlayerEntity) event.getEntityLiving();
                ItemStack itemStack = event.getItem();
                int duration = event.getDuration();
                String itemName = Objects.requireNonNull(itemStack.getItem().getRegistryName()).toString();

                if (itemName.matches("minecraft:potion") && itemStack.hasTag() && itemStack.getItem() instanceof PotionItem) {
                    if (duration <= 1) {
                        useItemUpdatePotionEffect(playerEntity, itemStack);
                    }
                }
            }
        }
    }

    private static void updateAddedPotionEffect(Effect oldEffect, Effect newEffect, int oldAmp, int newAmp, int ampCap, int oldDur, int newDur, int durCap, int increaseDur, int increaseAmp, EffectInstance newPotionEffect) {

        EffectInstance effectInstance;
        if (oldEffect.equals(newEffect) && checkAmp(oldAmp, newAmp, ampCap)) {
            if (checkDur(oldDur, newDur, durCap)) {
                effectInstance = new EffectInstance(newEffect, increaseDur, increaseAmp);
                newPotionEffect.update(effectInstance);
            }else {
                effectInstance = new EffectInstance(newEffect, oldDur, increaseAmp);
                newPotionEffect.update(effectInstance);
            }
        } else if(oldEffect.equals(newEffect) && !checkAmp(oldAmp, newAmp, ampCap) && checkDur(oldDur, newDur, durCap)) {
            effectInstance = new EffectInstance(newEffect, increaseDur, oldAmp);
            newPotionEffect.update(effectInstance);
        }
    }

    private static void useItemUpdatePotionEffect(PlayerEntity playerEntity, ItemStack itemStack) {

        final Potion potion = PotionUtils.getPotion(itemStack);
        final List<EffectInstance> potionEffects = potion.getEffects();
        final Map<Effect, EffectInstance> effectInstanceMap = playerEntity.getActiveEffectsMap();

        if (!effectInstanceMap.isEmpty()) {

            if (!potionEffects.isEmpty()) {

                potionEffects.forEach(potionEffect -> {
                    if (potionEffect != null) {

                        final Effect newEffect = potionEffect.getEffect();
                        final int newAmp = potionEffect.getAmplifier();
                        final int newDur = potionEffect.getDuration();

                        int ampCap = 50;
                        int durCap = 30000;

                        if (PotionStackerConfig.effectCaps.containsKey(newEffect)) {
                            ampCap = PotionStackerConfig.effectCaps.get(newEffect).getAmpMax();
                            durCap = PotionStackerConfig.effectCaps.get(newEffect).getDurMax();
                        }

                        if (effectInstanceMap.containsKey(newEffect) && effectInstanceMap.get(newEffect).getEffect().equals(newEffect.getEffect()) && (!PotionStackerConfig.effectsBlacklist.contains(newEffect.getEffect())) && (newDur > 0) ) {

                            final int oldAmp = effectInstanceMap.get(newEffect).getAmplifier();
                            final int oldDur = effectInstanceMap.get(newEffect).getDuration();

                            int increaseAmp = oldAmp + (newAmp + 1);
                            int increaseDur = newDur + oldDur;

                            EffectInstance effectInstance;

                            if (checkAmp(oldAmp, newAmp, ampCap)) {

                                if (checkDur(oldDur, newDur, durCap)) {
                                    effectInstance = new EffectInstance(effectInstanceMap.get(newEffect).getEffect(), increaseDur, increaseAmp);
                                } else {
                                    effectInstance = new EffectInstance(effectInstanceMap.get(newEffect).getEffect(), oldDur, increaseAmp);
                                }
                                playerEntity.removeEffect(effectInstanceMap.get(newEffect).getEffect());
                                playerEntity.addEffect(effectInstance);

                            } else if(effectInstanceMap.get(newEffect).getEffect().equals(newEffect.getEffect()) && !checkAmp(oldAmp, newAmp, ampCap) && checkDur(oldDur, newDur, durCap)) {
                                effectInstance = new EffectInstance(effectInstanceMap.get(newEffect).getEffect(), increaseDur, oldAmp);
                                playerEntity.removeEffect(effectInstanceMap.get(newEffect).getEffect());
                                playerEntity.addEffect(effectInstance);
                            }
                        }
                    }
                });
            }
        }
    }

    private static boolean checkAmp(int oldAmp, int newAmp, int ampCap) {
        oldAmp += newAmp +1;
        return oldAmp <= (ampCap -1);
    }

    private static boolean checkDur(int oldDur, int newDur, int durCap) {
        oldDur += newDur;
        return oldDur <= durCap;
    }
}
