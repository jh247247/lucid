package com.lightspeed.GPR1;

import com.lightspeed.gpr.lib.DataInputInterface;

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
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.widget.Spinner;
import android.widget.Toast;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import butterknife.ButterKnife;
import butterknife.Bind;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    @Bind(R.id.drawer) DrawerLayout m_drawerLayout;
    @Bind(R.id.toolbar) Toolbar m_toolbar;
    @Bind(R.id.render) RenderView m_render;
    @Bind(R.id.left_drawer) LinearLayout m_leftDrawer;

    DataInputFragment m_inputManager;

    ActionBarDrawerToggle m_abtog;

    // fragment to retain data in, totally kinda stolen from example
    // on the internet
    RetainFragment m_retained;
    private static final String TAG_RETAIN_FRAGMENT = "retain_fragment";

    static final int TOOLBAR_MAX_ALPHA = 255;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);


        // expand the bindings
        ButterKnife.bind(this);

        // setup the drawer listener
        setupDrawerListener();

        // make the toolbar (actionbar) transparent, so content shows behind
        m_toolbar.getBackground().setAlpha(0); // TODO: fix magic number
        m_toolbar.setTitle(""); // make the title blank
        setSupportActionBar(m_toolbar); // set our toolbar as the toolbar

        // show the hamburger
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // set the hamburger to the current state of the drawer
        m_abtog.syncState();

        FragmentManager fm = getSupportFragmentManager();

        // setup the data input manager/fragment thing
        m_inputManager = (DataInputFragment)
            fm.findFragmentById(R.id.input_manager);

        // get back retained vars if required
        m_retained = (RetainFragment)
            fm.findFragmentByTag(TAG_RETAIN_FRAGMENT);

        // first start, fragment does not exist!
        if(m_retained == null) {
	    Log.w("MainActivity","Have to create retained fragment!");
	    m_retained = new RetainFragment();
            fm.beginTransaction().add(m_retained,
                                      TAG_RETAIN_FRAGMENT).commit();

        }

        m_render.start(m_retained.getBlitter());
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
                    ActivityCompat.invalidateOptionsMenu(MainActivity.this);
                    syncState();
                }

                @Override
                public void onDrawerOpened(View drawerView)
                {
                    super.onDrawerOpened(drawerView);
                    ActivityCompat.invalidateOptionsMenu(MainActivity.this);
                    syncState();
                }

                @Override
                public void onDrawerSlide(View drawerView, float slideOffset)
                {
                    super.onDrawerSlide(drawerView,slideOffset);
                    m_toolbar.getBackground().setAlpha((int)(slideOffset*TOOLBAR_MAX_ALPHA));
                }

            };
        // set the listener to the one we just made
        m_drawerLayout.setDrawerListener(m_abtog);
        m_drawerLayout.setScrimColor(Color.TRANSPARENT);
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
