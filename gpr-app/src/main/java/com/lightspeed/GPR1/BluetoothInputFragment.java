package com.lightspeed.GPR1;

import com.lightspeed.gpr.lib.AbstractDataInput;
import com.lightspeed.gpr.lib.Element;
import com.lightspeed.gpr.lib.GprFileReader;

import java.util.ArrayList;
import java.util.List;
import java.lang.ref.WeakReference;
import java.lang.Math;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.widget.Toast;
import android.util.Log;
import android.support.v4.app.Fragment;
import android.widget.Button;
import android.widget.TextView;
import android.widget.CheckedTextView;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.content.DialogInterface;

import butterknife.ButterKnife;
import butterknife.Bind;

import io.palaima.smoothbluetooth.SmoothBluetooth;

import com.google.common.util.concurrent.ListenableFuture;

import android.content.DialogInterface;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.afollestad.materialdialogs.internal.MDTintHelper;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem;
import com.afollestad.materialdialogs.util.DialogUtils;

import io.palaima.smoothbluetooth.Device;


/**
 * This file handles the android side of bluetooth communications
 */

public class BluetoothInputFragment extends Fragment {

    public static String TAG = "BluetoothInputFragment";

    private DataInputFragment.OnInputChangedListener m_inputCallback;
    private BluetoothDataInput m_dataInput;

    private MaterialDialog m_dialog;

    @Bind(R.id.record_checkbox) CheckedTextView m_record;
    @Bind(R.id.connect_button) Button m_connectBtn;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            m_inputCallback = (DataInputFragment.OnInputChangedListener) activity;
        }
        catch(Exception e) {
            Log.e(TAG, "Attached activity does not implement OnInputChangedListener!");
        }

        // setup bluetooth when user presses connect?
        m_dataInput = new BluetoothDataInput(activity, m_stateListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        View ret = inflater.inflate(R.layout.bluetooth_input_ui,container,false);
        ButterKnife.bind(this,ret);

        m_connectBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // bluetooth open -> connect to device
                    m_dataInput.open();
                }
            });

        return ret;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        m_dataInput.close();
    }

    private BluetoothDataInput.StateListener m_stateListener = new BluetoothDataInput.StateListener() {
            public void startDiscovery() {
                Activity act = getActivity();
                if(act == null) return;

                m_dialog = new MaterialDialog.Builder(act)
                    .title(R.string.bluetooth_discovery)
                    .content(R.string.please_wait)
                    .progress(true, 0)
                    .progressIndeterminateStyle(false)
                    .show();
            }

            public void stopDiscovery() {
		m_dialog.dismiss();
            }

            public void startConnect() {

            }

            public void stopConnect() {

            }

            public int selectDevice(final List<Device> dl) {
                return 0;
            }
        };
}
