/*
 * @file Effects.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Effect handling.
 */
package wile.wilescollection.libmc;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;


public class Effects
{
  public static boolean assign(LivingEntity entity, MobEffect effect, int duration, int amplifier, boolean ambient, boolean visible, boolean showIcon)
  { return entity.addEffect(new MobEffectInstance(effect, duration, amplifier, ambient, visible, showIcon)); }

  public static boolean assign(LivingEntity entity, MobEffect effect, int duration)
  { return entity.addEffect(new MobEffectInstance(effect, duration, 0, false, false, false)); }
}
