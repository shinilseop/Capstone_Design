package com.example.first;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    // GPS Tracker class
    private Func_GPS gps;
    // Calculate Solar Zenith class
    private Func_SolarZenith funcSolarZenith;
    // Calculate UVI class
    private Func_UVI funcUVI;
    // Notice UVI class
    private Func_Notice funcNotice;
    // BackPressCloseHandler class
    private BackPressCloseHandler backPressCloseHandler;

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private TextView illumText;
    private TextView solarZenithText;
    private TextView gpsText;
    private TextView uviText;
    private TextView notice;
    private TextView noticeText;
    private Button illumButton;
    private Button startButton;
    private Button Button_uvi;
    private Button Button_user;
    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private final int PERMISSIONS_EXTERNAL_STORAGE = 1002;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isExternalStorage = false;
    private boolean isPermission = false;
    private boolean isPermission2 = false;
    private boolean illumFlag;
    private int illumValue;
    private double latitude;
    private double longitude;
    private double solarZenith;
    private float uvi;
    private String directory;
    private String modelPath;
    private final int CODE_uvi = 999;
    private final int CODE_user = 1000;
    private float Age;
    private float PBE;
    private float STF;
    private float time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        backPressCloseHandler = new BackPressCloseHandler(this);

        if (!isPermission2) {
            callPermission2();
        }

        // model 파일 다운로드
        directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/tfmodel/") + "";
        CallbackToDownloadFile cbToDownloadFile = new CallbackToDownloadFile(
                directory,
                "model.tflite"
        );
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://210.102.142.14/model.tflite")
                .build();
        client.newCall(request).enqueue(cbToDownloadFile);

        // 조도 측정
        illumText = (TextView) findViewById(R.id.illumText);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (lightSensor == null) {
            Toast.makeText(MainActivity.this, "조도 센서를 찾을 수 없습니다!", Toast.LENGTH_SHORT).show();
            illumButton.setEnabled(true);
        }
        illumFlag = true;
        illumButton = (Button) findViewById(R.id.illumButton);
        illumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (illumFlag) {
                    illumFlag = false;
                    illumButton.setText("측정 중지");
                    sensorManager.registerListener(MainActivity.this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
                } else {
                    illumFlag = true;
                    illumButton.setText("조도 측정");
                    sensorManager.unregisterListener(MainActivity.this);
                }
            }
        });



        Button_uvi = (Button) findViewById(R.id.uvi_info);
        Button_uvi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, uviActivity.class);
                intent.putExtra("UVI",String.valueOf(uviText.getText()));
                intent.putExtra("age",String.valueOf(Age));
                intent.putExtra("PBE",String.valueOf(PBE));
                intent.putExtra("STF",String.valueOf(STF));
                intent.putExtra("time",String.valueOf(time));
                startActivityForResult(intent, CODE_uvi);
            }
        });

        Button_user = (Button) findViewById(R.id.user_info);
        Button_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, userActivity.class);
                startActivityForResult(intent,CODE_user);
            }
        });

        // GPS 측정 및 태양천정각, UVI 계산
        gpsText = (TextView) findViewById(R.id.gpsText);
        solarZenithText = (TextView) findViewById(R.id.solarZenithText);
        uviText = (TextView) findViewById(R.id.uviText);
        notice = (TextView) findViewById(R.id.notice);
        noticeText = (TextView) findViewById(R.id.noticeText);
        startButton = (Button) findViewById(R.id.setupButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 권한 요청
                if (!isPermission) {
                    callPermission();
                    return;
                }

                gps = new Func_GPS(MainActivity.this);
                funcSolarZenith = new Func_SolarZenith();
                funcUVI = new Func_UVI();
                funcNotice = new Func_Notice();

                // GPS 사용유무 가져오기
                if (gps.isGetLocation()) {
                    // gps 출력
                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();
                    gpsText.setText("위도 : " + String.format("%.2f", latitude) + "°\n경도 : " + String.format("%.2f", longitude) + "°");

                    // 태양천정각 출력
                    solarZenith = funcSolarZenith.getSolarZenith(latitude);
                    solarZenithText.setText(solarZenith + "°");

                    // UVI 출력
                    modelPath = directory + "/model.tflite";
//                    System.out.println(directory+" "+modelPath);
                    uvi = funcUVI.Output((float) solarZenith, (float) illumValue, modelPath);
                    uviText.setText(String.format("%.5f", uvi));

                    // UVI 단계별 안내 출력
                    funcNotice.changeNotice(uvi, uviText, notice, noticeText);

                    gps.stopUsingGPS();
                } else {
                    // GPS를 사용할 수 없을경우
                    gps.showSettingAlert();
                }
            }
        });
        callPermission();  // 권한 요청
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT) {
            illumValue = (int) sensorEvent.values[0];
            illumText.setText("" + illumValue + " lx");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }

    // OkHttp - model 파일 다운로드 클래스
    private class CallbackToDownloadFile implements Callback {
        private File directory;
        private File fileToBeDownloaded;

        public CallbackToDownloadFile(String directory, String fileName) {
            this.directory = new File(directory);
            this.fileToBeDownloaded = new File(this.directory.getAbsolutePath() + "/" + fileName);
        }

        @Override
        public void onFailure(Request request, IOException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(
                            MainActivity.this,
                            "파일을 다운로드할 수 없습니다. 인터넷 연결을 확인하세요.",
                            Toast.LENGTH_SHORT
                    ).show();
                    finish();
                }
            });
        }

        @Override
        public void onResponse(Response response) throws IOException {
            if (!this.directory.exists()) {
                this.directory.mkdir();
            }
            if (this.fileToBeDownloaded.exists()) {
                this.fileToBeDownloaded.delete();
            }
            try {
                this.fileToBeDownloaded.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(
                                MainActivity.this,
                                "다운로드 파일을 생성할 수 없습니다.",
                                Toast.LENGTH_SHORT
                        ).show();
                        finish();
                    }
                });
                return;
            }

            InputStream is = response.body().byteStream();
            OutputStream os = new FileOutputStream(this.fileToBeDownloaded);
            final int BUFFER_SIZE = 2048;
            byte[] data = new byte[BUFFER_SIZE];
            int count;
            long total = 0;

            while ((count = is.read(data)) != -1) {
                total += count;
                os.write(data, 0, count);
            }
            os.flush();
            os.close();
            is.close();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(
                            MainActivity.this,
                            "다운로드가 완료되었습니다.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        }
    }

    // 권한 요청
    private void callPermission() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_ACCESS_FINE_LOCATION);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else {
            isPermission = true;
        }
    }

    private void callPermission2() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "저장소 사용을 위해 읽기/쓰기 권한 필요", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_EXTERNAL_STORAGE);
            } else {
                isPermission2 = true;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_ACCESS_FINE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            isAccessFineLocation = true;
        } else if (requestCode == PERMISSIONS_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            isAccessCoarseLocation = true;
        } else if (requestCode == PERMISSIONS_EXTERNAL_STORAGE
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            isExternalStorage = true;
        }
        if (isAccessFineLocation && isAccessCoarseLocation) {
            isPermission = true;
        }
        if (isExternalStorage) {
            isPermission2 = true;
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == CODE_uvi){
            if(resultCode==RESULT_OK){

            }
        }
        else if(requestCode==CODE_user){
            if(resultCode==RESULT_OK){
                Age = Float.parseFloat(data.getExtras().getString("age"));
                PBE = Float.parseFloat(data.getExtras().getString("PBE"));
                STF = Float.parseFloat(data.getExtras().getString("STF"));
                time = Float.parseFloat(data.getExtras().getString("time"));
            }
        }
    }
}