/*
 * @file Armors.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Armor collection.
 */
package wile.wilescollection.items;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import wile.wilescollection.ModConfig;
import wile.wilescollection.ModWilesCollection;
import wile.wilescollection.libmc.detail.Auxiliaries;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;


public class Armors
{
  public static long CFG_DEFAULT               = 0x0000000000000000L;
  public static long CFG_MAKES_PIGLINS_NEUTRAL = 0x0000000100000000L;

  public static abstract class ModArmorItem extends ArmorItem
  {
    protected Map<Attribute, AttributeModifier> modifiers_ = new HashMap<>();
    protected final long armor_config;

    public ModArmorItem(long config, ArmorMaterial material, EquipmentSlot slot, Properties properties)
    {
      super(material, slot, properties.tab(ModWilesCollection.ITEMGROUP));
      armor_config = config;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag)
    { Auxiliaries.Tooltip.addInformation(stack, world, tooltip, flag, true); }

    @Override
    public Collection<CreativeModeTab> getCreativeTabs()
    { return ModConfig.isOptedOut(this) ? (ModItem.DISABLED_TABS) : (ModItem.ENABLED_TABS); }

    @Override
    @SuppressWarnings("all")
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot)
    {
      final Multimap<Attribute, AttributeModifier> modifiers = super.getDefaultAttributeModifiers(slot);
      return (!modifiers.isEmpty()) ? MultimapBuilder.hashKeys().hashSetValues().build(modifiers) : MultimapBuilder.hashKeys().hashSetValues().build();
    }

    protected AttributeModifier attributeModifier(Attribute attribute, Supplier<AttributeModifier> initializer)
    {
      AttributeModifier modifier = modifiers_.get(attribute);
      if(modifier != null) return modifier;
      modifier = initializer.get();
      modifiers_.put(attribute, modifier);
      return modifier;
    }

    @Override
    public boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer)
    { return (armor_config & CFG_MAKES_PIGLINS_NEUTRAL)!=0; }

  }

  public static class HelmetArmorItem extends ModArmorItem
  {
    public HelmetArmorItem(long config, ArmorMaterial material, Properties properties)
    { super(config, material, EquipmentSlot.HEAD, properties); }
  }

  public static class ChestPlateArmorItem extends ModArmorItem
  {
    public ChestPlateArmorItem(long config, ArmorMaterial material, Properties properties)
    { super(config, material, EquipmentSlot.CHEST, properties); }
  }

  public static class LeggingsArmorItem extends ModArmorItem
  {
    public LeggingsArmorItem(long config, ArmorMaterial material, Properties properties)
    { super(config, material, EquipmentSlot.LEGS, properties); }
  }

  public static class BootsArmorItem extends ModArmorItem
  {
    public BootsArmorItem(long config, ArmorMaterial material, Properties properties)
    { super(config, material, EquipmentSlot.FEET, properties); }
  }

}
