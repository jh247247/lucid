package com.lightspeed.GPR1;

import com.lightspeed.gpr.lib.DataInputInterface;
import com.lightspeed.gpr.lib.Element;
import java.util.ArrayList;
import java.util.List;
import java.lang.ref.SoftReference;
import java.lang.Math;
import java.lang.ref.WeakReference;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.app.Activity;

public class BluetoothDataInput implements DataInputInterface {

    private ArrayList<SoftReference<Element>> m_previous;
    private ArrayList<Element> m_new;
    // TODO: write to file
    // TODO: Actually connect to bluetooth device

    public BluetoothDataInput(Context ctx) {
        m_previous = new ArrayList<SoftReference<Element>>();
        m_new = new ArrayList<Element>();
    }

    public int getCurrentIndex() {
        return 0; // TODO:
    }

    public Element getElement(int index) {
        return null;
    }

    public boolean open() {
        return true;
    }

    public void close() {

    }

    public boolean exists(int index) {
	return false;
    }

    public String getName() {
	return "Bluetooth"; // TODO: fixxxx
    }
}
