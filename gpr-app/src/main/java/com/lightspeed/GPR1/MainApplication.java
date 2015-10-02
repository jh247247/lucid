package com.lightspeed.GPR1;

import android.app.Application;
import android.os.StrictMode;
import com.squareup.leakcanary.LeakCanary;
import android.util.Log;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.GINGERBREAD;

public class MainApplication extends Application {

  @Override public void onCreate() {
    super.onCreate();
    enabledStrictMode();
    LeakCanary.install(this);
    Log.wtf("APPLICATION","CREATED");
  }

  private void enabledStrictMode() {
    if (SDK_INT >= GINGERBREAD) {
      StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder() //
          .detectAll() //
          .penaltyLog() //
          .penaltyDeath() //
          .build());
    }
  }
}
