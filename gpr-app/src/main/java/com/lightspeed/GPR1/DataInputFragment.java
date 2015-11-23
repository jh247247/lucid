package com.lightspeed.GPR1;

import com.lightspeed.gpr.lib.AbstractDataInput;

import android.app.Activity;

import android.content.Context;
import android.widget.Spinner;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.view.ViewGroup;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.util.Log;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.Bind;

public class DataInputFragment extends Fragment {
    static final String SPINNER_POS_SAVE = "spinnerPos";
    static final String FILENAME_SAVE = "filename";

    @Bind(R.id.inputSpinner) Spinner m_inputSpinner;
    @Bind(R.id.inputOptionLayout) LinearLayout m_inputOption;

    AbstractDataInput m_input;

    View m_inputView; // view that the interface exposes

    public interface OnInputChangedListener {
	public void onInputChanged(AbstractDataInput input);
    }

    OnInputChangedListener m_inputCallback;

    @Override
    public void onAttach(Activity activity) {
	super.onAttach(activity);

	try {
	    m_inputCallback = (OnInputChangedListener) activity;
	}
	catch(Exception e) {
	    Log.e("DataInputFragment","Attached activity does not implement OnInputChangedListener!");
	}

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

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

        // reset the spinner
        if(savedInstanceState != null) {
            m_inputSpinner.setSelection(savedInstanceState.getInt(SPINNER_POS_SAVE,0));
        }

        setupInputUI(m_inputSpinner.getSelectedItemPosition());

        return ret;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("spinnerPos", m_inputSpinner.getSelectedItemPosition());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
	m_inputCallback = null;
	
        ButterKnife.unbind(this);
    }


    public void setupInputUI(int selection) {
        LayoutInflater inflater = (LayoutInflater)
            getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        m_inputView = null;

        m_inputOption.removeAllViews();
        if(m_input != null) {
            // this should make the inputs stop all associated threads
            // and whatever
            m_input.close();
        }


        switch(selection) {
        case 0: // should be bluetooth, is there a better way to do this?
            m_inputView = inflater.inflate(R.layout.bluetooth_input_ui,
                                           m_inputOption, false);
            m_inputOption.addView(m_inputView,0);
            try {
                m_input = new BluetoothDataInput(getActivity());
            } catch(Exception e) {
                Toast.makeText(getActivity(), "Cannot use bluetooth?", Toast.LENGTH_SHORT).show();
            }

            break;
        case 1: // should be file
            m_inputView = inflater.inflate(R.layout.file_input_ui,
                                           m_inputOption, false);
            m_inputOption.addView(m_inputView,0);

            Button b =
                ButterKnife.findById(m_inputView,R.id.file_select_button);
            b.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View v) {
                        try {
                            new FileDialog().show(getActivity());
                        }
                        catch (Exception e) {
                            Log.e("FILE_DIALOG", "CANNOT GET EXTERNAL DIRECTORY");
                        }
                    }
                });

            m_input = new FileDataInput(getActivity());
            break;
        case 2: // should be random (for now...)
            m_input = new RandomDataInput();
            break;
        default:
            // wtf.
            break;
        }

	if(m_inputCallback != null) {
	    m_inputCallback.onInputChanged(m_input);
	}


        // send new input to receivers

	if(m_input != null) {
	    // post event?
	    m_input.open();
	}
    }

    // public void onEvent(FileDialog.FileChangedEvent e) {
    //     TextView t = null;
    //     if(m_inputView != null) {
    //         t = ButterKnife.findById(m_inputView,R.id.file_select_text);
    //     }

    //     if(t != null) {
    //         // TODO: make this saved between switching interfaces?
    //         String f = e.file.toString();
    //         t.setText(f);
    //     }
    // }

    // /**
    //  * This object contains the new input type, sent to receivers.
    //  */
    // public class InputChangeEvent {
    //     public final DataInputInterface input;
    //     public InputChangeEvent(DataInputInterface in) {
    //         this.input = in;
    //     }
    // }
}
