package com.xjl.ecamera2.helper;

import android.content.Context;
import android.util.Log;
import android.view.OrientationEventListener;

public class DeviceOrientationListener extends OrientationEventListener {

    private static final String TAG = "DeviceOrientationListener";

    public int orientation;

    public DeviceOrientationListener(Context context, int rate) {
        super(context, rate);
    }

    @Override
    public void onOrientationChanged(int orientation) {
        this.orientation = orientation;
    }

    public int getOrientation() {
        return orientation;
    }

}
