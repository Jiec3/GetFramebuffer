
package com.getpic;

import java.io.File;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.syouquan.script.ColorEngine;

public class GetPictureFromFrameBufferActivity extends Activity {
    /** Called when the activity is first created. */
    static {
        System.loadLibrary("getPic");
    }

    public ImageView iv1;

    public TextView tv;

    private static final String sFilePath = Environment.getExternalStorageDirectory()
            + File.separator + "framebuffer_temp.bmp";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE); // (NEW)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); // (NEW)

        setContentView(R.layout.main);

        Log.e("test", Build.MODEL);

        iv1 = (ImageView) findViewById(R.id.tx1);
        tv = (TextView) findViewById(R.id.textView1);

        CMDUtils.runWithRoot("chmod 777 /dev/graphics/fb0");

        final Button bt1 = (Button) findViewById(R.id.button1);
        bt1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                ColorEngine.nativeScreenShot(sFilePath);
                iv1.setImageBitmap(BitmapFactory.decodeFile(sFilePath));
            }
        });

    }
}
