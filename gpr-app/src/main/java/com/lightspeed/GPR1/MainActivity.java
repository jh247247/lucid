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
    @Bind(R.id.drawer) DrawerLayout drawerLayout;
    @Bind(R.id.toolbar) Toolbar toolbar;

    ActionBarDrawerToggle m_abtog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_my);

	ButterKnife.bind(this);

	m_abtog = new ActionBarDrawerToggle(this,
					    drawerLayout,
					    toolbar,
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
	drawerLayout.setDrawerListener(m_abtog);


	toolbar.getBackground().setAlpha(50);
	setSupportActionBar(toolbar);

	getSupportActionBar().setDisplayHomeAsUpEnabled(true);

	m_abtog.syncState();
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
