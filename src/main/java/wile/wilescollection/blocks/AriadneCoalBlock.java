/*
 * @file BlockAriadneCoal.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2019 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Common functionality class for decor blocks.
 * Mainly needed for:
 * - MC block defaults.
 * - Tooltip functionality
 * - Model initialisation
 * - Accumulating "deprecated" warnings from Block where "overriding/implementing is fine".
 */
package wile.wilescollection.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import wile.wilescollection.libmc.blocks.StandardBlocks;
import wile.wilescollection.libmc.detail.Auxiliaries;
import wile.wilescollection.libmc.detail.Registries;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AriadneCoalBlock extends StandardBlocks.BaseBlock
{
  public static final IntegerProperty ORIENTATION = IntegerProperty.create("orientation", 0, 15);
  public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

  final Map<Direction.Axis, VoxelShape[]> aabbs = new HashMap<>();

  public AriadneCoalBlock(long config, BlockBehaviour.Properties properties)
  {
    super(config, properties);
    registerDefaultState(super.defaultBlockState().setValue(ORIENTATION, 0));
    aabbs.put(Direction.Axis.X, new VoxelShape[] {
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,0, 0.1,16,16)),
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,0, 0.1,16,16)),
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,0, 0.1,16,16)),
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,0, 0.1,16,16)),
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,0, 0.1,16,16)),
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,0, 0.1,16,16)),
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,0, 0.1,16,16)),
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,0, 0.1,16,16)),
      Shapes.create(Auxiliaries.getPixeledAABB(15.9,0,0, 16,16,16)),
      Shapes.create(Auxiliaries.getPixeledAABB(15.9,0,0, 16,16,16)),
      Shapes.create(Auxiliaries.getPixeledAABB(15.9,0,0, 16,16,16)),
      Shapes.create(Auxiliaries.getPixeledAABB(15.9,0,0, 16,16,16)),
      Shapes.create(Auxiliaries.getPixeledAABB(15.9,0,0, 16,16,16)),
      Shapes.create(Auxiliaries.getPixeledAABB(15.9,0,0, 16,16,16)),
      Shapes.create(Auxiliaries.getPixeledAABB(15.9,0,0, 16,16,16)),
      Shapes.create(Auxiliaries.getPixeledAABB(15.9,0,0, 16,16,16)),
    });
    aabbs.put(Direction.Axis.Y, new VoxelShape[] {
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,0, 16,0.1,16)),
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,0, 16,0.1,16)),
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,0, 16,0.1,16)),
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,0, 16,0.1,16)),
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,0, 16,0.1,16)),
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,0, 16,0.1,16)),
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,0, 16,0.1,16)),
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,0, 16,0.1,16)),
      Shapes.create(Auxiliaries.getPixeledAABB(0,15.9,0, 16,16,16)),
      Shapes.create(Auxiliaries.getPixeledAABB(0,15.9,0, 16,16,16)),
      Shapes.create(Auxiliaries.getPixeledAABB(0,15.9,0, 16,16,16)),
      Shapes.create(Auxiliaries.getPixeledAABB(0,15.9,0, 16,16,16)),
      Shapes.create(Auxiliaries.getPixeledAABB(0,15.9,0, 16,16,16)),
      Shapes.create(Auxiliaries.getPixeledAABB(0,15.9,0, 16,16,16)),
      Shapes.create(Auxiliaries.getPixeledAABB(0,15.9,0, 16,16,16)),
      Shapes.create(Auxiliaries.getPixeledAABB(0,15.9,0, 16,16,16)),
    });
    aabbs.put(Direction.Axis.Z, new VoxelShape[] {
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,0   , 16,16,0.1)),
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,0   , 16,16,0.1)),
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,0   , 16,16,0.1)),
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,0   , 16,16,0.1)),
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,0   , 16,16,0.1)),
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,0   , 16,16,0.1)),
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,0   , 16,16,0.1)),
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,0   , 16,16,0.1)),
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,15.9, 16,16,16 )),
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,15.9, 16,16,16 )),
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,15.9, 16,16,16 )),
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,15.9, 16,16,16 )),
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,15.9, 16,16,16 )),
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,15.9, 16,16,16 )),
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,15.9, 16,16,16 )),
      Shapes.create(Auxiliaries.getPixeledAABB(0,0,15.9, 16,16,16 )),
    });
  }

  @Override
  public Item asItem()
  { return Registries.getItem("ariadne_coal"); }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag flag)
  { Auxiliaries.Tooltip.addInformation(stack, world, tooltip, flag, true); }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
  { builder.add(ORIENTATION, AXIS); }

  @Override
  @SuppressWarnings("deprecation")
  public VoxelShape getShape(BlockState state, BlockGetter source, BlockPos pos, CollisionContext selectionContext)
  { return aabbs.get(state.getValue(AXIS))[state.getValue(ORIENTATION)]; }

  @Override
  @SuppressWarnings("deprecation")
  public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext selectionContext)
  { return Shapes.empty(); }

  @Override
  @SuppressWarnings("deprecation")
  public boolean isPathfindable(BlockState state, BlockGetter world, BlockPos pos, PathComputationType type)
  { return true; }

  @Override
  @SuppressWarnings("deprecation")
  public boolean canBeReplaced(BlockState state, BlockPlaceContext context)
  { return true; }

  @Override
  @SuppressWarnings("deprecation")
  public PushReaction getPistonPushReaction(BlockState state)
  { return PushReaction.DESTROY; }

  @Override
  @SuppressWarnings("deprecation")
  public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder)
  { return Collections.singletonList(ItemStack.EMPTY); }

  @Override
  public boolean onDestroyedByPlayer(BlockState state, Level world, BlockPos pos, Player player, boolean willHarvest, FluidState fluid)
  { return world.removeBlock(pos, false); }

  @Override
  @SuppressWarnings("deprecation")
  public void attack(BlockState state, Level world, BlockPos pos, Player player)
  { world.removeBlock(pos, false); }

  @Override
  @SuppressWarnings("deprecation")
  public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving)
  {
    if(world.isClientSide) return;
    Direction facing = switch(state.getValue(AXIS)) {
      case X -> (state.getValue(ORIENTATION) <= 7) ? Direction.WEST : Direction.EAST;
      case Y -> (state.getValue(ORIENTATION) <= 7) ? Direction.DOWN : Direction.UP;
      case Z -> (state.getValue(ORIENTATION) <= 7) ? Direction.NORTH : Direction.SOUTH;
    };
    if(!pos.relative(facing).equals(fromPos)) return;
    if(Block.isFaceFull(world.getBlockState(fromPos).getCollisionShape(world, fromPos, CollisionContext.empty()), facing.getOpposite())) return;
    world.removeBlock(pos, isMoving);
  }
}
