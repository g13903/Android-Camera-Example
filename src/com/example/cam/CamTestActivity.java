package com.example.cam;

/**
 * @author Jose Davis Nidhin
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;



//androidアプリケーションはmainはない！
//androidアプリケーションはあらかじめ指定した一つのアクティビティが最初に作成されることでアプリケーションが起動
public class CamTestActivity extends Activity {//一つの画面につき一つのアクティビティクラスを定義
	private static final String TAG = "CamTestActivity";
	Preview preview;
	//Button buttonClick;
	Camera camera;
	Activity act;
	Context ctx;
	//Interface to global information about an application environment
	//アプリの状態を受け渡すためcontextを渡している
	//Object<-context<-ContextWraper<-ContextThemeWrapper<-Activityの継承関係

	@Override
	public void onCreate(Bundle savedInstanceState) {
		//onCreateはactivityに含まれる。superは大もとのactivityからひっぱっている
		super.onCreate(savedInstanceState);
		ctx = this;
		act = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//タイトルバーの非表示
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//フルスクリーン表示

		setContentView(R.layout.main);
		//アクティビティに部品を配置
		//「R.layout.main」はsetContentViewメソッドの引数に指定されている

		preview = new Preview(this, (SurfaceView)findViewById(R.id.surfaceView));
		preview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		((FrameLayout) findViewById(R.id.layout)).addView(preview);
		preview.setKeepScreenOn(true);
		//おそらく画面が落ちない機能？

		preview.setOnClickListener(new OnClickListener() {
			//setOnClickListner・・・クリックされた時の処理

			@Override
			public void onClick(View arg0) {
				//クリック時の処理
				camera.takePicture(shutterCallback, rawCallback, jpegCallback);
				//写真をとる
			}
		});

		Toast.makeText(ctx, getString(R.string.take_photo_help), Toast.LENGTH_LONG).show();

		//		buttonClick = (Button) findViewById(R.id.btnCapture);
		//		
		//		buttonClick.setOnClickListener(new OnClickListener() {
		//			public void onClick(View v) {
		////				preview.camera.takePicture(shutterCallback, rawCallback, jpegCallback);
		//				camera.takePicture(shutterCallback, rawCallback, jpegCallback);
		//			}
		//		});
		//		
		//	buttonClick.setOnLongClickListener(new OnLongClickListener(){
			//	@Override
				//	public boolean onLongClick(View arg0) {
					//	camera.autoFocus(new AutoFocusCallback(){
						//	@Override
							//public void onAutoFocus(boolean arg0, Camera arg1) {
								//camera.takePicture(shutterCallback, rawCallback, jpegCallback);
				//	}
					//	});
						//return true;
				//	}
	//			});
	}

	@Override
	protected void onResume() {
		super.onResume();
		int numCams = Camera.getNumberOfCameras();
		if(numCams > 0){
			try{
				camera = Camera.open(0);
				camera.startPreview();
				preview.setCamera(camera);
			} catch (RuntimeException ex){
				Toast.makeText(ctx, getString(R.string.camera_not_found), Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	protected void onPause() {
		if(camera != null) {
			camera.stopPreview();
			preview.setCamera(null);
			camera.release();
			camera = null;
		}
		super.onPause();
	}

	private void resetCam() {
		camera.startPreview();
		preview.setCamera(camera);
	}

	private void refreshGallery(File file) {
		Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		mediaScanIntent.setData(Uri.fromFile(file));
		sendBroadcast(mediaScanIntent);
	}

	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			//			 Log.d(TAG, "onShutter'd");
		}
	};

	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			//			 Log.d(TAG, "onPictureTaken - raw");
		}
	};

	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			new SaveImageTask().execute(data);
			resetCam();
			Log.d(TAG, "onPictureTaken - jpeg");
		}
	};

	private class SaveImageTask extends AsyncTask<byte[], Void, Void> {

		@Override
		protected Void doInBackground(byte[]... data) {
			FileOutputStream outStream = null;

			// Write to SD Card
			try {
				File sdCard = Environment.getExternalStorageDirectory();
				File dir = new File (sdCard.getAbsolutePath() + "/camtest");
				dir.mkdirs();				

				String fileName = String.format("%d.jpg", System.currentTimeMillis());
				File outFile = new File(dir, fileName);

				outStream = new FileOutputStream(outFile);
				outStream.write(data[0]);
				outStream.flush();
				outStream.close();

				Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length + " to " + outFile.getAbsolutePath());

				refreshGallery(outFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
			}
			return null;
		}

	}
}


