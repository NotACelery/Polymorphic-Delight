package dev.celerbi.polymorphicdelight.client;

import com.illusivesoulworks.polymorph.client.recipe.widget.PersistentRecipesWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;
import vectorwing.farmersdelight.client.gui.CookingPotScreen;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;

public final class CookingPotRecipesWidget extends PersistentRecipesWidget {
    private static final int BUTTON_RIGHT_GAP = 9;
    private static final int SLOT_SIZE = 18;

    private final Slot outputSlot;

    public CookingPotRecipesWidget(CookingPotScreen screen) {
        super(screen);
        outputSlot = screen.getMenu().slots.get(CookingPotMenu.INDEX_MEAL);
    }

    @Override
    public Slot getOutputSlot() {
        return outputSlot;
    }

    @Override
    public int getXPos() {
        return outputSlot.x + SLOT_SIZE + BUTTON_RIGHT_GAP;
    }

    @Override
    public int getYPos() {
        return outputSlot.y;
    }

    @Override
    public void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float renderPartialTicks
    ) {
        resetWidgetOffsets();
        super.render(guiGraphics, mouseX, mouseY, renderPartialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        resetWidgetOffsets();
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
