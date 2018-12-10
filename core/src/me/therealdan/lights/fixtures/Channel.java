package me.therealdan.lights.fixtures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Channel {

    private Type type;
    private List<Integer> addressOffsets;

    public Channel(String string) {
        String[] args = string.split(": ");
        this.type = Type.valueOf(args[0]);
        this.addressOffsets = new ArrayList<>();

        for (String offset : args[1].split(", "))
            this.addressOffsets.add(Integer.parseInt(offset));
    }

    public Channel(Type type, Integer... addressOffset) {
        this.type = type;
        this.addressOffsets = new ArrayList<>(Arrays.asList(addressOffset));
    }

    public Type getType() {
        return type;
    }

    public List<Integer> addressOffsets() {
        return new ArrayList<>(addressOffsets);
    }

    public enum Type {
        INTENSITY,
        RED, GREEN, BLUE;

        public boolean obeyMaster() {
            switch (this.getCategory()) {
                case COLOR:
                    return false;
                case INTENSITY:
                default:
                    return true;
            }
        }

        public Category getCategory() {
            switch (this) {
                case RED:
                case GREEN:
                case BLUE:
                    return Category.COLOR;
                case INTENSITY:
                default:
                    return Category.INTENSITY;
            }
        }

        public String getName() {
            return this.toString().substring(0, 1) + this.toString().substring(1).toLowerCase();
        }
    }

    public enum Category {
        INTENSITY,
        COLOR;

        public String getName() {
            return this.toString().substring(0, 1) + this.toString().substring(1).toLowerCase();
        }
    }
}