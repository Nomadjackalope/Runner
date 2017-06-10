package com.runrmby.runner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;

/**
 * Created by Mark on 3/13/2017.
 */

public class LocationNoObstacles {

    private float courseDistance = 15000f;

    private float increaseDifficultyDistance = 10000f;

    private Obstacles coins;
    private int coinsResId = R.mipmap.coin2;
    private int coinScale = 75;
    private int maxNumCoins = 4;
    private float distBetweenCoins = 1500f;
    private float coinsHorizontalSpeed = 0f;
    private float coinsVerticalSpeed = 0f;

    private float touchDownX;
    private float touchDownY;

    private Obstacles footprintsR;
    private int footprintsRImageResId = R.mipmap.right_foot_yellow;
    private int footprintsHeight;
    private int footprintsWidth;
    private int footprintsXScale = 80;
    private int footprintsYScale = 184;
    private int maxNumFootprints = 1;

    private Obstacles footprintsL;
    private int footprintsLImageResId = R.mipmap.left_foot_yellow;

    private float velocityFactor = .75f; //Must be less than 1 or else road will advance exponentially.
//    private float distanceFactor = 1.00f;  //Must be <= 1 or else road will advance exponentially.
    private float inertiaFactor = 0.75f; //Must be less than 1 or else road will advance exponentially.

    //Sound effects
    private SoundPool soundEffects;
    private int goodSoundId;
    private int crashSound5Id;

    private MainActivity mA;
    private GameView gS;
    private float sX;
    private float sY;

    private int backgroundWidth;
    private int backgroundHeight;

    private float lastY;
    private boolean stepFlag;
    private boolean hopping;

    Context context;

    boolean fpMode = true;

    public LocationNoObstacles(MainActivity mA, GameView gS, float x, float y, int bW, int bH) {
        this.mA = mA;
        this.gS =  gS;
        this.sX = x;
        this.sY = y;
        this.backgroundWidth = bW;
        this.backgroundHeight = bH;
        this.context = gS.getContext();
        //-----------------Initialize obstacles----------------------------------------------------
        coins = new Obstacles(context, (int) (sX * coinScale), (int) (sY * coinScale), coinsResId, maxNumCoins, false, sY * distBetweenCoins, sX * coinsHorizontalSpeed, sY * coinsVerticalSpeed, 0, backgroundWidth, backgroundHeight, true, false, false);

        coins.setLimitSpawnX(coins.getObstacleWidth() + (int)(sX * 50), backgroundWidth - coins.getObstacleWidth() - (int)(sX * 50), false);

        //Initialize fpMode
        footprintsWidth = (int)(sX * footprintsXScale);
        footprintsHeight = (int)(sY * footprintsYScale);
        footprintsR = new Obstacles(context, footprintsWidth, footprintsHeight, footprintsRImageResId, maxNumFootprints, true, 0, 0, 0, 0, backgroundWidth, backgroundHeight, false, false, false);
        footprintsR.setRotatedObsImage(R.mipmap.right_foot_yellow_transparent, (int)(sX*171), (int)(sY*394));

        footprintsL = new Obstacles(context, footprintsWidth, footprintsHeight, footprintsLImageResId, maxNumFootprints, true, 0, 0, 0, 0, backgroundWidth, backgroundHeight, false, false, false);
        footprintsL.setRotatedObsImage(R.mipmap.left_foot_yellow_transparent, (int)(sX*171), (int)(sY*394));

        //Sound effects.
        setAudio();
    }

    public float getCourseDistance(){
        return (this.courseDistance);
    }

    public float getIncreaseDifficultyDistance(){
        return (this.increaseDifficultyDistance * sY);
    }

    public float getInertiaFactor(){
        return inertiaFactor;
    }

//    public float getDistanceFactor(){
//        return distanceFactor;
//    }

    public float getVelocityFactor(){
        return velocityFactor;
    }

    public float getTouchDownY(){return touchDownY;}

    public float getFootprintHeight(){return footprintsHeight;}

    public void setBackgroundWidth(int w){
        this.backgroundWidth = w;

    }

    public void setBackgroundHeight(int h){
        this.backgroundHeight = h;
    }

    public void updateTouchDownY(float d){
        this.touchDownY += d;
    }

    public void spawnFootprint(){
        float x = touchDownX - footprintsWidth/2;
        float y = touchDownY - footprintsHeight/2;
        if(footprintsL.spawnTracker[0] != 2 && footprintsR.spawnTracker[0] != 2 || footprintsL.spawnTracker[0] == 0) {//both or no fpMode spawned
            if (touchDownX < backgroundWidth / 2) {//if touch is on left half of screen, spawn left footprint, otherwise spawn right
                footprintsL.spawnObstacle(0f, x, y, true);
                if (footprintsR.spawnTracker[0] == 1) {
                    footprintsR.coordinatesArray[0][0] -= footprintsWidth / 2;
                    footprintsR.coordinatesArray[0][1] -= footprintsHeight / 2;
                    footprintsR.hitObstacle(0, false, false, false, true);
                }
            } else {
                footprintsR.spawnObstacle(0f, x, y, true);
                if (footprintsL.spawnTracker[0] == 1) {
                    footprintsL.coordinatesArray[0][0] -= footprintsWidth / 2;
                    footprintsL.coordinatesArray[0][1] -= footprintsHeight / 2;
                    footprintsL.hitObstacle(0, false, false, false, true);
                }
            }
        } else if (footprintsL.spawnTracker[0] == 1) {//Left foot down.
            if (x < footprintsL.coordinatesArray[0][0] + footprintsWidth) {//Touch is to left of right side of left foot.
                //Spawn left foot.
                footprintsL.spawnObstacle(0f, x, y, true);
                hopping = true;
            } else {//Touch is to right of right side of left foot.
                //Spawn right foot.
                footprintsR.spawnObstacle(0f, x, y, true);
                //"Lift" left foot.
                if (footprintsL.spawnTracker[0] == 1) {
                    footprintsL.coordinatesArray[0][0] -= footprintsWidth / 2;
                    footprintsL.coordinatesArray[0][1] -= footprintsHeight / 2;
                    footprintsL.hitObstacle(0, false, false, false, true);
                }
                hopping = false;
            }
        } else {//Right foot down.
            if (x < footprintsR.coordinatesArray[0][0] - footprintsWidth) {//Touch is to left of left side of right foot.
                //Spawn left foot.
                footprintsL.spawnObstacle(0f, x, y, true);
                //"Lift" right foot.
                if (footprintsR.spawnTracker[0] == 1) {
                    footprintsR.coordinatesArray[0][0] -= footprintsWidth / 2;
                    footprintsR.coordinatesArray[0][1] -= footprintsHeight / 2;
                    footprintsR.hitObstacle(0, false, false, false, true);
                }
                hopping = false;
            } else {//Touch is to right of left side of right foot.
                //Spawn right foot.
                footprintsR.spawnObstacle(0f, x, y, true);
                hopping = true;
            }
        }

        if (!mA.musicMuted) {
            soundEffects.play(crashSound5Id, 1f, 1f, 0, 0, 1);
        }
    }

    public void setTouchDownX(float x){
        this.touchDownX = x;
    }

    public void setTouchDownY(float y){
        this.touchDownY = y;
        if(fpMode){
            this.lastY = this.touchDownY;
            stepFlag = true;
            spawnFootprint();
        }
    }

    //---------------------Draw obstacles---------------------------------------------
    public void draw(Canvas canvas, Paint paint, float interpolation, float velocity) {
        coins.drawObstacles(canvas, paint, interpolation, velocity);

        //Draw last so is the top layer.
        if(fpMode) {
            footprintsR.drawObstacles(canvas, paint, interpolation, velocity);
            footprintsL.drawObstacles(canvas, paint, interpolation, velocity);
        }
    }

    //--------------Check if an obstacle has been touched-----------------------------
    public void checkIfObstacleWasTouched(int livesLeft) {
        if (coins.wasObstacleTouched(touchDownX - footprintsWidth / 2, touchDownY - footprintsHeight / 2, footprintsWidth, footprintsHeight, true, true, true, false) != -1) {
            gS.coins++;
            if (!mA.musicMuted) {
                soundEffects.play(goodSoundId, 0.5f, 0.5f, 0, 0, 1);
            }
        }
    }

    //---------------Update Obstacles---------------------------------------------------
    public void updateObs(float distance) {
        coins.updateObstacles(distance, true);

        if(fpMode) {
            if (footprintsR.spawnTracker[0] != 2) {//Right foot is down.
                footprintsR.updateObstacles(distance, false);
            } else {//Right foot isn't down.
                if(footprintsR.coordinatesArray[0][0] < touchDownX + footprintsWidth || hopping) {
                    //If feet are crossed, move footprint that's not "down" to begin straightening out.
                    footprintsR.coordinatesArray[0][0] += 0.1 * (touchDownX + footprintsWidth - footprintsR.coordinatesArray[0][0]);
                }
                if(footprintsR.coordinatesArray[0][1] - lastY + footprintsHeight > 10 && stepFlag) {
                    footprintsR.coordinatesArray[0][1] += 0.1 * (lastY - footprintsHeight * 2 - footprintsR.coordinatesArray[0][1]);
                } else if (!stepFlag){
                    footprintsR.coordinatesArray[0][1] += 0.01 * (touchDownY - footprintsHeight * 2 - footprintsR.coordinatesArray[0][1]);
                } else {
                    stepFlag = false;
                    footprintsR.coordinatesArray[0][1] += 0.01 * (touchDownY - footprintsHeight * 2 - footprintsR.coordinatesArray[0][1]);
                }
            }

            if (footprintsL.spawnTracker[0] != 2) {//Left foot is down.
                footprintsL.updateObstacles(distance, false);
            } else {//Left foot isn't down.
                if(footprintsL.coordinatesArray[0][0] > touchDownX - footprintsWidth*3 || hopping){
                    //If feet are crossed, move footprint that's not "down" to begin straightening out.
                    footprintsL.coordinatesArray[0][0] += 0.1 * (touchDownX - footprintsL.coordinatesArray[0][0] - footprintsWidth*3);
                }
                if(footprintsL.coordinatesArray[0][1] - lastY + footprintsHeight > 10 && stepFlag) {
                    footprintsL.coordinatesArray[0][1] += 0.1 * (lastY - footprintsHeight * 2 - footprintsL.coordinatesArray[0][1]);
                } else if(!stepFlag){
                    footprintsL.coordinatesArray[0][1] += 0.01 * (touchDownY - footprintsHeight * 2 - footprintsL.coordinatesArray[0][1]);
                } else {
                    stepFlag = false;
                    footprintsL.coordinatesArray[0][1] += 0.01 * (touchDownY - footprintsHeight * 2 - footprintsL.coordinatesArray[0][1]);
                }
            }

            //Don't let last footprint go off bottom of screen (otherwise you could sort of cheat if you scrolled fast enough)
            if (gS.fingers.isEmpty()) {
                if (footprintsR.spawnTracker[0] == 0) {
                    gS.velocity *= (backgroundHeight - footprintsL.coordinatesArray[0][1] - footprintsHeight) / backgroundHeight;
//                }
                } else {
                    gS.velocity *= (backgroundHeight - footprintsR.coordinatesArray[0][1] - footprintsHeight) / backgroundHeight;
                }
            }
        }
    }

    //Move obstacles(any movement independent from the road).
    public void move(){
        coins.moveObstacles();
    }

    public float getFootDownYLocation(){
        if(footprintsR.spawnTracker[0] == 1){
            //Right foot is down
            return footprintsR.coordinatesArray[0][1] + footprintsHeight/2;
        } else if(footprintsL.spawnTracker[0] == 1) {
            //Left foot is down
            return footprintsL.coordinatesArray[0][1] + footprintsHeight/2;
        } else {
            return 0f;
        }
    }

    //--------------------------Reset obstacles------------------------------------------
    public void resetObstacles() {
        coins.resetObstacles(distBetweenCoins, backgroundWidth, backgroundHeight);
        footprintsR.resetObstacles(0f, backgroundWidth, backgroundHeight);
        footprintsL.resetObstacles(0f, backgroundWidth, backgroundHeight);
        touchDownX = backgroundWidth / 2;
        touchDownY = backgroundHeight + footprintsHeight / 2;
        lastY = touchDownY;
        hopping = false;

        //Show fpMode at bottom of screen at start of game.
        if(fpMode) {
            footprintsL.spawnObstacle(0f, touchDownX - footprintsWidth * 2, backgroundHeight - footprintsHeight, true);
            footprintsR.spawnObstacle(0f, touchDownX + footprintsWidth, backgroundHeight - footprintsHeight, true);
        }
    }

    public void releaseAudio(){
        if(soundEffects != null) {
            for(int i = 0; i < 10; i++){
                soundEffects.stop(i);
            }
            soundEffects.release();
            soundEffects = null;
        }
    }

    public void setAudio(){
        if(soundEffects == null) {
//        soundEffects = new SoundPool.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build();
            soundEffects = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
            goodSoundId = soundEffects.load(context, R.raw.coin_sound_2, 1);
            crashSound5Id = soundEffects.load(context, R.raw.finger_runner_crash_sound_5, 1);

//            soundEffects = mA.sfx;
//        badSoundId = mA.badSoundId;
//        goodSoundId = mA.goodSoundId;
//        crashSoundId = mA.crashSoundId;
//        crashSound2Id = mA.crashSound2Id;
//        crashSound3Id = mA.crashSound3Id;
//        crashSound4Id = mA.crashSound4Id;
//            crashSound5Id = mA.crashSound5Id;
//        truckHornId = mA.truckHornId;
//        carHornId = mA.carHornId;
//        wilhelmScreamId = mA.wilhelmScreamId;
        }
    }

//    public void setFpMode(boolean m){
//        this.fpMode = m;
//    }
}
