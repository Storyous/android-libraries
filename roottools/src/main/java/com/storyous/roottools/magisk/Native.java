package com.storyous.roottools.magisk;

class Native {

    static {
        System.loadLibrary("root-lib");
    }

    static native boolean isMagiskPresentNative();

}
