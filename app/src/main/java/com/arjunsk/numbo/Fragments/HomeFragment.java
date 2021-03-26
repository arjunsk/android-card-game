package com.arjunsk.numbo.Fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arjunsk.numbo.R;
import com.arjunsk.numbo.db.DaoMaster;
import com.arjunsk.numbo.db.DaoSession;
import com.arjunsk.numbo.db.G_CONTACTS;
import com.arjunsk.numbo.db.G_CONTACTSDao;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import net.colindodd.toggleimagebutton.ToggleImageButton;

import java.util.ArrayList;
import java.util.TreeSet;

import es.dmoral.toasty.Toasty;
import info.hoang8f.widget.FButton;
import mx.com.quiin.contactpicker.SimpleContact;
import mx.com.quiin.contactpicker.ui.ContactPickerActivity;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    // Home Buttons
    FButton bt_play,bt_settings;
    TextView tv_highScore, tv_gamePlays;
    ImageView logo;

    // Contact Picker
    private static final int READ_CONTACT_REQUEST = 1;
    private static final int CONTACT_PICKER_REQUEST = 2;


    // Shared Prefs
    private static final String PREFS_NAME = "APP_PREFS" ;
    SharedPreferences settings;

    //Highest Score + Game Plays
    int highest_score, game_plays;

    //DB
    G_CONTACTSDao gcontact_dao;
    final String DB_NAME ="g_contacts-db" ;
    // For Delete
    SQLiteDatabase db;
    DaoMaster master;

    FragmentManager fragmentManager;
    Fragment fragment;
    View view;

    //Toggles
    ToggleImageButton toggle_sound,toggle_vibration;
    Vibrator vibrator_obj ;
    MediaPlayer media_player_obj;




    public HomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gcontact_dao=setupDb(); //Initialise DAO
        fragmentManager = getActivity().getSupportFragmentManager();
        fragment = new GameFragment();


        vibrator_obj = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        media_player_obj = new MediaPlayer();
        media_player_obj = MediaPlayer.create(getContext(), R.raw.sound1);


    }

    private void initialize() {
        bt_play=(FButton) view.findViewById(R.id.btn_play);
        bt_settings=(FButton) view.findViewById(R.id.btn_settings);
        tv_gamePlays=(TextView) view.findViewById(R.id.tv_game_play);
        tv_highScore=(TextView) view.findViewById(R.id.tv_high_score);

        toggle_sound=(ToggleImageButton) view.findViewById(R.id.toggle_sound);
        toggle_vibration=(ToggleImageButton) view.findViewById(R.id.toggle_vibration);

        logo=(ImageView) view.findViewById(R.id.logo);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);

        initialize();

        bt_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togglesListener();
                launchContactPicker();
            }
        });

        bt_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togglesListener();
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
            }
        });


        //Highest Score and Game Plays
        settings = getActivity().getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        highest_score = settings.getInt("highest_score", 0);
        game_plays    = settings.getInt("game_plays", 0);

        tv_highScore.setText("HighScore: "+highest_score);
        tv_gamePlays.setText("GamePlay: "+game_plays);


        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeveloperInfo();
            }
        });

        //Vibration
        toggle_vibration.setChecked( settings.getBoolean("state_vibration", true) );
        toggle_vibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(toggle_vibration.isChecked()){
                    settings.edit().putBoolean("state_vibration", true).apply();
                    togglesListener();
                }else{
                    settings.edit().putBoolean("state_vibration", false).apply();
                }
            }
        });


        //Sound
        toggle_sound.setChecked( settings.getBoolean("state_sound", true) );
        toggle_sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(toggle_sound.isChecked()){
                    settings.edit().putBoolean("state_sound", true).apply();
                    togglesListener();
                }else{
                    settings.edit().putBoolean("state_sound", false).apply();
                }
            }
        });


        return view;
    }


    public void showDeveloperInfo(){

        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.dialog_about);

        dialog.findViewById(R.id.dialog_about_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void vibrate(){
        if(settings.getBoolean("state_vibration", true)) {
            vibrator_obj.vibrate(40);
        }
    }

    public void sound(){
        if(settings.getBoolean("state_sound", true)){
            media_player_obj.start();
        }
    }

    public void togglesListener(){
        vibrate();
        sound();
    }


    public void launchContactPicker() {

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Intent contactPicker = new Intent(getActivity(), ContactPickerActivity.class);
                contactPicker.putExtra(ContactPickerActivity.CP_EXTRA_SHOW_CHIPS, false);
                contactPicker.putExtra(ContactPickerActivity.CP_EXTRA_FAB_COLOR, "#FF173E");
                startActivityForResult(contactPicker, CONTACT_PICKER_REQUEST);
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toasty.error(getActivity(), "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT,true).show();
            }


        };

        new TedPermission(getActivity())
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.READ_CONTACTS)
                .check();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case CONTACT_PICKER_REQUEST:
                if(resultCode == RESULT_OK){
                    TreeSet<SimpleContact> selectedContacts = (TreeSet<SimpleContact>)data.getSerializableExtra(ContactPickerActivity.CP_SELECTED_CONTACTS);

                    if(selectedContacts.size()==0) {
                        Toasty.error(getActivity(), "No contacts selected!", Toast.LENGTH_SHORT, true).show();
                        return;
                    }

                    //Clear the tables
                    DaoMaster.dropAllTables(db, true);
                    DaoMaster.createAllTables(db, true);

                    //Save it to database
                    for (SimpleContact selectedContact : selectedContacts) {
                        Log.e("Selected", selectedContact.toString());
                        SaveToSQL(new G_CONTACTS(null,selectedContact.getDisplayName(),selectedContact.getCommunication()));
                    }

                    // Show success
                    Toasty.success(getActivity(), "Contacts added to the GameFragment!", Toast.LENGTH_SHORT, true).show();

                }else
                    Toasty.info(getActivity(), "No contacts selected!", Toast.LENGTH_SHORT, true).show();
                break;
            default:
                super.onActivityResult(requestCode,resultCode,data);
        }
    }



    //SQL QUERY
    public void SaveToSQL(G_CONTACTS log_object) {
        gcontact_dao.insert(log_object);
    }

    public G_CONTACTSDao setupDb(){
        DaoMaster.DevOpenHelper masterHelper = new DaoMaster.DevOpenHelper(getActivity(), DB_NAME, null);
        db = masterHelper.getWritableDatabase();  //get the created database db file
        master = new DaoMaster(db);//create masterDao
        DaoSession masterSession=master.newSession(); //Creates Session session
        return masterSession.getG_CONTACTSDao();
    }



    //  Boiler Plate codes
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
