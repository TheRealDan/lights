package me.therealdan.lights.ui.views.live.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import me.therealdan.lights.LightsCore;
import me.therealdan.lights.dmx.Output;
import me.therealdan.lights.renderer.Renderer;
import me.therealdan.lights.ui.views.Live;
import me.therealdan.lights.util.Util;

public class DMXInterfaceUI implements UI {

    public DMXInterfaceUI() {
    }

    @Override
    public boolean draw(Renderer renderer, float X, float Y, float WIDTH, float HEIGHT) {
        if (containsMouse()) Live.setSection(Live.Section.DMX_INTERFACE);
        boolean interacted = false;

        float cellHeight = 30;

        float x = getX();
        float y = getY();
        float width = getWidth();

        Util.box(renderer, x, y, width, cellHeight, LightsCore.DARK_BLUE, setWidth(renderer, "DMX Interface"));
        drag(x, y, width, cellHeight);
        y -= cellHeight;

        for (String port : Output.openPorts()) {
            if (Util.containsMouse(x, y, width, cellHeight) && canInteract()) {
                if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && LightsCore.actionReady(500)) {
                    if (Output.getActivePort().equals(port)) {
                        Output.disconnect();
                    } else {
                        Output.setActivePort(port);
                    }
                }
            }
            Util.box(renderer, x, y, width, cellHeight, Output.getActivePort().equals(port) ? LightsCore.DARK_RED : LightsCore.medium(), setWidth(renderer, port));
            y -= cellHeight;
        }

        setHeightBasedOnY(y);
        return interacted;
    }
}