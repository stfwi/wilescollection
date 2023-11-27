/*
 * @file RediaToolItem.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * REDia multi tool.
 */
package wile.wilescollection.items;

import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import wile.wilescollection.libmc.Auxiliaries;
import wile.wilescollection.libmc.ExtendedShapelessRecipe;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import wile.wilescollection.libmc.TreeCutting;

import javax.annotation.Nullable;
import java.util.*;


public class RediaToolItem extends AxeItem implements ExtendedShapelessRecipe.IRepairableToolItem
{
  private static int enchantability = 10;
  private static int max_item_damage = 3000;
  private static int initial_item_damage_percent = 100;
  private static final boolean with_torch_placing = true;
  private static boolean with_hoeing = true;
  private static boolean with_tree_felling = true;
  private static final boolean with_shearing = true;
  private static boolean with_safe_attacking = true;
  private static final int max_tree_felling_blocks_to_break = 128;

  public static void on_config(boolean without_redia_torchplacing, boolean without_redia_hoeing,
                               boolean without_redia_tree_chopping, boolean without_safe_attacking, int durability,
                               int redia_tool_initial_durability_percent, int tool_enchantability)
  {
    boolean with_torch_placing = !without_redia_torchplacing;
    with_hoeing = !without_redia_hoeing;
    with_tree_felling = !without_redia_tree_chopping;
    max_item_damage = Mth.clamp(durability, 750, 4000);
    initial_item_damage_percent = Mth.clamp(redia_tool_initial_durability_percent, 1, 100);
    with_safe_attacking = !without_safe_attacking;
    enchantability = Mth.clamp(tool_enchantability, 5, 25);
    Auxiliaries.logInfo("REDIA tool config: "
      + (with_torch_placing?"":"no-") + "torch-placing, "
      + (with_hoeing?"":"no-") + "hoeing, "
      + (with_tree_felling?"":"no-") + "tree-felling, "
      + (with_safe_attacking?"":"no-") + "safe-attack,"
      + (" durability:"+max_item_damage + ", initial-durability:"+redia_tool_initial_durability_percent)
    );
  }

  // -------------------------------------------------------------------------------------------------------------------

  public RediaToolItem(Item.Properties properties)
  { super(Tiers.DIAMOND, 5, -3, properties.stacksTo(1).rarity(Rarity.UNCOMMON).defaultDurability(max_item_damage)); }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag)
  { Auxiliaries.Tooltip.addInformation(stack, world, tooltip, flag, true); }

  @Override
  @OnlyIn(Dist.CLIENT)
  public boolean isFoil(ItemStack stack)
  { return false; } // don't show enchantment glint, looks awful. Also nice to cause some confusion ;-)

  // -------------------------------------------------------------------------------------------------------------------

  @Override
  public int getEnchantmentValue()
  { return enchantability; }

  @Override
  public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair)
  { return super.isValidRepairItem(toRepair, repair); }

  @Override
  public boolean canBeDepleted()
  { return true; }

  @Override
  public int getMaxDamage(ItemStack stack)
  { return max_item_damage; }

  @Override
  @SuppressWarnings("deprecation")
  public boolean isCorrectToolForDrops(BlockState state)
  { return true; }

  public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) // forge
  { return true; }

  @Override
  public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker)
  { return true; }

  @Override
  public boolean isBookEnchantable(ItemStack stack, ItemStack book)
  { return true; }

  @Override
  public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment)
  {
    if(enchantment == Enchantments.BLOCK_FORTUNE) return true;
    if(enchantment == Enchantments.BLOCK_EFFICIENCY) return true;
    if(enchantment == Enchantments.KNOCKBACK) return true;
    if(enchantment == Enchantments.MOB_LOOTING) return true;
    if(enchantment == Enchantments.SHARPNESS) return true;
    if(enchantment == Enchantments.FIRE_ASPECT) return true;
    return enchantment.category.canEnchant(stack.getItem());
  }

  // -------------------------------------------------------------------------------------------------------------------

  @Override
  public void onCraftedBy(ItemStack stack, Level world, Player player)
  {
    if(stack.getDamageValue()!=0) return;
    if(stack.hasTag() && stack.getTag().getAllKeys().stream().anyMatch(e->!e.equals("Damage"))) return;
    stack.setDamageValue(absoluteDmg(initial_item_damage_percent));
  }

  @Override
  public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker)
  { return super.hurtEnemy(stack, target, attacker); } // already 2 item dmg, ok

  @Override
  public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity)
  {
    if(!with_safe_attacking) return false;
    if(entity instanceof Villager) return true; // Cancel attacks for villagers.
    if((entity instanceof TamableAnimal animal) && animal.isTame() && animal.isOwnedBy(player)) return true; // Don't hit own pets
    if(((entity instanceof ZombifiedPiglin piglin)) && (piglin.getTarget() == null)) return true; // noone wants to accidentally step them on the foot.
    if(player.level().isClientSide) return false; // only server side evaluation
    return false; // allow attacking
  }

  @Override
  public InteractionResult useOn(UseOnContext context)
  {
    final Direction facing = context.getClickedFace();
    final InteractionHand hand = context.getHand();
    final Player player = context.getPlayer();
    final Level world = context.getLevel();
    final BlockPos pos = context.getClickedPos();
    final Vec3 hitvec = context.getClickLocation();
    InteractionResult rv;
    if(context.getPlayer().isShiftKeyDown()) {
      rv = tryPlantSnipping(player, world, pos, hand, facing, hitvec);
      if(rv != InteractionResult.PASS) return rv;
      if(facing == Direction.UP) {
        rv = tryDigOver(player, world, pos, hand, facing, hitvec);
        if(rv != InteractionResult.PASS) return rv;
      } else if(facing.getAxis().isHorizontal()) {
        rv = tryTorchPlacing(context);
        if(rv != InteractionResult.PASS) return rv;
      } else {
        rv = super.useOn(context); // axe log stripping
      }
    } else {
      rv = tryTorchPlacing(context);
      if(rv != InteractionResult.PASS) return rv;
    }
    return rv;
  }

  @Override
  public boolean mineBlock(ItemStack tool, Level world, BlockState state, BlockPos pos, LivingEntity entity)
  {
    if(world.isClientSide || !(entity instanceof Player player)) return true;
    if((state.getDestroySpeed(world, pos) > 0.5f) || (world.getRandom().nextDouble() > 0.67)) tool.hurtAndBreak(1, player, (p)->p.broadcastBreakEvent(player.getUsedItemHand()));
    if(with_tree_felling && player.isShiftKeyDown()) tryTreeFelling(world, state, pos, player);
    return true;
  }

  @Override
  public InteractionResult interactLivingEntity(ItemStack tool, Player player, LivingEntity entity, InteractionHand hand)
  {
    if(entity.level().isClientSide) return InteractionResult.PASS;
    return tryEntityShearing(tool, player, entity, hand);
  }

  @Override
  public float getDestroySpeed(ItemStack stack, BlockState state)
  { return this.speed; }

  // IRepairableToolItem -----------------------------------------------------------------------------------------------

  @Override
  public ItemStack onShapelessRecipeRepaired(ItemStack stack, int previousDamage, int repairedDamage)
  { return stack; }

  // Efficiency / Furtune ----------------------------------------------------------------------------------------------

  private int absoluteDmg(int dmg)
  { return (max_item_damage * (100-Mth.clamp(dmg, 1, 100))) / 100; }

  // Multi tool features -----------------------------------------------------------------------------------------------

  @SuppressWarnings("deprecation")
  private InteractionResult tryEntityShearing(ItemStack tool, Player player, LivingEntity entity, InteractionHand hand)
  {
    if((entity.level().isClientSide) || (!(entity instanceof net.minecraftforge.common.IForgeShearable target))) return InteractionResult.PASS;
    BlockPos pos = new BlockPos(entity.blockPosition());
    if (target.isShearable(tool, entity.level(), pos)) {
      List<ItemStack> drops = target.onSheared(player, tool, entity.level(), pos, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool));
      Random rand = new java.util.Random();
      drops.forEach(d -> {
        ItemEntity ent = entity.spawnAtLocation(d, 1f);
        ent.setDeltaMovement(ent.getDeltaMovement().add(((rand.nextFloat() - rand.nextFloat()) * 0.1f), (rand.nextFloat() * 0.05f), ((rand.nextFloat() - rand.nextFloat()) * 0.1f)));
      });
      tool.hurtAndBreak(1, entity, e -> e.broadcastBreakEvent(hand));
      player.level().playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 0.8f, 1.1f);
    }
    return InteractionResult.SUCCESS;
  }

  private InteractionResult tryPlantSnipping(Player player, Level world, BlockPos pos, InteractionHand hand, Direction facing, Vec3 hitvec)
  {
    if(!with_shearing) return InteractionResult.PASS;
    final ItemStack tool = player.getItemInHand(hand);
    if(tool.getItem()!=this) return InteractionResult.PASS;
    final BlockState state = world.getBlockState(pos);
    final Block block = state.getBlock();
    // replace with tag?
    if((!state.is(BlockTags.LEAVES)) && (block != Blocks.COBWEB) && (block != Blocks.GRASS) && (block != Blocks.FERN)
      && (block != Blocks.DEAD_BUSH) && (block != Blocks.VINE) && (block != Blocks.TRIPWIRE) && (!state.is(BlockTags.WOOL))
    ) return InteractionResult.PASS;
    ItemEntity ie = new ItemEntity(world, pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5, new ItemStack(block.asItem()));
    ie.setDefaultPickUpDelay();
    world.addFreshEntity(ie);
    world.setBlock(pos, Blocks.AIR.defaultBlockState(), 1|2|8);
    tool.hurtAndBreak(1, player, (p)->p.broadcastBreakEvent(player.getUsedItemHand()));
    world.playSound(player, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 0.8f, 1.1f);
    return InteractionResult.SUCCESS;
  }

  private InteractionResult tryTorchPlacing(UseOnContext context)
  {
    Player player = context.getPlayer();
    InteractionHand hand = context.getHand();
    if(!with_torch_placing) return InteractionResult.PASS;
    final Inventory inventory = player.getInventory();
    for(int i = 0; i < inventory.getContainerSize(); ++i) {
      ItemStack stack = inventory.getItem(i);
      if((!stack.isEmpty()) && (stack.getItem() == Blocks.TORCH.asItem())) {
        ItemStack tool = player.getItemInHand(hand);
        player.setItemInHand(hand, stack);
        UseOnContext torch_context = new UseOnContext(context.getPlayer(), context.getHand(), new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside()));
        InteractionResult r = stack.getItem().useOn(torch_context);
        player.setItemInHand(hand, tool);
        return r;
      }
    }
    return InteractionResult.PASS;
  }

  private InteractionResult tryDigOver(Player player, Level world, BlockPos pos, InteractionHand hand, Direction facing, Vec3 hitvec)
  {
    if(!with_hoeing) return InteractionResult.PASS;
    if(world.getBlockEntity(pos) != null) return InteractionResult.PASS;
    final BlockState state = world.getBlockState(pos);
    BlockState replaced = state;
    final Block block = state.getBlock();
    if((block instanceof GrassBlock) || (block==Blocks.DIRT)) {
      replaced = Blocks.FARMLAND.defaultBlockState();
    } else if(block instanceof FarmBlock) {
      replaced = Blocks.COARSE_DIRT.defaultBlockState();
    } else if(block==Blocks.COARSE_DIRT) {
      replaced = Blocks.DIRT_PATH.defaultBlockState();
    } else if(block instanceof DirtPathBlock) {
      replaced = Blocks.DIRT.defaultBlockState();
    }
    if(replaced != state) {
      world.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 0.8f, 1.1f);
      if(!world.isClientSide)
      {
        world.setBlock(pos, replaced,1|2);
        ItemStack stack = player.getItemInHand(hand);
        if(stack.getItem() == this) stack.hurtAndBreak(1, player, (p)->p.broadcastBreakEvent(player.getUsedItemHand()));
      }
      return InteractionResult.SUCCESS;
    } else {
      return InteractionResult.PASS;
    }
  }

  // Tree felling ------------------------------------------------------------------------------------------------------

  private boolean tryTreeFelling(Level world, BlockState state, BlockPos pos, LivingEntity player)
  {
    if((!with_tree_felling) || (!TreeCutting.canChop(state))) return false;
    BlockState state_below = world.getBlockState(pos.below());
    if((state_below!=null) && state.is(state_below.getBlock())) return false;
    return TreeCutting.chopTree(player.level(), state, pos, max_tree_felling_blocks_to_break, true) > 0;
  }

}
