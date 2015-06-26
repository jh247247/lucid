package com.lightspeed.GPR1;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;
import android.content.Context;
import a.Simple;

public class MainActivity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
	Context ctx = getApplicationContext();
        Toast t = Toast.makeText(ctx, "Test: " + Simple.add(2,3),
                                 Toast.LENGTH_SHORT);
        t.show();
    }
}
