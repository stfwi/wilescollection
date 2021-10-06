/*
 * @file JEIPlugin.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * JEI plugin (see https://github.com/mezz/JustEnoughItems/wiki/Creating-Plugins)
 */
package wile.wilescollection.eapi.jei;
/*
public class JEIPlugin {}
*/
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IJeiRuntime;
import wile.wilescollection.blocks.EdCraftingTable.CraftingTableTileEntity;
import wile.wilescollection.libmc.detail.Auxiliaries;
import wile.wilescollection.ModConfig;
import wile.wilescollection.ModContent;
import wile.wilescollection.blocks.EdCraftingTable;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;


@mezz.jei.api.JeiPlugin
public class JEIPlugin implements mezz.jei.api.IModPlugin
{
  @Override
  public ResourceLocation getPluginUid()
  { return new ResourceLocation(Auxiliaries.modid(), "jei_plugin_uid"); }

  @Override
  public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration)
  {
    if(!ModConfig.isOptedOut(ModContent.CRAFTING_TABLE)) {
      try {
        registration.addRecipeTransferHandler(
          EdCraftingTable.CraftingTableUiContainer.class,
          VanillaRecipeCategoryUid.CRAFTING,
          1, 9, 10, 36+CraftingTableTileEntity.NUM_OF_STORAGE_SLOTS
        );
      } catch(Throwable e) {
        Auxiliaries.logger().warn("Exception in JEI crafting table handler registration: '" + e.getMessage() + "'.");
      }
    }
  }

  @Override
  public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
  {
    HashSet<Item> blacklisted = new HashSet<>();
    for(Block e: ModContent.getRegisteredBlocks()) {
      if(ModConfig.isOptedOut(e) && (e.asItem().getRegistryName().getPath()).equals((e.getRegistryName().getPath()))) {
        blacklisted.add(e.asItem());
      }
    }
    for(Item e: ModContent.getRegisteredItems()) {
      if(ModConfig.isOptedOut(e) && (!(e instanceof BlockItem))) {
        blacklisted.add(e);
      }
    }
    if(!blacklisted.isEmpty()) {
      List<ItemStack> blacklist = blacklisted.stream().map(ItemStack::new).collect(Collectors.toList());
      try {
        jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM, blacklist);
      } catch(Exception e) {
        Auxiliaries.logger().warn("Exception in JEI opt-out processing: '" + e.getMessage() + "', skipping further JEI optout processing.");
      }
    }
  }

  @Override
  public void registerRecipeCatalysts(IRecipeCatalystRegistration registration)
  {
    if(!ModConfig.isOptedOut(ModContent.CRAFTING_TABLE)) {
      registration.addRecipeCatalyst(new ItemStack(ModContent.CRAFTING_TABLE), VanillaRecipeCategoryUid.CRAFTING);
    }
  }
}
