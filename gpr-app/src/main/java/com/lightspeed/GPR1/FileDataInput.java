package com.lightspeed.GPR1;

import com.lightspeed.gpr.lib.DataInputInterface;
import com.lightspeed.gpr.lib.Element;

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
    final static int ELEMENT_HEADER_LEN = 6;
    final static int TIMESTAMP_LEN = 7;
    final static byte TYPE_ELEMENT = 0;
    final static byte TYPE_TIMESTAMP = 1;

    File m_file;

    private ArrayList<Integer> m_elementIndex;
    private ArrayList<Integer> m_timestampIndex;

    InputUpdateCallback m_callback;

    WeakReference<Context> m_ctx;
    String m_inputName;

    public FileDataInput(Context ctx) {
	EventBus.getDefault().register(this);
        m_inputName =  ctx.getString(R.string.file);
        m_ctx = new WeakReference<Context>(ctx);

    }


    public int getCurrentIndex() {
        // get the index of the last element, don't know how useful it will be...
        if(m_elementIndex != null) {
            return m_elementIndex.get(m_elementIndex.size()-1);
        }
        return 0;

    }
    public boolean hasNext() {
        return false; // no more in the future, so return false.
    }

    public Element getNext() {
        return null;
    }

    // TODO: Buffering of the datastream so that we don't reopen the
    // file every single time
    public  Element getPrevious(int offset) {
        DataInputStream in = null;
        try {
            in = new DataInputStream(new BufferedInputStream(new FileInputStream(m_file)));
        } catch(FileNotFoundException e) {
            Log.e("FileDataInput","Cannot open the file!");
            return null;
            // TODO: handle
        } catch (Exception e) {
            Log.e("FileDataInput","General error!");
            return null;
        }


        // make sure that we don't try to read anything that doesn't exist.
        if(in == null || // make sure that we actually have the file open
           m_elementIndex == null ||
           offset < 0 ||
           offset > m_elementIndex.size()){
            if(m_elementIndex != null){
                Log.e("FileDataInput", "Error reading previous! " + (in == null) + " " +
                      (m_elementIndex == null) + " " + (offset < 0) + " " + (offset > m_elementIndex.size()));
            }
            return null;
        }
        // offset from the end of the file...
        int index = m_elementIndex.get(m_elementIndex.size()-1-offset);

        // seek to position
        try {
            in.skip(index);
        } catch(IOException e) {
            Log.e("FileDataInput", "IO exception when seeking!");
        }
        byte type;
        try {
            type = in.readByte();
        } catch(IOException e) {
            Log.e("FileDataInput", "Error reading type byte!");
            return null;
        }

        if(type != TYPE_ELEMENT) {
            Log.e("FileDataInput", "Seek'd to non-element! Expected: " + TYPE_ELEMENT +
                  " Recieved: " + type + " @ " + index);
            return null;
        }

        short start;
        short stop;
        byte bps;

        try {
            start = in.readShort(); // start of element
            stop = in.readShort(); // stop element
            bps = in.readByte(); // bytes per sample
        } catch(IOException e) {
            Log.e("FileDataInput", "Error reading element header!");
            return null;
        }

        Element el = new Element(start, stop);
        try{
            for(int i = start; i < stop; i++) {
                switch(bps) {
                case 1:
                    el.setSample(i, in.readByte());
                    break;
                case 2:
                    el.setSample(i, in.readShort());
                    break;
                case 4:
                    el.setSample(i, in.readInt());
                    break;
                case 8:
                    el.setSample(i, in.readDouble());
                    break;
                default:
                    Log.wtf("FileDataInput", "Invalid sample size!");
                    break;
                }
            }
        } catch (IOException e) {
            Log.e("FileDataInput", "IOException while reading samples!");
            return null;
        }

        Log.v("FileDataInput", "Get element " + offset + " @ " +
              index+" SUCCESS");

        // this should be handy?
        m_callback.updateInput();
        return el;
    }

    public void setUpdateCallback(InputUpdateCallback call) {
        m_callback = call;
    }

    public boolean open() {

	if(m_file == null) {
	    return false;
	}

        // index the file for us, should really do some callback, but idk.
        new FileIndexer(m_file);

        return true;
    }

    public void close() {
        //m_raf = null;
	EventBus.getDefault().unregister(this);
    }

    public String getName() {
        return m_inputName;
    }

    public void onEvent(FileDialog.FileChangedEvent e) {
        if(m_ctx.get() == null) {
            // what to do? Have to request new context or something
            // TODO:
            return;
        }
        Toast.makeText(m_ctx.get(), e.file.toString(),
                       Toast.LENGTH_SHORT).show();
        m_file = e.file;
        open();
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
            if(m_currFile == null ||
               m_ctx.get() == null) {
                // do nothing, wait for death.
                return;
            }

            // make the dialog so the user thinks that something is
            // actually going on...
            new MaterialDialog.Builder(m_ctx.get())
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
            int offset = 0;
            byte type = 0;
            Log.d("INDEX","Input file length: " + fileLen);
            ArrayList<Integer> locIndexList = new ArrayList<Integer>();
            ArrayList<Integer> locTimeStampList = new ArrayList<Integer>();
            DataInputStream in = null;
            try {
                in = new DataInputStream(new BufferedInputStream(new FileInputStream(m_currFile)));
            } catch(FileNotFoundException e) {
                // TODO: handle
            }


            // read in file, get indexes of files
            try{
                while(offset < fileLen) {
                    type = in.readByte();
                    switch(type) {
                    case TYPE_ELEMENT:
                        // add element to local list
                        locIndexList.add(offset);
                        //Log.d("INDEX","Element starting at " + offset);

                        // calculate how much data to skip
                        short start = in.readShort();
                        short stop = in.readShort();
                        short bytesPerSample = in.readByte();
                        in.skip((stop-start)*bytesPerSample);
                        offset += ELEMENT_HEADER_LEN + (stop-start)*bytesPerSample;
                        break;

                    case TYPE_TIMESTAMP:
                        // add to local timestamp list
                        locTimeStampList.add(offset);
                        //Log.d("INDEX","Timestamp starting at " + offset);
                        offset += TIMESTAMP_LEN + 1;
                        in.skip(TIMESTAMP_LEN);
                        break;

                    default:
                        //Log.e("INDEX","Unknown type byte found!");
                        // TODO: show user "invalid file" dialog or something
                        break;
                    }

                    // figure out if ui needs updating
                    if(m_progress != (int)(((double)offset/(double)fileLen)*MAX_PROGRESS)) {
                        Log.d("INDEX","Progress: " + (int)(((double)offset/(double)fileLen)*MAX_PROGRESS));
                        m_progress = (int)(((double)offset/(double)fileLen)*MAX_PROGRESS);
                        m_dialog.setProgress(m_progress);
                    }

                }
            }
            catch(EOFException e) {
                Log.v("INDEX","End of file reached!");
                // if I had to close the file, I would put the code here
            }
            catch(Exception e) {
                Log.e("INDEX","Generic error...");
                // TODO: proper handle...
            }

            try{
                // close the dialog, since the file is indexed
                ((Activity)m_ctx.get()).runOnUiThread(new Runnable() {
                        public void run() {
                            m_dialog.dismiss();
                        }
                    });
            } catch (NullPointerException e) {
                // Sheeeeiiiittttt. User rotated while we were
                // loading! How do we close the dialog now?
                Log.e("INDEX", "Config changed when trying to close dialog!");
            }

            // update the outer class so that we can actually parse the data
            FileDataInput.this.m_elementIndex = locIndexList;
            FileDataInput.this.m_timestampIndex = locTimeStampList;
            Log.d("INDEX", "Finished file index!");
        }
    }
}
