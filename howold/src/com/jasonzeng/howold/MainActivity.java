package com.jasonzeng.howold;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facepp.error.FaceppParseException;
import com.jasonzeng.howold.FaceppUtil.CallBack;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	private static final int PICK_CODE = 0X110;
	protected static final int MSG_SUCCESS = 0X111;
	protected static final int MSG_ERROR = 0x112;
	private ImageView ivPhoto = null;
	private Button btnImg = null;
	private Button btnDetect = null;
	private TextView tvTip = null;
	private View flWaitting = null;
	private String mCurrentPhotoStr;
	private Bitmap mPhotoImg;
	private Button btnAuthor = null;

	private ProgressBar pb = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		initEvent();
	}

	private void initView() {
		ivPhoto = (ImageView) findViewById(R.id.iv_photo);
		btnImg = (Button) findViewById(R.id.btn_img);
		btnAuthor = (Button) findViewById(R.id.btn_author);
		btnDetect = (Button) findViewById(R.id.btn_detect);
		tvTip = (TextView) findViewById(R.id.tv_tip);
		flWaitting = findViewById(R.id.fl_waitting);
	}

	private void initEvent() {
		btnImg.setOnClickListener(this);
		btnDetect.setOnClickListener(this);
		btnAuthor.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
	private void prepareResBitmap(JSONObject json) {
		// TODO Auto-generated method stub
		Bitmap bitmap = Bitmap.createBitmap(mPhotoImg.getWidth(),
				mPhotoImg.getHeight(), mPhotoImg.getConfig());
		Canvas canvas = new Canvas(bitmap);

		canvas.drawBitmap(mPhotoImg, 0, 0, null);

		try {
			JSONArray faces = json.getJSONArray("face");

			int faceCount = faces.length();

			tvTip.setText("find:"+faceCount);

			for (int i = 0; i < faceCount; i++) {
				JSONObject face = faces.getJSONObject(i);
				JSONObject posObj = face.getJSONObject("position");
				double x = posObj.getJSONObject("center").getDouble("x");

				double y = posObj.getJSONObject("center").getDouble("y");

				double w = posObj.getDouble("width");
				double h = posObj.getDouble("height");

				x = x / 100 * bitmap.getWidth();
				y = y / 100 * bitmap.getHeight();

				w = w / 100 * bitmap.getWidth();
				h = h / 100 * bitmap.getHeight();

				Paint mPaint = new Paint();
				mPaint.setColor(0xffffffff);
				mPaint.setStrokeWidth(2);

				canvas.drawLine((float) (x - w / 2), (float) (y - h / 2),
						(float) (x - w / 2), (float) (y + h / 2), mPaint);
				canvas.drawLine((float) (x - w / 2), (float) (y - h / 2),
						(float) (x + w / 2), (float) (y - h / 2), mPaint);
				canvas.drawLine((float) (x + w / 2), (float) (y - h / 2),
						(float) (x + w / 2), (float) (y + h / 2), mPaint);
				canvas.drawLine((float) (x - w / 2), (float) (y + h / 2),
						(float) (x + w / 2), (float) (y + h / 2), mPaint);

				int age = face.getJSONObject("attribute").getJSONObject("age")
						.getInt("value");
				String gender = face.getJSONObject("attribute")
						.getJSONObject("gender").getString("value");
	
				
				Bitmap ageBitmap = buildAgeMap(age, "Male".equals(gender));

				int ageWidth = ageBitmap.getWidth();
				int ageHeight = ageBitmap.getHeight();

				if (bitmap.getWidth() < ivPhoto.getWidth()
						&& bitmap.getHeight() < ivPhoto.getHeight()) {

					float ratio = Math.max(
							bitmap.getWidth() * 1.0f / ivPhoto.getWidth(),
							bitmap.getHeight() * 1.0f / ivPhoto.getHeight());

					ageBitmap = Bitmap.createScaledBitmap(ageBitmap,
							(int) (ageWidth * ratio),
							(int) (ageHeight * ratio), false);

				}

				canvas.drawBitmap(ageBitmap,
						(float) (x - ageBitmap.getWidth() / 2), (float) (y - h
								/ 2f - ageBitmap.getHeight()), null);

				mPhotoImg = bitmap;

			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private Bitmap buildAgeMap(int age, boolean isMale) {

		TextView tvHint = (TextView) flWaitting.findViewById(R.id.tv_hint);
		tvHint.setText(age + "");
		tvHint.setCompoundDrawablesWithIntrinsicBounds(getResources()
				.getDrawable(isMale ? R.drawable.male : R.drawable.female),
				null, null, null);
		
		tvHint.setDrawingCacheEnabled(true);
		Bitmap bitmap = Bitmap.createBitmap(tvHint.getDrawingCache());

		tvHint.destroyDrawingCache();

		return bitmap;
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.btn_img:
			Intent intent = new Intent(Intent.ACTION_PICK);
			intent.setType("image/*");
			startActivityForResult(intent, PICK_CODE);

			break;
		case R.id.btn_detect:

			flWaitting.setVisibility(View.VISIBLE);

			if (mCurrentPhotoStr != null && !mCurrentPhotoStr.trim().equals("")) {

				resizePhoto();
			} else {
				mPhotoImg = BitmapFactory.decodeResource(getResources(),
						R.drawable.defaultphoto);

			}
			new MyAsyncTask().execute(mPhotoImg);
			
			break;
		case R.id.btn_author:
			Toast.makeText(this, "Created by JasonZeng", 3000).show();

			break;
		}
	}

	public class MyAsyncTask extends AsyncTask<Bitmap, Integer, JSONObject> {

		protected void onPreExecute() {
			super.onPreExecute();
			// 在onPreExecute()中我们让ProgressDialog显示出来

		}

		@Override
		protected JSONObject doInBackground(Bitmap... arg0) {
			// TODO Auto-generated method stub
			try {
				return FaceppUtil.detect(arg0[0]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		protected void onPostExecute(JSONObject json) {
			super.onPostExecute(json);
			
			flWaitting.setVisibility(View.GONE);
			if (json != null) {
				prepareResBitmap(json);

				ivPhoto.setImageBitmap(mPhotoImg);
				

			}
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub

		if (requestCode == PICK_CODE) {
			if (data != null) {
				Uri uri = data.getData();
				Cursor cursor = getContentResolver().query(uri, null, null,
						null, null);
				cursor.moveToFirst();

				int idx = cursor
						.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
				mCurrentPhotoStr = cursor.getString(idx);

				cursor.close();

				resizePhoto();

				ivPhoto.setImageBitmap(mPhotoImg);
				tvTip.setText("Click Detect=>");

			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 压缩图片
	 * */
	private void resizePhoto() {
		// TODO Auto-generated method stub

		BitmapFactory.Options options = new Options();
		options.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(mCurrentPhotoStr, options);

		double ratio = Math.max(options.outWidth * 1.0d / 1024f,
				options.outHeight * 1.0d / 1024f);
		options.inSampleSize = (int) Math.ceil(ratio);
		options.inJustDecodeBounds = false;
		mPhotoImg = BitmapFactory.decodeFile(mCurrentPhotoStr, options);
	}

}
