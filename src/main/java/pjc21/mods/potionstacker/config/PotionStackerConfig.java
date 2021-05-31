package pjc21.mods.potionstacker.config;

import net.minecraft.potion.Effect;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import pjc21.mods.potionstacker.PotionStacker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Mod.EventBusSubscriber(modid = PotionStacker.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PotionStackerConfig {

    public static final PotionSettings COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    static
    {
        final Pair<PotionSettings, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(PotionSettings::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public static final List<Effect> effectsBlacklist = new ArrayList<>();
    public static final Map<Effect, EffectValues> effectCaps = new HashMap<>();
    public static int minDurationStackable = 40;
    public static boolean allowAllEffectSources = true;
    public static boolean allowAllEntities = true;
    public static boolean allowAmbient = true;

    public static class PotionSettings {

        private static ForgeConfigSpec.ConfigValue<List<? extends String>> potionEffectsBlacklist;
        private static ForgeConfigSpec.ConfigValue<List<? extends String>> potionEffectCaps;
        public static ForgeConfigSpec.IntValue minDurationStackValue;
        public static ForgeConfigSpec.BooleanValue allowForAllEffectSources;
        public static ForgeConfigSpec.BooleanValue allowAllEntities;
        public static ForgeConfigSpec.BooleanValue allowAmbient;
        public static ForgeConfigSpec.ConfigValue<String> notes;

        public PotionSettings(ForgeConfigSpec.Builder builder) {
            builder.comment("Potion Stacker Settings").push("settings");

            allowForAllEffectSources = builder.push("allowForAllEffectSources").comment("Should effects from any source stack or just for potions that players drink\n"
                    + "Set to true and effects from any source will stack, i.e. potions/splash/lingering/arrows/beacons/from mobs hits like hunger from husks etc..\n"
                    + "and will apply to any entity that the potion effect can be applied to if allowAllEntities=true.\n"
                    + "Set to false to only allow effect stacking from potions that players drink.")
                    .define("allowForAllEffectSources", true);

            allowAllEntities = builder.comment("Should effects stack on all entities or just players\nSet to 'false' to only allow stacking on players\n"
                    + "If allowForAllEffectSources=false this has no effect")
                    .define("allowAllEntities", true);

            allowAmbient = builder.comment("Should ambient effects stack\nThis is area effects like beacons\n"
                    + "If allowForAllEffectSources=false this has no effect")
                    .define("allowAmbient", true);

            minDurationStackValue = builder.comment("Minimum effect duration able to stack\n"
                    + "This is useful to only allow stacking if the effects duration is above this amount\n"
                    + "Example: If a modded armor adds a 15 second permanent effect set this to 16 seconds (640) to not stack the effects on the armor\n"
                    + "Any effect that has a lower duration than this will not stack, so this will affect splash potions as there duration depends on how much of the splash you get\n"
                    + "If allowForAllEffectSources=false this has no effect")
                    .defineInRange("minDurationStackValue", 40, 0, 24000);
            builder.pop(1);

            builder.comment("Potion Effect Blacklist").push("potionEffectsBlacklist");
            builder.comment("A list of effects to blacklist from stacking.\nFormat: modid:potion_effect_name, Example: [\"minecraft:slowness\", \"minecraft:mining_fatigue\", \"minecraft:nausea\"]");
            potionEffectsBlacklist = builder.defineList("potionEffectsBlacklist", getDefaultPotionEffectsBlacklist(), lt -> lt instanceof String);
            builder.pop(1);

            builder.comment("Potion Effect Caps").push("potionEffectCaps");
            builder.comment("A list of effects max amplifier & duration caps.\nDefault max amplifier is 50 & duration is 30000 (25m).\nFormat: modid:potion_effect_name|amplifier_cap|duration_cap, Example: [\"minecraft:jump_boost|94|24000\", \"minecraft:speed|200|72000\"]");
            potionEffectCaps = builder.defineList("potionEffectCaps", getDefaultPotionEffectsCaps(), lt -> lt instanceof String);
            builder.pop(1);

            builder.comment("Notes:").push("notes");
            builder.comment("By default minecraft's inventory gui will only display the effect level up to 10 (X) and after that will just have the effect name,\n"
                    + "The duration will only show up to 27:17 and after that will just display xx:xx\n"
                    + "So I have added a keybinding to display a player message with the current effects info, by default the button is 'n' but can be set in options controls.\n"
                    + "Some effects just don't work or have odd behaviour above a certain level, for instance jump boost in sp above level 128 you can no longer jump, in server same happens if above around level 94.\n"
                    + "Slowness above level 6 & you can no longer move, figured this could easily be used to grief players so would probably want to blacklist or set the amplifier cap & duration cap.\n"
                    + "So you will most likely want to test any effects and figure out what amplifier caps to set & what to blacklist.\n"
                    + "I have only allowed for hours/minutes/seconds for the duration so if you go above 23:59:59 I'm not sure what that will look like.");
            notes = builder.define("notes", "notes");
            builder.pop(1);

            builder.pop();
        }
    }

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfig.ModConfigEvent configEvent) {
        if(configEvent.getConfig().getSpec() == PotionStackerConfig.COMMON_SPEC) {
            bakeIt();
        }
    }

    public static void bakeIt() {
        loadConfigPotionEffectsBlacklist();
        loadConfigPotionEffectsEffectCaps();
        minDurationStackable = PotionSettings.minDurationStackValue.get();
        allowAllEffectSources = PotionSettings.allowForAllEffectSources.get();
        allowAllEntities = PotionSettings.allowAllEntities.get();
        allowAmbient = PotionSettings.allowAmbient.get();
    }

    private static List<String> getDefaultPotionEffectsBlacklist() {
        List<String> effectsBlacklist = new ArrayList<>();

        effectsBlacklist.add(Effects.MOVEMENT_SLOWDOWN.getRegistryName().toString());
        effectsBlacklist.add(Effects.CONFUSION.getRegistryName().toString());
        effectsBlacklist.add(Effects.DIG_SLOWDOWN.getRegistryName().toString());

        return effectsBlacklist;
    }

    private static List<String> getDefaultPotionEffectsCaps() {
        List<String> effectsCaps = new ArrayList<>();

        effectsCaps.add(Effects.MOVEMENT_SPEED.getRegistryName().toString()+"|200|72000");
        effectsCaps.add(Effects.JUMP.getRegistryName().toString()+"|94|24000");

        return effectsCaps;
    }

    private static void loadConfigPotionEffectsBlacklist() {
        effectsBlacklist.clear();

        Pattern validEntry = Pattern.compile("[\\w:]+");
        if (!PotionSettings.potionEffectsBlacklist.get().isEmpty()) {
            PotionSettings.potionEffectsBlacklist.get().forEach(entry -> {
                if (validEntry.matcher(entry).matches()) {
                    if (!entry.isEmpty()) {
                        Effect potionEffect = ForgeRegistries.POTIONS.getValue(new ResourceLocation(entry));
                        effectsBlacklist.add(potionEffect);
                    }
                } else {
                    PotionStacker.LOGGER.error("Potion Effect: " + entry + " is not valid.\n"
                            + "Format: modid:potion_effect_name, Example: [\"minecraft:slowness\", \"minecraft:mining_fatigue\", \"minecraft:nausea\"]\"\n");
                }
            });
        }
    }

    private static void loadConfigPotionEffectsEffectCaps() {
        effectCaps.clear();

        Pattern validEntry = Pattern.compile("[\\w:]+[\\d\\w|]+");
        if (!PotionSettings.potionEffectCaps.get().isEmpty()) {
            PotionSettings.potionEffectCaps.get().forEach(entry -> {
                if (validEntry.matcher(entry).matches()) {
                    String[] entryParts = entry.split("\\|");

                    String name = entryParts[0];
                    String amp = entryParts[1];
                    String dur = entryParts[2];

                    if (!name.isEmpty()) {
                        Effect potionEffect = ForgeRegistries.POTIONS.getValue(new ResourceLocation(name));
                        int amplifier = Integer.parseInt(amp);
                        int duration = Integer.parseInt(dur);

                        EffectValues effectValues = new EffectValues();
                        effectValues.setAmpMax(amplifier);
                        effectValues.setDurMax(duration);

                        effectCaps.put(potionEffect, effectValues);
                    }
                } else {
                    PotionStacker.LOGGER.error("Potion Effect Entry: " + entry + " is not valid.\n"
                            + "Format: modid:potion_effect_name|amplifier|duration\n"
                            + "Example: [\"minecraft:speed|100|72000\", \"minecraft:jump_boost|94|24000\"]\"\n");
                }
            });
        }
    }
}
