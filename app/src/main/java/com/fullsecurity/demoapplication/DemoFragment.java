package com.fullsecurity.demoapplication;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fullsecurity.shared.R;

@SuppressWarnings("all")
public class DemoFragment extends android.support.v4.app.Fragment {

    private Context context;

    public DemoFragment(Context context) { this.context = context; }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.recyclerView);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        return view;
    }

}
