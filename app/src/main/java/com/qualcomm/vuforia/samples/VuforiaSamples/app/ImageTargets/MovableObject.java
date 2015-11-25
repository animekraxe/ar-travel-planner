package com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargets;

import com.qualcomm.vuforia.Vec3F;
import com.qualcomm.vuforia.samples.SampleApplication.utils.SampleMath;


/**
 * Created by yuki on 11/19/15.
 */
public class MovableObject
{
    private static int nextObjectId = 1;

    private static int newId()
    {
        int newId = nextObjectId;
        nextObjectId += 1;
        return newId;
    }

    private int id;

    private Vec3F position = new Vec3F(0, 0, 0);

    private float boundingRadius = 11
    0.0f;

    private String imageTarget;

    private int textureIndex;

    private boolean onCamera = false;

    public MovableObject(String image, Vec3F startPoint)
    {
        id = newId();
        imageTarget = image;
        textureIndex = nameToTextureIndex(imageTarget);
        position = startPoint;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MovableObject other = (MovableObject)obj;
        return other.getId() == this.id;
    }

    public int getTextureIndex() {
        return textureIndex;
    }

    public void setTextureIndex(int textureIndex) {
        this.textureIndex = textureIndex;
    }

    public int getId() {
        return id;
    }

    public Vec3F getPosition() {
        return position;
    }

    public void setPosition(Vec3F position) {
        this.position = position;
    }

    public float getBoundingRadius() {
        return boundingRadius;
    }

    public void setBoundingRadius(float boundingRadius) {
        this.boundingRadius = boundingRadius;
    }

    public String getImageTarget() {
        return imageTarget;
    }

    public void setImageTarget(String imageTarget) {
        this.imageTarget = imageTarget;
    }

    public boolean getOnCamera() {
        return onCamera;
    }

    public void setOnCamera(boolean onCamera) {
        this.onCamera = onCamera;
    }

    public boolean inSphereBound(Vec3F point)
    {
        Vec3F originDiff = SampleMath.Vec3FSub(point, position);
        float originDist = SampleMath.Vec3FMagnitude(originDiff);

        return originDist <= boundingRadius;
    }

    public boolean inSphereBound(MovableObject obj)
    {
        Vec3F diff = SampleMath.Vec3FSub(obj.getPosition(), this.position);
        return SampleMath.Vec3FMagnitude(diff) <= (boundingRadius * 2.0f);
    }


    public static int nameToTextureIndex(String imageName)
    {
        int result = imageName.equalsIgnoreCase("stones") ? 0 : 1;
        result = imageName.equalsIgnoreCase("tarmac") ? 2 : result;
        return result;
    }
}
