/*
 * @file ProspectingDowserItem.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Block searching dowsing stick.
 */
package wile.wilescollection.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import wile.wilescollection.libmc.Auxiliaries;
import wile.wilescollection.libmc.Inventories;
import wile.wilescollection.libmc.Overlay;

import java.util.*;


public class BackfillWandItem extends ModItem
{
  private static Set<Item> blocks_spawnable_with_charges = new HashSet<>();
  private static int max_gap_filling_depth = 5;
  private static int max_charges_ = 4096;

  public static void on_config(int max_charges)
  {
    max_charges_ = Mth.clamp(max_charges, 1, 65536);
    blocks_spawnable_with_charges.clear();
    blocks_spawnable_with_charges.add(Items.DIRT);
    blocks_spawnable_with_charges.add(Items.STONE);
    blocks_spawnable_with_charges.add(Items.COBBLESTONE);
    blocks_spawnable_with_charges.add(Items.GRASS_BLOCK);
    blocks_spawnable_with_charges.add(Items.ANDESITE);
    blocks_spawnable_with_charges.add(Items.DEEPSLATE);
    blocks_spawnable_with_charges.add(Items.GRAVEL);
    blocks_spawnable_with_charges.add(Items.COBBLED_DEEPSLATE);
  }

  public BackfillWandItem(Item.Properties properties)
  { super(properties.stacksTo(1)); }

  public boolean isFoil(ItemStack stack)
  { return false; }

  @Override
  public boolean isEnchantable(ItemStack stack)
  { return false; }

  @Override
  public boolean isBookEnchantable(ItemStack stack, ItemStack book)
  { return false; }

  @Override
  public boolean isRepairable(ItemStack stack)
  { return false; }

  @Override
  public boolean isBarVisible(ItemStack stack)
  { return true; }

  @Override
  public int getBarWidth(ItemStack stack)
  { return (int)Math.round(13f * Math.max(0, max_charges_-getDamage(stack))/max_charges_); }

  @Override
  public int getBarColor(ItemStack stack)
  { return 0x2d9ff7; }

  @Override
  public boolean canBeDepleted()
  { return true; }

  @Override
  @SuppressWarnings("deprecation")
  public int getMaxDamage(ItemStack stack)
  { return max_charges_; }

  @Override
  public int getDamage(ItemStack stack)
  { return (!stack.hasTag()) ? (0) : (stack.getTag().getInt("uses")); }

  @Override
  public void setDamage(ItemStack stack, int damage)
  { stack.getOrCreateTag().putInt("uses", Math.max(0, damage)); }

  @Override
  public UseAnim getUseAnimation(ItemStack stack)
  { return UseAnim.BOW; }

  @Override
  public int getUseDuration(ItemStack stack)
  { return 120; }

  @Override
  public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
  {
    final ItemStack stack = player.getItemInHand(hand);
    if(player instanceof ServerPlayer splayer) {
      if(hand != InteractionHand.MAIN_HAND) {
        Overlay.show(player, Auxiliaries.localizable("item."+Auxiliaries.modid()+".wand_of_backfilling.fail.notmainhand"));
        return InteractionResultHolder.fail(stack);
      }
      ItemStack placememt_stack = getSelectedPlacementItem(splayer);
      if(placememt_stack.isEmpty()) {
        Overlay.show(player, Auxiliaries.localizable("item."+Auxiliaries.modid()+".wand_of_backfilling.fail.noblocks"));
        return InteractionResultHolder.fail(stack);
      }
      if(getSelectedPlacementBlock(splayer).isEmpty()) {
        Overlay.show(player, Auxiliaries.localizable("item."+Auxiliaries.modid()+".wand_of_backfilling.fail.cantplace", placememt_stack.getItem().getName(placememt_stack)));
        return InteractionResultHolder.fail(stack);
      }
    }
    player.startUsingItem(hand);
    return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
  }

  @Override
  public void onUsingTick(ItemStack wand, LivingEntity entity, int count)
  {
    if(!wand.is(this)) return;
    if(!(entity instanceof ServerPlayer player)) return;
    final ServerLevel world = player.getLevel();
    final BlockHitResult bh = Item.getPlayerPOVHitResult(world, player, ClipContext.Fluid.NONE);
    final Direction facing = bh.getDirection();
    BlockPos pos = bh.getBlockPos().relative(facing);
    if(!world.getBlockState(pos).is(Blocks.WATER) && (facing==Direction.UP)) pos = pos.relative(facing.getOpposite());
    final ItemStack stack = getSelectedPlacementBlock(player);
    if(backfill(world, pos, player, stack, !world.getBlockState(pos).getFluidState().isEmpty(), player.isSteppingCarefully())) {
      consumeStack(stack, player);
    }
  }

  // ------------------------------------------------------------------------------------------------

  private ItemStack getSelectedPlacementItem(ServerPlayer player)
  {
    final int sel_index = player.getInventory().selected+1;
    if(!Inventory.isHotbarSlot(sel_index)) return ItemStack.EMPTY;
    return player.getInventory().getItem(sel_index);
  }

  private ItemStack getSelectedPlacementBlock(ServerPlayer player)
  {
    final ItemStack ref_stack = getSelectedPlacementItem(player);
    if(ref_stack.isEmpty()) return ItemStack.EMPTY;
    if(!(ref_stack.getItem() instanceof BlockItem bi)) return ItemStack.EMPTY;
    if(bi.getBlock() instanceof EntityBlock) return ItemStack.EMPTY;
    final BlockState state = bi.getBlock().defaultBlockState();
    if(state.is(BlockTags.UNDERWATER_BONEMEALS) || state.is(BlockTags.SAPLINGS) || state.is(BlockTags.FLOWERS)) return ItemStack.EMPTY;
    return ref_stack;
  }

  private boolean isReplacable(ServerLevel world, BlockPos pos, boolean allow_fluid)
  {
    if(world.getBlockEntity(pos)!=null) return false;
    final BlockState state = world.getBlockState(pos);
    if(state.isAir()) return true;
    if(state.is(BlockTags.REPLACEABLE_PLANTS)) return true;
    if(allow_fluid && state.is(Blocks.WATER)) return true;
    return false;
  }

  private boolean isFullSolidBlock(ServerLevel world, BlockPos pos)
  { return world.getBlockState(pos).isCollisionShapeFullBlock(world, pos); }

  private boolean isTopSolidBlock(ServerLevel world, BlockPos pos)
  { return world.getBlockState(pos).isFaceSturdy(world, pos, Direction.UP); }

  private void consumeStack(ItemStack stack, ServerPlayer player)
  {
    if(stack.getCount() > 1) {
      stack.shrink(1);
      return;
    }
    // Alternative stack in inventory.
    {
      final int sel_index = player.getInventory().selected+1;
      final Optional<ItemStack> available = Inventories.InventoryRange.fromPlayerInventory(player).find((i,s)->{
        if((i==sel_index) || (!Inventories.areItemStacksIdentical(s, stack))) return Optional.empty();
        return Optional.of(s);
      });
      if(available.isPresent()) {
        available.get().shrink(1);
        return;
      }
    }
    // Check if charges can be used
    {
      final ItemStack wand = player.getMainHandItem();
      if(blocks_spawnable_with_charges.contains(stack.getItem())) {
        final int dmg = getDamage(wand);
        if(dmg < max_charges_) {
          setDamage(wand, dmg+1);
          return;
        } else if(player.isCreative()) {
          setDamage(wand, 0);
          return;
        }
      }
    }
    // Use last block.
    {
      stack.shrink(1);
    }
  }

  private boolean placeBlock(ServerLevel world, BlockPos pos, ItemStack stack)
  {
    if((stack.isEmpty()) || (!(stack.getItem() instanceof BlockItem block_item))) return false;
    if(!isReplacable(world, pos, true)) return false;
    if(!world.getEntities((Entity)null, new AABB(pos), e->(!(e instanceof ItemEntity))).isEmpty()) return false;
    final BlockState state = block_item.getBlock().defaultBlockState();
    if(!world.setBlock(pos, state, 2|8)) return false;
    world.playSound(null, pos, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 0.7f, 1.1f);
    return true;
  }

  private static Map<Vec3i, Integer> gap_weights = Map.of(
    new Vec3i( 1,0, 0), 3, new Vec3i( 0,0, 1), 3, new Vec3i(-1,0, 0), 3, new Vec3i( 0,0,-1), 3,
    new Vec3i( 1,0, 1), 2, new Vec3i( 1,0,-1), 2, new Vec3i(-1,0, 1), 2, new Vec3i(-1,0,-1), 2
  );

  private int getGapWeight(ServerLevel world, BlockPos pos, boolean allow_fluid)
  {
    if(!isReplacable(world, pos, allow_fluid)) return 0;
    if(!isTopSolidBlock(world, pos.below())) return 0;
    return gap_weights.entrySet().stream().mapToInt(e->isFullSolidBlock(world, pos.offset(e.getKey())) ? e.getValue() : 0).sum();
  }

  private List<BlockPos> getHorizontalPositions(BlockPos pos, int radius)
  {
    if(radius <= 0) return List.of(pos);
    ArrayList<BlockPos> rng_positions = new ArrayList<>();
    BlockPos.MutableBlockPos.betweenClosed(pos.offset(-radius,0,-radius), pos.offset(radius,0,radius)).forEach(p->rng_positions.add(new BlockPos(p)));
    Collections.shuffle(rng_positions);
    return rng_positions;
  }

  private boolean isBehindPlayer(ServerLevel world, BlockPos pos, ServerPlayer player)
  {
    final Vec3 placement_angle = Vec3.atCenterOf(pos).subtract(player.position()).with(Direction.Axis.Y, 0).normalize();
    final Vec3 look_angle = player.getLookAngle().with(Direction.Axis.Y, 0);
    final double da = Math.acos(look_angle.dot(placement_angle)) * 180/Math.PI;
    return da >= 110;
  }

  private boolean backfill(ServerLevel world, BlockPos from_pos, ServerPlayer player, ItemStack stack, boolean allow_fluid, boolean only_below)
  {
    final int min_weight = 5;
    final int min_depth = only_below ? 1 : 0;
    if((getGapWeight(world, from_pos, allow_fluid) >= min_weight) && placeBlock(world, from_pos, stack)) return true;
    for(int depth=max_gap_filling_depth; depth>=min_depth; --depth) {
      final BlockPos ref_pos = from_pos.below(depth);
      for(BlockPos r_pos: getHorizontalPositions(ref_pos,1)) {
        for(BlockPos fill_pos: getHorizontalPositions(r_pos,1)) {
          double ds = ref_pos.distToCenterSqr(fill_pos.getX(), ref_pos.getY(), fill_pos.getZ());
          if(ds > 3) continue;
          final int weight = getGapWeight(world, fill_pos, allow_fluid);
          if(weight <= 0 || weight <= min_weight) continue;
          if(isBehindPlayer(world, fill_pos, player)) continue;
          if(placeBlock(world, fill_pos, stack)) return true;
        }
      }
    }
    return false;
  }

}
