package dev.celerbi.polymorphicdelight.client;

import com.illusivesoulworks.polymorph.api.client.base.PersistentRecipesWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;
import vectorwing.farmersdelight.client.gui.CookingPotScreen;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;

public final class CookingPotRecipesWidget extends PersistentRecipesWidget {
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
        return outputSlot.x + 27;
    }

    @Override
    public int getYPos() {
        return outputSlot.y;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float tick) {
        resetWidgetOffsets();
        super.render(graphics, mouseX, mouseY, tick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        resetWidgetOffsets();
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
