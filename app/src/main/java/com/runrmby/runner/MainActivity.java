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
                    if(gameState != PLAYING_GAME && gameState != LOSE && gameState != WIN) {
                        gameScreen.setBackgroundSizePos(windowSize);
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

    public static final int NONE = 0;
    public static final int PLAYING_GAME = 1;
    public static final int LOSE = 2;
    public static final int WIN = 3;
    public static final int MAIN_MENU = 4;
    public static final int PAUSE = 5;


    MediaPlayer menuMusic;
    MediaPlayer gameMusic1;
    MediaPlayer loseMusic1;
    MediaPlayer winMusic1;
    MediaPlayer winMusicNewRecord;
    private static final int MENU_MUSIC = 0;
    private static final int GAME_MUSIC_1 = 1;
    private static final int LOSE_MUSIC_1 = 2;
    private static final int WIN_MUSIC_1 = 3;
    private static final int WIN_MUSIC_NEW_RECORD = 4;
    private int nowPlaying;
    boolean musicMuted;
    boolean musicPausedByLeavingApp;
    float volume;

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


    private File bestTimeFilePath;
    private Long savedTime;
    private Time bestTime = new Time();
    private Time timeDifferential = new Time();
    private Time yourTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Testing vcs

        setContentView(R.layout.activity_main);

        //mVisible = true;
        //mControlsView = findViewById(R.id.fullscreen_content_controls);
        //mContentView = findViewById(R.id.fullscreen_content);

        // Set up the user interaction to manually show or hide the system UI.
//        mContentView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //toggle();
//            }
//        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        sharedPref = getSharedPreferences("Runner", MODE_PRIVATE);
        prefEditor = sharedPref.edit();
//        bestTimeFilePath = new File(this.getFilesDir(), "best_time");
//        if(!bestTimeFilePath.exists()) {
//            bestTimeFilePath.mkdir();
//        }
        //bestTimeFilePath.delete(); //Deletes best time on start for testing.

        //----------------------- Game Code ---------------------------

//        this.getWindowManager().getDefaultDisplay().getSize(windowSize);
//        System.out.println("MA| windowSize: " + windowSize.x + ", " + windowSize.y);


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


        requestGameState(MAIN_MENU);

        endGameUserTime = (TextView) findViewById(R.id.endGameUserTime);
        endGameBestTime = (TextView) findViewById(R.id.endGameBestTime);
        endGameText = (TextView) findViewById(R.id.endGameText);

        playAgainButton = (Button) findViewById(R.id.playAgainButton);
        playAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestGameState(PLAYING_GAME);
            }
        });

        mainMenuButton = (Button) findViewById(R.id.mainMenuButton);
        mainMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestGameState(MAIN_MENU);
            }
        });


        playButton = (Button) findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        //Play/pause button listener. If music is playing when button is pressed, pause music. Otherwise play the music.
        final ToggleButton musicMuteButton = (ToggleButton)this.findViewById(R.id.mute_music_button);
        if(musicMuted){
            musicMuteButton.setChecked(false);
        }
        musicMuteButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(!musicMuted) {
                    menuMusic.pause();
                    musicMuted = true;
                    //musicPausedByButton = true;
                }
                else {
                    menuMusic.start();
                    musicMuted = false;
                    //musicPausedByButton = false;
                }
            }
        });

        pauseMMButton = (Button) findViewById(R.id.pauseMMButton);
        pauseMMButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestGameState(PAUSE);
            }
        });

    }

    // This function runs movement animations to get to other states
    //  then calls setGameState when they have completed
    public void requestGameState(int state) {
        hideAllMenus();
        switch (state) {
            case MAIN_MENU:
                if(gameState != MAIN_MENU) {
                    gameScreen.pause();
                    transitionToMainMenu();
                } else {
                    setGameState(MAIN_MENU);
                }
                break;
            case PLAYING_GAME:
                gameScreen.handleTouches = true;
                if(gameState == LOSE || gameState == WIN) {
                    gameScreen.resume();
                    setGameState(PLAYING_GAME);
                } else {
                    // Make sure gameScreen is in the right position
                    if(gameScreen.getTranslationY() != windowSize.y) {
                        gameScreen.setTranslationY(windowSize.y);
                        gameScreen.setBackgroundSizePos(windowSize);
                    }
                    gameScreen.resume();
                    transitionToGame();
                }
                break;
            case LOSE:
                gameScreen.handleTouches = false;
                System.out.println("hi");
                gameMusic1.pause();
                gameScreen.pause();
                setGameState(LOSE);
                break;
            case WIN:
                gameScreen.handleTouches = false;
                gameMusic1.pause();
                gameScreen.pause();
                setGameState(WIN);

                break;
            case PAUSE:
                setGameState(PAUSE);
                break;
            case NONE:
                // Nothing happens here
                break;
        }
    }

    // This function changes the state
    // Don't call this directly. Call requestGameState
    private void setGameState(int state) {
        gameState = state;
        switch (state) {
            case MAIN_MENU:
                mainMenu.setVisibility(View.VISIBLE);
                //Start menu music.
                if(loseMusic1 != null){
                    if(loseMusic1.isPlaying()){
                        loseMusic1.stop();
                    }
                    loseMusic1.release();
                    loseMusic1 = null;
                }
                if(winMusic1 != null){
                    if(winMusic1.isPlaying()){
                        winMusic1.stop();
                    }
                    winMusic1.release();
                    winMusic1 = null;
                }
                if(winMusicNewRecord != null){
                    if(winMusicNewRecord.isPlaying()){
                        winMusicNewRecord.stop();
                    }
                    winMusicNewRecord.release();
                    winMusicNewRecord = null;
                }
                menuMusic = MediaPlayer.create(this, R.raw.finger_runner_main_menu);
                menuMusic.setLooping(true);
                if(!musicMuted) {
                    menuMusic.start();
                }
                nowPlaying = MENU_MUSIC;
                break;
            case PLAYING_GAME:
                gameMenu.setVisibility(View.VISIBLE);
                //Start game music.
                if(loseMusic1 != null){
                    if(loseMusic1.isPlaying()){
                        loseMusic1.stop();
                    }
                    loseMusic1.release();
                    loseMusic1 = null;
                }
                if(winMusic1 != null){
                    if(winMusic1.isPlaying()){
                        winMusic1.stop();
                    }
                    winMusic1.release();
                    winMusic1 = null;
                }
                if(winMusicNewRecord != null){
                    if(winMusicNewRecord.isPlaying()){
                        winMusicNewRecord.stop();
                    }
                    winMusicNewRecord.release();
                    winMusicNewRecord = null;
                }
                gameMusic1 = MediaPlayer.create(this, R.raw.finger_runner_game_music_1);
                gameMusic1.setLooping(true);
                if(!musicMuted) {
                    gameMusic1.start();
                }
                nowPlaying = GAME_MUSIC_1;
                break;
            case LOSE:
                gameEndMenu.setVisibility(View.VISIBLE);
                root.removeView(gameEndMenu);
                root.addView(gameEndMenu);
                gameMusic1.stop();
                gameMusic1.release();
                gameMusic1 = null;
                //Start lose music.
                loseMusic1 = MediaPlayer.create(this, R.raw.finger_runner_lose_1);
                loseMusic1.setLooping(false);
                if(!musicMuted) {
                    loseMusic1.start();
                }
                nowPlaying = LOSE_MUSIC_1;
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
                gameScreen.resetVariables();
                break;
            case WIN:
                gameEndMenu.setVisibility(View.VISIBLE);
                root.removeView(gameEndMenu);
                root.addView(gameEndMenu);
                gameMusic1.stop();
                gameMusic1.release();
                gameMusic1 = null;
                //If new time was less than best time, save new time as best time. Display best time.
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
                        winMusicNewRecord = MediaPlayer.create(this, R.raw.finger_runner_win_new_record);
                        winMusicNewRecord.setLooping(false);
                        if(!musicMuted) {
                            winMusicNewRecord.start();
                        }
                    } else {
                        //Your time isn't a new best time.
                        endGameText.setText(R.string.win3);
                        endGameUserTime.setText("Your Time: \n" + yourTime.getTimeForDisplay());
                        endGameBestTime.setText("Best Time: \n" + bestTime.getTimeForDisplay());
                        //Start win music.
                        winMusic1 = MediaPlayer.create(this, R.raw.finger_runner_win_1);
                        winMusic1.setLooping(false);
                        if(!musicMuted) {
                            winMusic1.start();
                        }
                    }
                } else {
                    //No best time has been saved (a run has never been completed), so your time is a new best time.
                    saveNewBestTime(yourTime.getTime());
                    bestTime.changeTime(yourTime.getTime());
                    endGameText.setText(R.string.win2);
                    endGameUserTime.setText("Your Time: \n" + yourTime.getTimeForDisplay());
                    endGameBestTime.setText("Previous Best: \n" + "You'd never finished!");
                    //Start win music.
                    winMusicNewRecord = MediaPlayer.create(this, R.raw.finger_runner_win_new_record);
                    winMusicNewRecord.setLooping(false);
                    if(!musicMuted) {
                        winMusicNewRecord.start();
                    }
                }

                gameScreen.resetVariables();
                break;
            case PAUSE:
                pauseMenu.setVisibility(View.VISIBLE);

                break;
            case NONE:
                // Nothing happens here
                break;
        }
    }

    private void transitionToMainMenu() {
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

        //TODO: Fade out game music during transition? The commented-out section works, but it appears to delay the transition, and the fade-out time varies based on device speed.
        if(gameMusic1 != null) {
//            for (int i = 1000; i >= 0; i--) {
//                volume = i / 1000f;
//                gameMusic1.setVolume(volume, volume);
//            }
            //Stop game music.
            gameMusic1.stop();
            gameMusic1.release();
            gameMusic1 = null;
        }
    }

    private void transitionToGame() {
        gameScreen.animate()
                .translationY(0)//-windowSize.y)
                .setDuration(1000);

        titleScreen.animate()
                .translationY(-windowSize.y)
                .setDuration(1000)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        setGameState(PLAYING_GAME);
                    }
                });

        //Make button and timer visible.
        root.removeView(gameMenu);
        root.addView(gameMenu);

        //TODO: Fade out menu music during transition? The commented-out section works, but it appears to delay the transition, and the fade-out time varies based on device speed.
        if(menuMusic != null) {
//            for (int i = 1000; i >= 0; i--) {
//                volume = i / 1000f;
//                menuMusic.setVolume(volume, volume);
//            }
            //Stop menu music.
            menuMusic.stop();
            menuMusic.release();
            menuMusic = null;
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

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        //delayedHide(100);

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
        //mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void hideImmediately() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        //mControlsView.setVisibility(View.GONE);
        mVisible = false;

        root.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
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
        prefEditor.putBoolean("musicMuted", musicMuted);
        prefEditor.commit();
//        prefEditor.putInt("nowPlaying", nowPlaying);
//        prefEditor.commit();
        if(nowPlaying == MENU_MUSIC) {
            if(menuMusic != null) {
                if (menuMusic.isPlaying()) {
                    menuMusic.pause();
                    musicPausedByLeavingApp = true;
                }
            }
        }
        else if(nowPlaying == GAME_MUSIC_1){
            if(gameMusic1 != null) {
                if (gameMusic1.isPlaying()) {
                    gameMusic1.pause();
                    musicPausedByLeavingApp = true;
                }
            }
        }else if(nowPlaying == LOSE_MUSIC_1){
            if(loseMusic1 != null) {
                if (loseMusic1.isPlaying()) {
                    loseMusic1.pause();
                    musicPausedByLeavingApp = true;
                }
            }
        }else if(nowPlaying == WIN_MUSIC_1){
            if(winMusic1 != null) {
                if (winMusic1.isPlaying()) {
                    winMusic1.pause();
                    musicPausedByLeavingApp = true;
                }
            }
        }else if(nowPlaying == WIN_MUSIC_NEW_RECORD){
            if(winMusicNewRecord != null) {
                if (winMusicNewRecord.isPlaying()) {
                    winMusicNewRecord.pause();
                    musicPausedByLeavingApp = true;
                }
            }
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
            if(nowPlaying == MENU_MUSIC) {
                menuMusic.start();
            }
            else if(nowPlaying == GAME_MUSIC_1){
                gameMusic1.start();
            }
            musicPausedByLeavingApp = false;
        }

        //If not on game over screen, resume gameScreen.
        if(gameEndMenu.getVisibility() == View.GONE) {
            gameScreen.resume();
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
