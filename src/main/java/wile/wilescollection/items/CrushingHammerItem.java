/*
 * @file ItemCrushingHammer.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Early game manual ore duping hammer. Not much use
 * for other stuff.
 */
package wile.wilescollection.items;


import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class CrushingHammerItem extends ModItem
{
  public CrushingHammerItem(Item.Properties properties)
  { super(properties.stacksTo(1).defaultDurability(640).setNoRepair()); }

  @Override
  public int getEnchantmentValue()
  { return 0; }

  @Override
  public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair)
  { return false; }

  @Override
  public boolean canBeDepleted()
  { return true; }

  @Override
  public boolean isBookEnchantable(ItemStack stack, ItemStack book)
  { return false; }

  @Override
  public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment)
  { return false; }

  @Override
  public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType)
  { return 0; }

  @Override
  public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker)
  { return true; }

  @Override
  public boolean onLeftClickEntity(ItemStack stack, Player player, Entity target)
  {
    if(!(target instanceof LivingEntity)) return true;
    Level world = player.getCommandSenderWorld();
    if(world.isClientSide()) return true;
    boolean hard = (target instanceof Monster) && (((Monster)target).getTarget() != null);
    ((LivingEntity)target).knockback(hard ? 1.2f : 0.3f, Math.sin(Math.PI/180 * player.getYHeadRot()), -Math.cos(Math.PI/180 * player.getYHeadRot()));
    if(hard) {
      if(world.getRandom().nextInt(1) == 0) stack.hurtAndBreak(1, player, p->p.broadcastBreakEvent(player.getUsedItemHand()));
      world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_PLACE, player.getSoundSource(), 0.2f, 0.05f); // ITEM_TRIDENT_HIT_GROUND
    } else {
      world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BAMBOO_HIT, player.getSoundSource(), 0.5f, 0.3f);
    }
    return true;
  }

  @Override
  public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity player)
  {
    if((world.isClientSide()) || (!(player instanceof Player))) return true;
    if(state.getDestroySpeed(world, pos) > 0.5) stack.hurtAndBreak(1, player, p->p.broadcastBreakEvent(player.getUsedItemHand()));
    return false;
  }
}
