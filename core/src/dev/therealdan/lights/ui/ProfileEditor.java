package dev.therealdan.lights.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;
import dev.therealdan.lights.fixtures.fixture.Model;
import dev.therealdan.lights.fixtures.fixture.Profile;
import dev.therealdan.lights.fixtures.fixture.profile.Channel;
import dev.therealdan.lights.fixtures.fixture.profile.ModelDesign;
import dev.therealdan.lights.fixtures.fixture.profile.MutableProfile;
import dev.therealdan.lights.main.Mouse;
import dev.therealdan.lights.renderer.Renderer;
import dev.therealdan.lights.renderer.Task;
import dev.therealdan.lights.store.ProfilesStore;
import dev.therealdan.lights.util.Util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ProfileEditor implements Visual {

    // TODO - Finish Profile editor; Channels, Models and how they interact

    private ProfilesStore _profilesStore;

    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    private DisplayHandler _displayHandler;

    private PerspectiveCamera camera;
    private Environment environment;
    private ModelBatch modelBatch;
    private List<Model> models = new ArrayList<>();

    private Profile profile;
    private MutableProfile mutableProfile;

    private Section section;
    private ModelSetting modelSetting;

    private Channel channel;
    private ModelDesign modelDesign;

    private boolean mouseInPreviewArea = false;
    private float degreesPerPixel = 0.5f;
    private Vector3 tmp = new Vector3();

    public ProfileEditor(ProfilesStore profilesStore, DisplayHandler displayHandler) {
        _profilesStore = profilesStore;
        _displayHandler = displayHandler;

        camera = new PerspectiveCamera(75, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0f, 0f, 10f);
        camera.lookAt(0f, 0f, 0f);
        camera.near = 0.1f;
        camera.far = 1000f;
        camera.update();

        modelBatch = new ModelBatch();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 1f, 1f, 1f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
        environment.add(new DirectionalLight().set(0.6f, 0.6f, 0.6f, 1f, 0.8f, 0.2f));
    }

    @Override
    public boolean draw(Mouse mouse, Renderer renderer) {
        float X = 0;
        float Y = Gdx.graphics.getHeight();
        float WIDTH = Gdx.graphics.getWidth();
        float cellHeight = 30;

        float x = X;
        float y = Y;
        float width = WIDTH;

        renderer.box(x, y, width, cellHeight, renderer.getTheme().DARK_BLUE, "Profile Editor", Task.TextPosition.CENTER);
        y -= cellHeight;

        if (getMutableProfile() == null) return true;

        // PROFILE OPTIONS
        width = WIDTH / 2 / 3;
        renderer.box(x, y, width, cellHeight, renderer.getTheme().DARK_BLUE, "Profile Options", Task.TextPosition.CENTER);
        y -= cellHeight;

        renderer.box(x, y, width, cellHeight, isEditing(Section.NAME) ? renderer.getTheme().DARK_RED : renderer.getTheme().MEDIUM, "Name: " + getMutableProfile().getName());
        if (mouse.within(x, y, width, cellHeight) && mouse.leftClicked()) edit(Section.NAME);
        y -= cellHeight;

        renderer.box(x, y, width, cellHeight, isEditing(Section.PHYSICAL_CHANNELS) ? renderer.getTheme().DARK_RED : renderer.getTheme().MEDIUM, "Physical Channels: " + getMutableProfile().getPhysicalChannels());
        if (mouse.within(x, y, width, cellHeight) && mouse.leftClicked()) edit(Section.PHYSICAL_CHANNELS);
        y -= cellHeight;

        renderer.box(x, y, width, cellHeight, isEditing(Section.VIRTUAL_CHANNELS) ? renderer.getTheme().DARK_RED : renderer.getTheme().MEDIUM, "Virtual Channels: " + getMutableProfile().getVirtualChannels());
        if (mouse.within(x, y, width, cellHeight) && mouse.leftClicked()) edit(Section.VIRTUAL_CHANNELS);
        y -= cellHeight;

        renderer.box(x, y, width, cellHeight, isEditing(Section.MODEL) ? renderer.getTheme().DARK_RED : renderer.getTheme().MEDIUM, "Model: " + getMutableProfile().countModels());
        if (mouse.within(x, y, width, cellHeight) && mouse.leftClicked()) edit(Section.MODEL);
        y -= cellHeight;

        if (profile.isUsingHardcodedModelDesignsBasedOnProfileName()) {
            renderer.box(x, y, width, cellHeight, renderer.getTheme().MEDIUM, "Warning: Hardcoded models based on profile name");
            y -= cellHeight;
        }

        renderer.box(x, y, width, cellHeight, renderer.getTheme().MEDIUM, renderer.getTheme().YELLOW, "Save Changes");
        if (mouse.within(x, y, width, cellHeight) && mouse.leftClicked()) {
            profile.update(getMutableProfile());
            return false;
        }
        y -= cellHeight;

        renderer.box(x, y, width, cellHeight, renderer.getTheme().MEDIUM, renderer.getTheme().YELLOW, "Clear Changes");
        if (mouse.within(x, y, width, cellHeight) && mouse.leftClicked()) {
            edit(profile);
            return false;
        }
        y -= cellHeight;

        renderer.box(x, y, width, cellHeight, renderer.getTheme().MEDIUM, renderer.getTheme().YELLOW, "Close Editor");
        if (mouse.within(x, y, width, cellHeight) && mouse.leftClicked()) {
            _displayHandler.setFocus(DisplayHandler.Focus.MAIN_VIEW);
            return false;
        }
        y -= cellHeight;

        renderer.box(x, y, width, cellHeight, renderer.getTheme().MEDIUM, renderer.getTheme().RED, "Delete Profile");
        if (mouse.within(x, y, width, cellHeight) && Util.isShiftHeld() && mouse.leftClicked()) {
            _profilesStore.delete(getMutableProfile());
            edit((Profile) null);
            _displayHandler.setFocus(DisplayHandler.Focus.MAIN_VIEW);
            return false;
        }
        y -= cellHeight;

        x += width;
        y = Y - cellHeight;

        // PHYSICAL CHANNELS
        if (isEditing(Section.PHYSICAL_CHANNELS)) {
            renderer.box(x, y, width, cellHeight, renderer.getTheme().DARK_BLUE, "Physical Channels: " + getMutableProfile().getPhysicalChannels(), Task.TextPosition.CENTER);
            y -= cellHeight;
            for (int offset = 0; offset < getMutableProfile().getPhysicalChannels(); offset++) {
                StringBuilder channels = new StringBuilder();
                for (Channel channel : getMutableProfile().channels()) {
                    for (int addressOffset : channel.addressOffsets()) {
                        if (addressOffset == offset) {
                            channels.append(", ").append(channel.getType().getName());
                        }
                    }
                }
                renderer.box(x, y, width, cellHeight, renderer.getTheme().MEDIUM, offset + " - " + channels.toString().replaceFirst(", ", ""));
                y -= cellHeight;
            }
            x += width;
            y = Y - cellHeight;
        }

        // VIRTUAL CHANNELS
        if (isEditing(Section.VIRTUAL_CHANNELS)) {
            renderer.box(x, y, width, cellHeight, renderer.getTheme().DARK_BLUE, "Virtual Channels: " + getMutableProfile().getVirtualChannels(), Task.TextPosition.CENTER);
            y -= cellHeight;
            for (Channel channel : getMutableProfile().channels()) {
                renderer.box(x, y, width, cellHeight, channel.equals(getSelectedChannel()) ? renderer.getTheme().DARK_RED : renderer.getTheme().MEDIUM, channel.getType().getName() + " - " + channel.addressOffsetsAsString());
                if (mouse.within(x, y, width, cellHeight) && mouse.leftClicked()) {
                    select(channel);
                }
                y -= cellHeight;
            }

            if (hasChannelSelected()) {
                renderer.box(x, y, width, cellHeight, renderer.getTheme().MEDIUM, renderer.getTheme().YELLOW, "Change Type");
                if (mouse.within(x, y, width, cellHeight) && mouse.leftClicked(500)) {
                    getMutableProfile().changeType(getSelectedChannel());
                }
                y -= cellHeight;
            }

            renderer.box(x, y, width, cellHeight, renderer.getTheme().MEDIUM, renderer.getTheme().GREEN, "Add Channel");
            if (mouse.within(x, y, width, cellHeight) && mouse.leftClicked()) {
                getMutableProfile().addChannel(Channel.DEFAULT_TYPE);
            }
            y -= cellHeight;

            if (hasChannelSelected()) {
                renderer.box(x, y, width, cellHeight, renderer.getTheme().MEDIUM, renderer.getTheme().RED, "Delete Channel");
                if (mouse.within(x, y, width, cellHeight) && Util.isShiftHeld() && mouse.leftClicked()) {
                    getMutableProfile().removeChannel(getSelectedChannel());
                    select((Channel) null);
                }
                y -= cellHeight;
            }

            x += width;
            y = Y - cellHeight;
        }

        // MODELS
        if (isEditing(Section.MODEL)) {
            renderer.box(x, y, width, cellHeight, renderer.getTheme().DARK_BLUE, "Models: " + getMutableProfile().countModels(), Task.TextPosition.CENTER);
            y -= cellHeight;
            for (ModelDesign modelDesign : getMutableProfile().getModelDesigns()) {
                renderer.box(x, y, width, cellHeight, modelDesign.equals(getSelectedModelDesign()) ? renderer.getTheme().DARK_RED : renderer.getTheme().MEDIUM, modelDesign.toString());
                if (mouse.within(x, y, width, cellHeight)) {
                    if (mouse.leftClicked()) {
                        select(modelDesign);
                    }
                }
                y -= cellHeight;
            }

            renderer.box(x, y, width, cellHeight, renderer.getTheme().MEDIUM, renderer.getTheme().YELLOW, "Update Models");
            if (mouse.within(x, y, width, cellHeight) && mouse.leftClicked(500)) {
                rebuildModelInstances();
            }
            y -= cellHeight;

            renderer.box(x, y, width, cellHeight, renderer.getTheme().MEDIUM, renderer.getTheme().GREEN, "Add Model");
            if (mouse.within(x, y, width, cellHeight)) {
                if (mouse.leftClicked(500)) {
                    getMutableProfile().addModelDesign(new ModelDesign(1));
                }
            }
            y -= cellHeight;

            if (hasModelDesignSelected()) {
                renderer.box(x, y, width, cellHeight, renderer.getTheme().MEDIUM, renderer.getTheme().RED, "Delete Model");
                if (mouse.within(x, y, width, cellHeight) && Util.isShiftHeld() && mouse.leftClicked(500)) {
                    getMutableProfile().removeModelDesign(getSelectedModelDesign());
                    select((ModelDesign) null);
                }
            }
            y -= cellHeight;

            x += width;
            y = Y - cellHeight;
        }

        // MODEL SETTINGS
        if (hasModelDesignSelected()) {
            renderer.box(x, y, width, cellHeight, renderer.getTheme().DARK_BLUE, "Model Settings", Task.TextPosition.CENTER);
            y -= cellHeight;

            renderer.box(x, y, width, cellHeight, isEditing(ModelSetting.WIDTH) ? renderer.getTheme().DARK_RED : renderer.getTheme().MEDIUM, "Width: " + getSelectedModelDesign().getDimensions().x);
            if (mouse.within(x, y, width, cellHeight) && mouse.leftClicked()) edit(ModelSetting.WIDTH);
            y -= cellHeight;

            renderer.box(x, y, width, cellHeight, isEditing(ModelSetting.HEIGHT) ? renderer.getTheme().DARK_RED : renderer.getTheme().MEDIUM, "Height: " + getSelectedModelDesign().getDimensions().y);
            if (mouse.within(x, y, width, cellHeight) && mouse.leftClicked()) edit(ModelSetting.HEIGHT);
            y -= cellHeight;

            renderer.box(x, y, width, cellHeight, isEditing(ModelSetting.DEPTH) ? renderer.getTheme().DARK_RED : renderer.getTheme().MEDIUM, "Depth: " + getSelectedModelDesign().getDimensions().z);
            if (mouse.within(x, y, width, cellHeight) && mouse.leftClicked()) edit(ModelSetting.DEPTH);
            y -= cellHeight;

            renderer.box(x, y, width, cellHeight, isEditing(ModelSetting.X_OFFSET) ? renderer.getTheme().DARK_RED : renderer.getTheme().MEDIUM, "X Offset: " + getSelectedModelDesign().getOffset().x);
            if (mouse.within(x, y, width, cellHeight) && mouse.leftClicked()) edit(ModelSetting.X_OFFSET);
            y -= cellHeight;

            renderer.box(x, y, width, cellHeight, isEditing(ModelSetting.Y_OFFSET) ? renderer.getTheme().DARK_RED : renderer.getTheme().MEDIUM, "Y Offset: " + getSelectedModelDesign().getOffset().y);
            if (mouse.within(x, y, width, cellHeight) && mouse.leftClicked()) edit(ModelSetting.Y_OFFSET);
            y -= cellHeight;

            renderer.box(x, y, width, cellHeight, isEditing(ModelSetting.Z_OFFSET) ? renderer.getTheme().DARK_RED : renderer.getTheme().MEDIUM, "Z Offset: " + getSelectedModelDesign().getOffset().z);
            if (mouse.within(x, y, width, cellHeight) && mouse.leftClicked()) edit(ModelSetting.Z_OFFSET);
            y -= cellHeight;

            x += width;
            y = Y - cellHeight;
        }

        // MODEL PREVIEW
        width = WIDTH - x;
        mouseInPreviewArea = mouse.within(x, y, width, Gdx.graphics.getHeight());
        renderer.box(x, y, width, cellHeight, renderer.getTheme().DARK_BLUE, "Model Preview", Task.TextPosition.CENTER);

        resize((int) width, Gdx.graphics.getHeight());
        Gdx.gl.glViewport((int) x, 0, (int) width, Gdx.graphics.getHeight());

        camera.update();
        modelBatch.begin(camera);
        for (Model model : getModels())
            modelBatch.render(model.getModelInstance(), environment);
        modelBatch.end();

        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        return true;
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (mouseInPreviewArea) {
            float deltaX = -Gdx.input.getDeltaX() * degreesPerPixel;
            float deltaY = -Gdx.input.getDeltaY() * degreesPerPixel;
            camera.direction.rotate(camera.up, deltaX);
            tmp.set(camera.direction).crs(camera.up).nor();
            camera.direction.rotate(tmp, deltaY);
        }
        return true;
    }

    @Override
    public boolean keyDown(int keycode) {
        boolean shift = Util.isShiftHeld();

        switch (keycode) {
            case Input.Keys.ESCAPE:
                _displayHandler.setFocus(DisplayHandler.Focus.MAIN_VIEW);
                return false;
        }

        if (isEditing(Section.NAME)) {
            switch (keycode) {
                case Input.Keys.BACKSPACE:
                    if (getMutableProfile().getName().length() > 0)
                        getMutableProfile().rename(getMutableProfile().getName().substring(0, getMutableProfile().getName().length() - 1));
                    if (shift) getMutableProfile().rename("");
                    return false;
                case Input.Keys.SPACE:
                    getMutableProfile().rename(getMutableProfile().getName() + " ");
                    return false;
                default:
                    String string = Input.Keys.toString(keycode);
                    if ("ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".contains(string)) {
                        if (!shift) string = string.toLowerCase();
                        getMutableProfile().rename(getMutableProfile().getName() + string);
                    }
                    return false;
            }
        }

        if (isEditing(Section.VIRTUAL_CHANNELS) && hasChannelSelected()) {
            switch (keycode) {
                case Input.Keys.UP:
                    Channel previous = null;
                    for (Channel channel : getMutableProfile().channels()) {
                        if (channel.equals(getSelectedChannel())) {
                            if (previous == null) return false;
                            select(previous);
                            return false;
                        }
                        previous = channel;
                    }
                    return false;
                case Input.Keys.DOWN:
                    boolean next = false;
                    for (Channel channel : getMutableProfile().channels()) {
                        if (next) {
                            select(channel);
                            return false;
                        }
                        if (channel.equals(getSelectedChannel())) next = true;
                    }
                    return false;
                case Input.Keys.BACKSPACE:
                    if (getSelectedChannel().countAddressOffsets() == 0) return false;
                    if (Integer.toString(getSelectedChannel().getLastAddressOffset()).length() == 1) {
                        getMutableProfile().removeLastOffset(getSelectedChannel());
                    } else {
                        String lastOffset = Integer.toString(getSelectedChannel().getLastAddressOffset());
                        getMutableProfile().removeLastOffset(getSelectedChannel());
                        getMutableProfile().addOffset(getSelectedChannel(), Integer.parseInt(lastOffset.substring(0, lastOffset.length() - 1)));
                    }
                    return false;
                case Input.Keys.ENTER:
                    getMutableProfile().addOffset(getSelectedChannel(), 0);
                    return false;
                default:
                    if (getSelectedChannel().countAddressOffsets() == 0) return false;
                    String string = Input.Keys.toString(keycode);
                    if ("1234567890".contains(string)) {
                        string = getSelectedChannel().getLastAddressOffset() + string;
                        getMutableProfile().removeLastOffset(getSelectedChannel());
                        getMutableProfile().addOffset(getSelectedChannel(), Integer.parseInt(string));

                        if (getSelectedChannel().getLastAddressOffset() > 511) {
                            getMutableProfile().removeLastOffset(getSelectedChannel());
                            getMutableProfile().addOffset(getSelectedChannel(), 511);
                        }
                    }
                    return false;
            }
        }

        if (hasModelDesignSelected()) {
            switch (keycode) {
                case Input.Keys.MINUS:
                    setModelSettingValue(getModelSettingValue().contains("-") ? getModelSettingValue().replace("-", "") : "-" + getModelSettingValue());
                    return false;
                case Input.Keys.ENTER:
                    rebuildModelInstances();
                    return false;
                case Input.Keys.BACKSPACE:
                    String value = getModelSettingValue();
                    value = value.substring(0, value.length() - 1);
                    setModelSettingValue(value);
                    return false;
                default:
                    String string = Input.Keys.toString(keycode);
                    if ("1234567890.".contains(string)) {
                        setModelSettingValue(getModelSettingValue() + string);
                    }
                    return false;
            }
        }

        return true;
    }

    private void rebuildModelInstances() {
        models.clear();

        for (ModelDesign modelDesign : getMutableProfile().getModelDesigns()) {
            Vector3 position = new Vector3(
                    modelDesign.getOffset().x,
                    modelDesign.getOffset().y,
                    modelDesign.getOffset().z
            );
            models.add(new Model(modelDesign, position));
        }
    }

    private void select(Channel channel) {
        this.channel = channel;
    }

    private void select(ModelDesign modelDesign) {
        this.modelDesign = modelDesign;
    }

    public void edit(Profile profile) {
        this.profile = profile;
        this.mutableProfile = profile != null ? new MutableProfile(profile) : null;
        this.channel = null;
        this.modelDesign = null;

        rebuildModelInstances();
    }

    private void edit(Section section) {
        if (!section.equals(Section.MODEL)) this.modelDesign = null;
        this.section = section;
    }

    private void edit(ModelSetting modelSetting) {
        this.modelSetting = modelSetting;
    }

    private boolean isEditing(Section section) {
        return section.equals(this.section);
    }

    private boolean isEditing(ModelSetting modelSetting) {
        return modelSetting.equals(this.modelSetting);
    }

    private void setModelSettingValue(String value) {
        if (value.length() == 0) value = "0";
        if (value.equals("-")) value = "0";
        switch (modelSetting) {
            case WIDTH:
                getSelectedModelDesign().setDimensions(Float.parseFloat(value), getSelectedModelDesign().getDimensions().y, getSelectedModelDesign().getDimensions().z);
                break;
            case HEIGHT:
                getSelectedModelDesign().setDimensions(getSelectedModelDesign().getDimensions().x, Float.parseFloat(value), getSelectedModelDesign().getDimensions().z);
                break;
            case DEPTH:
                getSelectedModelDesign().setDimensions(getSelectedModelDesign().getDimensions().x, getSelectedModelDesign().getDimensions().y, Float.parseFloat(value));
                break;
            case X_OFFSET:
                getSelectedModelDesign().setXOffset(Float.parseFloat(value));
                break;
            case Y_OFFSET:
                getSelectedModelDesign().setYOffset(Float.parseFloat(value));
                break;
            case Z_OFFSET:
                getSelectedModelDesign().setZOffset(Float.parseFloat(value));
                break;
        }
    }

    private String getModelSettingValue() {
        switch (modelSetting) {
            case WIDTH:
                return decimalFormat.format(getSelectedModelDesign().getDimensions().x);
            case HEIGHT:
                return decimalFormat.format(getSelectedModelDesign().getDimensions().y);
            case DEPTH:
                return decimalFormat.format(getSelectedModelDesign().getDimensions().z);
            case X_OFFSET:
                return decimalFormat.format(getSelectedModelDesign().getOffset().x);
            case Y_OFFSET:
                return decimalFormat.format(getSelectedModelDesign().getOffset().y);
            case Z_OFFSET:
                return decimalFormat.format(getSelectedModelDesign().getOffset().z);
        }
        return "0";
    }

    public MutableProfile getMutableProfile() {
        return mutableProfile;
    }

    private boolean hasChannelSelected() {
        return getSelectedChannel() != null;
    }

    private Channel getSelectedChannel() {
        return channel;
    }

    private boolean hasModelDesignSelected() {
        return getSelectedModelDesign() != null;
    }

    private ModelDesign getSelectedModelDesign() {
        return modelDesign;
    }

    private List<Model> getModels() {
        return new ArrayList<>(models);
    }

    public enum Section {
        NAME, PHYSICAL_CHANNELS, VIRTUAL_CHANNELS, MODEL,
    }

    public enum ModelSetting {
        WIDTH, HEIGHT, DEPTH, X_OFFSET, Y_OFFSET, Z_OFFSET,
    }
}