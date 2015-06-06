package io.github.zkhan93.sharingtext;

import io.github.zkhan93.sharingtext.util.Constants;
import io.github.zkhan93.sharingtext.util.Utility;

import java.util.Hashtable;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class FragmentInfo extends Fragment {
	TextView tlocal, tserver;
	static SharedPreferences spf;
	public static final String TAG = "io.github.zkhan93.sharingtext.FragmentInfo";
	private static Context context;
	private static int lport;
	private static int sport;
	public static boolean updating;
	private static ImageView QRCode;
	private static Bitmap QRCodeBitmapImage;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		context = getActivity().getApplicationContext();
		View rootView = inflater.inflate(R.layout.fragment_info, container,
				false);
		spf = getActivity().getSharedPreferences(
				getString(R.string.pref_filename), Context.MODE_PRIVATE);
		tlocal = (TextView) rootView.findViewById(R.id.textViewLocal);
		tserver = (TextView) rootView.findViewById(R.id.textViewServer);
		QRCode = (ImageView) rootView.findViewById(R.id.imageViewQRCode);
		setHasOptionsMenu(true);
		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_info, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_refresh) {
			Utility.log(TAG, "in menu otions");
			updateIP();
			return true;
		} else {
			return false;
		}
	}

	public void updateIP() {
		Utility.log(TAG, "in update ip");
		if (!updating) {

			// Animation animrotate = AnimationUtils.loadAnimation(
			// getApplicationContext(), R.anim.rotate);
			// view.startAnimation(animrotate);

			updating = true;
			new SetIp().execute();
		} else {
			Toast.makeText(context, getString(R.string.updating),
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		((MainActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
		((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		new SetIp().execute();
	}

	private void updateTextViews() {
		tlocal.setText(Utility.getIpAddress() + Constants.COLON + lport);
		tserver.setText(
		// context.getString(R.string.textviewserver)+
		spf.getString(getActivity().getString(R.string.pref_ip), null)
				+ Constants.COLON + sport);
		QRCode.setImageBitmap(QRCodeBitmapImage);
	}

	private static final int WHITE = 0xFFFFFFFF;
	private static final int BLACK = 0xFF000000;

	public class SetIp extends AsyncTask<Void, Void, String> {
		@Override
		protected String doInBackground(Void... params) {
			try {
				lport = spf.getInt(context.getString(R.string.pref_port),
						Constants.DEF_PORT);
				sport = spf.getInt(context.getString(R.string.pref_sport),
						Constants.DEF_PORT);
				
				String data=Utility.getIpAddress()+Constants.COLON+lport;
				QRCodeWriter qrCodeWriter = new QRCodeWriter();
				int size = 300;
				Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
				hintMap.put(EncodeHintType.ERROR_CORRECTION,
						ErrorCorrectionLevel.L);
				BitMatrix byteMatrix = qrCodeWriter.encode(data,
						BarcodeFormat.QR_CODE, size, size, hintMap);
				int width = byteMatrix.getWidth();
				int height = byteMatrix.getHeight();
				int[] pixels = new int[width * height];
				for (int y = 0; y < height; y++) {
					int offset = y * width;
					for (int x = 0; x < width; x++) {
						pixels[offset + x] = byteMatrix.get(x, y) ? BLACK
								: WHITE;
					}
				}
				QRCodeBitmapImage = Bitmap.createBitmap(width, height,
						Bitmap.Config.ARGB_8888);
				QRCodeBitmapImage.setPixels(pixels, 0, width, 0, 0, width,
						height);

			} catch (Exception e) {
				Utility.log(TAG, e.getLocalizedMessage());
			}
			return Utility.getIpAddress();

		}

		@Override
		protected void onPostExecute(String result) {
			
			updateTextViews();
			updating = false;
			Log.d("msg", "done ip update");

		}
	}
}
