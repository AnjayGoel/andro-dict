package com.anjay.dictionary;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class Launcher_main extends Fragment {

    int Running = 1;
    int Stopped = 0;

    int state = Stopped;
    Button button;
    Context con;
    private void call() {

        Intent inMain = new Intent(con,PermanentBackgroundService.class);
        con.startService(inMain);
        Intent in = new Intent(con, DictService.class);
        con.startService(in);
    }
    private void end (){
        Intent inMain = new Intent(con, PermanentBackgroundService.class);
        con.stopService(inMain);
        Intent in = new Intent(con, DictService.class);
        con.stopService(in);
    }

    private void exitApp(){
        android.os.Process.killProcess(android.os.Process.myUid());
        System.exit(0);
    }

    private OnFragmentInteractionListener mListener;

    public Launcher_main() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        con = getActivity();
        View view = inflater.inflate(R.layout.fragment_launcher_main, container, false);

        button = (Button) view.findViewById(R.id.service_switch);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (state == Running) {
                    button.setText("Start");
                    end();
                    state = Stopped;
                } else if (state == Stopped) {
                    button.setText("Stop");
                    call();
                    state = Running;
                }

            }
        });
        return view;
    }

   
    
    
    
    
    
    
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
