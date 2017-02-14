package com.runrmby.runner;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
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



    private MediaPlayer activeMusic;
    private static final int MENU_MUSIC = 0;
    private static final int GAME_MUSIC_1 = 1;
    private static final int LOSE_MUSIC_1 = 2;
    private static final int WIN_MUSIC_1 = 3;
    private static final int WIN_MUSIC_NEW_RECORD = 4;
    private int nowPlaying;
    boolean musicMuted;
    boolean musicPausedByLeavingApp;
    //float volume;

    private Button playButton;
    private Button tempButton;
    private Button playAgainButton;
    private Button mainMenuButton;
    private Button resumeButton;
    private Button pauseMMButton;

    private TextView endGameUserTime;
    private TextView endGameBestTime;
    private TextView endGameText;
    public TextView timer;

    private RelativeLayout gameEndMenu;
    private RelativeLayout pauseMenu;
    private FrameLayout mainMenu;
    private FrameLayout gameMenu;
    private FrameLayout root;

    private View titleScreen;
    private GameView gameScreen;

    private Point windowSize = new Point(1000, 2000); // arbitrary values

    private int gameState = MAIN_MENU;
    private int previousGameState = MAIN_MENU;


    //private File bestTimeFilePath;
    private Long savedTime;
    private Time bestTime = new Time();
    private Time timeDifferential = new Time();
    private Time yourTime;

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
        mainMenu = (FrameLayout) findViewById(R.id.mainMenu);
        gameMenu = (FrameLayout) findViewById(R.id.gameMenu);
        gameEndMenu = (RelativeLayout) findViewById(R.id.gameEndMenu);
        pauseMenu = (RelativeLayout) findViewById(R.id.pauseMenu);

        timer = (TextView) findViewById(R.id.timer);

        //Screens
        titleScreen = findViewById(R.id.titleScreen);
        gameScreen = new GameView(this, windowSize);

        root.addView(gameScreen);

        gameScreen.setVisibility(View.VISIBLE);
        gameScreen.setTranslationY(windowSize.y);

        //Music
        musicMuted = sharedPref.getBoolean("musicMuted", false);


        setGameState2(MAIN_MENU);

        endGameUserTime = (TextView) findViewById(R.id.endGameUserTime);
        endGameBestTime = (TextView) findViewById(R.id.endGameBestTime);
        endGameText = (TextView) findViewById(R.id.endGameText);

        playAgainButton = (Button) findViewById(R.id.playAgainButton);
        playAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGameState2(GAME_INITIALIZING);
            }
        });

        mainMenuButton = (Button) findViewById(R.id.mainMenuButton);
        mainMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGameState2(TRANSIT_TO_MM);
            }
        });


        playButton = (Button) findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGameState2(TRANSIT_TO_GAME);
            }
        });

        tempButton = (Button) findViewById(R.id.tempButton);
        tempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGameState2(PAUSED);
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
                setGameState2(TRANSIT_TO_MM);
            }
        });

        resumeButton = (Button) findViewById(R.id.resumeButton);
        resumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGameState2(GAME_PLAYING);
            }
        });

    }

    public void setGameState2(int state) {
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
        }
    }

    public void setMenuState() {
        mainMenu.setVisibility(View.VISIBLE);
        //Start win music.
        setMusicState(R.raw.finger_runner_main_menu, MENU_MUSIC, true);

    }

    public void setGameInitState() {
        gameScreen.resetVariables();
        gameScreen.resumeGame();
        setGameState2(GAME_PLAYING);
        //Start music.
        setMusicState(R.raw.finger_runner_game_music_1, GAME_MUSIC_1, true);
    }

    public void setGamePlayingState() {
        gameMenu.setVisibility(View.VISIBLE);
        gameScreen.handleTouches = true;
    }

    public void setGameWonState() {
        gameScreen.pauseGame();
        gameEndMenu.setVisibility(View.VISIBLE);
        root.removeView(gameEndMenu);
        root.addView(gameEndMenu);

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
                endGameUserTime.setText("Your Time: \n" + yourTime.getTimeForDisplay());
                endGameBestTime.setText("Previous Best: \n" + bestTime.getTimeForDisplay());
                bestTime.changeTime(yourTime.getTime());

                //Start win music.
                setMusicState(R.raw.finger_runner_win_new_record, WIN_MUSIC_NEW_RECORD, false);

            } else {
                //Your time isn't a new best time.
                endGameText.setText(R.string.win3);
                endGameUserTime.setText("Your Time: \n" + yourTime.getTimeForDisplay());
                endGameBestTime.setText("Best Time: \n" + bestTime.getTimeForDisplay());

                //Start win music.
                setMusicState(R.raw.finger_runner_win_new_record, WIN_MUSIC_1, false);
            }
        } else {
            //No best time has been saved (a run has never been completed), so your time is a new best time.
            saveNewBestTime(yourTime.getTime());
            bestTime.changeTime(yourTime.getTime());
            endGameText.setText(R.string.win2);
            endGameUserTime.setText("Your Time: \n" + yourTime.getTimeForDisplay());
            endGameBestTime.setText("Previous Best: \n" + "You'd never finished!");

            //Start win music.
            setMusicState(R.raw.finger_runner_win_new_record, WIN_MUSIC_NEW_RECORD, false);
        }
    }

    public void setGameLostState() {
        gameScreen.pauseGame();
        gameEndMenu.setVisibility(View.VISIBLE);
        root.removeView(gameEndMenu);
        root.addView(gameEndMenu);


        //Start lose music with
        setMusicState(R.raw.finger_runner_lose_1, LOSE_MUSIC_1, false);

        //Set end game textviews for losing.
        endGameUserTime.setText("Your Time: \nYou didn't finish!");
        savedTime = loadBestTime();
        if(savedTime != 0) {
            //There was a saved best time.
            bestTime.changeTime(savedTime);
            endGameBestTime.setText("Best Time: \n" + bestTime.getTimeForDisplay());
            endGameText.setText(R.string.lose2);
        }else {
            //The loaded best time was null (there has never been a best time saved).
            endGameBestTime.setText("Best Time: \n" + "You've never finished!");
            endGameText.setText(R.string.lose1);
        }

    }

    // Game paused. Not app paused.
    public void setPausedState() {
        gameScreen.pauseGame();

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
                        setGameState2(MAIN_MENU);
                    }
                });

        //Move this into music state machine
        //TODO: Fade out game music during transition? The commented-out section works, but it appears to delay the transition, and the fade-out time varies based on device speed.
        if(activeMusic!= null) {
//            for (int i = 1000; i >= 0; i--) {
//                volume = i / 1000f;
//                gameMusic1.setVolume(volume, volume);
//            }
            //Stop game music.
            stopMusic();
        }
    }

    public void setTransitGameState() {
//        gameScreen.update();
//        gameScreen.draw();

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
                        setGameState2(GAME_INITIALIZING);
                    }
                });

        //Make button and timer visible.
        root.removeView(gameMenu);
        root.addView(gameMenu);

        //TODO: Fade out menu music during transition? The commented-out section works, but it appears to delay the transition, and the fade-out time varies based on device speed.
        // Probably needs a handler to run on a different thread
        // for i sticks the programming here until it is done then moves on
        if(activeMusic != null) {
//            for (int i = 1000; i >= 0; i--) {
//                volume = i / 1000f;
//                menuMusic.setVolume(volume, volume);
//            }
            //Stop menu music.
            stopMusic();
        }
    }

    // id should be from R.raw
    public void setMusicState(int id, int musicState, boolean loop) {
        stopMusic();

        activeMusic = MediaPlayer.create(this, id);
        activeMusic.setLooping(loop);
        if(!musicMuted) {
            activeMusic.start();
        }

        nowPlaying = musicState;
    }

    public void stopMusic() {
        if(activeMusic != null) {
            if (activeMusic.isPlaying()) {
                activeMusic.stop();
            }
            activeMusic.release();
            activeMusic = null;
        }
    }


    private void hideAllMenus() {
        mainMenu.setVisibility(View.GONE);
        gameMenu.setVisibility(View.GONE);
        gameEndMenu.setVisibility(View.GONE);
        pauseMenu.setVisibility(View.GONE);
    }

    //TODO: Start music function.
//    public void startMusic(int musicToPlay){
//        switch(musicToPlay) {
//            case MENU_MUSIC:
//                stopMusic(nowPlaying);
//                nowPlaying = MENU_MUSIC;
//                menuMusic = MediaPlayer.create(this, R.raw.finger_runner_main_menu);
//                menuMusic.setLooping(true);
//                if(!musicMuted){
//                    menuMusic.start();
//                }
//                break;
//
//            case GAME_MUSIC_1:
//        }
//    }

    //TODO: Pause music function.
//    public void pauseMusic(int musicToPause){
//        switch(musicToPause) {
//            case MENU_MUSIC:
//                //
//                break;
//            case GAME_MUSIC_1:
//        }
//    }

    //TODO: Stop music function.
//    public void stopMusic(int musicToStop){
//        switch(musicToStop) {
//            case MENU_MUSIC:
//                //
//                break;
//            case GAME_MUSIC_1:
//        }
//    }

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
        //If music is playing, pause upon leaving the app.
        if(activeMusic != null) {
            if(activeMusic.isPlaying()) {
                activeMusic.pause();
                musicPausedByLeavingApp = true;
            }
        }

        if(gameState == GAME_PLAYING){
            setGameState2(PAUSED);
        }
        gameScreen.pause();
    }

    /**
     *Actions to take when app is resumed.
     */
    @Override
    protected void onResume(){
        super.onResume();
        hide();
        //If music was paused upon leaving the app, resume playing the music.
        if(musicPausedByLeavingApp){
            activeMusic.start();
            musicPausedByLeavingApp = false;
        }

        //If not on game over screen, resume gameScreen.
//        if(gameEndMenu.getVisibility() == View.GONE) {
//            gameScreen.resume();
//        }

        gameScreen.resume();

        if(gameState == PAUSED){
            setGameState2(GAME_PLAYING);
            gameScreen.pauseGame();
            pauseMenu.setVisibility(View.VISIBLE);
            root.removeView(pauseMenu);
            root.addView(pauseMenu);
            //gameState = PAUSED;
            //setGameState2(PAUSED); //TODO: Resuming to PAUSED state doesn't seem to work.
        } else {
            setGameState2(gameState);//setGameState2(previousGameState);
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
}
