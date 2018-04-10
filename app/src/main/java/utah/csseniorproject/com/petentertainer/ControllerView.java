package utah.csseniorproject.com.petentertainer;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;

import static android.view.MotionEvent.INVALID_POINTER_ID;

public class ControllerView extends View {
    private final int DOT_RADIUS = 100;
    private final int RING_RADIUS = 400;
    private final int RING_THICKNESS = 10;
    private final int AESTHETIC_RING_RADIUS = RING_RADIUS + RING_THICKNESS + 5;
    private final int AESTHETIC_RING_THICKNESS = 3;
    private final int CHEVRON_RADIUS = 80;
    private final int CHEVRON_THICKNESS = 15;
    private final int LEVER_LINE_THICKNESS = 15;
    private final int LEVEL_LINE_LENGTH = 900;
    private final int LEVEL_LINE_SMALL_LENGTH = 100;
    private final int LASER_DOT_RADIUS = 150;
    private final int LASER_DOT_WIDTH = 30;
    private final int DETECTION_DISTANCE = 100;

    private final int PAINT_TRANSPARENCY = 200;

    private final int DOUBLE_TAP_DETECTION_TIME_MSEC = 300;

    private Paint dotPaint;
    private Paint ringPaint;
    private Paint aestheticRingPaint;
    private Paint chevronPaint;
    private Paint linePaint;
    private Paint laserInnerPaint;
    private Paint laserOuterPaint;

    private Path chevronPath;

    private final float screenCenterX = Resources.getSystem().getDisplayMetrics().widthPixels / 2;
    private final float screenCenterY = Resources.getSystem().getDisplayMetrics().heightPixels / 2;

    private boolean drawingAllowed = true;

    private DeviceManager deviceManager;

    public enum Device {
        arm,
        chassis
    }

    private Device activeDevice;

    private Control ringControl;
    private Control extendControl;
    private Control graspControl;
    private Control laserControl;

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

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.WHITE);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(LEVER_LINE_THICKNESS);
        linePaint.setAlpha(PAINT_TRANSPARENCY);

        laserInnerPaint = new Paint();
        laserInnerPaint.setAntiAlias(true);
        laserInnerPaint.setColor(Color.RED);
        laserInnerPaint.setAlpha(PAINT_TRANSPARENCY);

        laserOuterPaint = new Paint();
        laserOuterPaint.setAntiAlias(true);
        laserOuterPaint.setColor(Color.BLACK);
        laserOuterPaint.setStyle(Paint.Style.STROKE);
        laserOuterPaint.setStrokeWidth(LASER_DOT_WIDTH);
        laserOuterPaint.setAlpha(PAINT_TRANSPARENCY);

        ringControl = new Control(screenCenterX * 2 - (screenCenterY * 2 - RING_RADIUS) / 2, screenCenterY);
        extendControl = new Control(DOT_RADIUS * 2.5f, screenCenterY);
        graspControl = new Control(extendControl.centerX + DOT_RADIUS * 2.5f, screenCenterY);
        laserControl = new Control(graspControl.centerX + LASER_DOT_RADIUS * 2, screenCenterY);

        deviceManager = new DeviceManager(armAddress, chassisAddress);

        activeDevice = Device.chassis;
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
        chevronPaint.setColor(Color.RED);

        dotPaint.setAlpha(PAINT_TRANSPARENCY);
        chevronPaint.setAlpha(PAINT_TRANSPARENCY);
    }

    private void setChassisActive() {
        activeDevice = Device.chassis;

        dotPaint.setColor(Color.WHITE);
        chevronPaint.setColor(Color.WHITE);

        dotPaint.setAlpha(PAINT_TRANSPARENCY);
        chevronPaint.setAlpha(PAINT_TRANSPARENCY);
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (!drawingAllowed) {
                    drawingAllowed = true;
                    break;
                }

                final int pointerIndex = MotionEventCompat.getActionIndex(event);

                float eventX = event.getX();
                float eventY = event.getY();

                double distanceFromRingCenter = ringControl.distanceFromCenter(eventX, eventY);
                double distanceFromExtendLeverCenter = extendControl.distanceFromCenter(eventX, eventY);
                double distanceFromGraspLeverCenter = graspControl.distanceFromCenter(eventX, eventY);
                double distanceFromLaserCenter = laserControl.distanceFromCenter(eventX, eventY);

                if (distanceFromRingCenter > RING_RADIUS &&
                        distanceFromLaserCenter > LASER_DOT_RADIUS &&
                        Math.abs(extendControl.centerX - eventX) > DETECTION_DISTANCE &&
                        Math.abs(extendControl.centerY - eventY) > LEVEL_LINE_LENGTH / 2 &&
                        Math.abs(graspControl.centerX - eventX) > DETECTION_DISTANCE &&
                        Math.abs(graspControl.centerY - eventY) > LEVEL_LINE_LENGTH / 2) {
                    drawingAllowed = !drawingAllowed;
                } else if (drawingAllowed) {
                    if (distanceFromRingCenter <= DETECTION_DISTANCE) {
                        ringControl.isActive = true;
                        ringControl.currentX = eventX;
                        ringControl.currentY = eventY;
                        ringControl.pointerID = MotionEventCompat.getPointerId(event, pointerIndex);
                    } else if (distanceFromExtendLeverCenter <= DETECTION_DISTANCE) {
                        extendControl.isActive = true;
                        extendControl.currentY = eventY;
                        extendControl.pointerID = MotionEventCompat.getPointerId(event, pointerIndex);
                    } else if (distanceFromGraspLeverCenter <= DETECTION_DISTANCE) {
                        graspControl.isActive = true;
                        graspControl.currentY = eventY;
                        graspControl.pointerID = MotionEventCompat.getPointerId(event, pointerIndex);
                    } else if (distanceFromLaserCenter <= LASER_DOT_RADIUS) {
                        laserControl.isActive = true;
                        laserControl.pointerID = MotionEventCompat.getPointerId(event, pointerIndex);

                        deviceManager.turnOnLaser();

                        laserOuterPaint.setColor(Color.WHITE);
                        laserOuterPaint.setAlpha(PAINT_TRANSPARENCY);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = MotionEventCompat.getActionIndex(event);
                final int pointerID = MotionEventCompat.getPointerId(event, pointerIndex);

                if (pointerID == ringControl.pointerID) {
                    if (!ringControl.isActive) {
                        break;
                    }

                    float eventX = MotionEventCompat.getX(event, pointerIndex);
                    float eventY = MotionEventCompat.getY(event, pointerIndex);

                    double distanceFromRingCenter = ringControl.distanceFromCenter(eventX, eventY);

                    if (distanceFromRingCenter <= RING_RADIUS) {
                        ringControl.currentX = eventX;
                        ringControl.currentY = eventY;
                    } else {
                        distanceFromRingCenter = RING_RADIUS;

                        float theta = (float) Math.abs(Math.atan((eventY - ringControl.centerY) / (eventX - ringControl.centerX)));

                        if (eventX < ringControl.centerX) {
                            ringControl.currentX = ringControl.centerX - (float) Math.cos(theta) * RING_RADIUS;
                        } else {
                            ringControl.currentX = ringControl.centerX + (float) Math.cos(theta) * RING_RADIUS;
                        }

                        if (eventY < ringControl.centerY) {
                            ringControl.currentY = ringControl.centerY - (float) Math.sin(theta) * RING_RADIUS;
                        } else {
                            ringControl.currentY = ringControl.centerY + (float) Math.sin(theta) * RING_RADIUS;
                        }
                    }

                    float distanceUp = ringControl.centerY - ringControl.currentY;
                    float distanceDown = ringControl.currentY - ringControl.centerY;
                    float distanceLeft = ringControl.centerX - ringControl.currentX;
                    float distanceRight = ringControl.currentX - ringControl.centerX;

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
                            deviceManager.moveChassis(distanceFromRingCenter / RING_RADIUS * 100,
                                    distanceRight / RING_RADIUS * 100,
                                    distanceUp / RING_RADIUS * 100);
                            break;
                    }
                } else if (pointerID == extendControl.pointerID) {
                    if (!extendControl.isActive) {
                        break;
                    }

                    float eventY = MotionEventCompat.getY(event, pointerIndex);
                    extendControl.currentY = eventY;

                    if (Math.abs(extendControl.centerY - eventY) > LEVEL_LINE_LENGTH / 2) {
                        if (extendControl.centerY > eventY) {
                            extendControl.currentY = extendControl.centerY - LEVEL_LINE_LENGTH / 2;
                        } else {
                            extendControl.currentY = extendControl.centerY + LEVEL_LINE_LENGTH / 2;
                        }
                    }

                    deviceManager.extendArm((int) ((extendControl.centerY - extendControl.currentY) / (LEVEL_LINE_LENGTH / 2) * -100));
                } else if (pointerID == graspControl.pointerID) {
                    if (!graspControl.isActive) {
                        break;
                    }

                    float eventY = MotionEventCompat.getY(event, pointerIndex);
                    graspControl.currentY = eventY;

                    if (Math.abs(graspControl.centerY - eventY) > LEVEL_LINE_LENGTH / 2) {
                        if (graspControl.centerY > eventY) {
                            graspControl.currentY = graspControl.centerY - LEVEL_LINE_LENGTH / 2;
                        } else {
                            graspControl.currentY = graspControl.centerY + LEVEL_LINE_LENGTH / 2;
                        }
                    }

                    deviceManager.graspArm((int) ((graspControl.centerY - graspControl.currentY) / (LEVEL_LINE_LENGTH / 2) * 100));
                } else if (pointerID == laserControl.pointerID) {
                    if (!laserControl.isActive) {
                        break;
                    }

                    float eventX = MotionEventCompat.getX(event, pointerIndex);
                    float eventY = MotionEventCompat.getY(event, pointerIndex);

                    if (laserControl.distanceFromCenter(eventX, eventY) > LASER_DOT_RADIUS) {
                        laserControl.reset();
                        deviceManager.turnOffLaser();

                        laserOuterPaint.setColor(Color.BLACK);
                        laserOuterPaint.setAlpha(PAINT_TRANSPARENCY);
                    }
                }
            }
            break;
            case MotionEvent.ACTION_UP: {
                final int pointerIndex = MotionEventCompat.getActionIndex(event);
                final int pointerID = MotionEventCompat.getPointerId(event, pointerIndex);

                if (pointerID == ringControl.pointerID) {
                    if (!ringControl.isActive) {
                        break;
                    }

                    switch (activeDevice) {
                        case arm:
                            deviceManager.stopArm();
                            break;
                        case chassis:
                            deviceManager.stopChassis();
                            break;
                    }
                    
                    ringControl.reset();
                } else if (pointerID == extendControl.pointerID) {
                    if (!extendControl.isActive) {
                        break;
                    }

                    extendControl.reset();

                    deviceManager.stopExtend();
                } else if (pointerID == graspControl.pointerID) {
                    if (!graspControl.isActive) {
                        break;
                    }

                    graspControl.reset();

                    deviceManager.stopGrasp();
                } else if (pointerID == laserControl.pointerID) {
                    if (!laserControl.isActive) {
                        break;
                    }

                    laserControl.reset();

                    deviceManager.turnOffLaser();

                    laserOuterPaint.setColor(Color.BLACK);
                    laserOuterPaint.setAlpha(PAINT_TRANSPARENCY);
                }

                long currentTapTime_msec = System.currentTimeMillis();
                if (currentTapTime_msec - lastTapTime_msec <= DOUBLE_TAP_DETECTION_TIME_MSEC) {
                    switch (activeDevice) {
                        case arm:
                            setChassisActive();
                            eventListener.onEventOccurred(Device.chassis);
                            break;
                        case chassis:
                            setArmActive();
                            eventListener.onEventOccurred(Device.arm);
                            break;
                    }
                }
                lastTapTime_msec = currentTapTime_msec;

                break;
            }
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

    private void drawLeverBackground(Canvas canvas, float centerX, float centerY, int length) {
        canvas.drawLine(centerX, centerY + length / 2, centerX, centerY - length / 2, linePaint);
        canvas.drawLine(centerX - LEVEL_LINE_SMALL_LENGTH / 2,
                centerY + length / 2,
                centerX + LEVEL_LINE_SMALL_LENGTH / 2,
                centerY + length / 2, linePaint);
        canvas.drawLine(centerX - LEVEL_LINE_SMALL_LENGTH / 2,
                centerY - length / 2,
                centerX + LEVEL_LINE_SMALL_LENGTH / 2,
                centerY - length / 2, linePaint);
    }

    private void drawLeverControl(Canvas canvas, float centerX, float centerY) {
        canvas.drawRoundRect(centerX - LEVEL_LINE_SMALL_LENGTH,
                centerY - LEVER_LINE_THICKNESS,
                centerX + LEVEL_LINE_SMALL_LENGTH,
                centerY + LEVER_LINE_THICKNESS,
                LEVER_LINE_THICKNESS,
                LEVER_LINE_THICKNESS,
                dotPaint);
    }

    protected void onDraw(Canvas canvas) {
        if (drawingAllowed) {
            drawChevron(canvas, 45, ringControl.centerX - (RING_RADIUS - DOT_RADIUS), ringControl.centerY);
            drawChevron(canvas, 225, ringControl.centerX + (RING_RADIUS - DOT_RADIUS), ringControl.centerY);
            drawChevron(canvas, 135, ringControl.centerX, ringControl.centerY + (RING_RADIUS - DOT_RADIUS));
            drawChevron(canvas, 315, ringControl.centerX, ringControl.centerY - (RING_RADIUS - DOT_RADIUS));

            canvas.drawCircle(ringControl.currentX, ringControl.currentY, DOT_RADIUS, dotPaint);
            canvas.drawCircle(ringControl.centerX, ringControl.centerY, RING_RADIUS, ringPaint);
            canvas.drawCircle(ringControl.centerX, ringControl.centerY, AESTHETIC_RING_RADIUS, aestheticRingPaint);

            if (activeDevice == Device.arm) {
                drawLeverBackground(canvas, extendControl.centerX, extendControl.centerY, LEVEL_LINE_LENGTH);
                drawLeverControl(canvas, extendControl.currentX, extendControl.currentY);

                drawLeverBackground(canvas, graspControl.centerX, graspControl.centerY, LEVEL_LINE_LENGTH);
                drawLeverControl(canvas, graspControl.currentX, graspControl.currentY);

                canvas.drawCircle(laserControl.centerX, laserControl.centerY, LASER_DOT_RADIUS, laserInnerPaint);
                canvas.drawCircle(laserControl.centerX, laserControl.centerY, LASER_DOT_RADIUS, laserOuterPaint);
            }
        }
    }
}
