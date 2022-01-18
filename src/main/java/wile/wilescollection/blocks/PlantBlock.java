/*
 * @file PlantBlock.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Base block for special plants.
 */
package wile.wilescollection.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import wile.wilescollection.libmc.blocks.StandardBlocks;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;


public class PlantBlock extends FlowerBlock implements StandardBlocks.IStandardBlock
{
  public static final IntegerProperty MOOD = IntegerProperty.create("mood", 0, 3);

  public PlantBlock(long config, BlockBehaviour.Properties properties)
  {
    super(MobEffects.JUMP, 10, properties);
    registerDefaultState(super.defaultBlockState().setValue(MOOD,0));
  }

  @Override
  public long config()
  { return StandardBlocks.CFG_CUTOUT; }

  @Override
  public boolean hasDynamicDropList()
  { return true; }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
  { super.createBlockStateDefinition(builder); builder.add(MOOD); }

  @Override
  @Nullable
  public BlockState getStateForPlacement(BlockPlaceContext context)
  { return getAdaptedState(super.getStateForPlacement(context), context.getLevel(), context.getClickedPos()); }

  @Override
  @SuppressWarnings("deprecation")
  public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos pos, BlockPos facingPos)
  { return getAdaptedState(super.updateShape(state, facing, facingState, world, pos, facingPos), world, pos); }

  @Override
  @SuppressWarnings("deprecation")
  public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder)
  {
    final Float r = builder.getOptionalParameter(LootContextParams.EXPLOSION_RADIUS);
    return dropList(state, builder.getLevel(), null, (r!=null) && (r>0));
  }

  private BlockState getAdaptedState(BlockState state, LevelAccessor access, BlockPos pos)
  {
    if(!(access instanceof final Level world) || (world.isClientSide())) return state;
    int mood = 0;
    int light = world.getMaxLocalRawBrightness(pos);
    if(light < 5) {
      mood = 1;
    }
    return state.setValue(MOOD, mood);
  }

  @Override
  @SuppressWarnings("deprecation")
  public void tick(BlockState state, ServerLevel world, BlockPos pos, Random rnd)
  {
    final BlockState new_state = getAdaptedState(state, world, pos);
    if(new_state != state) world.setBlock(pos, new_state, 1|2);
  }

}
