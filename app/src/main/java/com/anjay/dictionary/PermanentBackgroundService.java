package com.anjay.dictionary;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class PermanentBackgroundService extends Service implements Callback {

    static Callback return_cb (){
        return cb;
    }

    @Override
    public void callback(Callback_Object o) {
        if (o.data_s=="back"){
            prepare_view(0);
            handler.post(WorkOnMainThread);
        }
    }

    private enum State {
        Minimized(0), Maximized(0), QuickView(0);
        int Size;

        State(int size) {
            Size = size;
        }
    }
    static Callback cb;
    View container;
    State state = State.Maximized;
    State prev_state = State.Maximized;
    static String app_name = "Dictionary";
    public Context con = this;
    ImageButton close;
    Handler handler = new Handler();
    WindowManager.LayoutParams params;
    Point p = new Point();
    int Screenheight;
    int Screenwidth;
    int StatusBarHeight;
    private FingerprintHandler fpp;
    private FingerprintManager fp;
    TextView word_box;
    private View main_float_window;
    ImageButton toggle;
    ImageButton back;
    private boolean visible = false;
    boolean QuickView = true;
    private WindowManager win;
    Runnable WorkOnMainThread = new Runnable() { //on main(ui) thread
        @Override
        public void run() {
            prev_state = State.Minimized;
            if (!visible) {
                if (QuickView) {
                    state = State.QuickView;

                } else {
                    state = State.Minimized;
                }
                params.y = state.Size;

                win.addView(main_float_window, params);
                visible = true;
            }

        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void FP() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fp = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
            if (fpp != null) fpp.cancel();
            fpp = new FingerprintHandler(this);
            fpp.startAuth(fp);
        }

    }

    void prep_main_layout() {
    }

    static String eq = "";

    void prep_calc_layout() {

        final TextView[] button_arr = new TextView[27];
        button_arr[0] = (TextView) main_float_window.findViewById(R.id.calcresult);
        button_arr[1] = (TextView) main_float_window.findViewById(R.id.clear);
        button_arr[2] = (TextView) main_float_window.findViewById(R.id.equate);
        button_arr[26] = (TextView) main_float_window.findViewById(R.id._0);
        button_arr[3] = (TextView) main_float_window.findViewById(R.id._1);
        button_arr[4] = (TextView) main_float_window.findViewById(R.id._2);
        button_arr[5] = (TextView) main_float_window.findViewById(R.id._3);
        button_arr[6] = (TextView) main_float_window.findViewById(R.id._4);
        button_arr[7] = (TextView) main_float_window.findViewById(R.id._5);
        button_arr[8] = (TextView) main_float_window.findViewById(R.id._6);
        button_arr[9] = (TextView) main_float_window.findViewById(R.id._7);
        button_arr[10] = (TextView) main_float_window.findViewById(R.id._8);
        button_arr[11] = (TextView) main_float_window.findViewById(R.id._9);
        button_arr[12] = (TextView) main_float_window.findViewById(R.id._add);
        button_arr[13] = (TextView) main_float_window.findViewById(R.id._subtract);
        button_arr[14] = (TextView) main_float_window.findViewById(R.id._multiply);
        button_arr[15] = (TextView) main_float_window.findViewById(R.id._divide);
        button_arr[16] = (TextView) main_float_window.findViewById(R.id.log);
        button_arr[17] = (TextView) main_float_window.findViewById(R.id.sin);
        button_arr[18] = (TextView) main_float_window.findViewById(R.id.cos);
        button_arr[19] = (TextView) main_float_window.findViewById(R.id.tan);
        button_arr[20] = (TextView) main_float_window.findViewById(R.id.pi);
        button_arr[21] = (TextView) main_float_window.findViewById(R.id.e);
        button_arr[22] = (TextView) main_float_window.findViewById(R.id.bo);
        button_arr[23] = (TextView) main_float_window.findViewById(R.id.bc);
        button_arr[24] = (TextView) main_float_window.findViewById(R.id.pow);
        button_arr[25] = (TextView) main_float_window.findViewById(R.id.decimal);
        button_arr[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button_arr[0].setText(""+Math_parser.eval_init(eq));
                eq="";
            }


        });
        button_arr[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eq = "";
                button_arr[0].setText(eq);
            }
        });
        for (int i = 3; i < button_arr.length; i++) {
            button_arr[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    eq += ((TextView) view).getText().toString();
                    button_arr[0].setText(eq);
                }
            });
        }


    word_box.setText("Calculator");
}

    void prep_bookmark_layout() {
    }

    void prep_clipboard_layout() {
    }

    void prep_browser_layout() {
    }

    void prepare_view(int id) {
        win = (WindowManager) getSystemService(WINDOW_SERVICE);
        win.getDefaultDisplay().getSize(p);
        Screenheight = p.y;
        Screenwidth = p.x;
        StatusBarHeight = getStatusBarHeight();
        State.Maximized.Size = Screenheight - (Screenheight / 2) - StatusBarHeight;
        State.Minimized.Size = Screenheight - (Screenheight / 20) - StatusBarHeight;
        State.QuickView.Size = Screenheight - (Screenheight / 10) - StatusBarHeight;
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                Screenheight,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSPARENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;


        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        final LayoutInflater f = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        main_float_window = f.inflate(R.layout.main_float_window, null);


        word_box = (TextView) main_float_window.findViewById(R.id.Title_Box);
        toggle = (ImageButton) main_float_window.findViewById(R.id.toggleMain);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle_size();
            }
        });
        close = (ImageButton) main_float_window.findViewById(R.id.closeMain);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                remove();
            }
        });
        back = (ImageButton) main_float_window.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remove();
                prepare_view(0);
                handler.post(WorkOnMainThread);
            }
        });

        main_float_window.setOnTouchListener(new View.OnTouchListener() {
            private int initialY;
            private float initialTouchY;
            private float initialTouchX;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        if (state != State.QuickView) prev_state = state;
                        initialY = params.y;
                        initialTouchY = event.getRawY();
                        initialTouchX = event.getRawX();
                        break;

                    case MotionEvent.ACTION_UP:

                        if (Math.abs(initialTouchX - event.getRawX()) < 1 && Math.abs(initialTouchY - event.getRawY()) < 1) {

                            if (state == State.Minimized) {
                                state = State.QuickView;
                                params.y = state.Size;
                                toggle.setImageResource(R.drawable.up);
                            }

                            break;
                        }
                        if ((event.getX() > initialTouchX + (Screenwidth / 3) && Math.abs(initialTouchY - event.getRawY()) < 40) || (event.getX() < initialTouchX - (Screenwidth / 2) && Math.abs(initialTouchY - event.getRawY()) < 40)) {
                            remove();
                        }
                        if (params.y < Screenheight - (Screenheight / 4)) {

                            state = State.Maximized;
                            toggle.setImageResource(R.drawable.down);
                            params.y = state.Size;

                        } else {
                            state = State.Minimized;
                            toggle.setImageResource(R.drawable.up);
                            params.y = state.Size;


                        }


                        break;
                    case MotionEvent.ACTION_MOVE:
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        if (params.y < Screenheight - (Screenheight / 2) - StatusBarHeight) {
                            params.y = State.Maximized.Size;

                        } else if (params.y > Screenheight - (Screenheight / 24) - StatusBarHeight) {
                            params.y = State.Minimized.Size;
                        }
                }
                try {
                    win.updateViewLayout(main_float_window, params);
                } catch (Exception ex) {
                    Toast.makeText(con, ex.toString(), Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
        if (QuickView) state = State.QuickView;
        params.y = state.Size;
        params.x = 0;

        View container_view = main_float_window.findViewById(R.id.main_container);
        ViewGroup parent = (ViewGroup) container_view.getParent();
        int ind = parent.indexOfChild(container_view);
        parent.removeView(container_view);
        switch (id) {
            case 0://main_window

                container = f.inflate(R.layout.functions, null);
                parent.addView(container, ind, lp);
                prep_main_layout();
                break;
            case 2://calc
                container = f.inflate(R.layout.calc, null);
                parent.addView(container, ind, lp);
                prep_calc_layout();
                break;
            case 3://bookmark
                container = f.inflate(R.layout.bookmark, null);
                parent.addView(container, ind, lp);
                prep_bookmark_layout();
                break;
            case 4://clipboard
                container = f.inflate(R.layout.clipboard, null);
                parent.addView(container, ind, lp);
                prep_clipboard_layout();
                break;
            case 5://browser
                container = f.inflate(R.layout.browser, null);
                parent.addView(container, ind, lp);
                prep_browser_layout();
                break;
        }

    }

    @Override
    public void onCreate() {
        this.cb = this;
        prepare_view(2);
        super.onCreate();
        handler.post(WorkOnMainThread);

    }


    private void toggle_size() {
        Log.d(app_name, "" + state.toString());
        switch (state) {

            case Minimized:
                state = State.QuickView;
                toggle.setImageResource(R.drawable.up);
                params.y = state.Size;
                break;
            case QuickView:
                if (prev_state == State.Minimized) {
                    state = State.Maximized;
                    toggle.setImageResource(R.drawable.down);
                } else {
                    state = State.Minimized;
                    toggle.setImageResource(R.drawable.up);
                }

                params.y = state.Size;
                break;
            case Maximized:
                state = State.QuickView;
                toggle.setImageResource(R.drawable.down);
                params.y = state.Size;
                break;

        }
        if (state != State.QuickView) prev_state = state;
        win.updateViewLayout(main_float_window, params);

    }

    private void remove() {
        if (!visible) return;
        win.removeView(main_float_window);
        visible = false;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Destroying Background Service", Toast.LENGTH_LONG).show();
        remove();
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
