
package com.getpic;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.syouquan.script.ColorEngine;
import com.syouquan.script.ColorHelper;

public class GetPictureFromFrameBufferActivity extends Activity {
    /** Called when the activity is first created. */
    static {
        System.loadLibrary("getPic");
    }

    public TextView tx1;

    int i = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        tx1 = (TextView) findViewById(R.id.tx1);

        CMDUtils.runWithRoot("chmod 777 /dev/graphics/fb0");

        DisplayMetrics dm = new DisplayMetrics();
        dm = getApplicationContext().getResources().getDisplayMetrics();
        final int width = dm.widthPixels;
        final int height = dm.heightPixels;

        final int colors[] = {
                R.color.common_gray_lighter, R.color.common_red, R.color.common_hint_color,
                R.color.usercenter_common_background
        };
        final Button bt1 = (Button) findViewById(R.id.bt1);
        bt1.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                tx1.setBackgroundColor(getResources().getColor(colors[(i++) % 4]));

                tx1.setText("color"
                        + ColorHelper.getColor(GetPictureFromFrameBufferActivity.this, width / 2,
                                height / 2));

                ColorEngine.nativeScreenShot("/sdcard/temp.bmp");

            }
        });

    }
}
