package com.gly091020.CreateTreadmill;

import com.gly091020.CreateTreadmill.block.TreadmillBlock;
import com.gly091020.CreateTreadmill.block.TreadmillBlockEntity;
import com.gly091020.CreateTreadmill.config.TreadmillConfig;
import com.gly091020.CreateTreadmill.item.TreadmillItem;
import com.gly091020.CreateTreadmill.maid.MaidPlugin;
import com.gly091020.CreateTreadmill.renderer.TreadmillRenderer;
import com.gly091020.CreateTreadmill.renderer.TreadmillVisual;
import com.mojang.logging.LogUtils;
import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;

@Mod(CreateTreadmillMod.MOD_ID)
public class CreateTreadmillMod {
    public static final String MOD_ID = "createtreadmill";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static TreadmillConfig CONFIG;

    public static final CreateRegistrate REGISTRIES = CreateRegistrate.create(MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB_REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final ItemEntry<TreadmillItem> TREADMILL_ITEM = REGISTRIES
            .item("treadmill", TreadmillItem::new)
            .register();
    public static final RegistryObject<CreativeModeTab> CREATIVE_MODE_TAB = CREATIVE_MODE_TAB_REGISTER.register("treadmill",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("tab.createtreadmill.title"))
                    .withTabsBefore(AllCreativeModeTabs.PALETTES_CREATIVE_TAB.getId())
                    .icon(TREADMILL_ITEM::asStack)
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(TREADMILL_ITEM, CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
                    })
                    .build()
    );
    public static final BlockEntry<TreadmillBlock> TREADMILL_BLOCK = REGISTRIES
            .block("treadmill", TreadmillBlock::new)
            .initialProperties(SharedProperties::stone)
            .onRegister(b -> BlockStressValues.CAPACITIES.register(b, CONFIG.TREADMILL_STRESS::get))
            .transform(axeOrPickaxe())
            .register();
    public static final BlockEntityEntry<TreadmillBlockEntity> TREADMILL_ENTITY = REGISTRIES
            .blockEntity("treadmill_entity", TreadmillBlockEntity::new)
            .visual(() -> TreadmillVisual::new)
            .renderer(() -> TreadmillRenderer::new)
            .validBlock(TREADMILL_BLOCK)
            .register();

    public static final Map<Integer, LivingEntity> WALKING_ENTITY = new HashMap<>();

    public CreateTreadmillMod() {
        ModLoadingContext context = ModLoadingContext.get();
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Using create's weird config system. Just use forge's config bruh
        var specPair = new ForgeConfigSpec.Builder().configure((builder) -> {
            var cfg = new TreadmillConfig();
            cfg.registerAll(builder);
            return cfg;
        });
        CONFIG = specPair.getLeft();
        CONFIG.specification = specPair.getRight();
        context.registerConfig(ModConfig.Type.COMMON, CONFIG.specification);

        REGISTRIES.registerEventListeners(modBus);
        CREATIVE_MODE_TAB_REGISTER.register(modBus);
        if (ModList.get().isLoaded("touhou_little_maid")) {
            MaidPlugin.registryData(modBus);
        }

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> CreateTreadmillClient.onCtorClient(context, modBus));

    }

    @EventBusSubscriber(value = Dist.CLIENT)
    public static class ClientEventHandler {

        @SubscribeEvent
        public static void onRenderEntity(RenderLivingEvent.Pre<?, ?> event) {
            if (WALKING_ENTITY.containsKey(event.getEntity().getId()) && !(event.getEntity() instanceof Player)) {
                var speed = 1;
                var entity = TreadmillBlockEntity.getBlockEntityByEntity(event.getEntity());
                if (entity != null && Math.abs(entity.getSpeed()) > entity.getSettingSpeed()) {
                    speed = (int) (Math.abs(entity.getSpeed()) / 32);
                }
                event.getEntity().walkAnimation.setSpeed(speed);
            }
        }
    }

    @EventBusSubscriber
    public static class CommonEventHandler {

        @SubscribeEvent
        public static void onEntityDie(LivingDeathEvent deathEvent) {
            var entity = deathEvent.getEntity();
            if (entity.level().getBlockState(entity.blockPosition()).is(TREADMILL_BLOCK.get())) {
                var last = entity.getLastAttacker();
                if (last instanceof ServerPlayer player) {
                    var manager = player.server.getAdvancements();
                    var adv = manager.getAdvancement(new ResourceLocation(MOD_ID, "run_to_die"));
                    if (adv != null) {
                        player.getAdvancements().award(adv, "0");
                    }
                }
            }
        }

    }
}
