package com.lightspeed.GPR1;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.widget.LinearLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.MenuItem;

import butterknife.ButterKnife;
import butterknife.Bind;


public class MainActivity extends AppCompatActivity {
    @Bind(R.id.drawer) DrawerLayout m_drawerLayout;
    @Bind(R.id.toolbar) Toolbar m_toolbar;

    ActionBarDrawerToggle m_abtog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        // expand the bindings
        ButterKnife.bind(this);

	// setup the drawer listener
	setupDrawerListener();

	// make the toolbar (actionbar) transparent, so content shows behind
	m_toolbar.getBackground().setAlpha(50);
	m_toolbar.setTitle(""); // make the title blank
	    setSupportActionBar(m_toolbar); // set our toolbar as the toolbar

	    // show the hamburger
	    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

	    // set the hamburger to the current state of the drawer
	    m_abtog.syncState();
    }

    private void setupDrawerListener() {
	// make the listener
        m_abtog = new ActionBarDrawerToggle(this,
                                            m_drawerLayout,
                                            m_toolbar,
                                            R.string.open,
                                            R.string.close){
                @Override
                public void onDrawerClosed(View view)
                {
                    super.onDrawerClosed(view);
                    invalidateOptionsMenu();
                    syncState();
                }

                @Override
                public void onDrawerOpened(View drawerView)
                {
                    super.onDrawerOpened(drawerView);
                    invalidateOptionsMenu();
                    syncState();
                }
            };
        // set the listener to the one we just made
        m_drawerLayout.setDrawerListener(m_abtog);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle
        // If it returns true, then it has handled
        // the nav drawer indicator touch event
        if (m_abtog.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }
}
