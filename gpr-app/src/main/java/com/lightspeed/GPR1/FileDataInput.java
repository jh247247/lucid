package com.lightspeed.GPR1;

import com.lightspeed.gpr.lib.DataInputInterface;
import com.lightspeed.gpr.lib.Element;

import java.util.ArrayList;
import java.util.List;
import java.lang.ref.SoftReference;
import java.lang.Math;
import java.io.File;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.widget.Toast;
import android.util.Log;

import android.content.DialogInterface;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.afollestad.materialdialogs.ThemeSingleton;
import com.afollestad.materialdialogs.internal.MDTintHelper;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem;
import com.afollestad.materialdialogs.util.DialogUtils;

import de.greenrobot.event.EventBus;

public class FileDataInput implements DataInputInterface {
    ArrayList<Integer> m_indexes;

    private Context m_ctx;

    public FileDataInput(Context ctx) {
        m_indexes = null;
        m_ctx = ctx;

        EventBus.getDefault().register(this);
    }

    public int getCurrentIndex() {
        return 0; // TODO:
    }

    public boolean hasNext() {
        return false;
    }

    public Element getNext() {
        return null;
    }


    public Element getPrevious(int offset) {
        return null;
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

    public void onEvent(FileDialog.FileChangedEvent e) {
        Toast.makeText(m_ctx, e.file.toString(), Toast.LENGTH_SHORT).show();
        new FileIndexer(e.file);
    }


    private class FileIndexer implements Runnable {
        final static int MAX_PROGRESS = 100; // amount of discreteness

        int m_progress; // current progress of reading the file
        File m_currFile;
        Thread m_thr;
        MaterialDialog m_dialog;

        public FileIndexer(File f) {
            Log.d("INDEX", "Starting file index...");

            m_currFile = f;


            new MaterialDialog.Builder(m_ctx)
                .title(R.string.file_read)
                .progress(false, MAX_PROGRESS, false)
                .showListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            m_dialog = (MaterialDialog) dialog;
                            m_thr = new Thread(FileIndexer.this);
                            m_thr.start();
                        }
                    }).show();
        }

        // read back the file, update progress as it goes along.
        public void run() {
            long fileLen = m_currFile.length();

            // read file
            while(m_progress != MAX_PROGRESS) {

                m_dialog.setProgress(m_progress);
            }

            // dismiss dialog
            ((Activity)m_ctx).runOnUiThread(new Runnable() {
                    public void run() {
                        m_dialog.dismiss();
                    }
                });
            Log.d("INDEX", "Finished file index!");
        }
    }
}
