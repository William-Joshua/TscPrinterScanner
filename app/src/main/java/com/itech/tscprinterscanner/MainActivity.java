package com.itech.tscprinterscanner;
// import static android.content.ContentValues.TAG;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.decode.BarcodeManager;
import com.android.decode.DecodeResult;
import com.android.decode.ReadListener;
import com.android.decode.StartListener;
import com.android.decode.StopListener;
import com.android.decode.TimeoutListener;
import com.example.tscdll.TSCActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private TSCActivity TscDll = new TSCActivity();//BT
    private BarcodeManager mBarcodeManager;
    private ReadListener readListener;
    private StartListener startListener;
    private StopListener stopListener;
    private TimeoutListener timeoutListener;
    private String ScanResult;
    private Button scanButton;
    private Button openBTButton;
    private EditText resultEditText;
    private EditText tscWifiText;
    private ListView resultHistory;

    private static final int REQUEST_LOCATION_PERMISSION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scanButton = findViewById(R.id.scanButton);
        openBTButton= findViewById(R.id.openBTButton);
        resultEditText = findViewById(R.id.scanResult);
        tscWifiText = findViewById(R.id.tscWifi);
        resultHistory = findViewById(R.id.scanHistory);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 初始化扫描仪器
                readScanner();
            }
        });

        openBTButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isBluetoothConnected()) {
                    Toast.makeText(MainActivity.this, "蓝牙已连接", Toast.LENGTH_SHORT).show();
                }
                else {
                    // 蓝牙未连接，打开蓝牙设置
                    openBluetoothSettings();
                }
            }
        });

    }

    private boolean isBluetoothConnected() {
        Log.d(TAG, "isBluetoothConnected: always true");
        return true;
    }

    public static final int RC_LOCATION_PERM = 123;
    private static final String[] LOCATION_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private void requestLocationPermission() {
        if (!EasyPermissions.hasPermissions(this, LOCATION_PERMS)) {
            EasyPermissions.requestPermissions(this, "需要位置权限来使用蓝牙功能",
                    RC_LOCATION_PERM, LOCATION_PERMS);
        } else {
            // 已经有权限，可以执行蓝牙操作
            doBluetoothOperation();
        }
    }

    private void doBluetoothOperation() {
        Log.d(TAG, "doBluetoothOperation: do something");
    }

    private void openBluetoothSettings() {
        Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(intent);
    }

    private void readScanner() {
        if(this.mBarcodeManager.isInitialized())
        {
            this.mBarcodeManager.startDecode();
        }
    }

    private  void  printTscMark(){
        TscDll.sendcommand("SIZE 75 mm, 50 mm\r\n");
        TscDll.sendcommand("SPEED 4\r\n");
        TscDll.sendcommand("DENSITY 12\r\n");
        TscDll.sendcommand("CODEPAGE UTF-8\r\n");
        TscDll.sendcommand("SET TEAR ON\r\n");
        TscDll.sendcommand("SET COUNTER @1 1\r\n");
        TscDll.sendcommand("@1 = \"0001\"\r\n");

        TscDll.clearbuffer();
        TscDll.sendcommand("TEXT 100,300,\"ROMAN.TTF\",0,12,12,@1\r\n");
        TscDll.sendcommand("TEXT 100,400,\"ROMAN.TTF\",0,12,12,\"TEST FONT\"\r\n");
        TscDll.barcode(100, 100, "128", 100, 1, 0, 3, 3, "123456789");
        TscDll.printerfont(100, 250, "3", 0, 1, 1, "987654321");
        TscDll.printlabel(2, 1);
    }

    private boolean initializeScanner() {
        try {
            BarcodeManager localBarcodeManager = new BarcodeManager();
            localBarcodeManager.isInitialized();
            this.mBarcodeManager = localBarcodeManager;
            this.readListener = new ReadListener() {
                public void onRead(DecodeResult DecodeResult) {

                    Log.e(TAG, "onRead   BarcodeID" + DecodeResult.getBarcodeID().toString());
                    Log.e(TAG, "BarcodeText" + DecodeResult.getText());
                    ScanResult = DecodeResult.getText();
                    Log.e(TAG, "ScanResult" + ScanResult);
                    resultEditText.setText(ScanResult);

                }
            };
            this.startListener = new StartListener() {
                public void onScanStarted() {
                    Log.e(TAG, "onstart   BarcodeID");
                }
            };
            this.stopListener = new StopListener() {
                public void onScanStopped() {
                    Log.e(TAG, "onstop   BarcodeID");
                }
            };
            this.timeoutListener = new TimeoutListener() {
                public void onScanTimeout() {
                    Log.e(TAG, "onTimeout   BarcodeID");
                }
            };

            this.mBarcodeManager.addReadListener(this.readListener);
            this.mBarcodeManager.addStartListener(this.startListener);
            this.mBarcodeManager.addStopListener(this.stopListener);
            this.mBarcodeManager.addTimeoutListener(this.timeoutListener);
            return true;
        } catch (Exception localException) {
            return false;
        }
    }

    private  boolean initializeTscPrinter() {
        try {
            TscDll.openport(tscWifiText.getText().toString(),9100);
            return true;

        } catch (Exception ex) {
            return false;
        }
    }

    private void addToHistory(String scanResult) {
        // 获取当前时间
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

    }

    protected void onResume() {
        super.onResume();
        initializeScanner();
        //initializeTscPrinter();
    }

    protected void onPause() {
        super.onPause();
        this.mBarcodeManager.removeReadListener(this.readListener);
        this.mBarcodeManager.removeStartListener(this.startListener);
        this.mBarcodeManager.removeStopListener(this.stopListener);
        this.mBarcodeManager.removeTimeoutListener(this.timeoutListener);
        //TscDll.closeport(5000);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

}

