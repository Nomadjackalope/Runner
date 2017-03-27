package com.runrmby.runner;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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

    private MediaPlayer activeMusic;
    private static final int MENU_MUSIC = 0;
    private static final int GAME_MUSIC_1 = 1;
    private static final int WHISTLE_MUSIC = 2;
    private int nowPlaying;
    boolean musicMuted;
    boolean musicPausedByLeavingApp;
    int activeMusicPosition;
    private boolean whistleMusicSelected = false;

    //Sound effects
    public SoundPool sfx;
    public int crashSound5Id;

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
    private Button creditsButton;

    private TextView endGameHeaderText;
    private TextView endGameUserTime;
    private TextView endGameBestTime;
    private TextView endGameText;
    private TextView endGameStatNames;
    private TextView endGameStats;
    private TextView endGameScoreText;
    private TextView endGameScoreValues;
    private TextView menuText;
    public TextView timer;
    public TextView experiencePointsText;
    private TextView titleText;
    private TextView creditsText;
    private TextView menuTimeText;
    private TextView menuDistanceText;
    private TextView loadingText;
    private TextView statsText;

    public Typeface pixellari;

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

    public int locationState = 0;
    private static int numLocations = 4;
    private int locationRes;
    private int locationsUnlocked;

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
    private float bestD1 = -1;
    private float bestD2 = -1;
    private float bestD3 = -1;
    private float highScoreT;
    private float highScoreD;
    private float prevHighScore;
    private float highScoreT0 = -1;
    private float highScoreT1 = -1;
    private float highScoreT2 = -1;
    private float highScoreT3 = -1;
    private float highScoreD1 = -1;
    private float highScoreD2 = -1;
    private float highScoreD3 = -1;

    //Temp game won/lost variables.
    long bT;
    long yT;
    float yourDistance;
    int steps;
    float adjSteps;
    float distance;
    float adjDistance;
    int coins;
    float adjCoins;
    float adjTime;
    float denominator;
    float runScore;
    float highScore;

    private int runnerCoins = 0;
    private float distPerSec = 0;
    private float distPerStep = 0;
    private float coinsPerSec = 0;
    private float secPerLife = 0;
    private int numRuns = 0;

    Thread lt = null;

    boolean initialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.FullscreenTheme);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        sharedPref = getSharedPreferences("Runner", MODE_PRIVATE);
        prefEditor = sharedPref.edit();

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


        root.addView(gameScreen);

        gameScreen.setVisibility(View.VISIBLE);
        gameScreen.setTranslationY(windowSize.y);
        gameScreen.setBackgroundColor(getResources().getColor(R.color.transparent, null));

        //Music
        musicMuted = sharedPref.getBoolean("musicMuted", false);


        setGameState(MAIN_MENU);

        //Fonts.
        pixellari = Typeface.createFromAsset(getAssets(), "fonts/Pixellari.ttf");

        //Text Views
        timer = (TextView) findViewById(R.id.timer);
        timer.setTypeface(pixellari);
        loadingText = (TextView) findViewById(R.id.loadingText);
        loadingText.setTypeface(pixellari);
        endGameHeaderText = (TextView) findViewById(R.id.endGameHeader);
        endGameHeaderText.setTypeface(pixellari);
        endGameUserTime = (TextView) findViewById(R.id.endGameUserTime);
        endGameUserTime.setTypeface(pixellari);
        endGameBestTime = (TextView) findViewById(R.id.endGameBestTime);
        endGameBestTime.setTypeface(pixellari);
        endGameText = (TextView) findViewById(R.id.endGameText);
        endGameText.setTypeface(pixellari);
        endGameStatNames = (TextView) findViewById(R.id.endGameStatNames);
        endGameStatNames.setTypeface(pixellari);
        endGameStats = (TextView) findViewById(R.id.endGameStats);
        endGameStats.setTypeface(pixellari);
        endGameScoreText = (TextView) findViewById(R.id.endGameScoreText);
        endGameScoreText.setTypeface(pixellari);
        endGameScoreValues = (TextView) findViewById(R.id.endGameScoreValues);
        endGameScoreValues.setTypeface(pixellari);
        experiencePointsText = (TextView) findViewById(R.id.experiencePoints);
        experiencePointsText.setTypeface(pixellari);
        menuText = (TextView) findViewById(R.id.menuText);
        menuText.setTypeface(pixellari);
        titleText = (TextView) findViewById(R.id.textView);
        titleText.setTypeface(pixellari);
        menuTimeText = (TextView) findViewById(R.id.menuBestTimeText);
        menuTimeText.setTypeface(pixellari);
        menuDistanceText = (TextView) findViewById(R.id.menuDistanceText);
        menuDistanceText.setTypeface(pixellari);
        creditsText = (TextView) findViewById(R.id.creditsText);
        creditsText.setTypeface(pixellari);
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
        statsText = (TextView) findViewById(R.id.statsText);
        statsText.setTypeface(pixellari);

        //Buttons
        playAgainButton = (Button) findViewById(R.id.playAgainButton);
        playAgainButton.setTypeface(pixellari);
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
        mainMenuButton.setTypeface(pixellari);
        mainMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!musicMuted && sfx != null){
                    sfx.play(crashSound5Id, 1, 1, 0, 0, 1);
                }
                updateLoc(locationState);
                setGameState(TRANSIT_TO_MM);
            }
        });


        playButton = (Button) findViewById(R.id.playButton);
        playButton.setTypeface(pixellari);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!musicMuted && sfx != null){
                    sfx.play(crashSound5Id, 1, 1, 0, 0, 1);
                }
                gameScreen.distanceMode = false;
                setGameState(TRANSIT_TO_GAME);
            }
        });

        playMarathonButton = (Button) findViewById(R.id.playDistanceModeButton);
        playMarathonButton.setTypeface(pixellari);
        playMarathonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!musicMuted && sfx != null){
                    sfx.play(crashSound5Id, 1, 1, 0, 0, 1);
                }
                gameScreen.distanceMode = true;
                setGameState(TRANSIT_TO_GAME);
            }
        });

        nextLocationButton = (Button) findViewById(R.id.nextLocationButton);
        nextLocationButton.setTypeface(pixellari);
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
        previousLocationButton.setTypeface(pixellari);
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
        settingsButton.setTypeface(pixellari);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                if(!musicMuted && sfx != null){
                    sfx.play(crashSound5Id, 1, 1, 0, 0, 1);
                }
                setGameState(SETTINGS);
            }
        });

        exitSettingsButton = (Button) findViewById(R.id.exitSettingsButton);
        exitSettingsButton.setTypeface(pixellari);
        exitSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!musicMuted && sfx != null){
                    sfx.play(crashSound5Id, 1, 1, 0, 0, 1);
                }
                setGameState(MAIN_MENU);
            }
        });

        tempButton = (Button) findViewById(R.id.tempButton);
        tempButton.setTypeface(pixellari);
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
        resetButton.setTypeface(pixellari);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!musicMuted && sfx != null){
                    sfx.play(crashSound5Id, 1, 1, 0, 0, 1);
                }
                //Reset.
                AlertDialog.Builder resetWarning = new AlertDialog.Builder(root.getContext());
                resetWarning.setMessage("Are you sure you want to reset EVERYTHING?");
                resetWarning.setCancelable(true);
                resetWarning.setPositiveButton(
                        "Yes, please.",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if(!musicMuted && sfx != null){
                                    sfx.play(crashSound5Id, 1, 1, 0, 0, 1);
                                }
                                locationsUnlocked = 0;
                                saveLocationsUnlocked(0);

                                for(int i = 0; i < numLocations; i++) {
                                    String sTime = "bestTime" + String.valueOf(i);
                                    String sDist = "bestDistance" + String.valueOf(i);
                                    String sHighT = "highScoreT" + String.valueOf(i);
                                    String sHighD = "highScoreD" + String.valueOf(i);
                                    prefEditor.putLong(sTime, 0L);
                                    prefEditor.putFloat(sDist, 0f);
                                    prefEditor.putFloat(sHighT, 0f);
                                    prefEditor.putFloat(sHighD, 0f);
                                }
                                bestTime.changeTime(0L);
                                bestDistance = 0f;
                                bestT0 = -1;
                                bestT1 = 0;
                                bestT2 = 0;
                                bestT3 = 0;
                                bestD1 = 0;
                                bestD2 = 0;
                                bestD3 = 0;
                                highScoreT = 0;
                                highScoreD = 0;
                                highScoreT0 = 0;
                                highScoreT1 = 0;
                                highScoreT2 = 0;
                                highScoreT3 = 0;
                                highScoreD1 = 0;
                                highScoreD2 = 0;
                                highScoreD3 = 0;

                                runnerCoins = 0;
                                distPerSec = 0;
                                distPerStep = 0;
                                coinsPerSec = 0;
                                secPerLife = 0;
                                numRuns = 0;
                                prefEditor.commit();
                                Toast.makeText(root.getContext(), "Records reset.", Toast.LENGTH_SHORT).show();

                                updateLoc(locationState);
                                setGameState(gameState);

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
        whistleMusicButton.setTypeface(pixellari);
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

        creditsButton = (Button) findViewById(R.id.creditsButton);
        creditsButton.setTypeface(pixellari);
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
        musicMuteButton.setTypeface(pixellari);
        if(musicMuted && sfx != null){
            musicMuteButton.setChecked(false);
        }
        musicMuteButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(!musicMuted) {
                    stopMusic();
                    musicMuted = true;
                }
                else {
                    musicMuted = false;
                    if(!musicMuted && sfx != null){
                        sfx.play(crashSound5Id, 1, 1, 0, 0, 1);
                    }
                    setMusicState(R.raw.finger_runner_main_menu, MENU_MUSIC, true);
                }

                //Store mute preference.
                prefEditor.putBoolean("musicMuted", musicMuted);
                prefEditor.commit();
            }
        });

        pauseMMButton = (Button) findViewById(R.id.pauseMMButton);
        pauseMMButton.setTypeface(pixellari);
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
        resumeButton.setTypeface(pixellari);
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

        loadRunnerCoins();
        loadStats();
        updateLoc(0); //TODO: probably not necessary, but could save last state to initialize with.
    }

    public void setGameState(int state) {
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
        gameScreen.handleTouches = true;
    }

    public void setGameWonState() {
        gameScreen.pauseGame();
        setSFX();
        gameEndMenu.setVisibility(View.VISIBLE);
        root.removeView(gameEndMenu);
        root.addView(gameEndMenu);
        endGameText.setBackgroundResource(R.drawable.rectangle_white);
        switch(locationState){
            case 0:
                endGameHeaderText.setText("Warm-Up\nTIME TRIAL RESULTS");
                break;
            case 1:
                if(gameScreen.distanceMode) {
                    endGameHeaderText.setText("Race Track\nDISTANCE RESULTS");
                } else {
                    endGameHeaderText.setText("Race Track\nTIME TRIAL RESULTS");
                }
                break;
            case 2:
                if(gameScreen.distanceMode) {
                    endGameHeaderText.setText("The Highway\nDISTANCE RESULTS");
                } else {
                    endGameHeaderText.setText("The Highway\nTIME TRIAL RESULTS");
                }
                break;
            case 3:
                if(gameScreen.distanceMode) {
                    endGameHeaderText.setText("Tiny Traffic\nDISTANCE RESULTS");
                } else {
                    endGameHeaderText.setText("Tiny Traffic\nTIME TRIAL RESULTS");
                }
                break;
        }

        // TODO Shrink this or at least separate it into its functional parts
        yourTime = gameScreen.gameTimer;
        bT = bestTime.getTime();
        yT = yourTime.getTime();

        steps = gameScreen.steps;
        adjSteps = steps * 0.1f;
        distance = gameScreen.courseDistance / gameScreen.sY;
        adjDistance = distance * 0.001f;
        coins = gameScreen.coins;
        if((gameScreen.yourDistance  / gameScreen.sY) < distance && !gameScreen.distanceMode){
            adjCoins = coins;
        } else {
            adjCoins = coins * (1.25f + locationState * 0.25f);
        }
        adjTime = yT * 0.001f;
        denominator = (float)Math.pow((adjSteps + adjTime), 1.5);
        runScore = 5*((((adjCoins + (adjDistance * adjDistance)) / denominator) * ((1 + adjCoins) / adjSteps) + (adjCoins * (1+locationState) * 0.25f)) * ((1 + locationState) * 0.25f));

        //TODO: make coin spawning rate depend on previous run speed
        distPerSec = (distPerSec + (distance / (yT / 1000L))) / 2;
        distPerStep = (distPerStep + ( distance / (float)steps)) / 2;
        coinsPerSec = (coinsPerSec + ((float)coins / (yT / 1000L))) / 2;
        numRuns++;

        //If high score (not best time), get more experience points
        highScore = loadHighScoreT();
        prevHighScore = highScore;
        if(runScore > highScore){
            highScore = runScore;
            saveHighScoreT(highScore);
            endGameStatNames.setBackgroundResource(R.drawable.rectangle_white_border_yellow);
            endGameScoreText.setText("New High Score:\nPrevious High:\nBONUS:");//\nRunner Coins:");
            addRunnerCoins(coins * (2 + locationState));
            endGameScoreValues.setText(String.format("%.2f", runScore) + "\n(" + String.format("%.2f", prevHighScore) + ")\n+" + String.valueOf(coins * (1 + locationState)) + " coins");//\n" + String.valueOf(runnerCoins));
        } else {
            endGameStatNames.setBackgroundResource(R.drawable.rectangle_white);
            endGameScoreText.setText("Run Score:\nHigh Score:");//\nRunner Coins:");
            addRunnerCoins(coins);
            endGameScoreValues.setText(String.format("%.2f", runScore) + "\n(" + String.format("%.2f", prevHighScore) + ")");//\n" + String.valueOf(runnerCoins));
        }

        if(bT != 0) {
            //A best time exists.
            if (yT < bT) {
                //Your time is a new best time.

                endGameUserTime.setBackgroundResource(R.drawable.rectangle_white_border_yellow);
                endGameStats.setText(String.format("%.0f", distance) + "\n" + yourTime.getTimeForDisplay() + "\n" + String.valueOf(steps) + "\n" + String.valueOf(coins) + "\n______________");


//                saveXP(xp);
                timeDifferential.changeTime(bT - yT);
                saveNewBestTime(yT);

                String s = "You beat the best time by\n" + timeDifferential.getTimeForDisplay() + "!\n" + getResources().getString(R.string.win1);
                switch(locationState){
                    case 0:
                        //no distance mode to unlock
                        break;
                    case 1:
                        if(bT > 24999){
                            if(yT < 25000){
                                s += "\nStage 1 Distance Mode unlocked!";
                                endGameText.setBackgroundResource(R.drawable.rectangle_white_border_yellow);
                            }
                        }
                        break;
                    case 2:
                        if(bT > 24999){
                            if(yT < 25000){
                                s += "\nStage 2 Distance Mode unlocked!";
                                endGameText.setBackgroundResource(R.drawable.rectangle_white_border_yellow);
                            }
                        }
                        break;
                    case 3:
                        if(bT > 24999){
                            if(yT < 25000){
                                s += "\nStage 3 Distance Mode unlocked!";
                                endGameText.setBackgroundResource(R.drawable.rectangle_white_border_yellow);
                            }
                        }
                        break;
                }
                s += checkStagesUnlocked();

                endGameText.setText(s);

                endGameUserTime.setText("New Best Time:\nPrevious Best:");
                endGameBestTime.setText(yourTime.getTimeForDisplay() + "\n(" + bestTime.getTimeForDisplay() + ")");
                bestTime.changeTime(yT);

            } else {
                //Your time isn't a new best time.
                endGameUserTime.setBackgroundResource(R.drawable.rectangle_white);
                endGameStats.setText(String.format("%.0f", distance) + "\n" + yourTime.getTimeForDisplay() + "\n" + String.valueOf(steps) + "\n" + String.valueOf(coins) + "\n______________");

                String s = getResources().getString(R.string.win3);
                s += checkStagesUnlocked();

                endGameText.setText(s);

                endGameUserTime.setText("Your Run:\nBest Time:");
                endGameBestTime.setText(yourTime.getTimeForDisplay() + "\n(" + bestTime.getTimeForDisplay() + ")");
            }
        } else {
            //No best time has been saved (a run has never been completed), so your time is a new best time.
            endGameUserTime.setBackgroundResource(R.drawable.rectangle_white_border_yellow);
            endGameStats.setText(String.format("%.0f", distance) + "\n" + yourTime.getTimeForDisplay() + "\n" + String.valueOf(steps) + "\n" + String.valueOf(coins) + "\n______________");


            saveNewBestTime(yT);
            bestTime.changeTime(yT);

            String s = getResources().getString(R.string.win2);
            switch(locationState) {
                case 0:
                    //no distance mode to unlock
                    break;
                case 1:
                    if (yT < 25000) {
                        s += "\nStage 1 Distance Mode unlocked!";
                        endGameText.setBackgroundResource(R.drawable.rectangle_white_border_yellow);
                    }
                    break;
                case 2:
                    if (yT < 25000) {
                        s += "\nStage 2 Distance Mode unlocked!";
                        endGameText.setBackgroundResource(R.drawable.rectangle_white_border_yellow);
                    }
                    break;
                case 3:
                    if (yT < 25000) {
                        s += "\nStage 3 Distance Mode unlocked!";
                        endGameText.setBackgroundResource(R.drawable.rectangle_white_border_yellow);
                    }
                    break;
            }
            s += checkStagesUnlocked();

            endGameText.setText(s);

            endGameUserTime.setText("New Best Time:\nPrevious Best:");
            endGameBestTime.setText(yourTime.getTimeForDisplay() + "\n(-:--:---)");
        }

        saveStats();
    }

    public void setGameLostState() {
        gameScreen.pauseGame();
        setSFX();
        gameEndMenu.setVisibility(View.VISIBLE);
        root.removeView(gameEndMenu);
        root.addView(gameEndMenu);
        endGameText.setBackgroundResource(R.drawable.rectangle_white);

        switch(locationState){
            case 0:
                endGameHeaderText.setText("Warm-Up\nTIME TRIAL RESULTS");
                break;
            case 1:
                if(gameScreen.distanceMode) {
                    endGameHeaderText.setText("Race Track\nDISTANCE RESULTS");
                } else {
                    endGameHeaderText.setText("Race Track\nTIME TRIAL RESULTS");
                }
                break;
            case 2:
                if(gameScreen.distanceMode) {
                    endGameHeaderText.setText("The Highway\nDISTANCE RESULTS");
                } else {
                    endGameHeaderText.setText("The Highway\nTIME TRIAL RESULTS");
                }
                break;
            case 3:
                if(gameScreen.distanceMode) {
                    endGameHeaderText.setText("Tiny Traffic\nDISTANCE RESULTS");
                } else {
                    endGameHeaderText.setText("Tiny Traffic\nTIME TRIAL RESULTS");
                }
                break;
        }

        distance = gameScreen.courseDistance / gameScreen.sY;
        yourDistance = gameScreen.yourDistance / gameScreen.sY;
        distanceDifferential = yourDistance - bestDistance;

        yourTime = gameScreen.gameTimer;
        yT = yourTime.getTime();
        steps = gameScreen.steps;
        adjSteps = steps * 0.1f;
        adjDistance = yourDistance * 0.001f;
        coins = gameScreen.coins;
        if(yourDistance < (int)(gameScreen.courseDistance / gameScreen.sY) && !gameScreen.distanceMode){
            adjCoins = coins;
        } else {
            adjCoins = coins * (1.25f + locationState * 0.25f);
        }
        adjTime = (yT + 1000*locationState) * 0.001f;
        denominator = (float)Math.pow(adjSteps + adjTime, 1.5);
        runScore = 5*((((adjCoins + (adjDistance * adjDistance)) / denominator) * ((1 + adjCoins) / adjSteps) + (adjCoins * (1+locationState) * 0.25f)) * ((1 + locationState) * 0.25f));

        //TODO: make coin spawning rate depend on previous run speed
        distPerSec = (distPerSec + (yourDistance / (yT / 1000L))) / 2;
        distPerStep = (distPerStep + (yourDistance / (float)steps)) / 2;
        coinsPerSec = (coinsPerSec + ((float)coins / (yT / 1000L))) / 2;
        numRuns++;

        //Set end game textviews for losing.
        if(!gameScreen.distanceMode) {
            endGameUserTime.setBackgroundResource(R.drawable.rectangle_white_border_red);
            highScore = loadHighScoreT();
            prevHighScore = highScore;
            //TODO: make sure can't be higher if you don't finish
            if(runScore > highScore){
                highScore = runScore;
                saveHighScoreT(highScore);
                endGameStatNames.setBackgroundResource(R.drawable.rectangle_white_border_yellow);
                endGameScoreText.setText("New High Score:\nPrevious High:\nBONUS:");//\nRunner Coins:");
                addRunnerCoins(coins * (2 + locationState));
                endGameScoreValues.setText(String.format("%.2f", runScore) + "\n(" + String.format("%.2f", prevHighScore) + ")\n+" + String.valueOf(coins * (1 + locationState)) + " coins");//\n" + String.valueOf(runnerCoins));
            } else {
                endGameStatNames.setBackgroundResource(R.drawable.rectangle_white);
                endGameScoreText.setText("Run Score:\nHigh Score:");//\nRunner Coins:");
                addRunnerCoins(coins);
                endGameScoreValues.setText(String.format("%.2f", runScore) + "\n(" + String.format("%.2f", prevHighScore) + ")");//\n" + String.valueOf(runnerCoins));
            }
            endGameStats.setText(String.format("%.0f", yourDistance) + " (" + String.format("%.0f", (yourDistance)/distance*100) + "%)\n" + yourTime.getTimeForDisplay() + "\n" + String.valueOf(steps) + "\n" + String.valueOf(coins) + "\n______________");

//            xp += newXP;

            long bT = bestTime.getTime();

            //Start lose music.
            endGameUserTime.setText("Your Run:\nBest Time:");
//            savedTime = loadBestTime();
            String s;
            if (bT != 0) {
                //There was a saved best time.
                endGameBestTime.setText("DISQUALIFIED\n(" + bestTime.getTimeForDisplay() + ")");
                s = getResources().getString(R.string.lose2);

            } else {
                //The loaded best time was null (there has never been a best time saved).
                endGameBestTime.setText("DISQUALIFIED\n(-:--:---)");
                s = getResources().getString(R.string.lose1);
            }
            s += checkStagesUnlocked();
            endGameText.setText(s);

        } else{
            secPerLife = (secPerLife + ((yT / 1000) / 3)) / 2;
            float highScore = loadHighScoreD();
            prevHighScore = highScore;
            if(runScore > highScore){
                highScore = runScore;
//                newXP *= 1.5f;
                saveHighScoreD(highScore);
                endGameStatNames.setBackgroundResource(R.drawable.rectangle_white_border_yellow);
                endGameScoreText.setText("New High Score:\nPrevious High:\nBONUS:");//\nRunner Coins:");
                addRunnerCoins(coins * (2 + locationState));
                endGameScoreValues.setText(String.format("%.2f", runScore) + "\n(" + String.format("%.2f", prevHighScore) + ")\n+" + String.valueOf(coins * (1 + locationState)) + " coins");//\n" + String.valueOf(runnerCoins));
            } else {
                endGameStatNames.setBackgroundResource(R.drawable.rectangle_white);
                endGameScoreText.setText("Run Score:\nHigh Score:");//\nRunner Coins");
                addRunnerCoins(coins);
                endGameScoreValues.setText(String.format("%.2f", runScore) + "\n(" + String.format("%.2f", prevHighScore) + ")");//\n" + String.valueOf(runnerCoins));
            }

            endGameStats.setText(String.format("%.0f", yourDistance) + " (" + String.format("%.0f", (yourDistance)/distance*100) + "%)\n" + yourTime.getTimeForDisplay() + "\n" + String.valueOf(steps) + "\n" + String.valueOf(coins) + "\n______________");
//            bestDistance = loadBestDistance();

            if(distanceDifferential > 0){
                //New record!
                endGameUserTime.setBackgroundResource(R.drawable.rectangle_white_border_yellow);
//                newXP *= 1.5;   //TODO: check if good value
//                xp += newXP;
                endGameUserTime.setText("New Long Run:\nPrevious Best:");
                endGameBestTime.setText(String.format("%.0f", yourDistance) + "\n(" + String.format("%.0f", bestDistance) + ")");

                String s = "You beat the longest run by\n" + String.format("%.0f", distanceDifferential) + "!\n" + getResources().getString(R.string.win1);
                s += checkStagesUnlocked();

                endGameText.setText(s);

//                setMusicState(R.raw.finger_runner_win_new_record, WIN_MUSIC_NEW_RECORD, false);
                //Keep game music going.
                setMusicState(R.raw.finger_runner_game_music_1, GAME_MUSIC_1, true);

                bestDistance = yourDistance;
                saveBestDistance(bestDistance);
            } else {
                //Not a record.
                endGameUserTime.setBackgroundResource(R.drawable.rectangle_white);
//                xp += newXP;
                endGameUserTime.setText("Your Run:\nLongest Run:");
                endGameBestTime.setText(String.format("%.0f", yourDistance) + "\n(" + String.format("%.0f", bestDistance) + ")");

                String s = getResources().getString(R.string.lose2);
                s += checkStagesUnlocked();

                endGameText.setText(s);

                //Keep game music going.
                setMusicState(R.raw.finger_runner_game_music_1, GAME_MUSIC_1, true);
            }
        }

        saveStats();
//        saveXP(xp);
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
        //show it's loading
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
    }

    public void setSettingsState() {
        hideAllMenus();
        creditsText.setVisibility(View.GONE);
//        statsText.setText("STATS\nRuns: " + String.valueOf(numRuns) + "\nAvg. Speed = " + String.format("%.0f", distPerSec) + " pix/sec\nAvg. Lifespan (Dist.) = " + String.format("%.2f", secPerLife) + " sec\nRunner Income = " + String.format("%.2f", coinsPerSec) + " coins/sec");

        settingsMenu.setVisibility(View.VISIBLE);
        if(runnerCoins > 1999){
            if(!whistleMusicButton.isEnabled()) {
                whistleMusicButton.setEnabled(true);
            }
        } else {
            if(whistleMusicButton.isEnabled()) {
                whistleMusicButton.setEnabled(false);
            }
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
        if(!musicPausedByLeavingApp) {
            final int finalId = id;
            final int finalMusicState = musicState;
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

    public void setSFX() {
        if (sfx == null) {
//        soundEffects = new SoundPool.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build();
            sfx = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
            crashSound5Id = sfx.load(this, R.raw.finger_runner_crash_sound_5, 1);
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

            //Manually set paused state.
            gameMenu.setVisibility(View.INVISIBLE);
            gameScreen.pauseGame();
            setSFX();
            setMusicState(R.raw.finger_runner_game_music_1,GAME_MUSIC_1, true);
            pauseMenu.setVisibility(View.VISIBLE);
            root.removeView(pauseMenu);
            root.addView(pauseMenu);

        } else if (gameState == GAME_LOST || gameState == GAME_WON) {
            gameScreen.pauseGame();
            setSFX();
            setMusicState(R.raw.finger_runner_game_music_1,GAME_MUSIC_1, true);
            gameEndMenu.setVisibility(View.VISIBLE);
            root.removeView(gameEndMenu);
            root.addView(gameEndMenu);
        } else {
            setSFX();
            setGameState(gameState);
        }
    }

    private void updateLoc(int loc) {
        locationState = loc;
        experiencePointsText.setText(String.valueOf(runnerCoins) + " Runner Coins");
        highScoreT = loadHighScoreT();
        highScoreD = loadHighScoreD();
        bestTime.changeTime(loadBestTime());
        if (loc < numLocations && loc > -1) {
            switch (loc) {
                case 0:
                    menuText.setText(getResources().getString(R.string.loc0));
                    if(!playButton.isEnabled()) {
                        playButton.setEnabled(true);
                        playButton.setBackgroundResource(R.mipmap.time_trial_text_extra_space);
                    }
                    playMarathonButton.setVisibility(View.GONE);
                    menuDistanceText.setVisibility(View.GONE);

                    if (bestTime.getTime() == 0L) {
                        menuTimeText.setText("Best Time: -:--:---\nHigh Score: " + String.format("%.2f", highScoreT));
                    }
                    else {
                        menuTimeText.setText("Best Time: " + bestTime.getTimeForDisplay() + "\nHigh Score: " + String.format("%.2f", highScoreT));
                    }

                    previousLocationButton.setVisibility(View.INVISIBLE);    //For the first location, this button should not be seen.
                    nextLocationButton.setVisibility(View.VISIBLE);    //For the last location, this button should not be seen.
                    break;
                case 1:
                    menuText.setText(getResources().getString(R.string.loc1));
                    menuDistanceText.setVisibility(View.VISIBLE);
                    playMarathonButton.setVisibility(View.VISIBLE);

                    if (locationsUnlocked > 0) {
                        if(!playButton.isEnabled()) {
                            playButton.setEnabled(true);
                            playButton.setBackgroundResource(R.mipmap.time_trial_text_extra_space);
                        }

                        if (bestTime.getTime() == 0L) {
                            menuTimeText.setText("Best Time: -:--:---\nHigh Score: " + String.format("%.2f", highScoreT));
                            if(playMarathonButton.isEnabled()) {
                                playMarathonButton.setEnabled(false);
                                playMarathonButton.setBackgroundResource(R.mipmap.distance_text_locked);
                            }
                            menuDistanceText.setText(getResources().getString(R.string.loc1MarathonLocked));
                        } else if (bestTime.getSeconds() > 24) {
                            menuTimeText.setText("Best Time: " + bestTime.getTimeForDisplay() + "\nHigh Score: " + String.format("%.2f", highScoreT));
                            if(playMarathonButton.isEnabled()) {
                                playMarathonButton.setEnabled(false);
                                playMarathonButton.setBackgroundResource(R.mipmap.distance_text_locked);
                            }
                            menuDistanceText.setText(getResources().getString(R.string.loc1MarathonLocked));
                        } else {
                            menuTimeText.setText("Best Time: " + bestTime.getTimeForDisplay() + "\nHigh Score: " + String.format("%.2f", highScoreT));
                            if(!playMarathonButton.isEnabled()) {
                                playMarathonButton.setEnabled(true);
                                playMarathonButton.setBackgroundResource(R.mipmap.distance_text_extra_space);
                            }
                            bestDistance = loadBestDistance();
                            menuDistanceText.setText("Longest Run: " + String.format("%.0f", bestDistance) + "\nHigh Score: " + String.format("%.2f", highScoreD));
                        }

                    } else {
                        if(playButton.isEnabled()) {
                            playButton.setEnabled(false);
                            playButton.setBackgroundResource(R.mipmap.time_trial_text_locked);
                        }
                        if(playMarathonButton.isEnabled()) {
                            playMarathonButton.setEnabled(false);
                            playMarathonButton.setBackgroundResource(R.mipmap.distance_text_locked);
                        }
                        menuTimeText.setText("LOCKED: 75 coins required.");
                        menuDistanceText.setText(getResources().getString(R.string.loc1MarathonLocked));
                    }
                    previousLocationButton.setVisibility(View.VISIBLE);
                    nextLocationButton.setVisibility(View.VISIBLE);
                    break;
                case 2:
//                    timeTrial2Flag = false;
                    menuText.setText(getResources().getString(R.string.loc2));
                    menuDistanceText.setVisibility(View.VISIBLE);

                    if (locationsUnlocked > 1) {
                        if(!playButton.isEnabled()) {
                            playButton.setEnabled(true);
                            playButton.setBackgroundResource(R.mipmap.time_trial_text_extra_space);
                        }

                        if (bestTime.getTime() == 0L) {
                            menuTimeText.setText("Best Time: -:--:---\nHigh Score: " + String.format("%.2f", highScoreT));
                            if(playMarathonButton.isEnabled()) {
                                playMarathonButton.setEnabled(false);
                                playMarathonButton.setBackgroundResource(R.mipmap.distance_text_locked);
                            }
                            menuDistanceText.setText(getResources().getString(R.string.loc2MarathonLocked));
                        } else if (bestTime.getSeconds() > 24) {
                            menuTimeText.setText("Best Time: " + bestTime.getTimeForDisplay() + "\nHigh Score: " + String.format("%.2f", highScoreT));
                            if(playMarathonButton.isEnabled()) {
                                playMarathonButton.setEnabled(false);
                                playMarathonButton.setBackgroundResource(R.mipmap.distance_text_locked);
                            }
                            menuDistanceText.setText(getResources().getString(R.string.loc2MarathonLocked));
                        } else {
                            menuTimeText.setText("Best Time: " + bestTime.getTimeForDisplay() + "\nHigh Score: " + String.format("%.2f", highScoreT));
                            if(!playMarathonButton.isEnabled()) {
                                playMarathonButton.setEnabled(true);
                                playMarathonButton.setBackgroundResource(R.mipmap.distance_text_extra_space);
                            }
                            bestDistance = loadBestDistance();
                            menuDistanceText.setText("Longest Run: " + String.format("%.0f", bestDistance) + "\nHigh Score: " + String.format("%.2f", highScoreD));
                        }

                    } else {
                        if(playButton.isEnabled()) {
                            playButton.setEnabled(false);
                            playButton.setBackgroundResource(R.mipmap.time_trial_text_locked);
                        }
                        if(playMarathonButton.isEnabled()) {
                            playMarathonButton.setEnabled(false);
                            playMarathonButton.setBackgroundResource(R.mipmap.distance_text_locked);
                        }
                        menuTimeText.setText("LOCKED: 400 coins required.");
                        menuDistanceText.setText(getResources().getString(R.string.loc2MarathonLocked));
                    }
                    previousLocationButton.setVisibility(View.VISIBLE);
                    nextLocationButton.setVisibility(View.VISIBLE);    //For the last location, this button should not be seen.
                    break;
                case 3:
//                    timeTrial2Flag = false;
                    //TODO: display xp somewhere on menu
                    menuText.setText(getResources().getString(R.string.loc3));
                    menuDistanceText.setVisibility(View.VISIBLE);

                    if (locationsUnlocked > 2) {
                        if(!playButton.isEnabled()) {
                            playButton.setEnabled(true);
                            playButton.setBackgroundResource(R.mipmap.time_trial_text_extra_space);
                        }

                        if (bestTime.getTime() == 0L) {
                            menuTimeText.setText("Best Time: -:--:---\nHigh Score: \n" + String.format("%.2f", highScoreT));
                            if(playMarathonButton.isEnabled()) {
                                playMarathonButton.setEnabled(false);
                                playMarathonButton.setBackgroundResource(R.mipmap.distance_text_locked);
                            }
                            menuDistanceText.setText(getResources().getString(R.string.loc3MarathonLocked));
                        } else if (bestTime.getSeconds() > 24) {
                            menuTimeText.setText("Best Time: " + bestTime.getTimeForDisplay() + "\nHigh Score: " + String.format("%.2f", highScoreT));
                            if(playMarathonButton.isEnabled()) {
                                playMarathonButton.setEnabled(false);
                                playMarathonButton.setBackgroundResource(R.mipmap.distance_text_locked);
                            }
                            menuDistanceText.setText(getResources().getString(R.string.loc3MarathonLocked));
                        } else {
                            menuTimeText.setText("Best Time: " + bestTime.getTimeForDisplay() + "\nHigh Score: " + String.format("%.2f", highScoreT));
                            if(!playMarathonButton.isEnabled()) {
                                playMarathonButton.setEnabled(true);
                                playMarathonButton.setBackgroundResource(R.mipmap.distance_text_extra_space);
                            }
                            bestDistance = loadBestDistance();
                            menuDistanceText.setText("Longest Run: " + String.format("%.0f", bestDistance) + "\nHigh Score: " + String.format("%.2f", highScoreD));
                        }

                    } else {
                        if(playButton.isEnabled()) {
                            playButton.setEnabled(false);
                            playButton.setBackgroundResource(R.mipmap.time_trial_text_locked);
                        }
                        if(playMarathonButton.isEnabled()) {
                            playMarathonButton.setEnabled(false);
                            playMarathonButton.setBackgroundResource(R.mipmap.distance_text_locked);
                        }
                        menuTimeText.setText("LOCKED: 1000 coins required.");
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
                locationRes = R.mipmap.background_00;
                break;
            case 1:
                locationRes = R.mipmap.background_1;
                break;
            case 2:
                locationRes = R.mipmap.background_2;
                break;
            case 3:
                locationRes = R.mipmap.background_3;
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

            alternateTitleScreen.animate()
                    .translationX(windowSize.x)
                    .setDuration(500);

            updateLoc(loc);

            titleScreen.animate()
                    .translationX(0)
                    .setDuration(500);
        } else if (loc > prevLoc) {
            alternateTitleScreen = findViewById(R.id.titleScreen);
            alternateTitleScreen.setBackground(titleScreen.getBackground());
            alternateTitleScreen.setTranslationX(0);
            titleScreen = findViewById(R.id.titleScreenAlternate);
            titleScreen.setBackgroundResource(locationRes);
            titleScreen.setTranslationX(windowSize.x);
            titleScreen.setVisibility(View.VISIBLE);

            alternateTitleScreen.animate()
                    .translationX(-windowSize.x)
                    .setDuration(500);
            updateLoc(loc);
            titleScreen.animate()
                    .translationX(0)
                    .setDuration(500);
        }
    }


    private String checkStagesUnlocked(){
        String s = "";
        if(runnerCoins > 74 && locationsUnlocked < 1){
            s += "\nStage 1 unlocked!";
            locationsUnlocked++;
            saveLocationsUnlocked(locationsUnlocked);
            endGameText.setBackgroundResource(R.drawable.rectangle_white_border_yellow);
        }
        if(runnerCoins > 399 && locationsUnlocked < 2){
            s += "\nStage 2 unlocked!";
            locationsUnlocked++;
            saveLocationsUnlocked(locationsUnlocked);
            endGameText.setBackgroundResource(R.drawable.rectangle_white_border_yellow);
        }
        if(runnerCoins > 999 && locationsUnlocked < 3){
            s += "\nStage 3 unlocked!";
            locationsUnlocked++;
            saveLocationsUnlocked(locationsUnlocked);
            endGameText.setBackgroundResource(R.drawable.rectangle_white_border_yellow);
        }
        return s;
    }

    private void saveNewBestTime(long newBestTime){
        switch (locationState){
            case 0:
                bestT0 = newBestTime;
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
            s = "bestTime" + String.valueOf(locationState);
        prefEditor.putLong(s, newBestTime);
        prefEditor.commit();
    }

    private Long loadBestTime (){
        long time = -1;
        switch (locationState){
            case 0:
                time = bestT0;
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

        if(time < 0) {
            String s;
            s = "bestTime" + String.valueOf(locationState);
            time = sharedPref.getLong(s, 0L);

            switch (locationState){
                case 0:
                    bestT0 = time;
//                }
                    break;
                case 1:
                    bestT1 = time;
                    break;
                case 2:
                    bestT2 = time;
                    break;
                case 3:
                    bestT3 = time;
                    break;
            }
        }
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
        if(distance < 0) {
            String s = "bestDistance" + String.valueOf(locationState);
            distance = sharedPref.getFloat(s, 0f);

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
        s = "highScoreT" + String.valueOf(locationState);
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
        if(score < 0) {
            String s = "highScoreT" + String.valueOf(locationState);
            score = sharedPref.getFloat(s, 0f);

            switch (locationState){
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
        String s = "highScoreD" + String.valueOf(locationState);
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
        if(score < 0) {
            String s = "highScoreD" + String.valueOf(locationState);
            score = sharedPref.getFloat(s, 0f);

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
        }
        return score;
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

    private void addRunnerCoins(int coins){
        runnerCoins += coins;
        prefEditor.putInt("coins", runnerCoins);
        prefEditor.commit();
    }

    private int loadRunnerCoins(){
        runnerCoins = sharedPref.getInt("coins", 0);
        return runnerCoins;
    }

    private void saveStats(){
        prefEditor.putFloat("distPerSec", distPerSec);
        prefEditor.putFloat("coinsPerSec", coinsPerSec);
        prefEditor.putFloat("distPerStep", distPerStep);
        prefEditor.putFloat("secPerLife", secPerLife);
        prefEditor.putInt("numRuns" , numRuns);
        prefEditor.commit();
    }

    private void loadStats(){
        distPerSec = sharedPref.getFloat("distPerSec", 0);
        coinsPerSec  = sharedPref.getFloat("coinsPerSec", 0);
        distPerStep = sharedPref.getFloat("distPerStep", 0);
        secPerLife = sharedPref.getFloat("secPerLife", 0);
        numRuns = sharedPref.getInt("numRuns", 0);
    }

}
