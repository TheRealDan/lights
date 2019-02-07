package me.therealdan.lights.ui.views.live.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import me.therealdan.lights.LightsCore;
import me.therealdan.lights.controllers.Button;
import me.therealdan.lights.programmer.Sequence;
import me.therealdan.lights.renderer.Renderer;
import me.therealdan.lights.ui.views.Live;
import me.therealdan.lights.ui.views.Sequences;
import me.therealdan.lights.util.Util;

import java.text.DecimalFormat;

public class ButtonEditUI implements UI {

    private static ButtonEditUI buttonEditUI;
    private DecimalFormat decimalFormat = new DecimalFormat("#.##");
    private Button editing;
    private Section section;
    private Sequence scroll = null;

    public ButtonEditUI() {
        buttonEditUI = this;
    }

    @Override
    public boolean draw(Renderer renderer, float X, float Y, float WIDTH, float HEIGHT) {
        if (containsMouse()) Live.setSection(Live.Section.BUTTON_EDIT);
        boolean interacted = false;
        boolean shift = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);

        float x = getX();
        float y = getY();
        float uiWidth = getWidth();
        float width = 250;
        float cellHeight = 30;

        Util.box(renderer, x, y, uiWidth, cellHeight, LightsCore.DARK_BLUE, "Button Editor");
        drag(x, y, uiWidth, cellHeight);
        y -= cellHeight;

        Util.box(renderer, x, y, width, cellHeight, canEdit(Section.NAME) ? LightsCore.DARK_RED : LightsCore.medium(), "Name: " + getButton().getName());
        if (Util.containsMouse(x, y, width, cellHeight) && canInteract()) {
            interacted = true;
            if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && LightsCore.actionReady(-1)) {
                edit(Section.NAME);
            }
        }
        y -= cellHeight;

        Util.box(renderer, x, y, width, cellHeight, canEdit(Section.SEQUENCE) ? LightsCore.DARK_RED : LightsCore.medium(), "Sequences: " + getButton().sequences().size());
        if (Util.containsMouse(x, y, width, cellHeight) && canInteract()) {
            interacted = true;
            if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && LightsCore.actionReady(-1)) {
                edit(Section.SEQUENCE);
            }
        }
        y -= cellHeight;

        Util.box(renderer, x, y, width, cellHeight, LightsCore.medium(), "Red: " + getButton().getColor().r);
        Util.box(renderer, x, y, getButton().getColor().r * width, cellHeight, canEdit(Section.RED) ? LightsCore.RED : getButton().getColor());
        if (Util.containsMouse(x, y, width, cellHeight) && canInteract()) {
            interacted = true;
            if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && LightsCore.actionReady(-1)) {
                edit(Section.RED);
                float start = x + 20;
                getButton().getColor().r = Float.parseFloat(decimalFormat.format(Math.min(Math.max((Gdx.input.getX() - start) / ((x + width - 20) - start), 0), 1)));
            }
        }
        y -= cellHeight;

        Util.box(renderer, x, y, width, cellHeight, LightsCore.medium(), "Green: " + getButton().getColor().g);
        Util.box(renderer, x, y, getButton().getColor().g * width, cellHeight, canEdit(Section.GREEN) ? LightsCore.GREEN : getButton().getColor());
        if (Util.containsMouse(x, y, width, cellHeight) && canInteract()) {
            interacted = true;
            if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && LightsCore.actionReady(-1)) {
                edit(Section.GREEN);
                float start = x + 20;
                getButton().getColor().g = Float.parseFloat(decimalFormat.format(Math.min(Math.max((Gdx.input.getX() - start) / ((x + width - 20) - start), 0), 1)));
            }
        }
        y -= cellHeight;

        Util.box(renderer, x, y, width, cellHeight, LightsCore.medium(), "Blue: " + getButton().getColor().b);
        Util.box(renderer, x, y, getButton().getColor().b * width, cellHeight, canEdit(Section.BLUE) ? LightsCore.BLUE : getButton().getColor());
        if (Util.containsMouse(x, y, width, cellHeight) && canInteract()) {
            interacted = true;
            if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && LightsCore.actionReady(-1)) {
                edit(Section.BLUE);
                float start = x + 20;
                getButton().getColor().b = Float.parseFloat(decimalFormat.format(Math.min(Math.max((Gdx.input.getX() - start) / ((x + width - 20) - start), 0), 1)));
            }
        }
        y -= cellHeight;

        Util.box(renderer, x, y, width, cellHeight, LightsCore.medium(), "Move");
        if (Util.containsMouse(x, y, width, cellHeight) && canInteract()) {
            interacted = true;
            if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && LightsCore.actionReady(-1)) {
                ButtonsUI.move(getButton());
                return interacted;
            }
        }
        y -= cellHeight;

        Util.box(renderer, x, y, width, cellHeight, LightsCore.medium(), LightsCore.RED, "Delete");
        if (Util.containsMouse(x, y, width, cellHeight) && canInteract()) {
            interacted = true;
            if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && shift && LightsCore.actionReady(-1)) {
                ButtonsUI.remove(getButton());
                edit((Button) null);
                return interacted;
            }
        }
        y -= cellHeight;

        Util.box(renderer, x, y, width, cellHeight, LightsCore.medium(), "Close");
        if (Util.containsMouse(x, y, width, cellHeight) && canInteract()) {
            interacted = true;
            if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && LightsCore.actionReady(-1)) {
                edit((Button) null);
                return interacted;
            }
        }
        y -= cellHeight;

        setHeightBasedOnY(y);

        if (canEdit(Section.SEQUENCE)) {
            float sequencesWidth = renderer.getWidth("Selected Sequences") + 25;
            for (Sequence sequence : Sequences.sequences())
                sequencesWidth = Math.max(sequencesWidth, renderer.getWidth(sequence.getName()) + 25);

            x += width;
            y = getY();

            Util.box(renderer, x, y, sequencesWidth, cellHeight, LightsCore.DARK_BLUE, "All Sequences");
            drag(x, y, sequencesWidth, cellHeight);
            y -= cellHeight;

            int i = 0;
            boolean display = false;
            for (Sequence sequence : Sequences.sequences(true)) {
                if (sequence.equals(getScroll())) display = true;
                if (display) {
                    Util.box(renderer, x, y, sequencesWidth, cellHeight, getButton().contains(sequence) ? LightsCore.DARK_GREEN : LightsCore.medium(), sequence.getName());
                    if (Util.containsMouse(x, y, sequencesWidth, cellHeight) && canInteract()) {
                        interacted = true;
                        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && LightsCore.actionReady(500)) {
                            if (getButton().contains(sequence)) {
                                getButton().remove(sequence);
                            } else {
                                getButton().set(sequence, 1);
                            }
                        }
                    }
                    y -= cellHeight;
                    if (++i == 8) break;
                }
            }

            x += sequencesWidth;
            y = getY();

            Util.box(renderer, x, y, sequencesWidth, cellHeight, LightsCore.DARK_BLUE, "Selected Sequences");
            drag(x, y, sequencesWidth, cellHeight);
            y -= cellHeight;

            for (Sequence sequence : getButton().sequences(true)) {
                Util.box(renderer, x, y, sequencesWidth, cellHeight, LightsCore.medium(), sequence.getName() + ": " + getButton().getPriority(sequence));
                if (Util.containsMouse(x, y, sequencesWidth, cellHeight) && canInteract()) {
                    interacted = true;
                    if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && LightsCore.actionReady(500)) {
                        if (Util.containsMouse(x, y, sequencesWidth / 2, cellHeight)) {
                            getButton().set(sequence, getButton().getPriority(sequence) + 1);
                        } else {
                            getButton().set(sequence, Math.max(getButton().getPriority(sequence) - 1, 1));
                            if (shift) getButton().remove(sequence);
                        }
                    }
                }
                y -= cellHeight;
            }

            setWidth(width + sequencesWidth + sequencesWidth);
        } else {
            setWidth(width);
        }

        return interacted;
    }

    @Override
    public void scrolled(int amount) {
        if (canEdit(Section.SEQUENCE)) {
            if (amount > 0) {
                boolean next = false;
                int i = 0;
                for (Sequence sequence : Sequences.sequences(true)) {
                    if (i++ > Sequences.sequences().size() - 8) return;
                    if (next) {
                        setScroll(sequence);
                        return;
                    }
                    if (sequence.equals(getScroll())) next = true;
                }
            } else {
                Sequence previous = null;
                for (Sequence sequence : Sequences.sequences(true)) {
                    if (sequence.equals(getScroll()) && previous != null) {
                        setScroll(previous);
                        return;
                    }
                    previous = sequence;
                }
            }
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        boolean shift = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);

        if (canEdit(Section.NAME)) {
            switch (keycode) {
                case Input.Keys.BACKSPACE:
                    if (getButton().getName().length() > 0)
                        getButton().rename(getButton().getName().substring(0, getButton().getName().length() - 1));
                    if (shift) getButton().rename("");
                    break;
                case Input.Keys.SPACE:
                    getButton().rename(getButton().getName() + " ");
                    break;
                default:
                    String string = Input.Keys.toString(keycode);
                    if ("ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".contains(string)) {
                        if (!shift) string = string.toLowerCase();
                        getButton().rename(getButton().getName() + string);
                    }
            }
        }

        return true;
    }

    @Override
    public boolean isVisible() {
        return isEditing();
    }

    @Override
    public boolean ignoreVisibilityUI() {
        return true;
    }

    private void setScroll(Sequence sequence) {
        this.scroll = sequence;
    }

    private Sequence getScroll() {
        if (scroll == null) setScroll(Sequences.sequences(true).get(0));
        return scroll;
    }

    private void edit(Section section) {
        this.section = section;
    }

    private boolean canEdit(Section section) {
        return section.equals(this.section);
    }

    public enum Section {
        NAME, SEQUENCE, RED, GREEN, BLUE;
    }

    public static void edit(Button button) {
        buttonEditUI.editing = button;
    }

    public static boolean isEditing() {
        return getButton() != null;
    }

    public static Button getButton() {
        return buttonEditUI.editing;
    }
}