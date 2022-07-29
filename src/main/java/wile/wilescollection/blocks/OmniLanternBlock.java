/*
 * @file OmniLanternBlock.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Omnidirectional Lantern.
 */
package wile.wilescollection.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import wile.wilescollection.libmc.blocks.StandardBlocks;
import wile.wilescollection.libmc.Auxiliaries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;


public class OmniLanternBlock extends StandardBlocks.DirectedWaterLoggable implements StandardBlocks.IStandardBlock
{
  public OmniLanternBlock(long config, BlockBehaviour.Properties builder, final AABB[] wallAABBs, final AABB[] standingAABBs, final AABB[] handingAABBs)
  {
    super(config, builder, ()->{
      final boolean is_horizontal = false;
      return new ArrayList<VoxelShape>(Arrays.asList(
        Auxiliaries.getUnionShape(standingAABBs),
        Auxiliaries.getUnionShape(handingAABBs),
        Auxiliaries.getUnionShape(Auxiliaries.getRotatedAABB(wallAABBs, Direction.NORTH, is_horizontal)),
        Auxiliaries.getUnionShape(Auxiliaries.getRotatedAABB(wallAABBs, Direction.SOUTH, is_horizontal)),
        Auxiliaries.getUnionShape(Auxiliaries.getRotatedAABB(wallAABBs, Direction.WEST, is_horizontal)),
        Auxiliaries.getUnionShape(Auxiliaries.getRotatedAABB(wallAABBs, Direction.EAST, is_horizontal)),
        Shapes.block(),
        Shapes.block()
      ));
    });
  }

  @Override
  @Nullable
  @SuppressWarnings("deprecation")
  public BlockState getStateForPlacement(BlockPlaceContext context)
  {
    final BlockState state = super.getStateForPlacement(context);
    if(state==null) return state;
    final Direction facing = context.getClickedFace().getOpposite();
    final Level world = context.getLevel();
    final BlockPos pos = context.getClickedPos();
    return (world.getBlockState(pos.relative(facing)).isAir()) ? null : state;
  }

  @Override
  public PushReaction getPistonPushReaction(BlockState state)
  { return PushReaction.DESTROY; }

  @Override
  @SuppressWarnings("deprecation")
  public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
  {
    final Direction facing = state.getValue(FACING);
    return Block.canSupportCenter(world, pos.relative(facing), facing.getOpposite());
  }

  @Override
  @SuppressWarnings("deprecation")
  public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos pos, BlockPos facingPos)
  {
    state = super.updateShape(state, facing, facingState, world, pos, facingPos);
    return (canSurvive(state, world, pos) && state.is(this)) ? (state) : (state.getFluidState().createLegacyBlock());
  }

}
