package my.com.codeplay.training.marsmobilityapp;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.http.GET;

import static java.lang.Integer.parseInt;

public class DataActivity extends AppCompatActivity {

    private TextView tvTemperature, tvHumidity, tvHeatIndex, tvHeartRate;
    private String mDeviceName;
    private String mDeviceAddress;
    private RBLService mBluetoothLeService;
    private Map<UUID, BluetoothGattCharacteristic> map = new HashMap<UUID, BluetoothGattCharacteristic>();
    String stringArray = "";
    String tempString2 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTemperature = (TextView) findViewById(R.id.temperature);
        tvHumidity = (TextView) findViewById(R.id.humidity);
        tvHeatIndex = (TextView) findViewById(R.id.heatindex);
        tvHeartRate = (TextView) findViewById(R.id.heartrate);

        // Set Background Colour To Light Blue
        View root = this.getWindow().getDecorView();
        root.setBackgroundColor(0xFF448AFF);              // Format: α (FF) + Blue 50 A200 colour (448AFF) = FF448AFF

        // Connect with Bluetooth BLE
        Intent intent = getIntent();

        mDeviceAddress = intent.getStringExtra(Device.EXTRA_DEVICE_ADDRESS);
        mDeviceName = intent.getStringExtra(Device.EXTRA_DEVICE_NAME);

        Intent gattServiceIntent = new Intent(this, RBLService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            mBluetoothLeService = ((RBLService.LocalBinder) service)
                    .getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e("Data", "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up
            // initialization.
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

            if (RBLService.ACTION_GATT_DISCONNECTED.equals(action)) {
            } else if (RBLService.ACTION_GATT_SERVICES_DISCOVERED
                    .equals(action)) {
                getGattService(mBluetoothLeService.getSupportedGattService());
            } else if (RBLService.ACTION_DATA_AVAILABLE.equals(action)) {
//                displayData(intent.getByteArrayExtra(RBLService.EXTRA_DATA));

                byte[] byteArray;
                byteArray = intent.getByteArrayExtra(RBLService.EXTRA_DATA);

                String tempString = new String (byteArray);

                if (tempString != null) {
                    //Log.d("Data", "BroadcastReceiver()" + tempString);
                    //Log.d("Data", "BroadcastReceiver() Start");

                    if(stringArray!=null){
                        if(tempString.contains(System.getProperty("line.separator"))){
                            String[] tmepStringArray = tempString.split(System.getProperty("line.separator"));
                            if(tmepStringArray.length > 1){
                                if(tmepStringArray[1] == ""){
                                    stringArray += tmepStringArray[0];
                                } else {
                                    stringArray += tmepStringArray[0];
                                    tempString2 = tmepStringArray[1];
                                }
                            } else if (tmepStringArray.length == 1) {
                                stringArray += tmepStringArray[0];
                            }
                            int counter = 0;
                            for( int i=0; i<stringArray.length(); i++ ) {
                                if( stringArray.charAt(i) == '|' ) {
                                    counter++;
                                }
                            }
                            if(stringArray.indexOf('B') == 0 && counter == 8)
                            {
                                Log.d("Data", "Full String Receive "+stringArray);
                                String[] data = stringArray.split(Pattern.quote("|"));
                                for( int j=0; j<data.length; j++ ) {
                                    Log.d("Data", data[j]);
                                }
                                // Heart Rate (B)
                                Log.d("Data", data[0]+" Data "+data[1]);

                                // Humidity (H)
                                Log.d("Data", data[2]+" Data "+data[3]);

                                // Temperature (T)
                                Log.d("Data", data[4]+" Data "+data[5]);

                                // Heat Index (I)
                                Log.d("Data", data[6]+" Data "+data[7]);
                                //stringArray.substring(2);
                                //Log.d("Data", "BroadcastReceiver() T1 Final" + stringArray);
                                tvHeartRate.setText(data[1]);
                                tvHumidity.setText(data[3]);
                                tvTemperature.setText(data[5]);
                                tvHeatIndex.setText(data[7]);
                            }
                            stringArray = "";
                            if(tempString2 != "") {
                                stringArray += tempString2;
                                tempString2 = "";
                            }
                        } else {
                            stringArray += tempString;
                        }
                    } else {
                        stringArray += tempString;
                    }
                    /*else if(stringArray.indexOf(0)==parseInt("T"))
                    {
                        Log.d("Data", "BroadcastReceiver() T2");
                        stringArray.substring(2);
                        Log.d("Data", "BroadcastReceiver() T1 Final" + stringArray);
                        tvTemperature.setText(stringArray);
                    }
                    else if(stringArray.charAt(0) == parseInt("T"))
                    {
                        Log.d("Data", "BroadcastReceiver() T3");
                        stringArray.substring(2);
                        Log.d("Data", "BroadcastReceiver() T1 Final" + stringArray);
                        tvTemperature.setText(stringArray);
                    }*/
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mBluetoothLeService.disconnect();
        mBluetoothLeService.close();

        System.exit(0);
    }

    private void parseResult (String result) {

        if(result == null) {
            Log.d("Data", "result is null!");
        }

        else if(result != null) {
            Log.d("Data", "result not null!");

            try {
                final JSONObject weatherJSON = new JSONObject(result);

//                tvHeartRate.setText(weatherJSON.getString("name") + "," + weatherJSON.getJSONObject("sys").getString("country"));
//                Log.d("Data", "tvLocation: " + weatherJSON.getString("name") + "," + weatherJSON.getJSONObject("sys").getString("country"));

                tvHeatIndex.setText(String.valueOf(weatherJSON.getJSONObject("wind").getDouble("speed")) + " mps");
                Log.d("Data", "tvWindSpeed: " + String.valueOf(weatherJSON.getJSONObject("wind").getDouble("speed")) + " mps");

                tvHeartRate.setText(String.valueOf(weatherJSON.getJSONObject("clouds").getInt("all")) + "%");
                Log.d("Data", "tvCloudiness: " + String.valueOf(weatherJSON.getJSONObject("clouds").getInt("all")) + "%");

                final JSONObject mainJSON = weatherJSON.getJSONObject("main");

                tvTemperature.setText(String.valueOf(Math.round(mainJSON.getDouble("temp")-273)));
                Log.d("Data", "tvTemperature: " + String.valueOf(mainJSON.getDouble("temp")));

                tvHumidity.setText(String.valueOf(mainJSON.getInt("humidity")) + "%");
                Log.d("Data", "tvHumidity: " + String.valueOf(mainJSON.getInt("humidity")) + "%");

                final JSONArray weatherJSONArray = weatherJSON.getJSONArray("weather");
                if(weatherJSONArray.length()>0) {
                    int code = weatherJSONArray.getJSONObject(0).getInt("id");
                    Log.d("Data", "code: " + String.valueOf(code));

//                    getIcon(code);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void getGattService(BluetoothGattService gattService) {
        if (gattService == null)
            return;

        BluetoothGattCharacteristic characteristic = gattService
                .getCharacteristic(RBLService.UUID_BLE_SHIELD_TX);
        map.put(characteristic.getUuid(), characteristic);

        BluetoothGattCharacteristic characteristicRx = gattService
                .getCharacteristic(RBLService.UUID_BLE_SHIELD_RX);
        mBluetoothLeService.setCharacteristicNotification(characteristicRx,
                true);
        mBluetoothLeService.readCharacteristic(characteristicRx);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(RBLService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(RBLService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(RBLService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(RBLService.ACTION_DATA_AVAILABLE);

        return intentFilter;
    }
}
