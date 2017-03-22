package com.runrmby.runner;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    //private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            root.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

            titleScreen.post(new Runnable() {
                @Override
                public void run() {
                    System.out.println("MA| titleScreen Size: " + titleScreen.getMeasuredWidth() + ", " + titleScreen.getMeasuredHeight());
                    windowSize.set(titleScreen.getMeasuredWidth(), titleScreen.getMeasuredHeight());
                    if(gameState != GAME_PLAYING && gameState != GAME_INITIALIZING && gameState != GAME_LOST && gameState != GAME_WON) {
                        gameScreen.setBackgroundSizePos(windowSize);
                        gameScreen.setTranslationY(windowSize.y);
                    }
                    if(!initialized) {
                        gameScreen.resetVariables();
                        initialized = true;
                        gameScreen.pause();
                        gameScreen.resume();
                    }
                }
            });

        }
    };
    //private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            //mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    //---------------------- Game variables ----------------------------

    SharedPreferences sharedPref;
    SharedPreferences.Editor prefEditor;

    // NEW STATES
    public static final int MAIN_MENU = 4;
    public static final int PAUSED = 5;
    public static final int GAME_INITIALIZING = 6;
    public static final int GAME_PLAYING = 7;
    public static final int GAME_WON = 8;
    public static final int GAME_LOST = 9;
    public static final int TRANSIT_TO_GAME = 10;
    public static final int TRANSIT_TO_MM = 11;
    public static final int SETTINGS =  12;

//    private int colWit;
    private int xp;
    boolean timeTrial2Flag = false; //Necessary for level 0 because it has a second time trial instead of a marathon mode

    private MediaPlayer activeMusic;
    private static final int MENU_MUSIC = 0;
    private static final int GAME_MUSIC_1 = 1;
    private static final int LOSE_MUSIC_1 = 2;
    private static final int WIN_MUSIC_1 = 3;
    private static final int WIN_MUSIC_2 = 4;
    private static final int WIN_MUSIC_NEW_RECORD = 5;
    private static final int WHISTLE_MUSIC = 6;
    private int nowPlaying;
    boolean musicMuted;
    boolean musicPausedByLeavingApp;
    int activeMusicPosition;
//    Thread musicThread;
    private boolean whistleMusicSelected = false;

    //Sound effects
    public SoundPool sfx;
//    public int badSoundId;
//    public int goodSoundId;
//    public int crashSoundId;
//    public int crashSound2Id;
//    public int crashSound3Id;
//    public int crashSound4Id;
    public int crashSound5Id;
//    public int truckHornId;
//    public int carHornId;
//    public int wilhelmScreamId;

    private Button playButton;
    private Button playMarathonButton;
    private Button tempButton;
    private Button playAgainButton;
    private Button mainMenuButton;
    private Button resumeButton;
    private Button pauseMMButton;
    private Button resetButton;
    private Button settingsButton;
    private Button exitSettingsButton;
    private ToggleButton musicMuteButton;
    private ToggleButton whistleMusicButton;
    private Button nextLocationButton;
    private Button previousLocationButton;
//    private Button statsButton;
    private Button creditsButton;

    private TextView endGameHeaderText;
    private TextView endGameUserTime;
    private TextView endGameBestTime;
    private TextView endGameText;
    private TextView endGameStatNames;
    private TextView endGameStats;
    private TextView menuText;
    public TextView timer;
    public TextView experiencePointsText;
    private TextView titleText;
    private TextView creditsText;
    private TextView menuTimeText;
    private TextView menuDistanceText;
    private TextView loadingText;

//    public Typeface font1;
    public Typeface font2;
//    public Typeface font3;
//    public Typeface font4;

    private RelativeLayout gameEndMenu;
    private RelativeLayout pauseMenu;
    private RelativeLayout settingsMenu;
    private RelativeLayout mainMenu;
    private FrameLayout gameMenu;
    private FrameLayout root;

    private View titleScreen;
    private View alternateTitleScreen;
    private GameView gameScreen;

    private Point windowSize = new Point(1000, 2000); // arbitrary values

    private int gameState = MAIN_MENU;
    private int previousGameState = MAIN_MENU;

    public int locationState = 0;  //0=original road artwork, 1 would be next location, etc. todo save variable
    private static int numLocations = 4;
    private int locationRes;
    private int locationsUnlocked;
    private static int xpToUnlockEachLvl = 100;    //todo arbitrary value
    private Animation fadeOut;

    //private File bestTimeFilePath;
    private Long savedTime;
    private Time bestTime = new Time();
    private Time timeDifferential = new Time();
    private Time yourTime;
    private float bestDistance;
    private float distanceDifferential;
    private long bestT0 = -1;
//    private long bestT02 = -1;
    private long bestT1 = -1;
    private long bestT2 = -1;
    private long bestT3 = -1;
//    private float bestD0 = -1;
    private float bestD1 = -1;
    private float bestD2 = -1;
    private float bestD3 = -1;
    private float highScoreT0 = -1;
    private float highScoreT1 = -1;
    private float highScoreT2 = -1;
    private float highScoreT3 = -1;
    private float highScoreD1 = -1;
    private float highScoreD2 = -1;
    private float highScoreD3 = -1;


    Thread lt = null;

    boolean initialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.FullscreenTheme);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        sharedPref = getSharedPreferences("Runner", MODE_PRIVATE);
        prefEditor = sharedPref.edit();
//        bestTimeFilePath = new File(this.getFilesDir(), "best_time");
//        if(!bestTimeFilePath.exists()) {
//            bestTimeFilePath.mkdir();
//        }
        //bestTimeFilePath.delete(); //Deletes best time on start for testing.

        //Get saved preferences.
        whistleMusicSelected = loadWhistleMusicSelection();
        locationsUnlocked = loadLocationsUnlocked();

        root = (FrameLayout) findViewById(R.id.root);

        // Menus
        mainMenu = (RelativeLayout) findViewById(R.id.mainMenu);
        gameMenu = (FrameLayout) findViewById(R.id.gameMenu);
        gameEndMenu = (RelativeLayout) findViewById(R.id.gameEndMenu);
        pauseMenu = (RelativeLayout) findViewById(R.id.pauseMenu);
        settingsMenu = (RelativeLayout) findViewById(R.id.settingsMenu);

        //Screens
        titleScreen = findViewById(R.id.titleScreen);
        alternateTitleScreen = findViewById(R.id.titleScreenAlternate);
        gameScreen = new GameView(this, windowSize);

//        //Handle menu swipes.   //TODO: Swipe or buttons? The ability to swipe to change locations isn't obvious
//        mainMenu.setOnTouchListener(new OnSwipeTouchListener(this){  //Swipe options.
//            public void onSwipeTop() {
////                Toast.makeText(root.getContext(), "top", Toast.LENGTH_SHORT).show();
//            }
//            public void onSwipeRight() {
////                Toast.makeText(root.getContext(), "right", Toast.LENGTH_SHORT).show();
//                setLocationState(locationState - 1);
//            }
//            public void onSwipeLeft() {
////                Toast.makeText(root.getContext(), "left", Toast.LENGTH_SHORT).show();
//                setLocationState(locationState + 1);
////                else{
////                    Toast.makeText(root.getContext(), "left", Toast.LENGTH_SHORT).show();
////                }
//            }
//            public void onSwipeBottom() {
////                Toast.makeText(root.getContext(), "bottom", Toast.LENGTH_SHORT).show();
//            }
//        });

//        //Background animation.
//        fadeOut = AnimationUtils.loadAnimation(titleScreen.getContext(), R.anim.fade_out);
//        fadeOut.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//            }
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                titleScreen.setBackgroundResource(locationRes);
//                Animation fadeOut = AnimationUtils.loadAnimation(titleScreen.getContext(), R.anim.fade_in);
//                root.startAnimation(fadeOut);
//            }
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//            }
//        });


        root.addView(gameScreen);

        gameScreen.setVisibility(View.VISIBLE);
        gameScreen.setTranslationY(windowSize.y);
        gameScreen.setBackgroundColor(0x00000000); // Wish there was a way to get this from colors.xml

        //Music
        musicMuted = sharedPref.getBoolean("musicMuted", false);


        setGameState(MAIN_MENU);

        //Fonts.
//        font1 = Typeface.createFromAsset(getAssets(), "fonts/minecraft.ttf");
        font2 = Typeface.createFromAsset(getAssets(), "fonts/Pixellari.ttf");
//        font3 = Typeface.createFromAsset(getAssets(), "fonts/3Dventure.ttf");

        //Text Views
        timer = (TextView) findViewById(R.id.timer);
        timer.setTypeface(font2);
//        timer.setTextSize(20f); //TODO: make cooler-looking timer
        loadingText = (TextView) findViewById(R.id.loadingText);
        loadingText.setTypeface(font2);
        endGameHeaderText = (TextView) findViewById(R.id.endGameHeader);
        endGameHeaderText.setTypeface(font2);
        endGameUserTime = (TextView) findViewById(R.id.endGameUserTime);
        endGameUserTime.setTypeface(font2);
        endGameBestTime = (TextView) findViewById(R.id.endGameBestTime);
        endGameBestTime.setTypeface(font2);
        endGameText = (TextView) findViewById(R.id.endGameText);
        endGameText.setTypeface(font2);
        endGameStatNames = (TextView) findViewById(R.id.endGameStatNames);
        endGameStatNames.setTypeface(font2);
        endGameStats = (TextView) findViewById(R.id.endGameStats);
        endGameStats.setTypeface(font2);
        experiencePointsText = (TextView) findViewById(R.id.experiencePoints);
        experiencePointsText.setTypeface(font2);
        menuText = (TextView) findViewById(R.id.menuText);
        menuText.setTypeface(font2);
        titleText = (TextView) findViewById(R.id.textView);
        titleText.setTypeface(font2);
        menuTimeText = (TextView) findViewById(R.id.menuBestTimeText);
        menuTimeText.setTypeface(font2);
        menuDistanceText = (TextView) findViewById(R.id.menuDistanceText);
        menuDistanceText.setTypeface(font2);
        creditsText = (TextView) findViewById(R.id.creditsText);
        creditsText.setTypeface(font2);
        creditsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!musicMuted && sfx != null){
                    sfx.play(crashSound5Id, 1, 1, 0, 0, 1);
                }
                settingsMenu.setVisibility(View.VISIBLE);
                creditsText.setVisibility(View.GONE);
            }
        });

        //Buttons
        playAgainButton = (Button) findViewById(R.id.playAgainButton);
        playAgainButton.setTypeface(font2);
        playAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!musicMuted && sfx != null){
                    sfx.play(crashSound5Id, 1, 1, 0, 0, 1);
                }
                setGameState(GAME_INITIALIZING);
            }
        });

        mainMenuButton = (Button) findViewById(R.id.mainMenuButton);
        mainMenuButton.setTypeface(font2);
        mainMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!musicMuted && sfx != null){
                    sfx.play(crashSound5Id, 1, 1, 0, 0, 1);
                }
                updateLoc(locationState); //TODO: current method to reset menu text, but could probably be done more efficiently.
                setGameState(TRANSIT_TO_MM);
            }
        });


        playButton = (Button) findViewById(R.id.playButton);
        playButton.setTypeface(font2);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!musicMuted && sfx != null){
                    sfx.play(crashSound5Id, 1, 1, 0, 0, 1);
                }
                if(timeTrial2Flag) {
                    timeTrial2Flag = false;
                    bestTime.changeTime(loadBestTime());
                }
                gameScreen.distanceMode = false;
                setGameState(TRANSIT_TO_GAME);
            }
        });

        playMarathonButton = (Button) findViewById(R.id.playDistanceModeButton);
        playMarathonButton.setTypeface(font2);
        playMarathonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!musicMuted && sfx != null){
                    sfx.play(crashSound5Id, 1, 1, 0, 0, 1);
                }
                if(locationState == 0) {
                    timeTrial2Flag = true;
                    bestTime.changeTime(loadBestTime());
                    gameScreen.distanceMode = false;
                } else {
                    timeTrial2Flag = false;
                    gameScreen.distanceMode = true;
                }
                setGameState(TRANSIT_TO_GAME);
            }
        });

        nextLocationButton = (Button) findViewById(R.id.nextLocationButton);
        nextLocationButton.setTypeface(font2);
        nextLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!musicMuted && sfx != null){
                    sfx.play(crashSound5Id, 1, 1, 0, 0, 1);
                }
                setLocationState(locationState + 1);
            }
        });

        previousLocationButton = (Button) findViewById(R.id.previousLocationButton);
        previousLocationButton.setTypeface(font2);
        previousLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!musicMuted && sfx != null){
                    sfx.play(crashSound5Id, 1, 1, 0, 0, 1);
                }
                setLocationState(locationState - 1);
            }
        });

        settingsButton = (Button) findViewById(R.id.settingsButton);
        settingsButton.setTypeface(font2);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                if(!musicMuted && sfx != null){
                    sfx.play(crashSound5Id, 1, 1, 0, 0, 1);
                }
                setGameState(SETTINGS);
//                hideAllMenus();
//                settingsMenu.setVisibility(View.VISIBLE);
            }
        });

        exitSettingsButton = (Button) findViewById(R.id.exitSettingsButton);
        exitSettingsButton.setTypeface(font2);
        exitSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!musicMuted && sfx != null){
                    sfx.play(crashSound5Id, 1, 1, 0, 0, 1);
                }
                setGameState(MAIN_MENU);
//                hideAllMenus();
//                mainMenu.setVisibility(View.VISIBLE);
            }
        });

        tempButton = (Button) findViewById(R.id.tempButton);
        tempButton.setTypeface(font2);
        tempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGameState(PAUSED);
                //TODO: sfx isn't loading in time to play
                if(!musicMuted && sfx != null){
                    sfx.play(crashSound5Id, 1, 1, 0, 0, 1);
                }
            }
        });

        resetButton = (Button) findViewById(R.id.resetButton);
        resetButton.setTypeface(font2);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!musicMuted && sfx != null){
                    sfx.play(crashSound5Id, 1, 1, 0, 0, 1);
                }
                //Reset.
                AlertDialog.Builder resetWarning = new AlertDialog.Builder(root.getContext());//, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                resetWarning.setMessage("Are you sure you want to reset EVERYTHING?");
                resetWarning.setCancelable(true);
                resetWarning.setPositiveButton(
                        "Yes, please.",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if(!musicMuted && sfx != null){
                                    sfx.play(crashSound5Id, 1, 1, 0, 0, 1);
                                }
                                xp = 0; //TODO get rid of this after testing.
                                prefEditor.putInt("xP", xp); //TODO get rid of this after testing.
                                experiencePointsText.setText(String.valueOf(xp) + " XP"); //TODO get rid of this after testing.
                                locationsUnlocked = 0; //TODO get rid of this after testing.

                                for(int i = 0; i < numLocations; i++) {
                                    String sTime = "bestTime" + String.valueOf(i);
                                    String sDist = "bestDistance" + String.valueOf(i);
                                    prefEditor.putLong(sTime, 0L);
                                    prefEditor.putFloat(sDist, 0f);
                                }
                                prefEditor.putLong("bestTime0_2", 0L);
                                prefEditor.commit();
                                Toast.makeText(root.getContext(), "Records reset.", Toast.LENGTH_SHORT).show();//TODO: make sure this can't cause a crash

                                setLocationState(locationState);

                                dialog.dismiss();
                            }
                        });

                resetWarning.setNegativeButton(
                        "Oops, no!",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if(!musicMuted && sfx != null){
                                    sfx.play(crashSound5Id, 1, 1, 0, 0, 1);
                                }
                                dialog.dismiss();
                            }
                        });

                AlertDialog alert11 = resetWarning.create();
                alert11.show();
                hide();
            }
        });

        whistleMusicButton = (ToggleButton) findViewById(R.id.whistleMusicButton);
        whistleMusicButton.setTypeface(font2);
        if(whistleMusicSelected){
            whistleMusicButton.setChecked(true);
        }
        whistleMusicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!musicMuted && sfx != null){
                    sfx.play(crashSound5Id, 1, 1, 0, 0, 1);
                }
                if(whistleMusicButton.isChecked()){
                    whistleMusicSelected = true;
                    setMusicState(R.raw.finger_runner_main_theme_whistling, WHISTLE_MUSIC, true);
                }
                else{
                    whistleMusicSelected = false;
                    setMusicState(R.raw.finger_runner_main_menu, MENU_MUSIC, true);
                }
                saveWhistleMusicSelection(whistleMusicSelected);
            }
        });

//        statsButton = (Button) findViewById(R.id.statsButton);
//        statsButton.setTypeface(font2);
//        statsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //TODO
//            }
//        });

        creditsButton = (Button) findViewById(R.id.creditsButton);
        creditsButton.setTypeface(font2);
        creditsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!musicMuted && sfx != null){
                    sfx.play(crashSound5Id, 1, 1, 0, 0, 1);
                }
                creditsText.setVisibility(View.VISIBLE);
                settingsMenu.setVisibility(View.GONE);
            }
        });

        //Mute music toggle button.
        musicMuteButton = (ToggleButton)this.findViewById(R.id.mute_music_button);
        musicMuteButton.setTypeface(font2);
        if(musicMuted && sfx != null){
            musicMuteButton.setChecked(false);
        }
        musicMuteButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(!musicMuted) {
                    stopMusic();
                    musicMuted = true;
                    //musicPausedByButton = true;
                }
                else {
                    musicMuted = false;
                    if(!musicMuted && sfx != null){
                        sfx.play(crashSound5Id, 1, 1, 0, 0, 1);
                    }
                    setMusicState(R.raw.finger_runner_main_menu, MENU_MUSIC, true);
                    //musicPausedByButton = false;
                }

                //Store mute preference.
                prefEditor.putBoolean("musicMuted", musicMuted);
                prefEditor.commit();
            }
        });

        pauseMMButton = (Button) findViewById(R.id.pauseMMButton);
        pauseMMButton.setTypeface(font2);
        pauseMMButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSFX();
                if(!musicMuted && sfx != null){
                    sfx.play(crashSound5Id, 1, 1, 0, 0, 1);
                }
                setGameState(TRANSIT_TO_MM);
            }
        });

        resumeButton = (Button) findViewById(R.id.resumeButton);
        resumeButton.setTypeface(font2);
        resumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSFX();
                if(!musicMuted && sfx != null){
                    sfx.play(crashSound5Id, 1, 1, 0, 0, 1);
                }
                setGameState(GAME_PLAYING);
            }
        });

//        colWit = loadXP();
        xp = loadXP();
        updateLoc(0); //TODO: probably not necessary, but could save last state to initialize with.
    }

    public void setGameState(int state) {
        if(gameState != state) {
            previousGameState = gameState;
        }
        gameState = state;
        hideAllMenus();

        switch (state) {
            case MAIN_MENU:
                setMenuState();
                break;
            case GAME_INITIALIZING:
                setGameInitState();
                break;
            case GAME_PLAYING:
                setGamePlayingState();
                break;
            case GAME_WON:
                setGameWonState();
                break;
            case GAME_LOST:
                setGameLostState();
                break;
            case PAUSED:
                setPausedState();
                break;
            case TRANSIT_TO_MM:
                setTransitMMState();
                break;
            case TRANSIT_TO_GAME:
                setTransitGameState();
                break;
            case SETTINGS:
                setSettingsState();
                break;
        }
    }

    public void setMenuState() {
        gameScreen.pauseGame();
        setSFX();
        mainMenu.setVisibility(View.VISIBLE);
        //Start menu music.
        setMusicState(R.raw.finger_runner_main_menu, MENU_MUSIC, true);

    }

    public void setGameInitState() {
        gameScreen.resetVariables();
        gameScreen.resumeGame();
        setGameState(GAME_PLAYING);
        //Start music.
//        setMusicState(R.raw.finger_runner_game_music_1, GAME_MUSIC_1, true);
    }

    public void setGamePlayingState() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                releaseSFX();
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(r, 1000);

        //Start music.
        setMusicState(R.raw.finger_runner_game_music_1, GAME_MUSIC_1, true);

        gameMenu.setVisibility(View.VISIBLE);
//        timer.setBackgroundColor(Color.LTGRAY);
        gameScreen.handleTouches = true;
    }

    public void setGameWonState() {
        gameScreen.pauseGame();
        setSFX();
        gameEndMenu.setVisibility(View.VISIBLE);
        root.removeView(gameEndMenu);
        root.addView(gameEndMenu);
        switch(locationState){
            case 0:
//                if(gameScreen.distanceMode) {
//                    endGameHeaderText.setText(getResources().getString(R.string.loc0) + "\nDISTANCE MODE\nRESULTS:");
//                } else {
                    endGameHeaderText.setText("WARM-UP\nTIME TRIAL RESULTS");
//                }
                break;
            case 1:
                if(gameScreen.distanceMode) {
                    endGameHeaderText.setText("RACE TRACK\nDISTANCE MODE RESULTS");
                } else {
                    endGameHeaderText.setText("RACE TRACK\nTIME TRIAL RESULTS");
                }
                break;
            case 2:
                if(gameScreen.distanceMode) {
                    endGameHeaderText.setText("THE HIGHWAY\nDISTANCE MODE RESULTS");
                } else {
                    endGameHeaderText.setText("THE HIGHWAY\nTIME TRIAL RESULTS");
                }
                break;
            case 3:
                if(gameScreen.distanceMode) {
                    endGameHeaderText.setText("TINY TRAFFIC\nDISTANCE MODE RESULTS");
                } else {
                    endGameHeaderText.setText("TINY TRAFFIC\nTIME TRIAL RESULTS");
                }
                break;
        }
//        endGameText.setBackgroundColor(Color.RED);

        // TODO Shrink this or at least separate it into its functional parts
        yourTime = gameScreen.gameTimer;
//        savedTime = loadBestTime();
        long bT = bestTime.getTime();
        long yT = yourTime.getTime();
//        int newXP = (locationState + 1)*(int)((20 * gameScreen.courseDistance) / yT);    //TODO: test this out for gaining time trial xp

        int steps = gameScreen.steps;
        float adjSteps = steps * 0.1f;
        int distance = (int)gameScreen.courseDistance;
        float adjDistance = distance * 0.001f;
        int coins = gameScreen.coins;
        float adjCoins;
        if(gameScreen.yourDistance < distance && !gameScreen.distanceMode){
            adjCoins = coins;
        } else {
            adjCoins = coins * (1 + locationState * 0.75f);
        }
        float adjTime = (yT + 1000*locationState) * 0.001f;
        float denominator = (float)Math.pow((adjSteps + adjTime), 1.5);
        float runScore = 10*((((adjCoins + (adjDistance * adjDistance)) / denominator) * ((1 + adjCoins) / adjSteps) + (adjCoins * (1+locationState) * 0.25f)) * ((1 + locationState) * 0.25f));
        int newXP = (int)(0.1 * runScore);
        if(newXP == 0){
            newXP = 1;
        }

        float highScore = loadHighScoreT();
        if(runScore > highScore){
            highScore = runScore;
            xp += newXP * 1.5f;
            saveHighScoreT(highScore);
            endGameStatNames.setBackgroundResource(R.drawable.rectangle_white_border_yellow);
        } else {
            endGameStatNames.setBackgroundResource(R.drawable.rectangle_white);
        }

        if(bT != 0) {
            //A best time exists.
//            bestTime.changeTime(savedTime); //Put best time into Time class.
            if (yT < bT) {
                //Your time is a new best time.
                endGameUserTime.setBackgroundResource(R.drawable.rectangle_white_border_yellow);
//              //TODO: check if good value
                newXP += (locationState * 2 * newXP);
                xp += newXP;
                endGameStats.setText(String.valueOf(distance) + "\n" + yourTime.getTimeForDisplay() + "\n" + String.valueOf(steps) + "\n" + String.valueOf(coins) + "\n__________\n"
                        + String.format("%.2f", runScore) + "\n(" + String.format("%.2f", highScore) + ")\n" + String.valueOf(newXP));


                saveXP(xp);
                timeDifferential.changeTime(bT - yT);
                saveNewBestTime(yT);
                if(locationsUnlocked < numLocations - 1) {
                    if (xp > (locationsUnlocked + 1) * xpToUnlockEachLvl - 1) { //TODO: figure out how to do location/mode unlocked message
                        locationsUnlocked = xp / xpToUnlockEachLvl;
                        endGameText.setText(getResources().getString(R.string.win1) + "\nYou beat the record by\n" + timeDifferential.getTimeForDisplay() + "!\n\n" + getResources().getString(R.string.location_unlocked));
                        saveLocationsUnlocked(locationsUnlocked);
                    }else {
                        endGameText.setText(getResources().getString(R.string.win1) + "\nYou beat the record by\n" + timeDifferential.getTimeForDisplay() + "!");
                    }
                } else {
                    endGameText.setText(getResources().getString(R.string.win1) + "\nYou beat the record by\n" + timeDifferential.getTimeForDisplay() + "!");
                }
                endGameUserTime.setText("New Best Time:\nPrevious Best:");
                endGameBestTime.setText(yourTime.getTimeForDisplay() + "\n(" + bestTime.getTimeForDisplay() + ")");
                bestTime.changeTime(yT);

                //Start win music.
//                    setMusicState(R.raw.finger_runner_win_new_record, WIN_MUSIC_NEW_RECORD, false);

            } else {
                //Your time isn't a new best time.
                endGameUserTime.setBackgroundResource(R.drawable.rectangle_white);
//              //TODO: check if good value
                newXP += (locationState * newXP);
                xp += newXP;
                endGameStats.setText(String.valueOf(distance) + "\n" + yourTime.getTimeForDisplay() + "\n" + String.valueOf(steps) + "\n" + String.valueOf(coins) + "\n__________\n"
                        + String.format("%.2f", runScore) + "\n(" + String.format("%.2f", highScore) + ")\n" + String.valueOf(newXP));

                if(locationsUnlocked < numLocations - 1) {
                    if (xp > (locationsUnlocked + 1) * xpToUnlockEachLvl - 1) {
                        locationsUnlocked = xp / xpToUnlockEachLvl;
                        endGameText.setText(getResources().getString(R.string.win3) + "\n\n" + getResources().getString(R.string.location_unlocked));
                        saveLocationsUnlocked(locationsUnlocked);
                    }else {
                        endGameText.setText(getResources().getString(R.string.win3));
                    }
                } else {
                    endGameText.setText(getResources().getString(R.string.win3));
                }
                endGameUserTime.setText("Your Run:\nBest Time:");
                endGameBestTime.setText(yourTime.getTimeForDisplay() + "\n(" + bestTime.getTimeForDisplay() + ")");

                //Start win music.
//                    setMusicState(R.raw.finger_runner_win_2, WIN_MUSIC_2, false);
            }
        } else {
            //No best time has been saved (a run has never been completed), so your time is a new best time.
            endGameUserTime.setBackgroundResource(R.drawable.rectangle_white_border_yellow);
//          //TODO: check if good value
            newXP += (locationState * 2 * newXP);
            xp += newXP;
            endGameStats.setText(String.valueOf(distance) + "\n" + yourTime.getTimeForDisplay() + "\n" + String.valueOf(steps) + "\n" + String.valueOf(coins) + "\n__________\n"
                    + String.format("%.2f", runScore) + "\n(" + String.format("%.2f", highScore) + ")\n" + String.valueOf(newXP));


            saveNewBestTime(yT);
            bestTime.changeTime(yT);
            if(locationsUnlocked < numLocations - 1) {
                if (xp > (locationsUnlocked + 1) * xpToUnlockEachLvl - 1) {
                    locationsUnlocked = xp / xpToUnlockEachLvl;
                    endGameText.setText(getResources().getString(R.string.win2) + "\n\n" + getResources().getString(R.string.location_unlocked));
                    saveLocationsUnlocked(locationsUnlocked);
                }else {
                    endGameText.setText(getResources().getString(R.string.win2));
                }
            } else {
                endGameText.setText(getResources().getString(R.string.win2));
            }
            endGameUserTime.setText("New Best Time:\nPrevious Best");
            endGameBestTime.setText(yourTime.getTimeForDisplay() + "\n(-:--:---)");

            //Start win music.
//                setMusicState(R.raw.finger_runner_win_new_record, WIN_MUSIC_NEW_RECORD, false);
        }

        saveXP(xp);
    }

    public void setGameLostState() {
        gameScreen.pauseGame();
        setSFX();
        gameEndMenu.setVisibility(View.VISIBLE);
        root.removeView(gameEndMenu);
        root.addView(gameEndMenu);

        switch(locationState){
            case 0:
//                if(gameScreen.distanceMode) {
//                    endGameHeaderText.setText(getResources().getString(R.string.loc0) + "\nDISTANCE MODE\nRESULTS:");
//                } else {
                endGameHeaderText.setText("WARM-UP\nTIME TRIAL RESULTS");
//                }
                break;
            case 1:
                if(gameScreen.distanceMode) {
                    endGameHeaderText.setText("RACE TRACK\nDISTANCE MODE RESULTS");
                } else {
                    endGameHeaderText.setText("RACE TRACK\nTIME TRIAL RESULTS");
                }
                break;
            case 2:
                if(gameScreen.distanceMode) {
                    endGameHeaderText.setText("THE HIGHWAY\nDISTANCE MODE RESULTS");
                } else {
                    endGameHeaderText.setText("THE HIGHWAY\nTIME TRIAL RESULTS");
                }
                break;
            case 3:
                if(gameScreen.distanceMode) {
                    endGameHeaderText.setText("TINY TRAFFIC\nDISTANCE MODE RESULTS");
                } else {
                    endGameHeaderText.setText("TINY TRAFFIC\nTIME TRIAL RESULTS");
                }
                break;
        }

//        int newXP = gameScreen.collisionsWitnessed; //TODO: test this way of gaining xp for marathon (add distance somehow)

        int yourDistance = (int)gameScreen.yourDistance;
        distanceDifferential = yourDistance - bestDistance;

        yourTime = gameScreen.gameTimer;
        long yT = yourTime.getTime();
        int steps = gameScreen.steps;
        float adjSteps = steps * 0.1f;
        float adjDistance = yourDistance * 0.001f;
        int coins = gameScreen.coins;
        float adjCoins;
        if(yourDistance < (int)gameScreen.courseDistance && !gameScreen.distanceMode){
            adjCoins = coins;
        } else {
            adjCoins = coins * (1 + locationState * 0.75f);
        }
        float adjTime = (yT + 1000*locationState) * 0.001f;
        float denominator = (float)Math.pow(adjSteps + adjTime, 1.5);
        float runScore = 10*((((adjCoins + (adjDistance * adjDistance)) / denominator) * ((1 + adjCoins) / adjSteps) + (adjCoins * (1+locationState) * 0.25f)) * ((1 + locationState) * 0.25f));
        int newXP = (int)(0.1 * runScore);


        //Set end game textviews for losing.
        if(!gameScreen.distanceMode) {
            endGameUserTime.setBackgroundResource(R.drawable.rectangle_white_border_red);
            float highScore = loadHighScoreT();
            //TODO: make sure can't be higher if you don't finish
            if(runScore > highScore){
                highScore = runScore;
                xp += newXP * 1.5f;
                saveHighScoreT(highScore);
                endGameStatNames.setBackgroundResource(R.drawable.rectangle_white_border_yellow);
            } else {
                endGameStatNames.setBackgroundResource(R.drawable.rectangle_white);
            }
            endGameStats.setText(String.valueOf(yourDistance) + "\n" + yourTime.getTimeForDisplay() + "\n" + String.valueOf(steps) + "\n" + String.valueOf(coins) + "\n__________\n"
                    + String.format("%.2f", runScore) + "\n(" + String.format("%.2f", highScore) + ")\n" + String.valueOf(newXP));

            xp += newXP;

            long bT = bestTime.getTime();

            //Start lose music.
//        if(nowPlaying != LOSE_MUSIC_1) {
//            setMusicState(R.raw.finger_runner_lose_1, LOSE_MUSIC_1, false);
//        }
            endGameUserTime.setText("Your Run:\nBest Time");
//            savedTime = loadBestTime();
            if (bT != 0) {
                //There was a saved best time.
//                bestTime.changeTime(savedTime);
                endGameBestTime.setText("DQed\n(" + bestTime.getTimeForDisplay() + ")");
                if(locationsUnlocked < numLocations - 1) {
                    if (xp > (locationsUnlocked + 1) * xpToUnlockEachLvl - 1) {
                        locationsUnlocked = xp / xpToUnlockEachLvl;
                        endGameText.setText(getResources().getString(R.string.lose2) + "\n\n" + getResources().getString(R.string.location_unlocked));
                        saveLocationsUnlocked(locationsUnlocked);
                    }else {
                        endGameText.setText(getResources().getString(R.string.lose2));
                    }
                } else {
                    endGameText.setText(getResources().getString(R.string.lose2));
                }
            } else {
                //The loaded best time was null (there has never been a best time saved).
                endGameBestTime.setText("DQed\n(-:--:---)");
                if(locationsUnlocked < numLocations - 1) {
                    if (xp > (locationsUnlocked + 1) * xpToUnlockEachLvl - 1) {
                        locationsUnlocked = xp / xpToUnlockEachLvl;
                        endGameText.setText(getResources().getString(R.string.lose1) + "\n\n" + getResources().getString(R.string.location_unlocked));
                        saveLocationsUnlocked(locationsUnlocked);
                    }else {
                        endGameText.setText(getResources().getString(R.string.lose1));
                    }
                } else {
                    endGameText.setText(getResources().getString(R.string.lose1));
                }
            }
        } else{
            float highScore = loadHighScoreD();
            if(runScore > highScore){
                highScore = runScore;
                xp += newXP * 1.5f;
                saveHighScoreD(highScore);
                endGameStatNames.setBackgroundResource(R.drawable.rectangle_white_border_yellow);
            } else {
                endGameStatNames.setBackgroundResource(R.drawable.rectangle_white);
            }

            endGameStats.setText(String.valueOf(yourDistance) + "\n" + yourTime.getTimeForDisplay() + "\n" + String.valueOf(steps) + "\n" + String.valueOf(coins) + "\n__________\n"
                    + String.format("%.2f", runScore) + "\n(" + String.format("%.2f", highScore) + ")\n" + String.valueOf(newXP));
//            bestDistance = loadBestDistance();

            if(distanceDifferential > 0){
                //New record!
                endGameUserTime.setBackgroundResource(R.drawable.rectangle_white_border_yellow);
                newXP *= 1.5;   //TODO: check if good value
                xp += newXP;
                endGameUserTime.setText("New Long Run:\nPrevious Best");
                endGameBestTime.setText(String.valueOf(yourDistance) + "\n(" + String.format("%.0f", bestDistance) + ")");
                if(locationsUnlocked < numLocations - 1) {
                    if (xp > (locationsUnlocked + 1) * xpToUnlockEachLvl - 1) {
                        locationsUnlocked = xp / xpToUnlockEachLvl - 1;
                        endGameText.setText(getResources().getString(R.string.win1) + "\nYou beat the record by\n" + String.valueOf(distanceDifferential) + "!\n\n" + getResources().getString(R.string.location_unlocked));
                        saveLocationsUnlocked(locationsUnlocked);
                    }else {
                        endGameText.setText(getResources().getString(R.string.win1) + "\nYou beat the record by\n" + String.valueOf(distanceDifferential) + "!");
                    }
                } else {
                    endGameText.setText(getResources().getString(R.string.win1) + "\nYou beat the record by\n" + String.valueOf(distanceDifferential) + "!");
                }
//                setMusicState(R.raw.finger_runner_win_new_record, WIN_MUSIC_NEW_RECORD, false);
                //Keep game music going.
                setMusicState(R.raw.finger_runner_game_music_1, GAME_MUSIC_1, true);

                bestDistance = yourDistance;
                saveBestDistance(bestDistance);
            } else {
                //Not a record.
                endGameUserTime.setBackgroundResource(R.drawable.rectangle_white);
                xp += newXP;
                endGameUserTime.setText("Your Run:\nLongest Run");
                endGameBestTime.setText(String.valueOf(yourDistance) + "\n(" + String.format("%.0f", bestDistance) + ")");
                if(locationsUnlocked < numLocations - 1) {
                    if (xp > (locationsUnlocked + 1) * xpToUnlockEachLvl - 1) {
                        locationsUnlocked = xp / xpToUnlockEachLvl;
                        endGameText.setText(getResources().getString(R.string.lose2) + "\n\n" + getResources().getString(R.string.location_unlocked));
                        saveLocationsUnlocked(locationsUnlocked);
                    }else {
                        endGameText.setText(getResources().getString(R.string.lose2));
                    }
                } else {
                    endGameText.setText(getResources().getString(R.string.lose2));
                }
                //Keep game music going.
                setMusicState(R.raw.finger_runner_game_music_1, GAME_MUSIC_1, true);
            }
        }

        saveXP(xp);
    }

    // Game paused. Not app paused.
    public void setPausedState() {
        gameScreen.pauseGame();
        setSFX();

        if(activeMusic != null) {
            if (activeMusic.isPlaying()) {
                activeMusic.pause();
            }
        }

        pauseMenu.setVisibility(View.VISIBLE);

        root.removeView(pauseMenu);
        root.addView(pauseMenu);
    }

    public void setTransitMMState() {
        //Have music fade out?
//        fadeoutMusic();

        gameScreen.animate()
                .translationY(windowSize.y)
                .setDuration(1000);
        titleScreen.animate()
                .translationY(0)//windowSize.y)
                .setDuration(1000)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        setGameState(MAIN_MENU);
                    }
                });
    }

    public void setTransitGameState() {
        //Have music fade out.
//        fadeoutMusic();


        //TODO: show it's loading
        loadingText.setVisibility(View.VISIBLE);

        lt = new Thread() {
            @Override
            public void run() {
                gameScreen.resetVariables();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingText.setVisibility(View.GONE);
                        gameScreen.animate()
                                .translationY(0)//-windowSize.y)
                                .setDuration(1000);

                        titleScreen.animate()
                                .translationY(-windowSize.y)
                                .setDuration(1000)
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        setGameState(GAME_INITIALIZING);
                                    }
                                });

                        //Make button and timer visible.
                        root.removeView(gameMenu);
                        root.addView(gameMenu);
                    }
                });
            }
        };
        lt.start();
//
//        gameScreen.animate()
//                .translationY(0)//-windowSize.y)
//                .setDuration(1000);
//        Update gameScreen to look reset.
//        gameScreen.resetVariables();
//        titleScreen.animate()
//                .translationY(-windowSize.y)
//                .setDuration(1000)
//                .withEndAction(new Runnable() {
//                    @Override
//                    public void run() {
//                        setGameState(GAME_INITIALIZING);
//                    }
//                });
//
//        //Make button and timer visible.
//        root.removeView(gameMenu);
//        root.addView(gameMenu);
    }

    public void setSettingsState() {
        hideAllMenus();
        creditsText.setVisibility(View.GONE);
        settingsMenu.setVisibility(View.VISIBLE);
        if(xp > 999){//TODO: arbitrary
            whistleMusicButton.setEnabled(true);
        }
        setMusicState(R.raw.finger_runner_main_menu, MENU_MUSIC, true);
    }

    // id should be from R.raw
    public void setMusicState(int id, int musicState, final boolean loop) {
        if(musicState == MENU_MUSIC || musicState == GAME_MUSIC_1) {
            if (whistleMusicSelected) {
                musicState = WHISTLE_MUSIC;
                id = R.raw.finger_runner_main_theme_whistling;
            }
        }
        if(musicState == nowPlaying) {
            if (activeMusic != null) {
                if (!musicMuted) {
                    if (!activeMusic.isPlaying()) {
                        activeMusic.start();
                    }
                }
                return;
            }
        }
//        if(musicThread != null){
//            if(musicThread.isAlive()){
//                try {
//                    musicThread.join();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
        if(!musicPausedByLeavingApp) {
            final int finalId = id;
            final int finalMusicState = musicState;
//            musicThread = new Thread(new Runnable() {
//                @Override
//                public void run() {
                    stopMusic();

                    activeMusic = MediaPlayer.create(root.getContext(), finalId);
                    activeMusic.setLooping(loop);
                    //If music was killed by leaving the app, resume at previous position.
                    if (finalMusicState == nowPlaying && activeMusicPosition > 0) {
                        activeMusic.seekTo(activeMusicPosition);
                    }
                    activeMusicPosition = 0;
                    if (!musicMuted) {
                        activeMusic.start();
                    }

                    nowPlaying = finalMusicState;
//                }
//            });
//            musicThread.start();
        } else {
            musicPausedByLeavingApp = false;
        }
    }

    public void stopMusic() {
        if(activeMusic != null) {
            if (activeMusic.isPlaying()) {
                activeMusic.stop();
            }
            activeMusic.reset();
            activeMusic.release();
            activeMusic = null;
        }
    }

//    public void fadeoutMusic(){
//        if(activeMusic != null) {
//            musicThread = new Thread((new Runnable() {
//                @Override
//                public void run() {
//                    float volume;
//                    for(int i = 100; i >= 0; i--){
//                        volume = i / 100f;
//                        final float v = volume;
//                        handler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                if(activeMusic != null) {
//                                    activeMusic.setVolume(v, v);
//                                }
//                            }
//                        });
//                        try {
//                            musicThread.sleep(5);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }));
//            musicThread.start();
//        }
//    }

    public void setSFX() {
        if (sfx == null) {
//        soundEffects = new SoundPool.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build();
            sfx = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
//            badSoundId = sfx.load(this, R.raw.finger_runner_bad_sound, 1);
//            goodSoundId = sfx.load(this, R.raw.finger_runner_good_sound, 1);
//            crashSound2Id = sfx.load(this, R.raw.finger_runner_crash_sound_2, 1);
//            crashSound3Id = sfx.load(this, R.raw.finger_runner_crash_sound_3, 1);
//            crashSound4Id = sfx.load(this, R.raw.finger_runner_crash_sound_4, 1);
            crashSound5Id = sfx.load(this, R.raw.finger_runner_crash_sound_5, 1);
//            truckHornId = sfx.load(this, R.raw.finger_runner_bad_truck_horn, 1);
//            carHornId = sfx.load(this, R.raw.finger_runner_bad_car_horn, 1);
//            wilhelmScreamId = sfx.load(this, R.raw.wilhelm_scream, 1);
        }
    }

    public void releaseSFX(){
        if(sfx != null) {
            sfx.release();
            sfx = null;
        }
    }

    private void hideAllMenus() {
        mainMenu.setVisibility(View.GONE);
        gameMenu.setVisibility(View.GONE);
        settingsMenu.setVisibility(View.GONE);
        gameEndMenu.setVisibility(View.GONE);
        pauseMenu.setVisibility(View.GONE);
    }


    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        //mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        root.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }


    /**
     *Actions to take when app is moved to background.
     */
    @Override
    protected void onPause(){
        super.onPause();
        if(gameState == GAME_PLAYING){
            setGameState(PAUSED);
        }

        gameScreen.pause();

        //Set so music won't start playing when resuming to paused state, or playing after paused during a transition.
        if(gameState == PAUSED || gameState == TRANSIT_TO_GAME || gameState == TRANSIT_TO_MM){
            musicPausedByLeavingApp = true;
        }

        //If music is playing, stop upon leaving the app.
        if(activeMusic != null) {
            if (activeMusic.isPlaying()) {
                activeMusic.pause();
            }
            activeMusicPosition = activeMusic.getCurrentPosition();
        }
//        if(musicThread != null) {
//            musicThread.interrupt();
//            musicThread = null;
//        }
        stopMusic();
        releaseSFX();
    }

    /**
     *Actions to take when app is resumed.
     */
    @Override
    protected void onResume(){
        super.onResume();
        hide();


        gameScreen.resume();

        if(gameState == PAUSED){
            //Game_Playing must be set first or else a blue screen appears.
            setGameState(GAME_PLAYING);
//
            // TODO: Resuming to PAUSED state doesn't seem to work unless it's slightly delayed, so currently it's set manually so it happens quicker.
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    setGameState(PAUSED);
//                }
//            }, 300);
            //Manually set paused state.
            gameMenu.setVisibility(View.INVISIBLE);
            gameScreen.pauseGame();
            setSFX();
            pauseMenu.setVisibility(View.VISIBLE);
            root.removeView(pauseMenu);
            root.addView(pauseMenu);

        } else if (gameState == GAME_LOST || gameState == GAME_WON) {
            gameScreen.pauseGame();
            setSFX();
            gameEndMenu.setVisibility(View.VISIBLE);
            root.removeView(gameEndMenu);
            root.addView(gameEndMenu);
        } else {
            setSFX();
            setGameState(gameState);//setGameState(previousGameState);
        }
    }

    private void updateLoc(int loc) {
//        int prevLoc = locationState;
        locationState = loc;
        experiencePointsText.setText(String.valueOf(xp) + " Runner Points (RP)");
        if (loc < numLocations && loc > -1) {
            switch (loc) {
                case 0:
                    timeTrial2Flag = false;

                    //TODO: display xp somewhere on menu
                    menuText.setText(getResources().getString(R.string.loc0));
                    bestTime.changeTime(loadBestTime());
                    playButton.setEnabled(true);
//                    playButton.setText(getResources().getString(R.string.time_trial));
                    playMarathonButton.setVisibility(View.GONE);
                    menuDistanceText.setText("");

//                    Time bestTime2 = new Time();
                    if (bestTime.getTime() == 0L) {
                        menuTimeText.setText("-:--:---");
//                        playMarathonButton.setEnabled(false);
//                        menuDistanceText.setText("LOCKED: You must complete Time Trial 1 in under 0:10:000.");
                    }
//                    else if (bestTime.getSeconds() > 9){
//                        menuTimeText.setText(bestTime.getTimeForDisplay());
//                        playMarathonButton.setEnabled(false);
//                        menuDistanceText.setText("LOCKED: You must complete Time Trial 1 in under 0:10:000.");
//                    }
                    else {
//                        playMarathonButton.setEnabled(true);
                        menuTimeText.setText("Best Time: " + bestTime.getTimeForDisplay());//"Best Time: " + bestTime.getTimeForDisplay());
//
//                        timeTrial2Flag = true;
//                        bestTime2.changeTime(loadBestTime());
//                        timeTrial2Flag = false;
//                        if(bestTime2.getTime() == 0L){
//                            menuDistanceText.setText("-:--:---");
//                        } else {
//                            menuDistanceText.setText(bestTime2.getTimeForDisplay());
//                        }
                    }
//                    playMarathonButton.setText("Time Trial 2");

                    previousLocationButton.setVisibility(View.INVISIBLE);    //For the first location, this button should not be seen.
                    nextLocationButton.setVisibility(View.VISIBLE);    //For the last location, this button should not be seen.
                    //titleScreen.startAnimation(fadeOut);
                    break;
                case 1:
                    timeTrial2Flag = false;

                    //TODO: display xp somewhere on menu
                    menuText.setText(getResources().getString(R.string.loc1));
//                    playButton.setText(getResources().getString(R.string.time_trial));
//                    playMarathonButton.setText(getResources().getString(R.string.marathon));
                    playMarathonButton.setVisibility(View.VISIBLE);

                    if (locationsUnlocked > 0) {
                        playButton.setEnabled(true);
                        bestTime.changeTime(loadBestTime());

                        if (bestTime.getTime() == 0L) {
                            menuTimeText.setText("-:--:---");
                            playMarathonButton.setEnabled(false);
                            menuDistanceText.setText(getResources().getString(R.string.loc1MarathonLocked));
                        } else if (bestTime.getSeconds() > 29) {
                            menuTimeText.setText("Best Time: " + bestTime.getTimeForDisplay());
                            playMarathonButton.setEnabled(false);
                            menuDistanceText.setText(getResources().getString(R.string.loc1MarathonLocked));
                        } else {
                            menuTimeText.setText("Best Time: " + bestTime.getTimeForDisplay());
                            playMarathonButton.setEnabled(true);
                            bestDistance = loadBestDistance();
                            menuDistanceText.setText("Farthest Run: " + String.valueOf(bestDistance)); //TODO: format
                        }

                    } else {
                        playButton.setEnabled(false);
                        playMarathonButton.setEnabled(false);
                        menuTimeText.setText("LOCKED: 100 RP required.");
                        menuDistanceText.setText(getResources().getString(R.string.loc1MarathonLocked));
                    }
                    previousLocationButton.setVisibility(View.VISIBLE);
                    nextLocationButton.setVisibility(View.VISIBLE);
                    //titleScreen.startAnimation(fadeOut);
                    break;
                case 2:
                    timeTrial2Flag = false;
                    //TODO: display xp somewhere on menu
                    menuText.setText(getResources().getString(R.string.loc2));
//                    playButton.setText(getResources().getString(R.string.time_trial));
//                    playMarathonButton.setText(getResources().getString(R.string.marathon));

                    if (locationsUnlocked > 1) {
                        playButton.setEnabled(true);
                        bestTime.changeTime(loadBestTime());

                        if (bestTime.getTime() == 0L) {
                            menuTimeText.setText("-:--:---");
                            playMarathonButton.setEnabled(false);
                            menuDistanceText.setText(getResources().getString(R.string.loc2MarathonLocked));
                        } else if (bestTime.getSeconds() > 24) {
                            menuTimeText.setText("Best Time: " + bestTime.getTimeForDisplay());
                            playMarathonButton.setEnabled(false);
                            menuDistanceText.setText(getResources().getString(R.string.loc2MarathonLocked));
                        } else {
                            menuTimeText.setText("Best Time: " + bestTime.getTimeForDisplay());
                            playMarathonButton.setEnabled(true);
                            bestDistance = loadBestDistance();
                            menuDistanceText.setText("Farthest Run: " + String.valueOf(bestDistance)); //TODO: format
                        }

                    } else {
                        playButton.setEnabled(false);
                        playMarathonButton.setEnabled(false);
                        menuTimeText.setText("LOCKED: 200 RP required.");
                        menuDistanceText.setText(getResources().getString(R.string.loc2MarathonLocked));
                    }
                    previousLocationButton.setVisibility(View.VISIBLE);
                    nextLocationButton.setVisibility(View.VISIBLE);    //For the last location, this button should not be seen.
                    break;
                case 3:
                    timeTrial2Flag = false;
                    //TODO: display xp somewhere on menu
                    menuText.setText(getResources().getString(R.string.loc3));
//                    playButton.setText(getResources().getString(R.string.time_trial));
//                    playMarathonButton.setText(getResources().getString(R.string.marathon));

                    if (locationsUnlocked > 2) {
                        playButton.setEnabled(true);
                        bestTime.changeTime(loadBestTime());

                        if (bestTime.getTime() == 0L) {
                            menuTimeText.setText("-:--:---");
                            playMarathonButton.setEnabled(false);
                            menuDistanceText.setText(getResources().getString(R.string.loc3MarathonLocked));
                        } else if (bestTime.getSeconds() > 24) {
                            menuTimeText.setText("Best Time: " + bestTime.getTimeForDisplay());
                            playMarathonButton.setEnabled(false);
                            menuDistanceText.setText(getResources().getString(R.string.loc3MarathonLocked));
                        } else {
                            menuTimeText.setText("Best Time: " + bestTime.getTimeForDisplay());
                            playMarathonButton.setEnabled(true);
                            bestDistance = loadBestDistance();
                            menuDistanceText.setText("Farthest Run: " + String.valueOf(bestDistance)); //TODO: format
                        }

                    } else {
                        playButton.setEnabled(false);
                        playMarathonButton.setEnabled(false);
                        menuTimeText.setText("LOCKED: 300 RP required.");
                        menuDistanceText.setText(getResources().getString(R.string.loc3MarathonLocked));
                    }
                    previousLocationButton.setVisibility(View.VISIBLE);
                    nextLocationButton.setVisibility(View.INVISIBLE);    //For the last location, this button should not be seen.
                    break;
            }
        }
    }

    public void setLocationState(int loc) {
        int prevLoc = locationState;
        switch (loc){
            case 0:
                locationRes = R.mipmap.background_0;
                break;
            case 1:
                locationRes = R.drawable.cone_xxhdpi;
                break;
            case 2:
                locationRes = R.drawable.right_foot_yellow_xxhdpi;
                break;
            case 3:
                locationRes = R.drawable.red_bug_xxhdpi;
                break;
        }
        if (loc < prevLoc) {
            alternateTitleScreen = findViewById(R.id.titleScreen);
            alternateTitleScreen.setBackground(titleScreen.getBackground());
            alternateTitleScreen.setTranslationX(0);
            titleScreen = findViewById(R.id.titleScreenAlternate);
            titleScreen.setTranslationX(-windowSize.x);
            titleScreen.setBackgroundResource(locationRes);
            titleScreen.setVisibility(View.VISIBLE);
            mainMenu.setVisibility(View.INVISIBLE);

            alternateTitleScreen.animate()
                    .translationX(windowSize.x)
                    .setDuration(500);

            updateLoc(loc);
//            lt = new Thread() {
//                @Override
//                public void run() {
//                    gameScreen.resetVariables();
////                    runOnUiThread(new Runnable() {
////                        @Override
////                        public void run() {
////                            mainMenu.setVisibility(View.VISIBLE);
////                        }
////                    });
//                }
//            };
//            lt.start();

            titleScreen.animate()
                    .translationX(0)
                    .setDuration(500)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
//                                gameScreen.resetVariables();
                            mainMenu.setVisibility(View.VISIBLE);
                        }
                    });
        } else if (loc > prevLoc) {
            alternateTitleScreen = findViewById(R.id.titleScreen);
            alternateTitleScreen.setBackground(titleScreen.getBackground());
            alternateTitleScreen.setTranslationX(0);
            titleScreen = findViewById(R.id.titleScreenAlternate);
            titleScreen.setBackgroundResource(locationRes);
            titleScreen.setTranslationX(windowSize.x);
            titleScreen.setVisibility(View.VISIBLE);
            mainMenu.setVisibility(View.INVISIBLE);

//                Thread t = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        gameScreen.resetVariables();
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                mainMenu.setVisibility(View.VISIBLE);
//                            }
//                        });
//                    }
//                });
//                t.start();

            alternateTitleScreen.animate()
                    .translationX(-windowSize.x)
                    .setDuration(500);
            updateLoc(loc);
            titleScreen.animate()
                    .translationX(0)
                    .setDuration(500)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
//                                gameScreen.resetVariables();
                            mainMenu.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }


    private void saveNewBestTime(long newBestTime){
        switch (locationState){
            case 0:
//                if(timeTrial2Flag){
//                    bestT02 = newBestTime;
//                } else {
                    bestT0 = newBestTime;
//                }
                break;
            case 1:
                bestT1 = newBestTime;
                break;
            case 2:
                bestT2 = newBestTime;
                break;
            case 3:
                bestT3 = newBestTime;
                break;
        }
        String s;
        if(timeTrial2Flag){
            s = "bestTime" + String.valueOf(locationState) + "_2";
        } else {
            s = "bestTime" + String.valueOf(locationState);
        }
        prefEditor.putLong(s, newBestTime);
        prefEditor.commit();
//        try {
//            FileOutputStream fileOut = new FileOutputStream(bestTimeFilePath);
//            ObjectOutputStream out = new ObjectOutputStream(fileOut);
//            out.writeObject(newBestTime);
//            out.close();
//            fileOut.close();
//        } catch (IOException i) {
//            i.printStackTrace();
//        }
    }

    private Long loadBestTime (){
        long time = -1;
        switch (locationState){
            case 0:
//                if(timeTrial2Flag){
//                    time = bestT02;
//                } else {
                    time = bestT0;
//                }
                break;
            case 1:
                time = bestT1;
                break;
            case 2:
                time = bestT2;
                break;
            case 3:
                time = bestT3;
                break;
        }

        if(time == -1) {
            String s;
            if (timeTrial2Flag) {
                s = "bestTime" + String.valueOf(locationState) + "_2";
            } else {
                s = "bestTime" + String.valueOf(locationState);
            }
            time = sharedPref.getLong(s, 0L);
        }

//        try {
//            FileInputStream fileIn = new FileInputStream(bestTimeFilePath);
//            ObjectInputStream in = new ObjectInputStream(fileIn);
//            time = (long) in.readObject();
//            in.close();
//            fileIn.close();
//        } catch (IOException | ClassNotFoundException i){
//            i.printStackTrace();
//        }
        return time;
    }

    private void saveBestDistance(float distance){
        switch (locationState){
            case 0:
                //no distance mode
                break;
            case 1:
                bestD1 = distance;
                break;
            case 2:
                bestD2 = distance;
                break;
            case 3:
                bestD3 = distance;
                break;
        }
        String s = "bestDistance" + String.valueOf(locationState);
        prefEditor.putFloat(s, distance);
        prefEditor.commit();
    }

    private float loadBestDistance(){
        float distance = -1;
        switch (locationState){
            case 0:
                //no distance mode
                break;
            case 1:
                distance = bestD1;
                break;
            case 2:
                distance = bestD2;
                break;
            case 3:
                distance = bestD3;
                break;
        }
        if(distance == -1) {
            String s = "bestDistance" + String.valueOf(locationState);
            distance = sharedPref.getFloat(s, 0f);
        }
        return distance;
    }

    private void saveHighScoreT(float score) {
        switch (locationState) {
            case 0:
                highScoreT0 = score;
                break;
            case 1:
                highScoreT1 = score;
                break;
            case 2:
                highScoreT2 = score;
                break;
            case 3:
                highScoreT3 = score;
                break;
        }
        String s;
        s = "highScoreTMode" + String.valueOf(locationState);
        prefEditor.putFloat(s, score);

        prefEditor.commit();
    }

    private float loadHighScoreT(){
        float score = -1;
        switch (locationState){
            case 0:
                score = highScoreT0;
                break;
            case 1:
                score = highScoreT1;
                break;
            case 2:
                score = highScoreT2;
                break;
            case 3:
                score = highScoreT3;
                break;
        }
        if(score == -1) {
            String s = "highScoreTMode" + String.valueOf(locationState);
            score = sharedPref.getFloat(s, 0f);
        }
        return score;
    }

    private void saveHighScoreD(float score){
        switch (locationState){
            case 0:
                //no distance mode
                break;
            case 1:
                highScoreD1 = score;
                break;
            case 2:
                highScoreD2 = score;
                break;
            case 3:
                highScoreD3 = score;
                break;
        }
        String s = "highScoreDMode" + String.valueOf(locationState);
        prefEditor.putFloat(s, score);
        prefEditor.commit();
    }

    private float loadHighScoreD(){
        float score = -1;
        switch (locationState){
            case 0:
                //No distance mode;
                break;
            case 1:
                score = highScoreD1;
                break;
            case 2:
                score = highScoreD2;
                break;
            case 3:
                score = highScoreD3;
                break;
        }
        if(score == -1) {
            String s = "highScoreDMode" + String.valueOf(locationState);
            score = sharedPref.getFloat(s, 0f);
        }
        return score;
    }

    private void saveXP(int xP){
        prefEditor.putInt("xP", xP);
        prefEditor.commit();
    }

    private int loadXP(){
        int c = sharedPref.getInt("xP", 0);
        return c;
    }

    private void saveWhistleMusicSelection(boolean b){
        prefEditor.putBoolean("whistleMusic", b);
        prefEditor.commit();
    }

    private boolean loadWhistleMusicSelection(){
        boolean b = sharedPref.getBoolean("whistleMusic", false);
        return b;
    }

    private void saveLocationsUnlocked(int loc){
        prefEditor.putInt("locationsUnlocked", loc);
        prefEditor.commit();
    }

    private int loadLocationsUnlocked(){
        int loc = sharedPref.getInt("locationsUnlocked", 0);
        return loc;
    }

//    //TODO: testing
//    //Bitmap processing
//    // http://stackoverflow.com/questions/17839388/creating-a-scaled-bitmap-with-createscaledbitmap-in-android#17839663
//    // https://developer.android.com/training/displaying-bitmaps/load-bitmap.html
//    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
//        // Raw height and width of image
//        final int height = options.outHeight;
//        final int width = options.outWidth;
//        int inSampleSize = 1;
//
//        // If image is larger in either dimension than the requested dimension
//        if(height > reqHeight || width > reqWidth) {
//
//            final int halfHeight = height / 2;
//            final int halfWidth = width / 2;
//
//            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
//            // height and width larger than the requested height and width
//            while((halfHeight / inSampleSize) >= reqHeight
//                    && (halfWidth / inSampleSize) <= reqWidth) {
//                inSampleSize *=2;
//            }
//        }
//
//        return inSampleSize;
//    }
//
//    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
//                                                         int reqWidth, int reqHeight) {
//
//        // First decode with inJustDecodeBounds=true to check dimensions
//        final BitmapFactory.Options options = new BitmapFactory.Options();
//
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeResource(res, resId, options);
//
//        // Calculate inSampleSize
//        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
//
//        // Decode bitmap with inSampleSize set
//        options.inJustDecodeBounds = false;
//        return BitmapFactory.decodeResource(res, resId, options);
//    }

}
