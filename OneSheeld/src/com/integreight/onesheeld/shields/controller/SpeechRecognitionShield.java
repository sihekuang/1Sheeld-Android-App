package com.integreight.onesheeld.shields.controller;

import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.SpeechRecognizer;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.utils.SpeechRecognition.RecognitionEventHandler;
import com.integreight.onesheeld.shields.controller.utils.SpeechRecognitionService;
import com.integreight.onesheeld.utils.Log;

public class SpeechRecognitionShield extends
		ControllerParent<SpeechRecognitionShield> {
	private SpeechRecognitionService mSpeechRecognitionService;
	private RecognitionEventHandler eventHandler;
	private static final byte SEND_RESULT = 0x01;
	private static final byte SEND_ERROR = 0x02;

	public SpeechRecognitionShield() {
		super();
	}

	public SpeechRecognitionShield(Activity activity, String tag) {
		super(activity, tag);
	}

	private ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mSpeechRecognitionService = ((SpeechRecognitionService.LocalBinder) service)
					.getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mSpeechRecognitionService = null;
		}

	};

	@Override
	public ControllerParent<SpeechRecognitionShield> setTag(String tag) {
		activity.bindService(new Intent(activity,
				SpeechRecognitionService.class), mServiceConnection,
				Context.BIND_AUTO_CREATE);
		System.out.println("int AUDIO=" + SpeechRecognizer.ERROR_AUDIO
				+ ",NETWORK=" + SpeechRecognizer.ERROR_NETWORK
				+ ",NETWORK_TIMEOUT=" + SpeechRecognizer.ERROR_NETWORK_TIMEOUT
				+ ",NO_MATCH=" + SpeechRecognizer.ERROR_NO_MATCH
				+ ",RECOGNIZER_BUSY=" + SpeechRecognizer.ERROR_RECOGNIZER_BUSY
				+ ",SERVER=" + SpeechRecognizer.ERROR_SERVER
				+ ",SPEECH_TIMEOUT=" + SpeechRecognizer.ERROR_SPEECH_TIMEOUT);
		return super.setTag(tag);
	}

	public void setEventHandler(final RecognitionEventHandler eventHandler) {
		this.eventHandler = eventHandler;
	}

	private ShieldFrame sf;
	RecognitionEventHandler controllerHandler = new RecognitionEventHandler() {

		@Override
		public void onResult(List<String> result) {
			if (result.size() > 0) {
				if (eventHandler != null)
					eventHandler.onResult(result);
				sf = new ShieldFrame(UIShield.SPEECH_RECOGNIZER_SHIELD.getId(),
						SEND_RESULT);
				String recognized = result.get(0);
				sf.addStringArgument(recognized.toLowerCase());
				Log.d("Frame", sf.toString());
				sendShieldFrame(sf);
			} else {
				onError("No Matching result", SpeechRecognizer.ERROR_NO_MATCH);
			}
		}

		@Override
		public void onReadyForSpeach(Bundle params) {
			if (eventHandler != null)
				eventHandler.onReadyForSpeach(params);
		}

		@Override
		public void onError(String error, int errorCode) {
			if (eventHandler != null)
				eventHandler.onError(error, errorCode);
			int errorSent = ERROR.SERVER;
			switch (errorCode) {
			case SpeechRecognizer.ERROR_AUDIO:
				errorSent = ERROR.AUDIO;
				break;
			case SpeechRecognizer.ERROR_NETWORK:
				errorSent = ERROR.NETWORK;
				break;
			case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
				errorSent = ERROR.NETWORK_TIMEOUT;
				break;
			case SpeechRecognizer.ERROR_NO_MATCH:
				errorSent = ERROR.NO_MATCH;
				break;
			case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
				errorSent = ERROR.RECOGNIZER_BUSY;
				break;
			case SpeechRecognizer.ERROR_SERVER:
				errorSent = ERROR.SERVER;
				break;
			case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
				errorSent = ERROR.SPEECH_TIMEOUT;
				break;

			default:
				break;
			}
			sf = new ShieldFrame(UIShield.SPEECH_RECOGNIZER_SHIELD.getId(),
					SEND_ERROR);
			sf.addIntegerArgument(1, false, errorSent);
			Log.d("Frame", sf.toString());
			sendShieldFrame(sf);
		}

		@Override
		public void onEndOfSpeech() {
			if (eventHandler != null)
				eventHandler.onEndOfSpeech();
		}

		@Override
		public void onBeginingOfSpeech() {
			eventHandler.onBeginingOfSpeech();
		}

		@Override
		public void onRmsChanged(float rmsdB) {
			eventHandler.onRmsChanged(rmsdB);
			Log.d("RMS", rmsdB + "");
		}
	};

	@Override
	public void refresh() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {
		if (frame.getShieldId() == UIShield.SPEECH_RECOGNIZER_SHIELD.getId()) {
			if (frame.getFunctionId() == 0x01) {
				startRecognizer();
			}
		}
	}

	public void startRecognizer() {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (mSpeechRecognitionService != null)
					mSpeechRecognitionService
							.startRecognition(controllerHandler);
			}
		});
	}

	@Override
	public void reset() {
		if (mServiceConnection != null && activity != null)
			activity.unbindService(mServiceConnection);
	}

	private static class ERROR {
		protected static int AUDIO = 3, NETWORK = 2, NETWORK_TIMEOUT = 1,
				NO_MATCH = 7, RECOGNIZER_BUSY = 8, SERVER = 4,
				SPEECH_TIMEOUT = 6;
	}
}
