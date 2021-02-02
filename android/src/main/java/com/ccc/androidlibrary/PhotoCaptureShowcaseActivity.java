package com.ccc.androidlibrary;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.cccis.mobile.sdk.android.qephotocapture.QEPhotoCaptureRunTimePermissionActivity;
import com.cccis.mobile.sdk.android.qephotocapture.utils.QEPhotoCaptureConfigurationFactory;
import com.cccis.sdk.android.common.activity.LogSupportActivity;
import com.cccis.sdk.android.common.helper.ActivityHelper;
import com.cccis.sdk.android.common.helper.MessageHelper;
import com.cccis.sdk.android.common.legacy.CapturedPhotoInfo;
import com.cccis.sdk.android.common.permission.PermissionsHelper;
import com.cccis.sdk.android.domain.ImageCollection;
import com.cccis.sdk.android.domain.ImageMetadata;
import com.cccis.sdk.android.photocapturelocalstorage.QELocalStorageCapturedPhotoService;
import com.cccis.sdk.android.photocapturelocalstorage.QELocalStoragePhotoCaptureActivity;
import com.cccis.sdk.android.photocapturelocalstorage.QELocalStoragePhotoCaptureLandscapeActivity;
import com.cccis.sdk.android.photocapturelocalstorage.QELocalStorageRetakePhotoActivity;
import com.cccis.sdk.android.photocapturelocalstorage.QELocalStorageRetakePhotoLandscapeActivity;
import com.cccis.sdk.android.services.data.DataService;
import com.cccis.sdk.android.services.legacy.CapturedPhotoService;
import com.cccis.sdk.android.services.rest.context.ENVFactory;
import com.cccis.sdk.android.upload.MCEPClientService;

import java.util.List;

//import com.cccis.sdk.android.blurdetection.OpenCVHelper;
//import com.cccis.sdk.android.cccsdkdemointegration.lang.DemoConstants;
//import com.cccis.sdk.android.photocapturelocalstorage.QELocalStorageRetakePhotoActivity;

public class PhotoCaptureShowcaseActivity extends LogSupportActivity {
    private static final int DEMO_ACTIVITY_REQUEST_CODE = 100;
    private DataService dataService;
    private MCEPClientService mcepClientService;
    private CapturedPhotoService capturedPhotoService;

    private PermissionsHelper permissionsHelper;

    private ProgressBar spinner;
    private LinearLayout buttons;

//This only sets the UI components currently.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pc_sdk_showcase);

        permissionsHelper = new PermissionsHelper(this);

        spinner=(ProgressBar)findViewById(R.id.progressBar);
        spinner.getIndeterminateDrawable().setColorFilter(Color.parseColor("#d97a23"), android.graphics.PorterDuff.Mode.MULTIPLY);
        spinner.setVisibility(View.GONE);

        buttons = (LinearLayout)findViewById(R.id.buttons);

        try {
            dataService = DataService.getInstance(getApplicationContext());
            mcepClientService = new MCEPClientService(ENVFactory.getInstance(this).SHARED_ENV);
            capturedPhotoService = QELocalStorageCapturedPhotoService.getInstance(getApplicationContext(), DemoConstants.ESTIMATE_PDF_NAME, DemoConstants.IMAGE_COLLECTION_KEY);

            QEPhotoCaptureConfigurationFactory.getInstance(this).configure();
            //Or
            QEPhotoCaptureConfigurationFactory.getInstance(this)
                    .setWizardMode(R.string.PhotoCaptureConfiguration_WizardMode)
                    //.setAutoShowHelpOverlayFlags(R.array.AutoShowHelpOverlayFlags)
                    //.setCarouselImages(R.array.CarouselImagesCoupe)
                    //.setCarouselItemNames(R.array.CarouselItemNames)
                    .setOverlayDescriptions(R.array.OverlayDescriptions)
                    .setOverlayTitles(R.array.OverlayTitles)
                    .setOverlayHeaders(R.array.OverlayHeaders)
                    //.setOverlayImages(R.array.OverlayImagesCoupe)
                    .reconfigure();

            //Enable this only if Blur Detection dependencies are included (blurdetection and opencv)
            // SDKConfigurator.setBlurDetectionHandler(new OpenCVHelper());
        } catch (final Exception e){
            MessageHelper.showPopupError(this, e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cancel();
    }

//This calls to QELocalStoragePhotoCaptureActivity.class, and runs that.
    public void photo(View view) {
        Intent intent = new Intent(getApplicationContext(), QELocalStoragePhotoCaptureActivity.class);
        startActivityForResult(intent, DEMO_ACTIVITY_REQUEST_CODE);
    }

//This calls to QELocalStoragePhotoCaptureLandscapeActivity.class, and runs that.
    public void photoLandscape(View view) {
        Intent intent = new Intent(getApplicationContext(), QELocalStoragePhotoCaptureLandscapeActivity.class);
        startActivityForResult(intent, DEMO_ACTIVITY_REQUEST_CODE);
    }

//This will check to see if all of the required images have been taken. If not, it will ask you to take all of the required photos. If they have all been taken,
//then it will call upload(), which will upload the photos. And if at any time, there are pending photos, it will upload those as well and let the user know.
    public void uploadImages(View view){
//        if(!mcepClientService.isAuthenticated()){
//            ActivityHelper.showMessage(this, "Please run the login demo first before executing this demo");
//            return;
//        }

        if (dataService.imageCollectionExists(DemoConstants.IMAGE_COLLECTION_KEY)) {
            final ImageCollection collection = dataService.getImageCollection(DemoConstants.IMAGE_COLLECTION_KEY);
            if(collection.getImages() != null){
                if(collection.getImages().size() < QEPhotoCaptureConfigurationFactory.getInstance(this).getPhotoCaptureParameters().getNumPhotos()){
                    ActivityHelper.showMessage(this, "You have only taken " + collection.getImages().size() + " of the required " + QEPhotoCaptureConfigurationFactory.getInstance(this).getPhotoCaptureParameters().getNumPhotos() + " photos! " +
                            "\nPlease run the Photo Caputre demo and take all required photos first before executing this demo!");
                    return;
                } else {
                    int pendingUploads = 0;
                    for(ImageMetadata meta: collection.getImages()){
                        if(!meta.isUploaded()){
                            ++pendingUploads;
                        }
                    }

                    if(pendingUploads > 0){
                        upload();//The real SDK upload call
                    } else {
                        ActivityHelper.showMessage(this, "There are no new images to upload. All images have already been uploaded!");
                        return;
                    }
                }
            } else {
                ActivityHelper.showMessage(this, "Please run the Photo Caputre demo and take all required photos first before executing this demo!");
                return;
            }
        } else {
            ActivityHelper.showMessage(this, "Please run the Photo Caputre demo and take all required photos first before executing this demo!");
            return;
        }
    }

//This attempts to upload the photos to CCC. Per image, if it succeeds or if it fails, then it will let the user know. If it fails, it will then cancel its actions
//and not upload any more.
    private void upload(){
        spinner.setVisibility(View.VISIBLE);
        buttons.setVisibility(View.GONE);

        capturedPhotoService.upload("Last picture", new CapturedPhotoService.OnUploadCallback() {
            @Override
            public void onSuccess(final CapturedPhotoInfo info, final int pending) {
                final String message = info.getName() + " upload success! " + pending + " image(s) remaining...";
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (pending == 0) {cancel();}
                        ActivityHelper.showMessage(PhotoCaptureShowcaseActivity.this, message);
                    }
                });
            }

            @Override
            public void onFailure(final CapturedPhotoInfo info, final String message, final int pending) {
                final String fullmessage = "Failed to upload " + (info != null ? info.getName() : "") + " - Pending=" + pending + " - Message=" + message;
                runOnUiThread(new Runnable() {
                    public void run() {
                        cancel();

                        log.e("demo", fullmessage);

                        ActivityHelper.showMessage(PhotoCaptureShowcaseActivity.this, fullmessage);
                    }
                });
            }

           // @Override
//            public void onFinish() {
//                ActivityHelper.showMessage(PhotoCaptureShowcaseActivity.this, "Hello");
//            }
        });
    }

//This allows the user to take some additional photos. It also has special options as well.
    public void takeAdditionalPhoto(View view){
        Intent intent = new Intent(this, QELocalStorageRetakePhotoActivity.class);

        //Option 1: pass no data - a new image with defaults attributes will be created (this example uses option 1)
        //Also, option 1 assumes a sequential order. That is, the order of the additional image will be the number of already taken images.

        //Option 2: customize by setting the order, name, description, angle, and/or type
        //With this option, you can allow the user to take additional images in both sequential and non-sequential orders.
        /*
        CapturedPhotoInfo info = new CapturedPhotoInfo();
        //info.setOrder(getCustomOrder());
        info.setName("Custom Name");
        info.setDescription("Custom description");
        info.setAngle("");
        info.setType(IMAGE_TYPE.ADDITIONAL);
        intent.putExtra(QELocalStorageRetakePhotoActivity.INTENT_DATA, info);
        */

        startActivityForResult(intent, DEMO_ACTIVITY_REQUEST_CODE);
    }

//This allows the user to take some additional landscape photos. It also has special options as well.
    public void takeAdditionalPhotoLandscape(View view){
        Intent intent = new Intent(this, QELocalStorageRetakePhotoLandscapeActivity.class);

        startActivityForResult(intent, DEMO_ACTIVITY_REQUEST_CODE);
    }

//This allows the user to retake the first image.
    public void retakeFirstPhoto(View view){
        List<CapturedPhotoInfo> infos = capturedPhotoService.getAllCapturedPhotoInfos();
        if(infos != null && !infos.isEmpty()){
            Intent intent = new Intent(this, QELocalStorageRetakePhotoActivity.class);
            intent.putExtra(QELocalStorageRetakePhotoActivity.INTENT_DATA, infos.get(0));
            startActivityForResult(intent, DEMO_ACTIVITY_REQUEST_CODE);
        } else {
            MessageHelper.showPopupError(this, "No photo has yet been taken!");
        }
    }

//This allows the user to retake the first landscape image.
    public void retakeFirstPhotoLandscape(View view){
        List<CapturedPhotoInfo> infos = capturedPhotoService.getAllCapturedPhotoInfos();
        if(infos != null && !infos.isEmpty()){
            Intent intent = new Intent(this, QELocalStorageRetakePhotoLandscapeActivity.class);
            intent.putExtra(QELocalStorageRetakePhotoActivity.INTENT_DATA, infos.get(0));
            startActivityForResult(intent, DEMO_ACTIVITY_REQUEST_CODE);
        } else {
            MessageHelper.showPopupError(this, "No photo has yet been taken!");
        }
    }

//This makes the spinner visibility gone, and the button visibilities visible.
    private void cancel(){
        spinner.setVisibility(View.GONE);
        buttons.setVisibility(View.VISIBLE);
    }

//This helps handle permissions.
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsHelper.handlePermissionResults(requestCode, permissions, grantResults);
    }

//This lets the viewer know the result of their activities.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        int success = Activity.RESULT_OK;
        int failure = Activity.RESULT_CANCELED;
        int firstUser = Activity.RESULT_FIRST_USER;
        int permissionDenied = QEPhotoCaptureRunTimePermissionActivity.PERM_DENIED_RESULT_CODE;
        final String message = "Hurray! You have captured all the photos!";

        if(resultCode == success){
            ActivityHelper.showMessage(PhotoCaptureShowcaseActivity.this, message);
        }

        if(resultCode == permissionDenied){
            //Do something
        }
    }

//    public void firstMethod() {
//        try {
//            dataService = DataService.getInstance(getApplicationContext());
//            mcepClientService = new MCEPClientService(ENVFactory.getInstance(this).SHARED_ENV);
//            capturedPhotoService = QELocalStorageCapturedPhotoService.getInstance(getApplicationContext(), "CCCSDK_DEMO_ESTIMATE_PDF.pdf", "CCCSDK_DEMO_IMAGE_COOLLECTION_KEY");
//
//            QEPhotoCaptureConfigurationFactory.getInstance(this).configure();
//            //Or
//            QEPhotoCaptureConfigurationFactory.getInstance(this)
//                    .setWizardMode(R.string.PhotoCaptureConfiguration_WizardMode)
//                    //.setAutoShowHelpOverlayFlags(R.array.AutoShowHelpOverlayFlags)
//                    //.setCarouselImages(R.array.CarouselImagesCoupe)
//                    //.setCarouselItemNames(R.array.CarouselItemNames)
//                    .setOverlayDescriptions(R.array.OverlayDescriptions)
//                    .setOverlayTitles(R.array.OverlayTitles)
//                    .setOverlayHeaders(R.array.OverlayHeaders)
//                    //.setOverlayImages(R.array.OverlayImagesCoupe)
//                    .reconfigure();
//
//            //Enable this only if Blur Detection dependencies are included (blurdetection and opencv)
//            // SDKConfigurator.setBlurDetectionHandler(new OpenCVHelper());
//        } catch (final Exception e){
//            MessageHelper.showPopupError(this, e);
//        }
//    }
}
