package me.therealdan.lights.ui.views.live.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import me.therealdan.lights.LightsCore;
import me.therealdan.lights.fixtures.Group;
import me.therealdan.lights.programmer.Programmer;
import me.therealdan.lights.renderer.Renderer;
import me.therealdan.lights.renderer.Task;
import me.therealdan.lights.ui.views.Live;
import me.therealdan.lights.util.Util;

public class GroupsUI implements UI {

    public static float WIDTH = 800;
    public static int GROUPS_PER_ROW = 10;

    public GroupsUI() {
    }

    @Override
    public boolean draw(Renderer renderer, float X, float Y, float WIDTH, float HEIGHT) {
        if (containsMouse()) Live.setSection(Live.Section.GROUPS);
        boolean interacted = false;

        setWidth(GroupsUI.WIDTH);

        float cellHeight = 30;
        float cellSize = GroupsUI.WIDTH / GroupsUI.GROUPS_PER_ROW;

        float x = getX();
        float y = getY();
        float width = getWidth();

        Util.box(renderer, x, y, width, cellHeight, LightsCore.DARK_BLUE, setWidth(renderer, "Groups"), Task.TextPosition.CENTER);
        drag(x, y, width, cellHeight);
        y -= cellHeight;

        for (Group group : PatchUI.groups()) {
            Util.box(renderer, x, y, cellSize, cellSize, Programmer.isSelected(group) ? LightsCore.DARK_RED : LightsCore.medium(), setWidth(renderer, group.getName()), Task.TextPosition.CENTER);
            if (Util.containsMouse(x, y, cellSize, cellSize) && canInteract()) {
                interacted = true;
                if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && LightsCore.actionReady(500)) {
                    if (Programmer.isSelected(group)) {
                        Programmer.deselect(group);
                    } else {
                        Programmer.select(group);
                    }
                }
            }
            x += cellSize;

            if (x + cellSize > getX() + getWidth()) {
                x = getX();
                y -= cellSize;
            }
        }
        y -= cellSize;

        setHeightBasedOnY(y);
        return interacted;
    }
}