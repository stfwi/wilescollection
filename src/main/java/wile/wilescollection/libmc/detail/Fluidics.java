/*
 * @file Fluidics.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * General fluid handling functionality.
 */
package wile.wilescollection.libmc.detail;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;


public class Fluidics
{
  public static class SingleTankFluidHandler implements IFluidHandler
  {
    private final IFluidTank tank_;
    public SingleTankFluidHandler(IFluidTank tank) { tank_ = tank; }
    @Override public int getTanks() { return 1; }
    @Override public FluidStack getFluidInTank(int tank) { return tank_.getFluid(); }
    @Override public int getTankCapacity(int tank) { return tank_.getCapacity(); }
    @Override public boolean isFluidValid(int tank, @Nonnull FluidStack stack) { return tank_.isFluidValid(stack); }
    @Override public int fill(FluidStack resource, FluidAction action)  { return tank_.fill(resource, action); }
    @Override public FluidStack drain(FluidStack resource, FluidAction action) { return tank_.drain(resource, action); }
    @Override public FluidStack drain(int maxDrain, FluidAction action) { return tank_.drain(maxDrain, action); }
  }

  private static class SingleTankOutputFluidHandler implements IFluidHandler
  {
    private final IFluidTank tank_;
    public SingleTankOutputFluidHandler(IFluidTank tank) { tank_ = tank; }
    @Override public int getTanks() { return 1; }
    @Override public FluidStack getFluidInTank(int tank) { return tank_.getFluid().copy(); }
    @Override public int getTankCapacity(int tank) { return tank_.getCapacity(); }
    @Override public boolean isFluidValid(int tank, @Nonnull FluidStack stack) { return true; }
    @Override public int fill(FluidStack resource, FluidAction action) { return 0; }
    @Override public FluidStack drain(FluidStack resource, FluidAction action) { return tank_.drain(resource, action); }
    @Override public FluidStack drain(int maxDrain, FluidAction action) { return tank_.drain(maxDrain, action); }
  }

  public static class Tank implements IFluidTank
  {
    private Predicate<FluidStack> validator_ = ((e)->true);
    private BiConsumer<Tank,Integer> interaction_notifier_ = ((tank,diff)->{});
    private FluidStack fluid_ = FluidStack.EMPTY;
    private int capacity_;
    private int fill_rate_;
    private int drain_rate_;

    public Tank(int capacity)
    { this(capacity, capacity, capacity); }

    public Tank(int capacity, int fill_rate, int drain_rate)
    { this(capacity, fill_rate, drain_rate, e->true); }

    public Tank(int capacity, int fill_rate, int drain_rate, Predicate<FluidStack> validator)
    {
      capacity_ = capacity;
      setMaxFillRate(fill_rate);
      setMaxDrainRate(drain_rate);
      setValidator(validator);
    }

    public Tank load(CompoundTag nbt)
    {
      if(nbt.contains("tank", Constants.NBT.TAG_COMPOUND)) {
        setFluid(FluidStack.loadFluidStackFromNBT(nbt.getCompound("tank")));
      } else {
        clear();
      }
      return this;
    }

    public CompoundTag save(CompoundTag nbt)
    { if(!isEmpty()) { nbt.put("tank", fluid_.writeToNBT(new CompoundTag())); } return nbt; }

    public void reset()
    { clear(); }

    public Tank clear()
    { setFluid(null); return this; }

    public int getCapacity()
    { return capacity_; }

    public Tank setCapacity(int capacity)
    { capacity_ = capacity; return this; }

    public int getMaxDrainRate()
    { return drain_rate_; }

    public Tank setMaxDrainRate(int rate)
    { drain_rate_ = Mth.clamp(rate, 0, capacity_); return this; }

    public int getMaxFillRate()
    { return fill_rate_; }

    public Tank setMaxFillRate(int rate)
    { fill_rate_ = Mth.clamp(rate, 0, capacity_); return this; }

    public Tank setValidator(Predicate<FluidStack> validator)
    { validator_ = (validator!=null) ? validator : ((e)->true); return this; }

    public Tank setInteractionNotifier(BiConsumer<Tank,Integer> notifier)
    { interaction_notifier_ = (notifier!=null) ? notifier : ((tank,diff)->{}); return this; }

    public LazyOptional<IFluidHandler> createFluidHandler()
    { return LazyOptional.of(() -> new Fluidics.SingleTankFluidHandler(this)); }

    public LazyOptional<IFluidHandler> createOutputFluidHandler()
    { return LazyOptional.of(() -> new Fluidics.SingleTankOutputFluidHandler(this)); }

    // IFluidTank ------------------------------------------------------------------------------------

    @Nonnull
    public FluidStack getFluid()
    { return fluid_; }

    public void setFluid(@Nullable FluidStack stack)
    { fluid_ = (stack==null) ? FluidStack.EMPTY : stack; }

    public int getFluidAmount()
    { return fluid_.getAmount(); }

    public boolean isEmpty()
    { return fluid_.isEmpty(); }

    public boolean isFull()
    { return getFluidAmount() >= getCapacity(); }

    public boolean isFluidValid(FluidStack stack)
    { return validator_.test(stack); }

    public boolean isFluidEqual(FluidStack stack)
    { return (stack==null) ? (fluid_.isEmpty()) : fluid_.isFluidEqual(stack); }

    @Override
    public int fill(FluidStack fs, FluidAction action)
    {
      if((fs==null) || fs.isEmpty() || (!isFluidValid(fs))) {
        return 0;
      } else if(action.simulate()) {
        if(fluid_.isEmpty()) return Math.min(capacity_, fs.getAmount());
        if(!fluid_.isFluidEqual(fs)) return 0;
        return Math.min(capacity_-fluid_.getAmount(), fs.getAmount());
      } else if(fluid_.isEmpty()) {
        fluid_ = new FluidStack(fs, Math.min(capacity_, fs.getAmount()));
        return fluid_.getAmount();
      } else if(!fluid_.isFluidEqual(fs)) {
        return 0;
      } else {
        int amount = capacity_ - fluid_.getAmount();
        if(fs.getAmount() < amount) {
          fluid_.grow(fs.getAmount());
          amount = fs.getAmount();
        } else {
          fluid_.setAmount(capacity_);
        }
        if(amount != 0) interaction_notifier_.accept(this, amount);
        return amount;
      }
    }

    @Nonnull
    public FluidStack drain(int maxDrain)
    { return drain(maxDrain, FluidAction.EXECUTE); }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack fs, FluidAction action)
    { return ((fs.isEmpty()) || (!fs.isFluidEqual(fluid_))) ? FluidStack.EMPTY : drain(fs.getAmount(), action); }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action)
    {
      final int amount = Math.min(fluid_.getAmount(), maxDrain);
      final FluidStack stack = new FluidStack(fluid_, amount);
      if((amount > 0) && action.execute()) {
        fluid_.shrink(amount);
        if(fluid_.isEmpty()) fluid_ = FluidStack.EMPTY;
        if(amount != 0) interaction_notifier_.accept(this, -amount);
      }
      return stack;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------

  public static @Nullable IFluidHandler handler(Level world, BlockPos pos, @Nullable Direction side)
  { return FluidUtil.getFluidHandler(world, pos, side).orElse(null); }

  /**
   * Fills or drains items with fluid handlers from or into tile blocks with fluid handlers.
   */
  public static boolean manualFluidHandlerInteraction(Level world, BlockPos pos, @Nullable Direction side, Player player, InteractionHand hand)
  { return manualTrackedFluidHandlerInteraction(world, pos, side, player, hand) != null; }

  public static boolean manualFluidHandlerInteraction(Player player, InteractionHand hand, IFluidHandler handler)
  { return FluidUtil.interactWithFluidHandler(player, hand, handler); }

  /**
   * Fills or drains items with fluid handlers from or into tile blocks with fluid handlers.
   * Returns the fluid and (possibly negative) amount that transferred from the item into the block.
   */
  public static @Nullable Tuple<Fluid, Integer> manualTrackedFluidHandlerInteraction(Level world, BlockPos pos, @Nullable Direction side, Player player, InteractionHand hand)
  {
    if(world.isClientSide()) return null;
    final ItemStack held = player.getItemInHand(hand);
    if(held.isEmpty()) return null;
    final IFluidHandler fh = handler(world, pos, side);
    if(fh==null) return null;
    final IItemHandler ih = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
    if(ih==null) return null;
    FluidActionResult far = FluidUtil.tryFillContainerAndStow(held, fh, ih, Integer.MAX_VALUE, player, true);
    if(!far.isSuccess()) far = FluidUtil.tryEmptyContainerAndStow(held, fh, ih, Integer.MAX_VALUE, player, true);
    if(!far.isSuccess()) return null;
    final ItemStack rstack = far.getResult().copy();
    player.setItemInHand(hand, far.getResult());
    final IFluidHandler fh_before = FluidUtil.getFluidHandler(held).orElse(null);
    final IFluidHandler fh_after = FluidUtil.getFluidHandler(rstack).orElse(null);
    if((fh_before==null) || (fh_after==null) || (fh_after.getTanks()!=fh_before.getTanks())) return null; // should not be, but y'never know.
    for(int i=0; i<fh_before.getTanks(); ++i) {
      final int vol_before = fh_before.getFluidInTank(i).getAmount();
      final int vol_after = fh_after.getFluidInTank(i).getAmount();
      if(vol_before != vol_after) {
        return new Tuple<>(
          (vol_before>0) ? (fh_before.getFluidInTank(i).getFluid()) : (fh_after.getFluidInTank(i).getFluid()),
          (vol_before - vol_after)
        );
      }
    }
    return null;
  }

  public static boolean manualFluidHandlerInteraction(Player player, InteractionHand hand, Level world, BlockPos pos, @Nullable Direction side)
  { return FluidUtil.interactWithFluidHandler(player, hand, world, pos, side); }

  public static int fill(Level world, BlockPos pos, Direction side, FluidStack fs, FluidAction action)
  {
    IFluidHandler fh = FluidUtil.getFluidHandler(world, pos, side).orElse(null);
    return (fh==null) ? (0) : (fh.fill(fs, action));
  }

  public static int fill(Level world, BlockPos pos, Direction side, FluidStack fs)
  { return fill(world, pos, side, fs, FluidAction.EXECUTE); }

  /**
   * Fluid tank access when itemized.
   */
  public static class FluidContainerItemCapabilityWrapper implements IFluidHandlerItem, ICapabilityProvider
  {
    private final LazyOptional<IFluidHandlerItem> handler_ = LazyOptional.of(()->this);
    private final Function<ItemStack, CompoundTag> nbt_getter_;
    private final BiConsumer<ItemStack, CompoundTag> nbt_setter_;
    private final Predicate<FluidStack> validator_;
    private final ItemStack container_;
    private final int capacity_;
    private final int transfer_rate_;

    public FluidContainerItemCapabilityWrapper(ItemStack container, int capacity, int transfer_rate,
                                               Function<ItemStack, CompoundTag> nbt_getter,
                                               BiConsumer<ItemStack, CompoundTag> nbt_setter,
                                               Predicate<FluidStack> validator)
    {
      container_ = container;
      capacity_ = capacity;
      transfer_rate_ = transfer_rate;
      nbt_getter_ = nbt_getter;
      nbt_setter_ = nbt_setter;
      validator_ = (validator!=null) ? validator : (e->true);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side)
    { return (capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY) ? handler_.cast() : LazyOptional.empty(); }

    protected FluidStack readnbt()
    {
      final CompoundTag nbt = nbt_getter_.apply(container_);
      return ((nbt==null) || (nbt.isEmpty())) ? FluidStack.EMPTY : FluidStack.loadFluidStackFromNBT(nbt);
    }

    protected void writenbt(FluidStack fs)
    {
      CompoundTag nbt = new CompoundTag();
      if(!fs.isEmpty()) fs.writeToNBT(nbt);
      nbt_setter_.accept(container_, nbt);
    }

    @Override
    public ItemStack getContainer()
    { return container_; }

    @Override
    public int getTanks()
    { return 1; }

    @Override
    public FluidStack getFluidInTank(int tank)
    { return readnbt(); }

    @Override
    public int getTankCapacity(int tank)
    { return capacity_; }

    @Override
    public boolean isFluidValid(int tank, FluidStack fs)
    { return isFluidValid(fs); }

    public boolean isFluidValid(FluidStack fs)
    { return validator_.test(fs); }

    @Override
    public int fill(FluidStack fs, FluidAction action)
    {
      if((fs.isEmpty()) || (!isFluidValid(fs) || (container_.getCount()!=1))) return 0;
      FluidStack tank = readnbt();
      final int amount = Math.min(Math.min(fs.getAmount(),transfer_rate_), capacity_-tank.getAmount());
      if(amount <= 0) return 0;
      if(tank.isEmpty()) {
        if(action.execute()) {
          tank = new FluidStack(fs.getFluid(), amount, fs.getTag());
          writenbt(tank);
        }
      } else {
        if(!tank.isFluidEqual(fs)) {
          return 0;
        } else if(action.execute()) {
          tank.grow(amount);
          writenbt(tank);
        }
      }
      return amount;
    }

    @Override
    public FluidStack drain(FluidStack fs, FluidAction action)
    {
      if((fs.isEmpty()) || (container_.getCount()!=1)) return FluidStack.EMPTY;
      final FluidStack tank = readnbt();
      if((!tank.isEmpty()) && (!tank.isFluidEqual(fs))) return FluidStack.EMPTY;
      return drain(fs.getAmount(), action);
    }

    @Override
    public FluidStack drain(int max, FluidAction action)
    {
      if((max<=0) || (container_.getCount()!=1)) return FluidStack.EMPTY;
      FluidStack tank = readnbt();
      if(tank.isEmpty()) return FluidStack.EMPTY;
      final int amount = Math.min(Math.min(tank.getAmount(), max), transfer_rate_);
      final FluidStack fs = tank.copy();
      fs.setAmount(amount);
      if(action.execute()) { tank.shrink(amount); writenbt(tank); }
      return fs;
    }
  }

}
