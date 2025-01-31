package dev.therealdan.lights.fixtures.fixture;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;
import dev.therealdan.lights.fixtures.fixture.profile.ModelDesign;

public class Model {

    private ModelInstance modelInstance;
    private Vector3 position, offset, dimensions;

    public Model(ModelDesign modelDesign, Vector3 position) {
        this.modelInstance = new ModelInstance(modelDesign.getModel(), position);
        this.position = position;
        this.offset = modelDesign.getOffset();
        this.dimensions = modelDesign.getDimensions();
    }

    public void setColor(Color color) {
        getModelInstance().materials.get(0).set(ColorAttribute.createDiffuse(color));
    }

    public void move(float x, float y, float z) {
        getPosition().add(x, y, z);
        getModelInstance().transform.setTranslation(getPosition());
    }

    public void teleport(float x, float y, float z, boolean offset) {
        if (offset) {
            x += getOffset().x;
            y += getOffset().y;
            z += getOffset().z;
        }
        getModelInstance().transform.setTranslation(position.set(x, y, z));
    }

    public ModelInstance getModelInstance() {
        return modelInstance;
    }

    public Vector3 getPosition() {
        return position;
    }

    public Vector3 getOffset() {
        return offset;
    }

    public Vector3 getDimensions() {
        return dimensions;
    }
}