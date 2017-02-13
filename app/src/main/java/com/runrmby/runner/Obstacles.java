package com.runrmby.runner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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
    static Boolean obstacleDestroyed = false;
    static Boolean obstacleSpawned = true;
    Random random = new Random();
    Boolean randomizeParameters;

    /**
     * @param randomize allows certain parameters to have variation.
     * //TODO: add two more columns to coordinates array to keep track of horizontal and vertical speed for individual obstacles so that randomizing doesn't affect spawned obstacles, even though that's kind of fun.
     */
    public Obstacles(Context context, int obstacleImageResID, int maxNumberOfObstacles, float distanceBetweenObstacles, float horizontalSpeed, float verticalSpeed, int windowWidth, int windowHeight, Boolean randomize){
        this.obstacleImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.practice3_small, null);
        this.maxNumberOfObstacles = maxNumberOfObstacles;
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
        this.randomizeParameters = randomize;
    }

    public void updateObstacles(float distance){
        //Update distance moved.
        for(int i = 0; i < maxNumberOfObstacles; i++){
            if(spawnTracker[i] == obstacleSpawned){
                coordinatesArray[i][1] += distance;
            }
            //If now off screen, set as destroyed.
            if(coordinatesArray[i][1] > windowHeight || coordinatesArray[i][1] < -obstacleHeight || coordinatesArray[i][0] > windowWidth || coordinatesArray[i][0] < -obstacleWidth){
                spawnTracker[i] = obstacleDestroyed;
                //Could reset coordinates here, but doesn't seem necessary as they are always set when a spawn occurs.
            }
        }

        distanceToNextObstacle -= distance;

        //If enough distance has been covered and not at max spawns, spawn an obstacle.
        if(distanceToNextObstacle < 0){
            for(int i = 0; i < maxNumberOfObstacles; i++){
                if(spawnTracker[i] == obstacleDestroyed){
                    spawnTracker[i] = obstacleSpawned;
                    coordinatesArray[i][0] = random.nextInt(windowWidth - obstacleWidth);
                    coordinatesArray[i][1] = -obstacleHeight;
                    if(!randomizeParameters) {
                        distanceToNextObstacle = distanceBetweenObstacles;
                    } else {
                        //If randomizeParameters selected, vary several parameters for each spawn. !!!THIS AFFECTS MOTION OF ALL SPAWNED OBSTACLES!!!
                        distanceToNextObstacle = distanceBetweenObstacles * (random.nextInt(7) + 1) / 4;
                        horizontalSpeed = originalHSpeed * ((random.nextInt(7) + 1) / 4);
                        verticalSpeed = originalVSpeed * ((random.nextInt(7) + 1) / 4);
                        if (random.nextBoolean()) {
                            horizontalSpeed = -originalHSpeed;
                        }
//                        if (random.nextBoolean()){
//                            verticalSpeed = -originalVSpeed;
//                        }

                    }
                    break;
                }
            }
        }
    }

    public void drawObstacles(Canvas canvas, Paint paint){
        for(int i = 0; i < maxNumberOfObstacles; i++) {
            if(spawnTracker[i] == obstacleSpawned) {
                if(coordinatesArray[i][0] < windowWidth){
                    coordinatesArray[i][0] += horizontalSpeed;
                }
                if(coordinatesArray[i][1] < windowHeight){
                    coordinatesArray[i][1] += verticalSpeed;
                }
                canvas.drawBitmap(obstacleImage, coordinatesArray[i][0], coordinatesArray[i][1], paint);
            }
        }
    }

    //x and y are view coordinates. Width and height should be 0f if checking a point instead of an area.
    public Boolean wasObstacleTouched(float x, float y, float width, float height) {
        for (int i = 0; i < maxNumberOfObstacles; i++) {
            if (spawnTracker[i] == obstacleSpawned) {
                if (x + width > coordinatesArray[i][0] && x < coordinatesArray[i][0] + obstacleWidth
                        && y + height > coordinatesArray[i][1] && y < coordinatesArray[i][1] + obstacleHeight) {
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
    }
}

