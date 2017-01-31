package com.runrmby.runner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ToggleButton;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * Ben is testing too
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
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
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

    private static final int NONE = 0;
    private static final int PLAYING_GAME = 1;
    private static final int LOSE = 2;
    private static final int WIN = 3;
    private static final int MAIN_MENU = 4;


    MediaPlayer mediaPlayer = new MediaPlayer();
    boolean musicPausedByLeavingApp;

    private Button playButton;
    private Button tempButton;

    private FrameLayout loseMenu;
    private FrameLayout mainMenu;
    private FrameLayout winMenu;
    private FrameLayout gameMenu;

    private View titleScreen;
    private View gameScreen;

    private Point windowSize = new Point();


    private int gameState = MAIN_MENU;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        //----------------------- Game Code ---------------------------

        //Play looping theme music.
        mediaPlayer = MediaPlayer.create(this, R.raw.finger_runner_theme_swing_beat_version_1);
        mediaPlayer.setLooping(true);
        //if(!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        //}
        //Play/pause button listener. If music is playing when button is pressed, pause music. Otherwise play the music.
        final ToggleButton pauseMusicButton = (ToggleButton)this.findViewById(R.id.pause_music_button);
        pauseMusicButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
                else mediaPlayer.start();
            }
        });

        this.getWindowManager().getDefaultDisplay().getSize(windowSize);
        System.out.println("MA| windowSize: " + windowSize.x + ", " + windowSize.y);

        // Menus
        mainMenu = (FrameLayout) findViewById(R.id.mainMenu);
        gameMenu = (FrameLayout) findViewById(R.id.gameMenu);
        loseMenu = (FrameLayout) findViewById(R.id.loseMenu);
        winMenu = (FrameLayout) findViewById(R.id.winMenu);

        //Screens
        titleScreen = findViewById(R.id.titleScreen);
        gameScreen = findViewById(R.id.gameScreen);

        gameScreen.setVisibility(View.VISIBLE);
        gameScreen.setTranslationY(windowSize.y);


        setGameState(MAIN_MENU);


        playButton = (Button) findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("MA| here");
                requestGameState(PLAYING_GAME);
            }
        });

        tempButton = (Button) findViewById(R.id.tempButton);
        tempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestGameState(MAIN_MENU);
            }
        });
    }

    private void requestGameState(int state) {
        hideAllMenus();
        switch (state) {
            case MAIN_MENU:
                transitionToMainMenu();
                break;
            case PLAYING_GAME:
                transitionToGame();
                break;
            case LOSE:

                break;
            case WIN:

                break;
            case NONE:
                // Nothing happens here
                break;
        }
    }

    private void setGameState(int state) {
        gameState = state;
        switch (state) {
            case MAIN_MENU:
                mainMenu.setVisibility(View.VISIBLE);
                break;
            case PLAYING_GAME:
                gameMenu.setVisibility(View.VISIBLE);
                break;
            case LOSE:
                loseMenu.setVisibility(View.VISIBLE);
                break;
            case WIN:
                winMenu.setVisibility(View.VISIBLE);
                break;
            case NONE:
                // Nothing happens here
                break;
        }
    }

    private void transitionToMainMenu() {
        gameScreen.animate()
                .translationYBy(windowSize.y)
                .setDuration(1000);

        titleScreen.animate()
                .translationYBy(windowSize.y)
                .setDuration(1000)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        setGameState(MAIN_MENU);
                    }
                });
    }

    private void transitionToGame() {
        gameScreen.animate()
                .translationYBy(-windowSize.y)
                .setDuration(1000);

        titleScreen.animate()
                .translationYBy(-windowSize.y)
                .setDuration(1000)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        setGameState(PLAYING_GAME);
                    }
                });
    }

    private void hideAllMenus() {
        mainMenu.setVisibility(View.GONE);
        gameMenu.setVisibility(View.GONE);
        loseMenu.setVisibility(View.GONE);
        winMenu.setVisibility(View.GONE);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
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
     *Pause music when app is in background.
     */
    @Override
    protected void onPause(){
        super.onPause();
        //If music is playing, pause upon leaving the app.
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            musicPausedByLeavingApp = true;
        }
    }

    /**
     *Resume music when app is resumed.
     */
    @Override
    protected void onResume(){
        super.onResume();
        //If music was paused upon leaving the app, resume playing the music.
        if(musicPausedByLeavingApp){
            mediaPlayer.start();
            musicPausedByLeavingApp = false;
        }
    }

}
