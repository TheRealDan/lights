package dev.therealdan.lights.main;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import dev.therealdan.lights.dmx.Output;
import dev.therealdan.lights.programmer.Programmer;
import dev.therealdan.lights.renderer.Renderer;
import dev.therealdan.lights.store.*;
import dev.therealdan.lights.ui.DisplayHandler;

public class Lights extends ApplicationAdapter {

    private SettingsStore _settingsStore;
    private ControlsStore _controlsStore;
    private ProfilesStore _profilesStore;
    private FixturesStore _fixturesStore;
    private GroupsStore _groupsStore;

    private Mouse _mouse;
    private Renderer _renderer;
    private Output _output;
    private DisplayHandler _displayHandler;

    @Override
    public void create() {
        _settingsStore = new SettingsStore();
        _controlsStore = new ControlsStore();
        _profilesStore = new ProfilesStore();
        _fixturesStore = new FixturesStore(_profilesStore);
        _groupsStore = new GroupsStore(_fixturesStore);

        new Programmer(_fixturesStore, _groupsStore); // todo review static abuse

        _mouse = new Mouse();
        _renderer = new Renderer();
        _output = new Output(_settingsStore);
        _displayHandler = new DisplayHandler(_settingsStore, _controlsStore, _profilesStore, _fixturesStore, _groupsStore, _mouse, _renderer.getTheme(), _output);

        Gdx.graphics.setVSync(true);
        Gdx.input.setInputProcessor(_displayHandler);
    }

    @Override
    public void render() {
        Color background = _renderer.getTheme().BACKGROUND;
        Gdx.gl.glClearColor(background.r, background.g, background.b, background.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        _mouse.update();
        _displayHandler.update();
        _displayHandler.draw(_mouse, _renderer);
        _renderer.draw();
    }

    @Override
    public void resize(int width, int height) {
        _renderer.resize();
        _displayHandler.resize(width, height);
    }

    @Override
    public void dispose() {
        _settingsStore.saveToFile();
        _controlsStore.saveToFile();
        _profilesStore.saveToFile();

        _displayHandler.save();
        _renderer.dispose();
    }
}