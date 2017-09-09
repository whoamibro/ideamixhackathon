package com.example.jeon_yongjin.bedcontroller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by jeon-yongjin on 2017. 9. 9..
 */

public class HistoryFragment extends android.support.v4.app.Fragment {
    public TextView fsrView = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);

        fsrView = rootView.findViewById(R.id.fsr_dataView);

        return rootView;
    }

}
