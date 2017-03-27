package com.runrmby.runner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.SystemClock;
import android.view.MotionEvent;

/**
 * Created by Mark on 3/15/2017.
 */

public class LocationCrazyRoad {

    private float courseDistance = 40000f;

    private Obstacles cone;
    private int coneResId = R.mipmap.cone;
    private int coneXScale = 74;
    private int coneYScale = 104;
    private int maxNumCones = 10;
    private float distBetweenCones = 4000f;
    private float coneXSpeed = 0f;
    private float coneYSpeed = 0f;

    private Obstacles semi;
    private int semiResId = R.mipmap.semi;
    private int semiXScale = 117;
    private int semiYScale = 470;
    private int maxNumSemis = 2;
    private float distBetweenSemis = 5000f;
    private float semiXSpeed = 0f;
    private float semiYSpeed = 4f;

    private Obstacles truck;
    private int truckResId = R.mipmap.orange_truck;
    private int truckXScale = 91;
    private int truckYScale = 189;
    private int maxNumTrucks = 10;
    private float distBetweenTrucks = 2500f;
    private float truckXSpeed = 0f;
    private float truckYSpeed = 5f;
//
    private Obstacles crowd;
    private int crowdResId = R.mipmap.chicken;
    private int crowdXScale = 83;
    private int crowdYScale = 85;
    private int maxNumCrowds = 10;
    private float distBetweenCrowds = 3000f;
    private float crowdXSpeed = 3f;
    private float crowdYSpeed = 1f;

    private Obstacles car;
    private int carResId = R.mipmap.race_car;
    private int carXScale = 84;
    private int carYScale = 166;
    private int maxNumCars = 10;
    private float distBetweenCars = 1100f;
    private float carXSpeed = 0f;
    private float carYSpeed = 7f;

    private Obstacles car2;
    private int car2ResId = R.mipmap.red_bug;
    private int car2XScale = 88;
    private int car2YScale = 169;
    private int maxNumCars2 = 12;
    private float distBetweenCars2 = 1000f;
    private float car2XSpeed = 0f;
    private float car2YSpeed = 6f;

    private Obstacles homingOb;
    private int homingObResID = R.mipmap.dude;
    private int homingObXScale = 75;
    private int homingObYScale = 83;
    private int homingObWidth;
    private int homingObHeight;
    private int homingObMaxNum = 2;
    private float homingObDistBetween = 50000f;
    private float homingObXSpeed = 0f;
    private float homingObYSpeed = 0f;
    private float homingObHomingSpeed = 0.015f;

    private float increaseDifficultyDistance = 1200f;
    private int difficultly;

    private Obstacles coins;
    private int coinsResId = R.mipmap.coin2;
    private int coinScale = 75;
    private int maxNumCoins = 4;
    private float distBetweenCoins = 1500f;
    private float coinsHorizontalSpeed = 4f;
    private float coinsVerticalSpeed = 0f;

    private float touchDownX;
    private float touchDownY;

    private Obstacles footprintsR;
    private int footprintsRImageResId = R.mipmap.right_foot_yellow;
    private int footprintsHeight;
    private int footprintsWidth;
    private int footprintsXScale = 60;
    private int footprintsYScale = 138;
    private int maxNumFootprints = 1;

    private Obstacles footprintsL;
    private int footprintsLImageResId = R.mipmap.left_foot_yellow;

    private float velocityFactor = .75f; //Must be less than 1 or else road will advance exponentially.
    private float distanceFactor = 1.00f;  //Must be <= 1 or else road will advance exponentially.
    private float inertiaFactor = 0.75f; //Must be less than 1 or else road will advance exponentially.

    //Sound effects
    private SoundPool soundEffects;
    private int badSoundId;
    private int goodSoundId;
    private int crashSound2Id;
    private int crashSound4Id;
    private int crashSound5Id;
    private int chickenSoundId;
    private int chickenSound2Id;
    private int wilhelmScreamId;
    private int driveSoundId;

    private MainActivity mA;
    private GameView gS;
    private float sX;
    private float sY;

    private int backgroundResId = R.mipmap.road_0;
    private int backgroundWidth;
    private int backgroundHeight;

    float factor;

    private float lastY;
    private boolean stepFlag;
    boolean hopping;

    Context context;

    boolean fpMode = true;

    float boundary = 20;
    float wc;
    float hc;
    int obHit;

    public LocationCrazyRoad(MainActivity mA, GameView gS, float x, float y, int bW, int bH) {
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
        semi = new Obstacles(context, (int) (sX * semiXScale), (int) (sY * semiYScale), semiResId, maxNumSemis, false, sY * distBetweenSemis, sX * semiXSpeed, sY * semiYSpeed, 0, backgroundWidth, backgroundHeight, true, true, true);
        truck = new Obstacles(context, (int) (sX * truckXScale), (int) (sY * truckYScale), truckResId, maxNumTrucks, false, sY * distBetweenTrucks, sX * truckXSpeed, sY * truckYSpeed, 0, backgroundWidth, backgroundHeight, true, true, true);
        crowd = new Obstacles(context, (int) (sX * crowdXScale), (int) (sY * crowdYScale), crowdResId, maxNumCrowds, false, sY * distBetweenCrowds, sX * crowdXSpeed, sY * crowdYSpeed, 0, backgroundWidth, backgroundHeight, true, false, false);
        car = new Obstacles(context, (int) (sX * carXScale), (int) (sY * carYScale), carResId, maxNumCars, false, sY * distBetweenCars, sX * carXSpeed, sY * carYSpeed, 0, backgroundWidth, backgroundHeight, true, true, true);
        car2 = new Obstacles(context, (int) (sX * car2XScale), (int) (sY * car2YScale), car2ResId, maxNumCars2, false, sY * distBetweenCars2, sX * car2XSpeed, sY * car2YSpeed, 0, backgroundWidth, backgroundHeight, true, true, true);
        homingOb = new Obstacles(context, homingObWidth, homingObHeight, homingObResID, homingObMaxNum, false, sY * homingObDistBetween, sX * homingObXSpeed, sY * homingObYSpeed, sY * homingObHomingSpeed, backgroundWidth, backgroundHeight + backgroundHeight, false, false, false);
        coins = new Obstacles(context, (int) (sX * coinScale), (int) (sY * coinScale), coinsResId, maxNumCoins, false, sY * distBetweenCoins, sX * coinsHorizontalSpeed, sY * coinsVerticalSpeed, 0, backgroundWidth, backgroundHeight, true, false, false);

        crowd.setDirectionalX(true);
        crowd.setRotatedObsImage2(R.mipmap.chicken_right, (int) (sX * crowdXScale), (int) (sY * crowdYScale));

        //Initialize fpMode
        footprintsWidth = (int)(sX * footprintsXScale);
        footprintsHeight = (int)(sY * footprintsYScale);
        footprintsR = new Obstacles(context, footprintsWidth, footprintsHeight, footprintsRImageResId, maxNumFootprints, true, 0, 0, 0, 0, backgroundWidth, backgroundHeight, false, false, false);
        footprintsR.setRotatedObsImage(R.mipmap.right_foot_yellow_transparent, (int)(sX*120), (int)(sY*276));

        footprintsL = new Obstacles(context, footprintsWidth, footprintsHeight, footprintsLImageResId, maxNumFootprints, true, 0, 0, 0, 0, backgroundWidth, backgroundHeight, false, false, false);
        footprintsL.setRotatedObsImage(R.mipmap.left_foot_yellow_transparent, (int)(sX*120), (int)(sY*276));

        //Sound effects.
        setAudio();
    }

    public float getCourseDistance(){
        return (this.courseDistance * sY);
    }

    public float getIncreaseDifficultyDistance(){
        return (this.increaseDifficultyDistance * sY);
    }

    public float getInertiaFactor(){
        return inertiaFactor;
    }

    public float getDistanceFactor(){
        return distanceFactor;
    }

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
        if(footprintsL.spawnTracker[0] != 2 && footprintsR.spawnTracker[0] != 2 || footprintsL.spawnTracker[0] == 0 && footprintsR.spawnTracker[0] == 0) {//both or no fpMode spawned
//            if(fRTempY == fLTempY){//neither foot ahead (start)
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

    public void updateHomingObstacle() {
        for (int i = 0; i < homingObMaxNum; i++) {
            if (homingOb.spawnTracker[i] == 1) {
                if (homingOb.coordinatesArray[i][0] + homingObWidth / 2 != touchDownX) {
                    homingOb.speedArray[i][0] = homingOb.speedArray[i][2] * (touchDownX - homingOb.coordinatesArray[i][0] - homingObWidth / 2);
                }
                if (homingOb.coordinatesArray[i][1] + homingObHeight / 2 != touchDownY) {
                    homingOb.speedArray[i][1] = homingOb.speedArray[i][3] * (touchDownY - homingOb.coordinatesArray[i][1] - homingObHeight / 2);
                }
            }
        }
    }

    public void updateDifficulty(int difficultly){
        updateObstacleSeparation();
        this.difficultly = difficultly;
    }

    public void updateObstacleSeparation() {
        factor = (difficultly + 49f) / (difficultly + 50f);
        cone.updateDistanceBetweenObstacles(factor);
        semi.updateDistanceBetweenObstacles(factor);
        truck.updateDistanceBetweenObstacles(factor);
        crowd.updateDistanceBetweenObstacles(factor);
        car.updateDistanceBetweenObstacles(factor);
        car2.updateDistanceBetweenObstacles(factor);
        homingOb.updateDistanceBetweenObstacles(factor);

        factor = 1f / (difficultly + 8f);
        updateHomingSpeed(factor);
        car.increaseVerticalSpeed(factor);
        car2.increaseVerticalSpeed(factor);
        semi.increaseVerticalSpeed(factor);
        truck.increaseVerticalSpeed(factor);
        crowd.increaseHorizontalSpeed(factor);
        crowd.increaseVerticalSpeed(factor);
        coins.increaseHorizontalSpeed(factor);
        coins.increaseVerticalSpeed(factor);
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
        crowd.drawObstacles(canvas, paint, interpolation, velocity);
        homingOb.drawObstacles(canvas, paint, interpolation, velocity);
        car.drawObstacles(canvas, paint, interpolation, velocity);
        car2.drawObstacles(canvas, paint, interpolation, velocity);
        truck.drawObstacles(canvas, paint, interpolation, velocity);
        semi.drawObstacles(canvas, paint, interpolation, velocity);

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
        if(coins.wasObstacleTouched(touchDownX - footprintsWidth/2, touchDownY - footprintsHeight/2, footprintsWidth, footprintsHeight, true, true, true, false) != -1) {
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
        if(coins.wasObstacleTouched(touchDownX - footprintsWidth/2, touchDownY - footprintsHeight/2, footprintsWidth, footprintsHeight, true, true, true, false) != -1) {
            gS.coins++;
            if (!mA.musicMuted) {
                soundEffects.play(goodSoundId, 0.5f, 0.5f, 0, 0, 1);
            }
        }
    }

    //---------------Update Obstacles---------------------------------------------------
    public void updateObs(float distance) {
        cone.updateObstacles(distance, true);
        if (truck.updateObstacles(distance, true)) {
            if (!mA.musicMuted) {
                soundEffects.play(driveSoundId, 0.4f, 0.4f, 0, 0, 1.5f);
            }
        }
        if(semi.updateObstacles(distance, true)){
            if(!mA.musicMuted){
                soundEffects.play(driveSoundId, 0.4f, 0.4f, 0, 0, 1);
            }
        }
        if(crowd.updateObstacles(distance, true)){
            if(!mA.musicMuted){
                soundEffects.play(chickenSoundId, 1f, 1f, 0, 0, 1);
            }
        }
        if (car.updateObstacles(distance, true)) {
            if (!mA.musicMuted) {
                soundEffects.play(driveSoundId, 0.4f, 0.4f, 0, 0, 2f);
            }
        }
        if (car2.updateObstacles(distance, true)) {
            if (!mA.musicMuted) {
                soundEffects.play(driveSoundId, 0.4f, 0.4f, 0, 0, 1.75f);
            }
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
        semi.moveObstacles();
        truck.moveObstacles();
        crowd.moveObstacles();
        car.moveObstacles();
        car2.moveObstacles();
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
                } else if (semi.wasObstacleTouched(footprintsR.coordinatesArray[i][0], footprintsR.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
                    return true;
                } else if (truck.wasObstacleTouched(footprintsR.coordinatesArray[i][0], footprintsR.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
                    return true;
                } else if (crowd.wasObstacleTouched(footprintsR.coordinatesArray[i][0], footprintsR.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
                    return true;
                } else if (car.wasObstacleTouched(footprintsR.coordinatesArray[i][0], footprintsR.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
                    return true;
                } else if (car2.wasObstacleTouched(footprintsR.coordinatesArray[i][0], footprintsR.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
                    return true;
                } else if (homingOb.wasObstacleTouched(footprintsR.coordinatesArray[i][0], footprintsR.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
                    return true;
                }
            }
            if (footprintsL.spawnTracker[i] == 1) {
                if (cone.wasObstacleTouched(footprintsL.coordinatesArray[i][0], footprintsL.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
                    return true;
                } else if (semi.wasObstacleTouched(footprintsL.coordinatesArray[i][0], footprintsL.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
                    return true;
                } else if (truck.wasObstacleTouched(footprintsL.coordinatesArray[i][0], footprintsL.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
                    return true;
                } else if (crowd.wasObstacleTouched(footprintsL.coordinatesArray[i][0], footprintsL.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
                    return true;
                } else if (car.wasObstacleTouched(footprintsL.coordinatesArray[i][0], footprintsL.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
                    return true;
                } else if (car2.wasObstacleTouched(footprintsL.coordinatesArray[i][0], footprintsL.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
                    return true;
                } else if (homingOb.wasObstacleTouched(footprintsL.coordinatesArray[i][0], footprintsL.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
                    return true;
                }
            }
        }
        return false;
    }

    public void checkCollisions(){
        for(int i = 0; i < maxNumSemis; i++) {
            if (semi.spawnTracker[i] == 1 || semi.spawnTracker[i] == 2) {
                if (cone.wasObstacleTouched(semi.coordinatesArray[i][0], semi.coordinatesArray[i][1], semi.obstacleWidth, semi.obstacleHeight, false, false, false, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(crashSound5Id, 0.5f, 0.5f, 0, 0, 1);
                    }
                    gS.collisionsWitnessed++;
                }
                if (crowd.wasObstacleTouched(semi.coordinatesArray[i][0], semi.coordinatesArray[i][1], semi.obstacleWidth, semi.obstacleHeight, false, true, false, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(chickenSound2Id, 1f, 1f, 0, 0, 1);
                        soundEffects.play(crashSound4Id, 0.5f, 0.5f, 0, 0, 1);
                    }
                    gS.collisionsWitnessed++;
                }
                if (homingOb.wasObstacleTouched(semi.coordinatesArray[i][0], semi.coordinatesArray[i][1], semi.obstacleWidth, semi.obstacleHeight, false, true, false, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(wilhelmScreamId, 0.5f, 0.5f, 0, 0, 1.2f);
                    }
                    gS.collisionsWitnessed++;
                }
                if (truck.wasObstacleTouched(semi.coordinatesArray[i][0], semi.coordinatesArray[i][1], semi.obstacleWidth, semi.obstacleHeight, false, false, false, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(crashSound2Id, 0.5f, 0.5f, 0, 0, 1);
                    }
                    gS.collisionsWitnessed++;
                }
                if (car.wasObstacleTouched(semi.coordinatesArray[i][0], semi.coordinatesArray[i][1], semi.obstacleWidth, semi.obstacleHeight, false, false, false, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(crashSound4Id, 0.5f, 0.5f, 0, 0, 1);
                    }
                    gS.collisionsWitnessed++;
                }
                if (car2.wasObstacleTouched(semi.coordinatesArray[i][0], semi.coordinatesArray[i][1], semi.obstacleWidth, semi.obstacleHeight, false, false, false, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(crashSound2Id, 0.5f, 0.5f, 0, 0, 1);
                    }
                    gS.collisionsWitnessed++;
                }
                if (semi.checkOverlap(i, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(crashSound2Id, 0.5f, 0.5f, 0, 0, 1);
                    }
                    gS.collisionsWitnessed++;
                }
            }
        }
//        //Check if trucks have touched other obstacles.
        for (int i = 0; i < maxNumTrucks; i++) {
            if (truck.spawnTracker[i] == 1 || truck.spawnTracker[i] == 2) {
                if (cone.wasObstacleTouched(truck.coordinatesArray[i][0], truck.coordinatesArray[i][1], truck.obstacleWidth, truck.obstacleHeight, false, false, false, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(crashSound5Id, 0.5f, 0.5f, 0, 0, 1);
                    }
                    gS.collisionsWitnessed++;
                }
                if (crowd.wasObstacleTouched(truck.coordinatesArray[i][0], truck.coordinatesArray[i][1], truck.obstacleWidth, truck.obstacleHeight, false, true, false, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(chickenSound2Id, 1f, 1f, 0, 0, 1);
                        soundEffects.play(crashSound4Id, 0.5f, 0.5f, 0, 0, 1);
                    }
                    gS.collisionsWitnessed++;
                }
                if (homingOb.wasObstacleTouched(truck.coordinatesArray[i][0], truck.coordinatesArray[i][1], truck.obstacleWidth, truck.obstacleHeight, false, false, true, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(wilhelmScreamId, 0.5f, 0.5f, 0, 0, 1.2f);
                    }
                    gS.collisionsWitnessed++;
                }
                if (car.wasObstacleTouched(truck.coordinatesArray[i][0], truck.coordinatesArray[i][1], truck.obstacleWidth, truck.obstacleHeight, false, false, false, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(crashSound2Id, 0.5f, 0.5f, 0, 0, 1);
                    }
                    truck.hitObstacle(i, false, false, false, false);
                    gS.collisionsWitnessed++;
                }
                if (car2.wasObstacleTouched(truck.coordinatesArray[i][0], truck.coordinatesArray[i][1], truck.obstacleWidth, truck.obstacleHeight, false, false, false, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(crashSound2Id, 0.5f, 0.5f, 0, 0, 1);
                    }
                    truck.hitObstacle(i, false, false, false, false);
                    gS.collisionsWitnessed++;
                }
                if (truck.checkOverlap(i, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(crashSound2Id, 0.5f, 0.5f, 0, 0, 1);
                    }
                    gS.collisionsWitnessed++;
                }
            }
        }
        //Check if cars have touched other obstacles.
        for (int i = 0; i < maxNumCars; i++){
            if (car.spawnTracker[i] == 1 || car.spawnTracker[i] == 2){
                if(cone.wasObstacleTouched(car.coordinatesArray[i][0], car.coordinatesArray[i][1], car.obstacleWidth, car.obstacleHeight, false, false, false, false) != -1){
                    if (!mA.musicMuted) {
                        soundEffects.play(crashSound5Id, 0.5f, 0.5f, 0, 0, 1);
                    }
                    gS.collisionsWitnessed++;
                }
                if (crowd.wasObstacleTouched(car.coordinatesArray[i][0], car.coordinatesArray[i][1], car.obstacleWidth, car.obstacleHeight, false, true, false, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(chickenSound2Id, 1f, 1f, 0, 0, 1);
                        soundEffects.play(crashSound4Id, 0.5f, 0.5f, 0, 0, 1);
                    }
                    gS.collisionsWitnessed++;
                }
                if (homingOb.wasObstacleTouched(car.coordinatesArray[i][0] - 20, car.coordinatesArray[i][1], car.obstacleWidth, car.obstacleHeight, false, true, false, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(wilhelmScreamId, 0.5f, 0.5f, 0, 0, 1.2f);
                    }
                    gS.collisionsWitnessed++;
                }
                if(car2.wasObstacleTouched(car.coordinatesArray[i][0], car.coordinatesArray[i][1], car.obstacleWidth, car.obstacleHeight, false, false, false, false) != -1){
                    if (!mA.musicMuted) {
                        soundEffects.play(crashSound2Id, 0.5f, 0.5f, 0, 0, 1);
                    }
                    gS.collisionsWitnessed++;
                }
                if (car.checkOverlap(i, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(crashSound2Id, 0.5f, 0.5f, 0, 0, 1);
                    }
                    gS.collisionsWitnessed++;
                }
            }
        }
        for (int i = 0; i < maxNumCars2; i++) {
            if (car2.spawnTracker[i] == 1 || car2.spawnTracker[i] == 2) {
                if (cone.wasObstacleTouched(car2.coordinatesArray[i][0], car2.coordinatesArray[i][1], car2.obstacleWidth, car2.obstacleHeight, false, false, false, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(crashSound5Id, 0.5f, 0.5f, 0, 0, 1);
                    }
                    gS.collisionsWitnessed++;
                }
                if (crowd.wasObstacleTouched(car2.coordinatesArray[i][0], car2.coordinatesArray[i][1], car2.obstacleWidth, car2.obstacleHeight, false, true, false, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(chickenSound2Id, 1f, 1f, 0, 0, 1);
                        soundEffects.play(crashSound4Id, 0.5f, 0.5f, 0, 0, 1);
                    }
                    gS.collisionsWitnessed++;
                }
                if (homingOb.wasObstacleTouched(car2.coordinatesArray[i][0], car2.coordinatesArray[i][1], car2.obstacleWidth, car2.obstacleHeight, false, true, false, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(wilhelmScreamId, 0.5f, 0.5f, 0, 0, 1.2f);
                    }
                    gS.collisionsWitnessed++;
                }
                if (car2.checkOverlap(i, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(crashSound2Id, 0.5f, 0.5f, 0, 0, 1);
                    }
                    gS.collisionsWitnessed++;
                }
            }
        }

        for (int i = 0; i < maxNumCrowds; i++){
            if (crowd.spawnTracker[i] == 1) {
                boundary = 20;
                wc = crowd.obstacleWidth + 2*boundary;
                hc = crowd.obstacleHeight;
                obHit = truck.wasObstacleTouched(crowd.coordinatesArray[i][0], crowd.coordinatesArray[i][1] - boundary, wc, hc, false, false, false, true);
                if (obHit != -1) {
                    if (crowd.speedArray[i][0] < 0 && crowd.coordinatesArray[i][0] > truck.coordinatesArray[obHit][0]) {//crowd going left, toward truck
                        crowd.speedArray[i][0] *= -1;
                    } else if (crowd.speedArray[i][0] > 0 && crowd.coordinatesArray[i][0] < truck.coordinatesArray[obHit][0]) {//crowd going right, toward truck
                        crowd.speedArray[i][0] *= -1;
                    } //Else crowd is moving away from truck
                }
                obHit = car.wasObstacleTouched(crowd.coordinatesArray[i][0], crowd.coordinatesArray[i][1] - boundary, wc, hc, false, false, false, true);
                if (obHit != -1) {
                    if (crowd.speedArray[i][0] < 0 && crowd.coordinatesArray[i][0] > car.coordinatesArray[obHit][0]) {//crowd going left, toward car
                        crowd.speedArray[i][0] *= -1;
                    } else if (crowd.speedArray[i][0] > 0 && crowd.coordinatesArray[i][1] < car.coordinatesArray[obHit][0]) {//crowd going right, toward car
                        crowd.speedArray[i][0] *= -1;
                    } //Else crowd is moving away from car
                }
                if (cone.wasObstacleTouched(crowd.coordinatesArray[i][0], crowd.coordinatesArray[i][1], crowd.obstacleWidth, crowd.obstacleHeight, false, false, false, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(crashSound5Id, 0.5f, 0.5f, 0, 0, 1);
                    }
                    crowd.speedArray[i][0] *= -1;
                    crowd.speedArray[i][1] *= -1;
                    gS.collisionsWitnessed++;
                }
                obHit = crowd.checkOverlap(i, true);
                if (obHit != -1 && crowd.speedArray[i][1] * crowd.speedArray[obHit][1] < 0) {
                    crowd.speedArray[i][0] *= -1;
                    crowd.speedArray[i][1] *= -1;
                }
            }
        }

        for (int i = 0; i < homingObMaxNum; i++){
            if (homingOb.spawnTracker[i] == 1){
                if(cone.wasObstacleTouched(homingOb.coordinatesArray[i][0], homingOb.coordinatesArray[i][1], homingOb.obstacleWidth, homingOb.obstacleHeight, false, false, false, false) != -1){
                    if (!mA.musicMuted) {
                        soundEffects.play(crashSound5Id, 0.5f, 0.5f, 0, 0, 1);
                    }
                    gS.collisionsWitnessed++;
                }
            }
        }
    }

    //--------------------------Reset obstacles------------------------------------------
    public void resetObstacles() {
        cone.resetObstacles(distBetweenCones, backgroundWidth, backgroundHeight);
        semi.resetObstacles(distBetweenSemis, backgroundWidth, backgroundHeight);
        truck.resetObstacles(distBetweenTrucks, backgroundWidth, backgroundHeight);
        crowd.resetObstacles(distBetweenCrowds, backgroundWidth, backgroundHeight);
        car.resetObstacles(distBetweenCars, backgroundWidth, backgroundHeight);
        car2.resetObstacles(distBetweenCars, backgroundWidth, backgroundHeight);
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
            for(int i = 0; i < 30; i++){
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
            soundEffects = new SoundPool(30, AudioManager.STREAM_MUSIC, 0);
            badSoundId = soundEffects.load(context, R.raw.finger_runner_bad_sound, 1);
            goodSoundId = soundEffects.load(context, R.raw.coin_sound_2, 1);
            crashSound2Id = soundEffects.load(context, R.raw.finger_runner_crash_sound_2, 1);
            crashSound4Id = soundEffects.load(context, R.raw.finger_runner_crash_sound_4, 1);
            crashSound5Id = soundEffects.load(context, R.raw.finger_runner_crash_sound_5, 1);
            chickenSoundId = soundEffects.load(context, R.raw.chicken, 1);
            chickenSound2Id = soundEffects.load(context, R.raw.chicken_2, 1);
            wilhelmScreamId = soundEffects.load(context, R.raw.wilhelm_scream, 1);
            driveSoundId = soundEffects.load(context, R.raw.car_driving, 1);

//            soundEffects = mA.sfx;
//            badSoundId = mA.badSoundId;
//            goodSoundId = mA.goodSoundId;
//            crashSoundId = mA.crashSoundId;
//            crashSound2Id = mA.crashSound2Id;
//            crashSound4Id = mA.crashSound4Id;
//            crashSound5Id = mA.crashSound5Id;
//            wilhelmScreamId = mA.wilhelmScreamId;
        }
    }

//    public void setFpMode(boolean m){
//        this.fpMode = m;
//    }

}
