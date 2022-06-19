/*
 * @file TrackerItem.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 */
package wile.wilescollection.items;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IItemRenderProperties;
import wile.wilescollection.detail.ModRenderers;
import wile.wilescollection.libmc.detail.Auxiliaries;
import wile.wilescollection.libmc.detail.Overlay;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;


public class TrackerItem extends ModItem
{
  public TrackerItem(Item.Properties properties)
  { super(properties.stacksTo(1).setNoRepair()); }

  //--------------------------------------------------------------------------------------------------------------------
  // Item
  //--------------------------------------------------------------------------------------------------------------------

  @Override
  public void initializeClient(Consumer<IItemRenderProperties> consumer)
  {
    consumer.accept(new IItemRenderProperties() {
      @Override public BlockEntityWithoutLevelRenderer getItemStackRenderer()
      { return new ModRenderers.TrackerIster();
      }});
  }

  @OnlyIn(Dist.CLIENT)
  public void registerModels()
  {
    net.minecraftforge.client.model.ForgeModelBakery.addSpecialModel(new ModelResourceLocation(new ResourceLocation(Auxiliaries.modid(), "tracking_compass_pointer_model"), "inventory"));
  }

  @Override
  public boolean isFoil(ItemStack stack)
  { return false; }

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
  @OnlyIn(Dist.CLIENT)
  public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag)
  {
    if(Auxiliaries.Tooltip.helpCondition() || Auxiliaries.Tooltip.extendedTipCondition()) {
      super.appendHoverText(stack, world, tooltip, flag);
      return;
    }
    CompoundTag nbt = stack.getTagElement("trackerdata");
    if(nbt==null) return;
    int distance = -1;
    String dimension_name = "";
    String text = "";
    if(nbt.contains("target")) {
      BlockPos target_pos = BlockPos.of(nbt.getLong("target"));
      if(nbt.contains("playerpos")) distance = (int)Math.sqrt(BlockPos.of(nbt.getLong("playerpos")).distSqr(target_pos));
    } else {
      dimension_name = nbt.getString("dimensionid");
    }
    if(nbt.contains("location")) {
      BlockPos pos = BlockPos.of(nbt.getLong("location"));
      text = "["+pos.getX()+","+pos.getY()+","+pos.getZ()+"]";
      text = Auxiliaries.localizable("item."+Auxiliaries.modid()+".tracking_compass.tip.target.location", text).getString();
    } else if(nbt.contains("entityname")) {
      text = nbt.getString("entityname");
      if(text.isEmpty()) return;
      text = Auxiliaries.localizable("item."+Auxiliaries.modid()+".tracking_compass.tip.target.entity", text).getString();
    }
    if(distance >= 0) {
      if(distance > 0) {
        text += Auxiliaries.localizable("item."+Auxiliaries.modid()+".tracking_compass.tip.target.distance", distance).getString();
      }
    } else if(!dimension_name.isEmpty()) {
      text += Auxiliaries.localizable("item."+Auxiliaries.modid()+".tracking_compass.tip.dimension."+dimension_name, distance).getString();
    }
    tooltip.add(Component.translatable(text));
  }

  @Override
  public InteractionResult useOn(UseOnContext context)
  {
    Level world = context.getLevel();
    if(world.isClientSide()) return InteractionResult.CONSUME;
    if(!checkOverwrite(context.getItemInHand(), context.getPlayer())) return InteractionResult.FAIL;
    CompoundTag nbt = new CompoundTag();
    nbt.putLong("location", context.getClickedPos().asLong());
    nbt.putInt("dimension", dimensionIdentifier(world));
    nbt.putString("dimensionid", dimensionName(world));
    context.getItemInHand().addTagElement("trackerdata", nbt);
    Overlay.show(context.getPlayer(), Auxiliaries.localizable("item."+Auxiliaries.modid()+".tracking_compass.msg.locationset"));
    return InteractionResult.SUCCESS;
  }

  @Override
  public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand)
  {
    if(player.getCommandSenderWorld().isClientSide()) return InteractionResult.CONSUME;
    if(!checkOverwrite(stack, player)) return InteractionResult.FAIL;
    CompoundTag nbt = new CompoundTag();
    nbt.putUUID("entity", target.getUUID());
    nbt.putString("entityname", target.getDisplayName().getString());
    stack.addTagElement("trackerdata", nbt);
    Overlay.show(player, Auxiliaries.localizable("item."+Auxiliaries.modid()+".tracking_compass.msg.entityset", target.getDisplayName()));
    return InteractionResult.SUCCESS;
  }

  @Override
  public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected)
  {
    if(!(entity instanceof Player)) return;
    if(world instanceof ServerLevel) {
      serverTick(stack, (ServerLevel)world, (Player)entity, slot, selected);
    } else {
      clientTick(stack, world, (Player)entity, slot, selected);
    }
  }

  //--------------------------------------------------------------------------------------------------------------------
  // Tracker
  //--------------------------------------------------------------------------------------------------------------------

  private boolean checkOverwrite(ItemStack stack, Player player)
  {
    CompoundTag nbt = stack.getTagElement("trackerdata");
    if((nbt==null) || (nbt.isEmpty())) return true;
    if(player.getLookAngle().y < -0.98) {
      stack.setTag(null);
      Overlay.show(player, Auxiliaries.localizable("item."+Auxiliaries.modid()+".tracking_compass.hint.cleared"));
    } else {
      Overlay.show(player, Auxiliaries.localizable("item."+Auxiliaries.modid()+".tracking_compass.hint.clearfirst"));
    }
    return false;
  }

  //Dist.CLIENT
  private static final ConcurrentHashMap<Integer, Tuple<Integer,Integer>> tracker_angles = new ConcurrentHashMap<Integer, Tuple<Integer,Integer>>();

  //Dist.CLIENT
  public static Optional<Tuple<Integer,Integer>> getUiAngles(ItemStack stack)
  {
    final CompoundTag nbt = stack.getTagElement("trackerdata");
    if((nbt==null) || (!nbt.contains("id"))) return Optional.empty();
    Tuple<Integer,Integer> rot = tracker_angles.getOrDefault(nbt.getInt("id"), null);
    return (rot==null) ? Optional.empty() : Optional.of(rot);
  }

  private int dimensionIdentifier(Level world)
  { return dimensionName(world).hashCode(); }

  private String dimensionName(Level world)
  {
    if(world.dimension() == Level.OVERWORLD) return "overworld";
    if(world.dimension() == Level.NETHER) return "nether";
    if(world.dimension() == Level.END) return "end";
    return "other";
  }

  //Dist.CLIENT
  private void clientTick(ItemStack stack, Level world, Player player, int slot, boolean selected)
  {
    final CompoundTag nbt = stack.getTagElement("trackerdata");
    if((nbt==null) || nbt.isEmpty() || (!nbt.contains("id"))) {
      return;
    } else if(!nbt.contains("target")) {
      tracker_angles.remove(nbt.getInt("id"));
      return;
    } else {
      BlockPos pos = BlockPos.of(nbt.getLong("target"));
      Vec3 gdv = (new Vec3(pos.getX(), pos.getY(), pos.getZ())).subtract(player.position());
      final double dsq = gdv.lengthSqr();
      if(dsq < 0.3) return;
      final double xz_distance = (new Vec3(gdv.x, 0, gdv.z)).length();
      final double y_distance = Math.abs(gdv.y);
      gdv = gdv.normalize();
      final Vec3 ldv = player.getLookAngle();
      double ry = (Math.atan2(ldv.z, ldv.x) - Math.atan2(gdv.z, gdv.x)) * 180./Math.PI;
      double rx = (y_distance+5 > xz_distance) ? ((Math.acos(ldv.y)-Math.acos(gdv.y)) * 180./Math.PI) : (0);
      final double inc = 10;
      if(rx > 180) rx -= 360;
      if(ry > 180) ry -= 360;
      if(Math.abs(rx) < 30) rx = 0;
      nbt.putLong("playerpos", new BlockPos(player.position()).asLong());
      tracker_angles.put(nbt.getInt("id"), new Tuple<Integer,Integer>((int)rx,(int)ry));
    }
  }

  private void serverTick(ItemStack stack, ServerLevel world, Player player, int slot, boolean selected)
  {
    if((world.getGameTime() & 0x7) != 0) return;
    if((stack.getTag()==null) || (stack.getTag().isEmpty())) return;
    boolean changed = false;
    final CompoundTag nbt = stack.getOrCreateTagElement("trackerdata");
    if(!nbt.contains("id")) {
      nbt.putInt("id", world.getRandom().nextInt());
      if(tracker_angles.size() > 128) {
        tracker_angles.clear();
      }
    }
    if(nbt.contains("location")) {
      long pos = nbt.getLong("location");
      long uipos = nbt.getLong("target");
      if(dimensionIdentifier(world) != nbt.getLong("dimension")) {
        if(nbt.contains("target")) {
          nbt.remove("target");
          changed = true;
        }
      } else if(pos != uipos) {
        nbt.putLong("target", pos);
        changed = true;
      }
    } else if(nbt.contains("entity")) {
      Entity target = world.getEntity(nbt.getUUID("entity"));
      if((target == null) || (target.getCommandSenderWorld()==null) || (dimensionIdentifier(target.getCommandSenderWorld()) != dimensionIdentifier(player.getCommandSenderWorld()))) {
        if(nbt.contains("target")) {
          nbt.remove("target");
          changed = true;
        }
      } else {
        BlockPos uipos = BlockPos.of(nbt.getLong("target"));
        if(player.distanceToSqr(target) > 200) {
          if(uipos.distToCenterSqr(target.position()) > 10) {
            nbt.putLong("target", (new BlockPos(target.position())).asLong());
            changed = true;
          }
        } else {
          if(uipos.distToCenterSqr(target.position()) > 2.78) {
            nbt.putLong("target", (new BlockPos(target.position())).asLong());
            changed = true;
          }
        }
      }
      if(changed && (target != null) && (target.getCommandSenderWorld()!=null)) {
        String target_dimension = dimensionName(target.getCommandSenderWorld());
        if(!nbt.getString("dimensionid").equals(target_dimension)) {
          nbt.putString("dimensionid", target_dimension);
          changed = true;
        }
      }
    }
    if(changed) {
      stack.addTagElement("trackerdata", nbt); // client sync only when needed
    }
  }

}
