/*
 * @file Trinkets.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Trinket collection.
 */
package wile.wilescollection.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import wile.wilescollection.libmc.Auxiliaries;

import javax.annotation.Nullable;
import java.util.*;


public class Trinkets
{
  public static final long CFG_DEFAULT               = 0x0000000000000000L;

  private static int max_charges_ = 4096;
  private static int repair_max_tick_damage_ = 100;
  private static Map<Item, Integer> charging_items_ = new HashMap<>();
  private static Set<Enchantment> allowed_enchantments_ = new HashSet<>();

  public static void on_config()
  {
    charging_items_.clear();
    charging_items_.put(Items.LAPIS_LAZULI, 25);
    charging_items_.put(Items.LAPIS_BLOCK, 25*9);
    charging_items_.put(Items.AMETHYST_SHARD, 20);
    charging_items_.put(Items.AMETHYST_BLOCK, 50);
  }

  //--------------------------------------------------------------------------------------------------------------------
  // Item
  //--------------------------------------------------------------------------------------------------------------------

  public static class TrinketItem extends ModItem
  {
    protected final long trinket_config;

    public TrinketItem(long config, Properties properties)
    {
      super(properties);
      trinket_config = config;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag)
    {
      if(!Auxiliaries.Tooltip.extendedTipCondition() && !Auxiliaries.Tooltip.helpCondition()) {
        final int charges = getCharge(stack);
        if(charges > 0) {
          tooltip.add(Component.translatable(
            Auxiliaries.localize(getDescriptionId()+".tip.charge", new Object[]{charges})
          ));
        }
      }
      super.appendHoverText(stack, world, tooltip, flag);
    }

    @Override
    public boolean isBarVisible(ItemStack stack)
    { return getDurability(stack) > 0; }

    @Override
    public int getBarWidth(ItemStack stack)
    { return (int)Math.round(13f * getDurability(stack)); }

    @Override
    public int getBarColor(ItemStack stack)
    { return 0x2d9ff7; }

    @Override
    public boolean isFoil(ItemStack stack)
    { return false; }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity)
    { return true; }

    @Override
    public boolean isEnchantable(ItemStack stack)
    { return true; }

    @Override
    @SuppressWarnings("deprecation")
    public int getEnchantmentValue()
    { return 35; }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book)
    { return EnchantmentHelper.getEnchantments(book).keySet().stream().anyMatch((ench)->allowed_enchantments_.contains(ench)); }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment)
    { return allowed_enchantments_.contains(enchantment); }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected)
    {
      if((!stack.hasTag()) || ((world.getGameTime() & 0x3) != 0) || (!(entity instanceof ServerPlayer))) return;
      final CompoundTag tag = getNbt(stack);
      boolean changed = false;
      changed |= repairTick(stack, world, (ServerPlayer)entity, itemSlot, isSelected);
      if(changed) updateDurabilityBarValue(stack);
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack)
    { return true; }

    @Override
    @Nullable
    public Entity createEntity(Level world, Entity entity, ItemStack stack)
    {
      if(entity instanceof ItemEntity) {
        ((ItemEntity)entity).setExtendedLifetime();
        ((ItemEntity)entity).setInvulnerable(true);
      }
      return null;
    }

    // -----------------------------------------------------------------------------------------------------------------

    private CompoundTag getNbt(ItemStack trinket)
    { return trinket.getOrCreateTagElement("trinket"); }

    private TrinketItem setNbt(ItemStack trinket, CompoundTag nbt)
    { trinket.getOrCreateTag().put("trinket", nbt); return this; }

    private float getDurability(ItemStack trinket)
    { return getNbt(trinket).getFloat("durability"); }

    private TrinketItem setDurability(ItemStack trinket, float value)
    { getNbt(trinket).putFloat("durability", Mth.clamp(value, 0f, 1f)); return this; }

    private void updateDurabilityBarValue(ItemStack trinket)
    {
      float charges = (((float) getCharge(trinket)) / Math.max(getMaxCharge(trinket), 1));
      setDurability(trinket, charges);
    }

    // -----------------------------------------------------------------------------------------------------------------

    private void setCharge(ItemStack trinket, int charges)
    {
      if(charges <= 0) {
        getNbt(trinket).remove("charge");
      } else {
        getNbt(trinket).putInt("charge", charges);
      }
    }

    private int getCharge(ItemStack trinket)
    { return getNbt(trinket).getInt("charge"); }

    private int getMaxCharge(ItemStack trinket)
    { return max_charges_; }

    // -----------------------------------------------------------------------------------------------------------------

    private void rechargeTick(final ItemStack trinket, final Level world, final ItemEntity entity)
    {
      // Recharging by tossing items
      if(!entity.isOnGround()) {
        Vec3 pos = Vec3.atLowerCornerOf(entity.blockPosition()).add(0.5,0, 0.5);
        if(entity.position().distanceToSqr(pos) > 0.01) {
          entity.setPos(pos.x, entity.getY(), pos.z);
          entity.setDeltaMovement(0, entity.getDeltaMovement().y,0);
        }
      }
      // Recharging by tossing items
      {
        final List<ItemEntity> near_items = world.getEntitiesOfClass(ItemEntity.class, (new AABB(entity.blockPosition())).deflate(0.5).inflate(0,0.5,0), e->(e.isAlive()));
        if(!near_items.isEmpty()) {
          boolean changed = false;
          for(ItemEntity e:near_items) {
            final ItemStack stack = e.getItem();
            final Item item = stack.getItem();
            if(charging_items_.containsKey(item)) {
              final int max_charge = getMaxCharge(trinket);
              int charge = getCharge(trinket);
              if(charge < max_charge) {
                final int item_charges = charging_items_.getOrDefault(item, 1);
                final int n = Mth.clamp((int)Math.ceil(((float)max_charge-charge)/item_charges), 1, stack.getCount());
                setCharge(trinket, charge+(n*item_charges));
                changed = true;
                if(n >= stack.getCount()) {
                  e.getItem().setCount(0);
                  e.remove(Entity.RemovalReason.DISCARDED);
                } else {
                  e.getItem().shrink(n);
                }
              }
            }
          }
          if(changed) {
            updateDurabilityBarValue(trinket);
            world.playSound(null, entity.blockPosition(), SoundEvents.CHICKEN_EGG, SoundSource.BLOCKS, 0.7f, 1.8f);
          }
        }
      }
      // Pickup only when player close to the item.
      {
        Vec3 pos = entity.position();
        AABB bb = new AABB(pos.add(0.5, 2, 0.5), pos.subtract(0.5, 1, 0.5));
        if(world.getEntitiesOfClass(Player.class, bb, e->(e.isAlive())).isEmpty()) {
          entity.setPickUpDelay(10);
        }
      }
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity)
    {
      if(entity.isAlive()) {
        final Block block_below = entity.level.getBlockState(entity.blockPosition().below()).getBlock();
        if(block_below == Blocks.ANVIL || block_below == Blocks.DAMAGED_ANVIL || block_below == Blocks.CHIPPED_ANVIL) {
          rechargeTick(stack, entity.level, entity);
        }
      }
      return false;
    }

    // -----------------------------------------------------------------------------------------------------------------

    private boolean repairTick(ItemStack trinket, Level world, ServerPlayer player, int itemSlot, boolean isSelected)
    {
      if(((world.getGameTime() & 0x7f) != 0)) return false;
      final int initial_repair_left = getCharge(trinket);
      if(initial_repair_left == 0) return false;
      int repair_left = initial_repair_left;
      for(ItemStack armor:player.getArmorSlots()) repair_left = repair(trinket, world, player, repair_left, armor);
      if(!player.swinging) {
        repair_left = repair(trinket, world, player, repair_left, player.getMainHandItem());
        repair_left = repair(trinket, world, player, repair_left, player.getOffhandItem());
      }
      setCharge(trinket, repair_left);
      if(repair_left <= 0) world.playSound(null, player.blockPosition(), SoundEvents.CHAIN_BREAK, SoundSource.PLAYERS,1f, 1.5f);
      return repair_left != initial_repair_left;
    }

    private int repair(ItemStack trinket, Level world, ServerPlayer player, int repair_left, ItemStack item)
    {
      if((repair_left<=0) || item.isEmpty() || (!item.isDamaged()) || (!item.isDamageableItem())) return repair_left;
      if(!(item.getItem() instanceof ArmorItem) && !(item.getItem() instanceof TieredItem) && !(item.getItem() instanceof ProjectileWeaponItem)) return repair_left;
      final int max_repair = Mth.clamp(Math.min(repair_max_tick_damage_, item.getMaxDamage()/10), 1, repair_left);
      final int repair = Math.min(item.getDamageValue(), max_repair);
      item.setDamageValue(item.getDamageValue() - repair);
      return Math.max(repair_left - repair, 0);
    }

  }

}
