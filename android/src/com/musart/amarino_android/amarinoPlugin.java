package com.musart.amarino_android;

import org.apache.cordova.api.CordovaInterface;
import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;

import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;

public class amarinoPlugin extends Plugin {
	// "00:12:03:10:22:91"
	private String mDeviceAddress = "";
	private boolean mIsConnected = false;
	
	private ArduinoReceiver m_arduinoReceiver = null;
	
	private static final String TAG = "amarinoPlugin";
	
	public amarinoPlugin(){
		m_arduinoReceiver = new ArduinoReceiver();	
	}

	@Override
	public void setContext(CordovaInterface ctx) {
		super.setContext(ctx);
	}
	
	@Override
	public void onDestroy() {
		// if you connect in onStart() you must not forget to disconnect when your app is closed
		Amarino.disconnect(cordova.getActivity(), mDeviceAddress, "");
		// do never forget to unregister a registered receiver
		cordova.getActivity().unregisterReceiver(m_arduinoReceiver);
	}

	@Override
	public PluginResult execute(String action, JSONArray args, final String callbackId) {
		Log.i(TAG, "execute() action:" + action + ", callbackId:" + callbackId);

		if(action.equals("setConnectionId")) {
			try {
				Log.i(TAG, "setConnectionId:" + args.toString());
				mDeviceAddress = args.getString(0);
				
				cordova.getActivity().registerReceiver(m_arduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_CONNECT));
				cordova.getActivity().registerReceiver(m_arduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_DISCONNECT));
				cordova.getActivity().registerReceiver(m_arduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_SEND));
				cordova.getActivity().registerReceiver(m_arduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_RECEIVED));
				cordova.getActivity().registerReceiver(m_arduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_CONNECTED));
				cordova.getActivity().registerReceiver(m_arduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_DISCONNECTED));
				cordova.getActivity().registerReceiver(m_arduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_CONNECTION_FAILED));
				cordova.getActivity().registerReceiver(m_arduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_PAIRING_REQUESTED));
				cordova.getActivity().registerReceiver(m_arduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_GET_CONNECTED_DEVICES));
				cordova.getActivity().registerReceiver(m_arduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_CONNECTED_DEVICES));
				cordova.getActivity().registerReceiver(m_arduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_ENABLE));
				cordova.getActivity().registerReceiver(m_arduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_DISABLE));
				cordova.getActivity().registerReceiver(m_arduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_DISABLE_ALL));
				cordova.getActivity().registerReceiver(m_arduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_EDIT_PLUGIN));
								
				Log.d(TAG, "setConnectionId is called, mDeviecAddress:" + mDeviceAddress);
				return new PluginResult(PluginResult.Status.OK, mDeviceAddress);
			} catch (JSONException e) {
				e.printStackTrace();
				return new PluginResult(PluginResult.Status.ERROR, "Parameters are not valid.");
			}
		}
		else if(action.equals("connectBT")) {
			if(mDeviceAddress == "") {
				return new PluginResult(PluginResult.Status.ERROR, "Bluetooth Address is not set properly.");
			}

			Amarino.connect(cordova.getActivity(), mDeviceAddress, callbackId);
			
			PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
			r.setKeepCallback(true);
			return r;
		}
		else if (action.equals("controlLED")) {
			if(mDeviceAddress == "") {
				return new PluginResult(PluginResult.Status.ERROR, "Bluetooth Address is not set properly.");
			}
			
			if(mIsConnected == false) {
				return new PluginResult(PluginResult.Status.ERROR, "Bluetooth is not connected.");
			}
			
			Amarino.sendDataToArduino(cordova.getActivity(), mDeviceAddress, 'L', 0, callbackId);

			PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
			r.setKeepCallback(true);
			return r;
		}
		else if (action.equals("buttonEvent")) {
			if(mDeviceAddress == "") {
				return new PluginResult(PluginResult.Status.ERROR, "Bluetooth Address is not set properly.");
			}
			
			if(mIsConnected == false) {
				return new PluginResult(PluginResult.Status.ERROR, "Bluetooth is not connected.");
			}
			
			Amarino.sendDataToArduino(cordova.getActivity(), mDeviceAddress, 'B', 0, callbackId);
			
			PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
			r.setKeepCallback(true);
			return r;
		}
		
		return null;
	}

	private class ArduinoReceiver extends BroadcastReceiver {

		String TAG = amarinoPlugin.TAG + ":ArduinoReceiver";
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				String action = intent.getAction();
				if(action.equals(AmarinoIntent.ACTION_CONNECT)) {
					Log.i(TAG, "ACTION_CONNECT");
				} else if(action.equals(AmarinoIntent.ACTION_DISCONNECT)) {
					Log.i(TAG, "disconnected");
				} else if(action.equals(AmarinoIntent.ACTION_SEND)) {
					Log.i(TAG, "send");
				} else if(action.equals(AmarinoIntent.ACTION_RECEIVED)) {
					String data = null;
					final String address = intent.getStringExtra(AmarinoIntent.EXTRA_DEVICE_ADDRESS);
					String callbackId = intent.getStringExtra(AmarinoIntent.EXTRA_PHONEGAP_CALLBACK_ID);
					Log.i(TAG, "received, callbackId[" + callbackId + "]");
					// the type of data which is added to the intent
					final int dataType = intent.getIntExtra(AmarinoIntent.EXTRA_DATA_TYPE, -1);
					Log.i(TAG, "ArudinoReceiver::onReceive() action:" + action + ", address:" + address + ", dataType:" + dataType);

					if(address.equals(mDeviceAddress)) {
						if (dataType == AmarinoIntent.STRING_EXTRA) {
							data = intent.getStringExtra(AmarinoIntent.EXTRA_DATA);
							
							Log.d(TAG, "[ArudinoReceiver]Sensor value =" + data);
							Log.d(TAG, "[ArudinoReceiver]addr =" + address);
							Log.d(TAG, "[ArudinoReceiver]dataType =" + dataType);
							Log.d(TAG, "[ArudinoReceiver]callbackId = " + callbackId);

							success(new PluginResult(PluginResult.Status.OK, data), callbackId);
						}
					} else {
						Log.i(TAG, "Address[" + mDeviceAddress + "] is not matched");
					}
				} else if(action.equals(AmarinoIntent.ACTION_CONNECTED)) {
					mIsConnected = true;
					String callbackId = intent.getStringExtra(AmarinoIntent.EXTRA_PHONEGAP_CALLBACK_ID);
					Log.i(TAG, "ACTION_CONNECTED, callbackId[" + callbackId + "]");
					success(new PluginResult(PluginResult.Status.OK, "connected"), callbackId);
				} else if(action.equals(AmarinoIntent.ACTION_DISCONNECTED)) {
					Log.i(TAG, "ACTION_DISCONNECTED");
					mIsConnected = false;
				} else if(action.equals(AmarinoIntent.ACTION_CONNECTION_FAILED)) {
					Log.i(TAG, "connection failed");
					mIsConnected = false;
				} else if(action.equals(AmarinoIntent.ACTION_PAIRING_REQUESTED)) {
					Log.i(TAG, "pairing requested");
				} else if(action.equals(AmarinoIntent.ACTION_GET_CONNECTED_DEVICES)) {
					Log.i(TAG, "get connected devices");
				} else if(action.equals(AmarinoIntent.ACTION_CONNECTED_DEVICES)) {
					Log.i(TAG, "connected devices");
				} else if(action.equals(AmarinoIntent.ACTION_ENABLE)) {
					Log.i(TAG, "enable");
				} else if(action.equals(AmarinoIntent.ACTION_DISABLE)) {
					Log.i(TAG, "disable");
				} else if(action.equals(AmarinoIntent.ACTION_DISABLE_ALL)) {
					Log.i(TAG, "disable all");
				} else if(action.equals(AmarinoIntent.ACTION_EDIT_PLUGIN)) {
					Log.i(TAG, "edit plugin");
				} else {
					Log.i(TAG, "default");
				}
			}
		}
	};
}