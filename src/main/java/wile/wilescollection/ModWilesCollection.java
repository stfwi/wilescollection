package wile.wilescollection;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wile.wilescollection.blocks.ExtLadderBlock;
import wile.wilescollection.libmc.detail.Auxiliaries;
import wile.wilescollection.libmc.detail.OptionalRecipeCondition;


@Mod("wilescollection")
public class ModWilesCollection
{
  public static final String MODID = "wilescollection";
  public static final String MODNAME = "WilE's Collection";
  public static final int VERSION_DATAFIXER = 0;
  private static final Logger LOGGER = LogManager.getLogger();

  public ModWilesCollection()
  {
    Auxiliaries.init(MODID, LOGGER, ModConfig::getServerConfig);
    Auxiliaries.logGitVersion(MODNAME);
    OptionalRecipeCondition.init(MODID, LOGGER);
    ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, ModConfig.COMMON_CONFIG_SPEC);
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onSetup);
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    MinecraftForge.EVENT_BUS.register(this);
  }

  public static final Logger logger() { return LOGGER; }

  //
  // Events
  //

  private void onSetup(final FMLCommonSetupEvent event)
  {
    CraftingHelper.register(OptionalRecipeCondition.Serializer.INSTANCE);
    wile.wilescollection.libmc.detail.Networking.init(MODID);
  }

  private void onClientSetup(final FMLClientSetupEvent event)
  {
    ModContent.registerContainerGuis(event);
    ModContent.registerTileEntityRenderers(event);
    ModContent.processContentClientSide(event);
    wile.wilescollection.libmc.detail.Overlay.register();
  }

  @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
  public static class ForgeEvents
  {
    @SubscribeEvent
    public static void onBlocksRegistry(final RegistryEvent.Register<Block> event)
    { ModContent.registerBlocks(event); }

    @SubscribeEvent
    public static void onItemRegistry(final RegistryEvent.Register<Item> event)
    { ModContent.registerItems(event); ModContent.registerBlockItems(event); }

    @SubscribeEvent
    public static void onTileEntityRegistry(final RegistryEvent.Register<BlockEntityType<?>> event)
    { ModContent.registerTileEntities(event); }

    @SubscribeEvent
    public static void onRegisterEntityTypes(final RegistryEvent.Register<EntityType<?>> event)
    { ModContent.registerEntities(event); }

    @SubscribeEvent
    public static void onRegisterContainerTypes(final RegistryEvent.Register<MenuType<?>> event)
    { ModContent.registerContainers(event); }

    @SubscribeEvent
    public static final void onRegisterModels(final ModelRegistryEvent event)
    { ModContent.registerModels(); }

    @SubscribeEvent
    public static void onConfigLoad(final ModConfigEvent.Loading event)
    { ModConfig.apply(); }

    @SubscribeEvent
    public static void onConfigReload(final ModConfigEvent.Reloading event)
    {
      try {
        logger().info("Config file changed {}", event.getConfig().getFileName());
        ModConfig.apply();
      } catch(Throwable e) {
        logger().error("Failed to load changed config: " + e.getMessage());
      }
    }
  }

  //
  // Item group / creative tab
  //
  public static final CreativeModeTab ITEMGROUP = (new CreativeModeTab("tab" + MODID) {
    @OnlyIn(Dist.CLIENT)
    public ItemStack makeIcon()
    { return new ItemStack(ModContent.CRAFTING_TABLE); }
  });

  //
  // Player update event
  //
  @SubscribeEvent
  public void onPlayerEvent(final LivingEvent.LivingUpdateEvent event)
  {
    if((event.getEntity().level == null) || (!(event.getEntity() instanceof final Player player))) return;
    if(player.onClimbable()) ExtLadderBlock.onPlayerUpdateEvent(player);
  }

}
