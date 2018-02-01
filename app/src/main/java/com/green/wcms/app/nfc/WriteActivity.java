package com.green.wcms.app.nfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.green.wcms.app.R;
import com.google.common.base.Charsets;
import com.google.common.primitives.Bytes;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;

public class WriteActivity extends Activity {

	private NfcAdapter nfcAdapter;
	private PendingIntent pendingIntent;

	public static final int TYPE_TEXT = 1;
	public static final int TYPE_URI = 2;

	EditText writeText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_write);

		if (savedInstanceState == null) {
		}

		writeText = (EditText) findViewById(R.id.writeText);
		writeText.setPrivateImeOptions("defaultInputmode=english;");

		// NFC 관련 객체 생성
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		Intent intent = new Intent(this, getClass())
				.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_write,
					container, false);
			return rootView;
		}
	}

	/************************************
	 * 여기서부턴 NFC 관련 메소드
	 ************************************/
	@Override
	protected void onPause() {
		if (nfcAdapter != null) {
			nfcAdapter.disableForegroundDispatch(this);
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (nfcAdapter != null) {
			nfcAdapter
					.enableForegroundDispatch(this, pendingIntent, null, null);
		}
	}

	// NFC 태그 스캔시 호출되는 메소드
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if (intent != null) {
			processTag(intent); // processTag 메소드 호출
			//키보드 내리기
			InputMethodManager imm= (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
		}

	}

	// onNewIntent 메소드 수행 후 호출되는 메소드
	private void processTag(Intent intent) {
		// EditText에 입력된 값을 가져옴
		String s = writeText.getText().toString();

		// 감지된 태그를 가리키는 객체
		Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

		// 아무것도 입력받지 않으면 태그에 쓰지 않음
		if (s.equals("")) {
			Toast.makeText(getApplicationContext(), "내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
		}

		// 입력받은 값을 감지된 태그에 씀
		else {
			NdefMessage message = createTagMessage(s, TYPE_TEXT);
			writeTag(message, detectedTag);
		}
	}

	// 감지된 태그에 NdefMessage를 쓰는 메소드
	public boolean writeTag(NdefMessage message, Tag tag) {
		int size = message.toByteArray().length;
		try {
			Ndef ndef = Ndef.get(tag);
			if (ndef != null) {
				ndef.connect();
				if (!ndef.isWritable()) {
					return false;
				}

				if (ndef.getMaxSize() < size) {
					return false;
				}

				ndef.writeNdefMessage(message);
				Toast.makeText(getApplicationContext(), "저장 되었습니다.", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "포맷되지 않은 태그이므로 먼저 포맷하고 데이터를 씁니다.",
						Toast.LENGTH_SHORT).show();

				NdefFormatable formatable = NdefFormatable.get(tag);
				if (formatable != null) {
					try {
						formatable.connect();
						formatable.format(message);
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}

				return false;
			}
		} catch (Exception ex) {
			ex.printStackTrace();

			return false;
		}

		return true;
	}

	/**
	 * Create a new tag message
	 * 
	 * @param msg
	 * @param type
	 * @return
	 */
	private NdefMessage createTagMessage(String msg, int type) {
		NdefRecord[] records = new NdefRecord[1];

		if (type == TYPE_TEXT) {
			records[0] = createTextRecord(msg, Locale.KOREAN, true);
		} else if (type == TYPE_URI) {
			records[0] = createUriRecord(msg.getBytes());
		}

		NdefMessage mMessage = new NdefMessage(records);

		return mMessage;
	}

	private NdefRecord createTextRecord(String text, Locale locale,
			boolean encodeInUtf8) {
		final byte[] langBytes = locale.getLanguage().getBytes(
				Charsets.US_ASCII);
		final Charset utfEncoding = encodeInUtf8 ? Charsets.UTF_8 : Charset
				.forName("UTF-16");
		final byte[] textBytes = text.getBytes(utfEncoding);
		final int utfBit = encodeInUtf8 ? 0 : (1 << 7);
		final char status = (char) (utfBit + langBytes.length);
		final byte[] data = Bytes.concat(new byte[] { (byte) status },
				langBytes, textBytes);

		String rtd = "application/first";
		return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT,
				new byte[0], data);
	}

	private NdefRecord createUriRecord(byte[] data) {
		return new NdefRecord(NdefRecord.TNF_ABSOLUTE_URI, NdefRecord.RTD_URI,
				new byte[0], data);
	}

}
