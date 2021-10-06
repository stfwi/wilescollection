/*
 * @file EdWindowBlock.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Mod windows.
 */
package wile.wilescollection.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import wile.wilescollection.libmc.blocks.StandardBlocks;

import javax.annotation.Nullable;


public class VariantVineBlock extends StandardBlocks.DirectedWaterLoggable implements StandardBlocks.IStandardBlock
{
  public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 7);

  public VariantVineBlock(long config, BlockBehaviour.Properties builder, final AABB unrotatedAABB)
  { super(config, builder, unrotatedAABB); }

  @Override
  public RenderTypeHint getRenderTypeHint()
  { return RenderTypeHint.TRANSLUCENT; }

  @Override
  public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos)
  { return true; }

  @Override
  @SuppressWarnings("deprecation")
  public boolean useShapeForLightOcclusion(BlockState state)
  { return true; }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
  { super.createBlockStateDefinition(builder); builder.add(VARIANT); }

  @Override
  @Nullable
  public BlockState getStateForPlacement(BlockPlaceContext context)
  {
    BlockState state = super.getStateForPlacement(context);
    return state;
  }

}
