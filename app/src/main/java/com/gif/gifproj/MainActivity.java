package com.gif.gifproj;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import com.encoder.Encoder;

import java.io.File;

/**
 * Created by Arman on 9/7/15.
 */
public class MainActivity extends Activity{
	Encoder encoder = null;
	Bitmap bmp = null;
	Bitmap bmmp = null;
	Canvas canvas;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(new TextView(this));
		 encoder = new Encoder();
		BitmapFactory.Options ops =  new BitmapFactory.Options();
		ops.inMutable = true;

		bmp = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath()+"/vvvvv.png",new BitmapFactory.Options());
		bmmp = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath()+"/vvvvv.png",ops);
		canvas = new Canvas(bmmp);
		encoder.init(bmp.getWidth(), bmp.getHeight(), 5, null);

		encoder.startVideoGeneration(new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/testing.webm"));

		Log.i("Btm resolution", bmp.getWidth() + "  x " + bmp.getHeight());


	}

	@Override
	protected void onResume() {
		super.onResume();


		for (int i = 0; i < 30; i++) {
			try {
				Thread.currentThread().sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			canvas.drawBitmap(bmp,0,0,new Paint());
			encoder.addFrame(bmp,200);

		}

		encoder.endVideoGeneration();

	}







}
