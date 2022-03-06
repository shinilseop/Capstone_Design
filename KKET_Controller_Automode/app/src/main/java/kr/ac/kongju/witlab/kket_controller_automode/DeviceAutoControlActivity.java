package kr.ac.kongju.witlab.kket_controller_automode;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import kr.ac.kongju.witlab.kket_controller_automode.database.PacketDatabaseManager;

import static kr.ac.kongju.witlab.kket_controller_automode.database.QueryRepository.Drop.QUERY_DROP_TABLE_CTRLPKT;

/**
 * Created by WitLab on 2018-05-04.
 */

public class DeviceAutoControlActivity extends Activity {
    //constants
    private static final String TAG = DeviceAutoControlActivity.class.getSimpleName();
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    //BLE components
    private BluetoothLeService mBluetoothLeService;

    //view components
    private Button btnStart;
    private EditText edtxtInterval;
    private EditText edtxtStart;
    private EditText edtxtEnd;
    private TextView tvLogView;

    private EditText edtxtQuery;
    private Button btnDebug;

    //Jake's components
    private boolean mConnected = false;
    private String mDeviceName;
    private String mDeviceAddress;
    private Thread thAutomode;
    private boolean automode_on = false;
    private PacketDatabaseManager pdm = null;

    //BLE service interfaces
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }


        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
//                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
//                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
//                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
//                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
//                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auto_control_activity);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        pdm = new PacketDatabaseManager(this);
        pdm.open();
        pdm.create();

        bind();
        setListeners();

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private void bind() {
        btnStart = findViewById(R.id.btnStart);
        edtxtInterval = findViewById(R.id.edtxtInterval);
        edtxtStart = findViewById(R.id.edtxtStart);
        edtxtEnd = findViewById(R.id.edtxtEnd);
        tvLogView = findViewById(R.id.tvLogView);
        tvLogView.setMovementMethod(ScrollingMovementMethod.getInstance());

        edtxtQuery = findViewById(R.id.edtxtQuery);
        btnDebug = findViewById(R.id.btnDebug);
    }

    private void setListeners() {
        btnStart.setOnClickListener((View view) -> {
            final int interval = Integer.parseInt(edtxtInterval.getText().toString()) * 1000;
            final int start = Integer.parseInt(edtxtStart.getText().toString());
            final int end = Integer.parseInt(edtxtEnd.getText().toString());
            if (!automode_on) { //꺼져있으면
                automode_on = true; //켬
                if (thAutomode == null) {
                    thAutomode = new Thread(() -> {
                        //TODO: send packet continually
                        runOnUiThread(() -> tvLogView.append(">> packet send start : " + start + " to " + end + "\n"));

                        for (int i = start; (i <= end) && automode_on; i++) {
                            byte[] send_packet=pdm.getPacket(i);
                            Log.d("setListeners()",PacketDatabaseManager.byteArrayToHex(send_packet));
                            mBluetoothLeService.writeCustomCharacteristic(send_packet);
                            runOnUiThread(() -> tvLogView.append(pdm.selectLastLogToString() + "\n"));
                            try { Thread.sleep(interval); } catch (InterruptedException e) {}
                        }

                        runOnUiThread(() -> {
                            tvLogView.append(">> packet send end\n");
                            btnStart.setText("start");
                        });
                        automode_on = false; //끔
                    });
                }
                thAutomode.start();
                btnStart.setText("stop");
            } else { //켜져있으면
                thAutomode.interrupt();
                thAutomode = null;
                automode_on = false; //끔
                btnStart.setText("start");
            }
        });

        btnDebug.setOnClickListener((View view) -> {
            //debug
            String query = edtxtQuery.getText().toString();
            edtxtQuery.setText("");
            //view query result
            tvLogView.append(pdm.selectDebug(query));
//            Log.d("debug","select * from control_packet => " + pdm.selectDebug());
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (thAutomode != null) {
            thAutomode.interrupt();
            thAutomode = null;
            automode_on = false;
        }

        pdm.close();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;


//        System.exit(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
