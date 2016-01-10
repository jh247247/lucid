package com.gecko.gpr.input;

import android.util.Log;
import com.lightspeed.gpr.lib.AbstractDataInput;
import com.lightspeed.gpr.lib.DataPacketParser;
import com.lightspeed.gpr.lib.Element;
import com.lightspeed.gpr.lib.PacketParser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.lang.ref.SoftReference;
import java.lang.Math;
import java.lang.ref.WeakReference;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.app.Activity;

import com.google.common.util.concurrent.ListenableFuture;

import com.gecko.gpr.bluetooth.BluetoothHandler;
import com.gecko.gpr.bluetooth.Device;

public class BluetoothDataInput
    extends AbstractDataInput {

    private static String TAG = "BluetoothDataInput";

    private BluetoothHandler m_bt;
    private WeakReference<StateListener> m_stateListener;
    private PacketParser m_parser = new PacketParser();

    private HashMap<Integer,Element> m_prevElementCache = new HashMap<Integer,Element>();
    private int m_currIndex = 0;

    // TODO: write to file
    // TODO: Actually connect to bluetooth device

    public BluetoothDataInput(Activity act) {
        m_bt = new BluetoothHandler(act.getApplicationContext(),m_listener);
    }

    public BluetoothDataInput(Activity act, StateListener sl) {
        this(act);
        setStateListener(sl);
    }

    public void setStateListener(StateListener sl) {
        m_stateListener = new WeakReference<StateListener>(sl);
    }

    public int getCurrentIndex() {
        return m_currIndex; // TODO:
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

        public void selectDevice(final List<Device> dl, final BluetoothHandler.ConnectionCallback connectionCallback);
    }

    private BluetoothHandler.Listener m_listener = new BluetoothHandler.Listener() {
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
                                       final BluetoothHandler.ConnectionCallback connectionCallback) {

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
            public void onDataReceived(ByteBuffer data) {
                Log.d(TAG,"Received: " + data);
                //receives all bytes
                try {
                    while(data.hasRemaining()) {
                        if(m_parser.parse(data) && m_elementListener != null) {
                            m_elementListener.onNewElement(m_parser.getDataPacketParser().getDecodedElement(),
                                                           m_currIndex++);
                        }
                    }
                } catch (IOException ex) {
                } finally {
                }

            }
        };
}
