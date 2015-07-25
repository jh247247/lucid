package com.lightspeed.GPR1;

import com.lightspeed.gpr.lib.DataInputInterface;

import android.app.Activity;

import android.content.Context;
import android.widget.Spinner;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.view.ViewGroup;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;

import butterknife.ButterKnife;
import butterknife.Bind;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.afollestad.materialdialogs.ThemeSingleton;
import com.afollestad.materialdialogs.internal.MDTintHelper;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem;
import com.afollestad.materialdialogs.util.DialogUtils;

public class DataInputFragment extends Fragment {
    @Bind(R.id.inputSpinner) Spinner m_inputSpinner;
    @Bind(R.id.inputOptionLayout) LinearLayout m_inputOption;

    DataInputManagerCallback m_callback;

    @Override
    public View onCreateView(LayoutInflater inflater,
			     ViewGroup container,
			     Bundle savedInstanceState) {
        // inflate layout...
        View ret = inflater.inflate(R.layout.input_selector_view,
                                    container, false);

        ButterKnife.bind(this,ret);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter =
            ArrayAdapter.createFromResource(getActivity(),
                                            R.array.input_options,
                                            android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        m_inputSpinner.setAdapter(adapter);

        m_inputSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView,
                                           View selectedItemView,
                                           int position,
                                           long id) {
                    setupInputUI(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Another interface callback
                }
            });

        // TODO: get back last setting...

        setupInputUI(m_inputSpinner.getSelectedItemPosition());
        return ret;
    }

    @Override
    public void onDestroyView() {
	super.onDestroyView();
	ButterKnife.unbind(this);
    }

    public void setupInputUI(int selection) {
        LayoutInflater inflater = (LayoutInflater)
            getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = null;

	DataInputInterface newIn = null;

	m_inputOption.removeAllViews();

        switch(selection) {
        case 0: // should be bluetooth, is there a better way to do this?
            v = inflater.inflate(R.layout.bluetooth_input_ui,
                                 m_inputOption, false);
            m_inputOption.addView(v,0);
            break;
        case 1: // should be file
            v = inflater.inflate(R.layout.file_input_ui,
                                 m_inputOption, false);
            m_inputOption.addView(v,0);

            Button b =
            ButterKnife.findById(v,R.id.file_select_button);
	    b.setOnClickListener(new View.OnClickListener(){
		    public void onClick(View v) {
			new MaterialDialog.Builder(getActivity())
			    .content("WOO TEST")
			    .positiveText("YEAH")
			    .negativeText("NAH")
			    .show();
		    }
		});
		
	    break;
        case 2: // should be random (for now...)
	    newIn = new RandomDataInput();
	    break;
        default:
            // wtf.
            break;
        }
	if(m_callback != null) {
	    m_callback.updateDataInput(newIn);
	}
    }

    public void setDataManagerCallback(DataInputManagerCallback call){
	m_callback = call;
    }

    // when the ui gets updated, this gets called so that the rest of
    // the app has an idea that something happened.
    public interface DataInputManagerCallback {
        public void updateDataInput(DataInputInterface in);
    }
}
