package me.therealdan.lights.ui.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import me.therealdan.lights.fixtures.Channel;
import me.therealdan.lights.fixtures.ModelDesign;
import me.therealdan.lights.fixtures.Profile;
import me.therealdan.lights.renderer.Renderer;
import me.therealdan.lights.ui.view.Tab;

import java.util.ArrayList;
import java.util.List;

public class ProfileEditor implements Tab {

    private static ProfileEditor profileEditor;

    private List<Profile> profiles = new ArrayList<>();

    public ProfileEditor() {
        profileEditor = this;

        FileHandle fileHandle = Gdx.files.local("Lights/Profiles/");
        if (fileHandle.exists() && fileHandle.isDirectory())
            for (FileHandle child : fileHandle.list())
                load(child);
    }

    private void load(FileHandle fileHandle) {
        String name = fileHandle.name().replace(".txt", "");
        int physicalChannels = 0;
        List<Channel> channels = new ArrayList<>();
        List<ModelDesign> modelDesigns = new ArrayList<>();

        boolean isChannel = false, isModel = false;
        for (String line : fileHandle.readString().split("\\r?\\n")) {
            if (line.startsWith("Physical Channels: ")) {
                physicalChannels = Integer.parseInt(line.split(": ")[1]);
                continue;
            } else if (line.startsWith("Channels:")) {
                isChannel = true;
                isModel = false;
                continue;
            } else if (line.startsWith("Models:")) {
                isChannel = false;
                isModel = true;
                continue;
            }

            if (isChannel) {
                channels.add(new Channel(line.substring(2)));
            } else if (isModel) {
                modelDesigns.add(ModelDesign.fromString(line.substring(2)));
            }
        }

        Profile profile = new Profile(name, modelDesigns, channels);
        profile.setPhysicalChannels(physicalChannels);
        add(profile);
    }

    @Override
    public void save() {
        for (Profile profile : profiles()) {
            FileHandle fileHandle = Gdx.files.local("Lights/Profiles/" + profile.getName() + ".txt");
            fileHandle.writeString("", false);

            fileHandle.writeString("Physical Channels: " + profile.getPhysicalChannels() + "\r\n", true);

            fileHandle.writeString("Channels:\r\n", true);
            for (Channel channel : profile.channels()) {
                fileHandle.writeString("  " + channel.getType().toString() + ": ", true);
                StringBuilder offsets = new StringBuilder();
                for (int offset : channel.addressOffsets())
                    offsets.append(", ").append(offset);
                fileHandle.writeString(offsets.toString().replaceFirst(", ", ""), true);
                fileHandle.writeString("\r\n", true);
            }

            fileHandle.writeString("Models:\r\n", true);
            for (ModelDesign modelDesign : profile.getModelDesigns())
                fileHandle.writeString("- " + modelDesign.toString() + "\r\n", true);
        }
    }

    @Override
    public void draw(Renderer renderer, float X, float Y, float WIDTH, float HEIGHT) {
    }

    public static void add(Profile profile) {
        profileEditor.profiles.add(profile);
    }

    public static Profile profileByName(String name) {
        for (Profile profile : profiles())
            if (profile.getName().equalsIgnoreCase(name))
                return profile;

        return null;
    }

    public static List<Profile> profiles() {
        return new ArrayList<>(profileEditor.profiles);
    }
}