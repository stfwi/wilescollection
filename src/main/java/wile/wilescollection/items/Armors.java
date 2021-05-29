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
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
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

    public ModArmorItem(long config, IArmorMaterial material, EquipmentSlotType slot, Properties properties)
    {
      super(material, slot, properties.group(ModWilesCollection.ITEMGROUP));
      armor_config = config;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag)
    { Auxiliaries.Tooltip.addInformation(stack, world, tooltip, flag, true); }

    @Override
    public Collection<ItemGroup> getCreativeTabs()
    { return ModConfig.isOptedOut(this) ? (ModItem.DISABLED_TABS) : (ModItem.ENABLED_TABS); }

    @Override
    @SuppressWarnings("all")
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot)
    {
      final Multimap<Attribute, AttributeModifier> modifiers = super.getAttributeModifiers(slot);
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
    public HelmetArmorItem(long config, IArmorMaterial material, Properties properties)
    { super(config, material, EquipmentSlotType.HEAD, properties); }
  }

  public static class ChestPlateArmorItem extends ModArmorItem
  {
    public ChestPlateArmorItem(long config, IArmorMaterial material, Properties properties)
    { super(config, material, EquipmentSlotType.CHEST, properties); }
  }

  public static class LeggingsArmorItem extends ModArmorItem
  {
    public LeggingsArmorItem(long config, IArmorMaterial material, Properties properties)
    { super(config, material, EquipmentSlotType.LEGS, properties); }
  }

  public static class BootsArmorItem extends ModArmorItem
  {
    public BootsArmorItem(long config, IArmorMaterial material, Properties properties)
    { super(config, material, EquipmentSlotType.FEET, properties); }
  }

}
