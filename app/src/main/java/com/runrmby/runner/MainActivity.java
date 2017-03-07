package com.runrmby.runner;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaPlayer;
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

import java.io.File;

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



    private MediaPlayer activeMusic;
    private static final int MENU_MUSIC = 0;
    private static final int GAME_MUSIC_1 = 1;
    private static final int LOSE_MUSIC_1 = 2;
    private static final int WIN_MUSIC_1 = 3;
    private static final int WIN_MUSIC_NEW_RECORD = 4;
    private int nowPlaying;
    boolean musicMuted;
    boolean musicPausedByLeavingApp;
    int activeMusicPosition;
    Handler handler = new Handler();
    Thread musicThread;

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

    private TextView endGameUserTime;
    private TextView endGameBestTime;
    private TextView endGameText;
    public TextView timer;

    private RelativeLayout gameEndMenu;
    private RelativeLayout pauseMenu;
    private RelativeLayout settingsMenu;
    private RelativeLayout mainMenu;
    private FrameLayout gameMenu;
    private FrameLayout root;

    private View titleScreen;
    private GameView gameScreen;

    private Point windowSize = new Point(1000, 2000); // arbitrary values

    private int gameState = MAIN_MENU;
    private int previousGameState = MAIN_MENU;

    private int menuState = 1;  //0=settings, 1=mainMenu, 2 would be next location


    //private File bestTimeFilePath;
    private Long savedTime;
    private Time bestTime = new Time();
    private Time timeDifferential = new Time();
    private Time yourTime;
    private float savedDistance;
    private float distanceDifferential;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);



        sharedPref = getSharedPreferences("Runner", MODE_PRIVATE);
        prefEditor = sharedPref.edit();
//        bestTimeFilePath = new File(this.getFilesDir(), "best_time");
//        if(!bestTimeFilePath.exists()) {
//            bestTimeFilePath.mkdir();
//        }
        //bestTimeFilePath.delete(); //Deletes best time on start for testing.



        root = (FrameLayout) findViewById(R.id.root);

        // Menus
        mainMenu = (RelativeLayout) findViewById(R.id.mainMenu);
        gameMenu = (FrameLayout) findViewById(R.id.gameMenu);
        gameEndMenu = (RelativeLayout) findViewById(R.id.gameEndMenu);
        pauseMenu = (RelativeLayout) findViewById(R.id.pauseMenu);
        settingsMenu = (RelativeLayout) findViewById(R.id.settingsMenu);

        timer = (TextView) findViewById(R.id.timer);

        //Screens
        titleScreen = findViewById(R.id.titleScreen);
        gameScreen = new GameView(this, windowSize);

        titleScreen.setOnTouchListener(new OnSwipeTouchListener(this){  //Swipe options.
            public void onSwipeTop() {
                Toast.makeText(root.getContext(), "top", Toast.LENGTH_SHORT).show();
            }
            public void onSwipeRight() {
                Toast.makeText(root.getContext(), "right", Toast.LENGTH_SHORT).show();
            }
            public void onSwipeLeft() {
                Toast.makeText(root.getContext(), "left", Toast.LENGTH_SHORT).show();
//                if(settingsMenu.getVisibility() == View.VISIBLE){
//                }
//                else{
//                    Toast.makeText(root.getContext(), "left", Toast.LENGTH_SHORT).show();
//                }
            }
            public void onSwipeBottom() {
                Toast.makeText(root.getContext(), "bottom", Toast.LENGTH_SHORT).show();
            }

        });

        root.addView(gameScreen);

        gameScreen.setVisibility(View.VISIBLE);
        gameScreen.setTranslationY(windowSize.y);
        gameScreen.setBackgroundColor(0x00000000); // Wish there was a way to get this from colors.xml

        //Music
        musicMuted = sharedPref.getBoolean("musicMuted", false);


        setGameState(MAIN_MENU);

        //Text Views
        endGameUserTime = (TextView) findViewById(R.id.endGameUserTime);
        endGameBestTime = (TextView) findViewById(R.id.endGameBestTime);
        endGameText = (TextView) findViewById(R.id.endGameText);

        //Buttons
        playAgainButton = (Button) findViewById(R.id.playAgainButton);
        playAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGameState(GAME_INITIALIZING);
            }
        });

        mainMenuButton = (Button) findViewById(R.id.mainMenuButton);
        mainMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGameState(TRANSIT_TO_MM);
            }
        });


        playButton = (Button) findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameScreen.distanceMode = false;
                setGameState(TRANSIT_TO_GAME);
            }
        });

        playMarathonButton = (Button) findViewById(R.id.playDistanceMode);
        playMarathonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameScreen.distanceMode = true;
                setGameState(TRANSIT_TO_GAME);
            }
        });

        settingsButton = (Button) findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                setGameState(SETTINGS);
//                hideAllMenus();
//                settingsMenu.setVisibility(View.VISIBLE);
            }
        });

        exitSettingsButton = (Button) findViewById(R.id.exitSettingsButton);
        exitSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setGameState(MAIN_MENU);
//                hideAllMenus();
//                mainMenu.setVisibility(View.VISIBLE);
            }
        });

        tempButton = (Button) findViewById(R.id.tempButton);
        tempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGameState(PAUSED);
            }
        });

        resetButton = (Button) findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Reset best time.
                AlertDialog.Builder resetWarning = new AlertDialog.Builder(root.getContext());//, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                resetWarning.setMessage("Are you sure you want to reset?");
                resetWarning.setCancelable(true);
                resetWarning.setPositiveButton(
                        "Yes, please.",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                prefEditor.putLong("bestTime", 0l);
                                prefEditor.putFloat("bestDistance", 0f);
                                prefEditor.commit();
                                Toast.makeText(root.getContext(), "Records reset.", Toast.LENGTH_SHORT).show();//TODO: make sure this can't cause a crash
                                dialog.dismiss();
                            }
                        });

                resetWarning.setNegativeButton(
                        "Oops, nooo!",
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

        //Mute music toggle button.
        final ToggleButton musicMuteButton = (ToggleButton)this.findViewById(R.id.mute_music_button);
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
        pauseMMButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGameState(TRANSIT_TO_MM);
            }
        });

        resumeButton = (Button) findViewById(R.id.resumeButton);
        resumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGameState(GAME_PLAYING);
            }
        });

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
        int colWit = gameScreen.collisionsWitnessed + loadCollisionsWitnessed();

        // TODO Shrink this or at least separate it into its functional parts
        yourTime = gameScreen.gameTimer;
        savedTime = loadBestTime();
        if(savedTime != 0) {
            //A best time exists.
            bestTime.changeTime(savedTime); //Put best time into Time class.
            if (yourTime.getTime() < bestTime.getTime()) {
                //Your time is a new best time.
                timeDifferential.changeTime(bestTime.getTime() - yourTime.getTime());
                saveNewBestTime(yourTime.getTime());
                endGameText.setText(getResources().getString(R.string.win1) + "\nYou beat the record by\n" + timeDifferential.getTimeForDisplay() + "!");
                endGameUserTime.setText("Your Time: \n" + yourTime.getTimeForDisplay() + "\nCollisions:\n" + String.valueOf(gameScreen.collisionsWitnessed));
                endGameBestTime.setText("Previous Best: \n" + bestTime.getTimeForDisplay() + "\nCollisions Witnessed:\n" + String.valueOf(colWit));
                bestTime.changeTime(yourTime.getTime());
                endGameBestTime.setBackgroundColor(Color.GREEN);
                endGameUserTime.setBackgroundColor(Color.GREEN);
                endGameText.setBackgroundColor(Color.GREEN);

                //Start win music.
//                if(nowPlaying != WIN_MUSIC_NEW_RECORD) {
                    setMusicState(R.raw.finger_runner_win_new_record, WIN_MUSIC_NEW_RECORD, false);
//                }

            } else {
                //Your time isn't a new best time.
                endGameText.setText(R.string.win3);
                endGameUserTime.setText("Your Time: \n" + yourTime.getTimeForDisplay() + "\nCollisions:\n" + String.valueOf(gameScreen.collisionsWitnessed));
                endGameBestTime.setText("Best Time: \n" + bestTime.getTimeForDisplay() + "\nCollisions Witnessed:\n" + String.valueOf(colWit));
                endGameBestTime.setBackgroundColor(Color.LTGRAY);
                endGameUserTime.setBackgroundColor(Color.LTGRAY);
                endGameText.setBackgroundColor(Color.LTGRAY);

                //Start win music.
//                if(nowPlaying != WIN_MUSIC_1) {
                    setMusicState(R.raw.finger_runner_win_1, WIN_MUSIC_1, false);
//                }
            }
        } else {
            //No best time has been saved (a run has never been completed), so your time is a new best time.
            saveNewBestTime(yourTime.getTime());
            bestTime.changeTime(yourTime.getTime());
            endGameText.setText(R.string.win2);
            endGameUserTime.setText("Your Time: \n" + yourTime.getTimeForDisplay() + "\nCollisions:\n" + String.valueOf(gameScreen.collisionsWitnessed));
            endGameBestTime.setText("Previous Best: \n" + "You'd never finished!" + "\nCollisions Witnessed:\n" + String.valueOf(colWit));
            endGameBestTime.setBackgroundColor(Color.LTGRAY);
            endGameUserTime.setBackgroundColor(Color.GREEN);
            endGameText.setBackgroundColor(Color.GREEN);

            //Start win music.
//            if(nowPlaying != WIN_MUSIC_NEW_RECORD) {
                setMusicState(R.raw.finger_runner_win_new_record, WIN_MUSIC_NEW_RECORD, false);
//            }
        }
    }

    public void setGameLostState() {
        gameScreen.pauseGame();
        gameEndMenu.setVisibility(View.VISIBLE);
        root.removeView(gameEndMenu);
        root.addView(gameEndMenu);
        endGameUserTime.setBackgroundColor(Color.RED);
        endGameText.setBackgroundColor(Color.RED);
        int colWit = gameScreen.collisionsWitnessed + loadCollisionsWitnessed();
        saveCollisionsWitnessed(colWit);

        //Set end game textviews for losing.
        if(gameScreen.distanceMode == false) {
            //Start lose music.
//        if(nowPlaying != LOSE_MUSIC_1) {
//            setMusicState(R.raw.finger_runner_lose_1, LOSE_MUSIC_1, false);
//        }
            endGameUserTime.setText("Your Time: \nYou didn't finish!");
            savedTime = loadBestTime();
            if (savedTime != 0) {
                //There was a saved best time.
                bestTime.changeTime(savedTime);
                endGameBestTime.setText("Best Time: \n" + bestTime.getTimeForDisplay() + "\nCollisions Witnessed:\n" + String.valueOf(colWit));
                endGameText.setText(R.string.lose2);
                endGameBestTime.setBackgroundColor(Color.LTGRAY);
            } else {
                //The loaded best time was null (there has never been a best time saved).
                endGameBestTime.setText("Best Time: \n" + "You've never finished!");
                endGameText.setText(R.string.lose1);
                endGameBestTime.setBackgroundColor(Color.RED);
            }
        } else{
            //TODO: Not sure if a new state would be necessary for distance mode.
            savedDistance = loadBestDistance();
            float yourDistance = gameScreen.odometer + gameScreen.tFY;
            distanceDifferential = yourDistance - savedDistance;
            if(distanceDifferential > 0){
                //New record!
                endGameUserTime.setText("New Record: \n" + String.valueOf(yourDistance) + "\nCollisions:\n" + String.valueOf(gameScreen.collisionsWitnessed));
                endGameBestTime.setText("Previous Record: \n" + String.valueOf(savedDistance) + "\nCollisions Witnessed:\n" + String.valueOf(colWit));
                endGameText.setText(getResources().getString(R.string.win1) + "\nYou beat the record by\n" + String.valueOf(distanceDifferential) + "!");
                setMusicState(R.raw.finger_runner_win_new_record, WIN_MUSIC_NEW_RECORD, false);
                saveBestDistance(yourDistance);
                endGameBestTime.setBackgroundColor(Color.LTGRAY);
                endGameUserTime.setBackgroundColor(Color.GREEN);
                endGameText.setBackgroundColor(Color.GREEN);
            } else {
                endGameUserTime.setText("Your Distance: \n" + String.valueOf(yourDistance) + "\nCollisions:\n" + String.valueOf(gameScreen.collisionsWitnessed));
                endGameBestTime.setText("Record: \n" + String.valueOf(savedDistance) + "\nCollisions Witnessed:\n" + String.valueOf(colWit));
                endGameText.setText(R.string.lose2);
                endGameBestTime.setBackgroundColor(Color.LTGRAY);
                endGameUserTime.setBackgroundColor(Color.LTGRAY);
                endGameText.setBackgroundColor(Color.LTGRAY);
                //Keep game music going.
                setMusicState(R.raw.finger_runner_game_music_1, GAME_MUSIC_1, true);
            }
        }

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
        settingsMenu.setVisibility(View.VISIBLE);
    }

    // id should be from R.raw
    public void setMusicState(final int id, final int musicState, final boolean loop) {
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
            musicThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    stopMusic();

                    activeMusic = MediaPlayer.create(root.getContext(), id);
                    activeMusic.setLooping(loop);
                    //If music was killed by leaving the app, resume at previous position.
                    if (musicState == nowPlaying && activeMusicPosition > 0) {
                        activeMusic.seekTo(activeMusicPosition);
                    }
                    activeMusicPosition = 0;
                    if (!musicMuted) {
                        activeMusic.start();
                    }

                    nowPlaying = musicState;
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

    public void fadeoutMusic(){
        if(activeMusic != null) {
            musicThread = new Thread((new Runnable() {
                @Override
                public void run() {
                    float volume;
                    for(int i = 100; i >= 0; i--){
                        volume = i / 100f;
                        final float v = volume;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(activeMusic != null) {
                                    activeMusic.setVolume(v, v);
                                }
                            }
                        });
                        try {
                            musicThread.sleep(5);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }));
            musicThread.start();
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

        } else {
            setGameState(gameState);//setGameState(previousGameState);
        }
    }

    private void saveNewBestTime(long newBestTime){
        prefEditor.putLong("bestTime", newBestTime);
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
        Long time = sharedPref.getLong("bestTime", 0l);
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
        prefEditor.putFloat("bestDistance", distance);
        prefEditor.commit();
    }

    private float loadBestDistance(){
        float distance = sharedPref.getFloat("bestDistance", 0f);
        return distance;
    }

    private void saveCollisionsWitnessed(int c){
        prefEditor.putInt("collisionsWitnessed", c);
        prefEditor.commit();
    }

    private int loadCollisionsWitnessed(){
        int c = sharedPref.getInt("collisionsWitnessed", 0);
        return c;
    }
}
