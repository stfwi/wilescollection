package wile.wilescollection;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import wile.wilescollection.blocks.ExtLadderBlock;
import wile.wilescollection.detail.ModRenderers;
import wile.wilescollection.libmc.Auxiliaries;
import wile.wilescollection.libmc.OptionalRecipeCondition;
import wile.wilescollection.libmc.Overlay;
import wile.wilescollection.libmc.Registries;


@Mod("wilescollection")
public class ModWilesCollection
{
  public static final String MODID = "wilescollection";
  public static final String MODNAME = "WilE's Collection";
  public static final int VERSION_DATAFIXER = 0;
  private static final Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

  public ModWilesCollection()
  {
    Auxiliaries.init(MODID, LOGGER, ModConfig::getServerConfig);
    Auxiliaries.logGitVersion(MODNAME);
    Registries.init(MODID, "rustic_iron_lantern", (reg)->reg.register(FMLJavaModLoadingContext.get().getModEventBus()));
    ModContent.init(MODID);
    OptionalRecipeCondition.init(MODID, LOGGER);
    ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, ModConfig.COMMON_CONFIG_SPEC);
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onSetup);
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegisterModels);
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCreativeModeTabContents);
    MinecraftForge.EVENT_BUS.register(this);
  }

  public static Logger logger() { return LOGGER; }


  private void onSetup(final FMLCommonSetupEvent event)
  {
    CraftingHelper.register(OptionalRecipeCondition.Serializer.INSTANCE);
    wile.wilescollection.libmc.Networking.init(MODID);
  }

  private void onClientSetup(final FMLClientSetupEvent event)
  {
    Overlay.TextOverlayGui.on_config(0.75, 0x00ffaa00, 0x55333333, 0x55333333, 0x55444444);
    wile.wilescollection.libmc.Networking.OverlayTextMessage.setHandler(Overlay.TextOverlayGui::show);
    ModContent.registerMenuGuis(event);
    ModContent.registerBlockEntityRenderers(event);
    ModContent.processContentClientSide(event);
  }

  private void onRegisterModels(final ModelEvent.RegisterAdditional event)
  {
    ModRenderers.CraftingTableTer.registerModels().forEach(event::register);
    ModRenderers.LabeledCrateTer.registerModels().forEach(event::register);
    ModRenderers.TrackerIster.registerModels().forEach(event::register);
    ModRenderers.ProspectingDowserIster.registerModels().forEach(event::register);
  }

  private void onCreativeModeTabContents(BuildCreativeModeTabContentsEvent event)
  {}

  @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
  public static class ForgeEvents
  {
    @SubscribeEvent
    public static void onConfigLoad(final ModConfigEvent.Loading event)
    { ModConfig.apply(); }

    @SubscribeEvent
    public static void onConfigReload(final ModConfigEvent.Reloading event)
    { try { ModConfig.apply(); } catch(Throwable e) { logger().error("Failed to load changed config: " + e.getMessage()); } }
  }

  @OnlyIn(Dist.CLIENT)
  @Mod.EventBusSubscriber(Dist.CLIENT)
  public static class ForgeClientEvents
  {
    @SubscribeEvent
    public static void onRenderGui(net.minecraftforge.client.event.RenderGuiOverlayEvent.Post event)
    { Overlay.TextOverlayGui.INSTANCE.onRenderGui(event.getGuiGraphics()); }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onRenderWorldOverlay(net.minecraftforge.client.event.RenderLevelStageEvent event)
    {
      if(event.getStage() == net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_WEATHER) {
        Overlay.TextOverlayGui.INSTANCE.onRenderWorldOverlay(event.getPoseStack(), event.getPartialTick());
      }
    }
  }

  @SubscribeEvent
  public void onPlayerEvent(final LivingEvent.LivingTickEvent event)
  {
    if(!(event.getEntity() instanceof final Player player)) return;
    if(player.onClimbable()) ExtLadderBlock.onPlayerUpdateEvent(player);
  }

}
