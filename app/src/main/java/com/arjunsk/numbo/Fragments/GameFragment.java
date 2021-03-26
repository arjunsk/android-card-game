package com.arjunsk.numbo.Fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.arjunsk.numbo.R;
import com.arjunsk.numbo.adapter.SwipeDeckAdapter;
import com.arjunsk.numbo.db.DaoMaster;
import com.arjunsk.numbo.db.DaoSession;
import com.arjunsk.numbo.db.G_CONTACTS;
import com.arjunsk.numbo.db.G_CONTACTSDao;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.daprlabs.cardstack.SwipeDeck;
import com.jungly.gridpasswordview.GridPasswordView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import es.dmoral.toasty.Toasty;
import info.hoang8f.widget.FButton;
import mehdi.sakout.fancybuttons.FancyButton;
import mx.com.quiin.contactpicker.SimpleContact;

import static android.content.Context.VIBRATOR_SERVICE;


public class GameFragment extends Fragment implements View.OnClickListener{

    private OnFragmentInteractionListener mListener;

    //Contact Card
    SwipeDeck contact_card;
    ArrayList<SimpleContact> selected_contacts;
    SwipeDeckAdapter contact_card_adapter;

    // Dialer
    FancyButton btn1,btn2,btn3,btn4,btn5,btn6,btn7,btn8,btn9,btn0;
    FancyButton btn_next_card;
    FancyButton btn_call;

    EditText phone_number_view;
    FancyButton btn_backspace;


    FancyButton btn_pause;
    TextView timer_view, score_view;

    Long millisecondResume;
    Long totalSeconds;
    CountDownTimer count_down_timer;

    Dialog pauseDialog; //gameOverDialog;


    //Pause Dialog
    FButton dia_btn1,dia_btn2,dia_btn3,dia_btn4;
    FancyButton dia_btn_show_number;
    GridPasswordView dia_phone_number_view;

    //GameFragment Over Dialog
    FButton dia_go_btnHome, dia_go_btnExit, dia_go_btnAgain;
    TextView dia_go_show_score;


    //DB
    private G_CONTACTSDao gcontact_dao;
    private  final String DB_NAME ="g_contacts-db" ;

    int score;
    int top_card_index;
    boolean looked_at_answer;

    //Screen changing
    FragmentManager fragmentManager;
    View view;

    // Shared Prefs
    private static final String PREFS_NAME = "APP_PREFS" ;
    SharedPreferences settings;

    //Highest Score + Game Plays
    int highest_score, game_plays;

    //Toggle
    Vibrator vibrator_obj ;
    MediaPlayer media_player_obj;
    boolean state_vibrator, state_sound;

    public GameFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gcontact_dao=setupDb(); //Initialise DAO
        settings = getActivity().getApplicationContext().getSharedPreferences(PREFS_NAME, 0);


        vibrator_obj = (Vibrator) getActivity().getSystemService(VIBRATOR_SERVICE);
        media_player_obj = MediaPlayer.create(getActivity(), R.raw.sound1);

        state_vibrator=settings.getBoolean("state_vibration", true);
        state_sound=settings.getBoolean("state_sound", true);
    }


    public void initialize(View v){



        selected_contacts = new ArrayList<>();

        //UI
        contact_card = (SwipeDeck) v.findViewById(R.id.swipe_deck);

        btn1=(FancyButton) v.findViewById(R.id.btn_num_1);
        btn2=(FancyButton) v.findViewById(R.id.btn_num_2);
        btn3=(FancyButton) v.findViewById(R.id.btn_num_3);
        btn4=(FancyButton) v.findViewById(R.id.btn_num_4);
        btn5=(FancyButton) v.findViewById(R.id.btn_num_5);
        btn6=(FancyButton) v.findViewById(R.id.btn_num_6);
        btn7=(FancyButton) v.findViewById(R.id.btn_num_7);
        btn8=(FancyButton) v.findViewById(R.id.btn_num_8);
        btn9=(FancyButton) v.findViewById(R.id.btn_num_9);
        btn0=(FancyButton) v.findViewById(R.id.btn_num_0);
        btn_call=(FancyButton) v.findViewById(R.id.btn_call);
        btn_next_card =(FancyButton) v.findViewById(R.id.btn_next_card);

        btn_backspace =(FancyButton) v.findViewById(R.id.btn_backspace);
        phone_number_view=(EditText) v.findViewById(R.id.digits);

        btn_pause = (FancyButton) v.findViewById(R.id.btn_top_pause);
        score_view = (TextView)   v.findViewById(R.id.tv_curr_score);
        timer_view = (TextView)   v.findViewById(R.id.tv_curr_time);



        //UI Listeners
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);
        btn5.setOnClickListener(this);
        btn6.setOnClickListener(this);
        btn7.setOnClickListener(this);
        btn8.setOnClickListener(this);
        btn9.setOnClickListener(this);
        btn0.setOnClickListener(this);
        btn_backspace.setOnClickListener(this);
        btn_next_card.setOnClickListener(this);
        btn_pause.setOnClickListener(this);
        btn_call.setOnClickListener(this);

        score=0;
        top_card_index=0;
        looked_at_answer=false;


        fragmentManager = getActivity().getSupportFragmentManager();
    }

    public void initial_pause_dialog(){
        pauseDialog = new Dialog(getActivity());
        pauseDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        pauseDialog.setContentView(R.layout.dialog_pause);
        pauseDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dia_btn1 = (FButton) pauseDialog.findViewById(R.id.dialog_btn_1);
        dia_btn2 = (FButton) pauseDialog.findViewById(R.id.dialog_btn_2);
        dia_btn3 = (FButton) pauseDialog.findViewById(R.id.dialog_btn_3);
        dia_btn4 = (FButton) pauseDialog.findViewById(R.id.dialog_btn_4);

        dia_btn_show_number = (FancyButton) pauseDialog.findViewById(R.id.btn_show_number);
        dia_phone_number_view= (GridPasswordView) pauseDialog.findViewById(R.id.show_phone_number);

    }


    public void vibrate(){
        if(state_vibrator){
            vibrator_obj.vibrate(40);
        }
    }


    public void sound(){
        if(state_sound){
            media_player_obj.start();
        }
    }

    public void togglesListener(){
        vibrate();
        sound();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.fragment_game, container, false);
        initialize(view);
        if( !load_db_data())
            return view;

        // Backspace long click
        btn_backspace.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                togglesListener();
                phone_number_view.setText("");
                return true;
            }
        });

        // Contact Card
        contact_card_adapter = new SwipeDeckAdapter(selected_contacts, getActivity());
        if(contact_card != null)contact_card.setAdapter(contact_card_adapter);
        contact_card.setEventCallback(new SwipeDeck.SwipeEventCallback() {
            @Override
            public void cardSwipedLeft(int position) {
                phone_number_view.setText("");
                top_card_index=position+1;
            }

            @Override
            public void cardSwipedRight(int position) {
                phone_number_view.setText("");
                top_card_index=position+1;
            }

            @Override
            public void cardsDepleted() {
                gameOver();
            }

            @Override
            public void cardActionDown() {

            }

            @Override
            public void cardActionUp() {

            }
        });

        // Start timer only at the last
        millisecondResume = 0L;
        totalSeconds =(selected_contacts.size()*15000L) + 1000L;
        countDownStart(totalSeconds);
        initial_pause_dialog();

        return view;
    }



    // Pause on Back button click
    public boolean onBackPressed() {
        pauseGame();
        return true;
    }

    public void changeToFragment(Fragment fragment){
        if(pauseDialog!=null && pauseDialog.isShowing()) pauseDialog.dismiss();
        if(count_down_timer!=null) count_down_timer.cancel();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    }




    // Game Checker
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_num_0:
                updateNumberView(0);
                break;
            case R.id.btn_num_1:
                updateNumberView(1);
                break;
            case R.id.btn_num_2:
                updateNumberView(2);
                break;
            case R.id.btn_num_3:
                updateNumberView(3);
                break;
            case R.id.btn_num_4:
                updateNumberView(4);
                break;
            case R.id.btn_num_5:
                updateNumberView(5);
                break;
            case R.id.btn_num_6:
                updateNumberView(6);
                break;
            case R.id.btn_num_7:
                updateNumberView(7);
                break;
            case R.id.btn_num_8:
                updateNumberView(8);
                break;
            case R.id.btn_num_9:
                updateNumberView(9);
                break;
            case R.id.btn_backspace:
                updateNumberView(-1);
                break;
            case R.id.btn_call :  check();
                break;
            case R.id.btn_next_card:  contact_card.swipeTopCardLeft(150);
                break;

            case R.id.btn_top_pause : pauseGame();
                break;

        }
        togglesListener();
    }

    public void updateNumberView(int digit){
        String text=phone_number_view.getText().toString();
        int len=text.length();

        if (digit >= 0 && digit <= 9) {
            phone_number_view.setText(text + "" + digit);
        } else if (digit == -1) {
            if (len > 0) {
                phone_number_view.getText().delete(len - 1, len);
            }
        }

    }

    public String getPlainPhone(String phone){
        return phone.replaceAll("[\\D]", "");
    }

    public void check(){

        if(top_card_index>=selected_contacts.size()){
            return;
        }

        String entered_number = phone_number_view.getText().toString();
        String correct_number = selected_contacts.get(top_card_index).getCommunication();

        // Remove all the dashes and plus
        correct_number = getPlainPhone(correct_number);


        if(entered_number.equals(correct_number)){
            //Increment score
            score+=10;
            score_view.setText("Score: "+score);

            //shake scoreboard
            YoYo.with(Techniques.Tada)
                    .duration(800)
                    .repeat(1)
                    .playOn(view.findViewById(R.id.tv_curr_score));

            //Move to next
            contact_card.swipeTopCardLeft(1);

        }else{
            YoYo.with(Techniques.Shake)
                    .duration(800)
                    .repeat(1)
                    .playOn(view.findViewById(R.id.tv_curr_score));

            contact_card.swipeTopCardRight(1);
        }
    }




    // Pause , Game Over Dialogs
    public void pauseGame(){
        countDownPause();

        //Resume
        dia_btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togglesListener();
                pauseDialog.dismiss();
                countDownResume();
            }
        });

        //Restart
        dia_btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togglesListener();
                changeToFragment(new GameFragment());
            }
        });

        //Home
        dia_btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togglesListener();
                changeToFragment(new HomeFragment());
            }
        });

        //Exit
        dia_btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togglesListener();
                getActivity().finishAffinity();
            }
        });


        // Show Phone Number
        dia_btn_show_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Unlock
                dia_btn_show_number.setIconResource("\uf09c");
                dia_btn_show_number.setEnabled(false); // no more clicking again on lock icon

                //update looked_at_answer
                looked_at_answer=true;

                togglesListener();

                //Show Phone Number
                dia_phone_number_view.togglePasswordVisibility();

            }
        });


        // Phone Number view
        dia_phone_number_view.clearFocus();
        dia_phone_number_view.setOnKeyListener(null);
        dia_phone_number_view.setClickable(false);
        dia_phone_number_view.setFocusable(false);
        dia_phone_number_view.setFocusableInTouchMode(false);

        // # Hiding keyboard
        View view2 = getActivity().getCurrentFocus();
        if (view2 != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view2.getWindowToken(), 0);
        }
        // 2nd method
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        dia_phone_number_view.setPassword(getPlainPhone(selected_contacts.get(top_card_index).getCommunication()));



        pauseDialog.show();


        pauseDialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    pauseDialog.dismiss();
                    countDownResume();
                }
                return true;
            }
        });


    }

    public void gameOver(){

        if(pauseDialog!=null && pauseDialog.isShowing()) {
            pauseDialog.dismiss();
        }

        countDownPause();

        togglesListener();

        //Get Shared Prefs
        highest_score = settings.getInt("highest_score", 0);
        game_plays    = settings.getInt("game_plays", 0);

        //Put in Shared Prefs
        settings.edit().putInt("game_plays", game_plays + 1 ).apply();
        if(score>highest_score){
            settings.edit().putInt("highest_score", score).apply();
        }

        final Dialog gameOverDialog = new Dialog(getActivity());
        gameOverDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        gameOverDialog.setContentView(R.layout.dialog_gameover);
        gameOverDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dia_go_btnHome = (FButton) gameOverDialog.findViewById(R.id.dialog_go_home);
        dia_go_btnExit = (FButton) gameOverDialog.findViewById(R.id.dialog_go_exit);
        dia_go_btnAgain= (FButton) gameOverDialog.findViewById(R.id.dialog_go_play_again);
        dia_go_show_score=(TextView) gameOverDialog.findViewById(R.id.dialog_go_score);


        //Restart
        dia_go_btnAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togglesListener();
                gameOverDialog.dismiss();
                changeToFragment(new GameFragment());
            }
        });

        //Home
        dia_go_btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togglesListener();
                gameOverDialog.dismiss();
                changeToFragment(new HomeFragment());
            }
        });

        //Exit
        dia_go_btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togglesListener();
                getActivity().finishAffinity();
            }
        });

        dia_go_show_score.setText("Your Score: "+score);

        gameOverDialog.show();

        gameOverDialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    getActivity().finishAffinity();
                }
                return true;
            }
        });



    }


    //Pause Resume and Start Count Down
    public void countDownStart(Long startAt) {
        count_down_timer = new CountDownTimer(startAt , 1000) {
            public void onTick(long millisUntilFinished) {
                millisecondResume = millisUntilFinished;
                timer_view.setText("" + String.format(Locale.ENGLISH,"%02d", millisUntilFinished /1000) );
            }
            public void onFinish() {
                timer_view.setText("Times Up!");
                //Jumping Textviews
                YoYo.with(Techniques.Tada)
                        .duration(800)
                        .repeat(1)
                        .playOn(view.findViewById(R.id.tv_curr_time));


                //GameFragment Over
                gameOver();
            }

        };
        count_down_timer.start();
    }

    public void countDownPause(){
        count_down_timer.cancel();
    }

    public void countDownResume(){
        if(looked_at_answer){
            //Remove the top card
            contact_card.swipeTopCardRight(5000);

            //restore
            looked_at_answer=false;
        }

        countDownStart(millisecondResume);

        //Jumping Textviews
        YoYo.with(Techniques.Tada)
                .duration(800)
                .repeat(1)
                .playOn(view.findViewById(R.id.tv_curr_time));

        //Restore phone number toggle icon
        dia_btn_show_number.setIconResource("\uf023");
        dia_btn_show_number.setEnabled(true);
        dia_phone_number_view.setPasswordVisibility(false);

    }


    //SQL
    public boolean  load_db_data(){
        List<G_CONTACTS> log_list = gcontact_dao.queryBuilder().orderDesc(G_CONTACTSDao.Properties.Id).build().list();
        if(log_list.size()<=0){
            // No contact selected
            Toasty.error(getActivity(), "Please select your favorite contacts!", Toast.LENGTH_SHORT, true).show();

            //Go to main screen
            changeToFragment(new HomeFragment());

            return false;
        }
        for(int i=0;i<log_list.size();i++){
            selected_contacts.add(new SimpleContact(log_list.get(i).getDisplayName(),log_list.get(i).getCommunication()));
        }

        //Shuffling
        long seed = System.nanoTime();
        Collections.shuffle(selected_contacts, new Random(seed));
        return true;
    }

    public G_CONTACTSDao setupDb(){
        DaoMaster.DevOpenHelper masterHelper = new DaoMaster.DevOpenHelper(getActivity(), DB_NAME, null); //create database db file if not exist
        SQLiteDatabase db = masterHelper.getWritableDatabase();  //get the created database db file
        DaoMaster master = new DaoMaster(db);//create masterDao
        DaoSession masterSession=master.newSession(); //Creates Session session
        return masterSession.getG_CONTACTSDao();
    }


    // Boiler Plate
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
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
