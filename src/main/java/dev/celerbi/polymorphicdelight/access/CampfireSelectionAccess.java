package dev.celerbi.polymorphicdelight.access;

import dev.celerbi.polymorphicdelight.selection.SlotRecipeSelections;

public interface CampfireSelectionAccess {
    SlotRecipeSelections polymorphicDelight$getSelections();
    int polymorphicDelight$getCookingProgress(int slot);
    int polymorphicDelight$getCookingTime(int slot);
    void polymorphicDelight$setCookingProgress(int slot, int value);
    void polymorphicDelight$setCookingTime(int slot, int value);
    default int polymorphicDelight$mapRecipeCookingTime(int time) {
        return time;
    }
}
