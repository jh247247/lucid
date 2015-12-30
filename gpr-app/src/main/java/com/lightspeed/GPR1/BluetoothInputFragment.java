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
/**
 * This file handles the android side of bluetooth communications
 */

public class BluetoothInputFragment extends Fragment {
    public static String TAG = "BluetoothInputFragment";

    private DataInputFragment.OnInputChangedListener m_inputCallback;
    private BluetoothDataInput m_dataInput;

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
        m_dataInput = new BluetoothDataInput(activity);
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
}
