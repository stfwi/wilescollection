/*
 * @file JEIPlugin.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * JEI plugin (see https://github.com/mezz/JustEnoughItems/wiki/Creating-Plugins)
 */
package wile.wilescollection.eapi.jei;

public class JEIPlugin {}
/*
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import wile.wilescollection.ModConfig;
import wile.wilescollection.blocks.EdCraftingTable;
import wile.wilescollection.blocks.EdCraftingTable.CraftingTableTileEntity;
import wile.wilescollection.libmc.detail.Auxiliaries;
import wile.wilescollection.libmc.detail.Registries;

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
    if(!ModConfig.isOptedOut(Registries.getBlock("crafting_table"))) {
      try {
        registration.addRecipeTransferHandler(
          EdCraftingTable.CraftingTableUiContainer.class,
          RecipeTypes.CRAFTING,
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
    for(Block e: Registries.getRegisteredBlocks()) {
      if(ModConfig.isOptedOut(e) && (e.asItem().getRegistryName().getPath()).equals((e.getRegistryName().getPath()))) {
        blacklisted.add(e.asItem());
      }
    }
    for(Item e: Registries.getRegisteredItems()) {
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
    if(!ModConfig.isOptedOut(Registries.getBlock("crafting_table"))) {
      registration.addRecipeCatalyst(new ItemStack(Registries.getBlock("crafting_table")), RecipeTypes.CRAFTING);
    }
  }
}
*/