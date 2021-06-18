package wile.wilescollection;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import wile.wilescollection.blocks.*;
import wile.wilescollection.libmc.detail.Auxiliaries;
import wile.wilescollection.libmc.detail.OptionalRecipeCondition;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


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
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onSetup);
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    FMLJavaModLoadingContext.get().getModEventBus().addListener(ForgeEvents::onConfigLoad);
    FMLJavaModLoadingContext.get().getModEventBus().addListener(ForgeEvents::onConfigReload);
    ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, ModConfig.COMMON_CONFIG_SPEC);
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
    public static void onTileEntityRegistry(final RegistryEvent.Register<TileEntityType<?>> event)
    { ModContent.registerTileEntities(event); }

    @SubscribeEvent
    public static void onRegisterEntityTypes(final RegistryEvent.Register<EntityType<?>> event)
    { ModContent.registerEntities(event); }

    @SubscribeEvent
    public static void onRegisterContainerTypes(final RegistryEvent.Register<ContainerType<?>> event)
    { ModContent.registerContainers(event); }

    @SubscribeEvent
    public static final void onRegisterModels(final ModelRegistryEvent event)
    { ModContent.registerModels(); }

    public static void onConfigLoad(net.minecraftforge.fml.config.ModConfig.Loading configEvent)
    { ModConfig.apply(); }

    public static void onConfigReload(net.minecraftforge.fml.config.ModConfig.Reloading configEvent)
    {
      try {
        ModConfig.apply();
      } catch(Throwable e) {
        ModWilesCollection.logger().error("Failed to load changed config: " + e.getMessage());
      }
    }

    @SubscribeEvent
    public static void onDataGeneration(GatherDataEvent event)
    {
      event.getGenerator().addProvider(new wile.wilescollection.libmc.datagen.LootTableGen(event.getGenerator(), ModContent::allBlocks));
    }
  }

  //
  // Item group / creative tab
  //
  public static final ItemGroup ITEMGROUP = (new ItemGroup("tab" + MODID) {
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
    if((event.getEntity().level == null) || (!(event.getEntity() instanceof PlayerEntity))) return;
    final PlayerEntity player = (PlayerEntity)event.getEntity();
    if(player.onClimbable()) ExtLadderBlock.onPlayerUpdateEvent(player);
  }

}
