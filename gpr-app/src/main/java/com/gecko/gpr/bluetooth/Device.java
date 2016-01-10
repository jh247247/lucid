package com.gecko.gpr.bluetooth;

public class Device {
    private final String mName;
    private final String mAddress;
    private final boolean mPaired;

    public Device(String name, String address, boolean paired) {
        mName = name;
        mAddress = address;
        mPaired = paired;
    }

    public String getName() {
        return mName;
    }

    public String getAddress() {
        return mAddress;
    }

    public boolean isPaired(){
        return mPaired;
    }
}
