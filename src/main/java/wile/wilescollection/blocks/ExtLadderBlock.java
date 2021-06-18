/*
 * @file ExtLadderBlock.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Ladder block. The implementation is based on the vanilla
 * net.minecraft.block.BlockLadder. Minor changes to enable
 * later configuration (for block list based construction
 * time configuration), does not drop when the block behind
 * is broken, etc.
 */
package wile.wilescollection.blocks;

import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.IWorldReader;
import wile.wilescollection.ModConfig;
import wile.wilescollection.libmc.blocks.StandardBlocks;
import wile.wilescollection.libmc.detail.Auxiliaries;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.vector.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.*;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import javax.annotation.Nullable;
import java.util.List;


public class ExtLadderBlock extends LadderBlock implements StandardBlocks.IStandardBlock
{
  protected static final AxisAlignedBB STDLADDER_UNROTATED_AABB = Auxiliaries.getPixeledAABB(3, 0, 0, 13, 16, 3);
  protected static final VoxelShape STDLADDER_SOUTH_AABB =  VoxelShapes.create(Auxiliaries.getRotatedAABB(STDLADDER_UNROTATED_AABB, Direction.SOUTH, false));
  protected static final VoxelShape STDLADDER_EAST_AABB  = VoxelShapes.create(Auxiliaries.getRotatedAABB(STDLADDER_UNROTATED_AABB, Direction.EAST, false));
  protected static final VoxelShape STDLADDER_WEST_AABB  = VoxelShapes.create(Auxiliaries.getRotatedAABB(STDLADDER_UNROTATED_AABB, Direction.WEST, false));
  protected static final VoxelShape STDLADDER_NORTH_AABB = VoxelShapes.create(Auxiliaries.getRotatedAABB(STDLADDER_UNROTATED_AABB, Direction.NORTH, false));
  private static boolean without_speed_boost_ = false;

  public static void on_config(boolean without_speed_boost)
  {
    without_speed_boost_ = without_speed_boost;
    ModConfig.log("Config ladder: without-speed-boost:" + without_speed_boost_);
  }

  public ExtLadderBlock(long config, AbstractBlock.Properties builder)
  { super(builder); }

  @Override
  public RenderTypeHint getRenderTypeHint()
  { return RenderTypeHint.CUTOUT; }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void appendHoverText(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag flag)
  { Auxiliaries.Tooltip.addInformation(stack, world, tooltip, flag, true); }

  public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos)
  {
    switch ((Direction)state.getValue(FACING)) {
      case NORTH: return STDLADDER_NORTH_AABB;
      case SOUTH: return STDLADDER_SOUTH_AABB;
      case WEST: return STDLADDER_WEST_AABB;
      default: return STDLADDER_EAST_AABB;
    }
  }

  @Override
  public boolean isPossibleToRespawnInThis()
  { return false; }

  @Override
  public boolean canCreatureSpawn(BlockState state, IBlockReader world, BlockPos pos, EntitySpawnPlacementRegistry.PlacementType type, @Nullable EntityType<?> entityType)
  { return false; }

  @Override
  @SuppressWarnings("deprecation")
  public PushReaction getPistonPushReaction(BlockState state)
  { return PushReaction.DESTROY; }

  @Override
  public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity)
  { return true; }

  // Player update event, forwarded from the main mod instance.
  public static void onPlayerUpdateEvent(final PlayerEntity player)
  {
    if((without_speed_boost_) || (player.isOnGround()) || (!player.onClimbable()) || (player.isSteppingCarefully()) || (player.isSpectator())) return;
    double lvy = player.getLookAngle().y;
    if(Math.abs(lvy) < 0.92) return;
    final BlockPos pos = player.blockPosition();
    final BlockState state = player.level.getBlockState(pos);
    if(!(state.getBlock() instanceof ExtLadderBlock)) return;
    player.fallDistance = 0;
    if((player.getDeltaMovement().y() < 0) == (player.getLookAngle().y < 0)) {
      player.makeStuckInBlock(state, new Vector3d(0.2, (lvy>0)?(3):(6), 0.2));
      if(Math.abs(player.getDeltaMovement().y()) > 0.1) {
        Vector3d vdiff = Vector3d.atBottomCenterOf(pos).subtract(player.position()).scale(1);
        vdiff.add(Vector3d.atBottomCenterOf(state.getValue(FACING).getNormal()).scale(0.5));
        vdiff = new Vector3d(vdiff.x, player.getDeltaMovement().y, vdiff.z);
        player.setDeltaMovement(vdiff);
      }
    } else if(player.getLookAngle().y > 0) {
      player.makeStuckInBlock(state, new Vector3d(1, 0.05, 1));
    }
  }

}
