package com.ccc.androidlibrary;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.cccis.sdk.android.auth.CCCAPIAuthClientService;
import com.cccis.sdk.android.rest.RESTErrorResponse;
import com.cccis.sdk.android.services.callback.OnCCCAPIActionCallback;
import com.cccis.sdk.android.services.rest.context.ENVFactory;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.util.HashMap;
import java.util.Map;

public class Module extends ReactContextBaseJavaModule {

  private static final String DURATION_SHORT_KEY = "SHORT";
  private static final String DURATION_LONG_KEY = "LONG";

  public Module(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  public String getName() {
    return "Boilerplate";
  }

  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    constants.put(DURATION_SHORT_KEY, Toast.LENGTH_SHORT);
    constants.put(DURATION_LONG_KEY, Toast.LENGTH_LONG);
    return constants;
  }

  @ReactMethod
  public void show(String message, int duration) {
    Toast.makeText(getReactApplicationContext(), message, duration).show();
  }

//This method gets an ID and a last name. It then calls CCC's authentication service. If the ID and lastname are in CCC's library, it will go to the onSuccess and
//let the user know it was successful. If it is not in the library, then it will let the user know it failed and what the status code for failure is.
  @ReactMethod
  public void authenticateUser(String claimId, String lastName) {
    Log.i("claimId in library",claimId);
    Log.i("last name in library",lastName);
    CCCAPIAuthClientService service = new CCCAPIAuthClientService(ENVFactory.getInstance(getReactApplicationContext()).SHARED_ENV);
    service.onLogon(claimId, lastName, new OnCCCAPIActionCallback() {
      @Override
      public void onSuccess() {
        Log.i("onSuccess of library", "Login success!");
        //Toast.makeText(getReactApplicationContext(), "Login success!", Toast.LENGTH_SHORT).show();
      }

      @Override
      public void onFailure(RESTErrorResponse result, int statusCode, Throwable t) {
        if (t != null) {
          Log.i("onFailure if in library", "Login failed!");
          Log.i("onFailure if in library", t.getClass().getSimpleName() + ": " + t.getMessage());
          //Toast.makeText(getReactApplicationContext(), t.getClass().getSimpleName() + ": " + t.getMessage(), Toast.LENGTH_SHORT).show();
        } else {
          Log.i("onFailure else library", "Login failed!");
          Log.i("onFailure else library", "StatusCode: " + statusCode + " - Payload=" + result);
          //Toast.makeText(getReactApplicationContext(), "StatusCode: " + statusCode + " - Payload=" + result, Toast.LENGTH_SHORT).show();
        }
      }
    });
  }

  @ReactMethod
  public void currentActivity() {
    //ReactApplicationContext context = getReactApplicationContext();
    Activity activity = getCurrentActivity();
    
  }

//This method calls to capture photos, which is SDKShowcaseActivity.class. The methods for that class are listed down below.
  @ReactMethod
  public void capture() {
    //ReactApplicationContext context = getReactApplicationContext();
    Activity activity = getCurrentActivity();

    Intent intent = new Intent(activity, SDKShowcaseActivity.class);
    activity.startActivity(intent);
  }

//This method calls to capture VIN, which is SDKShowcaseVinScanActivity.class. The methods for that class are listed down below.
  @ReactMethod
  public void captureVIN() {
    //ReactApplicationContext context = getReactApplicationContext();
    Activity activity = getCurrentActivity();

    Intent intent = new Intent(activity, SDKShowcaseVinScanActivity.class);
    activity.startActivity(intent);
  }
}