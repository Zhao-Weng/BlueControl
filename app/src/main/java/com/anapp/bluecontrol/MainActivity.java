package com.anapp.bluecontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.anapp.bluecontrol.R.id.add;

public class MainActivity extends Activity {
  private static final String TAG = "LEDOnOff";
  
  Button  addButton, deleteButton;
  TextView text;
    TextView dataString;
    Boolean addEnable = false;



    private ListView checkList;
  private BluetoothAdapter btAdapter = null;
  private BluetoothSocket btSocket = null;
  private OutputStream outStream = null;
    private ConnectedThread connectedThread = null;
    public char[] arr = new char[16];

    private Map<String, String> map = null;
    private Set<String> hs = null;
    // Intent request codes
  private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
  private static final int REQUEST_ENABLE_BT = 3;
  
  // Well known SPP UUID
  private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private boolean tempHigh = false;
  // Insert your bluetooth devices MAC address
  private static String address = "00:00:00:00:00:00";

    Handler handler = new Handler() {
        @Override
        public void handleMessage( Message msg) {
            Bundle bundle = msg.getData();
            String string = bundle.getString("myKey");
            if (string.equals("show")) {
                showCheckList(hs);
            }
            else if (string.equals("temphigh")) {
                openDialog();
            }

        }
    };




  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);
      checkList = (ListView) findViewById(R.id.list);
      Intent intent = getIntent();
      map = (HashMap<String, String>)intent.getSerializableExtra("map");
      for (String key:map.keySet()) {
          System.out.printf("map keys are %s\n", key);
      }
      hs = new HashSet<>(map.values());
      if (map != null ) {
          showCheckList(hs);
      }






    text = (TextView) findViewById(R.id.textEdit);


      addButton = (Button) findViewById(add);
      addButton.setEnabled(false);
      deleteButton = (Button) findViewById(R.id.delete);
      deleteButton.setEnabled(false);
      addButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              addEnable = true;
              showCheckList(hs);
          }
      });
      deleteButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              addEnable = false;
          }
      });

//      Runnable runnable = new Runnable() {
//          public void run() {
//
//                  Message msg = handler.obtainMessage();
//                  Bundle bundle = new Bundle();
//
//                  bundle.putString("myKey", "show");
//                  msg.setData(bundle);
//                  handler.sendMessage(msg);
//              }
//
//      };
//
//      Thread mythread = new Thread(runnable);
//      mythread.start();

    btAdapter = BluetoothAdapter.getDefaultAdapter();
    checkBTState();







  }
    public void openDialog() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setTitle(R.string.dialog_title);
        builder1.setMessage(R.string.dialog_text);
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Confirm",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });


        AlertDialog alert11 = builder1.create();
        //alert11.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.parseColor("#FFFFFF"));
        alert11.show();


//        final Dialog dialog = new Dialog(this); // Context, this, etc.
//        dialog.setContentView(R.layout.dialog_demo);
//        dialog.setTitle(R.string.dialog_title);
//        dialog.show();

    }

    private void showCheckList(Set<String> hs) {
        if (hs != null && hs.size() > 0) {
            List<String> entries = new ArrayList<>(hs);
            String[] arr = new String[entries.size()];
            int i = 0;
            for (String name : entries) {
                arr[i++] = name;
            }
            for (String k : hs) System.out.printf("check list contains %s\n", k);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, arr);
            checkList.setAdapter(adapter);
        }
        else {
            String[] notFound = new String[1];
            notFound[0] = new String("No books missing\n");

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, notFound);
            checkList.setAdapter(adapter);
        }
    }


//    public void update(View v){
//	  sendData("1");
//      Toast msg = Toast.makeText(getBaseContext(), "LED is ON", Toast.LENGTH_SHORT);
//      msg.show();
//  }
  




  public void connectToDevice(String adr) {
    super.onResume();
    

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
        addButton.setEnabled(true);
        deleteButton.setEnabled(true);
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
                        for (int i = 0; i < 16; i ++) {
                            arr[i] = '0';
                        }
                        for (int i = 0; i < 16; i++) {

                            arr[i] = (char) mmBuffer[i];
                        }
                        String tag = String.valueOf(arr);
                        if (wordDistance(tag.substring(0, 8), "temphigh") <= 2) {
                            System.out.printf("temp high !!!!!!!!!!!!!!!!!");
                            Message msg = handler.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putString("myKey", "temphigh");
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                        }


                        System.out.printf("substring is %s\n", tag.substring(0, 8));
                        System.out.printf("wd temp high is %d, tag is %s\n", wordDistance(tag.substring(0, 8), "temphigh"), tag);

                        for (String item: map.keySet()) {
                            System.out.printf("testing" + item + "\n");
                        }


                        for (String k: map.keySet()) System.out.printf("hm keys are %s\n", k);
                        String item = contain(map, tag);
                        if (item != null) System.out.printf("item is %s\n", item);
                        else System.out.printf("item is null\n");
                        if (item != null) {
                            String bookN = map.get(item);
                           if (bookN != null) System.out.printf("book is %s\n", bookN);
                            else System.out.println("bookN is null");
                            if (hs.contains(bookN) && addEnable) {
                                hs.remove(bookN);            //previously missing, now in the bag
                            }
                            else if (!addEnable){
                                hs.add(bookN);               //previously in the bag, now missing
                            }
                            Message msg = handler.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putString("myKey", "show");
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                        }
                    }

                } catch (IOException e) {
                    Log.d(testTag, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public String contain(Map<String, String> hm, String tag) {
            for (String item: hm.keySet()) {
                if (wordDistance(item, tag.substring(0, 8)) <= 1) {
                    return item;
                }
            }
            return null;
        }

        public  int wordDistance( String word1, String word2) {
            if (word1 == null || word2 == null) return Integer.MAX_VALUE / 2;
            int l1 = word1.length(), l2 = word2.length();
            int[][] arr = new int[2][l1 + 1];
            for (int i = 0; i <= l1; i++) arr[0][i] = i;
            int which = 1;
            for (int i = 1; i <= l2; i ++) {
                arr[which % 2][0] = i;
                for (int j = 1; j <= l1; j++) {
                    if (word1.charAt(j - 1) == word2.charAt(i - 1)) {
                        arr[which % 2][j] = arr[(which + 1) % 2][j - 1];
                        //System.out.printf("hello %b\n", '7' == '7');
                    }
                    else {
                        int m = Math.min(arr[(which + 1) % 2][j], arr[which % 2][j - 1]);
                        arr[which % 2][j] = Math.min(m, arr[(which + 1) %2][j - 1]) + 1;
                    }
                }
                which = 1 - which;
            }
            return arr[(which + 1) %2][l1];
        }
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

