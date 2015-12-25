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

import com.picsart.studio.gifencoder.GifEncoder;

/**
 * Created by Arman on 9/7/15.
 */
public class MainActivity extends Activity{
	GifEncoder encoder = null;
	Bitmap bmp = null;
	Bitmap bmmp = null;
	Canvas canvas;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(new TextView(this));
		  encoder =  new GifEncoder() ;
		BitmapFactory.Options ops =  new BitmapFactory.Options();
		ops.inMutable = true;

		bmp = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath()+"/vvvvv.jpg");
		bmmp = Bitmap.createScaledBitmap(bmp, 1024, 1024, false);
		canvas = new Canvas(bmmp);
		encoder.init(Environment.getExternalStorageDirectory().getAbsolutePath() + "/testingGIF.gif", bmmp.getWidth(), bmmp.getHeight(), 256, 10, 8);
		//encoder.init(bmp.getWidth(), bmp.getHeight(), 5, null);


		Log.i("Btm resolution", bmmp.getWidth() + "  x " + bmmp.getHeight());


	}

	@Override
	protected void onResume() {
		super.onResume();

		int[] pixels = new int[bmmp.getByteCount()];
		for (int i = 0; i < 11; i++) {

			canvas.drawBitmap(bmp, 0, 0, new Paint());
			canvas.rotate(90);

			bmmp.getPixels(pixels,0,bmmp.getWidth(),0,0,bmmp.getWidth(),bmmp.getHeight());
			encoder.addFrame(pixels);
		}

		encoder.close();

	}







}
