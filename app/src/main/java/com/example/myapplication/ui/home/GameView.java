package com.example.myapplication.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameView extends View {
    private int currentLevel = 1;
    private final List<Shape> shapes = new ArrayList<>();
    private final Random random = new Random();
    private GameEventListener gameEventListener;
    private Shape targetShape;
    private long timeLimit = 10000; // Default time limit for level 1
    private CountDownTimer gameTimer;
    private final Handler handler = new Handler();
    private static final int UPDATE_MILLIS = 50;
    private final float velocityIncrement = 5.0f;

    public enum ShapeType {
        CIRCLE, RECTANGLE, OVAL, SQUARE
    }

    public enum ColorName {
        RED(Color.RED, "Red"),
        BLUE(Color.BLUE, "Blue"),
        GREEN(Color.GREEN, "Green"),
        YELLOW(Color.YELLOW, "Yellow"),
        CYAN(Color.CYAN, "Cyan"),
        ORANGE(Color.rgb(255, 165, 0), "Orange"),
        BLACK(Color.BLACK, "Black"),
        GRAY(Color.GRAY, "GRAY");

        private final String colorName;

        private final int colorValue;

        ColorName(int colorValue, String colorName) {
            this.colorValue = colorValue;
            this.colorName = colorName;
        }

        public String getColorName() {
            return colorName;
        }

        public int getColorValue() {
            return colorValue;
        }

        public static int[] getPaletteForVisionType(String visionType) {
            int[] palette;
            switch (visionType) {
                case "Protanopia":
                    palette = new int[]{Color.MAGENTA, Color.YELLOW, Color.GRAY, Color.CYAN, Color.rgb(255, 165, 0)};
                    Log.d("GAME_PALETTE", "Protanopia palette: " + Arrays.toString(palette));
                    break; // Add a break after each case
                case "Deuteranopia":
                    palette = new int[]{Color.BLUE, Color.YELLOW, Color.GRAY, Color.CYAN, Color.BLACK};
                    Log.d("GAME_PALETTE", "Deuteranopia palette: " + Arrays.toString(palette));
                    break;
                case "Tritanopia":
                    palette = new int[]{Color.MAGENTA, Color.BLACK, Color.YELLOW, Color.RED, Color.GREEN};
                    Log.d("GAME_PALETTE", "Tritanopia palette: " + Arrays.toString(palette));
                    break;
                default:
                    palette = new int[]{Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN};
                    Log.d("GAME_PALETTE", "Nominal palette: " + Arrays.toString(palette));
            }
            return palette; // Return the palette after the switch block
        }
    }

    public GameView(Context context, GameEventListener listener) {
        super(context);
        this.gameEventListener = listener;
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Initialize shapes only if they haven't been initialized yet
        if (shapes.isEmpty()) {
            initShapes();
        }
    }

    public void nextLevel() {
        currentLevel++; // Increment the level
        setLevel(currentLevel); // Update the GameView level
        if (gameEventListener != null) {
            gameEventListener.onNextLevel(currentLevel); // Notify MainActivity
        }
    }

    private void initShapes() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("GamePrefs", Context.MODE_PRIVATE);
        String visionType = sharedPreferences.getString("ColorVisionType", "Nominal"); // Default to "Nominal"
        Log.d("GAME_PREFS", "Retrieved vision type from preferences: " + visionType);

        shapes.clear();
        List<ShapeType> shuffledTypes = new ArrayList<>(Arrays.asList(ShapeType.values()));
        Collections.shuffle(shuffledTypes);

        int[] palette = ColorName.getPaletteForVisionType(visionType);

        for (int i = 0; i < 5; i++) {
            int x = random.nextInt(getWidth() - 150) + 50;
            int y = random.nextInt(getHeight() - 150) + 50;
            int size = random.nextInt(50) + 50;
            int height = size;

            if (shuffledTypes.get(i % shuffledTypes.size()) == ShapeType.OVAL) {
                height *= 1.5; // Ovals have 1.5 times the height
            }

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            int color = palette[i % palette.length];
            paint.setColor(color);

            String colorName = Arrays.stream(ColorName.values())
                    .filter(cn -> cn.getColorValue() == color)
                    .findFirst()
                    .map(ColorName::getColorName)
                    .orElse("Unknown");

            shapes.add(new Shape(x, y, size, height, paint, shuffledTypes.get(i % shuffledTypes.size()), colorName));
        }

        setLevel(1);
        moveShapes();
    }

    public void setLevel(int level) {
        this.currentLevel = level;

        if (level == 1) {
            for (Shape shape : shapes) {
                // Reset velocities to initial state
                shape.vx = random.nextInt(20) - 10; // Example initial velocity
                shape.vy = random.nextInt(20) - 10; // Example initial velocity
            }
            // ... reset any other necessary state
        }

        if (level > 3) {
            // Player has passed all levels, trigger success event
            if (gameTimer != null) {
                gameTimer.cancel(); // Cancel the timer to stop countdown
                gameTimer = null; // Clear the timer reference
            }
            // Save the success state here
            SharedPreferences sharedPreferences = getContext().getSharedPreferences("GamePrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("GameSuccess", true); // 'true' indicates the game was successfully completed
            editor.apply();
            Log.d("GAME_LEVEL", "Setting level to: " + level);

            if (gameEventListener != null) {
                gameEventListener.onGameSuccess();
            }
            return; // Exit the method to prevent further game progression
        }

        for (Shape shape : shapes) {
            // Increase the velocity of each shape with each level
            shape.vx += velocityIncrement;
            shape.vy += velocityIncrement;
        }


        // Existing level setup logic
        switch (level) {
            case 1:
                timeLimit = 10000;
                break;
            case 2:
                timeLimit = 7000;
                break;
            case 3:
                timeLimit = 4000;
                break;


            // No default case needed as we've handled levels > 3 above
        }

        startNewRound();
    }

    private void startNewRound() {
        if (!shapes.isEmpty()) {
            targetShape = shapes.get(random.nextInt(shapes.size()));
            if (gameEventListener != null) {
                // Directly use the color name from the target shape for the prompt
                gameEventListener.onNewRound(targetShape.shapeType.name(), targetShape.colorName);
            }

            if (gameTimer != null) {
                gameTimer.cancel();
            }
            gameTimer = new CountDownTimer(timeLimit, 1000) {
                public void onTick(long millisUntilFinished) {
                    if (gameEventListener != null) {
                        gameEventListener.onTimerTick(millisUntilFinished / 1000);
                    }
                }

                public void onFinish() {
                    // Check if the game has already been successfully completed
                    if (currentLevel > 3) {
                        return; // Do nothing if the game was already won
                    }

                    if (gameEventListener != null) {
                        gameEventListener.onGameOver();
                    }
                }
            }.start();
        }
    }


    private void moveShapes() {
        Runnable updatePosition = new Runnable() {
            @Override
            public void run() {
                for (Shape shape : shapes) {
                    // Update shape position based on its velocity
                    shape.x += shape.vx;
                    shape.y += shape.vy;

                    // Collision detection with the view's boundaries
                    // For the X axis, consider the shape's width
                    if (shape.x < 0 || shape.x + shape.width > getWidth()) {
                        shape.vx *= -1; // Reverse the X velocity
                        shape.x = Math.max(0, Math.min(getWidth() - shape.width, shape.x)); // Adjust position within bounds
                    }
                    // For the Y axis, consider the shape's height
                    if (shape.y < 0 || shape.y + shape.height > getHeight()) {
                        shape.vy *= -1; // Reverse the Y velocity
                        shape.y = Math.max(0, Math.min(getHeight() - shape.height, shape.y)); // Adjust position within bounds
                    }
                }

                invalidate(); // Redraw with updated positions
                handler.postDelayed(this, UPDATE_MILLIS); // Schedule the next update
            }
        };
        handler.post(updatePosition);
    }


    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        for (Shape shape : shapes) {
            switch (shape.shapeType) {
                case CIRCLE:
                    // Use width for the radius. Since it's a circle, width = height.
                    canvas.drawCircle(shape.x + shape.width / 2f, shape.y + shape.height / 2f, shape.width / 2f, shape.paint);
                    break;
                case RECTANGLE:
                case SQUARE:
                    // Use width and height to draw rectangles and squares
                    canvas.drawRect(shape.x, shape.y, shape.x + shape.width, shape.y + shape.height, shape.paint);
                    break;
                case OVAL:
                    // Update the oval's bounding RectF before drawing
                    shape.updateOvalRect();
                    canvas.drawOval(shape.ovalRect, shape.paint);
                    break;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float touchX = event.getX();
            float touchY = event.getY();
            Log.d("GAME_TOUCH", "Touch at: [" + touchX + ", " + touchY + "]");

            // Add touch event handling for different shapes
            if (targetShape.contains(touchX, touchY)) {
                nextLevel(); // Start a new round if the target shape is tapped
                performClick();
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    class Shape {
        float x, y;
        int width, height; // Use separate width and height to differentiate ovals from circles
        Paint paint;
        ShapeType shapeType;
        String colorName;
        float vx, vy;
        RectF ovalRect = new RectF(); // Initialize RectF for ovals

        Shape(float x, float y, int width, int height, Paint paint, ShapeType shapeType, String colorName) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.paint = paint;
            this.shapeType = shapeType;
            this.colorName = colorName;
            this.vx = random.nextInt(20) - 5;
            this.vy = random.nextInt(20) - 5;
            if (vx == 0) vx = 1;
            if (vy == 0) vy = 1;
        }

        void updateOvalRect() {
            if (shapeType == ShapeType.OVAL) {
                ovalRect.set(x, y, x + width, y + height);
            }
        }

        boolean contains(float touchX, float touchY) {
            float dx, dy; // Declare variables outside the switch to use across all cases
            switch (shapeType) {
                case CIRCLE:
                    // For circles, you can still use size since width and height are the same
                    dx = (x + width / 2f) - touchX;
                    dy = (y + height / 2f) - touchY;
                    return dx * dx + dy * dy <= (width / 2f) * (width / 2f); // Use width for radius

                case RECTANGLE:
                case SQUARE:
                    // For rectangles and squares, directly compare touch points with shape boundaries
                    return touchX >= x && touchX <= x + width && touchY >= y && touchY <= y + height;

                case OVAL:
                    // For ovals, consider the distinct width and height for radius calculations
                    dx = (x + width / 2f) - touchX;
                    dy = (y + height / 2f) - touchY;
                    float rx = width / 2f; // Horizontal radius
                    float ry = height / 2f; // Vertical radius
                    return (dx * dx) / (rx * rx) + (dy * dy) / (ry * ry) <= 1;

                default:
                    return false;
            }
        }


    }

    public interface GameEventListener {
        void onNewRound(String shapeType, String colorName);

        void onTimerTick(long secondsLeft);

        void onGameOver();

        void onNextLevel(int newLevel);

        void onGameSuccess();
    }
}