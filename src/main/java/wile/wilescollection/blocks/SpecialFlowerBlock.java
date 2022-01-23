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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.TickPriority;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import wile.wilescollection.libmc.blocks.StandardBlocks;
import wile.wilescollection.libmc.detail.Effects;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;


public class SpecialFlowerBlock extends FlowerBlock implements StandardBlocks.IStandardBlock
{
  // Mood values may change, therefore a generic int is used.
  public static final IntegerProperty MOOD = IntegerProperty.create("mood", 0, 3);
  public static final int MOOD_NORMAL = 0;
  public static final int MOOD_FRIGHTENED = 1;
  public static final int MOOD_HAPPY = 2;
  public static final int MOOD_ANGRY = 3;

  public SpecialFlowerBlock(long config, BlockBehaviour.Properties properties)
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
    int mood = MOOD_NORMAL;
    if(world.getMaxLocalRawBrightness(pos) < 5) mood = MOOD_FRIGHTENED;
    return state.setValue(MOOD, mood);
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void animateTick(BlockState state, Level world, BlockPos pos, Random rand)
  {
    switch(state.getValue(MOOD)) {
      case MOOD_NORMAL     -> {
        if(rand.nextInt(10) != 0) return;
        final Vec3 p = Vec3.atCenterOf(pos).add(state.getOffset(world, pos)).add(0,0.2,0);
        world.addParticle(ParticleTypes.MYCELIUM, p.x(), p.y(), p.z(), rand.nextGaussian() * 5e-3, rand.nextGaussian() * 5e-3, rand.nextGaussian() * 5e-3);
      }
      case MOOD_FRIGHTENED -> {
        if(rand.nextInt(10) != 0) return;
        final Vec3 p = Vec3.atCenterOf(pos).add(state.getOffset(world, pos)).subtract(0,0.1,0);
        world.addParticle(ParticleTypes.BUBBLE_POP, p.x(), p.y(), p.z(), rand.nextGaussian() * 5e-3, rand.nextGaussian() * 5e-3, rand.nextGaussian() * 5e-3);
      }
      case MOOD_HAPPY      -> {
        final Vec3 p = Vec3.atCenterOf(pos).add(state.getOffset(world, pos)).add(0,0.2,0);
        world.addParticle(ParticleTypes.WHITE_ASH, p.x(), p.y(), p.z(), rand.nextGaussian() * 5e-3, rand.nextGaussian() * 5e-3, rand.nextGaussian() * 5e-3);
      }
      case MOOD_ANGRY      -> {
        if(rand.nextInt(4) != 0) return;
        final Vec3 p = Vec3.atCenterOf(pos).add(state.getOffset(world, pos)).add(0,0.2,0);
        world.addParticle(ParticleTypes.ANGRY_VILLAGER, p.x(), p.y(), p.z(), rand.nextGaussian() * 5e-3, rand.nextGaussian() * 5e-3, rand.nextGaussian() * 5e-3);
      }
    }
  }

  @Override
  @SuppressWarnings("deprecation")
  public void tick(BlockState state, ServerLevel world, BlockPos pos, Random rnd)
  {
    if(!world.getEntitiesOfClass(LivingEntity.class, AABB.unitCubeFromLowerCorner(Vec3.atLowerCornerOf(pos))).isEmpty()) return;
    final BlockState new_state = getAdaptedState(state, world, pos);
    if(new_state != state) world.setBlock(pos, new_state, 1|2);
  }

  @Override
  public float getJumpFactor()
  { return 1.75f; }

  @Override
  public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos)
  { return (state.getValue(MOOD) == MOOD_ANGRY) ? (15) : (4); }

  @Override
  @SuppressWarnings("deprecation")
  public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity)
  {
    if(entity instanceof Player player) {
      if(!player.isFallFlying()) {
        world.setBlock(pos, state.setValue(MOOD, MOOD_HAPPY), 1|2);
        if(player.isHurt() && ((world.getGameTime() & 0xf)==0)) player.heal(0.25f);
        Effects.assign(player, MobEffects.MOVEMENT_SPEED, 200);
        Effects.assign(player, MobEffects.SLOW_FALLING, 60);
      } else if(state.getValue(MOOD) != MOOD_HAPPY) {
        world.setBlock(pos, state.setValue(MOOD, MOOD_FRIGHTENED), 1|2);
      }
    } else if(entity instanceof Monster monster) {
      world.setBlock(pos, state.setValue(MOOD, MOOD_ANGRY), 1|2);
      if((world.random.nextInt() & 0xf) < 4) {
        monster.setRemainingFireTicks(80);
        Effects.assign(monster, MobEffects.MOVEMENT_SLOWDOWN, 200);
        Effects.assign(monster, MobEffects.CONFUSION, 200);
      }
    }
    if(!world.getBlockTicks().hasScheduledTick(pos, this)) {
      world.scheduleTick(pos, this, 100, TickPriority.LOW);
    }
  }

}
