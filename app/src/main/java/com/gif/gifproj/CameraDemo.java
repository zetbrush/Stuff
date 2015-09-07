package com.gif.gifproj;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraDemo extends Activity {
	private static final String TAG = "FrontCamera";
	Camera camera;
	Preview preview;
	Button buttonClick;
	int stillCount = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		preview = new Preview(this);
		((FrameLayout) findViewById(R.id.preview)).addView(preview);

		buttonClick = (Button) findViewById(R.id.buttonClick);
		buttonClick.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				preview.camera.takePicture(shutterCallback, rawCallback,
						jpegCallback);
				buttonClick.setEnabled(false);
			}
		});
		File fold = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/giftemp");
		if (!fold.exists())
			fold.mkdir();

		Log.d(TAG, "onCreate'd");
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (camera != null) {
			camera.release();
		}
	}







	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			Log.d(TAG, "onShutter'd");
		}
	};

	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d(TAG, "onPictureTaken - raw with data = " + ((data != null) ? data.length : " NULL"));
		}
	};

	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, final Camera camera) {
			FileOutputStream outStream = null;
			try {
				// write to local sandbox file system
				// outStream =
				// CameraDemo.this.openFileOutput(String.format("%d.jpg",
				// System.currentTimeMillis()), 0);
				// Or write to sdcard
				outStream = new FileOutputStream(String.format(
						Environment.getExternalStorageDirectory().getAbsolutePath()+"/giftemp/still%d.jpg",
						stillCount));
				outStream.write(data);
				outStream.close();
				Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {




			}
			Log.d(TAG, "onPictureTaken - jpeg");
			try {
				stillCount++;
				camera.startPreview();
				if (stillCount < 10) {
					try{
						Thread.currentThread().sleep(250);
					}catch (Exception e){
						e.printStackTrace();
					}
					preview.camera.takePicture(shutterCallback, rawCallback,
							jpegCallback);
				} else {
					stillCount = 0;
					buttonClick.setEnabled(true);
					Bundle b = new Bundle();
					b.putInt("key", 1);
					Intent intent = new Intent(CameraDemo.this,MainActiv.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
					intent.putExtra("bundle",b);
					startActivity(intent);
					finish();


				}
			} catch (Exception e) {
				Log.d(TAG, "Error starting preview: " + e.toString());
			}
		}
	};

}
