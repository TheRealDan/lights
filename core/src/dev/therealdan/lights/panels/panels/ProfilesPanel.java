package dev.therealdan.lights.panels.panels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import dev.therealdan.lights.fixtures.fixture.Profile;
import dev.therealdan.lights.main.Lights;
import dev.therealdan.lights.panels.Panel;
import dev.therealdan.lights.panels.menuicons.CloseIcon;
import dev.therealdan.lights.renderer.Renderer;

import java.util.ArrayList;

import static dev.therealdan.lights.util.sorting.Sortable.Sort.NAME;

public class ProfilesPanel implements Panel {

    private final int ROWS = 6;

    private Profile selectedProfile = null;

    private int profileScroll = 0;
    private boolean canEditName = false;

    public ProfilesPanel() {
        register(new CloseIcon());

        setMinimumWidth(200);
        setMinimumHeight(7 * 30);
    }

    @Override
    public boolean drawContent(Renderer renderer, float x, float y, float width, float height, boolean interacted) {
        setTitle("Profiles: " + Profile.count());

        if (hasSelectedProfile()) {
            width /= 2;
        }

        float cellHeight = 30;

        int i = 0;
        boolean display = false;
        int current = 0;
        for (Profile profile : Profile.profiles(NAME)) {
            if (current == getProfileScroll()) display = true;
            current++;
            if (display) {
                renderer.box(x, y, width, cellHeight, profile.equals(getSelectedProfile()) ? renderer.getTheme().DARK_GREEN : renderer.getTheme().MEDIUM, profile.getName());
                if (Lights.mouse.contains(x, y, width, cellHeight) && canInteract(interacted)) {
                    interacted = true;
                    if (Lights.mouse.leftClicked()) {
                        select(profile);
                    }
                }
                y -= cellHeight;
                if (++i == ROWS - 1) break;
            }
        }

        renderer.box(x, y, width, cellHeight, renderer.getTheme().MEDIUM, renderer.getTheme().GREEN, "Add New");
        if (Lights.mouse.contains(x, y, width, cellHeight) && canInteract(interacted)) {
            interacted = true;
            if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && Lights.mouse.leftReady(500)) {
                Profile newProfile = new Profile("New Profile", new ArrayList<>(), new ArrayList<>());
                Profile.add(newProfile);
                select(newProfile);
            }
        }
        y -= cellHeight;

        while (++i < ROWS) y -= cellHeight;

        // PROFILE OPTIONS

        if (!hasSelectedProfile()) return interacted;

        x += width;
        y = getY() - cellHeight;

        renderer.box(x, y, width, cellHeight, canEditName() ? renderer.getTheme().DARK_RED : renderer.getTheme().MEDIUM, "Name: " + getSelectedProfile().getName());
        if (Lights.mouse.contains(x, y, width, cellHeight) && canInteract(interacted)) {
            interacted = true;
            if (Lights.mouse.leftClicked(500)) toggleCanEditName();
        }
        y -= cellHeight;

        renderer.box(x, y, width, cellHeight, renderer.getTheme().MEDIUM, "Physical Channels: " + getSelectedProfile().getPhysicalChannels());
        if (Lights.mouse.contains(x, y, width, cellHeight) && canInteract(interacted)) interacted = true;
        y -= cellHeight;

        renderer.box(x, y, width, cellHeight, renderer.getTheme().MEDIUM, "Virtual Channels: " + getSelectedProfile().getVirtualChannels());
        if (Lights.mouse.contains(x, y, width, cellHeight) && canInteract(interacted)) interacted = true;
        y -= cellHeight;

        renderer.box(x, y, width, cellHeight, renderer.getTheme().MEDIUM, "Model: " + getSelectedProfile().countModels());
        if (Lights.mouse.contains(x, y, width, cellHeight) && canInteract(interacted)) interacted = true;
        y -= cellHeight;

        renderer.box(x, y, width, cellHeight, renderer.getTheme().MEDIUM, renderer.getTheme().YELLOW, "Advanced");
        if (Lights.mouse.contains(x, y, width, cellHeight) && canInteract(interacted)) {
            interacted = true;
            if (Lights.mouse.leftClicked()) Lights.openProfileEditor(getSelectedProfile());
        }
        y -= cellHeight;

        renderer.box(x, y, width, cellHeight, renderer.getTheme().MEDIUM, renderer.getTheme().RED, "Delete");
        if (Lights.mouse.contains(x, y, width, cellHeight) && canInteract(interacted)) {
            interacted = true;
            if (Lights.mouse.leftClicked()) {
                if (Lights.keyboard.isShift()) {
                    Profile.delete(getSelectedProfile());
                    select(null);
                    return interacted;
                }
            }
        }

        return interacted;
    }

    @Override
    public boolean keyDown(int keycode) {
        boolean shift = Lights.keyboard.isShift();

        if (canEditName()) {
            switch (keycode) {
                case Input.Keys.BACKSPACE:
                    if (getSelectedProfile().getName().length() > 0)
                        getSelectedProfile().rename(getSelectedProfile().getName().substring(0, getSelectedProfile().getName().length() - 1));
                    if (shift) getSelectedProfile().rename("");
                    break;
                case Input.Keys.SPACE:
                    getSelectedProfile().rename(getSelectedProfile().getName() + " ");
                    break;
                default:
                    String string = Input.Keys.toString(keycode);
                    if ("ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".contains(string)) {
                        if (!shift) string = string.toLowerCase();
                        getSelectedProfile().rename(getSelectedProfile().getName() + string);
                    }
            }
        }

        return true;
    }

    @Override
    public void scrolled(int amount) {
        profileScroll += amount;
        if (getProfileScroll() < 0) profileScroll = 0;
        if (getProfileScroll() > Math.max(0, Profile.count() - (ROWS - 1)))
            profileScroll = Math.max(0, Profile.count() - (ROWS - 1));
    }

    private void toggleCanEditName() {
        this.canEditName = !this.canEditName;
    }

    private boolean canEditName() {
        return canEditName;
    }

    private int getProfileScroll() {
        return profileScroll;
    }

    private void select(Profile profile) {
        this.selectedProfile = profile;
    }

    private boolean hasSelectedProfile() {
        return getSelectedProfile() != null;
    }

    private Profile getSelectedProfile() {
        return selectedProfile;
    }

    @Override
    public boolean isResizeable() {
        return true;
    }
}