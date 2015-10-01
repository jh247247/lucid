package com.lightspeed.GPR1;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FileDialog extends DialogFragment implements MaterialDialog.ListCallback{
    private File m_parentFolder;
    private File[] m_parentContents;
    private boolean m_canGoUp;

    /**
     * Callbacks for dialog we are hosting.
     */
    private final MaterialDialog.ButtonCallback m_buttonCallback = new
        MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog d) {
                d.dismiss();
            }

            @Override
            public void onNegative(MaterialDialog d) {
                d.dismiss();
            }
        };

    /**
     * Interface for callback once file is selected
     */
    public interface FileDialogCallback {
        void onFileSelection(File f);
    }

    public FileDialog() throws FileNotFoundException{
        m_parentFolder = Environment.getExternalStorageDirectory();
        m_parentContents = m_parentFolder.listFiles(); // TODO:
	if(m_parentContents == null) {
	    // not allowed to read external directory for some reason...
	    throw new FileNotFoundException();
	}
	m_canGoUp = m_parentFolder.getParent() != null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new MaterialDialog.Builder(getActivity())
            .title(m_parentFolder.getAbsolutePath())
            .items(getContentsArray())
            .itemsCallback(this)
            .callback(m_buttonCallback)
            .autoDismiss(false)
            .negativeText(android.R.string.cancel)
            .build();
    }

    String[] getContentsArray() {
	String[] results = new String[m_parentContents.length + (m_canGoUp ? 1 : 0)];
	if (m_canGoUp) results[0] = "..";
	for (int i = 0; i < m_parentContents.length; i++)
	    results[m_canGoUp ? i + 1 : i] = m_parentContents[i].getName();
	return results;
    }

    @Override
    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence s) {
	if (m_canGoUp && i == 0) {
	    m_parentFolder = m_parentFolder.getParentFile();
	    m_canGoUp = m_parentFolder.getParent() != null;
	} else {
	    File curr = m_parentContents[m_canGoUp ? i-1 : i];
	    if(!curr.isDirectory()){
		//EventBus.getDefault().post(new FileChangedEvent(curr));
		materialDialog.dismiss();
		return;
	    } else {
		m_parentFolder = m_parentContents[m_canGoUp ? i - 1 : i];
		m_canGoUp = true;
	    }

	}
	m_parentContents = m_parentFolder.listFiles();
	MaterialDialog dialog = (MaterialDialog) getDialog();
	dialog.setTitle(m_parentFolder.getAbsolutePath());
	dialog.setItems(getContentsArray());
    }

    @Override
    public void onAttach(Activity activity) {
	super.onAttach(activity);
    }

    public void show(FragmentActivity context) {
	show(context.getSupportFragmentManager(), "FILE_SELECTOR");
    }

    private static class FolderSorter implements Comparator<File> {
	@Override
	public int compare(File lhs, File rhs) {
	    return lhs.getName().compareTo(rhs.getName());
	}
    }

    // object to pass as a message
    // public class FileChangedEvent {
    // 	final public File file;
    // 	public FileChangedEvent(File f) {
    // 	    this.file = f;
    // 	}
    // }
}
