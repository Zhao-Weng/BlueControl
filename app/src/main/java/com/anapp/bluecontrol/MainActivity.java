package com.anapp.bluecontrol;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity {
  private static final String TAG = "LEDOnOff";
  
  Button updateButton, btnOff;
  TextView text;
    TextView dataString;
  
  private BluetoothAdapter btAdapter = null;
  private BluetoothSocket btSocket = null;
  private OutputStream outStream = null;
    private ConnectedThread connectedThread = null;
    public char[] arr = new char[16];
    String rfidData = "";

    // Intent request codes
  private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
  private static final int REQUEST_ENABLE_BT = 3;
  
  // Well known SPP UUID
  private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

  // Insert your bluetooth devices MAC address
  private static String address = "00:00:00:00:00:00";
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    updateButton = (Button) findViewById(R.id.update);
    btnOff = (Button) findViewById(R.id.btnOff);
    text = (TextView) findViewById(R.id.textEdit);
    dataString = (TextView) findViewById(R.id.textView1);
    updateButton.setEnabled(false);
    btnOff.setEnabled(false);
    updateButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            for(int i = 0; i<arr.length;i++){
                System.out.print(arr[i]);
                rfidData +=arr[i];
            }
            System.out.print("Test Data String: ");
            dataString.setText(rfidData);
        }
    });


    btAdapter = BluetoothAdapter.getDefaultAdapter();
    checkBTState();
    
    //Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
    

  }



    public void update(View v){
	  sendData("1");
      Toast msg = Toast.makeText(getBaseContext(), "LED is ON", Toast.LENGTH_SHORT);
      msg.show();
  }
  
  public void ledOff(View v){
	  sendData("0");
      Toast msg = Toast.makeText(getBaseContext(), "LED is OFF", Toast.LENGTH_SHORT);
      msg.show();
  }
  

  public void connectToDevice(String adr) {
    super.onResume();
    
    //enable buttons once connection established.
    updateButton.setEnabled(true);
    btnOff.setEnabled(true);
    String dataString = "";
    
    
    // Set up a pointer to the remote node using it's address.
    BluetoothDevice device = btAdapter.getRemoteDevice(adr);
    
    // Two things are needed to make a connection:
    //   A MAC address, which we got above.
    //   A Service ID or UUID.  In this case we are using the
    //     UUID for SPP.
    try {
      btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
    } catch (IOException e) {
      errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
    }
  
    // Discovery is resource intensive.  Make sure it isn't going on
    // when you attempt to connect and pass your message.
    btAdapter.cancelDiscovery();

    // Establish the connection.  This will block until it connects.
    try {
      btSocket.connect();
//        myBlueToothService = new MyBlueToothService();
//        myBlueToothService.start();
        connectedThread = new ConnectedThread(btSocket);
        connectedThread.start();


    } catch (IOException e) {
      try {
        btSocket.close();
      } catch (IOException e2) {
        errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
      }
    }
    
    // Create a data stream so we can talk to server.
    try {
      outStream = btSocket.getOutputStream();
    } catch (IOException e) {
      errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
    }
  }

  
  private void checkBTState() {
    // Check for Bluetooth support and then check to make sure it is turned on

    // Emulator doesn't support Bluetooth and will return null
    if(btAdapter==null) { 
      errorExit("Fatal Error", "Bluetooth Not supported. Aborting.");
    } else {
      if (btAdapter.isEnabled()) {
        Log.d(TAG, "...Bluetooth is enabled...");
      } else {
        //Prompt user to turn on Bluetooth
        Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
      }
    }
  }

  private void errorExit(String title, String message){
    Toast msg = Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_SHORT);
    msg.show();
    finish();
  }

  private void sendData(String message) {
    byte[] msgBuffer = message.getBytes();
    try {
      outStream.write(msgBuffer);
    } catch (IOException e) {
      String msg = "In onResume() and an exception occurred during write: " + e.getMessage();      
      errorExit("Fatal Error", msg);       
    }
  }
  
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
      switch (requestCode) {
      case REQUEST_CONNECT_DEVICE_SECURE:
          // When DeviceListActivity returns with a device to connect
          if (resultCode == Activity.RESULT_OK) {
              connectDevice(data, true);
          }
          break;
      }
  }
  
  private void connectDevice(Intent data, boolean secure) {
      // Get the device MAC address
      address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
      text.setText("Device Address: " + address);
      connectToDevice(address);
      // Get the BluetoothDevice object
      //BluetoothDevice device = btAdapter.getRemoteDevice(address);
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.option_menu, menu);
      return true;
  }
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
      Intent serverIntent = null;
      switch (item.getItemId()) {
      case R.id.secure_connect_scan:
          // Launch the DeviceListActivity to see devices and do scan
          serverIntent = new Intent(this, DeviceListActivity.class);
          startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
          return true;
      }
      return false;
  }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private int num = -1;
        private byte[] mmBuffer; // mmBuffer store for the stream
        public final String testTag = "test";
        private String dataString = "";

        public int getNum() {
            return num;
        }

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[32];
            int numBytes; // bytes returned from read()
            this.num = -3;
            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    if (mmInStream.available() > 0) {
                        this.num = -2;
                        numBytes = mmInStream.read(mmBuffer);

                        for (int i = 0; i < 16; i++) {

                            arr[i] = (char) mmBuffer[i];
                        }
//                        System.out.print("The length is:" + arr.length);
//                        handleThread newThread = new handleThread(arr);
//                        newThread.start();
                    }

                } catch (IOException e) {
                    Log.d(testTag, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);

            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);


                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");

            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

}
