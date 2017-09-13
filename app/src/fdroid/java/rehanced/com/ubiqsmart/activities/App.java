package rehanced.com.ubiqsmart.activities;

import android.support.multidex.MultiDexApplication;
import rehanced.com.ubiqsmart.R;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class App extends MultiDexApplication {

  private boolean isGooglePlayBuild = false;

  @Override public void onCreate() {
    super.onCreate();
    onCreateCalligraphy();
  }

  private void onCreateCalligraphy() {
    CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("Walkway_Bold.ttf").setFontAttrId(R.attr.fontPath).build());
  }

  public boolean isGooglePlayBuild() {
    return isGooglePlayBuild;
  }

  public void track(String s) {
    return;
  }

  public void event(String s) {
    return;
  }

}