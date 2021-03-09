package com.elec.c3.generatorbluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


public class MainActivity extends  Activity/*AppCompatActivity*/ {

    private double output_power;
    private static final Random RANDOM = new Random();
    private LineGraphSeries<DataPoint> series1;
    private int lastX1 = 0;
    private int ENDX1 = 10;
    private LineGraphSeries<DataPoint> series2;
    private int lastX2 = 0;
    private int scroll_length = 100;
    private LineGraphSeries<DataPoint> series3;
    private int lastX3 = 0;

    private LineGraphSeries<DataPoint> series4;
    private int lastX4 = 0;

    private LineGraphSeries<DataPoint> series5;
    private int lastX5 = 0;

    // GUI Components
    private TextView mBluetoothStatus, weather_country_tv, wind_speed_tv, wind_direction_tv;
    private TextView vin, vout, cout, pout, generator_title, wind_direction, duty_cycle;
    private Button mScanBtn;
    private Button mOffBtn;
    private Button mListPairedDevicesBtn;
    private Button mDiscoverBtn;
    private Button fetch_wind_direction, stop_generator, vancouver_weather, surrey_weather, richmond_weather, manual_duty, manual_angle;
    private LinearLayout bluetooth_layout, powerOutpus, fetch_weather_layout, manual_control_layout, power_info_and_graph;
    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;
    private ListView mDevicesListView;
    private BottomNavigationView bottom_navigation;
    private SeekBar adjust_duty_cycle, adjust_turbine_angle;
    private TextView current_duty, current_angle;
    private int set_duty_cycle, set_angle;
    GraphView graph;
    Viewport viewport;


    public void angle_seek() {
        adjust_turbine_angle = (SeekBar) findViewById(R.id.adjust_turbine_angle);
        current_angle = (TextView) findViewById(R.id.current_angle);


        adjust_turbine_angle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                current_angle.setText(Integer.toString(progress));
                set_angle = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    public void duty_cycle_seek() {
        adjust_duty_cycle = (SeekBar) findViewById(R.id.adjust_duty_cycle);
        current_duty = (TextView) findViewById(R.id.current_duty);

        adjust_duty_cycle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                current_duty.setText(Integer.toString(progress));
                set_duty_cycle = progress;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    private Handler mHandler; // Our main handler that will receive callback notifications
    private ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status


    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.nav_power:
                    mConnectedThread.write("eeee");
                    power_info_and_graph.setVisibility(View.VISIBLE);
                    bluetooth_layout.setVisibility(View.GONE);
                    fetch_weather_layout.setVisibility(View.GONE);
                    manual_control_layout.setVisibility(View.GONE);
                    break;
                case R.id.nav_weather_control:
                    mConnectedThread.write("cccc");
                    power_info_and_graph.setVisibility(View.GONE);
                    bluetooth_layout.setVisibility(View.GONE);
                    fetch_weather_layout.setVisibility(View.VISIBLE);
                    manual_control_layout.setVisibility(View.GONE);
                    break;
                case R.id.nav_stop:
                    mConnectedThread.write("aaaa");
                    power_info_and_graph.setVisibility(View.GONE);
                    bluetooth_layout.setVisibility(View.GONE);
                    fetch_weather_layout.setVisibility(View.GONE);
                    manual_control_layout.setVisibility(View.GONE);
                    break;
                case R.id.nav_manual_control:
                    mConnectedThread.write("cccc");
                    power_info_and_graph.setVisibility(View.GONE);
                    bluetooth_layout.setVisibility(View.GONE);
                    fetch_weather_layout.setVisibility(View.GONE);
                    manual_control_layout.setVisibility(View.VISIBLE);
                    break;


            }

            return true;
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // we get graph view instance
        graph = (GraphView) findViewById(R.id.power_graph);
        series1 = new LineGraphSeries<DataPoint>();
        graph.addSeries(series1);
        // customize a little bit viewport
        viewport = graph.getViewport();
        viewport.setXAxisBoundsManual(true);
        viewport.setMinX(0);
        viewport.setMaxX(10);
        viewport.setScrollable(true);

       
        power_info_and_graph = (LinearLayout)findViewById(R.id.power_info_and_graph_layout);
        mBluetoothStatus = (TextView) findViewById(R.id.bluetoothStatus);
        vin = (TextView) findViewById(R.id.vin);
        generator_title = (TextView) findViewById(R.id.powertTitle);
        vout = (TextView) findViewById(R.id.vout);
        cout = (TextView) findViewById(R.id.cout);
        pout = (TextView) findViewById(R.id.pout);
        duty_cycle = (TextView) findViewById(R.id.duty_cycle);
        wind_direction_tv = (TextView) findViewById(R.id.wind_direction_tv);
        wind_speed_tv = (TextView) findViewById(R.id.wind_speed_tv);
        weather_country_tv = (TextView) findViewById(R.id.weather_country_tv);
        mScanBtn = (Button) findViewById(R.id.scan);
        mOffBtn = (Button) findViewById(R.id.off);
        mDiscoverBtn = (Button) findViewById(R.id.discover);
        mListPairedDevicesBtn = (Button) findViewById(R.id.PairedBtn);
        bluetooth_layout = (LinearLayout) findViewById(R.id.bluetooth_layout);
        powerOutpus = (LinearLayout) findViewById(R.id.poweroutputs);
        fetch_weather_layout = (LinearLayout) findViewById(R.id.fetch_weather);
        fetch_wind_direction = (Button) findViewById(R.id.weather_control);
        stop_generator = (Button) findViewById(R.id.stop_generator);
        surrey_weather = (Button) findViewById(R.id.surrey_weather);
        vancouver_weather = (Button) findViewById(R.id.vancouver_weather);
        richmond_weather = (Button) findViewById(R.id.richmond_weather);
        bottom_navigation = (BottomNavigationView) findViewById(R.id.bottom_nav);

        mBTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio

        mDevicesListView = (ListView) findViewById(R.id.devicesListView);
        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);
        manual_control_layout = (LinearLayout) findViewById(R.id.manual_control_layout);
        manual_duty = (Button) findViewById(R.id.send_duty);
        manual_angle = (Button) findViewById(R.id.send_angle);


        power_info_and_graph.setVisibility(View.GONE);
        bluetooth_layout.setVisibility(View.VISIBLE);
        fetch_weather_layout.setVisibility(View.GONE);
        bottom_navigation.setVisibility(View.GONE);
        manual_control_layout.setVisibility(View.GONE);


        manual_angle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NumberFormat formatter2 = new DecimalFormat("#0000");
                mConnectedThread.write(formatter2.format(set_angle));
            }
        });

        manual_duty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NumberFormat formatter2 = new DecimalFormat("#0000");
                mConnectedThread.write(formatter2.format(set_duty_cycle));
            }
        });

        bottom_navigation.setOnNavigationItemSelectedListener(navListener);
        duty_cycle_seek();
        angle_seek();

      
        /***************************Encoding ***************************
         Input Voltage, Output Voltage$ Output Current* Output Power
         Example:
         4.8,12.6$0.130*1.5#

         */

        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {

                int indexOfVin = 0;
                int indexOfVout = 0;
                int indexOfCout = 0;
                int indexOfPout = 0;
                int indexOfDuty = 0;

                if (msg.what == MESSAGE_READ) {
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    Log.e("LENGTH", Integer.toString(readMessage.toString().length()));
                    Log.e("MSG", readMessage);

                    String conMsg = readMessage.substring(0, 30);


                    if (conMsg.contains(",") && conMsg.contains("$") && conMsg.contains("*") && conMsg.contains("^") && conMsg.contains("#")) {

                        indexOfVin = conMsg.indexOf(',');
                        indexOfVout = conMsg.indexOf('$');
                        indexOfCout = conMsg.indexOf('*');
                        indexOfDuty = conMsg.indexOf('^');
                        indexOfPout = conMsg.indexOf('#');

                        Log.e("indexofvin: ", Integer.toString(indexOfVin));
                        Log.e("indexofvout: ", Integer.toString(indexOfVout));
                        Log.e("indexofcout: ", Integer.toString(indexOfCout));
                        Log.e("indexofpout: ", Integer.toString(indexOfPout));
                        Log.e("indexofduty: ", Integer.toString(indexOfDuty));

                        if (indexOfVin == 5 && indexOfVout == 11 && indexOfCout == 17 && indexOfDuty == 23 && indexOfPout == 29) {


                            if (indexOfVin > 0 && indexOfVout > 0 && indexOfCout > 0 && indexOfPout > 0) {

                                vin.setText("Input Voltage: " + conMsg.substring(0, indexOfVin) + "V");
                                vout.setText("Output Voltage: " + conMsg.substring(indexOfVin + 1, indexOfVout) + "V");
                                cout.setText("Output Current: " + conMsg.substring(indexOfVout + 1, indexOfCout) + "A");
                                duty_cycle.setText("Duty Cycle: " + conMsg.substring(indexOfCout + 1, indexOfDuty));
                                pout.setText("Output Power: " + conMsg.substring(indexOfDuty + 1, indexOfPout) + "W");
                                output_power = Double.parseDouble(conMsg.substring(indexOfDuty + 1, indexOfPout));
                                Log.e("MESSAGE: ", conMsg);

                            }
                        }
                    }


                }

                if (msg.what == CONNECTING_STATUS) {
                    if (msg.arg1 == 1) {
                        mBluetoothStatus.setText("Connected to Device: " + (String) (msg.obj));
                        power_info_and_graph.setVisibility(View.VISIBLE);
                        bottom_navigation.setVisibility(View.VISIBLE);
                        bluetooth_layout.setVisibility(View.GONE);
                        fetch_weather_layout.setVisibility(View.GONE);
                        SystemClock.sleep(1000);

                    } else {
                        mBluetoothStatus.setText("Connection Failed");
                        power_info_and_graph.setVisibility(View.GONE);
                        bluetooth_layout.setVisibility(View.VISIBLE);
                        fetch_weather_layout.setVisibility(View.GONE);


                    }
                }
            }
        };

        if (mBTArrayAdapter == null) {
            // Device does not support Bluetooth
            mBluetoothStatus.setText("Status: Bluetooth not found");
            Toast.makeText(getApplicationContext(), "Bluetooth device not found!", Toast.LENGTH_SHORT).show();
        } else {

            mScanBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothOn(v);
                }
            });

            mOffBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothOff(v);
                }
            });

            mListPairedDevicesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDevicesListView.setVisibility(View.VISIBLE);
                    listPairedDevices(v);
                }
            });

            mDiscoverBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    discover(v);
                }
            });

            fetch_wind_direction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    fetch_weather_layout.setVisibility(View.VISIBLE);
                    power_info_and_graph.setVisibility(View.GONE);
                    bluetooth_layout.setVisibility(View.GONE);

                }
            });

            vancouver_weather.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    find_weather("Vancouver");

                }
            });

            richmond_weather.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    find_weather("Richmond");

                }
            });

            surrey_weather.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    find_weather("Surrey");

                }
            });
        }
    }

    private void bluetoothOn(View view) {
        if (!mBTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            mBluetoothStatus.setText("Bluetooth enabled");
            Toast.makeText(getApplicationContext(), "Bluetooth turned on", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth is already on", Toast.LENGTH_SHORT).show();
        }
    }

    // Enter here after user selects "yes" or "no" to enabling radio
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                mBluetoothStatus.setText("Enabled");
            } else
                mBluetoothStatus.setText("Disabled");
        }
    }

    private void bluetoothOff(View view) {
        mBTAdapter.disable(); // turn off
        mBluetoothStatus.setText("Bluetooth disabled");
        Toast.makeText(getApplicationContext(), "Bluetooth turned Off", Toast.LENGTH_SHORT).show();
    }

    private void discover(View view) {
        // Check if the device is already discovering
        if (mBTAdapter.isDiscovering()) {
            mBTAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(), "Discovery stopped", Toast.LENGTH_SHORT).show();
        } else {
            if (mBTAdapter.isEnabled()) {
                mBTArrayAdapter.clear(); // clear items
                mBTAdapter.startDiscovery();
                Toast.makeText(getApplicationContext(), "Discovery started", Toast.LENGTH_SHORT).show();
                registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            } else {
                Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }
    }

    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name to the list
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mBTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    private void listPairedDevices(View view) {
        mPairedDevices = mBTAdapter.getBondedDevices();
        if (mBTAdapter.isEnabled()) {
            // put it's one to the adapter
            for (BluetoothDevice device : mPairedDevices)
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());

            Toast.makeText(getApplicationContext(), "Show Paired Devices", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            if (!mBTAdapter.isEnabled()) {
                Toast.makeText(getBaseContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
                return;
            }

            mBluetoothStatus.setText("Connecting...");
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            final String address = info.substring(info.length() - 17);
            final String name = info.substring(0, info.length() - 17);

            // Spawn a new thread to avoid blocking the GUI one
            new Thread() {
                public void run() {
                    boolean fail = false;

                    BluetoothDevice device = mBTAdapter.getRemoteDevice(address);

                    try {
                        mBTSocket = createBluetoothSocket(device);
                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        mBTSocket.connect();
                    } catch (IOException e) {
                        try {
                            fail = true;
                            mBTSocket.close();
                            mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                    .sendToTarget();
                        } catch (IOException e2) {
                            //insert code to deal with this
                            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (fail == false) {
                        mConnectedThread = new ConnectedThread(mBTSocket);
                        mConnectedThread.start();

                        mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                                .sendToTarget();
                    }
                }
            }.start();
        }
    };

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.available();
                    if (bytes != 0) {
                        SystemClock.sleep(100); //pause and wait for rest of data. Adjust this depending on your sending speed.
                        bytes = mmInStream.available(); // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read
                        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget(); // Send the obtained bytes to the UI activity
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    public void find_weather(String location) {

        String url = "http://api.openweathermap.org/data/2.5/weather?q=" + location + ",CA&appid=7179df1964aeddf6297bbec47723178a";

        JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject main = response.getJSONObject("wind");
                    JSONArray array = response.getJSONArray("weather");
                    JSONObject object = array.getJSONObject(0);
                    String wind_val = String.valueOf(main.getDouble("deg"));
                    String wind_speed = String.valueOf(main.getDouble("speed"));
                    double wind_angle = main.getDouble("deg");
                    NumberFormat formatter = new DecimalFormat("#0000");
                    String name = response.getString("name");

                    //wind_direction.setText("Current Wind Direction: " + wind_val + " degrees with respect to North");
                    wind_direction_tv.setText("Wind Direction: " + wind_val);
                    wind_speed_tv.setText("Wind Speed: " + wind_speed);
                    weather_country_tv.setText(name);

                    if (mConnectedThread != null) //First check to make sure thread created
                        mConnectedThread.write(formatter.format(wind_angle));
                } catch (JSONException e) {

                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jor);
    }

    @Override
    public void onResume() {
        super.onResume();

        new Thread(new Runnable() {

            @Override
            public void run() {
                // we add 100 new entries
               while(true) /*for (int i = 0; i < scroll_length; i++) */ {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            addEntry();
                        }
                    });

                    // sleep to slow down the add of entries
                    try {
                        Thread.sleep(600);
                    } catch (InterruptedException e) {
                        // manage error ...
                    }
                }

            }
        }).start();

    }

    
    private void addEntry() {
        // here, we choose to display max 10 points on the viewport and we scroll to end
        series1.appendData(new DataPoint(lastX1++, output_power), true, 10);
    }

}



