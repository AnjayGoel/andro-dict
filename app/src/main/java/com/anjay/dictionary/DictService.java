package com.anjay.dictionary;

import android.app.Service;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;

public class DictService extends Service implements Callback {
    @Override
    public void callback(Callback_Object res) {
        if (res.data == show_dict) {
            word = "";
            startProcedure(false);

        }
    }

    private enum State {
        Minimized(0), Maximized(0), QuickView(0);
        int Size;

        State(int size) {
            Size = size;
        }
    }

    private enum Type {
        Defination("Example"), Example("Defination");
        String text;

        Type(String s) {
            text = s;
        }

    }

    final int show_dict = 4;
    Type data_type = Type.Defination;
    static public String box_data = "Defination";
    State state = State.Minimized;
    State prev_state = State.Minimized;
    static String app_name = "Dictionary";
    static String[] languages;
    static String[] codes;
    Button prev;
    TextToSpeech tts;
    Button next;
    View main;
    ImageButton close;
    ImageButton pronounce;
    ScrollView scroll_view;
    Handler handler = new Handler();
    WindowManager.LayoutParams params;
    Point p = new Point();
    int Screenheight;
    Button language_button;
    Button translate_lang_button;
    int Screenwidth;
    int StatusBarHeight;
    TextView box;
    TextView word_box;
    HorizontalScrollView def_view;
    private View main_view;
    ImageButton toggle;
    private boolean processing = false;
    private ClipboardManager.OnPrimaryClipChangedListener clipNotifier;
    private boolean visible = false;
    private String history = "";
    private int current = 0;
    private int current_example = 0;
    static int lang_index = 29;
    ImageButton back;
    int lang_to_translate_index = 0;
    private Button type;
    boolean QuickView = true;
    private WindowManager win;
    private String word = "Word";
    TextView def_view_text;
    private ClipboardManager clip_manager;
    private Runnable WorkOnMainThread = new Runnable() { //on main(ui) thread
        @Override
        public void run() {
            prev_state = State.Minimized;
            if (!visible) {
                if (QuickView) {
                    state = State.QuickView;
                    def_view.setVisibility(View.VISIBLE);
                } else {
                    state = State.Minimized;
                    def_view.setVisibility(View.GONE);
                }
                params.y = state.Size;
                win.addView(main_view, params);
                visible = true;
            }
            current = current_example = 0;
            language_button.setText(languages[lang_index]);
            word_box.setText(word);
            box.setText(box_data);
            def_view_text.setText(box_data);
            processing = false;
        }
    };


    public DictService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {

        super.onCreate();


        InternetRelated.fillWithNoWordFound();
        InternetRelated.fillWithNoExampleFound();
        languages = getResources().getStringArray(R.array.languages);
        codes = getResources().getStringArray(R.array.codes);
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
        Thread t = new Thread() {
            @Override
            public void run() {
                tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status != TextToSpeech.ERROR) {
                            tts.setLanguage(Locale.getDefault());
                        }
                    }
                });
            }
        };
        t.start();
        final LayoutInflater f = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        main_view = f.inflate(R.layout.dict, null);
        box = (TextView) main_view.findViewById(R.id.dictBox);
        word_box = (TextView) main_view.findViewById(R.id.Word_Box);

        def_view_text = (TextView) main_view.findViewById(R.id.def_view_text);
        def_view = (HorizontalScrollView) main_view.findViewById(R.id.def_view);
        toggle = (ImageButton) main_view.findViewById(R.id.toggle);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle_size();
            }
        });
        back = (ImageButton) main_view.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remove();
                (PermanentBackgroundService.return_cb()).callback(new Callback_Object(0,0,"back"));

            }
        });
        close = (ImageButton) main_view.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                remove();
            }
        });
        language_button = (Button) main_view.findViewById(R.id.language);
        language_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                build(true, language_button);
            }
        });
        translate_lang_button = (Button) main_view.findViewById(R.id.lang_to_translate);
        translate_lang_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                build(false, translate_lang_button);
            }
        });

        type = (Button) main_view.findViewById(R.id.type);
        type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data_type == Type.Defination) {
                    data_type = Type.Example;
                    type.setText(data_type.text);
                    box.setText(InternetRelated.example_array.get(current_example));
                } else {
                    data_type = Type.Defination;
                    type.setText(data_type.text);
                    box.setText(InternetRelated.def_array.get(current));
                }
            }
        });

        pronounce = (ImageButton) main_view.findViewById(R.id.speakButton);
        pronounce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak();
            }
        });
        scroll_view = (ScrollView) main_view.findViewById(R.id.scroll_View);
        next = (Button) main_view.findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextMeaning();
            }
        });
        prev = (Button) main_view.findViewById(R.id.prev);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevMeaning();
            }
        });
        main_view.setOnTouchListener(new View.OnTouchListener() {
            private int initialY;
            private float initialTouchY;
            private float initialTouchX;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        if (state != State.QuickView) prev_state = state;
                        def_view.setVisibility(View.GONE);
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
                                def_view.setVisibility(View.VISIBLE);
                            }
                            if (state == State.QuickView && def_view.getVisibility() == View.GONE)
                                def_view.setVisibility(View.VISIBLE);

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
                    win.updateViewLayout(main_view, params);
                } catch (Exception ex) {
                    Log.d(app_name, ex.toString());
                }
                return true;
            }
        });
        if (QuickView) state = State.QuickView;
        params.y = state.Size;
        params.x = 0;
        handler.post(WorkOnMainThread);
        clip_manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipNotifier = new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                word = "" + clip_manager.getPrimaryClip().getItemAt(0).getText().toString();
                startProcedure(false);
            }
        };
        clip_manager.addPrimaryClipChangedListener(clipNotifier);
    }

    private void speak() {
        if (Build.VERSION.SDK_INT >= 21) {
            String utteranceId = this.hashCode() + "";
            tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        } else {
            HashMap<String, String> map = new HashMap<>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
            tts.speak(word, TextToSpeech.QUEUE_FLUSH, map);
        }
    }

    private void startProcedure(boolean reTranslate) {
        word = prepareString(word);
        if (word.length() > 45 || word.equals("")) return;
        if (word.equals(history) && !reTranslate) {
            WorkOnMainThread.run();
            return;
        }
        getMeaning(reTranslate);
        data_type = Type.Defination;
        history = word;

    }

    private void build(final boolean isFirstLang, final Button button) {

        WindowManager.LayoutParams params2 = new WindowManager.LayoutParams(
                (Screenwidth * 9) / 10, (Screenheight * 4) / 5,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSPARENT);
        params2.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;

        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.list_item, languages);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        main = inflater.inflate(R.layout.language_select_view, null);

        main.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        if (event.getY() < 0 || event.getY() > main.getHeight() || main.getX() < 0 || event.getX() > main.getWidth()) {
                            win.removeView(main);
                        }
                        break;

                }

                return false;
            }
        });

        Button cancel = (Button) main.findViewById(R.id.lang_select_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                win.removeView(main);
            }
        });
        ListView language_selector = (ListView) main.findViewById(R.id.language);
        language_selector.setAdapter(adapter);
        if (isFirstLang) {
            language_selector.setSelection(lang_index);
        } else {
            language_selector.setSelection(lang_to_translate_index);
        }

        language_selector.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                win.removeView(main);
                button.setText(languages[position]);
                if (isFirstLang) {
                    lang_index = position;
                    InternetRelated.lang = codes[position];
                } else {
                    lang_to_translate_index = position;
                    InternetRelated.lang_to_translate = codes[position];
                }
                startProcedure(true);
            }
        });

        win.addView(main, params2);

    }

    private String prepareString(String s) {
        s = s.replaceAll("^\\s|\\s$", "");
        s = s.replaceAll("[^\\w\\s]", "").toLowerCase();

        return s;
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

                def_view.setVisibility(View.GONE);
                params.y = state.Size;
                break;
            case Maximized:
                state = State.QuickView;
                toggle.setImageResource(R.drawable.down);
                params.y = state.Size;
                break;

        }
        if (state != State.QuickView) prev_state = state;
        win.updateViewLayout(main_view, params);

    }

    private void getMeaning(final boolean reTranslate) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                if (!processing) {
                    processing = true;
                    box_data = "Please Wait...";
                    handler.post(WorkOnMainThread);
                }
                box_data = InternetRelated.getDef(word, reTranslate);
                //update text;
                if (!processing) {
                    processing = true;
                    handler.post(WorkOnMainThread);
                }
            }
        });
        t.start();

    }


    public void prevMeaning() {
        if (data_type == Type.Defination) {
            if (current == 0 || InternetRelated.def_array.size() == 0) return;
            current--;
            box_data = InternetRelated.def_array.get(current);
            box.setText(box_data);
        } else {
            if (current_example == 0 || InternetRelated.example_array.size() == 0) return;
            current_example--;
            box_data = InternetRelated.example_array.get(current_example);
            box.setText(box_data);
        }
    }

    public void nextMeaning() {
        if (data_type == Type.Defination) {
            if (current == InternetRelated.def_array.size() - 1 || InternetRelated.def_array.size() == 0)
                return;
            current++;
            box_data = InternetRelated.def_array.get(current);
            box.setText(box_data);
            def_view_text.setText(box_data);
        } else {
            if (current_example == InternetRelated.example_array.size() - 1 || InternetRelated.example_array.size() == 0)
                return;
            current_example++;
            box_data = InternetRelated.example_array.get(current_example);
            def_view_text.setText(box_data);
            box.setText(box_data);
        }
    }

    private void remove() {
        data_type = Type.Defination;
        type.setText(data_type.text);
        box_data = InternetRelated.def_array.get(0);
        current = current_example = 0;
        if (!visible) return;
        win.removeView(main_view);
        visible = false;
    }

    private void fade_out() {
        int dist = (Screenheight - params.y) / 20;
        for (int i = 1; i <= 20; i++) {
            params.y += dist;
            win.updateViewLayout(main_view, params);
            Log.d(app_name, params.y + "");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        win.removeView(main_view);

    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "destroying", Toast.LENGTH_SHORT).show();
        remove();
        tts.shutdown();
        clip_manager.removePrimaryClipChangedListener(clipNotifier);
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
