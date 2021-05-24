/*
 * @file StraightPoleBlock.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Smaller (cutout) block with a defined facing.
 */
package wile.wilescollection.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DirectionalPlaceContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import wile.wilescollection.libmc.blocks.StandardBlocks;
import wile.wilescollection.libmc.detail.Inventories;

import javax.annotation.Nullable;
import java.util.Arrays;


public class StraightPoleBlock extends StandardBlocks.DirectedWaterLoggable implements StandardBlocks.IStandardBlock
{
  private final StraightPoleBlock default_pole;

  public StraightPoleBlock(long config, Block.Properties builder, final AxisAlignedBB unrotatedAABB, @Nullable StraightPoleBlock defaultPole)
  { super(config, builder, unrotatedAABB); default_pole=(defaultPole==null) ? (this) : (defaultPole); }

  @Override
  @Nullable
  public BlockState getStateForPlacement(BlockItemUseContext context)
  {
    Direction facing = context.getFace();
    BlockState state = super.getStateForPlacement(context).with(FACING, facing);
    if((config & StandardBlocks.CFG_FLIP_PLACEMENT_IF_SAME) != 0) {
      World world = context.getWorld();
      BlockPos pos = context.getPos();
      if(world.getBlockState(pos.offset(facing.getOpposite())).getBlock() instanceof StraightPoleBlock) {
        state = state.with(FACING, state.get(FACING).getOpposite());
      }
    }
    return state;
  }

  @Override
  @SuppressWarnings("deprecation")
  public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit)
  {
    if((hit.getFace().getAxis() == state.get(FACING).getAxis())) return ActionResultType.PASS;
    final ItemStack held_stack = player.getHeldItem(hand);
    if((held_stack.isEmpty()) || (!(held_stack.getItem() instanceof BlockItem))) return ActionResultType.PASS;
    if(!(((BlockItem)(held_stack.getItem())).getBlock() instanceof StraightPoleBlock)) return ActionResultType.PASS;
    if(held_stack.getItem() != default_pole.asItem()) return ActionResultType.func_233537_a_(world.isRemote());
    final Block held_block = ((BlockItem)(held_stack.getItem())).getBlock();
    final Direction block_direction = state.get(FACING);
    final Vector3d block_vec = Vector3d.copy(state.get(FACING).getDirectionVec());
    final double colinearity = 1.0-block_vec.crossProduct(player.getLookVec()).length();
    final Direction placement_direction = Arrays.stream(Direction.getFacingDirections(player)).filter(d->d.getAxis()==block_direction.getAxis()).findFirst().orElse(Direction.NORTH);
    final BlockPos adjacent_pos = pos.offset(placement_direction);
    final BlockState adjacent = world.getBlockState(adjacent_pos);
    final BlockItemUseContext ctx = new DirectionalPlaceContext(world, adjacent_pos, placement_direction, player.getHeldItem(hand), placement_direction.getOpposite());
    if(!adjacent.isReplaceable(ctx)) return ActionResultType.func_233537_a_(world.isRemote());
    final BlockState new_state = held_block.getStateForPlacement(ctx);
    if(new_state == null) return ActionResultType.FAIL;
    if(!world.setBlockState(adjacent_pos, new_state, 1|2)) return ActionResultType.FAIL;
    world.playSound(player, pos, SoundEvents.BLOCK_METAL_PLACE, SoundCategory.BLOCKS, 1f, 1f);
    if(!player.isCreative()) {
      held_stack.shrink(1);
      Inventories.setItemInPlayerHand(player, hand, held_stack);
    }
    return ActionResultType.func_233537_a_(world.isRemote());
  }

}
