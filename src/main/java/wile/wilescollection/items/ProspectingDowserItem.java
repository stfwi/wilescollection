/*
 * @file ProspectingDowserItem.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Block searching dowsing stick.
 */
package wile.wilescollection.items;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.registries.ForgeRegistries;
import wile.wilescollection.ModWilesCollection;
import wile.wilescollection.detail.ModRenderers;
import wile.wilescollection.libmc.detail.Auxiliaries;
import wile.wilescollection.libmc.detail.Registries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;


public class ProspectingDowserItem extends ModItem
{
  private static int search_range = 12;

  public ProspectingDowserItem(Item.Properties properties)
  { super(properties.stacksTo(1)); }

  @OnlyIn(Dist.CLIENT)
  public void registerModels()
  {
    net.minecraftforge.client.model.ForgeModelBakery.addSpecialModel(new ModelResourceLocation(new ResourceLocation(ModWilesCollection.MODID, "prospecting_dowser_model"), "inventory"));
    net.minecraftforge.client.model.ForgeModelBakery.addSpecialModel(new ModelResourceLocation(new ResourceLocation(ModWilesCollection.MODID, "prospecting_dowser_model_e"), "inventory"));
  }

  @Override
  public void initializeClient(Consumer<IItemRenderProperties> consumer)
  {
    consumer.accept(new IItemRenderProperties() {
      @Override public BlockEntityWithoutLevelRenderer getItemStackRenderer()
      { return new ModRenderers.ProspectingDowserIster();
      }});
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag)
  {
    super.appendHoverText(stack, world, tooltip, flag);
    if(!stack.hasTag()) return;
    final ResourceLocation rl = ResourceLocation.tryParse(stack.getTag().getString("target"));
    if(rl == null) return;
    final Block block = ForgeRegistries.BLOCKS.getValue(rl);
    if(block == null) return;
    tooltip.add(Auxiliaries.localizable("item.wilescollection.prospecting_dowser.status", new Object[]{block.getName()}));
  }

  public boolean isFoil(ItemStack stack)
  { return false; }

  public boolean isFireResistant()
  { return true; }

  @Override
  public boolean isEnchantable(ItemStack stack)
  { return false; }

  @Override
  public boolean isBookEnchantable(ItemStack stack, ItemStack book)
  { return false; }

  @Override
  public boolean isRepairable(ItemStack stack)
  { return false; }

  @Override
  public boolean isBarVisible(ItemStack stack)
  { return false; }

  @Override
  public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean isSelected)
  {
    if(!world.isClientSide()) return; // client only
    final CompoundTag tag = stack.getOrCreateTag();
    final TagKey<Block> searches = Registries.getBlockTagKey("prospectible");
    if(isSelected && (searches!=null)) {
      final ResourceLocation rl = ResourceLocation.tryParse(tag.getString("target"));
      if(rl != null) {
        final BlockPos pos_found = blockSearch(world, entity.blockPosition(), searches);
        if(pos_found != null) {
          final Vec3 dist_vec = entity.position().add(0, entity.getEyeHeight(), 0).subtract(Vec3.atCenterOf(pos_found));
          final double dist = ((search_range - dist_vec.length()) / search_range);
          if(dist > 1e-2) {
            final double look = (entity.getLookAngle().subtract(dist_vec.normalize()).lengthSqr() * 2); // 0..8
            final int intensity = (int)Mth.clamp((dist * dist * 4) + (look * (Math.min(dist + 0.5, 1))), 0, 15);
            tag.putInt("rotation", (tag.getInt("rotation") + intensity * 8) % 360);
            tag.putString("target", world.getBlockState(pos_found).getBlock().getRegistryName().toString());
            return;
          }
        }
      }
    }
    tag.remove("rotation");
    tag.remove("target");
  }

  // ------------------------------------------------------------------------------------------------------

  @Nullable
  private BlockPos blockSearchPN(Level world, BlockPos origin, TagKey<Block> match_block, int x, int y, int z)
  {
    if(world.getBlockState(origin.offset( x, y, z)).is(match_block)) return origin.offset( x, y, z);
    if(world.getBlockState(origin.offset(-x, y, z)).is(match_block)) return origin.offset(-x, y, z);
    if(world.getBlockState(origin.offset( x,-y, z)).is(match_block)) return origin.offset( x,-y, z);
    if(world.getBlockState(origin.offset(-x,-y, z)).is(match_block)) return origin.offset(-x,-y, z);
    if(world.getBlockState(origin.offset( x, y,-z)).is(match_block)) return origin.offset( x, y,-z);
    if(world.getBlockState(origin.offset(-x, y,-z)).is(match_block)) return origin.offset(-x, y,-z);
    if(world.getBlockState(origin.offset( x,-y,-z)).is(match_block)) return origin.offset( x,-y,-z);
    if(world.getBlockState(origin.offset(-x,-y,-z)).is(match_block)) return origin.offset(-x,-y,-z);
    return null;
  }

  @Nullable
  private BlockPos blockSearch(Level world, BlockPos origin, TagKey<Block> match_block)
  {
    for(int y=0; y<search_range; ++y) {
      for(int x=0; x<search_range; ++x) {
        for(int z=0; z<search_range; ++z) {
          BlockPos p = blockSearchPN(world, origin, match_block, x, y, z);
          if(p != null) return p;
        }
      }
    }
    return null;
  }

}
