package com.runrmby.runner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.SystemClock;
import android.view.MotionEvent;

/**
 * Created by Mark on 3/15/2017.
 */

public class LocationNormalRoad {

    private float courseDistance = 25000f;

    private Obstacles cone;
    private int coneResId = R.mipmap.cone;
    private int coneXScale = 74;
    private int coneYScale = 104;
    private int maxNumCones = 8;
    private float distBetweenCones = 6000f;
    private float coneXSpeed = 0f;
    private float coneYSpeed = 0f;

    private Obstacles truck;
    private int truckResId = R.mipmap.orange_truck;
    private int truckXScale = 277;
    private int truckYScale = 557;
    private int maxNumTrucks = 5;
    private float distBetweenTrucks = 5000f;
    private float truckXSpeed = 0f;
    private float truckYSpeed = 4f;

    private Obstacles semi;
    private int semiResId = R.mipmap.semi;
    private int semiXScale = 293;
    private int semiYScale = 1175;
    private int maxNumSemis = 2;
    private float distBetweenSemis = 10000f;
    private float semiXSpeed = 0f;
    private float semiYSpeed = 4f;
//
    private Obstacles guy;
    private int guyResId = R.mipmap.dude2;
    private int guyXScale = 114;
    private int guyYScale = 120;
    private int maxNumGuys = 8;
    private float distBetweenGuys = 1500f;
    private float guyXSpeed = 0f;
    private float guyYSpeed = 1.5f;

    private Obstacles crowd;
    private int crowdResId = R.mipmap.chicken;
    private int crowdXScale = 167;
    private int crowdYScale = 170;
    private int maxNumCrowds = 8;
    private float distBetweenCrowds = 5000f;
    private float crowdXSpeed = 2.5f;
    private float crowdYSpeed = 0f;

    private Obstacles car;
    private int carResId = R.mipmap.blue_bug;
    private int carXScale = 232;
    private int carYScale = 443;
    private int maxNumCars = 4;
    private float distBetweenCars = 2000f;
    private float carXSpeed = 0f;
    private float carYSpeed = 5f;

    private Obstacles car2;
    private int car2ResId = R.mipmap.red_bug;
    private int car2XScale = 232;
    private int car2YScale = 443;
    private int maxNumCars2 = 4;
    private float distBetweenCars2 = 3000f;
    private float car2XSpeed = 0f;
    private float car2YSpeed = 6f;

    private Obstacles homingOb;
    private int homingObResID = R.mipmap.dude;
    private int homingObXScale = 172;
    private int homingObYScale = 204;
    private int homingObWidth;
    private int homingObHeight;
    private int homingObMaxNum = 1;
    private float homingObDistBetween = 50000f;
    private float homingObXSpeed = 0f;
    private float homingObYSpeed = 0f;
    private float homingObHomingSpeed = 0.01f;

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

    private float velocityFactor = .75f; //Must be less than 1 or else road will advance exponentially.
//    private float distanceFactor = 1.00f;  //Must be <= 1 or else road will advance exponentially.
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

    private int backgroundWidth;
    private int backgroundHeight;

    float factor;
    int obHit;
    float boundary = 20f;
    float hc;

    private float lastY;
    private boolean stepFlag;
    boolean hopping;

    Context context;

    boolean fpMode = true; //If false, using touchFollower character instead of fpMode.

    public LocationNormalRoad(MainActivity mA, GameView gS, float x, float y, int bW, int bH) {
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
        truck = new Obstacles(context, (int) (sX * truckXScale), (int) (sY * truckYScale), truckResId, maxNumTrucks, false, sY * distBetweenTrucks, sX * truckXSpeed, sY * truckYSpeed, 0, backgroundWidth, backgroundHeight, true, true, true);
        semi = new Obstacles(context, (int) (sX * semiXScale), (int) (sY * semiYScale), semiResId, maxNumSemis, false, sY * distBetweenSemis, sX * semiXSpeed, sY * semiYSpeed, 0, backgroundWidth, backgroundHeight, true, true, true);
        guy = new Obstacles(context, (int) (sX * guyXScale), (int) (sY * guyYScale), guyResId, maxNumGuys, false, sY * distBetweenGuys, sX * guyXSpeed, sY * guyYSpeed, 0, backgroundWidth, backgroundHeight, true, false, false);
        crowd = new Obstacles(context, (int) (sX * crowdXScale), (int) (sY * crowdYScale), crowdResId, maxNumCrowds, false, sY * distBetweenCrowds, sX * crowdXSpeed, sY * crowdYSpeed, 0, backgroundWidth, backgroundHeight, true, false, false);
        car = new Obstacles(context, (int) (sX * carXScale), (int) (sY * carYScale), carResId, maxNumCars, false, sY * distBetweenCars, sX * carXSpeed, sY * carYSpeed, 0, backgroundWidth, backgroundHeight, true, true, true);
        car2 = new Obstacles(context, (int) (sX * car2XScale), (int) (sY * car2YScale), car2ResId, maxNumCars2, false, sY * distBetweenCars2, sX * car2XSpeed, sY * car2YSpeed, 0, backgroundWidth, backgroundHeight, true, true, true);
        homingOb = new Obstacles(context, homingObWidth, homingObHeight, homingObResID, homingObMaxNum, false, sY * homingObDistBetween, sX * homingObXSpeed, sY * homingObYSpeed, sY * homingObHomingSpeed, backgroundWidth, backgroundHeight + backgroundHeight, false, false, false);
        coins = new Obstacles(context, (int) (sX * coinScale), (int) (sY * coinScale), coinsResId, maxNumCoins, false, sY * distBetweenCoins, sX * coinsHorizontalSpeed, sY * coinsVerticalSpeed, 0, backgroundWidth, backgroundHeight, true, false, false);

        //TODO: make sure 100 is okay
        int limit = (int)(sX * 100);
        truck.setLimitSpawnX(limit, backgroundWidth - limit - truck.getObstacleWidth(), false);
        semi.setLimitSpawnX(limit, backgroundWidth - limit - semi.getObstacleWidth(), false);
        car.setLimitSpawnX(limit, backgroundWidth - limit - car.getObstacleWidth(), false);
        car2.setLimitSpawnX(limit, backgroundWidth - limit - car2.getObstacleWidth(), false);
        cone.setLimitSpawnX(0, 10, true);
        guy.setLimitSpawnX(0, 1, true);
        coins.setLimitSpawnX(coins.getObstacleWidth(), backgroundWidth - coins.getObstacleWidth(), false);

        crowd.setDirectionalX(true);
        crowd.setRotatedObsImage2(R.mipmap.chicken_right, (int) (sX * crowdXScale), (int) (sY * crowdYScale));

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
        truck.updateDistanceBetweenObstacles(factor);
        semi.updateDistanceBetweenObstacles(factor);
        guy.updateDistanceBetweenObstacles(factor);
        crowd.updateDistanceBetweenObstacles(factor);
        car.updateDistanceBetweenObstacles(factor);
        car2.updateDistanceBetweenObstacles(factor);
        homingOb.updateDistanceBetweenObstacles(factor);

        factor = 1f / (difficultly + 8f);
        updateHomingSpeed(factor);
        car.increaseVerticalSpeed(factor);
        car2.increaseVerticalSpeed(factor);
        truck.increaseVerticalSpeed(factor);
        semi.increaseVerticalSpeed(factor);
        guy.increaseHorizontalSpeed(factor);
        crowd.increaseHorizontalSpeed(factor);
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
        guy.drawObstacles(canvas, paint, interpolation, velocity);
        crowd.drawObstacles(canvas, paint, interpolation, velocity);
        homingOb.drawObstacles(canvas, paint, interpolation, velocity);
        car2.drawObstacles(canvas, paint, interpolation, velocity);
        car.drawObstacles(canvas, paint, interpolation, velocity);
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
        guy.updateObstacles(distance, true);
        if(crowd.updateObstacles(distance, true)){
            if(!mA.musicMuted){
                soundEffects.play(chickenSoundId, 1f, 1f, 0, 0, 1);
            }
        }
        if (car.updateObstacles(distance, true)) {
            if (!mA.musicMuted) {
                soundEffects.play(driveSoundId, 0.4f, 0.4f, 0, 0, 1.8f);
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
        truck.moveObstacles();
        semi.moveObstacles();
        guy.moveObstacles();
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
                } else if (truck.wasObstacleTouched(footprintsR.coordinatesArray[i][0], footprintsR.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
                    return true;
                } else if (semi.wasObstacleTouched(footprintsR.coordinatesArray[i][0], footprintsR.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
                    return true;
                } else if (guy.wasObstacleTouched(footprintsR.coordinatesArray[i][0], footprintsR.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
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
                } else if (truck.wasObstacleTouched(footprintsL.coordinatesArray[i][0], footprintsL.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
                    return true;
                } else if (semi.wasObstacleTouched(footprintsL.coordinatesArray[i][0], footprintsL.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
                    return true;
                } else if (guy.wasObstacleTouched(footprintsL.coordinatesArray[i][0], footprintsL.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
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

    public void checkCollisions() {
        for (int i = 0; i < maxNumSemis; i++) {
            if (semi.spawnTracker[i] == 1 || semi.spawnTracker[i] == 2) {
//            if(semi.speedArray[i][1] != 0) {
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
//                if (truck.speedArray[i][1] != 0) { //Truck should only hit obstacles if it's moving.
                if (cone.wasObstacleTouched(truck.coordinatesArray[i][0], truck.coordinatesArray[i][1], truck.obstacleWidth, truck.obstacleHeight, false, false, false, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(crashSound5Id, 0.5f, 0.5f, 0, 0, 1);
                    }
                    gS.collisionsWitnessed++;
                }
                if (guy.wasObstacleTouched(truck.coordinatesArray[i][0], truck.coordinatesArray[i][1], truck.obstacleWidth, truck.obstacleHeight, false, true, false, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(wilhelmScreamId, 0.5f, 0.5f, 0, 0, 1.1f);
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
        for (int i = 0; i < maxNumCars; i++) {
            if (car.spawnTracker[i] == 1 || car.spawnTracker[i] == 2) {
                if (cone.wasObstacleTouched(car.coordinatesArray[i][0], car.coordinatesArray[i][1], car.obstacleWidth, car.obstacleHeight, false, false, false, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(crashSound5Id, 0.5f, 0.5f, 0, 0, 1);
                    }
                    gS.collisionsWitnessed++;
                }
                if (guy.wasObstacleTouched(car.coordinatesArray[i][0], car.coordinatesArray[i][1], car.obstacleWidth, car.obstacleHeight, false, true, false, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(wilhelmScreamId, 0.5f, 0.5f, 0, 0, 1.1f);
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
                if (truck.wasObstacleTouched(car.coordinatesArray[i][0], car.coordinatesArray[i][1], car.obstacleWidth, car.obstacleHeight, false, false, false, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(crashSound2Id, 0.5f, 0.5f, 0, 0, 1);
                    }
                    gS.collisionsWitnessed++;
                }
                if (car2.wasObstacleTouched(car.coordinatesArray[i][0], car.coordinatesArray[i][1], car.obstacleWidth, car.obstacleHeight, false, false, false, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(crashSound2Id, 0.5f, 0.5f, 0, 0, 1);
                    }
                    car.hitObstacle(i, false, false, false, false);
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
                if (truck.wasObstacleTouched(car2.coordinatesArray[i][0], car2.coordinatesArray[i][1], car2.obstacleWidth, car2.obstacleHeight, false, false, false, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(crashSound2Id, 0.5f, 0.5f, 0, 0, 1);
                    }
                    gS.collisionsWitnessed++;
                }
                if (car.wasObstacleTouched(car2.coordinatesArray[i][0], car2.coordinatesArray[i][1], car2.obstacleWidth, car2.obstacleHeight, false, false, false, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(crashSound2Id, 0.5f, 0.5f, 0, 0, 1);
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
        for (int i = 0; i < maxNumGuys; i++) {
            if (guy.spawnTracker[i] == 1) {
                hc = guy.obstacleHeight + 2 * boundary;
                obHit = truck.wasObstacleTouched(guy.coordinatesArray[i][0], guy.coordinatesArray[i][1] - boundary, guy.obstacleWidth, hc, false, false, false, true);
                if (obHit != -1) {
                    if (guy.speedArray[i][1] < 0 && guy.coordinatesArray[i][1] > truck.coordinatesArray[obHit][1]) {//guy going up, toward truck
                        guy.speedArray[i][1] *= -1;
                    } else if (guy.speedArray[i][1] > 0 && guy.coordinatesArray[i][1] < truck.coordinatesArray[obHit][1]) {//guy going down, toward truck
                        guy.speedArray[i][1] *= -1;
                    } //Else guy is moving away from truck
                }
                obHit = car.wasObstacleTouched(guy.coordinatesArray[i][0], guy.coordinatesArray[i][1] - boundary, guy.obstacleWidth, hc, false, false, false, true);
                if (obHit != -1) {
                    if (guy.speedArray[i][1] < 0 && guy.coordinatesArray[i][1] > car.coordinatesArray[obHit][1]) {//guy going up, toward car
                        guy.speedArray[i][1] *= -1;
                    } else if (guy.speedArray[i][1] > 0 && guy.coordinatesArray[i][1] < car.coordinatesArray[obHit][1]) {//guy going down, toward car
                        guy.speedArray[i][1] *= -1;
                    } //Else guy is moving away from car
                }
                obHit = car2.wasObstacleTouched(guy.coordinatesArray[i][0], guy.coordinatesArray[i][1] - boundary, guy.obstacleWidth, hc, false, false, false, true);
                if (obHit != -1) {
                    if (guy.speedArray[i][1] < 0 && guy.coordinatesArray[i][1] > car2.coordinatesArray[obHit][1]) {//guy going up, toward car
                        guy.speedArray[i][1] *= -1;
                    } else if (guy.speedArray[i][1] > 0 && guy.coordinatesArray[i][1] < car2.coordinatesArray[obHit][1]) {//guy going down, toward car
                        guy.speedArray[i][1] *= -1;
                    } //Else guy is moving away from car
                }
                if (cone.wasObstacleTouched(guy.coordinatesArray[i][0], guy.coordinatesArray[i][1], guy.obstacleWidth, guy.obstacleHeight, false, false, false, false) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(crashSound5Id, 0.5f, 0.5f, 0, 0, 1);
                    }
                    guy.speedArray[i][0] *= -1;
                    guy.speedArray[i][1] *= -1;
                    gS.collisionsWitnessed++;
                }
                obHit = guy.checkOverlap(i, true);
                if (obHit != -1) {
                    if (guy.speedArray[i][1] > 0 && guy.speedArray[obHit][1] < 0) {//guys going opposite directions, hit guy going up
                        if (guy.coordinatesArray[i][1] < guy.coordinatesArray[obHit][1]) {//hit guy is below
                            guy.speedArray[i][1] *= -1;
                            guy.speedArray[obHit][1] *= -1;
                        } else {//hit guy is above
                            continue;
                        }
                    } else if (guy.speedArray[i][1] < 0 && guy.speedArray[obHit][1] > 0) {//guys going opposite directions, hit guy going down
                        if (guy.coordinatesArray[i][1] > guy.coordinatesArray[obHit][1]) {//hit guy is above
                            guy.speedArray[i][1] *= -1;
                            guy.speedArray[obHit][1] *= -1;
                        } else {
                            continue;
                        }
                    } else if (guy.speedArray[i][1] < 0 && guy.speedArray[i][1] < guy.speedArray[obHit][1]) {//both going up and hit guy slower
                        guy.speedArray[i][1] *= -1;
                    } else if (guy.speedArray[i][1] < 0 && guy.speedArray[i][1] > guy.speedArray[obHit][1]) {//both going up and hit guy faster
                        guy.speedArray[obHit][1] *= -1;
                    } else if (guy.speedArray[i][1] > 0 && guy.speedArray[i][1] > guy.speedArray[obHit][1]) {//both going down and hit guy slower
                        guy.speedArray[i][1] *= -1;
                    } else {//both going down and hit guy faster
                        guy.speedArray[obHit][1] *= -1;
                    }
                }
            }
        }

        for (int i = 0; i < maxNumCrowds; i++) {
            if (crowd.spawnTracker[i] == 1) {
                float boundary = 20;
                float wc = crowd.obstacleWidth + 2 * boundary;
                float hc = crowd.obstacleHeight;// + 2 * boundary;
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
                obHit = car2.wasObstacleTouched(guy.coordinatesArray[i][0], guy.coordinatesArray[i][1] - boundary, wc, hc, false, false, false, true);
                if (obHit != -1) {
                    if (crowd.speedArray[i][0] < 0 && crowd.coordinatesArray[i][0] > car2.coordinatesArray[obHit][0]) {//crowd going left, toward car
                        crowd.speedArray[i][0] *= -1;
                    } else if (crowd.speedArray[i][0] > 0 && crowd.coordinatesArray[i][0] < car2.coordinatesArray[obHit][0]) {//crowd going right, toward car
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
                int c2 = crowd.checkOverlap(i, true);
                if (c2 != -1 && crowd.speedArray[i][1] * crowd.speedArray[c2][1] < 0) {
                    crowd.speedArray[i][0] *= -1;
                    crowd.speedArray[i][1] *= -1;
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
        truck.resetObstacles(distBetweenTrucks, backgroundWidth, backgroundHeight);
        semi.resetObstacles(distBetweenSemis, backgroundWidth, backgroundHeight);
        guy.resetObstacles(distBetweenGuys, backgroundWidth, backgroundHeight);
        crowd.resetObstacles(distBetweenCrowds, backgroundWidth, backgroundHeight);
        car.resetObstacles(distBetweenCars, backgroundWidth, backgroundHeight);
        car2.resetObstacles(distBetweenCars, backgroundWidth, backgroundHeight);
        coins.resetObstacles(distBetweenCoins, backgroundWidth, backgroundHeight);
        homingOb.resetObstacles(homingObDistBetween, backgroundWidth, backgroundHeight);
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
            for(int i = 0; i < 20; i++){
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
            soundEffects = new SoundPool(20, AudioManager.STREAM_MUSIC, 0);
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
