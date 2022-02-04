package wile.wilescollection;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
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
import wile.wilescollection.libmc.detail.Registries;


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
    Registries.init(MODID, "sign_decor");
    ModContent.init(MODID);
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
    ModContent.registerMenuGuis(event);
    ModContent.registerBlockEntityRenderers(event);
    ModContent.processContentClientSide(event);
    wile.wilescollection.libmc.detail.Overlay.register();
  }

  @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
  public static class ForgeEvents
  {
    @SubscribeEvent
    public static void onRegisterBlocks(final RegistryEvent.Register<Block> event)
    { Registries.onBlockRegistry((rl, block)->event.getRegistry().register(block)); }

    @SubscribeEvent
    public static void onRegistryItems(final RegistryEvent.Register<Item> event)
    { Registries.onItemRegistry((rl, item)->event.getRegistry().register(item)); }

    @SubscribeEvent
    public static void onRegisterBlockEntities(final RegistryEvent.Register<BlockEntityType<?>> event)
    { Registries.onBlockEntityRegistry((rl, tet)->event.getRegistry().register(tet)); }

    @SubscribeEvent
    public static void onRegisterEntityTypes(final RegistryEvent.Register<EntityType<?>> event)
    { Registries.onEntityRegistry((rl, et)->event.getRegistry().register(et)); }

    @SubscribeEvent
    public static void onRegisterMenuTypes(final RegistryEvent.Register<MenuType<?>> event)
    { Registries.onMenuTypeRegistry((rl, ct)->event.getRegistry().register(ct)); }

    @SubscribeEvent
    public static final void onRegisterModels(final ModelRegistryEvent event)
    { ModContent.registerModels(); }

    @SubscribeEvent
    public static final void onRegisterRecipes(final RegistryEvent.Register<RecipeSerializer<?>> event)
    { event.getRegistry().register(wile.wilescollection.libmc.detail.ExtendedShapelessRecipe.SERIALIZER); }

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
  // Player update event
  //
  @SubscribeEvent
  public void onPlayerEvent(final LivingEvent.LivingUpdateEvent event)
  {
    if((event.getEntity().level == null) || (!(event.getEntity() instanceof final Player player))) return;
    if(player.onClimbable()) ExtLadderBlock.onPlayerUpdateEvent(player);
  }

}
