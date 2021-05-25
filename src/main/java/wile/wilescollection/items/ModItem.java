/*
 * @file ModItem.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Basic item functionality for mod items.
 */
package wile.wilescollection.items;

import wile.wilescollection.ModWilesCollection;
import wile.wilescollection.ModConfig;
import wile.wilescollection.libmc.detail.Auxiliaries;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ModItem extends Item
{
  public static final Collection<ItemGroup> ENABLED_TABS  = Collections.singletonList(ModWilesCollection.ITEMGROUP);
  public static final Collection<ItemGroup> DISABLED_TABS = new ArrayList<ItemGroup>();

  public ModItem(Item.Properties properties)
  { super(properties.group(ModWilesCollection.ITEMGROUP)); }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag)
  { Auxiliaries.Tooltip.addInformation(stack, world, tooltip, flag, true); }

  @Override
  public Collection<ItemGroup> getCreativeTabs()
  { return ModConfig.isOptedOut(this) ? (DISABLED_TABS) : (ENABLED_TABS); }

}
