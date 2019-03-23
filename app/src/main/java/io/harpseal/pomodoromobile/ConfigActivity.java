package io.harpseal.pomodoromobile;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;

public class ConfigActivity extends PreferenceActivity {
    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState); //addPreferencesFromResource(R.xml.preferences);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            View view = findViewById(android.R.id.content);
//            int flags = view.getSystemUiVisibility();
//            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
//            view.setSystemUiVisibility(flags);
//            this.getWindow().setStatusBarColor(Color.WHITE);
//        }

        getFragmentManager().beginTransaction().replace(android.R.id.content, new ConfigFragment()).commit();
    }

}
