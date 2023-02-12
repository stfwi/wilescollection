/*
 * @file SimpleBlocks.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Mod windows.
 */
package wile.wilescollection.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import wile.wilescollection.libmc.Auxiliaries;
import wile.wilescollection.libmc.Registries;
import wile.wilescollection.libmc.blocks.StandardBlocks;
import wile.wilescollection.libmc.blocks.StandardEntityBlocks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;


public class SimpleBlocks
{
  /**
   * Table.
   */
  public static class TableBlock extends StandardBlocks.HorizontalFourWayWaterLoggable implements StandardBlocks.IStandardBlock
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

  /**
   * Simple window.
   */
  public static class WindowBlock extends StandardBlocks.DirectedWaterLoggable
  {
    public WindowBlock(long config, BlockBehaviour.Properties builder, final AABB unrotatedAABB)
    { super(config, builder, unrotatedAABB); }

    @Override
    public RenderTypeHint getRenderTypeHint()
    { return RenderTypeHint.TRANSLUCENT; }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
      Direction facing = context.getHorizontalDirection();
      if(Math.abs(context.getPlayer().getLookAngle().y) > 0.9) {
        facing = context.getNearestLookingDirection();
      } else {
        for(Direction f: Direction.values()) {
          BlockState st = context.getLevel().getBlockState(context.getClickedPos().relative(f));
          if(st.getBlock() == this) {
            facing = st.getValue(FACING);
            break;
          }
        }
      }
      return super.getStateForPlacement(context).setValue(FACING, facing);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
      if(player.getItemInHand(hand).getItem() != asItem()) return InteractionResult.PASS;
      final Direction facing = state.getValue(FACING);
      if(facing.getAxis() != hit.getDirection().getAxis()) return InteractionResult.PASS;
      Arrays.stream(Direction.orderedByNearest(player))
              .filter(d->d.getAxis() != facing.getAxis())
              .filter(d->world.getBlockState(pos.relative(d)).canBeReplaced((new DirectionalPlaceContext(world, pos.relative(d), facing.getOpposite(), player.getItemInHand(hand), facing))))
              .findFirst().ifPresent((d)->{
                        BlockState st = defaultBlockState()
                                .setValue(FACING, facing)
                                .setValue(WATERLOGGED,world.getBlockState(pos.relative(d)).getFluidState().getType()== Fluids.WATER);
                        world.setBlock(pos.relative(d), st, 1|2);
                        world.playSound(player, pos, SoundEvents.METAL_PLACE, SoundSource.BLOCKS, 1f, 1f);
                        player.getItemInHand(hand).shrink(1);
                      }
              );
      return InteractionResult.sidedSuccess(world.isClientSide());
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos)
    { return true; }

    @Override
    @SuppressWarnings("deprecation")
    public boolean useShapeForLightOcclusion(BlockState state)
    { return true; }

  }

  /**
   * Simple Lantern.
   */
  public static class OmniLanternBlock extends StandardBlocks.DirectedWaterLoggable implements StandardBlocks.IStandardBlock
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

  /**
   * Simple chimney.
   */
  public static class ChimneyBlock extends StandardBlocks.WaterLoggable implements StandardEntityBlocks.IStandardEntityBlock<ChimneyBlock.ChimneyBlockEntity>
  {
    public ChimneyBlock(long conf, BlockBehaviour.Properties properties, AABB[] aabbs)
    { super(conf, properties, aabbs); }

    @Override
    public boolean isBlockEntityClientTicking(Level world, BlockState state)
    { return true; }

    public static class ChimneyBlockEntity extends StandardEntityBlocks.StandardBlockEntity
    {
      public ChimneyBlockEntity(BlockPos pos, BlockState state)
      { super(Registries.getBlockEntityTypeOfBlock(state.getBlock()), pos, state); }

      @Override
      public void tick()
      {
        final Level world = getLevel();
        final RandomSource rnd = world.getRandom();
        final int num_particles = rnd.nextInt(4)-1;
        if((num_particles <= 0) || (!world.isClientSide()) || ((world.getGameTime() & 0x7) != 0)) return;
        if(!(world.getBlockState(getBlockPos()).getBlock() instanceof ChimneyBlock)) return;
        final BlockPos pos = getBlockPos();
        for(int i=num_particles; i>=0; --i) {
          world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true, pos.getX()+0.5+rnd.nextDouble()/3.0 * (rnd.nextBoolean()?1:-1), pos.getY()+0.9+rnd.nextDouble(), pos.getZ()+0.5+rnd.nextDouble()/3.0 * (rnd.nextBoolean() ? 1 : -1), 0.0, 0.07, 0.0);
        }
      }
    }
  }

  /**
   * Variant Vines
   */
  public static class VariantVineBlock extends StandardBlocks.DirectedWaterLoggable implements StandardBlocks.IStandardBlock
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

}
