package com.lightspeed.GPR1;

import com.lightspeed.gpr.lib.DataInputInterface;
import com.lightspeed.gpr.lib.Element;
import java.util.ArrayList;
import java.util.List;
import java.lang.ref.SoftReference;
import java.lang.Math;
import io.palaima.smoothbluetooth.SmoothBluetooth;
import io.palaima.smoothbluetooth.Device;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.app.Activity;

public class BluetoothDataInput implements DataInputInterface {

    public static final int ENABLE_BT__REQUEST = 1;

    private ArrayList<SoftReference<Element>> m_previous;
    private ArrayList<Element> m_new;
    private SmoothBluetooth m_bluetooth;
    private Context m_ctx;
    // TODO: write to file



    private SmoothBluetooth.Listener m_blistener = new
        SmoothBluetooth.Listener() {
            @Override
            public void onBluetoothNotSupported() {
                // device does not support bluetooth
                // how did they even install it??
            }

            @Override
            public void onBluetoothNotEnabled() {
                //bluetooth is disabled, probably call Intent request to enable bluetooth
                Intent enableBluetooth = new
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

                // this is disgusting, find a way to fix it.
                ((Activity)m_ctx).startActivityForResult(enableBluetooth, ENABLE_BT__REQUEST);
            }

            @Override
            public void onConnecting(Device device) {
                //called when connecting to particular device
            }

            @Override
            public void onConnected(Device device) {
                //called when connected to particular device
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
            }

            @Override
            public void onDiscoveryFinished() {
                //called when discovery is finished
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
            }

            @Override
            public void onDataReceived(int data) {
                //receives all bytes
            }
        };

    public BluetoothDataInput(Context ctx) {
        m_previous = new ArrayList<SoftReference<Element>>();
        m_new = new ArrayList<Element>();
        m_bluetooth = new SmoothBluetooth(ctx,m_blistener);

    }

    public int getCurrentIndex() {
        return 0; // TODO:
    }

    public boolean hasNext() {
        return !m_new.isEmpty();
    }

    public Element getNext() {
        // fifo, so remove from start
        if(m_new.size() > 0) {
            return m_new.remove(0);
        }
        return null;
    }

    private void trimPrevious() {
        while(m_previous.size() > 0 && // still have elements
              m_previous.get(m_previous.size()-1).get() == null) { // start element is expired
            m_previous.remove(m_previous.size()-1);
        }
    }


    public Element getPrevious(int offset) {
        trimPrevious();
        Element ret = null;
        if(offset < 0 ) {
            return null; // cannot grab from future...
        }

        // definitely not stored. have to reload... (FIXME)
        if(offset > m_previous.size()-1 ||
           m_previous.get(offset).get() == null) {
            // would load it from file, but cbf generate random var.
            ret = new Element(0,255);
            for(int i = 0; i < 255; i++) {
                ret.setSample(i,Math.random()*255);
            }
            return ret;
        }

        // woo! we still have it! return it.
        return m_previous.get(offset).get();
    }

    public void setUpdateCallback(InputUpdateCallback call) {

    }

    public boolean open() {
        return true;
    }

    public void close() {

    }

    public String getName() {
        return m_ctx.getString(R.string.bluetooth);
    }
}
