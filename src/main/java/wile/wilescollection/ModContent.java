/*
 * @file ModContent.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Definition and initialisation of blocks of this
 * module, along with their tile entities if applicable.
 *
 * Note: Straight forward definition of different blocks/entities
 *       to make recipes, models and texture definitions easier.
 */
package wile.wilescollection;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.commons.lang3.ArrayUtils;
import wile.wilescollection.blocks.*;
import wile.wilescollection.items.*;
import wile.wilescollection.libmc.blocks.StandardBlocks;
import wile.wilescollection.libmc.blocks.StandardBlocks.IStandardBlock;
import wile.wilescollection.libmc.detail.Auxiliaries;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import javax.annotation.Nonnull;


@SuppressWarnings("unused")
public class ModContent
{
  private static final String MODID = ModWilesCollection.MODID;

  //--------------------------------------------------------------------------------------------------------------------

  private static Boolean disallowSpawn(BlockState state, IBlockReader reader, BlockPos pos, EntityType<?> entity) { return false; }

  //--------------------------------------------------------------------------------------------------------------------
  // Blocks
  //--------------------------------------------------------------------------------------------------------------------

  //  public static final StandardBlocks.BaseBlock ACCENTUATED_STONE_BRICK_BLOCK = (StandardBlocks.BaseBlock)(new StandardBlocks.BaseBlock(
  //    StandardBlocks.CFG_DEFAULT,
  //    Block.Properties.create(Material.ROCK, MaterialColor.STONE).hardnessAndResistance(3f, 10f).sound(SoundType.STONE)
  //  )).setRegistryName(new ResourceLocation(MODID, "accentuated_stone_brick_block"));
  //
  //  public static final VariantSlabBlock ACCENTUATED_STONE_BRICK_SLAB = (VariantSlabBlock)(new VariantSlabBlock(
  //    StandardBlocks.CFG_DEFAULT,
  //    Block.Properties.create(Material.ROCK, MaterialColor.STONE).hardnessAndResistance(3f, 10f).sound(SoundType.STONE)
  //  )).setRegistryName(new ResourceLocation(MODID, "accentuated_stone_brick_slab"));
  //
  //  public static final StandardStairsBlock ACCENTUATED_STONE_BRICK_STAIRS = (StandardStairsBlock)(new StandardStairsBlock(
  //    StandardBlocks.CFG_DEFAULT,
  //    ACCENTUATED_STONE_BRICK_BLOCK.getDefaultState(),
  //    Block.Properties.create(Material.ROCK, MaterialColor.STONE).hardnessAndResistance(3f, 10f).sound(SoundType.STONE)
  //  )).setRegistryName(new ResourceLocation(MODID, "accentuated_stone_brick_stairs"));
  //
  //  public static final VariantWallBlock ACCENTUATED_STONE_BRICK_WALL = (VariantWallBlock)(new VariantWallBlock(
  //    StandardBlocks.CFG_DEFAULT,
  //    Block.Properties.create(Material.ROCK, MaterialColor.STONE).hardnessAndResistance(3f, 10f).sound(SoundType.STONE)
  //  )).setRegistryName(new ResourceLocation(MODID, "accentuated_stone_brick_wall"));
  //
  //  public static final StandardBlocks.HorizontalWaterLoggable ACCENTUATED_STONE_BRICK_PANEL = (StandardBlocks.HorizontalWaterLoggable)(new StandardBlocks.HorizontalWaterLoggable(
  //    StandardBlocks.CFG_CUTOUT|StandardBlocks.CFG_HORIZIONTAL|StandardBlocks.CFG_LOOK_PLACEMENT,
  //    Block.Properties.create(Material.ROCK, MaterialColor.STONE).hardnessAndResistance(3f, 10f).sound(SoundType.STONE),
  //    new AxisAlignedBB[] { Auxiliaries.getPixeledAABB( 0,0, 0, 16,16, 8) }
  //  )).setRegistryName(new ResourceLocation(MODID, "accentuated_stone_brick_panel"));

  // -------------------------------------------------------------------------------------------------------------------

  public static final ExtLadderBlock WOOD_LADDER = (ExtLadderBlock)(new ExtLadderBlock(
    StandardBlocks.CFG_DEFAULT,
    Block.Properties.create(Material.WOOD, MaterialColor.WOOD).hardnessAndResistance(0.7f, 3f).sound(SoundType.WOOD).notSolid()
  )).setRegistryName(new ResourceLocation(MODID, "ladder"));

  // -------------------------------------------------------------------------------------------------------------------

  public static final TableBlock WOOD_TABLE = (TableBlock)(new TableBlock(
    StandardBlocks.CFG_CUTOUT,
    Block.Properties.create(Material.WOOD, MaterialColor.WOOD).hardnessAndResistance(1f, 3f).sound(SoundType.WOOD).notSolid(),
    Auxiliaries.getPixeledAABB( 0,14, 0, 16,16,16), // top base aabb
    new AxisAlignedBB[]{ // side NORTH-EAST
      Auxiliaries.getPixeledAABB(14, 0, 0, 16,16, 2),
    }
  )).setRegistryName(new ResourceLocation(MODID, "wood_table"));

  public static final Chair.ChairBlock WOOD_CHAIR = (Chair.ChairBlock)(new Chair.ChairBlock(
    StandardBlocks.CFG_CUTOUT|StandardBlocks.CFG_HORIZIONTAL|StandardBlocks.CFG_LOOK_PLACEMENT,
    Block.Properties.create(Material.WOOD, MaterialColor.WOOD).hardnessAndResistance(1f, 3f).sound(SoundType.WOOD).notSolid(),
    new AxisAlignedBB[]{
      Auxiliaries.getPixeledAABB( 2, 0, 3,  4, 9, 5),
      Auxiliaries.getPixeledAABB(12, 0, 3, 14, 9, 5),
      Auxiliaries.getPixeledAABB(12, 0,12, 14,16,14),
      Auxiliaries.getPixeledAABB( 2, 0,12,  4,16,14),
      Auxiliaries.getPixeledAABB(2.5,7,3.5,13.5,8.875,13),
      Auxiliaries.getPixeledAABB(4,11.25,12,12,15.25,13),
    }
  )).setRegistryName(new ResourceLocation(MODID, "wood_chair"));

  // -------------------------------------------------------------------------------------------------------------------

  public static final OmniLanternBlock IRON_LANTERN = (OmniLanternBlock)(new OmniLanternBlock(
    StandardBlocks.CFG_CUTOUT|StandardBlocks.CFG_FACING_PLACEMENT|StandardBlocks.CFG_OPPOSITE_PLACEMENT|StandardBlocks.CFG_AI_PASSABLE,
    Block.Properties.create(Material.IRON, MaterialColor.IRON).hardnessAndResistance(2f, 4f).sound(SoundType.METAL).setLightLevel((state)->15).notSolid(),
    new AxisAlignedBB[]{
      Auxiliaries.getPixeledAABB( 7, 1, 3, 9,14, 5),
      Auxiliaries.getPixeledAABB( 6, 7, 2,10,13, 6),
      Auxiliaries.getPixeledAABB( 7, 2, 0, 9, 4, 3),
    },
    new AxisAlignedBB[]{
      Auxiliaries.getPixeledAABB( 7, 0, 7,  9,12, 9),
      Auxiliaries.getPixeledAABB( 6, 5, 6, 10,11,10)
    },
    new AxisAlignedBB[]{
      Auxiliaries.getPixeledAABB( 7,13, 7,  9,16, 9),
      Auxiliaries.getPixeledAABB( 6,7.5, 6, 10,13,10)
    }
  )).setRegistryName(new ResourceLocation(MODID, "iron_lantern"));

  // -------------------------------------------------------------------------------------------------------------------

  //  public static final WindowBlock WOOD_WINDOW = (WindowBlock)(new WindowBlock(
  //    StandardBlocks.CFG_LOOK_PLACEMENT,
  //    Block.Properties.create(Material.IRON, MaterialColor.IRON).hardnessAndResistance(2f, 8f).sound(SoundType.METAL).notSolid(),
  //    Auxiliaries.getPixeledAABB(0,0,7.5, 16,16,8.5)
  //  )).setRegistryName(new ResourceLocation(MODID, "wood_window"));
  //
  //  public static final StandardBlocks.DirectedWaterLoggable WOOD_WINDOWSILL = (StandardBlocks.DirectedWaterLoggable)(new StandardBlocks.DirectedWaterLoggable(
  //    StandardBlocks.CFG_CUTOUT|StandardBlocks.CFG_HORIZIONTAL|StandardBlocks.CFG_FACING_PLACEMENT|StandardBlocks.CFG_AI_PASSABLE,
  //    Block.Properties.create(Material.WOOD, MaterialColor.WOOD).hardnessAndResistance(2f, 5f).sound(SoundType.WOOD).notSolid(),
  //    Auxiliaries.getPixeledAABB(0.5,15,10.5, 15.5,16,16)
  //  )).setRegistryName(new ResourceLocation(MODID, "wood_windowsill"));
  //
  //  public static final StandardBlocks.DirectedWaterLoggable WOOD_BROAD_WINDOWSILL = (StandardBlocks.DirectedWaterLoggable)(new StandardBlocks.DirectedWaterLoggable(
  //    StandardBlocks.CFG_CUTOUT|StandardBlocks.CFG_HORIZIONTAL|StandardBlocks.CFG_FACING_PLACEMENT,
  //    Block.Properties.create(Material.WOOD, MaterialColor.WOOD).hardnessAndResistance(2f, 5f).sound(SoundType.WOOD).notSolid(),
  //    Auxiliaries.getPixeledAABB(0,14.5,4, 16,16,16)
  //  )).setRegistryName(new ResourceLocation(MODID, "wood_broad_windowsill"));
  //
  //  public static final StraightPoleBlock THIN_STEEL_POLE = (StraightPoleBlock)(new StraightPoleBlock(
  //    StandardBlocks.CFG_CUTOUT|StandardBlocks.CFG_FACING_PLACEMENT,
  //    Block.Properties.create(Material.IRON, MaterialColor.IRON).hardnessAndResistance(2f, 11f).sound(SoundType.METAL).notSolid(),
  //    Auxiliaries.getPixeledAABB(6,6,0, 10,10,16),
  //    null
  //  )).setRegistryName(new ResourceLocation(MODID, "thin_wood_pole"));
  //
  //  public static final StraightPoleBlock THICK_STEEL_POLE = (StraightPoleBlock)(new StraightPoleBlock(
  //    StandardBlocks.CFG_CUTOUT|StandardBlocks.CFG_FACING_PLACEMENT,
  //    Block.Properties.create(Material.IRON, MaterialColor.IRON).hardnessAndResistance(2f, 11f).sound(SoundType.METAL).notSolid(),
  //    Auxiliaries.getPixeledAABB(5,5,0, 11,11,16),
  //    null
  //  )).setRegistryName(new ResourceLocation(MODID, "thick_wood_pole"));

  // -------------------------------------------------------------------------------------------------------------------

  public static final EdCraftingTable.CraftingTableBlock CRAFTING_TABLE = (EdCraftingTable.CraftingTableBlock)(new EdCraftingTable.CraftingTableBlock(
    StandardBlocks.CFG_CUTOUT|StandardBlocks.CFG_HORIZIONTAL|StandardBlocks.CFG_LOOK_PLACEMENT|StandardBlocks.CFG_OPPOSITE_PLACEMENT,
    Block.Properties.create(Material.WOOD, MaterialColor.WOOD).hardnessAndResistance(1f, 5f).sound(SoundType.WOOD).notSolid(),
    new AxisAlignedBB[]{
      Auxiliaries.getPixeledAABB(0,13,0, 16,16,16),
      Auxiliaries.getPixeledAABB(1, 0,1, 15,16,15)
    }
  )).setRegistryName(new ResourceLocation(MODID, "crafting_table"));

  public static final FluidBarrel.FluidBarrelBlock FLUID_BARREL = (FluidBarrel.FluidBarrelBlock)(new FluidBarrel.FluidBarrelBlock(
    StandardBlocks.CFG_CUTOUT|StandardBlocks.CFG_LOOK_PLACEMENT|StandardBlocks.CFG_OPPOSITE_PLACEMENT,
    Block.Properties.create(Material.WOOD, MaterialColor.WOOD).hardnessAndResistance(0.5f, 5f).sound(SoundType.WOOD).notSolid(),
    new AxisAlignedBB[]{
      Auxiliaries.getPixeledAABB(2, 0,0, 14, 1,16),
      Auxiliaries.getPixeledAABB(1, 1,0, 15, 2,16),
      Auxiliaries.getPixeledAABB(0, 2,0, 16,14,16),
      Auxiliaries.getPixeledAABB(1,14,0, 15,15,16),
      Auxiliaries.getPixeledAABB(2,15,0, 14,16,16),
    }
  )).setRegistryName(new ResourceLocation(MODID, "fluid_barrel"));

  public static final LabeledCrate.LabeledCrateBlock CRATE = (LabeledCrate.LabeledCrateBlock)(new LabeledCrate.LabeledCrateBlock(
    StandardBlocks.CFG_HORIZIONTAL|StandardBlocks.CFG_LOOK_PLACEMENT|StandardBlocks.CFG_OPPOSITE_PLACEMENT,
    Block.Properties.create(Material.WOOD, MaterialColor.WOOD).hardnessAndResistance(0.5f, 5f).sound(SoundType.METAL).notSolid(),
    Auxiliaries.getPixeledAABB(0,0,0, 16,16,16)
  )).setRegistryName(new ResourceLocation(MODID, "crate"));

  // -------------------------------------------------------------------------------------------------------------------

  private static final Block modBlocks[] = {
    CRAFTING_TABLE,
    CRATE,
    FLUID_BARREL,
    WOOD_LADDER,
    WOOD_TABLE,
    WOOD_CHAIR,
    IRON_LANTERN
  };

  private static final Block devBlocks[] = {
  };

  //--------------------------------------------------------------------------------------------------------------------
  // Tile entities bound exclusively to the blocks above
  //--------------------------------------------------------------------------------------------------------------------

  public static final TileEntityType<?> TET_CRAFTING_TABLE = TileEntityType.Builder
    .create(EdCraftingTable.CraftingTableTileEntity::new, CRAFTING_TABLE)
    .build(null)
    .setRegistryName(MODID, "te_crafting_table");

  public static final TileEntityType<?> TET_LABELED_CRATE = TileEntityType.Builder
    .create(LabeledCrate.LabeledCrateTileEntity::new, CRATE)
    .build(null)
    .setRegistryName(MODID, "te_crate");

  public static final TileEntityType<?> TET_FLUID_BARREL = TileEntityType.Builder
    .create(FluidBarrel.FluidBarrelTileEntity::new, FLUID_BARREL)
    .build(null)
    .setRegistryName(MODID, "te_fluid_barrel");

  private static final TileEntityType<?> tile_entity_types[] = {
    TET_CRAFTING_TABLE,
    TET_LABELED_CRATE,
    TET_FLUID_BARREL
  };

  //--------------------------------------------------------------------------------------------------------------------
  // Items
  //--------------------------------------------------------------------------------------------------------------------

  private static Item.Properties default_item_properties()
  { return (new Item.Properties()).group(ModWilesCollection.ITEMGROUP); }

  public static final EdItem RING_ITEM = (EdItem)((new EdItem(default_item_properties()).setRegistryName(MODID, "ring")));

  @SuppressWarnings("all")
  private static final EdItem modItems[] = {
    //RING_ITEM
  };

  //--------------------------------------------------------------------------------------------------------------------
  // Entities bound exclusively to the blocks above
  //--------------------------------------------------------------------------------------------------------------------

  @SuppressWarnings("unchecked")
  public static final EntityType<Chair.EntityChair> ET_CHAIR = (EntityType<Chair.EntityChair>)(
    EntityType.Builder
      .create(Chair.EntityChair::new, EntityClassification.MISC)
      .immuneToFire().size(1e-3f, 1e-3f).disableSerialization()
      .setShouldReceiveVelocityUpdates(false).setUpdateInterval(4)
      .setCustomClientFactory(Chair.EntityChair::customClientFactory)
      .build(new ResourceLocation(MODID, "et_chair").toString())
      .setRegistryName(new ResourceLocation(MODID, "et_chair"))
  );

  private static final EntityType<?> entity_types[] = {
    ET_CHAIR
  };

  //--------------------------------------------------------------------------------------------------------------------
  // Container registration
  //--------------------------------------------------------------------------------------------------------------------

  public static final ContainerType<EdCraftingTable.CraftingTableContainer> CT_TREATED_WOOD_CRAFTING_TABLE;
  public static final ContainerType<LabeledCrate.LabeledCrateContainer> CT_LABELED_CRATE;

  static {
    CT_TREATED_WOOD_CRAFTING_TABLE = (new ContainerType<EdCraftingTable.CraftingTableContainer>(EdCraftingTable.CraftingTableContainer::new));
    CT_TREATED_WOOD_CRAFTING_TABLE.setRegistryName(MODID,"ct_treated_wood_crafting_table");
    CT_LABELED_CRATE = (new ContainerType<LabeledCrate.LabeledCrateContainer>(LabeledCrate.LabeledCrateContainer::new));
    CT_LABELED_CRATE.setRegistryName(MODID,"ct_labeled_crate");
  }

  private static final ContainerType<?> container_types[] = {
    CT_TREATED_WOOD_CRAFTING_TABLE,
    CT_LABELED_CRATE
  };

  //--------------------------------------------------------------------------------------------------------------------
  // Initialisation events
  //--------------------------------------------------------------------------------------------------------------------

  private static ArrayList<Block> registeredBlocks = new ArrayList<>();

  public static ArrayList<Block> allBlocks()
  {
    ArrayList<Block> blocks = new ArrayList<>();
    Collections.addAll(blocks, modBlocks);
    Collections.addAll(blocks, devBlocks);
    return blocks;
  }

  @SuppressWarnings("deprecation")
  public static boolean isExperimentalBlock(Block block)
  { return ArrayUtils.contains(devBlocks, block); }

  @Nonnull
  public static List<Block> getRegisteredBlocks()
  { return Collections.unmodifiableList(registeredBlocks); }

  @Nonnull
  public static List<Item> getRegisteredItems()
  { return new ArrayList<>(); }

  public static final void registerBlocks(final RegistryEvent.Register<Block> event)
  {
    registeredBlocks.addAll(allBlocks());
    for(Block e:registeredBlocks) event.getRegistry().register(e);
  }

  public static final void registerBlockItems(final RegistryEvent.Register<Item> event)
  {
    int n = 0;
    for(Block e:registeredBlocks) {
      ResourceLocation rl = e.getRegistryName();
      if(rl == null) continue;
      if(e instanceof StandardBlocks.IBlockItemFactory) {
        event.getRegistry().register(((StandardBlocks.IBlockItemFactory)e).getBlockItem(e, (new BlockItem.Properties().group(ModWilesCollection.ITEMGROUP))).setRegistryName(rl));
      } else {
        event.getRegistry().register(new BlockItem(e, (new BlockItem.Properties().group(ModWilesCollection.ITEMGROUP))).setRegistryName(rl));
      }
      ++n;
    }
  }

  public static final void registerItems(final RegistryEvent.Register<Item> event)
  { for(Item e:modItems) event.getRegistry().register(e); }

  public static final void registerTileEntities(final RegistryEvent.Register<TileEntityType<?>> event)
  {
    int n_registered = 0;
    for(final TileEntityType<?> e:tile_entity_types) {
      event.getRegistry().register(e);
      ++n_registered;
    }
  }

  public static final void registerEntities(final RegistryEvent.Register<EntityType<?>> event)
  {
    int n_registered = 0;
    for(final EntityType<?> e:entity_types) {
      if((e==ET_CHAIR) && (!registeredBlocks.contains(WOOD_CHAIR))) continue;
      event.getRegistry().register(e);
      ++n_registered;
    }
  }

  public static final void registerContainers(final RegistryEvent.Register<ContainerType<?>> event)
  {
    int n_registered = 0;
    for(final ContainerType<?> e:container_types) {
      event.getRegistry().register(e);
      ++n_registered;
    }
  }

  @OnlyIn(Dist.CLIENT)
  public static final void registerContainerGuis(final FMLClientSetupEvent event)
  {
    ScreenManager.registerFactory(CT_TREATED_WOOD_CRAFTING_TABLE, EdCraftingTable.CraftingTableGui::new);
    ScreenManager.registerFactory(CT_LABELED_CRATE, LabeledCrate.LabeledCrateGui::new);
  }

  @OnlyIn(Dist.CLIENT)
  @SuppressWarnings("unchecked")
  public static final void registerTileEntityRenderers(final FMLClientSetupEvent event)
  {
    ClientRegistry.bindTileEntityRenderer(
      (TileEntityType<EdCraftingTable.CraftingTableTileEntity>)TET_CRAFTING_TABLE,
      wile.wilescollection.detail.ModRenderers.CraftingTableTer::new
    );
    ClientRegistry.bindTileEntityRenderer(
      (TileEntityType<LabeledCrate.LabeledCrateTileEntity>)TET_LABELED_CRATE,
      wile.wilescollection.detail.ModRenderers.DecorLabeledCrateTer::new
    );
  }

  @OnlyIn(Dist.CLIENT)
  public static final void processContentClientSide(final FMLClientSetupEvent event)
  {
    // Block renderer selection
    for(Block block: getRegisteredBlocks()) {
      if(block instanceof IStandardBlock) {
        switch(((IStandardBlock)block).getRenderTypeHint()) {
          case CUTOUT:
            RenderTypeLookup.setRenderLayer(block, RenderType.getCutout());
            break;
          case CUTOUT_MIPPED:
            RenderTypeLookup.setRenderLayer(block, RenderType.getCutoutMipped());
            break;
          case TRANSLUCENT:
            RenderTypeLookup.setRenderLayer(block, RenderType.getTranslucent());
            break;
          case TRANSLUCENT_NO_CRUMBLING:
            RenderTypeLookup.setRenderLayer(block, RenderType.getTranslucentNoCrumbling());
            break;
          case SOLID:
            break;
        }
      }
    }
    // Entity renderers
    RenderingRegistry.registerEntityRenderingHandler(ET_CHAIR,
      manager->(new wile.wilescollection.detail.ModRenderers.InvisibleEntityRenderer<Chair.EntityChair>(manager))
    );
  }

}
