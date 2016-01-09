package com.gecko.gpr.ui;

import com.gecko.gpr.R;

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
import com.gecko.gpr.input.RandomDataInput;

public class DataInputFragment extends Fragment {

    static final String SPINNER_POS_SAVE = "spinnerPos";

    @Bind(R.id.inputSpinner) Spinner m_inputSpinner;
    int m_prevSpinnerPos;

    InputInterfaceHandler m_inputInterfaceHandler;


    public interface OnInputChangedListener {
        public void onInputChanged(AbstractDataInput input);
    }

    public interface InputInterfaceHandler {
        public void setInputInterface(Fragment f);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            m_inputInterfaceHandler = (DataInputFragment.InputInterfaceHandler) activity;
        }
        catch(Exception e) {
            Log.e("DataInputFragment","Attached activity does not implement InputInterfaceHandler!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate layout...
        View ret = inflater.inflate(R.layout.input_selector_view,
                                    container, false);

        ButterKnife.bind(this,ret);

        // get back the previous spinner position, so that
        // reinstantiating it does not cause a new fragment to be created.
        if(savedInstanceState != null) {
            m_prevSpinnerPos = savedInstanceState.getInt(SPINNER_POS_SAVE,0);
        } else {
            m_prevSpinnerPos = 0;
        }

        setupInputUI(m_prevSpinnerPos);

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
                    // if the selected position is the same as
                    // previously selected, don't do anything.
                    if(m_prevSpinnerPos != position) {
                        // otherwise set the input interface to
                        // whatever is expected.
                        Log.d("SPINNER","Input changed to: " + position);
                        setupInputUI(position);
                        m_prevSpinnerPos = position;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Another interface callback
                }
            });

        return ret;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SPINNER_POS_SAVE, m_prevSpinnerPos);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ButterKnife.unbind(this);
    }


    public void setupInputUI(int selection) {
        if(m_inputInterfaceHandler == null) {
            // what can we actually do now??
            return;
        }
        Fragment tmpInput = null;
        switch(selection) {
        case 0: // should be bluetooth, is there a better way to do this?
            tmpInput = new BluetoothInputFragment();
            break;

        case 1: // should be file
            tmpInput = new FileInputFragment();
            break;

        case 2: // should be random (for now...)
            RandomDataInput rin = new RandomDataInput();
            rin.open();
            if(getActivity() != null) {
                ((OnInputChangedListener)getActivity()).onInputChanged(rin);
            }
            break;

        default:
            // wtf.
            break;
        }

        m_inputInterfaceHandler.setInputInterface(tmpInput);

    }
}
