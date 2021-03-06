package com.m2dl.sheraf.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.m2dl.sheraf.enums.TypePins;
import com.m2dl.sheraf.ObstacleSize;
import com.m2dl.sheraf.dynamics.elements.Background;
import com.m2dl.sheraf.dynamics.elements.Player;
import com.m2dl.sheraf.enums.LightValue;
import com.m2dl.sheraf.dynamics.elements.Obstacle;
import com.m2dl.sheraf.dynamics.elements.Pins;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import static android.content.ContentValues.TAG;

public class GameView extends SurfaceView implements  Runnable {

    private final int screenWidth;
    private final int screenHeight;
    private Canvas canvas;
    private long fps;
    private long timeThisFrame;
    private Thread gameThread = null;
    private SurfaceHolder ourHolder;
    private volatile boolean playing;
    private float obstacleSpeed = -1000;
    private float pinSpeed = -1000;
    private Player player;

    private Background background;
    private ArrayList<Obstacle> obstacles;
    private ArrayList<Pins> pins;
    private LightValue lightValue = LightValue.LOW;

    public GameView(Context context) {
        super(context);
        // Initialize ourHolder and paint objects
        ourHolder = getHolder();

        player = new Player(context, 30, 30);
        background = new Background(context, obstacleSpeed);
        obstacles = new ArrayList<>();
        pins = new ArrayList<>();

        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        // Set our boolean to true - game on!
        playing = true;
    }

    @Override
    public void run() {
        while (playing) {

            // Capture the current time in milliseconds in startFrameTime
            long startFrameTime = System.currentTimeMillis();

            // Update the frame
            update();

            // Draw the frame
            draw();

            // Calculate the fps this frame
            // We can then use the result to
            // time animations and more.
            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame > 0) {
                fps = 1000 / timeThisFrame;
            }

        }
    }

    private void draw() {
        if (ourHolder.getSurface().isValid()) {
            // Lock the canvas ready to draw
            canvas = ourHolder.lockCanvas();

            // Draw bob at bobXPosition, 200 pixels
            background.draw(canvas);

            for (Obstacle obstacle: obstacles) {
                obstacle.draw(canvas);
            }
            for (Pins pin: pins) {
                pin.draw(canvas);
            }

            player.draw(canvas);
            // Draw everything to the screen
            ourHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void update() {
        player.update(fps);
        background.update(fps);
        if (fps != 0) {
            generateRandomObstacle();
            for (Iterator<Obstacle> iterator = obstacles.iterator(); iterator.hasNext(); ) {
                Obstacle obstacle = iterator.next();
                obstacle.update(fps);
                if (obstacle.getxPosition() + obstacle.getWidth() < 0) {
                    iterator.remove();
                }
            }

            generateRandomPin();
            for (Iterator<Pins> iterator = pins.iterator(); iterator.hasNext(); ) {
                Pins pin = iterator.next();
                pin.update(fps);
                if (pin.getxPosition() + Pins.size < 0) {
                    iterator.remove();
                }
            }

            checkHitBox();
        }
    }

    private void checkHitBox() {


        for (Iterator<Obstacle> iterator = obstacles.iterator(); iterator.hasNext(); ) {
            Obstacle obstacle = iterator.next();
            if (player.touchObstacle(obstacle)) {
                Log.d(TAG, "checkHitBox: Obstacle touched");
                //iterator.remove();
                looseGame();
            }
        }

        for (Iterator<Pins> iterator = pins.iterator(); iterator.hasNext(); ) {
            Pins pin = iterator.next();
            if (player.touchPins(pin)) {
                player.pickupPins(pin);
                Log.d(TAG, "checkHitBox: Pin touched");
                iterator.remove();
            }
        }
    }

    private void looseGame() {
        playing = false;
    }

    private int nbUpdateObstacle = 0;
    private int nbUpdateBerforeGenerationObstacle = 1000;
    private void generateRandomObstacle() {
        Random rn = new Random();
        if (nbUpdateObstacle == nbUpdateBerforeGenerationObstacle) {
            if (rn.nextBoolean()) {
                int randomSize = getRandomInt(1, 2);

                switch (randomSize) {
                    case 1:
                        obstacles.add(new Obstacle(getContext(), ObstacleSize.SMALL, obstacleSpeed));
                        break;
                    case 2:
                        obstacles.add(new Obstacle(getContext(), ObstacleSize.MEDIUM, obstacleSpeed));
                        break;
                }

                setRandomNbUpdateObstacleBerforeGenerationDependingOnDifficulty();
                nbUpdateObstacle = 0;
            }
        } else {
            nbUpdateObstacle++;
        }
    }

    private int nbUpdatePin = 0;
    private int nbUpdateBerforeGenerationPin = 1500;
    private void generateRandomPin() {
        Random rn = new Random();
        if (nbUpdatePin == nbUpdateBerforeGenerationPin) {
            if (rn.nextBoolean()) {
                int randomSize = getRandomInt(0, 100);

                if (randomSize > 10) {
                    pins.add(new Pins(getContext(), TypePins.OUICHE, pinSpeed));
                } else {
                    pins.add(new Pins(getContext(), TypePins.SHERAF, pinSpeed));
                }

                setRandomNbUpdatePinBerforeGenerationDependingOnDifficulty();
                nbUpdatePin = 0;
            }
        } else {
            nbUpdatePin++;
        }
    }

    private void setRandomNbUpdateObstacleBerforeGenerationDependingOnDifficulty() {
        //TODO write algo pour générer random en fonction de la difficulté
        switch (lightValue) {
            case LOW :
                nbUpdateBerforeGenerationObstacle = getRandomInt(75, 100);
                obstacleSpeed = -1500;
                break;
            case MEDIUM :
                nbUpdateBerforeGenerationObstacle = getRandomInt(75, 100);
                obstacleSpeed = -1000;
                break;
            case HIGH :
                nbUpdateBerforeGenerationObstacle = getRandomInt(75, 100);
                obstacleSpeed = -750;
                break;
        }
    }

    private void setRandomNbUpdatePinBerforeGenerationDependingOnDifficulty() {
        //TODO write algo pour générer random en fonction de la difficulté
        switch (lightValue) {
            case HIGH :
                nbUpdateBerforeGenerationPin = getRandomInt(75, 100);
                break;
            case MEDIUM :
                nbUpdateBerforeGenerationPin = getRandomInt(100, 200);
                break;
            case LOW :
                nbUpdateBerforeGenerationPin = getRandomInt(200, 400);
                break;
        }
    }

    private int getRandomInt(int min, int max) {
        Random rn = new Random();
        return rn.nextInt(max - min + 1) + min;
    }

    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "joining thread");
        }

    }

    // If SimpleGameEngine Activity is started then
    // start our thread.
    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

            // Player has touched the screen
            case MotionEvent.ACTION_DOWN:
                player.jump();
                break;

            // Player has removed finger from screen
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    public void onShake() {
        Log.d("SENSOR", "onShout: Non parce que moi je suis un spécialiste de la ouiche lorraine");
        player.tryEatOuiche();
    }

    public void onShout(int value) {
        if(player.isLimiteNervousBreakdown()){
            Log.d("SENSOR", "onShout: SHOUT SHOUT LET IT ALL OUT" + value);
            obstacles.clear();
            setRandomNbUpdateObstacleBerforeGenerationDependingOnDifficulty();
            nbUpdateBerforeGenerationObstacle+=500;
            player.setNervousBreakdown(false);
        }
    }

    public void setLight(LightValue value) {
        this.lightValue = value;
    }
}
