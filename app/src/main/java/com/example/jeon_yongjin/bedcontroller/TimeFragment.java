package com.example.jeon_yongjin.bedcontroller;

import android.app.Fragment;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static android.R.attr.value;

/**
 * Created by jeon-yongjin on 2017. 9. 8..
 */

public class TimeFragment extends android.support.v4.app.Fragment {

    public TextView timercountup = null;
    public TextView fixedtime = null;
    public TextView fsrView = null;
    Button button1;
    Button button2;
    Button manual;

    MainActivity parent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_time, container, false);

        timercountup = (TextView) rootView.findViewById(R.id.timecountup);
        fixedtime = (TextView) rootView.findViewById(R.id.fixedtime);

        button1 = (Button) rootView.findViewById(R.id.min1);
        button2 = (Button) rootView.findViewById(R.id.min3);
        manual = (Button) rootView.findViewById(R.id.manual);

        parent = (MainActivity)this.getContext();

        button1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                parent.setInterval(10);
            }
        });

        button2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                parent.setInterval(30);
            }
        });

        return rootView;
    }

}
