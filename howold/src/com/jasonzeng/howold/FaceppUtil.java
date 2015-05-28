package com.jasonzeng.howold;

import java.io.ByteArrayOutputStream;

import org.json.JSONObject;

import android.graphics.Bitmap;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

public class FaceppUtil {

	public interface CallBack {
		void success(JSONObject json);

		void fail(FaceppParseException ex);

	}

	public static JSONObject detect(Bitmap bm)
	{
		
		HttpRequests requests = new HttpRequests(Constant.KEY, Constant.SECRET,
				true, true);

		Bitmap bmSmall = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
				bm.getHeight());
		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		bmSmall.compress(Bitmap.CompressFormat.JPEG, 100, stream);

		byte[] bytes = stream.toByteArray();

		PostParameters params = new PostParameters();

		params.setImg(bytes);

		try {
			return requests.detectionDetect(params);
		} catch (FaceppParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;

			
		}

		
	}
}
