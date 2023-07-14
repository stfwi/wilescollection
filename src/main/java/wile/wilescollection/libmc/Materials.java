/*
 * @file Materials.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Material handling.
 */
package wile.wilescollection.libmc;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Supplier;


public class Materials
{

  public static class CustomArmorMaterial implements ArmorMaterial
  {
    final private String name_;
    final private SoundEvent sound_event_;
    final private Supplier<Ingredient> repair_material_;
    private int durability_[] = {1,1,1,1};
    private int damage_reduction_[] = {1,1,1,1};
    private int enchantability_ = 10;
    private float toughness_ = 0;
    private float knockback_resistance_ = 0;

    public CustomArmorMaterial(String name, SoundEvent sound_event, Supplier<Ingredient> repair_material, int damage_reduction[], int durability[], int enchantability, float toughness, float knockback_resistance)
    {
      name_ = name;
      sound_event_ = sound_event;
      repair_material_ = repair_material;
      configure(damage_reduction, durability, enchantability, toughness, knockback_resistance);
    }

    public void configure(int damage_reduction[], int durability[], int enchantability, float toughness, float knockback_resistance)
    {
      System.arraycopy(damage_reduction, 0, damage_reduction_, 0, Math.min(damage_reduction.length, damage_reduction_.length));
      durability_ = durability;
      enchantability_ = enchantability;
      toughness_ = toughness;
      knockback_resistance_ = knockback_resistance;
    }

    @OnlyIn(Dist.CLIENT)
    public String getName()
    { return name_; }

    public int getDurabilityForType(ArmorItem.Type type)
    { return durability_[type.getSlot().getIndex()]; }

    public int getDefenseForType(ArmorItem.Type type)
    { return damage_reduction_[type.getSlot().getIndex()]; }

    public int getEnchantmentValue()
    { return enchantability_; }

    public SoundEvent getEquipSound()
    { return sound_event_; }

    public Ingredient getRepairIngredient()
    { return repair_material_.get(); }

    public float getToughness()
    { return toughness_; }

    public float getKnockbackResistance()
    { return knockback_resistance_; }
  }
}
