package com.storyous.roottools.magisk;

class Native {

    static {
        System.loadLibrary("native-lib");
    }

    static native boolean isMagiskPresentNative();

}
