/*
 * @file ChargedLapisSqueezerItem.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * XP storage consumable.
 */
package wile.wilescollection.items;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

import wile.wilescollection.ModContent;
import wile.wilescollection.libmc.detail.Auxiliaries;
import wile.wilescollection.libmc.detail.Inventories;
import wile.wilescollection.libmc.detail.Overlay;

public class ChargedLapisSqueezerItem extends ModItem
{
  private static int min_xp_level = 15;

  public static void on_config(int min_player_xp_level)
  {
    min_xp_level = Mth.clamp(min_player_xp_level, 1, 30);
  }

  //--------------------------------------------------------------------------------------------------------------------
  // Item
  //--------------------------------------------------------------------------------------------------------------------

  public ChargedLapisSqueezerItem(Item.Properties properties)
  { super(properties.stacksTo(64)); }

  @Override
  public boolean isFoil(ItemStack stack)
  { return false; }

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
    if(world.isClientSide()) return InteractionResultHolder.success(stack);
    onUseTick(world, player, stack, 1);
    return InteractionResultHolder.success(stack);
  }

  @Override
  public void onUseTick(Level world, LivingEntity entity, ItemStack squeezer, int count)
  {
    if(!(entity instanceof final Player player) || (world.isClientSide())) return;
    if(player.experienceLevel < min_xp_level) {
      Overlay.show(player, Auxiliaries.localizable("item."+Auxiliaries.modid()+".charged_lapis_squeezer.msg.noxp"));
      world.playSound(null, player.blockPosition(), SoundEvents.WOOL_BREAK, SoundSource.PLAYERS, 1f, 0.6f);
      return;
    }
    if(player.getHealth() <= (player.getMaxHealth()/10)) {
      Overlay.show(player, Auxiliaries.localizable("item."+Auxiliaries.modid()+".charged_lapis_squeezer.msg.lowhealth"));
      world.playSound(null, player.blockPosition(), SoundEvents.WOOL_BREAK, SoundSource.PLAYERS, 1f, 0.6f);
      return;
    }
    final ItemStack lapis = Inventories.extract(Inventories.itemhandler(player), new ItemStack(Items.LAPIS_LAZULI), 1, false);
    if(lapis.isEmpty()) {
      Overlay.show(player, Auxiliaries.localizable("item."+Auxiliaries.modid()+".charged_lapis_squeezer.msg.nolapis"));
      world.playSound(null, player.blockPosition(), SoundEvents.WOOL_BREAK, SoundSource.PLAYERS, 1f, 0.6f);
      return;
    }
    world.playSound(null, player.blockPosition(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 0.2f, 1.4f);
    world.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.5f, 1.4f);
    Inventories.give(player, new ItemStack(ModContent.CHARGED_LAPIS));
    player.giveExperienceLevels(-1);
    player.causeFoodExhaustion(4f);
    player.setHealth(player.getHealth()-(player.getMaxHealth()/10));
    player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20, 0));
  }

}
