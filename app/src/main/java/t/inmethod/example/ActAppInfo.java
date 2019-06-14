package t.inmethod.example;


import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import t.inmethod.viewdesign.R;

public class ActAppInfo extends Activity {

    TextView testview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.app_info);
        //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.menu_main_title);
        testview = (TextView) findViewById(R.id.textAppInfo);

        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            testview.setText(extra.getCharSequence("AppInfo"));
        }
    }

}
