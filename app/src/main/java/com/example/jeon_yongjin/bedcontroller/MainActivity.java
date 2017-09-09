package com.example.jeon_yongjin.bedcontroller;

import android.app.FragmentTransaction;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    ImageView dotting;

    //Bluetooth variables && data from prototype
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    boolean stopWorker;
    int readBufferPosition;
    int pv1;
    int pv2;
    int pv3;
    int pv4;
    int pv5;
    int pv6;
    int airInterval;

    Long targetTime;
    int state = 0;

    TextView timeCountUp = null;
    TextView fixedTime = null;
    TextView fsrView = null;

    TimeFragment timeFragment;
    HistoryFragment historyFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timeFragment = new TimeFragment();
        historyFragment = new HistoryFragment();

        FragmentManager fragmentManager = getFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.time_status, timeFragment);
        fragmentTransaction.replace(R.id.history_status, historyFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        //fragmentTransaction.replace(R.id.history_status, historyFragment);

        timeCountUp = timeFragment.timercountup;
        fixedTime = timeFragment.fixedtime;
        fsrView = historyFragment.fsrView;

        setInterval(30);

        // 블루투스 연결.
        try{
            findBT();
            openBT();
            sendData("0");
            sendData("f");
//            sendData("");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void findBT()
    {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
        }

        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("JCNET-JARDUINO-5927")) //Device Name (블루투스 디바이스)
                {
                    mmDevice = device;
                    break;
                }
            }
        }
    }

    void openBT() throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

        beginListenForData();
    }

    void beginListenForData()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024]; // response data의 string buffer length define.

        TimerTask mTask2 = new TimerTask() {

            @Override
            public void run() {
                try
                {
                    int bytesAvailable = mmInputStream.available(); // input stream data를 받아온다.
                    if(bytesAvailable > 0) // 데이터가 있을 경우...
                    {
                        byte[] packetBytes = new byte[bytesAvailable]; // 위에서 받아온 input stream data를 byte 형식으로 변환.
                        mmInputStream.read(packetBytes);
                        for(int i=0;i<bytesAvailable;i++)
                        {
                            byte b = packetBytes[i];
                            if(b == delimiter)
                            {
                                byte[] encodedBytes = new byte[readBufferPosition];
                                System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                final String data = new String(encodedBytes, "US-ASCII"); // stream data > String 변환.

                                // A~F까지의 값으로 split 진행.
                                // A : sensor 1번 (0~1023)
                                final String[] datas = data.split("A|B|C|D|E|F|\r");

                                readBufferPosition = 0;

                                handler.post(new Runnable()
                                {
                                    public void run()
                                    {
                                        try
                                        {
                                            // 0 index에는 값이 들어가지 않음.
                                            pv1 = Integer.parseInt(datas[1]); // sensor 1
                                            pv2 = Integer.parseInt(datas[2]); // sensor 2
                                            pv3 = Integer.parseInt(datas[3]); // sensor 3
                                            pv4 = Integer.parseInt(datas[4]); // sensor 4
                                            pv5 = Integer.parseInt(datas[5]); // sensor 5
                                            pv6 = Integer.parseInt(datas[6]); // sensor 6
                                        }
                                        catch(Exception ee)
                                        {
                                        }
                                    }
                                });
                            }
                            else
                            {
                                readBuffer[readBufferPosition++] = b;
                            }
                        }
                    }

                    Long currentTime = System.currentTimeMillis();
                    sendData("f");

                    if (currentTime > targetTime) {
                        // Send command

                        logic();

                        // test
                        targetTime = System.currentTimeMillis() + (airInterval * 1000);

                    }

                    updateTimeView(targetTime - currentTime);

                }
                catch (IOException ex)
                {
                    stopWorker = true;
                }
            }

        };

        Timer mTimer2 = new Timer();
        mTimer2.schedule(mTask2, 0, 100);

    }

    Handler handler = new Handler();
    Long gDiff;
    void updateTimeView(Long diff)
    {
        gDiff = diff;
        handler.post(new Runnable() {
            public void run() {
                gDiff /= 1000;
                timeCountUp = timeFragment.timercountup;
                fixedTime = timeFragment.fixedtime;
                fsrView = historyFragment.fsrView;
                fsrView.setTextSize(30);
                if (timeCountUp != null) {
                    timeCountUp.setText(String.format("%02d:%02d", gDiff / 60, gDiff % 60));
                    fixedTime.setText(String.format("%02d:%02d", airInterval / 60, airInterval % 60));
                    fsrView.setText(String.format("Right: %03d Left: %03d", pv1 + pv2 + pv3, pv4 +pv5, pv6));
                }
            }
        });
    }

    void logic() {
        /*
        int left = pv1 + pv2 + pv3;
        int right = pv4 + pv5 + pv6;

        if (left > right) {

        } else {

        }
        */

        if (state > 2) state = 0;
        try {
            sendData(Integer.toString(state));
        }
        catch(IOException e){};
        state++;
    }

    void closeBT() throws IOException
    {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
    }


    // Request Data comment
    //압력 센서가 필요하면 "f"
    //에어펌프 끄기 "0"
    //에어펌프 오른쪽 "1"
    //에어펌프 왼쪽 "2"
    void sendData(String msg) throws IOException
    {
        if(mmOutputStream != null)
        {
            mmOutputStream.write(msg.getBytes());
            mmOutputStream.flush();
        }
    }

    public void setInterval(int intv)
    {
        airInterval = intv;
        targetTime = System.currentTimeMillis() + (airInterval * 1000);
        updateTimeView(targetTime - System.currentTimeMillis());
    }
}
