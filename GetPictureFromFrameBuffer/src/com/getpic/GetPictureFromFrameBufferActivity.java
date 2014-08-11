
package com.getpic;

import java.io.IOException;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

        try {
            Runtime.getRuntime().exec(new String[] {
                    "su", "-c", "chmod 777 /dev/graphics/fb0"
            });
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        DisplayMetrics dm = new DisplayMetrics();
        dm = getApplicationContext().getResources().getDisplayMetrics();
        final int width = dm.widthPixels;
        final int height = dm.heightPixels;

        final int colors[] = {
                Color.BLACK, Color.BLUE, Color.GRAY, Color.WHITE
        };
        final Button bt1 = (Button) findViewById(R.id.bt1);
        bt1.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                tx1.setBackgroundColor(colors[(i++) % 4]);

                tx1.setText("color"
                        + ColorHelper.getColor(GetPictureFromFrameBufferActivity.this, width / 2,
                                height / 2));

            }
        });

    }
}
