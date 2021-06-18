/*
 * @file EdWindowBlock.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Mod windows.
 */
package wile.wilescollection.blocks;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import wile.wilescollection.libmc.blocks.StandardBlocks;

import javax.annotation.Nullable;


public class VariantVineBlock extends StandardBlocks.DirectedWaterLoggable implements StandardBlocks.IStandardBlock
{
  public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 7);

  public VariantVineBlock(long config, AbstractBlock.Properties builder, final AxisAlignedBB unrotatedAABB)
  { super(config, builder, unrotatedAABB); }

  @Override
  public RenderTypeHint getRenderTypeHint()
  { return RenderTypeHint.TRANSLUCENT; }

  @Override
  public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos)
  { return true; }

  @Override
  @SuppressWarnings("deprecation")
  public boolean useShapeForLightOcclusion(BlockState state)
  { return true; }

  @Override
  protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
  { super.createBlockStateDefinition(builder); builder.add(VARIANT); }

  @Override
  @Nullable
  public BlockState getStateForPlacement(BlockItemUseContext context)
  {
    BlockState state = super.getStateForPlacement(context);
    if(state == null) return state;


    return state;
  }

}
