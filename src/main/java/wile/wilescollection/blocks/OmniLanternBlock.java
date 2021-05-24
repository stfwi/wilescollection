/*
 * @file OmniLanternBlock.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Omnidirectional Lantern.
 */
package wile.wilescollection.blocks;

import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.util.math.*;
import net.minecraft.util.*;
import wile.wilescollection.libmc.blocks.StandardBlocks;
import wile.wilescollection.libmc.detail.Auxiliaries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;


public class OmniLanternBlock extends StandardBlocks.DirectedWaterLoggable implements StandardBlocks.IStandardBlock
{
  public OmniLanternBlock(long config, Block.Properties builder, final AxisAlignedBB[] wallAABBs, final AxisAlignedBB[] standingAABBs, final AxisAlignedBB[] handingAABBs)
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
        VoxelShapes.fullCube(),
        VoxelShapes.fullCube()
      ));
    });
  }

  @Override
  @Nullable
  public BlockState getStateForPlacement(BlockItemUseContext context)
  {
    final BlockState state = super.getStateForPlacement(context);
    if(state==null) return state;
    final Direction facing = context.getFace().getOpposite();
    final World world = context.getWorld();
    final BlockPos pos = context.getPos();
    return (!world.getBlockState(pos.offset(facing)).isSolidSide(world, pos.offset(facing), facing.getOpposite())) ? null : state;
  }

  @Override
  @SuppressWarnings("deprecation")
  public BlockState updatePostPlacement(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos pos, BlockPos facingPos)
  {
    if(!world.getBlockState(pos.offset(facing)).isSolidSide(world, pos.offset(facing), facing.getOpposite())) {
      return state.getFluidState().getBlockState();
    }
    return super.updatePostPlacement(state, facing, facingState, world, pos, facingPos);
  }

}
