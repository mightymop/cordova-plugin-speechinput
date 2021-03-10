package de.mopsdom.speechinput;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.content.IntentFilter;
import android.content.ActivityNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.*;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;


public class Speechinput extends CordovaPlugin {

    private String targetIDField;

    private final static int REQ_CODE_SPEECH_INPUT = 132652;

    private CallbackContext callback = null;

    public void initialize(org.apache.cordova.CordovaInterface cordova, org.apache.cordova.CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    public boolean startSpeechInputPrompt(long pause, boolean offline, String prompttitle, boolean partials) {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,prompttitle);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.cordova.getActivity().getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,offline);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, partials);

        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,pause);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,pause);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,pause);

        try {
            this.cordova.getActivity().startActivityForResult(intent,REQ_CODE_SPEECH_INPUT);
            return true;
        } catch (ActivityNotFoundException a) {
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
        if( requestCode == REQ_CODE_SPEECH_INPUT )
        {
            if (resultCode == Activity.RESULT_OK ) {

				if (null != data)
				{
					ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
					try
					{
						JSONObject resultjson = new JSONObject();
						JSONArray resulttext = new JSONArray();

						for (String str : result)
						{
							resulttext.put(str);
						}

						resultjson.put("text",resulttext);
						resultjson.put("targetid",this.targetIDField);

						PluginResult presult = new PluginResult(PluginResult.Status.OK, resultjson.toString());
						presult.setKeepCallback(true);
						callback.sendPluginResult(presult);
						return;
					}
					catch (Exception e){
					}
				}
				PluginResult presult = new PluginResult(PluginResult.Status.ERROR, "no params found on onActivityResult but returned successfully (RESULT_OK)" );
				presult.setKeepCallback(true);
				callback.sendPluginResult(presult);
            }
			else
			{
				PluginResult presult = new PluginResult(PluginResult.Status.ERROR, "ERROR on onActivityResult ("+String.valueOf(resultCode)+")");
				presult.setKeepCallback(true);
				callback.sendPluginResult(presult);
			}
        }
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException  {

        if (action.equals("startSpeechInputPrompt")) {

			if (!SpeechRecognizer.isRecognitionAvailable(this.cordova.getActivity())) {
				PluginResult presult = new PluginResult(PluginResult.Status.ERROR, "SpeechRecognizer not available" );
				presult.setKeepCallback(true);
				callbackContext.sendPluginResult(presult);
				return true;
			}

            this.callback = callbackContext;
            cordova.setActivityResultCallback (this);

            JSONObject params = new JSONObject(data.getString(0));
            if (params!=null) {

				Iterator<String> keys = params.keys();

				long pause = 2500;
				boolean offline = true;
				boolean partials=false;
				String prompttitle = "Spracheingabe";

				this.targetIDField = null;

				while(keys.hasNext()) {
					String key = keys.next();
					  
					if (key.equals("pause"))
					{
						pause = params.getLong(key);
					}
					else
					if (key.equals("offline"))
					{
						offline=params.getBoolean(key);
					}
					else
					if (key.equals("prompttitle"))
					{
						prompttitle = params.getString("package");
					}
					else
					if (key.equals("partials"))
					{
						partials = params.getBoolean("partials");
					}
					else
					if (key.equals("targetid"))
					{
						this.targetIDField = params.getString("targetid");
					}
				}

				startSpeechInputPrompt(pause,offline,prompttitle,partials);
				return true;
			}
		}

		return false;
	}
}
