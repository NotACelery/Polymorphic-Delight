package dev.celerbi.polymorphicdelight.mixin;

import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CampfireBlockEntity.class)
public interface CampfireCookingTimesAccess {
  @Accessor("cookingProgress")
  int[] polymorphicDelight$getCookingProgress();

  @Accessor("cookingTime")
  int[] polymorphicDelight$getCookingTime();
}
