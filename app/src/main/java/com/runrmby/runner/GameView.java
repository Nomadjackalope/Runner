package com.runrmby.runner;

import android.content.AbstractThreadedSyncAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by benjamin on 1/31/17.
 *
 *  Do all game logic here. Ex. spawning obstacles
 *  Time based - the lowest time to finish is the best. Fixed distance. Quicker = better score
 *
 *  // Code used from
 *  http://gamecodeschool.com/android/building-a-simple-game-engine/
 *
 */
public class GameView extends SurfaceView implements Runnable {

    // Game thread
    Thread gameThread = null;

    SurfaceHolder holder;

    volatile boolean playing;

    Canvas canvas;
    Paint paint;

    Bitmap background;
    float backgroundPositionY;
    float backgroundPositionY2;

    Point windowSize;

    // Touches
    FingerPoint finger1 = new FingerPoint();
    FingerPoint finger2 = new FingerPoint();


    public GameView(Context context, Point windowSize) {
        super(context);
        init(windowSize);
    }

    public void init(Point p) {

        this.windowSize = p;
        holder = getHolder();
        paint = new Paint();

        BitmapFactory.Options ops = new BitmapFactory.Options();

        ops.inPreferredConfig = Bitmap.Config.RGB_565;

        background = BitmapFactory.decodeResource(this.getResources(), R.drawable.road, ops);

        System.out.println("GV| bitmap type: " + background.getConfig().name());

        //background = decodeSampledBitmapFromResource(getResources(), R.drawable.road, p.x, p.y);

        background = Bitmap.createScaledBitmap(background, p.x, p.y, true);

        backgroundPositionY2 = -background.getHeight();
    }

    // Bitmap processing
    // http://stackoverflow.com/questions/17839388/creating-a-scaled-bitmap-with-createscaledbitmap-in-android#17839663
    // https://developer.android.com/training/displaying-bitmaps/load-bitmap.html
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        // If image is larger in either dimesion than the requested dimension
        if(height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width
            while((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) <= reqWidth) {
                inSampleSize *=2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }



    @Override
    public void run() {
        while(playing) {

            update();

            draw();

        }
    }

    public void update() {
        //backgroundPositionY += 15; // This should be set by the person's touches
        //backgroundPositionY2 += 15;
        // 15 needs to be the amount that the background not being moved has travelled
        if(backgroundPositionY > background.getHeight()) {
            backgroundPositionY = -background.getHeight() + 15;
        }
        if(backgroundPositionY2 > background.getHeight()) {
            backgroundPositionY2 = -background.getHeight() + 15;
        }
    }

    public void draw() {

        if(holder.getSurface().isValid()) {

            // Lock the canvas to draw
            canvas = holder.lockCanvas();

            // Recolor canvas so to not have artifacts
            canvas.drawColor(Color.argb(255,255,0,0));

            // Draw road
            canvas.drawBitmap(background, 0, backgroundPositionY, paint);
            canvas.drawBitmap(background, 0, backgroundPositionY2, paint);

            // Draw all to screen and unlock
            holder.unlockCanvasAndPost(canvas);
        }

    }

    // The first touch is ACITON_DOWN

    // ACTION_POINTER_DOWN is for extra pointers that enter the screen beyond the first


    //

    int pointerIndex;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //final int action = MotionEventCompat.getActionMasked(event);

        switch(event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:

                // PointerId remains constant. Pointer index is order in touch list
                System.out.println("GV| action down");

                finger1.isNull = false;
                finger1.id = event.getPointerId(0);
                setMostRecentFinger(finger1.id);
                finger1.setXY(event.getX(), event.getY());


                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                //System.out.println("GV| action pointer down0: " + event.getPointerId(0));
                //System.out.println("GV| action pointer down1: " + event.getPointerId(1));

//                System.out.println("GV| find pointer index0: "
//                        + event.getX(event.findPointerIndex(event.getPointerId(0))));
//                System.out.println("GV| find pointer index1: "
//                        + event.getX(event.findPointerIndex(event.getPointerId(1))));

                if(event.getPointerCount() < 3) {
                    int id = 0;

                    // Figure out if I need to use index 0 or 1 to get the pointer id from
                    // If finger1 is null it needs an id
                    if (finger1.isNull) {
                        // To figure out the index we randomly choose one and
                        // see if it matches our non null finger
                        if(finger2.id == event.getPointerId(0)) {
                            finger1.id = event.getPointerId(1);
                        } else {
                            finger1.id = event.getPointerId(0);
                        }
                    } else {
                        if(finger1.id == event.getPointerId(0)) {
                            finger2.id = event.getPointerId(1);
                        } else {
                            finger2.id = event.getPointerId(0);
                        }

                    }


                    if (finger1.isNull) {
                        finger1.isNull = false;
                        setMostRecentFinger(finger1.id);

                        finger1.setXY(event.getX(event.findPointerIndex(finger1.id)), event.getY(event.findPointerIndex(finger1.id)));
                    } else if (finger2.isNull) {
                        finger2.isNull = false;
                        setMostRecentFinger(finger2.id);
                        finger1.setXY(event.getX(event.findPointerIndex(finger2.id)), event.getY(event.findPointerIndex(finger2.id)));
                    }
                }


            case MotionEvent.ACTION_MOVE:
                if(!finger1.isNull && finger1.mostRecent) {
                    System.out.println("GV| finger1 y: " + finger1.y);
                    advanceRoad(event.getY(event.findPointerIndex(finger1.id)) - finger1.y);
                    finger1.y = event.getY(event.findPointerIndex(finger1.id));
                } else if (!finger2.isNull && finger2.mostRecent) {
                    advanceRoad(event.getY(event.findPointerIndex(finger2.id)) - finger2.y);
                    finger2.y = event.getY(event.findPointerIndex(finger2.id));
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                if(event.getPointerId(0) == finger1.id) {
                    finger2.isNull = true;
                } else if(event.getPointerId(0) == finger2.id) {
                    finger1.isNull = true;
                } else {
                    // someone had 3 fingers on screen and lifted one
                    System.out.println("GV| APU 3 fingers ");
                }
                break;

            case MotionEvent.ACTION_UP:
                finger1.isNull = true;
                finger2.isNull = true;
                break;

        }
        return true;
    }

    public void setMostRecentFinger(int index) {
        if(index == 0) {
            finger1.mostRecent = true;
            finger2.mostRecent = false;
        } else if(index == 1) {
            finger1.mostRecent = false;
            finger2.mostRecent = true;
        }
    }

    public void advanceRoad(float distance) {
        System.out.println("GV| distance: " + distance);
        //distance = distance;
        backgroundPositionY += distance;
        backgroundPositionY2 += distance;
    }

    public FingerPoint getFingerFromIndex(int index) {
        if(index == 0) {
            return finger1;
        } else if(index == 1) {
            return finger2;
        } else {
            return null;
        }
    }

    // Call this from activity
    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "joining thread");
        }
    }

    // Call this from activity
    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public class FingerPoint {
        float x, y;

        int id;

        // Using this vs null because then we don't create a bunch of these objects
        boolean isNull = true;

        boolean mostRecent;

        public void setXY(float x, float y) {
            System.out.println("GV| finger1 y: " + y);
            this.x = x;
            this.y = y;
        }
    }
}
