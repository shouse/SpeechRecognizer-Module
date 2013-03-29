/**
 * 
 * 
 */
package jp.isisredirect.speechrecognizer;

import java.util.ArrayList;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.AsyncResult;
import org.appcelerator.kroll.common.TiMessenger;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.Log;
import org.appcelerator.titanium.util.TiConfig;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import org.appcelerator.titanium.TiLifecycle.OnLifecycleEvent;

// This proxy can be created by calling Speechrecognizer.createExample({message: "hello world"})
@Kroll.proxy(creatableInModule = SpeechrecognizerModule.class)
public class SpeechRecognizerProxy extends KrollProxy implements
		RecognitionListener, OnLifecycleEvent {

	// Standard Debugging variables
	private static final String LCAT = "SpeechrecognizerProxy";
	private static final boolean DBG = TiConfig.LOGD;

	private static final String UNSUPPORTED_GOOGLE_RESULTS_CONFIDENCE = "com.google.android.voicesearch.UNSUPPORTED_PARTIAL_RESULTS_CONFIDENCE";
	private static final String UNSUPPORTED_GOOGLE_RESULTS = "com.google.android.voicesearch.UNSUPPORTED_PARTIAL_RESULTS";

	private int action = SpeechrecognizerModule.RECOGNIZE;
	/*
	 * direct mode noes not use private String prompt = null;
	 */
	private String lang_tag = null;
	private Integer max_result = 1;
	private boolean free_formOrNot = true;
	private boolean partial_resultOrNot = true;
	private boolean websearchonlyOrNot = false;
	private String origin = null;
	private boolean secure = true;

	SpeechRecognizer mSpeechRecognizer = null;

	// Constructor
	public SpeechRecognizerProxy() {
		super();
		TiApplication.getInstance().getRootActivity()
				.addOnLifecycleEventListener(this);
	}

	private static final int MSG_CREATESPEECHRECOGNIZER = 60000;
	private static final int MSG_STARTSPEECHRECOGNIZER = 60001;
	private static final int MSG_CANCELSPEECHRECOGNIZER = 60002;
	private static final int MSG_STOPSPEECHRECOGNIZER = 60003;
	private final Handler handler = new Handler(TiMessenger.getMainMessenger()
			.getLooper(), new Handler.Callback() {
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_CREATESPEECHRECOGNIZER: {
				AsyncResult result = (AsyncResult) msg.obj;
				createSpeechRecognizer();
				result.setResult(null);
				return true;
			}
			case MSG_STARTSPEECHRECOGNIZER: {
				AsyncResult result = (AsyncResult) msg.obj;
				startRecognize();
				result.setResult(null);
				return true;
			}
			case MSG_CANCELSPEECHRECOGNIZER: {
				AsyncResult result = (AsyncResult) msg.obj;
				cancelRecognize();
				result.setResult(null);
				return true;
			}
			case MSG_STOPSPEECHRECOGNIZER: {
				AsyncResult result = (AsyncResult) msg.obj;
				stopRecognize();
				result.setResult(null);
				return true;
			}
			}
			return false;
		}

	});

	protected void createSpeechRecognizer() {
		Log.d(LCAT, "createSpeechRecognizer");
		mSpeechRecognizer = SpeechRecognizer
				.createSpeechRecognizer(TiApplication.getInstance());
		mSpeechRecognizer.setRecognitionListener(SpeechRecognizerProxy.this);
	}

	protected void startRecognize() {
		Log.d(LCAT, "startRecognize");
		Intent intent = null;
		switch (this.getAction()) {
		case SpeechrecognizerModule.RECOGNIZE:
			intent = getRecognizeSpeechIntent();
			break;
		case SpeechrecognizerModule.WEBSERACH:
			intent = getWebSearchIntent();
			break;
		case SpeechrecognizerModule.HANDSFREE:
			intent = getVoiceSearchHandsFree();
			break;
		}
		if (intent != null) {
			mSpeechRecognizer.startListening(intent);
		}
	}

	protected void cancelRecognize() {
		Log.d(LCAT, "cancelRecognize");
		mSpeechRecognizer.cancel();
	}

	protected void stopRecognize() {
		Log.d(LCAT, "stopRecognize");
		mSpeechRecognizer.stopListening();
	}

	// Handle creation options
	@Override
	public void handleCreationDict(KrollDict options) {
		super.handleCreationDict(options);

		/*
		 * if (options.containsKey(SpeechrecognizerModule.PROMPT)) {
		 * setPrompt((String) options.get(SpeechrecognizerModule.PROMPT)); }
		 * else { setPrompt(null); }
		 */
		if (options.containsKey(SpeechrecognizerModule.LANGTAG)) {
			setLangtag((String) options.get(SpeechrecognizerModule.LANGTAG));
		} else {
			setLangtag(null);
		}
		if (options.containsKey(SpeechrecognizerModule.MAXRESULT)) {
			setMaxresult((Integer) options
					.get(SpeechrecognizerModule.MAXRESULT));
		} else {
			setMaxresult(1);
		}
		if (options.containsKey(SpeechrecognizerModule.FREEFORM)) {
			setFreeform((Boolean) options.get(SpeechrecognizerModule.FREEFORM));
		} else {
			setFreeform(true);
		}
		if (options.containsKey(SpeechrecognizerModule.PARTIALRESULT)) {
			setPartialresult((Boolean) options
					.get(SpeechrecognizerModule.PARTIALRESULT));
		} else {
			setPartialresult(true);
		}
		if (options.containsKey(SpeechrecognizerModule.WEBSEARCHONLY)) {
			setWebsearchonly((Boolean) options
					.get(SpeechrecognizerModule.WEBSEARCHONLY));
		} else {
			setWebsearchonly(false);
		}
		if (options.containsKey(SpeechrecognizerModule.ORIGIN)) {
			setOrigin((String) options.get(SpeechrecognizerModule.ORIGIN));
		} else {
			setOrigin(null);
		}
		if (options.containsKey(SpeechrecognizerModule.SECURE)) {
			setSecure((Boolean) options.get(SpeechrecognizerModule.SECURE));
		} else {
			setSecure(true);
		}
		if (!TiApplication.isUIThread()) {
			TiMessenger.sendBlockingMainMessage(handler
					.obtainMessage(MSG_CREATESPEECHRECOGNIZER));
		} else {
			createSpeechRecognizer();
		}

	}

	@Override
	public void onResume(Activity arg0) {
	}

	@Override
	public void onStart(Activity arg0) {
	}

	@Override
	public void onPause(Activity arg0) {
	}

	@Override
	public void onStop(Activity arg0) {
		release();
	}

	@Override
	public void onDestroy(Activity arg0) {
	}

	private Intent getRecognizeSpeechIntent() {
		Log.d(LCAT, "getRecognizeSpeechIntent");
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		if (getIsFreeform()) {
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
					RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		} else {
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
					RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
		}
		/*
		 * if (getPrompt() != null) {
		 * intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getPrompt()); }
		 */
		if (getLangtag() != null) {
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, getLangtag());
		}
		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, getMaxresult());

		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, TiApplication
				.getInstance().getPackageName());
		return intent;
	}

	private Intent getWebSearchIntent() {
		Log.d(LCAT, "getWebSearchIntent");
		Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
		if (getIsFreeform()) {
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
					RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		} else {
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
					RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
		}
		/*
		 * if (getPrompt() != null) {
		 * intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getPrompt()); }
		 */
		if (getLangtag() != null) {
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, getLangtag());
		}
		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, getMaxresult());

		intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,
				getIsPartialresult());
		if (Build.VERSION.SDK_INT >= 11) {// API Level 11 EXTRA_WEB_SEARCH_ONLY
			intent.putExtra(RecognizerIntent.EXTRA_WEB_SEARCH_ONLY,
					getIsWebsearchonly());
		}
		if (Build.VERSION.SDK_INT >= 14) {// API Level 14 EXTRA_ORIGIN
			if (getOrigin() != null) {
				intent.putExtra(RecognizerIntent.EXTRA_ORIGIN, getOrigin());
			}
		}
		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, TiApplication
				.getInstance().getPackageName());
		return intent;
	}

	private Intent getVoiceSearchHandsFree() {
		Log.d(LCAT, "getVoiceSearchHandsFree");
		if (Build.VERSION.SDK_INT >= 16) {
			Intent intent = new Intent(
					RecognizerIntent.ACTION_VOICE_SEARCH_HANDS_FREE);
			intent.putExtra(RecognizerIntent.EXTRA_SECURE, getIsSecure());
			intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
					TiApplication.getInstance().getPackageName());
			return intent;
		}
		return null;
	}

	// Methods
	@Kroll.method
	public void release() {
		Log.d(LCAT, "release");
		if (mSpeechRecognizer != null) {
			mSpeechRecognizer.destroy();
			mSpeechRecognizer = null;
		}
	}

	@Kroll.method
	public void start() {
		Log.v(LCAT, "start");
		if (mSpeechRecognizer != null) {
			if (!TiApplication.isUIThread()) {
				TiMessenger.sendBlockingMainMessage(handler
						.obtainMessage(MSG_STARTSPEECHRECOGNIZER));
			} else {
				startRecognize();
			}
		}
	}

	@Kroll.method
	public void cancel() {
		Log.v(LCAT, "cancel");
		if (mSpeechRecognizer != null) {
			if (!TiApplication.isUIThread()) {
				TiMessenger.sendBlockingMainMessage(handler
						.obtainMessage(MSG_CANCELSPEECHRECOGNIZER));
			} else {
				cancelRecognize();
			}
		}
	}

	@Kroll.method
	public void stop() {
		Log.v(LCAT, "stop");
		if (mSpeechRecognizer != null) {
			if (!TiApplication.isUIThread()) {
				TiMessenger.sendBlockingMainMessage(handler
						.obtainMessage(MSG_STOPSPEECHRECOGNIZER));
			} else {
				stopRecognize();
			}
		}
	}

	// implements RecognitionListener
	@Override
	public void onReadyForSpeech(Bundle params) {
		Log.v(LCAT, "onReadyForSpeech");
		/*
		 * params :
		 * http://developer.android.com/reference/android/speech/RecognitionListener
		 * .html#onReadyForSpeech(android.os.Bundle) there is no information in
		 * document.
		 */
		KrollDict data = new KrollDict();
		data.put(TiC.EVENT_PROPERTY_SOURCE, SpeechRecognizerProxy.this);
		data.put(TiC.PROPERTY_TYPE, SpeechrecognizerModule.READYFORSPEECH);
		fireEvent(SpeechrecognizerModule.READYFORSPEECH, data);
	}

	@Override
	public void onBeginningOfSpeech() {
		Log.v(LCAT, "onBeginningOfSpeech");
		KrollDict data = new KrollDict();
		data.put(TiC.EVENT_PROPERTY_SOURCE, SpeechRecognizerProxy.this);
		data.put(TiC.PROPERTY_TYPE, SpeechrecognizerModule.BEGINNINGOFSPEECH);
		fireEvent(SpeechrecognizerModule.BEGINNINGOFSPEECH, data);
	}

	@Override
	public void onBufferReceived(byte[] buffer) {
		Log.v(LCAT, "onBufferReceived");
		KrollDict data = new KrollDict();
		data.put(TiC.EVENT_PROPERTY_SOURCE, SpeechRecognizerProxy.this);
		data.put(TiC.PROPERTY_TYPE, SpeechrecognizerModule.BUFFERRECEIVED);
		fireEvent(SpeechrecognizerModule.BUFFERRECEIVED, data);
	}

	@Override
	public void onRmsChanged(float rmsdB) {
		// Log.v(LCAT, "onRmsChanged");
		KrollDict data = new KrollDict();
		data.put(TiC.EVENT_PROPERTY_SOURCE, SpeechRecognizerProxy.this);
		data.put(SpeechrecognizerModule.RMSDB, rmsdB);
		data.put(TiC.PROPERTY_TYPE, SpeechrecognizerModule.RMSCHANGED);
		fireEvent(SpeechrecognizerModule.RMSCHANGED, data);
	}

	@Override
	public void onEndOfSpeech() {
		Log.v(LCAT, "onEndOfSpeech");
		KrollDict data = new KrollDict();
		data.put(TiC.EVENT_PROPERTY_SOURCE, SpeechRecognizerProxy.this);
		data.put(TiC.PROPERTY_TYPE, SpeechrecognizerModule.ENDOFSPEECH);
		fireEvent(SpeechrecognizerModule.ENDOFSPEECH, data);
	}

	@Override
	public void onError(int error) {
		Log.v(LCAT, "onError : " + error);
		/*
		 * switch (error) { case SpeechRecognizer.ERROR_AUDIO: break; case
		 * SpeechRecognizer.ERROR_CLIENT: break; case
		 * SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: break; case
		 * SpeechRecognizer.ERROR_NETWORK: Log.e(LCAT, "network error"); break;
		 * case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: Log.e(LCAT,
		 * "network timeout"); break; case SpeechRecognizer.ERROR_NO_MATCH:
		 * break; case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: break; case
		 * SpeechRecognizer.ERROR_SERVER: break; case
		 * SpeechRecognizer.ERROR_SPEECH_TIMEOUT: break; default: break; }
		 */
		KrollDict data = new KrollDict();
		data.put(TiC.EVENT_PROPERTY_SOURCE, SpeechRecognizerProxy.this);
		data.put(TiC.PROPERTY_TYPE, SpeechrecognizerModule.ERROR);
		data.put(SpeechrecognizerModule.ERROR, error);
		fireEvent(SpeechrecognizerModule.ERROR, data);
	}

	@Override
	public void onEvent(int eventType, Bundle params) {
		Log.v(LCAT, "onEvent");
		/*
		 * http://developer.android.com/reference/android/speech/RecognitionListener
		 * .html#onEvent(int, android.os.Bundle) there is no information
		 */
		KrollDict data = new KrollDict();
		data.put(TiC.EVENT_PROPERTY_SOURCE, SpeechRecognizerProxy.this);
		data.put(TiC.PROPERTY_TYPE, SpeechrecognizerModule.EVENT);
		fireEvent(SpeechrecognizerModule.EVENT, data);
	}

	@Override
	public void onPartialResults(Bundle partialResults) {
		Log.v(LCAT, "onPartialResults");
		String[] heard = null;
		float[] scores = null;
		if (partialResults != null) {
			if (partialResults.containsKey(UNSUPPORTED_GOOGLE_RESULTS)) {
				heard = partialResults
						.getStringArray(UNSUPPORTED_GOOGLE_RESULTS);
				scores = partialResults
						.getFloatArray(UNSUPPORTED_GOOGLE_RESULTS_CONFIDENCE);
			} else if (partialResults
					.containsKey(SpeechRecognizer.RESULTS_RECOGNITION)) {
				heard = partialResults.getStringArrayList(
						SpeechRecognizer.RESULTS_RECOGNITION).toArray(
						new String[0]);
				if (Build.VERSION.SDK_INT >= 14) {
					scores = partialResults
							.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
				}
			}
		}
		KrollDict data = getResultKrollDict(heard, scores);
		data.put(TiC.EVENT_PROPERTY_SOURCE, SpeechRecognizerProxy.this);
		data.put(TiC.PROPERTY_TYPE, SpeechrecognizerModule.PARTIALRESULTS);
		fireEvent(SpeechrecognizerModule.PARTIALRESULTS, data);
	}

	public KrollDict getResultKrollDict(String[] heard, float[] scores) {
		KrollDict data = new KrollDict();
		data.put(TiC.EVENT_PROPERTY_SOURCE, SpeechRecognizerProxy.this);
		if (heard != null) {
			String getData = new String();
			for (String s : heard) {
				getData += s + ",";
			}
			data.put(SpeechrecognizerModule.RESULTS, getData);
		}
		if (scores != null) {
			data.put(SpeechrecognizerModule.CONFIDENCE_SCORES, scores);
		}
		return data;
	}

	@Override
	public void onResults(Bundle results) {
		Log.v(LCAT, "onResults");
		String[] heard = null;
		float[] scores = null;
		if (results != null) {
			if (results.containsKey(SpeechRecognizer.RESULTS_RECOGNITION)) {
				ArrayList<String> resultsrecog = results
						.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
				if (resultsrecog != null) {
					heard = results.getStringArrayList(
							SpeechRecognizer.RESULTS_RECOGNITION).toArray(
							new String[0]);
					if (Build.VERSION.SDK_INT >= 14) {
						scores = results
								.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
					}
				}
			}
		}
		KrollDict data = getResultKrollDict(heard, scores);
		data.put(TiC.EVENT_PROPERTY_SOURCE, SpeechRecognizerProxy.this);
		data.put(TiC.PROPERTY_TYPE, SpeechrecognizerModule.RESULTS);
		fireEvent(SpeechrecognizerModule.RESULTS, data);

	}

	/*
	 * direct mode dose not use
	 * 
	 * @Kroll.setProperty
	 * 
	 * @Kroll.method public void setPrompt(String prompt) { this.prompt =
	 * prompt; }
	 * 
	 * @Kroll.getProperty
	 * 
	 * @Kroll.method public String getPrompt() { return prompt; }
	 */
	@Kroll.setProperty
	@Kroll.method
	public void setLangtag(String lang_tag) {
		this.lang_tag = lang_tag;
	}

	@Kroll.getProperty
	@Kroll.method
	public String getLangtag() {
		return lang_tag;
	}

	@Kroll.setProperty
	@Kroll.method
	public void setMaxresult(Integer max_result) {
		this.max_result = max_result;
	}

	@Kroll.getProperty
	@Kroll.method
	public Integer getMaxresult() {
		return max_result;
	}

	@Kroll.setProperty
	@Kroll.method
	public void setFreeform(boolean free_formOrNot) {
		this.free_formOrNot = free_formOrNot;
	}

	@Kroll.getProperty
	@Kroll.method
	public boolean getIsFreeform() {
		return free_formOrNot;
	}

	@Kroll.setProperty
	@Kroll.method
	public void setPartialresult(boolean partial_resultOrNot) {
		this.partial_resultOrNot = partial_resultOrNot;
	}

	@Kroll.getProperty
	@Kroll.method
	public boolean getIsPartialresult() {
		return partial_resultOrNot;
	}

	@Kroll.setProperty
	@Kroll.method
	public void setWebsearchonly(boolean websearchonlyOrNot) {
		this.websearchonlyOrNot = websearchonlyOrNot;
	}

	@Kroll.getProperty
	@Kroll.method
	public boolean getIsWebsearchonly() {
		return websearchonlyOrNot;
	}

	@Kroll.setProperty
	@Kroll.method
	public void setOrigin(String origin) {
		this.origin = origin;
	}

	@Kroll.getProperty
	@Kroll.method
	public String getOrigin() {
		return origin;
	}

	@Kroll.setProperty
	@Kroll.method
	public void setAction(int action) {
		this.action = action;
	}

	@Kroll.getProperty
	@Kroll.method
	public int getAction() {
		return action;
	}

	@Kroll.setProperty
	@Kroll.method
	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	@Kroll.getProperty
	@Kroll.method
	public boolean getIsSecure() {
		return secure;
	}

}