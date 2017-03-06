package com.anapp.bluecontrol;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.content.ContentValues.TAG;

/**
 * Created by zweng on 3/2/2017.
 */
//public class MyBluetoothService {
//    private static final String TAG = "MY_APP_DEBUG_TAG";
//    //private Handler mHandler; // handler that gets info from Bluetooth service
//
//    // Defines several constants used when transmitting messages between the
//    // service and the UI.
//    private interface MessageConstants {
//        public static final int MESSAGE_READ = 0;
//        public static final int MESSAGE_WRITE = 1;
//        public static final int MESSAGE_TOAST = 2;
//
//        // ... (Add other message types here as needed.)
//    }

    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private int num = -1;
        private byte[] mmBuffer; // mmBuffer store for the stream


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
            char[] arr = new char[5];
            // Keep listening to the InputStream until an exception occurs.
            //Log.d(TAG, "jfkajdkfajfksjajdfkajkfjsjfajdklfjakfjdsjkfajdlfjajdkjalfjdlsjafkjds");
            while (true) {
                try {
                    //if (numBytes > 0) this.num = numBytes;
                    if (mmInStream.available() > 0) {
                        //mmBuffer = new byte[32];
                        this.num = -2;
                        // Read from the InputStream.
                        numBytes = mmInStream.read(mmBuffer);
                        // Send the obtained bytes to the UI activity.
//                    Message readMsg = mHandler.obtainMessage(
//                            MessageConstants.MESSAGE_READ, numBytes, -1,
//                            mmBuffer);
//                    readMsg.sendToTarget();
                        //System.out.printf("Data Received: %d\n", numBytes);
                        //byte[] arr = Arrays.copyOfRange(mmBuffer, 0, 6);

                        for (int i = 0; i < 5; i++) {

                            arr[i ] = (char) mmBuffer[i];
                        }
                        System.out.printf("%s", String.valueOf(arr));
                    }

                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }
//        public byte[] getMessage(){
//            return mmBuffer;
//        }
        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);

                // Share the sent message with the UI activity.
//                Message writtenMsg = mHandler.obtainMessage(
//                        MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
//                writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
//                Message writeErrorMsg =
//                        mHandler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
//                writeErrorMsg.setData(bundle);
//                mHandler.sendMessage(writeErrorMsg);
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
//}