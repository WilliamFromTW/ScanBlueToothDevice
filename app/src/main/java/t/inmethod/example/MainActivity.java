package t.inmethod.example;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.StringTokenizer;
import java.util.Vector;

import inmethod.android.ApplicationContextHelper;
import inmethod.android.bt.BTInfo;
import inmethod.android.bt.handler.DiscoveryServiceCallbackHandler;
import inmethod.android.bt.interfaces.IDiscoveryService;
import inmethod.android.bt.le.LeDiscoveryService;
import t.inmethod.viewdesign.R;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "RecyclerViewExample";

    private boolean mScanning;
    private IDiscoveryService aDiscoveryService = null;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ENABLE_LOCATON = 2;
    private static final int REQUEST_PREMISSION_LOCATON = 3;

    private boolean bPermissionAndService = false;
    protected Activity activity;
    private RecyclerAdapterForDevice adapter;
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_recycler);
        activity = this;
    }


    public void grantLocationService() {
        // enable location service
        LocationManager locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // show open gps message
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Info");
            builder.setMessage("this app need to  enable location service");
            builder.setPositiveButton("OK", new
                    android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent enableGPSIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(enableGPSIntent, REQUEST_ENABLE_LOCATON);
                        }
                    });
            builder.show();
        } else if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PREMISSION_LOCATON);
                }
            });
            builder.show();
        } else bPermissionAndService = true;


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case REQUEST_PREMISSION_LOCATON:
                bPermissionAndService = true;
                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_DENIED) {
                        bPermissionAndService = false;
                    }
                }

                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_CANCELED) {
                    if (aDiscoveryService != null && aDiscoveryService.isDiscovering())
                        aDiscoveryService.cancelDiscovery();
                    if (mScanning) {
                        mScanning = false;
                        invalidateOptionsMenu();
                    }
                    bPermissionAndService = false;
                } else {
                    bPermissionAndService = true;
                }

                break;
            case REQUEST_ENABLE_LOCATON:
                if (resultCode == Activity.RESULT_CANCELED) {
                    if (aDiscoveryService != null && aDiscoveryService.isDiscovering())
                        aDiscoveryService.cancelDiscovery();
                    if (mScanning) {
                        mScanning = false;
                        invalidateOptionsMenu();
                    }
                    bPermissionAndService = false;
                } else
                    bPermissionAndService = true;
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.scan_menu_items, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.scan_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                adapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
            case R.id.ic_menu_setting:
                scanLeDevice(false);
//                Intent Intent = new Intent(ActBtMainScan.this, ActPrefs.class);
                //               startActivity(Intent);
                break;
            case R.id.ic_menu_info_details:
                Intent serverIntent = new Intent(activity, ActAppInfo.class);
                try {
                    serverIntent.putExtra("AppInfo", "APP Version : " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName + ":" + getPackageManager().getPackageInfo(getPackageName(), 0).versionCode );
                } catch (PackageManager.NameNotFoundException e) {
                    serverIntent.putExtra("AppInfo", "Base Lib Version :  Error");
                }
                startActivity(serverIntent);
                break;
        }
        return true;
    }


    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mScanning = true;
            try {
                if (aDiscoveryService.isRunning()) {
                    aDiscoveryService.doDiscovery();
                } else
                    aDiscoveryService.startService();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                mScanning = false;
                if (aDiscoveryService.isDiscovering())
                    aDiscoveryService.cancelDiscovery();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        invalidateOptionsMenu();
    }

    public class MyDiscoveryServiceCallbackHandler extends DiscoveryServiceCallbackHandler {

        public void StartServiceStatus(boolean bStatus, int iCode) {
            if (bStatus && iCode == DiscoveryServiceCallbackHandler.START_SERVICE_SUCCESS) {
                aDiscoveryService.doDiscovery();
            } else if (!bStatus && iCode == DiscoveryServiceCallbackHandler.START_SERVICE_BLUETOOTH_NOT_ENABLE) {
                Toast.makeText(activity, "SERVICE_BLUETOOTH_NOT_ENABLE ", Toast.LENGTH_SHORT).show();
            }

        }

        public void DeviceDiscoveryStatus(boolean bStatus, final BTInfo aBTInfo) {

            if (bStatus) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Device found! device name = " + aBTInfo.getDeviceName() + ",bt type=" + aBTInfo.getDeviceBlueToothType());
                        DeviceInfo aDeviceInfo = new DeviceInfo(aBTInfo);

                        adapter.addData(aDeviceInfo);
                        adapter.notifyDataSetChanged();
                    }
                });
            } else if (!bStatus) {
                Log.d(TAG, "Device not found!");
                if (mScanning) {
                    try {
                        mScanning = false;
                        if (aDiscoveryService.isDiscovering())
                            aDiscoveryService.cancelDiscovery();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                invalidateOptionsMenu();
            }
        }

        @Override
        public void discoveryFinished() {
            scanLeDevice(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() mLeDeviceListAdapter=" + adapter);
        init();
        scanLeDevice(true);
    }


    private void init() {
        grantLocationService();
        if (!bPermissionAndService) return;

        //Toast.makeText(this,  "start communication", Toast.LENGTH_SHORT).show();
        rv = (RecyclerView) findViewById(R.id.rv);
        adapter = new RecyclerAdapterForDevice(this);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setAdapter(adapter);
        adapter.setOnItemClickListener(new RecyclerAdapterForDevice.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(activity, "position=" + position, Toast.LENGTH_LONG).show();
            }
        });
        Log.d(TAG, "scan BLE (single or dual) device");
        aDiscoveryService = LeDiscoveryService.getInstance();
        Log.i(TAG, "is communication running?" + aDiscoveryService.isRunning());
        if (aDiscoveryService.isRunning()) {
            aDiscoveryService.stopService();
        }
        aDiscoveryService.setContext(ApplicationContextHelper.getContext());
        aDiscoveryService.setCallBackHandler(new MyDiscoveryServiceCallbackHandler());


        // DISCOVERY_MODE_FOUND_AND_FINISH_DISCOVERY discovery multiple device
        aDiscoveryService.setDiscoveryMode(IDiscoveryService.DISCOVERY_MODE_FOUND_AND_STOP_DISCOVERY);

        // DISCOVERY_MODE_FOUND_AND_CANCEL_DISCOVERY discovery only one device , refuse another device
        //aDiscoveryService.setDiscoveryMode(BlueToothCommunication.DISCOVERY_MODE_FOUND_AND_CANCEL_DISCOVERY  );

        Vector<String> aBlueToothDeviceNameFilter = new Vector<String>();

        String sDeviceNameFilter = PreferenceManager.getDefaultSharedPreferences(activity).getString("DeviceNameFilter", "DailyChek,BP,BG,BLE,CP,KNV,BT,TAP,HIB,350");

        StringTokenizer aST = new StringTokenizer(sDeviceNameFilter, ",");
        while (aST.hasMoreTokens()) {
            aBlueToothDeviceNameFilter.add(aST.nextToken());
        }
        aDiscoveryService.setBlueToothDeviceNameFilter(aBlueToothDeviceNameFilter);
        aDiscoveryService.alwaysCallBackIfTheSameDeviceDiscovery(false);
    }
}
