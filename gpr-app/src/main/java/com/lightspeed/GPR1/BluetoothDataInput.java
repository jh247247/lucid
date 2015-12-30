package com.lightspeed.GPR1;

import com.lightspeed.gpr.lib.AbstractDataInput;
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

import com.google.common.util.concurrent.ListenableFuture;

import io.palaima.smoothbluetooth.SmoothBluetooth;
import io.palaima.smoothbluetooth.Device;

public class BluetoothDataInput
    extends AbstractDataInput {

    private SmoothBluetooth m_bt;
    private StateListener m_stateListener;

    private ArrayList<SoftReference<Element>> m_previous;
    private ArrayList<Element> m_new;

    // TODO: write to file
    // TODO: Actually connect to bluetooth device



    public BluetoothDataInput(Activity act) {
        m_previous = new ArrayList<SoftReference<Element>>();
        m_new = new ArrayList<Element>();
	m_bt = new SmoothBluetooth(act.getApplicationContext());
    }

    public BluetoothDataInput(Activity act, StateListener sl) {
	this(act);
	m_stateListener = sl;
    }

    public int getCurrentIndex() {
        return 0; // TODO:
    }

    public ListenableFuture<Element> getElement(int index) {
        return null;
    }

    public boolean open() {
	m_bt.doDiscovery();
	if(m_stateListener != null) {
	    m_stateListener.startDiscovery();
	}
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

    public static interface StateListener {
	public void startDiscovery();
	public void stopDiscovery();

	public void startConnect();
	public void stopConnect();

	public int selectDevice(final List<Device> dl);
    }
}
