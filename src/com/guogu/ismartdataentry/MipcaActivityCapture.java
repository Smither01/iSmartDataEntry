package com.guogu.ismartdataentry;

import java.io.IOException;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.mining.app.zxing.camera.CameraManager;
import com.mining.app.zxing.decoding.CaptureActivityHandler;
import com.mining.app.zxing.decoding.InactivityTimer;
import com.mining.app.zxing.view.ViewfinderView;

/**
 * Initial the camera
 * 
 * @author Ryan.Tang
 */
public class MipcaActivityCapture extends Activity implements
		Callback, OnClickListener {

	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private RelativeLayout relTitle;

	/**
	 * @author Smither
	 * @function if devicesType is camera
	 */
	private String devicesType;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.add_device_barcodescanner);
		// ViewUtil.addTopView(getApplicationContext(), this,
		// R.string.scan_card);
		/**
		 * @author Smither
		 * @function save the devicesType
		 */
//		devicesType = getIntent().getStringExtra("type");
		
		CameraManager.init(getApplication());
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		relTitle = (RelativeLayout) findViewById(R.id.rel_title);
		Button input = (Button) findViewById(R.id.btn_input);
		TextView textInput = (TextView) findViewById(R.id.text_input);
		Button cancle = (Button) findViewById(R.id.btn_cancle);
		ImageButton mButtonBack = (ImageButton) findViewById(R.id.backBtn);

/*		*//**
		 * @author Smither
		 * if type is camera  show the input view
		 *//*
		if ((getIntent().getStringExtra("type") == null)
				||( !getIntent().getStringExtra("type").equals("RGBLIGHT")
				&& !getIntent().getStringExtra("type").equals(
						Constant.CAMERA_FLAG))) {
			textInput.setVisibility(View.GONE);
			input.setVisibility(View.GONE);
		}*/

		input.setOnClickListener(this);
		cancle.setOnClickListener(this);
		mButtonBack.setOnClickListener(this);

		relTitle.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {

					@Override
					public void onGlobalLayout() {
						// TODO Auto-generated method stub
						Rect frame = new Rect();
						getWindow().getDecorView()
								.getWindowVisibleDisplayFrame(frame);
						int statusBarHeight = frame.top;
						CameraManager.get().setStatusBarHeight(statusBarHeight);
					}
				});

		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.backBtn:
		case R.id.btn_cancle:
			finish();
			break;
		case R.id.btn_input:
			/**
			 * @author Smither 2014/9/10
			 * @funtion if devices type is camera
			 */
		/*	if (devicesType.equals(Constant.CAMERA_FLAG)) {
				
				Intent intent = new Intent(this,
						AddCameraDevicesActivity.class);
				intent.putExtra("from", AddCameraDevicesActivity.FROM_OTHER);
				startActivity(intent);
			} else {
				Intent intent = new Intent(this,
						ManuallyEnterCodeActivity.class);
				intent.putExtra("type", getIntent().getStringExtra("type"));
				startActivity(intent);
			}
			finish();*/
			break;
		default:
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();


	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	/**
	 * 处理扫描结果
	 * @param result
	 * @param barcode
	 */
	public void handleDecode(Result result, Bitmap barcode) {
		inactivityTimer.onActivity();
		playBeepSoundAndVibrate();
		String resultString = result.getText();
		Log.v("LZP", "resultString:"+resultString);
		if (resultString.equals("")) {
			Toast.makeText(MipcaActivityCapture.this, "Scan failed!",
					Toast.LENGTH_SHORT).show();
		} else {
			Intent resultIntent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putString("result", resultString);
			bundle.putString("devicesType", devicesType);
			// bundle.putParcelable("bitmap", barcode);
			// 不建议把图片传过去，因为不能超过40k
			resultIntent.putExtras(bundle);
			this.setResult(RESULT_OK, resultIntent);
		}
		MipcaActivityCapture.this.finish();
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats,
					characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(
					R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private void playBeepSoundAndVibrate() {

		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		InteractionUtils.vibrator(this);
		// if (vibrate) {
		// Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		// vibrator.vibrate(VIBRATE_DURATION);
		// }
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};

}