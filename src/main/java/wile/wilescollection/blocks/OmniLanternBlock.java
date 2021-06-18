/*
 * @file OmniLanternBlock.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Omnidirectional Lantern.
 */
package wile.wilescollection.blocks;

import net.minecraft.block.material.PushReaction;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.*;
import net.minecraft.util.*;
import wile.wilescollection.libmc.blocks.StandardBlocks;
import wile.wilescollection.libmc.detail.Auxiliaries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;


public class OmniLanternBlock extends StandardBlocks.DirectedWaterLoggable implements StandardBlocks.IStandardBlock
{
  public OmniLanternBlock(long config, AbstractBlock.Properties builder, final AxisAlignedBB[] wallAABBs, final AxisAlignedBB[] standingAABBs, final AxisAlignedBB[] handingAABBs)
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
        VoxelShapes.block(),
        VoxelShapes.block()
      ));
    });
  }

  @Override
  @Nullable
  @SuppressWarnings("deprecation")
  public BlockState getStateForPlacement(BlockItemUseContext context)
  {
    final BlockState state = super.getStateForPlacement(context);
    if(state==null) return state;
    final Direction facing = context.getClickedFace().getOpposite();
    final World world = context.getLevel();
    final BlockPos pos = context.getClickedPos();
    return (world.getBlockState(pos.relative(facing)).isAir()) ? null : state;
  }

  @Override
  public PushReaction getPistonPushReaction(BlockState state)
  { return PushReaction.DESTROY; }

  @Override
  @SuppressWarnings("deprecation")
  public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos)
  {
    final Direction facing = state.getValue(FACING);
    return Block.canSupportCenter(world, pos.relative(facing), facing.getOpposite());
  }

  @Override
  @SuppressWarnings("deprecation")
  public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos pos, BlockPos facingPos)
  {
    state = super.updateShape(state, facing, facingState, world, pos, facingPos);
    return (canSurvive(state, world, pos) && state.is(this)) ? (state) : (state.getFluidState().createLegacyBlock());
  }

}
