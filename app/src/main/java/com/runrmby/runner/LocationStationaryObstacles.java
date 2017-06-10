package com.runrmby.runner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.SystemClock;
import android.view.MotionEvent;

/**
 * Created by Mark on 3/14/2017.
 */

public class LocationStationaryObstacles {

    private float courseDistance = 30000f;

    private Obstacles cone;
    private int coneResId = R.mipmap.cone;
    private int coneXScale = 74;
    private int coneYScale = 104;
    private int maxNumCones = 50;
    private float distBetweenCones = 1000f;
    private float coneXSpeed = 0f;
    private float coneYSpeed = 0f;

    private Obstacles raceCar;
    private int raceCarResId = R.mipmap.race_car;
    private int raceCarXScale = 279;
    private int raceCarYScale = 487;
    private int maxNumRaceCars = 1;
    private float distBetweenRaceCars = 29000f;
    private float raceCarXSpeed = 0f;
    private float raceCarYSpeed = 0f;

    boolean racerTrigger = false;

    private Obstacles homingOb;
    private int homingObResID = R.mipmap.dude;
    private int homingObXScale = 172;
    private int homingObYScale = 204;
    private int homingObWidth;
    private int homingObHeight;
    private int homingObMaxNum = 1;
    private float homingObDistBetween = 75000f;
    private float homingObXSpeed = 0f;
    private float homingObYSpeed = 0f;
    private float homingObHomingSpeed = 0.02f;

    private float increaseDifficultyDistance = 1500f;
    private int difficultly;

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

    private Matrix matrixRotateClockwise = new Matrix();
    private Matrix matrixRotateCounterClockwise = new Matrix();

    private float velocityFactor = .75f; //Must be less than 1 or else road will advance exponentially.
//    private float distanceFactor = 1.00f;  //Must be <= 1 or else road will advance exponentially.
    private float inertiaFactor = 0.75f; //Must be less than 1 or else road will advance exponentially.

    //Sound effects
    private SoundPool soundEffects;
    private int badSoundId;
    private int goodSoundId;
    private int crashSound5Id;
    private int wilhelmScreamId;
    private int driveSoundId;

    private MainActivity mA;
    private GameView gS;
    private float sX;
    private float sY;

    private int backgroundWidth;
    private int backgroundHeight;

    float factor;

    private float lastY;
    private boolean stepFlag;
    boolean hopping;

    Context context;

    boolean fpMode = true;

    public LocationStationaryObstacles(MainActivity mA, GameView gS, float x, float y, int bW, int bH) {
        this.mA = mA;
        this.gS =  gS;
        this.sX = x;
        this.sY = y;
        this.backgroundWidth = bW;
        this.backgroundHeight = bH;
        this.context = gS.getContext();
        //-----------------Initialize obstacles----------------------------------------------------
        homingObWidth = (int) (sX * homingObXScale);
        homingObHeight = (int) (sY * homingObYScale);

        cone = new Obstacles(context, (int) (sX * coneXScale), (int) (sY * coneYScale), coneResId, maxNumCones, false, sY * distBetweenCones, sX * coneXSpeed, sY * coneYSpeed, 0, backgroundWidth, backgroundHeight, true, false, false);
        raceCar = new Obstacles(context, (int) (sX * raceCarXScale), (int) (sY * raceCarYScale), raceCarResId, maxNumRaceCars, false, sY * distBetweenRaceCars, sX * raceCarXSpeed, sY * raceCarYSpeed, 0, backgroundWidth, backgroundHeight, false, true, false);
        homingOb = new Obstacles(context, homingObWidth, homingObHeight, homingObResID, homingObMaxNum, false, sY * homingObDistBetween, sX * homingObXSpeed, sY * homingObYSpeed, sY * homingObHomingSpeed, backgroundWidth, backgroundHeight + backgroundHeight, false, false, false);
        coins = new Obstacles(context, (int) (sX * coinScale), (int) (sY * coinScale), coinsResId, maxNumCoins, false, sY * distBetweenCoins, sX * coinsHorizontalSpeed, sY * coinsVerticalSpeed, 0, backgroundWidth, backgroundHeight, true, false, false);

        coins.setLimitSpawnX(coins.getObstacleWidth() + (int)(sX * 50), backgroundWidth - coins.getObstacleWidth() - (int)(sX * 50), false);

        cone.setMultiSpawn(true);
        raceCar.setLimitSpawnX((int)(sX * 200), backgroundWidth - (int)(sX * 200), false);
        raceCar.setSpawnBottom(true);
        raceCar.setFlipY(true);

        //Initialize fpMode
        footprintsWidth = (int)(sX * footprintsXScale);
        footprintsHeight = (int)(sY * footprintsYScale);
        footprintsR = new Obstacles(context, footprintsWidth, footprintsHeight, footprintsRImageResId, maxNumFootprints, true, 0, 0, 0, 0, backgroundWidth, backgroundHeight, false, false, false);
        footprintsR.setRotatedObsImage(R.mipmap.right_foot_yellow_transparent, (int)(sX*171), (int)(sY*394));

        footprintsL = new Obstacles(context, footprintsWidth, footprintsHeight, footprintsLImageResId, maxNumFootprints, true, 0, 0, 0, 0, backgroundWidth, backgroundHeight, false, false, false);
        footprintsL.setRotatedObsImage(R.mipmap.left_foot_yellow_transparent, (int)(sX*171), (int)(sY*394));

        matrixRotateClockwise.postRotate(90);
        matrixRotateCounterClockwise.postRotate(270);

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

    public void updateHomingSpeed(float factor){
        homingOb.homingSpeed += homingObHomingSpeed * factor;
    }

    public void updateHomingObstacle(){
        for (int i = 0; i < homingObMaxNum; i++) {
            if (homingOb.spawnTracker[i] == 1) {
//                if(!gS.fingers.isEmpty()) {
                    if (homingOb.coordinatesArray[i][0] + homingObWidth/2 != touchDownX) {
//                        homingOb.coordinatesArray[i][0] += homingOb.speedArray[i][2] * (touchDownX - homingOb.coordinatesArray[i][0] - homingObWidth/2);
                        homingOb.speedArray[i][0] = homingOb.speedArray[i][2] * (touchDownX - homingOb.coordinatesArray[i][0] - homingObWidth/2);
                    }
                    if (homingOb.coordinatesArray[i][1] + homingObHeight/2 != touchDownY) {
//                        homingOb.coordinatesArray[i][1] += homingOb.speedArray[i][3] * (touchDownY - homingOb.coordinatesArray[i][1] - homingObHeight/2);//homingOb.speedArray[i][3]*(.0001f*(touchDownY - homingOb.coordinatesArray[i][1])*(touchDownY + homingOb.coordinatesArray[i][1]) + 4*(touchDownY - homingOb.coordinatesArray[i][1]));
                        homingOb.speedArray[i][1] = homingOb.speedArray[i][3] * (touchDownY - homingOb.coordinatesArray[i][1] - homingObHeight/2);
                    }
//                }
            }
        }
    }

    public void updateDifficulty(int difficultly){
        updateObstacleSeparation();
        this.difficultly = difficultly;
    }

    public void updateObstacleSeparation() {
        //TODO: make factor global
        factor = (difficultly + 49f) / (difficultly + 50f);
        if(racerTrigger) {
            raceCar.updateDistanceBetweenObstacles(factor);
        }
        homingOb.updateDistanceBetweenObstacles(factor);

        factor = 1f / (difficultly + 8f);
        raceCar.increaseVerticalSpeed(factor);

        factor = (difficultly + 9f) / (difficultly + 10f);
        cone.updateDistanceBetweenObstacles(factor);

//        updateHomingSpeed(1f / (difficulty + 8f));
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
        cone.drawObstacles(canvas, paint, interpolation, velocity);
        homingOb.drawObstacles(canvas, paint, interpolation, velocity);
        raceCar.drawObstacles(canvas, paint, interpolation, velocity);

        //Draw last so is the top layer.
        if(fpMode) {
            footprintsR.drawObstacles(canvas, paint, interpolation, velocity);
            footprintsL.drawObstacles(canvas, paint, interpolation, velocity);
        }
    }

    //--------------Check if an obstacle has run into touchFollower when no fingers are down (only necessary if friendly sprite triggers obstacles)-----------------------------
    public void checkIfObstacleRanIntoTouchFollower(int livesLeft) {
        if (checkObstaclesTouched(true)) {//Looks like if(false), but false is a passed boolean.
            if (livesLeft == 0) {
                mA.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gS.simulatedTouch = true;
                        gS.getRootView().dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 1, MotionEvent.ACTION_DOWN, 0f, 0f, 0));
                    }
                });
            } else {
                checkObstaclesTouched(false);
                gS.velocity = 0;
                gS.livesLeft--;
                if (!mA.musicMuted) {
                    soundEffects.play(badSoundId, 1, 1, 0, 0, 1);
                }
                gS.collisionsWitnessed++;
            }
        }
        if (coins.wasObstacleTouched(touchDownX - footprintsWidth / 2, touchDownY - footprintsHeight / 2, footprintsWidth, footprintsHeight, true, true, true, false) != -1) {
            gS.coins++;
            if (!mA.musicMuted) {
                soundEffects.play(goodSoundId, 0.5f, 0.5f, 0, 0, 1);
            }
        }
    }

    //--------------Check if an obstacle has been touched-----------------------------
    public void checkIfObstacleWasTouched(int livesLeft) {
        if (checkObstaclesTouched(false)) {
            if (livesLeft == 0) {
                if (!mA.musicMuted) {
                    soundEffects.play(wilhelmScreamId, 1, 1, 0, 0, 1);
                }
                gS.collisionsWitnessed++;
                mA.setGameState(MainActivity.GAME_LOST);
            } else {
                gS.velocity = 0;
                --gS.livesLeft;
                if (!mA.musicMuted) {
                    soundEffects.play(badSoundId, 1, 1, 0, 0, 1);
                    gS.collisionsWitnessed++;
                }
            }
        }
        if (coins.wasObstacleTouched(touchDownX - footprintsWidth / 2, touchDownY - footprintsHeight / 2, footprintsWidth, footprintsHeight, true, true, true, false) != -1) {
            gS.coins++;
            if (!mA.musicMuted) {
                soundEffects.play(goodSoundId, 0.5f, 0.5f, 0, 0, 1);
            }
        }
    }

    //---------------Update Obstacles---------------------------------------------------
    public void updateObs(float distance) {
        cone.updateObstacles(distance, true);
        if (raceCar.updateObstacles(distance, true)) {
            if (!mA.musicMuted && racerTrigger) {
                soundEffects.play(driveSoundId, 0.5f, 0.5f, 1, 0, 2);
            }
        }
        //Give car velocity after certain distance so it's hard to go really far.
        if(gS.odometer > (sY * 40000) && !racerTrigger){
            racerTrigger = true;
            raceCar.verticalSpeed = -40 * sY;
            raceCar.doAutoSpawn(0);
            if (!mA.musicMuted) {
                soundEffects.play(driveSoundId, 0.5f, 0.5f, 1, 0, 2);
            }
            raceCar.setDistanceBetweenObstacles(sY * 10000);
        }
        homingOb.updateObstacles(distance, true);

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
                } else {
                    gS.velocity *= (backgroundHeight - footprintsR.coordinatesArray[0][1] - footprintsHeight) / backgroundHeight;
                }
            }
        }
    }

    //Move obstacles(any movement independent from the road).
    public void move(){
        cone.moveObstacles();
        raceCar.moveObstacles();
        homingOb.moveObstacles();
        coins.moveObstacles();
    }

    //--------------Check if an obstacle was touched-----------------------------
    public Boolean checkObstaclesTouched(boolean checkOnly) {

        //Collide other obstacles.
        checkCollisions();

        for (int i = 0; i < maxNumFootprints; i++) {
            if (footprintsR.spawnTracker[i] == 1) {
                if (cone.wasObstacleTouched(footprintsR.coordinatesArray[i][0], footprintsR.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
                    return true;
                } else if (raceCar.wasObstacleTouched(footprintsR.coordinatesArray[i][0], footprintsR.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
                    return true;
                } else if (homingOb.wasObstacleTouched(footprintsR.coordinatesArray[i][0], footprintsR.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
                    return true;
                }
            }
            if (footprintsL.spawnTracker[i] == 1) {
                if (cone.wasObstacleTouched(footprintsL.coordinatesArray[i][0], footprintsL.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
                    return true;
                } else if (raceCar.wasObstacleTouched(footprintsL.coordinatesArray[i][0], footprintsL.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
                    return true;
                } else if (homingOb.wasObstacleTouched(footprintsL.coordinatesArray[i][0], footprintsL.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
                    return true;
                }
            }
        }
        return false;
    }

    public void checkCollisions() {
        //Check if trucks have touched other obstacles.
        for (int i = 0; i < maxNumRaceCars; i++) {
            if (raceCar.spawnTracker[i] == 1 || raceCar.spawnTracker[i] == 2) {
                if (raceCar.speedArray[i][1] != 0) { //Truck should only hit obstacles if it's moving.
                    if (cone.wasObstacleTouched(raceCar.coordinatesArray[i][0], raceCar.coordinatesArray[i][1], raceCar.obstacleWidth, raceCar.obstacleHeight, false, false, false, false) != -1) {
                        if (!mA.musicMuted) {
                            soundEffects.play(crashSound5Id, 0.5f, 0.5f, 0, 0, 1);
                        }
                        gS.collisionsWitnessed++;
                    }
                }
                //Destroy cone if not moving so it doesn't look weird.
                cone.wasObstacleTouched(raceCar.coordinatesArray[i][0], raceCar.coordinatesArray[i][1], raceCar.obstacleWidth, raceCar.obstacleHeight, false, false, true, false);
                if (homingOb.wasObstacleTouched(raceCar.coordinatesArray[i][0], raceCar.coordinatesArray[i][1], raceCar.obstacleWidth, raceCar.obstacleHeight, false, true, false, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(wilhelmScreamId, 0.5f, 0.5f, 0, 0, 1.2f);
                    }
                }
            }
        }
        for (int i = 0; i < homingObMaxNum; i++) {
            if (homingOb.spawnTracker[i] == 1) {
                if (cone.wasObstacleTouched(homingOb.coordinatesArray[i][0], homingOb.coordinatesArray[i][1], homingOb.obstacleWidth, homingOb.obstacleHeight, false, false, false, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(crashSound5Id, 0.5f, 0.5f, 0, 0, 1);
                    }
                    gS.collisionsWitnessed++;
                }
                if (raceCar.wasObstacleTouched(homingOb.coordinatesArray[i][0], homingOb.coordinatesArray[i][1], homingOb.obstacleWidth, homingOb.obstacleHeight, false, false, false, true) != -1) {
                    homingOb.speedArray[i][2] *= 0;
                    homingOb.speedArray[i][3] *= 0;
                }
            }
        }
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
        cone.resetObstacles(distBetweenCones, backgroundWidth, backgroundHeight);
        raceCar.resetObstacles(distBetweenRaceCars, backgroundWidth, backgroundHeight);
        racerTrigger = false;
        homingOb.resetObstacles(homingObDistBetween, backgroundWidth, backgroundHeight);
        coins.resetObstacles(distBetweenCoins, backgroundWidth, backgroundHeight);
        footprintsR.resetObstacles(0f, backgroundWidth, backgroundHeight);
        footprintsL.resetObstacles(0f, backgroundWidth, backgroundHeight);
        difficultly = 0;
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
//
    public void setAudio(){
        if(soundEffects == null) {
//        soundEffects = new SoundPool.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build();
            soundEffects = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
            badSoundId = soundEffects.load(context, R.raw.finger_runner_bad_sound, 1);
            goodSoundId = soundEffects.load(context, R.raw.coin_sound_2, 1);
            crashSound5Id = soundEffects.load(context, R.raw.finger_runner_crash_sound_5, 1);
            wilhelmScreamId = soundEffects.load(context, R.raw.wilhelm_scream, 1);
            driveSoundId = soundEffects.load(context, R.raw.car_driving, 1);

//            soundEffects = mA.sfx;
//            badSoundId = mA.badSoundId;
//            goodSoundId = mA.goodSoundId;
//            crashSoundId = mA.crashSoundId;
//            crashSound5Id = mA.crashSound5Id;
//            wilhelmScreamId = mA.wilhelmScreamId;
        }
    }
}
