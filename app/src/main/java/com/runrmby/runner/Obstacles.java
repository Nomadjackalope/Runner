package com.runrmby.runner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import java.util.Random;

/**
 * Created by Mark on 2/10/2017.
 * Creates a set of similar obstacles.
 */

public class Obstacles {

    //int obstacleImageResID;
    Bitmap obstacleImage;
    int maxNumberOfObstacles;
    int scaleX;
    int scaleY;
    float distanceBetweenObstacles;
    float originalHSpeed;
    float originalVSpeed;
    int windowWidth;
    int windowHeight;
    //float nextObstacleAt;
    float horizontalSpeed;
    float verticalSpeed;
    float distanceToNextObstacle;
    int obstacleWidth;
    int obstacleHeight;
    Boolean[] spawnTracker;
    float[][] coordinatesArray;
    float[][] speedArray;
    static Boolean obstacleDestroyed = false;
    static Boolean obstacleSpawned = true;
    Random random = new Random();
    Boolean respawnWithMax;
    int lastSpawnIndex;
    Boolean randomizeParameters;

    /**
     * @param randomize allows certain parameters to have variation.
     * //TODO: add two more columns to coordinates array to keep track of horizontal and vertical speed for individual obstacles so that randomizing doesn't affect spawned obstacles, even though that's kind of fun.
     */
    public Obstacles(Context context, int scaleX, int scaleY, int obstacleImageResID, int maxNumberOfObstacles, Boolean respawnWithMax, float distanceBetweenObstacles, float horizontalSpeed, float verticalSpeed, int windowWidth, int windowHeight, Boolean randomize){
        this.obstacleImage = BitmapFactory.decodeResource(context.getResources(), obstacleImageResID, null);
        this.obstacleImage = Bitmap.createScaledBitmap(this.obstacleImage, scaleX, scaleY, true);
        this.maxNumberOfObstacles = maxNumberOfObstacles;
        this.respawnWithMax = respawnWithMax;
        this.distanceBetweenObstacles = distanceBetweenObstacles;
        this.originalHSpeed = horizontalSpeed;
        this.horizontalSpeed = originalHSpeed;
        this.originalVSpeed = verticalSpeed;
        this.verticalSpeed = originalVSpeed;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        //this.nextObstacleAt = distanceBetweenObstacles;
        this.distanceToNextObstacle = distanceBetweenObstacles;
        this.obstacleWidth = obstacleImage.getWidth();
        this.obstacleHeight = obstacleImage.getHeight();
        this.spawnTracker = new Boolean[maxNumberOfObstacles];
        this.coordinatesArray = new float[maxNumberOfObstacles][2];
        this.speedArray = new float[maxNumberOfObstacles][2];
        this.randomizeParameters = randomize;
        this.lastSpawnIndex = maxNumberOfObstacles - 1;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    public void updateObstacles(float distance, boolean autoSpawn) {
        //Update distance moved.
        if(maxNumberOfObstacles > 0) {
            for (int i = 0; i < maxNumberOfObstacles; i++) {
                if (spawnTracker[i] == obstacleSpawned) {
                    coordinatesArray[i][1] += distance;
                }
                //If now off screen, set as destroyed.
                if (coordinatesArray[i][1] > windowHeight || coordinatesArray[i][1] < -obstacleHeight || coordinatesArray[i][0] > windowWidth || coordinatesArray[i][0] < -obstacleWidth) {
                    spawnTracker[i] = obstacleDestroyed;
                    //Could reset coordinates here, but doesn't seem necessary as they are always set when a spawn occurs.
                }
            }

            distanceToNextObstacle -= distance;

            if (autoSpawn) {
                spawnObstacle(distanceToNextObstacle, random.nextInt(windowWidth - obstacleWidth), -obstacleHeight);
            }
        }
    }

    public void spawnObstacle(float distance, float x, float y){
        //If enough distance has been covered and not at max spawns, spawn an obstacle.
        if(maxNumberOfObstacles > 0) {
            if (distance <= 0) {
                if (lastSpawnIndex < maxNumberOfObstacles - 1) {
                    lastSpawnIndex++;
                } else {
                    lastSpawnIndex = 0;
                }
                //for(int i = 0; i < maxNumberOfObstacles; i++){
                if (spawnTracker[lastSpawnIndex] == obstacleDestroyed || respawnWithMax) {
                    spawnTracker[lastSpawnIndex] = obstacleSpawned;
                    coordinatesArray[lastSpawnIndex][0] = x;
                    coordinatesArray[lastSpawnIndex][1] = y;
                    if (!randomizeParameters) {
                        distanceToNextObstacle = distanceBetweenObstacles;
                    } else {
                        //If randomizeParameters selected, vary several parameters for each spawn. !!!THIS AFFECTS MOTION OF ALL SPAWNED OBSTACLES!!!
                        distanceToNextObstacle = distanceBetweenObstacles * (random.nextInt(7) + 1) / 4;
                        horizontalSpeed = originalHSpeed * ((random.nextInt(15) - 8) / 4);
                        verticalSpeed = originalVSpeed * ((random.nextInt(15) - 8) / 4);
                        speedArray[lastSpawnIndex][0] = horizontalSpeed;
                        speedArray[lastSpawnIndex][1] = verticalSpeed;
//                        if (random.nextBoolean()) {
//                            horizontalSpeed = -originalHSpeed;
//                        }
//                        if (random.nextBoolean()){
//                            verticalSpeed = -originalVSpeed;
//                        }

                    }
                    //break;
                }
                //}
            }
        }
    }

    public void destroyObstacle(int obsIndex){
        spawnTracker[obsIndex] = obstacleDestroyed;
    }

    public void moveObstacles() {
        for (int i = 0; i < maxNumberOfObstacles; i++) {
            if (spawnTracker[i] == obstacleSpawned) {
                if (coordinatesArray[i][0] < windowWidth) {
                    coordinatesArray[i][0] += speedArray[i][0];
                }
                if (coordinatesArray[i][1] < windowHeight) {
                    coordinatesArray[i][1] += speedArray[i][1];
                }
            }
        }
    }

    public void drawObstacles(Canvas canvas, Paint paint) {
        for (int i = 0; i < maxNumberOfObstacles; i++) {
            if (spawnTracker[i] == obstacleSpawned) {
//                if(speedArray[i][1] < 0){
//                    Matrix matrix = new Matrix();
//                    matrix.postRotate(180);
//                    Bitmap rotatedObsImage = Bitmap.createBitmap(obstacleImage,);
//                }
                canvas.drawBitmap(obstacleImage, coordinatesArray[i][0], coordinatesArray[i][1], paint);
            }
        }
    }

    //x and y are view coordinates. Width and height should be 0f if checking a point instead of an area.
    public Boolean wasObstacleTouched(float x, float y, float width, float height, boolean destroy) {
        for (int i = 0; i < maxNumberOfObstacles; i++) {
            if (spawnTracker[i] == obstacleSpawned) {
                if (x + width > coordinatesArray[i][0] && x < coordinatesArray[i][0] + obstacleWidth
                        && y + height > coordinatesArray[i][1] && y < coordinatesArray[i][1] + obstacleHeight) {
                    if(destroy) {
                        destroyObstacle(i);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public void resetObstacles(){
        this.distanceToNextObstacle = distanceBetweenObstacles;
        //this.nextObstacleAt = distanceBetweenObstacles;
        for(int i = 0; i < maxNumberOfObstacles; i++){
            spawnTracker[i] = obstacleDestroyed;
        }
        this.lastSpawnIndex = maxNumberOfObstacles - 1;
    }

    public void resetObstacles(float distanceBetweenObstacles, int windowWidth, int windowHeight){
        resetObstacles();
        this.distanceBetweenObstacles = distanceBetweenObstacles;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
    }

    public int getObstacleWidth(){
        return this.obstacleWidth;
    }

    public int getObstacleHeight(){
        return this.obstacleHeight;
    }

    public void setDistanceBetweenObstacles(float d){
        this.distanceBetweenObstacles = d;
    }
}

