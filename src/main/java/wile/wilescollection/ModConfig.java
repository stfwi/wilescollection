/*
 * @file ModConfig.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Main class for module settings. Handles reading and
 * saving the config file.
 */
package wile.wilescollection;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;
import wile.wilescollection.blocks.*;
import wile.wilescollection.items.Trinkets;
import wile.wilescollection.libmc.blocks.VariantSlabBlock;
import wile.wilescollection.libmc.detail.Auxiliaries;
import wile.wilescollection.libmc.detail.OptionalRecipeCondition;

import javax.annotation.Nullable;
import java.util.*;


public class ModConfig
{
  //--------------------------------------------------------------------------------------------------------------------
  private static final Logger LOGGER = ModWilesCollection.logger();
  private static final String MODID = ModWilesCollection.MODID;
  public static final CommonConfig COMMON;
  public static final ForgeConfigSpec COMMON_CONFIG_SPEC;

  static {
    final Pair<CommonConfig, ForgeConfigSpec> common_ = (new ForgeConfigSpec.Builder()).configure(CommonConfig::new);
    COMMON_CONFIG_SPEC = common_.getRight();
    COMMON = common_.getLeft();
  }

  //--------------------------------------------------------------------------------------------------------------------

  public static class CommonConfig
  {
    // Optout
    public final ForgeConfigSpec.ConfigValue<String> pattern_excludes;
    public final ForgeConfigSpec.ConfigValue<String> pattern_includes;
    public final ForgeConfigSpec.BooleanValue without_chair_sitting;
    public final ForgeConfigSpec.BooleanValue without_ladder_speed_boost;
    public final ForgeConfigSpec.BooleanValue without_crafting_table_history;
    public final ForgeConfigSpec.BooleanValue without_direct_slab_pickup;
    // MISC
    public final ForgeConfigSpec.BooleanValue with_experimental;
    public final ForgeConfigSpec.BooleanValue with_config_logging;
    public final ForgeConfigSpec.BooleanValue with_debug_logging;
    public final ForgeConfigSpec.BooleanValue without_ters;
    // Tweaks
    public final ForgeConfigSpec.BooleanValue without_crafting_mouse_scrolling;

    CommonConfig(ForgeConfigSpec.Builder builder)
    {
      builder.comment("Settings affecting the logical server side.")
        .push("server");
      // --- OPTOUTS ------------------------------------------------------------
      {
        builder.comment("Opt-out settings")
          .push("optout");
        pattern_excludes = builder
          .translation(MODID + ".config.pattern_excludes")
          .comment("Opt-out any block by its registry name ('*' wildcard matching, "
            + "comma separated list, whitespaces ignored. You must match the whole name, "
            + "means maybe add '*' also at the begin and end. Example: '*wood*,*steel*' "
            + "excludes everything that has 'wood' or 'steel' in the registry name. "
            + "The matching result is also traced in the log file. ")
          .define("pattern_excludes", "");
        pattern_includes = builder
          .translation(MODID + ".config.pattern_includes")
          .comment("Prevent blocks from being opt'ed by registry name ('*' wildcard matching, "
            + "comma separated list, whitespaces ignored. Evaluated before all other opt-out checks. "
            + "You must match the whole name, means maybe add '*' also at the begin and end. Example: "
            + "'*wood*,*steel*' includes everything that has 'wood' or 'steel' in the registry name."
            + "The matching result is also traced in the log file.")
          .define("pattern_includes", "");
        without_chair_sitting = builder
          .translation(MODID + ".config.without_chair_sitting")
          .comment("Disable possibility to sit on stools and chairs.")
          .define("without_chair_sitting", false);
        without_ladder_speed_boost = builder
          .translation(MODID + ".config.without_ladder_speed_boost")
          .comment("Disable the speed boost of ladders in this mod.")
          .define("without_ladder_speed_boost", false);
        without_crafting_table_history = builder
          .translation(MODID + ".config.without_crafting_table_history")
          .comment("Disable history refabrication feature of the crafting table.")
          .define("without_crafting_table_history", false);
        builder.pop();
      }
      // --- MISC ---------------------------------------------------------------
      {
        builder.comment("Miscellaneous settings")
          .push("miscellaneous");
        with_experimental = builder
          .translation(MODID + ".config.with_experimental")
          .comment("Enables experimental features. Use at own risk.")
          .define("with_experimental", false);
        with_config_logging = builder
          .translation(MODID + ".config.with_debug_logging")
          .comment("Enable detailed logging of the config values and resulting calculations in each mod feature config.")
          .define("with_debug_logging", false);
        with_debug_logging = builder
          .translation(MODID + ".config.with_debug_logging")
          .comment("Enable debug log messages for trouble shooting. Don't activate if not really needed, this can spam the log file.")
          .define("with_debug_logging", false);
        without_ters = builder
                .translation(MODID + ".config.without_ters")
                .comment("Disable all TERs (tile entity renderers).")
                .define("without_ters", false);
        without_direct_slab_pickup = builder
                .translation(MODID + ".config.without_direct_slab_pickup")
                .comment("Disable directly picking up layers from slabs and slab " +
                        " slices by left clicking while looking up/down.")
                .define("without_direct_slab_pickup", true);
        without_crafting_mouse_scrolling = builder
                .translation(MODID + ".config.without_crafting_mouse_scrolling")
                .comment("Disables increasing/decreasing the crafting grid items by scrolling over the crafting result slot.")
                .define("without_crafting_mouse_scrolling", false);

        builder.pop();
      }
    }
  }

  //--------------------------------------------------------------------------------------------------------------------
  // Optout checks
  //--------------------------------------------------------------------------------------------------------------------

  public static final boolean isOptedOut(final @Nullable Block block)
  { return isOptedOut(block.asItem()); }

  public static final boolean isOptedOut(final @Nullable Item item)
  { return (item!=null) && optouts_.contains(item.getRegistryName().getPath()); }

  public static boolean withExperimental()
  { return with_experimental_features_; }

  public static boolean withoutRecipes()
  { return false; }

  public static boolean withDebug()
  { return with_debug_logs_; }

  public static boolean withDebugLogging()
  { return with_debug_logs_; }

  //--------------------------------------------------------------------------------------------------------------------
  // Cache
  //--------------------------------------------------------------------------------------------------------------------

  private static final CompoundTag server_config_ = new CompoundTag();
  private static HashSet<String> optouts_ = new HashSet<>();
  private static boolean with_experimental_features_ = false;
  private static boolean with_config_logging_ = false;
  private static boolean with_debug_logs_ = false;
  public static boolean without_direct_slab_pickup = false;
  public static boolean with_creative_mode_device_drops = false;

  public static final CompoundTag getServerConfig() // config that may be synchronized from server to client via net pkg.
  { return server_config_; }

  private static final void updateOptouts()
  {
    final ArrayList<String> includes = new ArrayList<>();
    final ArrayList<String> excludes = new ArrayList<>();
    {
      String inc = COMMON.pattern_includes.get().toLowerCase().replaceAll(MODID+":", "").replaceAll("[^*_,a-z0-9]", "");
      if(COMMON.pattern_includes.get() != inc) COMMON.pattern_includes.set(inc);
      String[] incl = inc.split(",");
      for(int i=0; i< incl.length; ++i) {
        incl[i] = incl[i].replaceAll("[*]", ".*?");
        if(!incl[i].isEmpty()) includes.add(incl[i]);
      }
    }
    {
      String exc = COMMON.pattern_excludes.get().toLowerCase().replaceAll(MODID+":", "").replaceAll("[^*_,a-z0-9]", "");
      String[] excl = exc.split(",");
      for(int i=0; i< excl.length; ++i) {
        excl[i] = excl[i].replaceAll("[*]", ".*?");
        if(!excl[i].isEmpty()) excludes.add(excl[i]);
      }
    }
    if(COMMON_CONFIG_SPEC.isLoaded()) {
      String inc = COMMON.pattern_includes.get().toLowerCase().replaceAll(MODID+":", "").replaceAll("[^*_,a-z0-9]", "");
      String[] incl = inc.split(",");
      for(int i=0; i< incl.length; ++i) {
        incl[i] = incl[i].replaceAll("[*]", ".*?");
        if(!incl[i].isEmpty()) includes.add(incl[i]);
      }
    }
    if(COMMON_CONFIG_SPEC.isLoaded()) {
      String exc = COMMON.pattern_excludes.get().toLowerCase().replaceAll(MODID+":", "").replaceAll("[^*_,a-z0-9]", "");
      String[] excl = exc.split(",");
      for(int i=0; i< excl.length; ++i) {
        excl[i] = excl[i].replaceAll("[*]", ".*?");
        if(!excl[i].isEmpty()) excludes.add(excl[i]);
      }
    }
    if(!excludes.isEmpty()) log("Config pattern excludes: '" + String.join(",", excludes) + "'");
    if(!includes.isEmpty()) log("Config pattern includes: '" + String.join(",", includes) + "'");
    {
      HashSet<String> optouts = new HashSet<>();
      ModContent.getRegisteredItems().stream().filter((item)->(item!=null)).forEach(
        e -> optouts.add(e.getRegistryName().getPath())
      );
      ModContent.getRegisteredBlocks().stream().filter((Block block) -> {
        if(block==null) return true;
        try {
          if(!with_experimental_features_) {
            if(block instanceof Auxiliaries.IExperimentalFeature) return true;
            if(ModContent.isExperimentalBlock(block)) return true;
          }
          // Force-include/exclude pattern matching
          final String rn = block.getRegistryName().getPath();
          try {
            for(String e : includes) {
              if(rn.matches(e)) {
                log("Optout force include: "+rn);
                return false;
              }
            }
            for(String e : excludes) {
              if(rn.matches(e)) {
                log("Optout force exclude: "+rn);
                return true;
              }
            }
          } catch(Throwable ex) {
            LOGGER.error("optout include pattern failed, disabling.");
            includes.clear();
            excludes.clear();
          }
        } catch(Exception ex) {
          LOGGER.error("Exception evaluating the optout config: '"+ex.getMessage()+"'");
        }
        return false;
      }).forEach(
        e -> optouts.add(e.getRegistryName().getPath())
      );
      optouts_ = optouts;
    }
    OptionalRecipeCondition.on_config(withExperimental(), withoutRecipes(), ModConfig::isOptedOut, ModConfig::isOptedOut);
  }

  public static final void apply()
  {
    with_config_logging_ = COMMON.with_config_logging.get();
    with_experimental_features_ = COMMON.with_experimental.get();
    with_debug_logs_ = COMMON.with_debug_logging.get();
    if(with_experimental_features_) LOGGER.info("Config: EXPERIMENTAL FEATURES ENABLED.");
    if(with_debug_logs_) LOGGER.info("Config: DEBUG LOGGING ENABLED, WARNING, THIS MAY SPAM THE LOG.");
    updateOptouts();
    if(!COMMON_CONFIG_SPEC.isLoaded()) return;
    without_direct_slab_pickup = COMMON.without_direct_slab_pickup.get();
    // -----------------------------------------------------------------------------------------------------------------
    Chair.on_config(COMMON.without_chair_sitting.get(), COMMON.without_chair_sitting.get(), 80, 10);
    ExtLadderBlock.on_config(COMMON.without_ladder_speed_boost.get());
    VariantSlabBlock.on_config(!COMMON.without_direct_slab_pickup.get());
    LabeledCrate.on_config(false);
    EdCraftingTable.on_config(COMMON.without_crafting_table_history.get(), false, COMMON.without_crafting_mouse_scrolling.get());
    FluidBarrel.on_config(12000, 1000);
    Trinkets.on_config();
  }

  public static final void log(String config_message)
  {
    if(!with_config_logging_) return;
    LOGGER.info(config_message);
  }

}
