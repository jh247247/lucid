package com.gecko.gpr.ui;

import com.gecko.gpr.R;

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
import com.gecko.gpr.input.BluetoothDataInput;


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

	    int m_connectionSelect; // hack so dialog will be happy

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
                m_dialog = null;
            }

            public void startConnect() {
                Activity act = getActivity();
                if(act == null) return;

                m_dialog = new MaterialDialog.Builder(act)
                    .title(R.string.bluetooth_connecting)
                    .content(R.string.please_wait)
                    .progress(true, 0)
                    .progressIndeterminateStyle(false)
                    .show();
            }

            public void stopConnect() {
                m_dialog.dismiss();
                m_dialog = null;
            }

            public void selectDevice(final List<Device> dl,
				     final SmoothBluetooth.ConnectionCallback connectionCallback) {
                Device[] da = dl.toArray(new Device[dl.size()]);
                String[] dn = new String[da.length];
                for(int i = 0; i < da.length; i++) {
                    dn[i] = da[i].getName();
                }

                // TODO: show dialog
                new MaterialDialog.Builder(getActivity())
                    .title(R.string.bluetooth_device_select)
                    .items(dn)
                    .itemsCallbackSingleChoice(2, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
				m_connectionSelect = which;
                                Log.d(TAG,which + ": " + text);
                                return true; // allow selection
                            }
                        })
		    .dismissListener(new DialogInterface.OnDismissListener() {
			    @Override
			    public void onDismiss(DialogInterface dialog) {
				connectionCallback.connectTo(dl.get(m_connectionSelect));
			    }
			})
                    .positiveText(R.string.bluetooth_connect)
		    .show();
            }
        };
}
