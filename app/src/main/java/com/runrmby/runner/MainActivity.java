package com.runrmby.runner;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.MediaPlayer;
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
    Handler handler = new Handler();
    Thread musicThread;
    private boolean whistleMusicSelected = false;

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

    private TextView endGameUserTime;
    private TextView endGameBestTime;
    private TextView endGameText;
    private TextView menuText;
    public TextView timer;
    public TextView experiencePointsText;
    private TextView titleText;
    private TextView creditsText;
    private TextView menuTimeText;
    private TextView menuDistanceText;

    public Typeface font1;
    public Typeface font2;
    public Typeface font3;
    public Typeface font4;

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
    private static int numLocations = 3;
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
        endGameUserTime = (TextView) findViewById(R.id.endGameUserTime);
        endGameUserTime.setTypeface(font2);
        endGameBestTime = (TextView) findViewById(R.id.endGameBestTime);
        endGameBestTime.setTypeface(font2);
        endGameText = (TextView) findViewById(R.id.endGameText);
        endGameText.setTypeface(font2);
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
                setGameState(GAME_INITIALIZING);
            }
        });

        mainMenuButton = (Button) findViewById(R.id.mainMenuButton);
        mainMenuButton.setTypeface(font2);
        mainMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLocationState(locationState); //TODO: current method to reset menu text, but could probably be done more efficiently.
                setGameState(TRANSIT_TO_MM);
            }
        });


        playButton = (Button) findViewById(R.id.playButton);
        playButton.setTypeface(font2);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeTrial2Flag = false;
                gameScreen.distanceMode = false;
                setGameState(TRANSIT_TO_GAME);
            }
        });

        playMarathonButton = (Button) findViewById(R.id.playDistanceModeButton);
        playMarathonButton.setTypeface(font2);
        playMarathonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(locationState == 0) {
                    timeTrial2Flag = true;
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
                setLocationState(locationState + 1);
            }
        });

        previousLocationButton = (Button) findViewById(R.id.previousLocationButton);
        previousLocationButton.setTypeface(font2);
        previousLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLocationState(locationState - 1);
            }
        });

        settingsButton = (Button) findViewById(R.id.settingsButton);
        settingsButton.setTypeface(font2);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
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
            }
        });

        resetButton = (Button) findViewById(R.id.resetButton);
        resetButton.setTypeface(font2);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Reset best time.
                AlertDialog.Builder resetWarning = new AlertDialog.Builder(root.getContext());//, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                resetWarning.setMessage("Are you sure you want to reset EVERYTHING?");
                resetWarning.setCancelable(true);
                resetWarning.setPositiveButton(
                        "Yes, please.",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

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
                creditsText.setVisibility(View.VISIBLE);
                settingsMenu.setVisibility(View.GONE);
            }
        });

        //Mute music toggle button.
        musicMuteButton = (ToggleButton)this.findViewById(R.id.mute_music_button);
        musicMuteButton.setTypeface(font2);
        if(musicMuted){
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
                setGameState(TRANSIT_TO_MM);
            }
        });

        resumeButton = (Button) findViewById(R.id.resumeButton);
        resumeButton.setTypeface(font2);
        resumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGameState(GAME_PLAYING);
            }
        });

//        colWit = loadXP();
        xp = loadXP();
        setLocationState(0); //TODO: probably not necessary, but could save last state to initialize with.
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
        //Start music.
        setMusicState(R.raw.finger_runner_game_music_1, GAME_MUSIC_1, true);

        gameMenu.setVisibility(View.VISIBLE);
        timer.setBackgroundColor(Color.LTGRAY);
        gameScreen.handleTouches = true;
    }

    public void setGameWonState() {
        gameScreen.pauseGame();
        gameEndMenu.setVisibility(View.VISIBLE);
        root.removeView(gameEndMenu);
        root.addView(gameEndMenu);
        endGameText.setBackgroundColor(Color.RED);

//        //TODO: pause menu buttons were unresponsive after a while, so checking click listeners here.
//        if(!playAgainButton.hasOnClickListeners()) {
//            System.out.println("-----------------Play again button click listener had to be reset!------------");
//            playAgainButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    setGameState(GAME_INITIALIZING);
//                }
//            });
//        }
//        if(!mainMenuButton.hasOnClickListeners()) {
//            System.out.println("-----------------Quit button click listener had to be reset!------------");
//            mainMenuButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    setGameState(TRANSIT_TO_MM);
//                }
//            });
//        }

        // TODO Shrink this or at least separate it into its functional parts
        yourTime = gameScreen.gameTimer;
//        savedTime = loadBestTime();
        long bT = bestTime.getTime();
        long yT = yourTime.getTime();
        int newXP = (locationState + 1)*(int)((20 * gameScreen.courseDistance) / yT);    //TODO: test this out for gaining time trial xp

        if(bT != 0) {
            //A best time exists.
//            bestTime.changeTime(savedTime); //Put best time into Time class.
            if (yT < bT) {
                //Your time is a new best time.
                newXP *= 1.5;   //TODO: check if good value
                xp += newXP;
                saveXP(xp);
                timeDifferential.changeTime(bT - yT);
                saveNewBestTime(yT);
//                colWit += 4*gameScreen.collisionsWitnessed;
//                saveXP(colWit);
                if(locationsUnlocked < numLocations - 1) {
                    if (xp > (locationsUnlocked + 1) * xpToUnlockEachLvl - 1) { //TODO: figure out how to do location/mode unlocked message
                        locationsUnlocked = xp / xpToUnlockEachLvl;
                        endGameText.setText(getResources().getString(R.string.win1) + "\nYou beat the record by\n" + timeDifferential.getTimeForDisplay() + "!\n\n" + getResources().getString(R.string.locationUnlocked));
                        saveLocationsUnlocked(locationsUnlocked);
                    }else {
                        endGameText.setText(getResources().getString(R.string.win1) + "\nYou beat the record by\n" + timeDifferential.getTimeForDisplay() + "!");
                    }
                } else {
                    endGameText.setText(getResources().getString(R.string.win1) + "\nYou beat the record by\n" + timeDifferential.getTimeForDisplay() + "!");
                }
                endGameUserTime.setText("Your Time: \n" + yourTime.getTimeForDisplay() + "\n+" + String.valueOf(newXP) + " XP");
                endGameBestTime.setText("Previous Best: \n" + bestTime.getTimeForDisplay() + "\n" + String.valueOf(xp) + " XP");
                bestTime.changeTime(yT);
                endGameBestTime.setBackgroundColor(Color.GREEN);
                endGameUserTime.setBackgroundColor(Color.GREEN);
                endGameText.setBackgroundColor(Color.GREEN);

                //Start win music.
//                    setMusicState(R.raw.finger_runner_win_new_record, WIN_MUSIC_NEW_RECORD, false);

            } else {
                //Your time isn't a new best time.
                xp += newXP;
//                colWit += 2*gameScreen.collisionsWitnessed;
//                saveXP(colWit);
                if(locationsUnlocked < numLocations - 1) {
                    if (xp > (locationsUnlocked + 1) * xpToUnlockEachLvl - 1) {
                        locationsUnlocked = xp / xpToUnlockEachLvl;
                        endGameText.setText(getResources().getString(R.string.win3) + "\n\n" + getResources().getString(R.string.locationUnlocked));
                        saveLocationsUnlocked(locationsUnlocked);
                    }else {
                        endGameText.setText(getResources().getString(R.string.win3));
                    }
                } else {
                    endGameText.setText(getResources().getString(R.string.win3));
                }
                endGameUserTime.setText("Your Time: \n" + yourTime.getTimeForDisplay() + "\n+" + String.valueOf(newXP) + " XP");
                endGameBestTime.setText("Best Time: \n" + bestTime.getTimeForDisplay() + "\n" + String.valueOf(xp) + " XP");
                endGameBestTime.setBackgroundColor(Color.LTGRAY);
                endGameUserTime.setBackgroundColor(Color.LTGRAY);
                endGameText.setBackgroundColor(Color.LTGRAY);

                //Start win music.
//                    setMusicState(R.raw.finger_runner_win_2, WIN_MUSIC_2, false);
            }
        } else {
            //No best time has been saved (a run has never been completed), so your time is a new best time.
            xp *= 2;    //TODO: check if good value
            xp += newXP;
            saveNewBestTime(yT);
            bestTime.changeTime(yT);
//            colWit += 4*gameScreen.collisionsWitnessed;
//            saveXP(colWit);
            if(locationsUnlocked < numLocations - 1) {
                if (xp > (locationsUnlocked + 1) * xpToUnlockEachLvl - 1) {
                    locationsUnlocked = xp / xpToUnlockEachLvl;
                    endGameText.setText(getResources().getString(R.string.win2) + "\n\n" + getResources().getString(R.string.locationUnlocked));
                    saveLocationsUnlocked(locationsUnlocked);
                }else {
                    endGameText.setText(getResources().getString(R.string.win2));
                }
            } else {
                endGameText.setText(getResources().getString(R.string.win2));
            }
            endGameUserTime.setText("Your Time: \n" + yourTime.getTimeForDisplay() + "\n+" + String.valueOf(newXP) + " XP");
            endGameBestTime.setText("Previous Best: \n" + "You'd never finished!" + "\n" + String.valueOf(xp) + " XP");
            endGameBestTime.setBackgroundColor(Color.LTGRAY);
            endGameUserTime.setBackgroundColor(Color.GREEN);
            endGameText.setBackgroundColor(Color.GREEN);

            //Start win music.
//                setMusicState(R.raw.finger_runner_win_new_record, WIN_MUSIC_NEW_RECORD, false);
        }

        saveXP(xp);
    }

    public void setGameLostState() {
        gameScreen.pauseGame();
        gameEndMenu.setVisibility(View.VISIBLE);
        root.removeView(gameEndMenu);
        root.addView(gameEndMenu);
        endGameUserTime.setBackgroundColor(Color.RED);
        endGameText.setBackgroundColor(Color.RED);

        //Set end game textviews for losing.
        if(!gameScreen.distanceMode) {
            int newXP = (locationState + 1)*(int)((10 * gameScreen.courseDistance) / gameScreen.gameTimer.getTime());    //TODO: test this out for gaining time trial xp
            xp += newXP;

            long bT = bestTime.getTime();

            //Start lose music.
//        if(nowPlaying != LOSE_MUSIC_1) {
//            setMusicState(R.raw.finger_runner_lose_1, LOSE_MUSIC_1, false);
//        }
//            colWit += gameScreen.collisionsWitnessed;
//            saveXP(colWit);
            endGameUserTime.setText("Your Time: \nYou didn't finish!" + "\n+" + String.valueOf(newXP) + " XP");
//            savedTime = loadBestTime();
            if (bT != 0) {
                //There was a saved best time.
//                bestTime.changeTime(savedTime);
                endGameBestTime.setText("Best Time: \n" + bestTime.getTimeForDisplay() + "\n" + String.valueOf(xp) + " XP");
                if(locationsUnlocked < numLocations - 1) {
                    if (xp > (locationsUnlocked + 1) * xpToUnlockEachLvl - 1) {
                        locationsUnlocked = xp / xpToUnlockEachLvl;
                        endGameText.setText(getResources().getString(R.string.lose2) + "\n\n" + getResources().getString(R.string.locationUnlocked));
                        saveLocationsUnlocked(locationsUnlocked);
                    }else {
                        endGameText.setText(getResources().getString(R.string.lose2));
                    }
                } else {
                    endGameText.setText(getResources().getString(R.string.lose2));
                }
                endGameBestTime.setBackgroundColor(Color.LTGRAY);
            } else {
                //The loaded best time was null (there has never been a best time saved).
                endGameBestTime.setText("Best Time: \n" + "You've never finished!" + "\n" + String.valueOf(xp) + " XP");
                if(locationsUnlocked < numLocations - 1) {
                    if (xp > (locationsUnlocked + 1) * xpToUnlockEachLvl - 1) {
                        locationsUnlocked = xp / xpToUnlockEachLvl;
                        endGameText.setText(getResources().getString(R.string.lose1) + "\n\n" + getResources().getString(R.string.locationUnlocked));
                        saveLocationsUnlocked(locationsUnlocked);
                    }else {
                        endGameText.setText(getResources().getString(R.string.lose1));
                    }
                } else {
                    endGameText.setText(getResources().getString(R.string.lose1));
                }
                endGameBestTime.setBackgroundColor(Color.RED);
            }
        } else{
//            bestDistance = loadBestDistance();
            float yourDistance = gameScreen.yourDistance;
            distanceDifferential = yourDistance - bestDistance;

            int newXP = gameScreen.collisionsWitnessed; //TODO: test this way of gaining xp for marathon (add distance somehow)
//            colWit += gameScreen.collisionsWitnessed;
//            saveXP(colWit);
            if(distanceDifferential > 0){
                //New record!
                newXP *= 1.5;   //TODO: check if good value
                xp += newXP;
                endGameUserTime.setText("New Record: \n" + String.valueOf(yourDistance) + "\n+" + String.valueOf(newXP) + " XP");
                endGameBestTime.setText("Previous Record: \n" + String.valueOf(bestDistance) + "\n" + String.valueOf(xp) + " XP");
                if(locationsUnlocked < numLocations - 1) {
                    if (xp > (locationsUnlocked + 1) * xpToUnlockEachLvl - 1) {
                        locationsUnlocked = xp / xpToUnlockEachLvl - 1;
                        endGameText.setText(getResources().getString(R.string.win1) + "\nYou beat the record by\n" + String.valueOf(distanceDifferential) + "!\n\n" + getResources().getString(R.string.locationUnlocked));
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

                saveBestDistance(yourDistance);
                endGameBestTime.setBackgroundColor(Color.LTGRAY);
                endGameUserTime.setBackgroundColor(Color.GREEN);
                endGameText.setBackgroundColor(Color.GREEN);
            } else {
                //Not a record.
                xp += newXP;
                endGameUserTime.setText("Your Distance: \n" + String.valueOf(yourDistance) + "\n+" + String.valueOf(newXP) + " XP");
                endGameBestTime.setText("Record: \n" + String.valueOf(bestDistance) + "\n" + String.valueOf(xp) + " XP");
                if(locationsUnlocked < numLocations - 1) {
                    if (xp > (locationsUnlocked + 1) * xpToUnlockEachLvl - 1) {
                        locationsUnlocked = xp / xpToUnlockEachLvl;
                        endGameText.setText(getResources().getString(R.string.lose2) + "\n\n" + getResources().getString(R.string.locationUnlocked));
                        saveLocationsUnlocked(locationsUnlocked);
                    }else {
                        endGameText.setText(getResources().getString(R.string.lose2));
                    }
                } else {
                    endGameText.setText(getResources().getString(R.string.lose2));
                }
                endGameBestTime.setBackgroundColor(Color.LTGRAY);
                endGameUserTime.setBackgroundColor(Color.LTGRAY);
                endGameText.setBackgroundColor(Color.LTGRAY);
                //Keep game music going.
                setMusicState(R.raw.finger_runner_game_music_1, GAME_MUSIC_1, true);
            }
        }

        saveXP(xp);
    }

    // Game paused. Not app paused.
    public void setPausedState() {
        gameScreen.pauseGame();

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

        gameScreen.animate()
                .translationY(0)//-windowSize.y)
                .setDuration(1000);

        //Update gameScreen to look reset.
        gameScreen.resetVariables();

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

    public void setSettingsState() {
        hideAllMenus();
        creditsText.setVisibility(View.GONE);
        settingsMenu.setVisibility(View.VISIBLE);
        experiencePointsText.setText(String.valueOf(xp) + " XP");
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
        if(musicThread != null){
            if(musicThread.isAlive()){
                try {
                    musicThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if(!musicPausedByLeavingApp) {
            final int finalId = id;
            final int finalMusicState = musicState;
            musicThread = new Thread(new Runnable() {
                @Override
                public void run() {
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
                }
            });
            musicThread.start();
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
        if(musicThread != null) {
            musicThread.interrupt();
            musicThread = null;
        }
        stopMusic();

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
            pauseMenu.setVisibility(View.VISIBLE);
            root.removeView(pauseMenu);
            root.addView(pauseMenu);

        } else if (gameState == GAME_LOST || gameState == GAME_WON) {
            gameScreen.pauseGame();
            gameEndMenu.setVisibility(View.VISIBLE);
            root.removeView(gameEndMenu);
            root.addView(gameEndMenu);
        } else {
            setGameState(gameState);//setGameState(previousGameState);
        }
    }

    private void setLocationState(int loc){
        int prevLoc = locationState;
        locationState = loc;
        if(loc < numLocations && loc > -1) {
            switch (loc) {
                case 0:
                    timeTrial2Flag = false;
                    locationRes = R.drawable.pan_1;

                    //TODO: display xp somewhere on menu
                    bestTime.changeTime(loadBestTime());
                    playButton.setEnabled(true);
                    playButton.setText("Time Trial 1");

                    Time bestTime2 = new Time();
                    if(bestTime.getTime() == 0l) {
                        menuTimeText.setText("-:--:---");
                        playMarathonButton.setEnabled(false);
                        menuDistanceText.setText("LOCKED: You must complete Time Trial 1 in under 0:10:000.");
                    } else if (bestTime.getSeconds() > 9){
                        menuTimeText.setText(bestTime.getTimeForDisplay());
                        playMarathonButton.setEnabled(false);
                        menuDistanceText.setText("LOCKED: You must complete Time Trial 1 in under 0:10:000.");
                    } else {
                        playMarathonButton.setEnabled(true);
                        menuTimeText.setText(bestTime.getTimeForDisplay());

                        timeTrial2Flag = true;
                        bestTime2.changeTime(loadBestTime());
                        timeTrial2Flag = false;
                        if(bestTime2.getTime() == 0L){
                            menuDistanceText.setText("-:--:---");
                        } else {
                            menuDistanceText.setText(bestTime2.getTimeForDisplay());
                        }

                    }
                    playMarathonButton.setText("Time Trial 2");

                    menuText.setText(getResources().getString(R.string.loc0));

                    previousLocationButton.setVisibility(View.INVISIBLE);    //For the first location, this button should not be seen.
                    nextLocationButton.setVisibility(View.VISIBLE);    //For the last location, this button should not be seen.
                    //titleScreen.startAnimation(fadeOut);
                    break;
                case 1:
                    timeTrial2Flag = false;

                    locationRes = R.drawable.fatty;

                    //TODO: display xp somewhere on menu
                    menuText.setText(getResources().getString(R.string.loc1));
                    playButton.setText("Time Trial");
                    playMarathonButton.setText("Marathon");

                    if(locationsUnlocked > 0) {
                        playButton.setEnabled(true);
                        bestTime.changeTime(loadBestTime());

                        if(bestTime.getTime() == 0l) {
                            menuTimeText.setText("-:--:---");
                            playMarathonButton.setEnabled(false);
                            menuDistanceText.setText("LOCKED: You must complete Level 1 Time Trial in under 0:20:000.");
                        } else if (bestTime.getSeconds() > 19){
                            menuTimeText.setText(bestTime.getTimeForDisplay());
                            playMarathonButton.setEnabled(false);
                            menuDistanceText.setText("LOCKED: You must complete Level 1 Time Trial in under 0:20:000.");
                        } else {
                            menuTimeText.setText(bestTime.getTimeForDisplay());
                            playMarathonButton.setEnabled(true);
                            bestDistance = loadBestDistance();
                            menuDistanceText.setText(String.valueOf(bestDistance)); //TODO: format
                        }

                    } else {
                        playButton.setEnabled(false);
                        playMarathonButton.setEnabled(false);
                        menuTimeText.setText("LOCKED: 100 XP required.");
                        menuDistanceText.setText("LOCKED: You must complete Level 1 Time Trial in under 0:20:000.");
                    }
                    previousLocationButton.setVisibility(View.VISIBLE);
                    nextLocationButton.setVisibility(View.VISIBLE);
                    //titleScreen.startAnimation(fadeOut);
                    break;
                case 2:
                    timeTrial2Flag = false;
                    locationRes = R.drawable.right_foot;
                    //TODO: display xp somewhere on menu
                    menuText.setText(getResources().getString(R.string.loc2));
                    playButton.setText("Time Trial");
                    playMarathonButton.setText("Marathon");

                    if(locationsUnlocked > 1) {
                        playButton.setEnabled(true);
                        bestTime.changeTime(loadBestTime());

                        if(bestTime.getTime() == 0l) {
                            menuTimeText.setText("-:--:---");
                            playMarathonButton.setEnabled(false);
                            menuDistanceText.setText("LOCKED: You must complete Level 2 Time Trial in under 1:00:000.");
                        } else if (bestTime.getMinutes() > 0){
                            menuTimeText.setText(bestTime.getTimeForDisplay());
                            playMarathonButton.setEnabled(false);
                            menuDistanceText.setText("LOCKED: You must complete Level 2 Time Trial in under 1:00:000.");
                        } else {
                            menuTimeText.setText(bestTime.getTimeForDisplay());
                            playMarathonButton.setEnabled(true);
                            bestDistance = loadBestDistance();
                            menuDistanceText.setText(String.valueOf(bestDistance)); //TODO: format
                        }

                    } else {
                        playButton.setEnabled(false);
                        playMarathonButton.setEnabled(false);
                        menuTimeText.setText("LOCKED: 200 XP required.");
                        menuDistanceText.setText("LOCKED: You must complete Level 2 Time Trial in under 1:00:000.");
                    }
                    previousLocationButton.setVisibility(View.VISIBLE);
                    nextLocationButton.setVisibility(View.INVISIBLE);    //For the last location, this button should not be seen.
                    break;
            }
            if(loc < prevLoc){
                alternateTitleScreen = findViewById(R.id.titleScreen);
                alternateTitleScreen.setBackground(titleScreen.getBackground());
                alternateTitleScreen.setTranslationX(0);
                titleScreen = findViewById(R.id.titleScreenAlternate);
                titleScreen.setTranslationX(-windowSize.x);
                titleScreen.setBackgroundResource(locationRes);
                titleScreen.setVisibility(View.VISIBLE);
                mainMenu.setVisibility(View.INVISIBLE);

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        gameScreen.resetVariables();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mainMenu.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
                t.start();

                alternateTitleScreen.animate()
                        .translationX(windowSize.x)
                        .setDuration(1500);
                titleScreen.animate()
                        .translationX(0)
                        .setDuration(1500);
//                        .withEndAction(new Runnable() {
//                            @Override
//                            public void run() {
////                                gameScreen.resetVariables();
////                                mainMenu.setVisibility(View.VISIBLE);
//                            }
//                        });
            }else if (loc > prevLoc){
                alternateTitleScreen = findViewById(R.id.titleScreen);
                alternateTitleScreen.setBackground(titleScreen.getBackground());
                alternateTitleScreen.setTranslationX(0);
                titleScreen = findViewById(R.id.titleScreenAlternate);
                titleScreen.setBackgroundResource(locationRes);
                titleScreen.setTranslationX(windowSize.x);
                titleScreen.setVisibility(View.VISIBLE);
                mainMenu.setVisibility(View.INVISIBLE);

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        gameScreen.resetVariables();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mainMenu.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
                t.start();

                alternateTitleScreen.animate()
                        .translationX(-windowSize.x)
                        .setDuration(1500);
                titleScreen.animate()
                        .translationX(0)
                        .setDuration(1500);
//                        .withEndAction(new Runnable() {
//                            @Override
//                            public void run() {
////                                gameScreen.resetVariables();
////                                mainMenu.setVisibility(View.VISIBLE);
//                            }
//                        });
            }
        }
    }

    private void saveNewBestTime(long newBestTime){
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
        //Long time = null;
        String s;
        if(timeTrial2Flag){
            s = "bestTime" + String.valueOf(locationState) + "_2";
        } else {
            s = "bestTime" + String.valueOf(locationState);
        }
        Long time = sharedPref.getLong(s, 0l);
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
        String s = "bestDistance" + String.valueOf(locationState);
        prefEditor.putFloat(s, distance);
        prefEditor.commit();
    }

    private float loadBestDistance(){
        String s = "bestDistance" + String.valueOf(locationState);
        float distance = sharedPref.getFloat(s, 0f);
        return distance;
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
}
