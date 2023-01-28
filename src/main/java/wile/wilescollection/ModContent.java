/*
 * @file ModContent.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Definition and initialisation mod features.
 */
package wile.wilescollection;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import wile.wilescollection.blocks.*;
import wile.wilescollection.detail.ModRenderers;
import wile.wilescollection.items.*;
import wile.wilescollection.libmc.blocks.*;
import wile.wilescollection.libmc.Auxiliaries;
import wile.wilescollection.libmc.Materials;
import wile.wilescollection.libmc.Registries;


@SuppressWarnings("unused")
public class ModContent
{
  private static class detail
  {
    public static String MODID = "";
    private static Materials.CustomArmorMaterial reinforced_armor_material_ = null;

    public static Boolean disallowSpawn(BlockState state, BlockGetter reader, BlockPos pos, EntityType<?> entity) { return false; }

    public static Materials.CustomArmorMaterial reinforced_armor_material()
    {
      if(reinforced_armor_material_ == null) {
        reinforced_armor_material_ = new Materials.CustomArmorMaterial(
          MODID+":plated_netherite",
          SoundEvents.ARMOR_EQUIP_NETHERITE,
          ()-> Ingredient.of(Items.NETHERITE_INGOT),
          new int[]{4, 8, 10, 4},
          new int[]{13*42, 15*42, 16*42, 11*42},
          15,
          4f,
          0.2f
        );
      }
      return reinforced_armor_material_;
    }

    public static Item.Properties default_item_properties()  { return (new Item.Properties()).tab(Registries.getCreativeModeTab()); }
  }

  public static void init(String modid)
  {
    detail.MODID = modid;
    initTags();
    initBlocks();
    initItems();
    initEntities();
    initPaintings();
    Registries.addRecipeSerializer("crafting_extended_shapeless", ()->wile.wilescollection.libmc.ExtendedShapelessRecipe.SERIALIZER);
  }

  public static void initTags()
  {
    Registries.addOptionalBlockTag("prospectible", new ResourceLocation("minecraft:diamond_ore")); // , "minecraft:ancient_debres"
  }

  public static void initBlocks()
  {
    Registries.addBlock("crafting_table",
      ()->new ExtCraftingTable.CraftingTableBlock(
        StandardBlocks.CFG_CUTOUT|StandardBlocks.CFG_HORIZIONTAL|StandardBlocks.CFG_LOOK_PLACEMENT|StandardBlocks.CFG_OPPOSITE_PLACEMENT,
        BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(0.2f, 5f).sound(SoundType.WOOD).noOcclusion(),
        new AABB[]{
          Auxiliaries.getPixeledAABB(0,13,0, 16,16,16),
          Auxiliaries.getPixeledAABB(1, 0,1, 15,16,15)
        }
      ),
      ExtCraftingTable.CraftingTableTileEntity::new,
      ExtCraftingTable.CraftingTableUiContainer::new
      //MenuScreens.register(CT_CRAFTING_TABLE, ExtCraftingTable.CraftingTableGui::new);
    );
    Registries.addBlock("fluid_barrel",
      ()->new FluidBarrel.FluidBarrelBlock(
        StandardBlocks.CFG_CUTOUT|StandardBlocks.CFG_LOOK_PLACEMENT|StandardBlocks.CFG_OPPOSITE_PLACEMENT,
        BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(0.2f, 5f).sound(SoundType.WOOD).noOcclusion(),
        new AABB[] {
          Auxiliaries.getPixeledAABB(2, 0,0, 14, 1,16),
          Auxiliaries.getPixeledAABB(1, 1,0, 15, 2,16),
          Auxiliaries.getPixeledAABB(0, 2,0, 16,14,16),
          Auxiliaries.getPixeledAABB(1,14,0, 15,15,16),
          Auxiliaries.getPixeledAABB(2,15,0, 14,16,16),
        }
      ),
      FluidBarrel.FluidBarrelItem::new,
      FluidBarrel.FluidBarrelTileEntity::new
    );
    Registries.addBlock("rustic_barrel",
      ()->new BarrelBlock(BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(3f).destroyTime(0.4f).sound(SoundType.WOOD))
    );
    Registries.addBlock("crate",
      ()->new LabeledCrate.LabeledCrateBlock(
        StandardBlocks.CFG_HORIZIONTAL|StandardBlocks.CFG_LOOK_PLACEMENT|StandardBlocks.CFG_OPPOSITE_PLACEMENT,
        BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(0.2f, 5f).sound(SoundType.WOOD).noOcclusion(),
        Auxiliaries.getPixeledAABB(0,0,0, 16,16,16)
      ),
      LabeledCrate.LabeledCrateTileEntity::new,
      LabeledCrate.LabeledCrateContainer::new
    );
    Registries.addBlock("ladder", ()->new ExtLadderBlock(
      StandardBlocks.CFG_DEFAULT,
      BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(0.2f, 3f).sound(SoundType.WOOD).noOcclusion()
    ));
    Registries.addBlock("wood_table", ()->new TableBlock(
      StandardBlocks.CFG_CUTOUT,
      BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(0.2f, 3f).sound(SoundType.WOOD).noOcclusion(),
      Auxiliaries.getPixeledAABB( 0,14, 0, 16,16,16), // top base aabb
      new AABB[]{ // side NORTH-EAST
        Auxiliaries.getPixeledAABB(14, 0, 0, 16,16, 2),
      }
    ));
    Registries.addBlock("wood_chair", ()->new Chair.ChairBlock(
      StandardBlocks.CFG_CUTOUT|StandardBlocks.CFG_HORIZIONTAL|StandardBlocks.CFG_LOOK_PLACEMENT,
      BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(0.2f, 3f).sound(SoundType.WOOD).noOcclusion(),
      new AABB[]{
        Auxiliaries.getPixeledAABB( 2, 0, 3,  4, 9, 5),
        Auxiliaries.getPixeledAABB(12, 0, 3, 14, 9, 5),
        Auxiliaries.getPixeledAABB(12, 0,12, 14,16,14),
        Auxiliaries.getPixeledAABB( 2, 0,12,  4,16,14),
        Auxiliaries.getPixeledAABB(2.5,7,3.5,13.5,8.875,13),
        Auxiliaries.getPixeledAABB(4,11.25,12,12,15.25,13),
      }
    ));
    Registries.addBlock("rustic_wood_planks",
      ()->new Block(BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(1.5f, 3.0f).sound(SoundType.WOOD))
    );
    Registries.addBlock("rustic_wood_slab", ()->new VariantSlabBlock(
      StandardBlocks.CFG_DEFAULT,
      BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(1.5f, 3.0f).sound(SoundType.WOOD)
    ));
    Registries.addBlock("rustic_wood_stairs", ()->new StandardStairsBlock(
      StandardBlocks.CFG_DEFAULT,
      ()->Registries.getBlock("rustic_wood_planks").defaultBlockState(),
      BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(1.5f, 3.0f).sound(SoundType.WOOD)
    ));
    Registries.addBlock("rustic_wood_door", ()->new StandardDoorBlock(
      StandardBlocks.CFG_DEFAULT,
      BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(1.5F, 6f).sound(SoundType.WOOD).noOcclusion(),
      Auxiliaries.getPixeledAABB(15,0, 0, 16,16,16),
      Auxiliaries.getPixeledAABB( 0,0,13, 16,16,16),
      SoundEvents.WOODEN_DOOR_OPEN, SoundEvents.WOODEN_DOOR_CLOSE,
      false
    ));
    Registries.addBlock("rustic_player_wood_door", ()->new StandardDoorBlock(
      StandardBlocks.CFG_DEFAULT,
      BlockBehaviour.Properties.of(Material.METAL, MaterialColor.WOOD).strength(1.5F, 6f).sound(SoundType.WOOD).noOcclusion(),
      Auxiliaries.getPixeledAABB(15,0, 0, 16,16,16),
      Auxiliaries.getPixeledAABB( 0,0,13, 16,16,16),
      SoundEvents.WOODEN_DOOR_OPEN, SoundEvents.WOODEN_DOOR_CLOSE,
      true
    ));
    Registries.addBlock("rustic_wood_trapdoor",
      ()->new TrapDoorBlock(BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.0f).sound(SoundType.WOOD).noOcclusion().isValidSpawn(detail::disallowSpawn))
    );
    Registries.addBlock("rustic_iron_lantern", ()->new OmniLanternBlock(
      StandardBlocks.CFG_CUTOUT|StandardBlocks.CFG_FACING_PLACEMENT|StandardBlocks.CFG_OPPOSITE_PLACEMENT|StandardBlocks.CFG_AI_PASSABLE,
      BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL).strength(0.2f, 4f).sound(SoundType.LANTERN).lightLevel((state)->15).noOcclusion(),
      new AABB[]{
        Auxiliaries.getPixeledAABB( 5, 2, 5, 11,8,11),
        Auxiliaries.getPixeledAABB( 6, 8, 6, 10,9,10),
        Auxiliaries.getPixeledAABB( 7.5, 11, 0, 8.5,13,2),
        Auxiliaries.getPixeledAABB( 7.5, 12, 2, 8.5,13,9)
      },
      new AABB[]{
        Auxiliaries.getPixeledAABB( 5, 0, 5, 11,6,11),
        Auxiliaries.getPixeledAABB( 6, 6, 6, 10,7,10)
      },
      new AABB[]{
        Auxiliaries.getPixeledAABB( 5,  5.75, 5, 11,11.75,11),
        Auxiliaries.getPixeledAABB( 6, 11.75, 6, 10,12.75,10),
        Auxiliaries.getPixeledAABB( 7, 12.00, 7,  9,16.00, 9)
      }
    ));
    Registries.addBlock("rustic_chain", ()->new StandardBlocks.AxisAlignedWaterLoggable(
      StandardBlocks.CFG_CUTOUT|StandardBlocks.CFG_FACING_PLACEMENT,
      BlockBehaviour.Properties.of(Material.METAL, MaterialColor.NONE).strength(0.2f, 6.0F).sound(SoundType.CHAIN).noOcclusion(),
      new AABB[]{
        Auxiliaries.getPixeledAABB( 7,  7, 0, 9,9,16),
      }
    ));
    Registries.addBlock("rustic_iron_framed_window", ()->new WindowBlock(
      StandardBlocks.CFG_CUTOUT|StandardBlocks.CFG_LOOK_PLACEMENT,
      BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL).strength(0.2f, 3f).sound(SoundType.METAL).noOcclusion(),
      Auxiliaries.getPixeledAABB(0,0,7.5, 16,16,8.5)
    ));
    Registries.addBlock("rustic_iron_framed_window_asym", ()->new WindowBlock(
      StandardBlocks.CFG_CUTOUT|StandardBlocks.CFG_LOOK_PLACEMENT,
      BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL).strength(0.2f, 3f).sound(SoundType.METAL).noOcclusion(),
      Auxiliaries.getPixeledAABB(0,0,7.5, 16,16,8.5)
    ));
    Registries.addBlock("rustic_iron_framed_window_diag", ()->new WindowBlock(
      StandardBlocks.CFG_CUTOUT|StandardBlocks.CFG_LOOK_PLACEMENT,
      BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL).strength(0.2f, 3f).sound(SoundType.METAL).noOcclusion(),
      Auxiliaries.getPixeledAABB(0,0,7.5, 16,16,8.5)
    ));
    Registries.addBlock("rustic_iron_framed_window_wide", ()->new WindowBlock(
      StandardBlocks.CFG_CUTOUT|StandardBlocks.CFG_LOOK_PLACEMENT,
      BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL).strength(0.2f, 3f).sound(SoundType.METAL).noOcclusion(),
      Auxiliaries.getPixeledAABB(0,0,7.5, 16,16,8.5)
    ));
    Registries.addBlock("rustic_stone_iron_fence", ()->new VariantWallBlock(
      StandardBlocks.CFG_CUTOUT,
      BlockBehaviour.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.3f, 10f).sound(SoundType.STONE).isValidSpawn(detail::disallowSpawn),
      1.5,16,1,10,14,16, // Visible shape -> pole_width, pole_height, side_width, side_min_y, side_max_low_y, side_max_tall_y,
      3.0,24,3, 0,24,24, // Collision shape -> collision_pole_width, collision_pole_height, collision_side_width, collision_side_min_y, collision_side_max_low_y, collision_side_max_tall_y
      true
    ));
    Registries.addBlock("ariadne_coal_block", ()->new AriadneCoalBlock(
      StandardBlocks.CFG_TRANSLUCENT,
      BlockBehaviour.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.2f, 2f).sound(SoundType.STONE).noCollission().noLootTable()
    ));
    Registries.addBlock("weathered_stone_brick_block", ()->new StandardBlocks.BaseBlock(
      StandardBlocks.CFG_DEFAULT,
      BlockBehaviour.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.5f, 7f).sound(SoundType.STONE).requiresCorrectToolForDrops()
    ));
    Registries.addBlock("weathered_stone_brick_slab", ()->new VariantSlabBlock(
      StandardBlocks.CFG_DEFAULT,
      BlockBehaviour.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.5f, 7f).sound(SoundType.STONE).requiresCorrectToolForDrops()
    ));
    Registries.addBlock("weathered_stone_brick_stairs", ()->new StandardStairsBlock(
      StandardBlocks.CFG_DEFAULT,
      ()->Registries.getBlock("weathered_stone_brick_block").defaultBlockState(),
      BlockBehaviour.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.5f, 7f).sound(SoundType.STONE).requiresCorrectToolForDrops()
    ));
    Registries.addBlock("calloty", ()->new SpecialFlowerBlock(
      StandardBlocks.CFG_DEFAULT,
      BlockBehaviour.Properties.of(Material.PLANT).noCollission().instabreak().sound(SoundType.GRASS).randomTicks()
    ));
  }

  public static void initItems()
  {
    Registries.addItem("rusty_iron_ingot", ()->new ModItem(
      detail.default_item_properties()
    ));
    Registries.addItem("rusty_iron_nugget", ()->new ModItem(
      detail.default_item_properties()
    ));
    Registries.addItem("prospecting_dowser", ()->new ProspectingDowserItem(
      detail.default_item_properties()
    ));
    Registries.addItem("plated_netherite_helmet", ()->new Armors.HelmetArmorItem(
      Armors.CFG_DEFAULT,
      detail.reinforced_armor_material(),
      detail.default_item_properties().fireResistant()
    ));
    Registries.addItem("plated_netherite_chestplate", ()->new Armors.ChestPlateArmorItem(
      Armors.CFG_MAKES_PIGLINS_NEUTRAL,
      detail.reinforced_armor_material(),
      detail.default_item_properties().fireResistant()
    ));
    Registries.addItem("plated_netherite_leggings", ()->new Armors.LeggingsArmorItem(
      Armors.CFG_MAKES_PIGLINS_NEUTRAL,
      detail.reinforced_armor_material(),
      detail.default_item_properties().fireResistant()
    ));
    Registries.addItem("plated_netherite_boots", ()->new Armors.BootsArmorItem(
      Armors.CFG_DEFAULT,
      detail.reinforced_armor_material(),
      detail.default_item_properties().fireResistant()
    ));
    Registries.addItem("peculiar_ring", ()->new Trinkets.TrinketItem(
      0,
      detail.default_item_properties().fireResistant().setNoRepair().rarity(Rarity.RARE).defaultDurability(1000).durability(1000)
    ));
    Registries.addItem("ariadne_coal", ()->new AriadneCoalItem(
      detail.default_item_properties().stacksTo(1).rarity(Rarity.UNCOMMON)
    ));
    Registries.addItem("charged_lapis", ()->new ChargedLapisItem(
      detail.default_item_properties().rarity(Rarity.UNCOMMON)
    ));
    Registries.addItem("charged_lapis_squeezer", ()->new ChargedLapisSqueezerItem(
      detail.default_item_properties().rarity(Rarity.UNCOMMON)
    ));
    Registries.addItem("crushing_hammer", ()->new CrushingHammerItem(
      detail.default_item_properties().stacksTo(1).rarity(Rarity.UNCOMMON)
    ));
    Registries.addItem("crushed_iron", ()->new GritItem(
      detail.default_item_properties()
    ));
    Registries.addItem("crushed_gold", ()->new GritItem(
      detail.default_item_properties()
    ));
    Registries.addItem("crushed_copper", ()->new GritItem(
      detail.default_item_properties()
    ));
    Registries.addItem("tracking_compass", ()->new TrackerItem(
      detail.default_item_properties()
    ));
  }

  public static void initEntities()
  {
    Registries.addEntityType("et_chair", ()->
      EntityType.Builder.of(Chair.EntityChair::new, MobCategory.MISC)
        .fireImmune().sized(1e-3f, 1e-3f).noSave()
        .setShouldReceiveVelocityUpdates(false).setUpdateInterval(4)
        .setCustomClientFactory(Chair.EntityChair::customClientFactory)
        .build(new ResourceLocation(Auxiliaries.modid(), "et_chair").toString())
    );
  }

  public static void initPaintings()
  {
    Registries.addPainting("painting_melon_16x16", 16, 16);
    Registries.addPainting("painting_poppy_meadow_16x32", 16, 32);
    Registries.addPainting("painting_sunflower_64x48", 64, 48);
    Registries.addPainting("painting_valley_64x32", 64, 32);
    Registries.addPainting("painting_mushroom_16x16", 16, 16);
    Registries.addPainting("painting_lillypads_32x16", 32, 16);
    Registries.addPainting("painting_holyrood_park_abstract_32x32", 32, 32);
    Registries.addPainting("painting_flower_abstract_32x32", 32, 32);
  }

  //--------------------------------------------------------------------------------------------------------------------
  // Initialisation events
  //--------------------------------------------------------------------------------------------------------------------

  @OnlyIn(Dist.CLIENT)
  @SuppressWarnings("unchecked")
  public static void registerMenuGuis(final FMLClientSetupEvent event)
  {
    MenuScreens.register((MenuType<ExtCraftingTable.CraftingTableUiContainer>)Registries.getMenuTypeOfBlock("crafting_table"), ExtCraftingTable.CraftingTableGui::new);
    MenuScreens.register((MenuType<LabeledCrate.LabeledCrateContainer>)Registries.getMenuTypeOfBlock("crate"), LabeledCrate.LabeledCrateGui::new);
  }

  @OnlyIn(Dist.CLIENT)
  @SuppressWarnings("unchecked")
  public static void registerBlockEntityRenderers(final FMLClientSetupEvent event)
  {
    BlockEntityRenderers.register((BlockEntityType<ExtCraftingTable.CraftingTableTileEntity>)Registries.getBlockEntityTypeOfBlock("crafting_table"), wile.wilescollection.detail.ModRenderers.CraftingTableTer::new);
    BlockEntityRenderers.register((BlockEntityType<LabeledCrate.LabeledCrateTileEntity>)Registries.getBlockEntityTypeOfBlock("crate"), wile.wilescollection.detail.ModRenderers.LabeledCrateTer::new);
  }

  @OnlyIn(Dist.CLIENT)
  public static void processContentClientSide(final FMLClientSetupEvent event)
  {
    // Block renderer selection
    // -> Disabled/removed in Forge. -> JSON model file root {"render_type":"cutout"/"translucent"}
    //  for(Block block: Registries.getRegisteredBlocks()) {
    //    if(block instanceof IStandardBlock) {
    //      switch(((IStandardBlock)block).getRenderTypeHint()) {
    //        case CUTOUT: ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutout()); break;
    //        case CUTOUT_MIPPED: ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutoutMipped()); break;
    //        case TRANSLUCENT: ItemBlockRenderTypes.setRenderLayer(block, RenderType.translucent()); break;
    //        case TRANSLUCENT_NO_CRUMBLING: ItemBlockRenderTypes.setRenderLayer(block, RenderType.translucentNoCrumbling()); break;
    //        case SOLID: break;
    //      }
    //    }
    //  }
    // Entity renderers
    EntityRenderers.register(Registries.getEntityType("et_chair"), ModRenderers.InvisibleEntityRenderer::new);
  }

}
