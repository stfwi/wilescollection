/*
 * @file ItemAriadneCoal.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Mod internal metal dusts, applied when no other mod
 * provides a similar dust. Explicitly not registered
 * to ore dict, only smelting. Other grits shall be preferred.
 */
package wile.wilescollection.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import wile.wilescollection.blocks.AriadneCoalBlock;
import wile.wilescollection.libmc.Registries;


public class AriadneCoalItem extends ModItem
{
  public AriadneCoalItem(Item.Properties properties)
  { super(properties.stacksTo(1).defaultDurability(100).setNoRepair()); }

  @Override
  public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair)
  { return false; }

  @Override
  public boolean canBeDepleted()
  { return true; }

  @Override
  public boolean isBookEnchantable(ItemStack stack, ItemStack book)
  { return false; }

  @Override
  public boolean isEnchantable(ItemStack stack)
  { return false; }

  @Override
  public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
  {
    if(context.getLevel().isClientSide) return InteractionResult.PASS;
    final Player player = context.getPlayer();
    final InteractionHand hand = context.getHand();
    final BlockPos pos = context.getClickedPos();
    final Direction facing = context.getClickedFace();
    final Level world = context.getLevel();
    final BlockState state = world.getBlockState(pos);
    final BlockPos markpos = pos.relative(facing);
    if(((!world.isEmptyBlock(markpos)) && (!(state.getBlock() instanceof AriadneCoalBlock))) || (stack.getItem()!=this)) return InteractionResult.PASS;
    if(!Block.isFaceFull(state.getCollisionShape(world, pos, CollisionContext.of(player)), facing)) return InteractionResult.PASS;
    final double hitX = context.getClickLocation().x() - pos.getX();
    final double hitY = context.getClickLocation().y() - pos.getY();
    final double hitZ = context.getClickLocation().z() - pos.getZ();
    Vec3 v = switch (facing) {
      case WEST -> new Vec3(0.0 + hitZ, hitY, 0);
      case EAST -> new Vec3(1.0 - hitZ, hitY, 0);
      case SOUTH -> new Vec3(0.0 + hitX, hitY, 0);
      case NORTH -> new Vec3(1.0 - hitX, hitY, 0);
      default -> new Vec3(0.0 + hitX, hitZ, 0); // UP/DOWN
    };
    v = v.subtract(0.5, 0.5, 0);
    final int orientation = (((int)(Math.rint(4.0/Math.PI * Math.atan2(v.y, v.x) + 16) ) % 8) + ((facing.getAxisDirection()== Direction.AxisDirection.NEGATIVE) ? 8 : 0)) & 0xf;
    BlockState setstate = Registries.getBlock("ariadne_coal_block").defaultBlockState().setValue(AriadneCoalBlock.AXIS, facing.getAxis());
    if(world.setBlock(markpos, setstate.setValue(AriadneCoalBlock.ORIENTATION, orientation), 1|2)) {
      stack.setDamageValue(stack.getDamageValue()+1);
      if(stack.getDamageValue() >= stack.getMaxDamage()) {
        player.setItemInHand(hand, ItemStack.EMPTY);
        world.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.4f, 2f);
      } else {
        world.playSound(null, pos, SoundEvents.GRAVEL_HIT, SoundSource.BLOCKS, 0.4f, 2f);
      }
      return InteractionResult.SUCCESS;
    } else {
      return InteractionResult.FAIL;
    }
  }
}
