package dev.therealdan.lights.dmx;

import com.badlogic.gdx.graphics.Color;
import dev.therealdan.lights.panels.panels.ConsolePanel;
import dev.therealdan.lights.renderer.Renderer;
import dev.therealdan.lights.renderer.Task;
import dev.therealdan.lights.settings.Setting;
import dev.therealdan.lights.settings.SettingsStore;
import dev.therealdan.lights.ui.PanelHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DMX {

    public static final float MAX_CHANNELS = 512;

    private SettingsStore _settingsStore;

    private static HashMap<String, DMX> dmx = new HashMap<>();

    private HashMap<Integer, Integer> channels = new HashMap<>();
    private HashMap<Integer, Integer> lastSent = new HashMap<>();

    private ArrayList<Long> channelsPerSecondCounter = new ArrayList<>();

    private String level;

    private int next = 1;

    public DMX(SettingsStore settingsStore, String level) {
        _settingsStore = settingsStore;
        this.level = level;
    }

    public void flood(int value) {
        for (int channel = 1; channel <= MAX_CHANNELS; channel++)
            set(channel, value);
    }

    public void zero() {
        flood(0);
    }

    public void clear() {
        channels.clear();
    }

    public void copy(DMX dmx) {
        for (int channel = 1; channel <= MAX_CHANNELS; channel++)
            set(channel, dmx.get(channel));
    }

    public void pull(DMX dmx) {
        for (int channel : active())
            set(channel, dmx.get(channel));
    }

    public void set(int channel, int value) {
        channels.put(
                Math.min(Math.max(channel, 1), (int) MAX_CHANNELS),
                Math.min(Math.max(value, 0), 255)
        );
    }

    public int get(int channel) {
        float value = pget(channel);
        return (int) (value * PanelHandler.getMaster());
    }

    private int pget(int channel) {
        return channels.getOrDefault(channel, 0);
    }

    public List<Integer> active() {
        return new ArrayList<>(channels.keySet());
    }

    public byte[] getNext() {
        StringBuilder data = new StringBuilder();

        if (_settingsStore.getByKey(Setting.Key.CONTINUOUS).isTrue()) {
            for (int address = next; address < next + _settingsStore.getByKey(Setting.Key.CHANNELS_PER_SEND).getInt(); address++) {
                int value = get(address);
                if (value < 10) data.append("0");
                if (value < 100) data.append("0");
                data.append(Double.toString(value).replace(".0", ""));
                if (address < 10) data.append("0");
                if (address < 100) data.append("0");
                data.append(Double.toString(address).replace(".0", ""));
                data.append(" ");
            }

            next += _settingsStore.getByKey(Setting.Key.CHANNELS_PER_SEND).getInt();
            if (next > MAX_CHANNELS) next = 1;
        } else {
            int currentValue = -1;
            int queued = 0;
            for (int address = 1; address <= MAX_CHANNELS; address++) {
                if (queued >= _settingsStore.getByKey(Setting.Key.CHANNELS_PER_SEND).getInt()) break;
                if (channelsPerSecondCounter.size() > _settingsStore.getByKey(Setting.Key.CHANNELS_PER_TIME).getInt()) break;
                int value = channels.get(address);
                if ((!lastSent.containsKey(address) || value != lastSent.get(address)) && (currentValue == -1 || value == currentValue)) {
                    if (currentValue == -1) {
                        if (value < 10) data.append("0");
                        if (value < 100) data.append("0");
                        data.append(Double.toString(value).replace(".0", ""));
                    }
                    currentValue = value;
                    lastSent.put(address, value);
                    if (address < 10) data.append("0");
                    if (address < 100) data.append("0");
                    data.append(Double.toString(address).replace(".0", ""));
                    queued++;
                    channelsPerSecondCounter.add(System.currentTimeMillis());
                }
            }
            data.append(" ");

            for (long timestamp : new ArrayList<>(channelsPerSecondCounter)) {
                if (System.currentTimeMillis() - timestamp > 250) {
                    channelsPerSecondCounter.remove(timestamp);
                }
            }
        }

        if (data.length() <= 1) return null;
        if (_settingsStore.getByKey(Setting.Key.SHOW_DMX_SEND_DEBUG).isTrue())
            ConsolePanel.log("Preparing to send: " + data.toString());
        try {
            return data.toString().getBytes("UTF-8");
        } catch (Exception e) {
            ConsolePanel.log("Unsupported encoding");
            return data.toString().getBytes();
        }
    }

    public String getLevel() {
        return level;
    }

    public static DMX get(SettingsStore settingsStore, String level) {
        if (!dmx.containsKey(level)) dmx.put(level, new DMX(settingsStore, level));
        return dmx.get(level);
    }

    public static List<String> levels() {
        return new ArrayList<>(dmx.keySet());
    }

    public static void draw(Renderer renderer, float x, float y, float width, float height, Color color, List<Integer> originalValues) {
        long timestamp = System.currentTimeMillis();
        int overflow = (int) (DMX.MAX_CHANNELS - width);
        List<Integer> values = new ArrayList<>();
        int previousValue = -1;
        for (int address = 1; address <= DMX.MAX_CHANNELS; address++) {
            int value = originalValues.get(address - 1);
            if (previousValue != -1 && overflow > 0) {
                if (previousValue == value) {
                    overflow--;
                    continue;
                }
            }
            values.add(value);
            previousValue = value;
        }

        for (int value : values) {
            renderer.queue(new Task(x, y - height).line(x, y - height + (height * value / 255f)).setColor(color));
            x++;
        }

        long timepassed = System.currentTimeMillis() - timestamp;
        if (timepassed > 1) System.out.println(timepassed + "ms");
    }
}