package com.fullsecurity.client;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fullsecurity.server.SDVServer;
import com.fullsecurity.shared.MainActivity;
import com.fullsecurity.shared.R;

@SuppressWarnings("all")
public class TopLeftFragment extends android.support.v4.app.Fragment {

    private SDVServer sdvServer;
    private View view;
    public SDVClient sdvClient;
    MainActivity mainActivity;

    public TopLeftFragment(MainActivity m) {
        mainActivity = m;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_main11, container, false);
        sdvClient = new SDVClient(view, mainActivity, "tlfrg", 0);
        return view;
    }

    public void runTest() {
        sdvClient.runTest(mainActivity,0);
    }

}
