/*
 * @file Table.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Base block for tables.
 */
package wile.wilescollection.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.Direction;
import net.minecraft.util.math.*;
import net.minecraft.world.IWorld;
import wile.wilescollection.libmc.blocks.StandardBlocks;

import javax.annotation.Nullable;


public class TableBlock extends StandardBlocks.HorizontalFourWayWaterLoggable implements StandardBlocks.IStandardBlock
{
  public TableBlock(long config, Block.Properties builder, final AxisAlignedBB base_aabb, final AxisAlignedBB[] side_aabb)
  { super(config, builder.tickRandomly(), base_aabb, side_aabb, 0); }

  private BlockState getAdaptedState(BlockState state, IWorld world, BlockPos pos)
  {
    final boolean n = world.getBlockState(pos.offset(Direction.NORTH)).getBlock()==this;
    final boolean e = world.getBlockState(pos.offset(Direction.EAST)).getBlock()==this;
    final boolean s = world.getBlockState(pos.offset(Direction.SOUTH)).getBlock()==this;
    final boolean w = world.getBlockState(pos.offset(Direction.WEST)).getBlock()==this;
    return (state == null) ? (null) : (state
      .with(StandardBlocks.HorizontalFourWayWaterLoggable.NORTH, !n && !e)
      .with(StandardBlocks.HorizontalFourWayWaterLoggable.EAST,  !e && !s)
      .with(StandardBlocks.HorizontalFourWayWaterLoggable.SOUTH, !s && !w)
      .with(StandardBlocks.HorizontalFourWayWaterLoggable.WEST,  !w && !n)
    );
  }

  @Override
  @Nullable
  public BlockState getStateForPlacement(BlockItemUseContext context)
  { return getAdaptedState(super.getStateForPlacement(context), context.getWorld(), context.getPos()); }

  @Override
  @SuppressWarnings("deprecation")
  public BlockState updatePostPlacement(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos pos, BlockPos facingPos)
  { return getAdaptedState(super.updatePostPlacement(state,facing,facingState,world,pos,facingPos), world, pos); }

}
