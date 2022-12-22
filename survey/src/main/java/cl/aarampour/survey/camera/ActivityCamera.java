package cl.aarampour.survey.camera;

import static android.os.Build.VERSION.SDK_INT;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import cl.aarampour.survey.Functions;
import cl.aarampour.survey.R;
import cl.aarampour.survey.SurveyConfig;
import cl.aarampour.survey.SurveyKey;

/**
 * First of all sub folder should named in pictures element acrtivity
 * to put image that related to that activity in one folder
 * {sub_folder_path}
 * <p>
 * returns path in storage in onActivityResult method
 */


public class ActivityCamera extends AppCompatActivity implements View.OnClickListener
        , View.OnTouchListener, SensorEventListener {
    private static final String TAG = "ActivityCamera";

    public static String FLAG_CUSTOM_CAMERA = "com.example.checklist.Camera.ActivityCamera";

    public static final int PICK_IMAGE_CODE = 1002;
    public static final int PICK_PDF_CODE = 1003;

    private String folderPath = "";
    private String viewId = "";
    private String viewName = "";
    private int typeId = 0;
    private String typeName = "";
    private int pagePosition = 0;
    private int index = 0;

    private int zoom = 0;
    private boolean isAppClosed;

    private int minWidthRange = 600;
    private int maxWidthRange = 800;

    private float last_number_of_x = 0;
    private float last_number_of_y = 0;
    private float last_number_of_z = 0;

    private boolean editModeEnable = false;

    /************************    camera api  ************************/
    private Camera mCamera;

    private ImageView rotateLeft;
    private ImageView rotateRight;

    private LinearLayout top_choice;
    private Boolean Camera_Status = false;

    private boolean has_flash = false;

    //    private Uri imageUri;
    private LinearLayout cancel;
    private LinearLayout tick;
    private int camera_id = Camera.CameraInfo.CAMERA_FACING_BACK;


    private CameraPreview mPreview;

    private boolean isParamsSet = true;
    private List<Integer> zooms;
    private LinearLayout search_linear;
    private int status_counter = 1;
    private float cr_x;
    private float cr_y;
    private FrameLayout cameraPreview;
    private ImageView captureButton;
    private ImageView img;
    private Camera.PictureCallback mPicture;
    /***************************************************************************/
    private ImageView focus;
    private RelativeLayout root;
    private boolean FLASH_IS_ON = false;
    private ImageView capture;
    private LinearLayout comment;
    private String path;
    private ImageView flash;
    private ImageView camera_rotate;
    //    private TextView edit;
    private int max_zoom = 0;
    private String mCurrentPhotoPath;
    private LinearLayout options;
    private LinearLayout camera_hldr;
    private String comment_txt;
    private String[] permissions = {Manifest.permission.CAMERA
            , Manifest.permission.WRITE_EXTERNAL_STORAGE
            , Manifest.permission.READ_EXTERNAL_STORAGE};

    private SensorManager sm;
    private long lastUpdate;
    private int last_known_orientation = 0;

    private LinearLayout zoom_option;
    private ImageView zoom_in;
    private ImageView zoom_out;

    private int last_known_z = 0;
    private int last_known_y = 0;

    private int xKey, yKey, zKey;
    private Bitmap currentBm;
    private String model;

    private ImageView settingsImageView;
    private int rotateOutPutDegree = 0;
    private final String KEY_ROTATE = "CAMERA_ROTATE_OUTPUT_ROTATE";
    private final String CAMERA_SETTINGS_SHRDPREF = "CAMERA_SETTING";
    private AlertDialog cameraSettingsAlert;

    private ImageView galleryPickImg;
    private ImageView pdfPickImg;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_library);

/***************************************** camera api **********************/
        cameraPreview = findViewById(R.id.camera_preview);
        img = findViewById(R.id.img);
        top_choice = findViewById(R.id.top_choice);
        cancel = findViewById(R.id.cancel);
        tick = findViewById(R.id.tick);
        search_linear = findViewById(R.id.search_linear);
        flash = findViewById(R.id.flash);
        camera_rotate = findViewById(R.id.camera_rotate);
        focus = findViewById(R.id.focus);
        options = findViewById(R.id.options);
        rotateLeft = findViewById(R.id.rotateLeft);
        rotateRight = findViewById(R.id.rotateRight);
//        edit = findViewById(R.id.edit);
        root = findViewById(R.id.root);
        zoom_option = findViewById(R.id.zoom_options);
        zoom_in = findViewById(R.id.zoom_in);
        zoom_out = findViewById(R.id.zoom_out);
        settingsImageView = findViewById(R.id.settingsImageView);
        galleryPickImg = findViewById(R.id.galleryPickImg);
        pdfPickImg = findViewById(R.id.pdfPickImg);
        cr_x = focus.getX();
        cr_x = focus.getY();
        cameraPreview.setOnTouchListener(this);
        tick.setOnClickListener(this);
        cancel.setOnClickListener(this);
        flash.setOnClickListener(this);
        options.setOnClickListener(this);
//        edit.setOnClickListener(this);
        camera_rotate.setOnClickListener(this);
        zoom_in.setOnClickListener(this);
        zoom_out.setOnClickListener(this);
        rotateLeft.setOnClickListener(this);
        rotateRight.setOnClickListener(this);
        galleryPickImg.setOnClickListener(this);
        pdfPickImg.setOnClickListener(this);

        settingsImageView.setOnClickListener(this);

        settingsImageView.bringToFront();

        //get_permission();

        zoom_option.bringToFront();

        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        lastUpdate = System.currentTimeMillis();

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {

            folderPath = bundle.getString(SurveyKey.IMAGE_PROPS.Companion.getFOLDER_PATH());
            viewId = bundle.getString(SurveyKey.IMAGE_PROPS.Companion.getVIEW_ID());
            viewName = bundle.getString(SurveyKey.IMAGE_PROPS.Companion.getVIEW_NAME());
            typeId = bundle.getInt(SurveyKey.IMAGE_PROPS.Companion.getVIEW_ID());
            typeName = bundle.getString(SurveyKey.IMAGE_PROPS.Companion.getIMAGE_TYPE_NAME());
            pagePosition = bundle.getInt(SurveyKey.IMAGE_PROPS.Companion.getPAGE_POSITION());
            index = bundle.getInt(SurveyKey.IMAGE_PROPS.Companion.getINDEX());

        }


        // Create an instance of Camera

        create_camera();

        // Add a listener to the Capture button
        captureButton = findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        disableView();

                        rotateView(captureButton);

                        final Animation shake = AnimationUtils.loadAnimation(ActivityCamera.this, R.anim.shake);
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                shake.cancel();
                            }
                        }, 700);
                        Camera_Status = true;
                        galleryPickImg.setVisibility(View.INVISIBLE);
                        pdfPickImg.setVisibility(View.INVISIBLE);
                        camera_rotate.setVisibility(View.INVISIBLE);
                        flash.setVisibility(View.INVISIBLE);
                        zoom_option.setVisibility(View.INVISIBLE);
                        focus.setVisibility(View.VISIBLE);
                        Functions.Companion.playSound(ActivityCamera.this,R.raw.focuss);
                        focus.bringToFront();
                        focus.setAnimation(shake);
                        try {
                            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                                @Override
                                public void onAutoFocus(boolean b, Camera camera) {
                                    focus.setVisibility(View.GONE);
                                    if (b) {
                                        mCamera.takePicture(null, null, mPicture);
                                    }

                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            addEvenLog(ActivityCamera.this, -1, e.getMessage(), "Capture bottun", "N/I", path);
                            log(e.getMessage());
                        }
                    }
                }
        );
/**********************************************************************************/

        /**
         * Camera configuration
         */

        if (!SurveyConfig.Companion.isGalleryAllowed()){
            galleryPickImg.setVisibility(View.GONE);
        }

        if (!SurveyConfig.Companion.isPDFAllowed()){
            pdfPickImg.setVisibility(View.GONE);
        }


    }

    void rotateView(View view){

        RotateAnimation ra =new  RotateAnimation(
                0F, 360F,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        ra.setDuration(500);
        ra.setRepeatMode(RotateAnimation.REVERSE);
        ra.setRepeatCount(RotateAnimation.INFINITE);
        ra.setFillAfter(true);
        ra.setFillEnabled(true);
        view.startAnimation(ra);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PICK_IMAGE_CODE:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = null;
                    if (data != null) {

                        selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getContentResolver().query(selectedImage,
                                filePathColumn, null, null, null);
                        cursor.moveToFirst();
//                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String tempPath = FileUtils.getRealPath(this, selectedImage);
                        File file = new File(tempPath);
                        String picturePath = file.getAbsolutePath();

                        //Size
                        int acceptableSize = 1024;//KB

                        int file_size = Integer.parseInt(String.valueOf(file.length() / 1024));
                        cursor.close();
                        String extension = getImageExtension(picturePath);
                        if (extension.equals("")) {
                            if (file_size < acceptableSize) {
//                            If size is ok then add iamge
                                path = picturePath;
                            } else {
//                            If size is too big compress it
                                String resizedPath = compressImage(picturePath, file_size, data.getData());
                                path = resizedPath;
                            }
                        } else {
                            showToast(this, "Elija imágenes PNG, JPEG o JPG , Tu tipo de imagen es : " + extension + " Size : " + file_size);
                        }
                    } else {
                        showToast(this, "Elija imágenes PNG, JPEG o JPG , Tu tipo de imagen es");
                    }

                } else if (resultCode == 1) {
                    showToast(this, "Cancelled");
                }
                break;

            case PICK_PDF_CODE:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        // Get the URI of the selected file
                        final Uri urii = data.getData();
                        Log.i(TAG, "Uri = " + urii.toString());

                        try {
                            // Get the file path from the URI
                            final String pathh = FileUtils.getRealPath(this, urii);

                            File file = new File(pathh);
                            if (file.exists()) {
                                path = pathh;
                            }
                        } catch (Exception e) {
                            showToast(this, "Por favor, no use la carpeta reciente para elegir el archivo");
                            e.printStackTrace();
                        }
                    }
                } else {
                    showToast(this, "Cancelled");
                }
                break;

        }

        if (!path.equals("")) {
            Intent newData = new Intent();
            newData.putExtra(SurveyKey.IMAGE_PROPS.Companion.getVIEW_NAME(), viewName);
            newData.putExtra(SurveyKey.IMAGE_PROPS.Companion.getVIEW_ID(), viewId);
            newData.putExtra(SurveyKey.IMAGE_PROPS.Companion.getIMAGE_TYPE_NAME(), typeName);
            newData.putExtra(SurveyKey.IMAGE_PROPS.Companion.getIMAGE_TYPE_ID(), typeId);
            newData.putExtra(SurveyKey.IMAGE_PROPS.Companion.getINDEX(), index);
            newData.putExtra(SurveyKey.IMAGE_PROPS.Companion.getPAGE_POSITION(), pagePosition);
            newData.putExtra(SurveyKey.IMAGE_PROPS.Companion.getPATH(), path);
            setResult(RESULT_OK, newData);
            finish();
        }

    }




    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
//            setContentView(R.layout.activity_camera_landscape);

            mCamera.setDisplayOrientation(0);
//            FrameLayout.LayoutParams params = new
//                    FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT
//            , FrameLayout.LayoutParams.WRAP_CONTENT);
//            params.setMargins(50,0,50,0);
////            preview.setLayoutParams(params);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
//            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
//            setContentView(R.layout.activity_camera_library);
            mCamera.setDisplayOrientation(90);
//            FrameLayout.LayoutParams params = new
//                    FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT
//                    , FrameLayout.LayoutParams.WRAP_CONTENT);
//            params.setMargins(0,60,0,80);
//            preview.setLayoutParams(params);

        }
    }


    @Override
    public void onBackPressed() {
        this.isAppClosed = false;
        if (mCamera != null) {
            mCamera.stopPreview();
        }
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }


    @Override
    public void onDestroy() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
        }
        super.onDestroy();
    }



    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance(int camera_id) {
        Camera c = null;
        try {
            c = Camera.open(camera_id); // attempt to get a Camera instance
        } catch (Exception e) {
            e.printStackTrace();
            log(e.getMessage());
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }



    @Override
    protected void onResume() {
        this.isAppClosed = true;
        sm.registerListener(this,
                sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
//        onCreate(null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sm.unregisterListener(this);


        if (isAppClosed) {
            this.isAppClosed = false;
            setResult(1);
            finish();
        }
    }



    @SuppressLint("ResourceType")
    private void showSettingsAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View view = LayoutInflater.from(this).inflate(R.layout.layout_camera_settings_alert, root, false);

        RadioGroup cameraRotateGroup = view.findViewById(R.id.cameraRotateGroup);
        RadioButton zeroRotateBtn = view.findViewById(R.id.zeroRotateBtn);
        RadioButton rotateBtn = view.findViewById(R.id.rotateBtn);
        RadioButton rotateBtnn = view.findViewById(R.id.rotateBtnn);
        RadioButton rotateBtnnn = view.findViewById(R.id.rotateBtnnn);
        Button submitBtn = view.findViewById(R.id.submitBtn);

        int z_index = getIndexOfRotateOption();
        //TODO
        zeroRotateBtn.setId(0);
        rotateBtn.setId(1);
        rotateBtnn.setId(2);
        rotateBtnnn.setId(3);

        if (z_index == 0) {
            zeroRotateBtn.setChecked(true);
        } else if (z_index == 1) {
            rotateBtn.setChecked(true);
        } else if (z_index == 2) {
            rotateBtnn.setChecked(true);
        } else if (z_index == 3) {
            rotateBtnnn.setChecked(true);
        }

        cameraRotateGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case 0:
                        rotateOutPutDegree = 0;
                        break;

                    case 1:
                        rotateOutPutDegree = 90;
                        break;

                    case 2:
                        rotateOutPutDegree = 180;
                        break;

                    case 3:
                        rotateOutPutDegree = 270;
                        break;
                }
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setOutPutPhotoRotate();
                cameraSettingsAlert.dismiss();
            }
        });
        builder.setView(view);

        cameraSettingsAlert = builder.create();

        cameraSettingsAlert.show();

    }



    @Override
    public void onClick(View v) {

        if (rotateRight == v) {
            rotateRight();
        } else if (rotateLeft == v) {
            rotateLeft();
        } else if (settingsImageView == v) {
            showSettingsAlert();
        }

        if (v == zoom_in) {
            setZoomIn();
        }
        if (v == zoom_out) {
            setZoomOut();
        }

        if (v == cancel) {
            perfotm_cancel_pic();
        }
        if (v == tick) {
            saveImage();
            this.isAppClosed = false;
            Intent data = new Intent();
            data.putExtra(SurveyKey.IMAGE_PROPS.Companion.getVIEW_NAME(), viewName);
            data.putExtra(SurveyKey.IMAGE_PROPS.Companion.getVIEW_ID(), viewId);
            data.putExtra(SurveyKey.IMAGE_PROPS.Companion.getIMAGE_TYPE_NAME(), typeName);
            data.putExtra(SurveyKey.IMAGE_PROPS.Companion.getIMAGE_TYPE_ID(), typeId);
            data.putExtra(SurveyKey.IMAGE_PROPS.Companion.getINDEX(), index);
            data.putExtra(SurveyKey.IMAGE_PROPS.Companion.getPAGE_POSITION(), pagePosition);
            data.putExtra(SurveyKey.IMAGE_PROPS.Companion.getPATH(), path);
            setResult(RESULT_OK, data);
            finish();
        }
        if (flash == v) {
            open_flash_status();
        }
        if (camera_rotate == v) {
            rotate_camera();
        }
        if (v == galleryPickImg) {
            pickImageFromGallery();
        }
        if (v == pdfPickImg) {
            pickPdf();
        }
    }



    @Override
    public boolean onTouch(View v, MotionEvent event) {
        try {
            if (camera_id == Camera.CameraInfo.CAMERA_FACING_BACK) {


                final boolean[] IS_FOCUS_FINISHED = {false};
                int x = (int) event.getY();
                int y = (int) event.getX();

                final TranslateAnimation ta = new TranslateAnimation(cr_y, cr_x, x, y);
                ta.setDuration(1);
                ta.setFillAfter(true);
                ta.isFillEnabled();

//                focus.startAnimation(ta);

//                final Animation zoom = AnimationUtils.loadAnimation(this, R.anim.zoom_icon);
//                Animation zoom_out = AnimationUtils.loadAnimation(this, R.anim.zoom_out_icon);
                final Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
                if (IS_FOCUS_FINISHED[0]) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            shake.cancel();
                        }
                    }, 700);
                }
                if (!Camera_Status) {
                    focus.setVisibility(View.VISIBLE);
                    Functions.Companion.playSound(ActivityCamera.this,R.raw.focuss);
                    focus.bringToFront();
                    focus.setAnimation(shake);
                    if (mCamera != null) {
                        mCamera.autoFocus(new Camera.AutoFocusCallback() {
                            @Override
                            public void onAutoFocus(boolean success, Camera camera) {
                                try {
                                    focus.setVisibility(View.GONE);
                                    IS_FOCUS_FINISHED[0] = true;
                                    if (success) {
                                        MediaPlayer mediaPlayer = MediaPlayer.create(ActivityCamera.this, R.raw.focuss);
//                                    mediaPlayer.start();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    log(e.getMessage());
                                    addEvenLog(ActivityCamera.this, -1, e.getMessage(), "Focus", "N/I", path);
                                }

                            }
                        });
                    }
                }

                cr_x = x;
                cr_y = y;
            }

        } catch (Exception e) {
            e.printStackTrace();
            log(e.getMessage());
        }
        return true;
    }

    private Bitmap getRightBitmap(Bitmap bm, int x, int y, int z){
        if (z == 0) {
            if (x == 0) {
                if (bm.getHeight() < bm.getWidth()) {
//                    Toast.makeText(this, "1 -> rotate 90", Toast.LENGTH_SHORT).show();
                    bm = rotate(bm, -90);
                    String a = Build.MANUFACTURER;
                    if (Build.MANUFACTURER.toLowerCase().equals("samsung")) {
                        bm = rotate(bm, 180);
//                        Toast.makeText(this, "2 -> rotate 90", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (x == 2) {
                if (bm.getHeight() > bm.getWidth()) {
                    //its not ok
//                    Toast.makeText(this, "3 -> rotate 90", Toast.LENGTH_SHORT).show();
                    bm = rotate(bm, 90);
                }
                if (Build.MANUFACTURER.toLowerCase().equals("samsung")) {
//                    Toast.makeText(this, "4 -> rotate 90", Toast.LENGTH_SHORT).show();
                    bm = rotate(bm, 180);
                }
            } else {
                if (bm.getHeight() > bm.getWidth()) {
                    //its not ok
//                    Toast.makeText(this, "5 -> rotate 90", Toast.LENGTH_SHORT).show();
                    bm = rotate(bm, -90);
                }
            }
        } else {
            if (y == 0) {
                if (bm.getHeight() < bm.getWidth()) {
//                    Toast.makeText(this, "6 -> rotate 90", Toast.LENGTH_SHORT).show();
                    bm = rotate(bm, -90);
                }
            } else {
                if (bm.getHeight() > bm.getWidth()) {
//                    Toast.makeText(this, "7 -> rotate 90", Toast.LENGTH_SHORT).show();
                    bm = rotate(bm, -90);
                }
            }
        }
        return rotate(bm, getRotateOutPutDegree());
    }


    /**
     * create camera surface and camera
     */
    private void create_camera() {
        try {

            mPicture = new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(final byte[] data, Camera camera) {

                    xKey = last_known_orientation;
                    yKey = last_known_y;
                    zKey = last_known_z;

//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
                    if (mCamera != null) {
                        mCamera.stopPreview();
                        MediaPlayer mediaPlayer = MediaPlayer.create(ActivityCamera.this, R.raw.defult);
//                        mediaPlayer.start();
                    }

                    currentBm = BitmapFactory.decodeByteArray(data, 0, data.length);

                    model = getDeviceName();
                    currentBm = getRightBitmap(currentBm,xKey,yKey,zKey);
                    img.setImageBitmap(currentBm);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            captureButton.clearAnimation();
                            search_linear.setVisibility(View.GONE);
                            cameraPreview.setVisibility(View.GONE);
                            img.setVisibility(View.VISIBLE);
                            img.bringToFront();
                            top_choice.setVisibility(View.VISIBLE);

                            top_choice.bringToFront();

                            rotateLeft.setVisibility(View.VISIBLE);
                            rotateRight.setVisibility(View.VISIBLE);
                            rotateLeft.bringToFront();
                            rotateRight.bringToFront();
                        }
                    });

                    Functions.Companion.playSound(ActivityCamera.this,R.raw.defult);

                    enableView();

                }

            };

            mCamera = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_BACK);

            // set Camera parameters

            Camera.Parameters params = mCamera.getParameters();
            max_zoom = params.getMaxZoom();
            boolean has_FLASH = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
            this.has_flash = has_FLASH;
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
            mCamera.setDisplayOrientation(0);
            params.setRotation(90);
            if (has_FLASH) {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }
            List<Camera.Size> sizes = params.getSupportedPictureSizes();
            int cindex = getCameraSize(minWidthRange, maxWidthRange);
            params.setPictureSize(sizes.get(cindex).width, sizes.get(cindex).height);
//            params.setPreviewSize(sizes.get(cindex).width, sizes.get(cindex).height);
            zooms = params.getZoomRatios();
//        params.setZoom(zooms.get(zooms.size() - 1));
            params.setZoom(zoom);
            try {
                mCamera.setParameters(params);
            } catch (Exception e) {
                log(e.getMessage());
                e.printStackTrace();
                isParamsSet = false;
                addEvenLog(ActivityCamera.this,-1,e.getMessage(),"Create camera","N/I",path);
                Log.i(TAG, "create_camera: " + e);
                handle_catch_camera_params(has_FLASH);
            }
            // Create our Preview view and set it as the content of our activity.
            mPreview = new CameraPreview(this, mCamera);
            cameraPreview.addView(mPreview);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                mCamera.enableShutterSound(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log(e.getMessage());
            addEvenLog(ActivityCamera.this,-1,e.getMessage(),"Create camera","N/I",path);
        }
    }

    private void addEvenLog(ActivityCamera activityCamera, int i, String message, String focus, String s, String path) {

    }

    /**
     * orientation detector by accelerometer {@link Sensor}
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event);
        }
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private String compressImage(String filePath, int fileSize, Uri data) {

        try {

            int quality = 20;

            fileSize = fileSize / 1024;//Now is mb

            if (fileSize <= 1) {
                quality = 80;
            } else if (fileSize <= 2) {
                quality = 70;
            } else if (fileSize <= 3) {
                quality = 60;
            } else if (fileSize <= 4) {
                quality = 50;
            }

            String fileName = getFileName(filePath);

            // First decode with inJustDecodeBounds=true to check dimensions of image
            final BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inJustDecodeBounds = true;

            // Calculate inSampleSize(First we are going to resize the image to 800x800 image, in order to not have a big but very low quality image.
            //resizing the image will already reduce the file size, but after resizing we will check the file size and start to compress image
            options.inSampleSize = calculateInSampleSize(options, 800, 800);
            options.inMutable = true;
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            InputStream is = getContentResolver().openInputStream(data);

            Bitmap bmpPic = BitmapFactory.decodeStream(is, null, options);

            try {
                //save the resized and compressed file to disk cache
//                Log.d("compressBitmap", "cacheDir: " + context.getCacheDir());
                FileOutputStream bmpFile = new FileOutputStream(folderPath + "/" + fileName);
                bmpPic.compress(Bitmap.CompressFormat.JPEG, quality, bmpFile);
                bmpFile.flush();
                bmpFile.close();
            } catch (Exception e) {
                log(e.getMessage());
//                Log.e("compressBitmap", "Error on saving file");
            }
            //return the path of resized and compressed file
            String path = folderPath + "/" + fileName;
            return path;
        } catch (Exception e) {
            e.printStackTrace();
            return filePath;
        }

    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        String debugTag = "MemoryInformation";
        // Image nin islenmeden onceki genislik ve yuksekligi
        final int height = options.outHeight;
        final int width = options.outWidth;
        Log.d(debugTag, "image height: " + height + "---image width: " + width);
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        Log.d(debugTag, "inSampleSize: " + inSampleSize);
        return inSampleSize;
    }


    private String getFileName(String filePath) {
        String[] nameArr = filePath.split("/");
        return nameArr[nameArr.length - 1];
    }

    private String getImageExtension(String picturePath) {

        String extension = picturePath.substring(picturePath.lastIndexOf(".") + 1);

        if (extension.trim().toLowerCase(Locale.ROOT).equals("png") ||
                extension.trim().toLowerCase(Locale.ROOT).equals("jpeg") ||
                extension.trim().toLowerCase(Locale.ROOT).equals("jpg")) {
            return "";
        }
        return "-" + extension + "-";
    }

    private void setZoomIn() {
        if (zoom < max_zoom - 10) {
            zoom = zoom + 10;
            update_zoom();
        } else {
            zoom = max_zoom;
            update_zoom();
        }
    }

    private void setZoomOut() {
        if (zoom >= 10) {
            zoom = zoom - 10;
            update_zoom();
        } else {
            zoom = 0;
            update_zoom();
        }
    }

    private void update_zoom() {
//        addEvenLog(ActivityCamera.this,-1,zoom+"","Zoom camera","N/I",path);
        Camera.Parameters params = mCamera.getParameters();
        zooms = params.getZoomRatios();
        if (params.isZoomSupported()) {
            params.setZoom(zoom);
            mCamera.setParameters(params);
        }
    }

    private int camera_orientation_z(float z) {
        if (z > 8) {
//           Log.i(TAG, "camera_orientation_z: "+1);
            return 0;//means user bend phone forward
        } else {
//           Log.i(TAG, "camera_orientation_z: "+0);
            return 0;
        }
    }

    private int camera_orientation_y(float y) {
        if (y > 0) {
//            Log.i(TAG, "camera_orientation_y: "+0);
            return 0;
        } else {
//            Log.i(TAG, "camera_orientation_y: "+-1);
            return -1;
        }
    }


    private int camera_orientation_status(float x) {
        if (4 < x) {
            //this means landscape on right
//              Log.i(TAG, "camera_orientation_status: right landscape");
            return 1;
        } else if (x < -4) {
            //this means landscape on left
//              Log.i(TAG, "camera_orientation_status: left landscape");
            return 2;
        } else {
            //this is portrait
//             Log.i(TAG, "camera_orientation_status: portrait");
            return 0;
        }
    }

    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];

        log_check(x, y, z);

        this.last_known_y = camera_orientation_y(y);

//        Log.i(TAG, "getAccelerometer: "+z);

//        Log.i(TAG, "getAccelerometer: "+y);
//        TODO for anbdroid below 22 api
//        if (Build.VERSION.SDK_INT > 22) {//LOLIPOP version -> 22
        last_known_orientation = camera_orientation_status(x);//set landscape two modes and one portrait mode
        last_known_z = camera_orientation_z(z);
//        Log.i(TAG, "getAccelerometer: x ="+last_known_orientation);
//        Log.i(TAG, "getAccelerometer: z = "+last_known_z);
//        }else {
//            last_known_orientation = 0;//set only portrait mode
//        }

    }

    private void log_check(float x, float y, float z) {

        if ((x - last_number_of_x) > 5 || (last_number_of_x - x) > 5) {
            last_number_of_x = x;
            Log.i("CAMERA_INF", "Value of x -> " + x);
        }
        if ((y - last_number_of_y) > 5 || (last_number_of_y - y) > 5) {
            last_number_of_y = y;
            Log.i("CAMERA_INF", "Value of y -> " + y);
        }
        if ((z - last_number_of_z) > 5 || (last_number_of_z - z) > 5) {
            last_number_of_z = z;
            Log.i("CAMERA_INF", "Value of z -> " + z);
        }


    }

    private void pickPdf() {
        /**
         * This block of code is to pick files from storage ****************************************************************************************************************
         * Only for PDF right now                           **************************************************************************************************
         */
        isAppClosed = false;
        if (SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            } else {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                startActivityForResult(intent, PICK_PDF_CODE);
            }
        }
    }

    private void pickImageFromGallery() {
        /**
         * This block of code is to pick files from storage ****************************************************************************************************************
         * Only for PDF right now                           **************************************************************************************************
         */
        isAppClosed = false;
        if (SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            } else {
                Intent intent = new Intent(
                        Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE_CODE);
            }
        }
    }

    private void saveImage() {
        if (camera_id == Camera.CameraInfo.CAMERA_FACING_BACK) {
            //  if (last_known_orientation == 0) {
            if (model.equals("LGE")) {
                path = String.valueOf(createImageFile(rotate(currentBm, 90), xKey, yKey, zKey));
            } else {
                path = String.valueOf(createImageFile(currentBm, xKey, yKey, zKey));
            }
        } else {
            path = String.valueOf(createImageFile(rotate(currentBm, 270), xKey, yKey, zKey));
        }
    }

    private void disableView(){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                captureButton.setEnabled(false);
                galleryPickImg.setEnabled(false);
                pdfPickImg.setEnabled(false);
                settingsImageView.setEnabled(false);
                zoom_in.setEnabled(false);
                zoom_out.setEnabled(false);
                flash.setEnabled(false);
                camera_rotate.setEnabled(false);
                cameraPreview.setEnabled(false);
            }
        });

    }

    private void enableView(){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                captureButton.setEnabled(true);
                galleryPickImg.setEnabled(true);
                pdfPickImg.setEnabled(true);
                settingsImageView.setEnabled(true);
                zoom_in.setEnabled(true);
                zoom_out.setEnabled(true);
                flash.setEnabled(true);
                camera_rotate.setEnabled(true);
                cameraPreview.setEnabled(true);
            }
        });

    }

    /**
     * rotate camera if camera id changed
     */
    private void rotate_camera() {
        mCamera.stopPreview();
        if (camera_id == Camera.CameraInfo.CAMERA_FACING_BACK) {
            camera_id = Camera.CameraInfo.CAMERA_FACING_FRONT;
            flash.setVisibility(View.INVISIBLE);
            zoom_option.setVisibility(View.INVISIBLE);
            focus.setVisibility(View.INVISIBLE);
        } else {
            camera_id = Camera.CameraInfo.CAMERA_FACING_BACK;
            flash.setVisibility(View.VISIBLE);
            zoom_option.setVisibility(View.VISIBLE);
        }
        cameraPreview.removeAllViews();
        mCamera.release();
        mCamera = getCameraInstance(camera_id);
        mPreview = new CameraPreview(this, mCamera);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mPreview.setLayoutParams(params1);
        cameraPreview.addView(mPreview);

        // set Camera parameters
        Camera.Parameters params = mCamera.getParameters();
        boolean has_FLASH = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (has_FLASH && camera_id == Camera.CameraInfo.CAMERA_FACING_BACK) {
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }
//         params.setJpegThumbnailQuality(300);

        params.setJpegQuality(90);
        mCamera.setDisplayOrientation(0);
        params.setRotation(90);
        //if (!android.os.Build.MANUFACTURER.equals("HUAWEI")){

//        params.setJpegQuality(100);
        List<Camera.Size> sizes = params.getSupportedPictureSizes();
        int cindex = getCameraSize(minWidthRange, maxWidthRange);

        params.setPictureSize(sizes.get(cindex).width, sizes.get(cindex).height);
//        params.setPreviewSize(sizes.get(cindex).width, sizes.get(cindex).height);


        params.setZoom(zoom);
        try {
            mCamera.setParameters(params);
        } catch (Exception e) {
            log(e.getMessage());
            handle_catch_camera_params(has_FLASH);
            e.printStackTrace();
            addEvenLog(ActivityCamera.this, -1, e.getMessage(), "Rotate camera", "N/I", path);
            isParamsSet = false;
            Log.i(TAG, "rotate_camera: " + e);
        }

        mCamera.startPreview();

    }

    /**
     * set flash on/off/auto
     *
     * @param status
     */
    private void set_flash_status(int status) {
        boolean has_FLASH = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        Camera.Parameters params = mCamera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
//        params.setJpegQuality(300);
        if (has_FLASH) {
            params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
        }
//        params.setJpegThumbnailQuality(300);

        RotateAnimation ra = new RotateAnimation(0, 360, 50.0f, 50.0f);
        ra.setDuration(200);
        ra.setRepeatCount(0);
        ra.setRepeatMode(Animation.INFINITE);
        flash.setAnimation(ra);

        switch (status) {
            case 0:
                if (has_FLASH) {
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    flash.setImageResource(R.drawable.no_flash);
                }
                break;
            case 1:
                if (has_FLASH) {
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    flash.setImageResource(R.drawable.flash);
                }
                break;
            case 2:
                if (has_FLASH) {
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                    flash.setImageResource(R.drawable.auto_flash);
                }
                break;
        }

//        params.setJpegQuality(100);


        List<Camera.Size> sizes = params.getSupportedPictureSizes();
        int cindex = getCameraSize(minWidthRange, maxWidthRange);

        params.setPictureSize(sizes.get(cindex).width, sizes.get(cindex).height);
//        params.setPreviewSize(sizes.get(cindex).width, sizes.get(cindex).height);


        params.setZoom(zoom);
        // if (!android.os.Build.MANUFACTURER.equals("HUAWEI")){
        try {
            mCamera.setParameters(params);
        } catch (Exception e) {
            log(e.getMessage());
            handle_catch_camera_params(has_FLASH);
            isParamsSet = false;
            e.printStackTrace();
            addEvenLog(ActivityCamera.this, -1, e.getMessage(), "set flash status", "N/I", path);
        }
        // }


        options.setVisibility(View.INVISIBLE);
        flash.setVisibility(View.VISIBLE);
        zoom_option.setVisibility(View.VISIBLE);

    }

    /**
     * handle flash
     */
    private void open_flash_status() {
        //flash.setVisibility(View.INVISIBLE);
        //options.setVisibility(View.VISIBLE);

        set_flash_status(status_counter);
        // 0 -- 2
        if (status_counter == 2) {
            status_counter = 0;
        } else {
            status_counter++;
        }


    }


    private void perfotm_cancel_pic() {
        Camera_Status = false;
        rotateLeft.setVisibility(View.GONE);
        rotateRight.setVisibility(View.GONE);
        cameraPreview.setVisibility(View.VISIBLE);
        img.setVisibility(View.GONE);
        top_choice.setVisibility(View.INVISIBLE);
        search_linear.setVisibility(View.VISIBLE);
        search_linear.bringToFront();
        img.setVisibility(View.INVISIBLE);
        flash.bringToFront();
        options.bringToFront();
        mCamera.startPreview();
        camera_rotate.setVisibility(View.VISIBLE);
        flash.setVisibility(View.VISIBLE);
        zoom_option.setVisibility(View.VISIBLE);
        camera_rotate.bringToFront();
//        edit.setVisibility(View.INVISIBLE);

        if (!SurveyConfig.Companion.isGalleryAllowed()){
            galleryPickImg.setVisibility(View.GONE);
        }else{
            galleryPickImg.setVisibility(View.VISIBLE);
        }

        if (!SurveyConfig.Companion.isPDFAllowed()){
            pdfPickImg.setVisibility(View.GONE);
        }else{
            galleryPickImg.setVisibility(View.VISIBLE);
        }

    }


    private int getIndexOfRotateOption() {
        int rotation = getRotateOutPutDegree();

        if (rotation == 90) {
            return 1;
        } else if (rotation == 180) {
            return 2;
        } else if (rotation == 270) {
            return 3;
        }
        return 0;
    }

    private void setOutPutPhotoRotate() {

        SharedPreferences.Editor editor = getSharedPreferences(CAMERA_SETTINGS_SHRDPREF, MODE_PRIVATE).edit();
        editor.putInt(KEY_ROTATE, rotateOutPutDegree).apply();

    }

    private int getRotateOutPutDegree() {

        int degree = getSharedPreferences(CAMERA_SETTINGS_SHRDPREF, MODE_PRIVATE).getInt(KEY_ROTATE, 0);
        return degree;
    }

    public static void log(String msg) {

    }


    public static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private boolean isFromZero(List<Camera.Size> sizes) {

        int startIndex = 0;
        int endIndex = sizes.size() - 1;

        int startWidth = sizes.get(startIndex).width;
        int endWidth = sizes.get(endIndex).width;

        if (startWidth < endWidth) {
            return true;
        }

        return false;
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
//        String model = Build.MODEL;
//        showToast(this,manufacturer);
        return manufacturer;
    }

    private int getCameraSize(int minRange, int maxRange) {
        int cindex = -1;
        boolean isFromZero;

        Camera.Parameters params = mCamera.getParameters();
        List<Camera.Size> sizes = params.getSupportedPictureSizes();

        isFromZero = isFromZero(sizes);

        for (int i = 0; i < sizes.size(); i++) {

            if (sizes.get(i).width < maxRange && sizes.get(i).width > minRange) {
                cindex = i;
                break;
            }
        }

        if (cindex == -1) {//no match found
            if (isFromZero) {
                cindex = 1;
            } else {
                cindex = sizes.size() - 2;
            }
        }

        return cindex;

    }

    /**
     * handle if user phone not have flash
     *
     * @param has_FLASH
     */
    private void handle_catch_camera_params(boolean has_FLASH) {
        try {
            Camera.Parameters paramss = mCamera.getParameters();
            paramss.setRotation(90);
            if (has_FLASH) {
                paramss.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }
            paramss.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
            paramss.setZoom(zoom);
            mCamera.setParameters(paramss);
//            Toast.makeText(this, "parameters got error", Toast.LENGTH_SHORT).show();
            showToast(this, "parameters got error");
        } catch (Exception e) {
            e.printStackTrace();
            log(e.getMessage());
            addEvenLog(ActivityCamera.this, -1, e.getMessage(), "Handle catch camera params", "N/I", path);
        }
    }

    private void showToast(ActivityCamera activityCamera, String parameters_got_error) {
        //TODO
    }



    /**
     * get bitmap and create file in app folder
     *
     * @param bm
     * @return
     */

    private File createImageFile(Bitmap bm, int x, int y, int z) {
        if (bm == null)
            return new File("");


//        bm = getRightBitmap(bm,x,y,z);


//        createMandatoryFolders();

//
        String root = getExternalFilesDir(null).toString();
        File myDir = new File(folderPath);
        // myDir.mkdirs();
        String fname = System.currentTimeMillis() + "_" + x + ".jpg";
        File file = new File(myDir, fname);
        Log.i(TAG, "" + file);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            log(e.getMessage());
            addEvenLog(ActivityCamera.this, -1, e.getMessage(), "Create image file", "N/I", path);
            Log.i(TAG, "createImageFile: " + e);
        }
//        remove_image_from_pictures(fname);
        return file;
    }

    private void rotateRight() {
        currentBm = rotate(currentBm, 90);
        img.setImageBitmap(currentBm);
    }

    private void rotateLeft() {
        currentBm = rotate(currentBm, -90);
        img.setImageBitmap(currentBm);
    }

    /**
     * A basic Camera preview class
     */
    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera mCamera;

        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;
            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, now tell the camera where to draw the preview.
            try {

                Camera.Parameters parameters = mCamera.getParameters();
                Log.i(TAG, "surfaceCreated: " + parameters);
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
                parameters.setJpegQuality(90);
                int f = this.getResources().getConfiguration().orientation;
                if (this.getResources().getConfiguration().orientation
                        != Configuration.ORIENTATION_LANDSCAPE
                ) {
                    parameters.set("orientation", "portrait");
                    List<Integer> zooms = parameters.getZoomRatios();
                    parameters.setZoom(zooms.get(zooms.size() - 1));
                    mCamera.setDisplayOrientation(90);
//                        parameters.setRotation(90);
                }
//                parameters.setPictureSize(C,100);
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();


                // mCamera.setParameters(parameters);//commented
            } catch (IOException e) {
                log(e.getMessage());
                Log.d(TAG, "Error setting camera preview: " + e.getMessage());
            }

        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // empty. Take care of releasing the Camera preview in your activity.
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null) {
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                log(e.getMessage());
                // ignore: tried to stop a non-existent preview
            }

            // set preview size and make any resize, rotate or
            // reformatting changes here

            // start preview with new settings
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            } catch (Exception e) {
                log(e.getMessage());
                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            }
        }
    }
}
