package com.gecko.gpr.input;

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
    private WeakReference<StateListener> m_stateListener;

    // TODO: write to file
    // TODO: Actually connect to bluetooth device

    public BluetoothDataInput(Activity act) {
        m_bt = new SmoothBluetooth(act.getApplicationContext(),m_listener);
    }

    public BluetoothDataInput(Activity act, StateListener sl) {
        this(act);
        setStateListener(sl);
    }

    public void setStateListener(StateListener sl) {
        m_stateListener = new WeakReference<StateListener>(sl);
    }

    public int getCurrentIndex() {
        return 0; // TODO:
    }

    public ListenableFuture<Element> getElement(int index) {
        return null;
    }

    public boolean open() {
	m_bt.disconnect();
        m_bt.doDiscovery();
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

        public void selectDevice(final List<Device> dl, final SmoothBluetooth.ConnectionCallback connectionCallback);
    }

    private SmoothBluetooth.Listener m_listener = new SmoothBluetooth.Listener() {
            @Override
            public void onBluetoothNotSupported() {
                //device does not support bluetooth
            }

            @Override
            public void onBluetoothNotEnabled() {
                //bluetooth is disabled, probably call Intent request to enable bluetooth
            }

            @Override
            public void onConnecting(Device device) {
                //called when connecting to particular device
                StateListener sl = m_stateListener.get();

                if(sl != null) {
                    sl.startConnect();
                }
            }

            @Override
            public void onConnected(Device device) {
                //called when connected to particular device
		StateListener sl = m_stateListener.get();

                if(sl != null) {
                    sl.stopConnect();
                }

            }

            @Override
            public void onDisconnected() {
                //called when disconnected from device
            }

            @Override
            public void onConnectionFailed(Device device) {
                //called when connection failed to particular device
            }

            @Override
            public void onDiscoveryStarted() {
		//called when discovery is started
                StateListener sl = m_stateListener.get();

                if(sl != null) {
                    sl.startDiscovery();
                }

            }

            @Override
            public void onDiscoveryFinished() {
		//called when discovery is finished
                StateListener sl = m_stateListener.get();

                if(sl != null) {
                    sl.stopDiscovery();
                }

            }

            @Override
            public void onNoDevicesFound() {
                //called when no devices found
            }

            @Override
            public void onDevicesFound(final List<Device> deviceList,
                                       final SmoothBluetooth.ConnectionCallback connectionCallback) {

                //receives discovered devices list and connection callback
                //you can filter devices list and connect to specific one
                //connectionCallback.connectTo(deviceList.get(position));

                StateListener sl = m_stateListener.get();

                if(sl == null) {
		    return;
                }

		// this may be blocking... another thread?
		sl.selectDevice(deviceList,connectionCallback);
            }

            @Override
            public void onDataReceived(int data) {
                //receives all bytes
            }
        };
}
