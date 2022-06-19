/*
 * @file ChargedLapisItem.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * XP storage consumable.
 */
package wile.wilescollection.items;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;


public class ChargedLapisItem extends ModItem
{
  public ChargedLapisItem(Item.Properties properties)
  { super(properties.stacksTo(64)); }

  @Override
  public boolean isFoil(ItemStack stack)
  { return true; }

  @Override
  public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment)
  { return false; }

  @Override
  public boolean isBookEnchantable(ItemStack stack, ItemStack book)
  { return false; }

  @Override
  public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
  {
    ItemStack stack = player.getItemInHand(hand);
    onUseTick(world, player, stack, 1);
    return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
  }

  @Override
  @SuppressWarnings("deprecation")
  public void onUseTick(Level world, LivingEntity entity, ItemStack stack, int count)
  {
    if(!(entity instanceof Player player)) return;
    if(world.isClientSide()) {
      float rnd = (float)(world.getRandom().nextDouble()*.2);
      for(float pitch: new float[]{0.45f,0.4f,0.5f}) world.playSound(player, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.05f, pitch+rnd);
    } else {
      stack.shrink(1);
      player.giveExperienceLevels(1);
      player.curePotionEffects(new ItemStack(Items.MILK_BUCKET));
      player.clearFire();
      player.heal(player.getMaxHealth()/20);
    }
  }

}
