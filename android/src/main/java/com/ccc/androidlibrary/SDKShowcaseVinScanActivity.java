package com.ccc.androidlibrary;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.cccis.mobile.sdk.android.qephotocapture.utils.QEPhotoCaptureConfigurationFactory;
import com.cccis.sdk.android.services.callback.BaseCCCAPIRequestCallback;
import com.cccis.sdk.android.services.rest.context.ENVFactory;
import com.cccis.sdk.android.services.rest.request.VehicleServiceRequest;
import com.cccis.sdk.android.vindecode.CCCAPIVinDecodeClientService;
import com.cccis.sdk.android.vindecoding.VinDecodingActivity;
import com.cccis.sdk.android.vindecoding.ex.EnterVINManuallyActivity;
import com.fasterxml.jackson.core.type.TypeReference;

public class SDKShowcaseVinScanActivity extends VinDecodingActivity {

    //private RedFlagIntegrator integrator;

//This function currently only sets the content view to vin scan. This is currently purely working with the UI.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sdkshowcase_vin_scan);
        //this.integrator = new QERedFlagIntegrator();
    }

//THE MANUALLY ENTERING THE VIN NUMBER DOES NOT WORK AT CURRENT.
    public void scan(View view){
        //initiateScan(); //initiateScan() will pull up the camera with a scan function, and can only scan. There is no room for activity here.
        //Or 
        initiateScan(EnterVINManuallyActivity.class);//this adds the manual VIN entry option
        //Or 
        //initiateScan(EnterVINManuallyLandscapeActivity.class);//this adds the manual VIN entry option + Landscape mode
    }

    @Override
    protected void onCancelled() {
        Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
    }

//This function is called if the vin is valid, and will pop up a message stating that the vin is valid as well as the image path. It will then try to set the vin
//as the decoded vin. If no other errors occur, it will then set the overlay in the method configurePcBasedOnBodyType(Context context, String aBodyType).
    @Override
    protected void onValidVin(String decoded, String imagePath) {
        Toast.makeText(this, "Scanned valid VIN: " + decoded + " - Image Path=" + imagePath, Toast.LENGTH_LONG).show();
        //integrator.addRedFlagIntegration(getApplicationContext(), decoded, imagePath, false, null);

        //final String vin = "JHLRD68545C011932";
            CCCAPIVinDecodeClientService service = new CCCAPIVinDecodeClientService(ENVFactory.getInstance(this).SHARED_ENV);
            VehicleServiceRequest request = new VehicleServiceRequest();
            request.setVin(decoded);

        try {
            service.vindecodeDataCenter(request, new BaseCCCAPIRequestCallback(){
                @Override
                public TypeReference getSuccessTypeReference() {return new TypeReference<Object>() {};}

                @Override
                public void onSuccess(Object o) {
                    //ActivityHelper.showMessage(SDKShowcaseActivity.this, o + "");
                    Log.i("onSuccess of library", "Decode success! " + o);
                    int index = o.toString().indexOf("bodyTypeCode")+13;
                    Log.i("onSuccess of library", "Index is: " + index);
                    String subString = o.toString().substring(index);
                    Log.i("onSuccess of library", "SubString is: " + subString);
                    int secondIndex = subString.indexOf(",");
                    Log.i("onSuccess of library", "secondIndex is: " + secondIndex);
                    String vehicleType = subString.substring(0,secondIndex);
                    Log.i("onSuccess of library", "vehicleType is: " + vehicleType);
                    configurePcBasedOnBodyType(getApplicationContext(), vehicleType);
                    Intent intent = new Intent(getApplicationContext(), SDKShowcaseActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onFailure(Object o, int statusCode, Throwable throwable) {
                    Log.i("onFailure of library", "Object: " + o);
                    Log.i("onFailure of library", "statusCode: " + statusCode);
                    Log.i("onFailure of library", "throwable: " + throwable);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("Exception ", "Exception caught: " + e);
        }


        //Intent intent = new Intent(this, SDKShowcaseActivity.class);
        //intent.putExtra("vehicleType", vehicleType);
        //startActivity(intent);
        
    }

//This function is called if the vin is invalid, and will pop up a message stating that the vin is invalid as well as the image path.
    @Override
    protected void onInvalidVin(String decoded, String imagePath) {
        Toast.makeText(this, "Scanned invalid VIN: " + decoded + " - Image Path=" + imagePath, Toast.LENGTH_LONG).show();
        log.w("VIN", "Invalid VIN" + decoded);
    }

//This is where the overlay is decided. If the overlay is not switching, first check in onCreate within SDKShowcaseActivity.java to see if the vehicle type is
//being overridden. If it is not, then check between here and vinDecode in SDKShowcaseActivity.java to make sure that the vins are the same, and not being
//overridden. You can also check to make sure that the bodyTypeCode and the vehicleType are the same as well.
    public static void configurePcBasedOnBodyType(Context context, String aBodyType) {
        if (aBodyType.equals(context.getString(R.string.body_type_code_coupe))) {
            QEPhotoCaptureConfigurationFactory.getInstance(context, true).setVehicleType(QEPhotoCaptureConfigurationFactory.VEHICLE_TYPE.COUPE);
        } else if (aBodyType.equals(context.getString(R.string.body_type_code_hatchback))) {
            QEPhotoCaptureConfigurationFactory.getInstance(context, true).setVehicleType(QEPhotoCaptureConfigurationFactory.VEHICLE_TYPE.HATCHBACK);
        } else if (aBodyType.equals(context.getString(R.string.body_type_code_sedan))) {
            QEPhotoCaptureConfigurationFactory.getInstance(context, true).setVehicleType(QEPhotoCaptureConfigurationFactory.VEHICLE_TYPE.SEDAN);
        } else if (aBodyType.equals(context.getString(R.string.body_type_code_suv))) {
            QEPhotoCaptureConfigurationFactory.getInstance(context, true).setVehicleType(QEPhotoCaptureConfigurationFactory.VEHICLE_TYPE.SUV);
        } else if (aBodyType.equals(context.getString(R.string.body_type_code_van))) {
            QEPhotoCaptureConfigurationFactory.getInstance(context, true).setVehicleType(QEPhotoCaptureConfigurationFactory.VEHICLE_TYPE.VAN);
        } else if (aBodyType.equals(context.getString(R.string.body_type_code_wagon))) {
            QEPhotoCaptureConfigurationFactory.getInstance(context, true).setVehicleType(QEPhotoCaptureConfigurationFactory.VEHICLE_TYPE.WAGON);
        } else if ((aBodyType.equals(context.getString(R.string.body_type_code_truck1))) || (aBodyType.equals(context.getString(R.string.body_type_code_truck2))) || (aBodyType.equals(context.getString(R.string.body_type_code_truck3)))) {
            QEPhotoCaptureConfigurationFactory.getInstance(context, true).setVehicleType(QEPhotoCaptureConfigurationFactory.VEHICLE_TYPE.TRUCK);
        } else {
            QEPhotoCaptureConfigurationFactory.getInstance(context, true).setVehicleType(QEPhotoCaptureConfigurationFactory.VEHICLE_TYPE.SEDAN);
        }
    }

}
