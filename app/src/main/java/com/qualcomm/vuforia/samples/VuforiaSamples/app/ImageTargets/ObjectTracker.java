package com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargets;

import android.opengl.Matrix;
import android.util.Log;

import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.Vec3F;
import com.qualcomm.vuforia.samples.SampleApplication.utils.SampleMath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by yuki on 11/19/15.
 */


public class ObjectTracker
{
    private static final String LOGTAG = "ObjectTracker";

    private Map<String, ArrayList<MovableObject>> objectMap = new HashMap<>();

    private MovableObject holdingObj;

    // TODO: FIX THESE AS GLOBAL FROM RENDERER
    private int IMG_WIDTH = 247;
    private int IMG_HEIGHT = 173;
    private float PLANE_INTERSECT_Z_BOUND = 100.0f;
    private float OBJECT_SCALE_FLOAT = 1.0f;
    private float UI_MILLISECOND_DELAY = 1000.0f;

    // For calculating hold/drop delays
    private float mLastFrameTime = 0.0f;
    private float mTimer = 0.0f;

    // For calculating and placing held objects in center of screen.
    private boolean mSaveOnceMVCCalled = false;
    private float[] mModelViewCenter = new float[16];

    // Constants for repositioning objects on corners.
    private static final float REPO_X = 80.0f;
    private static final float REPO_Y = 80.0f;

    public ObjectTracker() {}

    public float[] getModelViewCenter() {
        return mModelViewCenter;
    }

    public boolean isHoldingObj() {
        return holdingObj != null;
    }

    public MovableObject getHeldObj() {
        return holdingObj;
    }

    public boolean hasSavedModelViewCenter() {
        return mSaveOnceMVCCalled;
    }

    public void add(String image, MovableObject obj) {
        if (!objectMap.containsKey(image)) {
            objectMap.put(image, new ArrayList<MovableObject>());
        }
        objectMap.get(image).add(obj);
    }

    public ArrayList<MovableObject> getObjectsByImage(String image)
    {
        return objectMap.get(image);
    }

    public boolean imageHasObjects(String image) { return getObjectsByImage(image) != null; }

    public boolean isOnDelay() { return mTimer > 0.0f; }

    public void setUIDelay(float milliseconds) { mTimer = milliseconds; }

    public void updateDeltaTime()
    {
        float currentTime = System.nanoTime() / 1000000.0f;

        if (mLastFrameTime == 0.0f)
            mLastFrameTime = currentTime;

        float deltaTime = currentTime - mLastFrameTime;
        mLastFrameTime = currentTime;

        if (mTimer > 0.0f)
            mTimer -= deltaTime;
    }

    public void onDrawFrame()
    {
        updateDeltaTime();
    }

    public void onDrawFrameUpdateTrackable(TrackableResult result)
    {
        if (isOnDelay()) {
            return;
        }

        if (holdingObj == null) {
            updateOnEmpty(result);
        } else {
            updateOnHolding(result);
        }
    }

    public void updateOnEmpty(TrackableResult result)
    {
        Vec3F cameraPos = getCameraPosition(Tool.convertPose2GLMatrix(result.getPose()));
        ArrayList<MovableObject> imgObjs = getObjectsByImage(result.getTrackable().getName());
        for (MovableObject obj : imgObjs) {
            if (obj.inSphereBound(cameraPos)) {
                hold(obj);
                break;
            }
        }
    }

    public void updateOnHolding(TrackableResult result)
    {
        String imageName = result.getTrackable().getName();
        ArrayList<MovableObject> imageObjs = getObjectsByImage(imageName);
        Vec3F cameraPos = getCameraPosition(Tool.convertPose2GLMatrix(result.getPose()));

        // Test that camera is not intersecting with other objects on the image.
        for (MovableObject obj : imageObjs) {
            // TODO: FIX TO RADIUS TEST
            if (obj.inSphereBound(cameraPos)) {
                Log.d("UpdateOnHolding: ", "inSphereBounds(): " + obj);
                return;
            }
        }

        // If intersecting on image, drop it.
        if (isPlaneIntersection(cameraPos)) {
            Vec3F dropPoint =
                    new Vec3F(cameraPos.getData()[0], cameraPos.getData()[1], OBJECT_SCALE_FLOAT);
            drop(imageName, dropPoint);
        }
    }

    public boolean isPlaneIntersection(Vec3F cameraPos) {
        float[] pos = cameraPos.getData();
        Log.d("ObjectTracker: ", "isPlaneIntersection(): cameraPos: " + pos[0] + ", " + pos[1] + ", " + pos[2]);
        return pos[0] < (float)(IMG_WIDTH / 2.0) && pos[0] > -(float)(IMG_WIDTH / 2.0) &&
                pos[1] < (float)(IMG_HEIGHT / 2.0) && pos[1] > -(float)(IMG_HEIGHT / 2.0) &&
                pos[2] < PLANE_INTERSECT_Z_BOUND;
    }

    public void hold(MovableObject obj)
    {
        holdingObj = obj;
        obj.setOnCamera(true);
        objectMap.get(obj.getImageTarget()).remove(obj);
        obj.setImageTarget("");
        Log.d("ObjTracker: ", "hold() on Obj: " + holdingObj);
        setUIDelay(UI_MILLISECOND_DELAY);
    }

    public void drop(String imageName, Vec3F point)
    {
        Log.d("ObjTracker: ", "drop() on Obj: " + holdingObj);
        holdingObj.setOnCamera(false);
        holdingObj.setPosition(point);
        holdingObj.setImageTarget(imageName);
        objectMap.get(imageName).add(holdingObj);
        holdingObj = null;
        setUIDelay(UI_MILLISECOND_DELAY);
    }

    private Vec3F getCameraPosition(Matrix44F modelViewMatrix)
    {
        Matrix44F inverseMV = SampleMath.Matrix44FInverse(modelViewMatrix);
        Matrix44F invTranspMV = SampleMath.Matrix44FTranspose(inverseMV);
        return new Vec3F(invTranspMV.getData()[12],
                invTranspMV.getData()[13],
                invTranspMV.getData()[14]);
    }

    public void repositionObjects()
    {
        Vec3F[] localCorners = {new Vec3F(REPO_X, REPO_Y, 0), new Vec3F(-REPO_X, REPO_Y, 0),
                new Vec3F(REPO_X, -REPO_Y, 0), new Vec3F(-REPO_X, -REPO_Y, 0)};
        for (Map.Entry<String, ArrayList<MovableObject>> entry : objectMap.entrySet()) {
            for (int i = 0; i < localCorners.length; ++i) {
                int objListSize = entry.getValue().size();
                if (i < objListSize) {
                    entry.getValue().get(i).setPosition(localCorners[i]);
                }
            }
        }
    }
}
