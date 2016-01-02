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
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.os.Bundle;
import android.os.PersistableBundle;

import android.content.DialogInterface;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.afollestad.materialdialogs.internal.MDTintHelper;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem;
import com.afollestad.materialdialogs.util.DialogUtils;

import butterknife.ButterKnife;
import butterknife.Bind;

import com.google.common.util.concurrent.ListenableFuture;

public class FileInputFragment
    extends Fragment
    implements FileDialog.FileDialogCallback {
    static final String FILE_PATH = "FILE_PATH";

    DataInputFragment.OnInputChangedListener m_inputCallback;

    @Bind(R.id.file_select_button) Button m_selectBtn;
    @Bind(R.id.file_select_text) TextView m_fileText;



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            m_inputCallback = (DataInputFragment.OnInputChangedListener) activity;
        }
        catch(Exception e) {
            Log.e("FileInputFragment","Attached activity does not implement OnInputChangedListener!");
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        View ret = inflater.inflate(R.layout.file_input_ui,
                                    container, false);
        ButterKnife.bind(this,ret);

        m_selectBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if(getActivity() == null) {
                        Log.e("FileInputFragment",
                              "Fragment detached while attempting to create dialog!");
                    }
                    try {
                        new FileDialog(FileInputFragment.this).show(getActivity());
                    }
                    catch(Exception e) {
                        Log.e("FileInputFragment",""+e);
                    }

                }
            });

        return ret;
    }

    @Override
    public void onActivityCreated(Bundle inState) {
	super.onActivityCreated(inState);
		Log.d("BUNDLE","RESTORING STATE OF FILE FRAGMENT: " + inState);
	if(inState != null) {
	    m_fileText.setText(inState.getString(FILE_PATH,"FIXME"));
	}
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
	super.onSaveInstanceState(outState);
	Log.d("BUNDLE","SAVING STATE OF FILE FRAGMENT");
	outState.putString(FILE_PATH, m_fileText.getText().toString());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        m_inputCallback = null;
        ButterKnife.unbind(this);
    }

    public void onFileSelection(File f) {
        m_fileText.setText(f.getAbsolutePath());

        // set the new input while it is indexing...
        m_inputCallback.onInputChanged(new GprFileReader(f,new FileIndexerDialog()));
    }

    // // TODO: Buffering of the datastream so that we don't reopen the
    // // file every single time
    // public  ListenableFuture<Element> getElement(int index) {
    //  return null; // TODO

    //     // DataInputStream in = null;
    //     // try {
    //  //     Log.v("FileDataInput", "File path: " + m_path);
    //     //     in = new DataInputStream(new BufferedInputStream(new
    //  //                                                   FileInputStream(new File(m_path))));

    //  // } catch(FileNotFoundException e) {
    //     //     Log.e("FileDataInput","Cannot open the file!");
    //     //     return null;
    //     //     // TODO: handle
    //     // } catch (Exception e) {
    //     //     Log.e("FileDataInput","General error: " + e);
    //     //     return null;
    //     // }


    //     // // make sure that we don't try to read anything that doesn't exist.
    //     // if(in == null || // make sure that we actually have the file open
    //     //    m_elementIndex == null ||
    //     //    index < 0 ||
    //     //    index > m_elementIndex.size()){
    //     //     if(m_elementIndex != null){
    //     //         Log.e("FileDataInput", "Error reading previous! " + (in == null) + " " +
    //     //               (m_elementIndex == null) + " " + (index < 0) + " " + (index > m_elementIndex.size()));
    //     //     }
    //     //     return null;
    //     // }

    //     // // seek to position
    //     // try {
    //     //     in.skip(index);
    //     // } catch(IOException e) {
    //     //     Log.e("FileDataInput", "IO exception when seeking!");
    //     // }
    //     // byte type;
    //     // try {
    //     //     type = in.readByte();
    //     // } catch(IOException e) {
    //     //     Log.e("FileDataInput", "Error reading type byte!");
    //     //     return null;
    //     // }

    //     // if(type != TYPE_ELEMENT) {
    //     //     Log.e("FileDataInput", "Seek'd to non-element! Expected: " + TYPE_ELEMENT +
    //     //           " Recieved: " + type + " @ " + index);
    //     //     return null;
    //     // }

    //     // short start;
    //     // short stop;
    //     // byte bps;

    //     // try {
    //     //     start = in.readShort(); // start of element
    //     //     stop = in.readShort(); // stop element
    //     //     bps = in.readByte(); // bytes per sample
    //     // } catch(IOException e) {
    //     //     Log.e("FileDataInput", "Error reading element header!");
    //     //     return null;
    //     // }

    //     // Element el = new Element(start, stop);
    //     // try{
    //     //     for(int i = start; i < stop; i++) {
    //     //         switch(bps) {
    //     //         case 1:
    //     //             el.setSample(i, in.readByte());
    //     //             break;
    //     //         case 2:
    //     //             el.setSample(i, in.readShort());
    //     //             break;
    //     //         case 4:
    //     //             el.setSample(i, in.readInt());
    //     //             break;
    //     //         case 8:
    //     //             el.setSample(i, in.readDouble());
    //     //             break;
    //     //         default:
    //     //             Log.wtf("FileDataInput", "Invalid sample size!");
    //     //             break;
    //     //         }
    //     //     }
    //     // } catch (IOException e) {
    //     //     Log.e("FileDataInput", "IOException while reading samples!");
    //     //     return null;
    //     // }

    //     // Log.v("FileDataInput", "Get element " + index + " @ " +
    //     //       index+" SUCCESS");

    //     // return el;
    // }

    // public boolean open() {

    //  if(m_path == null) {
    //      return false;
    //  }

    //     // index the file for us, should really do some callback, but idk.
    //     new GprFileReader(m_path);

    //     return true;
    // }

    // public void close() {
    //     //m_raf = null;

    // }

    // public String getName() {
    //     return m_inputName;
    // }

    // public void onEvent(FileDialog.FileChangedEvent e) {
    //     if(m_ctx.get() == null) {
    //         // what to do? Have to request new context or something
    //         // TODO:
    //         return;
    //     }
    //     Toast.makeText(m_ctx.get(), e.file.toString(),
    //                    Toast.LENGTH_SHORT).show();
    //     m_path = e.file.getAbsolutePath();
    //     open();
    // }


    private class FileIndexerDialog
        implements GprFileReader.FileIndexProgressListener {
        MaterialDialog m_dialog;

        public FileIndexerDialog() {
            Log.d("INDEX", "Starting file index...");
            if(getActivity() == null) {
                // fragment has been detached!
                Log.e("INDEX", "Fragment detached when creating dialog!");
                return;
            }

            // make the dialog so the user thinks that something is
            // actually going on...
            new MaterialDialog.Builder(getActivity())
                .title(R.string.file_read)
                .progress(false, GprFileReader.MAX_PROGRESS, false)
                .showListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            m_dialog = (MaterialDialog) dialog;
                        }
                    }).show();
        }

        public void onFileIndexProgress(int progress) {
            if(m_dialog != null) {
                m_dialog.setProgress(progress);
            }
            // if the indexer finishes, close the dialog.
            if(progress >= GprFileReader.MAX_PROGRESS) {
                m_dialog.dismiss();
            }
        }
    }
}
