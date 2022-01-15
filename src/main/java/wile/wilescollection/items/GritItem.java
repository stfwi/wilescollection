/*
 * @file ItemGrit.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Mod internal metal dusts, applied when no other mod
 * provides a similar dust. Explicitly not registered
 * to ore dict, only smelting. Other grits shall be preferred.
 */
package wile.wilescollection.items;

import net.minecraft.world.item.Item;

public class GritItem extends ModItem
{
  public GritItem(Item.Properties properties)
  { super(properties.stacksTo(64)); }
}
