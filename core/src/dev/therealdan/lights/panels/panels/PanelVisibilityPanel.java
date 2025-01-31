package dev.therealdan.lights.panels.panels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import dev.therealdan.lights.main.Mouse;
import dev.therealdan.lights.panels.Panel;
import dev.therealdan.lights.panels.menuicons.CloseIcon;
import dev.therealdan.lights.renderer.Renderer;
import dev.therealdan.lights.renderer.Task;
import dev.therealdan.lights.ui.PanelHandler;

import java.util.List;

public class PanelVisibilityPanel implements Panel {

    public PanelVisibilityPanel() {
        register(new CloseIcon());
    }

    @Override
    public boolean draw(Mouse mouse, Renderer renderer, float X, float Y, float WIDTH, float HEIGHT) {
        boolean interacted = false;

        float cellHeight = 30;

        float x = getX();
        float y = getY();
        float width = getWidth();

        renderer.box(x, y, width, cellHeight, renderer.getTheme().DARK_BLUE, setWidth(renderer, getFriendlyName()), Task.TextPosition.CENTER);
        drag(mouse, x, y, width, cellHeight);
        y -= cellHeight;

        List<Panel> panels = PanelHandler.UIs();
        while (panels.size() > 0) {
            Panel panel = null;
            for (Panel each : panels) {
                if (panel == null || panel.getName().compareTo(each.getName()) > 0) {
                    panel = each;
                }
            }

            panels.remove(panel);
            if (panel.ignoreVisibilityUI()) continue;
            if (mouse.within(x, y, width, cellHeight) && canInteract()) {
                interacted = true;
                if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && mouse.leftReady(200))
                    panel.toggleVisibility();
            }
            renderer.box(x, y, width, cellHeight, panel.isVisible() ? renderer.getTheme().DARK_GREEN : renderer.getTheme().MEDIUM, setWidth(renderer, panel.getFriendlyName()));
            y -= cellHeight;
        }

        setHeightBasedOnY(y);
        return interacted;
    }

    @Override
    public boolean isVisible() {
        return Gdx.input.isKeyPressed(Input.Keys.TAB);
    }

    @Override
    public boolean ignoreVisibilityUI() {
        return true;
    }
}