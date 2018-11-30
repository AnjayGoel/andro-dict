package com.anjay.dictionary;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;


public class Settings extends Fragment {
Spinner default_selector;
Context con;
    CheckBox quick_View;
    CheckBox auto_detect_lang;
    CheckBox auto_start;
    Button save;
    private OnFragmentInteractionListener mListener;

    public Settings() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        con = getActivity();
SharedPreferences sp = con.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String[] languages = getResources().getStringArray(R.array.languages);
        ArrayAdapter adapt = new ArrayAdapter(con,R.layout.list_item_1,languages);
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        save = (Button) v.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save_settings();
            }
        });
        auto_detect_lang = (CheckBox) v.findViewById(R.id.lang_auto_detect_setting);
        auto_start = (CheckBox) v.findViewById(R.id.auto_start_setting);
        quick_View = (CheckBox) v.findViewById(R.id.quik_view_setting);
        default_selector = (Spinner) v.findViewById(R.id.default_selector);
        default_selector.setAdapter(adapt);
        default_selector.setSelection(sp.getInt("Language",29));
        auto_detect_lang.setChecked(sp.getBoolean("Auto_Detect_Lang",true));
        quick_View.setChecked(sp.getBoolean("QuickView",true));
        auto_start.setChecked(sp.getBoolean("AutoStart",false));
        return v;
    }

private void save_settings (){

   SharedPreferences.Editor sp = con.getSharedPreferences("Settings", Context.MODE_PRIVATE).edit();
    sp.clear().commit();
    sp.putBoolean("Auto_Detect_Lang", auto_detect_lang.isChecked());
    sp.putBoolean("QuickView", quick_View.isChecked());
    sp.putBoolean("AutoStart",auto_start.isChecked());
    sp.putInt("Language", default_selector.getSelectedItemPosition());
    sp.commit();
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
