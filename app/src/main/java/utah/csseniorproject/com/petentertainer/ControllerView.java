package utah.csseniorproject.com.petentertainer;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;

public class ControllerView extends View {
    private final int DOT_RADIUS = 100;
    private final int RING_RADIUS = 400;
    private final int RING_THICKNESS = 10;
    private final int AESTHETIC_RING_RADIUS = RING_RADIUS + RING_THICKNESS + 5;
    private final int AESTHETIC_RING_THICKNESS = 3;
    private final int CHEVRON_RADIUS = 80;
    private final int CHEVRON_THICKNESS = 15;
    private final int DETECTION_DISTANCE = DOT_RADIUS;

    private final int PAINT_TRANSPARENCY = 200;

    private final int DOUBLE_TAP_DETECTION_TIME_MSEC = 300;

    private Paint dotPaint;
    private Paint ringPaint;
    private Paint aestheticRingPaint;
    private Paint chevronPaint;

    private Path chevronPath;

    private final float centerX = Resources.getSystem().getDisplayMetrics().widthPixels - (Resources.getSystem().getDisplayMetrics().heightPixels - RING_RADIUS) / 2;
    private final float centerY = Resources.getSystem().getDisplayMetrics().heightPixels / 2;
    private float currentX;
    private float currentY;

    private boolean drawingAllowed = true;
    private boolean sendCommands = false;

    private DeviceManager deviceManager;

    public enum Device {
        arm,
        chassis
    }

    private Device activeDevice = Device.chassis;

    private long lastTapTime_msec = 0;


    public ControllerView(Context context, String armAddress, String chassisAddress) {
        super(context);

        dotPaint = new Paint();
        dotPaint.setAntiAlias(true);
        dotPaint.setColor(Color.WHITE);
        dotPaint.setAlpha(PAINT_TRANSPARENCY);

        ringPaint = new Paint();
        ringPaint.setAntiAlias(true);
        ringPaint.setColor(Color.WHITE);
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(RING_THICKNESS);
        ringPaint.setAlpha(PAINT_TRANSPARENCY);

        aestheticRingPaint = new Paint();
        aestheticRingPaint.setAntiAlias(true);
        aestheticRingPaint.setColor(Color.WHITE);
        aestheticRingPaint.setStyle(Paint.Style.STROKE);
        aestheticRingPaint.setStrokeWidth(AESTHETIC_RING_THICKNESS);
        aestheticRingPaint.setAlpha(PAINT_TRANSPARENCY);

        chevronPaint = new Paint();
        chevronPaint.setAntiAlias(true);
        chevronPaint.setColor(Color.WHITE);
        chevronPaint.setStyle(Paint.Style.STROKE);
        chevronPaint.setStrokeWidth(CHEVRON_THICKNESS);
        chevronPaint.setStrokeJoin(Paint.Join.ROUND);
        chevronPaint.setAlpha(PAINT_TRANSPARENCY);
        chevronPath = new Path();

        currentX = centerX;
        currentY = centerY;

        deviceManager = new DeviceManager(armAddress, chassisAddress);
    }

    public interface ControllerSwitchListener {
        void onEventOccurred(Device currentDevice);
    }

    private ControllerSwitchListener eventListener;

    public void setEventListener(ControllerSwitchListener eventListener) {
        this.eventListener = eventListener;
    }

    private void setArmActive() {
        activeDevice = Device.arm;

        dotPaint.setColor(Color.RED);
        ringPaint.setColor(Color.RED);
        aestheticRingPaint.setColor(Color.RED);
        chevronPaint.setColor(Color.RED);

        dotPaint.setAlpha(PAINT_TRANSPARENCY);
        ringPaint.setAlpha(PAINT_TRANSPARENCY);
        aestheticRingPaint.setAlpha(PAINT_TRANSPARENCY);
        chevronPaint.setAlpha(PAINT_TRANSPARENCY);
    }

    private void setChassisActive() {
        activeDevice = Device.chassis;

        dotPaint.setColor(Color.WHITE);
        ringPaint.setColor(Color.WHITE);
        aestheticRingPaint.setColor(Color.WHITE);
        chevronPaint.setColor(Color.WHITE);

        dotPaint.setAlpha(PAINT_TRANSPARENCY);
        ringPaint.setAlpha(PAINT_TRANSPARENCY);
        aestheticRingPaint.setAlpha(PAINT_TRANSPARENCY);
        chevronPaint.setAlpha(PAINT_TRANSPARENCY);
    }

    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();

        double distanceFromCenter = Math.sqrt(Math.pow(centerX - eventX, 2) + Math.pow(centerY - eventY, 2));

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (distanceFromCenter > RING_RADIUS || !drawingAllowed) {
                    drawingAllowed = !drawingAllowed;
                } else if (drawingAllowed) {
                    if (distanceFromCenter <= DETECTION_DISTANCE) {
                        sendCommands = true;

                        currentX = eventX;
                        currentY = eventY;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE: {
                if (!sendCommands) {
                    break;
                }

                if (distanceFromCenter <= RING_RADIUS) {
                    currentX = eventX;
                    currentY = eventY;
                } else {
                    float theta = (float) Math.abs(Math.atan((eventY - centerY) / (eventX - centerX)));

                    if (eventX < centerX) {
                        currentX = centerX - (float) Math.cos(theta) * RING_RADIUS;
                    } else {
                        currentX = centerX + (float) Math.cos(theta) * RING_RADIUS;
                    }

                    if (eventY < centerY) {
                        currentY = centerY - (float) Math.sin(theta) * RING_RADIUS;
                    } else {
                        currentY = centerY + (float) Math.sin(theta) * RING_RADIUS;
                    }
                }

                float distanceUp = centerY - currentY;
                float distanceDown = currentY - centerY;
                float distanceLeft = centerX - currentX;
                float distanceRight = currentX - centerX;

                switch (activeDevice) {
                    case arm:
                        if (distanceUp > distanceDown && distanceLeft > distanceRight) {
                            deviceManager.moveArmNorthWest((int) (distanceLeft / RING_RADIUS * 100), (int) (distanceUp / RING_RADIUS * 100));
                        } else if (distanceUp > distanceDown && distanceLeft <= distanceRight) {
                            deviceManager.moveArmNorthEast((int) (distanceRight / RING_RADIUS * 100), (int) (distanceUp / RING_RADIUS * 100));
                        } else if (distanceUp <= distanceDown && distanceLeft > distanceRight) {
                            deviceManager.moveArmSouthWest((int) (distanceLeft / RING_RADIUS * 100), (int) (distanceDown / RING_RADIUS * 100));
                        } else if (distanceUp <= distanceDown && distanceLeft <= distanceRight) {
                            deviceManager.moveArmSouthEast((int) (distanceRight / RING_RADIUS * 100), (int) (distanceDown / RING_RADIUS * 100));
                        }
                        break;

                    case chassis:
                        deviceManager.moveChassis(distanceFromCenter / RING_RADIUS * 100,
                                distanceRight / RING_RADIUS * 100,
                                distanceUp / RING_RADIUS * 100);
                        break;
                }
            }
            break;
            case MotionEvent.ACTION_UP:
                if (sendCommands) {
                    sendCommands = false;

                    currentX = centerX;
                    currentY = centerY;

                    switch (activeDevice) {
                        case arm:
                            deviceManager.stopArm();
                            break;
                        case chassis:
                            deviceManager.stopChassis();
                            break;
                    }
                }

                long currentTapTime_msec = System.currentTimeMillis();
                if (currentTapTime_msec - lastTapTime_msec <= DOUBLE_TAP_DETECTION_TIME_MSEC) {
                    switch (activeDevice) {
                        case arm:
                            setChassisActive();
                            eventListener.onEventOccurred(activeDevice);
                            break;
                        case chassis:
                            setArmActive();
                            eventListener.onEventOccurred(activeDevice);
                            break;
                    }
                }
                lastTapTime_msec = currentTapTime_msec;
                break;
        }

        invalidate();

        return true;
    }

    private void drawChevron(Canvas canvas, int angle, float startX, float startY) {
        canvas.rotate(-angle, startX, startY);

        chevronPath.reset();

        float currentX = startX;
        float currentY = startY + CHEVRON_RADIUS;

        chevronPath.moveTo(currentX, currentY);
        chevronPath.lineTo(startX, startY);

        currentY -= CHEVRON_RADIUS;
        currentX += CHEVRON_RADIUS;

        chevronPath.lineTo(currentX, currentY);

        canvas.drawPath(chevronPath, chevronPaint);

        canvas.rotate(angle, startX, startY);
    }

    protected void onDraw(Canvas canvas) {
        if (drawingAllowed) {
            drawChevron(canvas, 45, centerX - (RING_RADIUS - DOT_RADIUS), centerY);
            drawChevron(canvas, 225, centerX + (RING_RADIUS - DOT_RADIUS), centerY);
            drawChevron(canvas, 135, centerX, centerY + (RING_RADIUS - DOT_RADIUS));
            drawChevron(canvas, 315, centerX, centerY - (RING_RADIUS - DOT_RADIUS));

            canvas.drawCircle(currentX, currentY, DOT_RADIUS, dotPaint);
            canvas.drawCircle(centerX, centerY, RING_RADIUS, ringPaint);
            canvas.drawCircle(centerX, centerY, AESTHETIC_RING_RADIUS, aestheticRingPaint);
        }
    }
}
