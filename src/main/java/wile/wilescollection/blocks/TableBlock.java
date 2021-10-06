/*
 * @file Table.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Base block for tables.
 */
package wile.wilescollection.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import wile.wilescollection.libmc.blocks.StandardBlocks;

import javax.annotation.Nullable;


public class TableBlock extends StandardBlocks.HorizontalFourWayWaterLoggable implements StandardBlocks.IStandardBlock
{
  public TableBlock(long config, BlockBehaviour.Properties builder, final AABB base_aabb, final AABB[] side_aabb)
  { super(config, builder.randomTicks(), base_aabb, side_aabb, 0); }

  private BlockState getAdaptedState(BlockState state, LevelAccessor world, BlockPos pos)
  {
    final boolean n = world.getBlockState(pos.relative(Direction.NORTH)).getBlock()==this;
    final boolean e = world.getBlockState(pos.relative(Direction.EAST)).getBlock()==this;
    final boolean s = world.getBlockState(pos.relative(Direction.SOUTH)).getBlock()==this;
    final boolean w = world.getBlockState(pos.relative(Direction.WEST)).getBlock()==this;
    return (state == null) ? (null) : (state
      .setValue(StandardBlocks.HorizontalFourWayWaterLoggable.NORTH, !n && !e)
      .setValue(StandardBlocks.HorizontalFourWayWaterLoggable.EAST,  !e && !s)
      .setValue(StandardBlocks.HorizontalFourWayWaterLoggable.SOUTH, !s && !w)
      .setValue(StandardBlocks.HorizontalFourWayWaterLoggable.WEST,  !w && !n)
    );
  }

  @Override
  @Nullable
  public BlockState getStateForPlacement(BlockPlaceContext context)
  { return getAdaptedState(super.getStateForPlacement(context), context.getLevel(), context.getClickedPos()); }

  @Override
  @SuppressWarnings("deprecation")
  public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos pos, BlockPos facingPos)
  { return getAdaptedState(super.updateShape(state, facing, facingState, world, pos, facingPos), world, pos); }

}
