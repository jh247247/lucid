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
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
										this,
										drawerLayout,
										toolbar,
										R.string.open,
										R.string.close
										)

	    {
		public void onDrawerClosed(View view)
		{
		    super.onDrawerClosed(view);
		    invalidateOptionsMenu();
		    syncState();
		}

		public void onDrawerOpened(View drawerView)
		{
		    super.onDrawerOpened(drawerView);
		    invalidateOptionsMenu();
		    syncState();
		}
	    };
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //Set the custom toolbar
        if (toolbar != null){
	    toolbar.getBackground().setAlpha(50);
            setSupportActionBar(toolbar);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        actionBarDrawerToggle.syncState();
    }
}
