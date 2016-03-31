package com.heartbeats.app.heartbeatsflashlight;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.util.Log;
import android.view.View.OnClickListener;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;


public class MainActivity extends Activity {

    //flag to detect flash is on or off
    private boolean isLighOn = true;
    private boolean lightTriger = false;
    private CameraManager manager;
    private String cameraId;

    static Camera camera = null;
    Thread thread1 = null;
    private Button button;

    @Override
    protected void onStop() {
        super.onStop();

        if (camera != null) {
            camera.release();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.buttonFlashlight);

        Context context = this;
        PackageManager pm = context.getPackageManager();

        // if device support camera?
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Log.e("err", "Device has no camera!");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            try {
                cameraId = findCamera();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        camera = Camera.open();
        final Parameters p = camera.getParameters();

        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                lightTriger = !lightTriger;
                if (thread1 == null) {
                    thread1 = new Thread() {
                        public void run() {
                            lightLoop(p);
                        }
                    };
                    Log.w("HearBeats", "Starting Thread");
                    thread1.start();

                } else {
                    Log.w("HearBeats", "STOP");
                    try {
                        thread1.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    thread1 = (null);


                }

            }
        });

    }

    void lightswitch(Parameters p){
        if (isLighOn) {

            activateTorch(p);

        } else {

            deactivateTorch(p);

        }
    }

    void lightLoop(Parameters p){
        while (lightTriger) {

            lightswitch(p);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            lightswitch(p);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            lightswitch(p);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lightswitch(p);
            try {
                Thread.sleep(650);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.w("HearBeats", "On Pause. Kill thread");
        lightTriger = false;
        if (thread1 != null) {
            try {
                thread1.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            thread1 = (null);
            if (camera != null) {
                camera.release();
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w("HearBeats", "On Resume");
        thread1=(null);
        try {

            releaseCameraAndPreview();

            camera = Camera.open();
            camera.startPreview();
            camera.autoFocus(new Camera.AutoFocusCallback() {
                public void onAutoFocus(boolean success, Camera camera) {
                }
            });
            final Parameters p = camera.getParameters();
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }
    }

    private void releaseCameraAndPreview() {

        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    private void activateTorch(Parameters p){
        Log.i("info", "torch is turn on!");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Call M APIs here
            if (camera != null) {
                camera.stopPreview();
                camera.release();
                camera = null;
            }
            try {
                manager.setTorchMode(cameraId, true);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            p.setFlashMode(Parameters.FLASH_MODE_TORCH);

            camera.setParameters(p);
            camera.startPreview();
            isLighOn = false;


        }
    }
    private void deactivateTorch(Parameters p){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Call M APIs here
            if (camera != null) {
                camera.stopPreview();
                camera.release();
                camera = null;
            }
            try {
                manager.setTorchMode(cameraId, false);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            Log.i("info", "torch is turn off!");

            p.setFlashMode(Parameters.FLASH_MODE_OFF);

            camera.setParameters(p);
            camera.stopPreview();
            isLighOn = true;


        }
    }
    private String findCamera() throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (final String cameraId : manager.getCameraIdList()) {
                if (manager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
                    return cameraId;
                }
            }}
            throw new Exception("Could not find a camera that has a flash");
        }

}