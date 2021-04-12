package dev.therealdan.lights.panels.panels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import dev.therealdan.lights.controllers.Button;
import dev.therealdan.lights.main.Lights;
import dev.therealdan.lights.panels.Panel;
import dev.therealdan.lights.panels.menuicons.CloseIcon;
import dev.therealdan.lights.renderer.Renderer;
import dev.therealdan.lights.renderer.Task;
import dev.therealdan.lights.settings.Control;

import java.util.List;

import static dev.therealdan.lights.util.sorting.Sortable.Sort.POSITION;

public class ControlsPanel implements Panel {

    private Control.Category selectedCategory;
    private Control selectedControl = null;

    public ControlsPanel() {
        register(new CloseIcon());
    }

    @Override
    public boolean draw(Renderer renderer, float X, float Y, float WIDTH, float HEIGHT) {
        boolean interacted = false;

        float x = getX();
        float y = getY();
        float width = getWidth();
        float cellHeight = 30;

        renderer.box(x, y, width, cellHeight, renderer.getTheme().DARK_BLUE, setWidth(renderer, getFriendlyName()), Task.TextPosition.CENTER);
        drag(x, y, width, cellHeight);
        y -= cellHeight;

        renderer.box(x, y, width, cellHeight, renderer.getTheme().MEDIUM, setWidth(renderer, "Category: " + getSelectedCategory().formatString()), Task.TextPosition.LEFT_CENTER);
        if (Lights.mouse.contains(x, y, width, cellHeight) && canInteract()) {
            interacted = true;
            if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && Lights.mouse.leftReady(500)) {
                select(true);
            } else if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT) && Lights.mouse.rightReady(500)) {
                select(false);
            }
        }
        y -= cellHeight;

        if (getSelectedCategory().equals(Control.Category.BUTTONS)) {
            for (Button button : Button.buttons(POSITION)) {
                Control control = Control.byButton(button);
                setWidth(renderer, button.getName(), 2);
                renderer.box(x, y, width / 2, cellHeight, isSelected(control) ? renderer.getTheme().DARK_RED : renderer.getTheme().MEDIUM, button.getName());
                renderer.box(x + width / 2, y, width / 2, cellHeight, isSelected(control) ? renderer.getTheme().DARK_RED : renderer.getTheme().MEDIUM, control.formatKeycode());
                if (Lights.mouse.contains(x, y, width, cellHeight) && canInteract()) {
                    interacted = true;
                    if (Lights.mouse.leftClicked(500, getSelectedControl() != null && !control.equals(getSelectedControl()))) {
                        select(isSelected(control) ? null : control);
                    }
                }
                y -= cellHeight;
            }
        } else {
            for (Control control : getSelectedCategory().getControls()) {
                setWidth(renderer, control.getName(), 2);
                renderer.box(x, y, width / 2, cellHeight, isSelected(control) ? renderer.getTheme().DARK_RED : renderer.getTheme().MEDIUM, control.getName());
                renderer.box(x + width / 2, y, width / 2, cellHeight, isSelected(control) ? renderer.getTheme().DARK_RED : renderer.getTheme().MEDIUM, control.formatKeycode());
                if (Lights.mouse.contains(x, y, width, cellHeight) && canInteract()) {
                    interacted = true;
                    if (Lights.mouse.leftClicked(500, getSelectedControl() != null && !control.equals(getSelectedControl()))) {
                        select(isSelected(control) ? null : control);
                    }
                }
                y -= cellHeight;
            }
        }

        setHeightBasedOnY(y);
        return interacted;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (selectedControl == null) return true;

        getSelectedControl().setKeycode(keycode);
        return false;
    }

    public void select(boolean next) {
        int i = 0;
        List<Control.Category> categories = Control.categories();
        for (Control.Category category : categories) {
            if (getSelectedCategory().equals(category)) {
                i += next ? 1 : -1;
                if (i >= categories.size()) i = 0;
                if (i < 0) i = categories.size() - 1;
                select(categories.get(i));
                return;
            }
            i++;
        }
    }

    public void select(Control.Category category) {
        this.selectedCategory = category;
    }

    public void select(Control control) {
        this.selectedControl = control;
    }

    public void deselectControl() {
        this.selectedControl = null;
    }

    public boolean isSelected(Control control) {
        return control.equals(getSelectedControl());
    }

    public Control.Category getSelectedCategory() {
        if (selectedCategory == null) selectedCategory = Control.Category.GLOBAL;
        return selectedCategory;
    }

    public Control getSelectedControl() {
        return selectedControl;
    }
}