package utah.csseniorproject.com.petentertainer;


public class DeviceManager {
    private ArmDevice arm;
    private ChassisDevice chassis;

    public DeviceManager(String armAddress, String chassisAddress) throws Exception {
        arm = new ArmDevice();
        chassis = new ChassisDevice();

        chassis.connectToDevice(chassisAddress);

        if (armAddress.equals(chassisAddress)) { // For testing
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        arm.connectToDevice(armAddress);
    }

    public void killThreads() {
        arm.killThreads();
        chassis.killThreads();
    }

    public void moveArmNorthWest(int percentLeft, int percentUp) {
        arm.moveArmUp(percentUp);

        // Reduce power by 60%
        //percentLeft = percentLeft / 100 * 70;
        chassis.moveArmLeft(percentLeft);
    }

    public void moveArmNorthEast(int percentRight, int percentUp) {
        arm.moveArmUp(percentUp);

        // Reduce power by 60%
        //percentRight = percentRight / 100 * 70;
        chassis.moveArmRight(percentRight);
    }

    public void moveArmSouthWest(int percentLeft, int percentDown) {
        arm.moveArmDown(percentDown);

        // Reduce power by 60%
        //percentLeft = percentLeft / 100 * 70;
        chassis.moveArmLeft(percentLeft);
    }

    public void moveArmSouthEast(int percentRight, int percentDown) {
        arm.moveArmDown(percentDown);

        // Reduce power by 60%
        //percentRight = percentRight / 100 * 70;
        chassis.moveArmRight(percentRight);
    }

    public void stopArm() {
        arm.stopArmVertical();
        chassis.stopArmHorizontal();
    }

    public void moveChassis(double percentPower, double percentHorizontal, double percentVertical) {
        double angle = getAngle(percentHorizontal, percentVertical);

        // Reduce power by 50%
        percentPower = percentPower / 100 * 50;

        double leftMotorPercent = 0;
        double rightMotorPercent = 0;

        if (angle >= 0 && angle < 45 || angle >= 315) {
            leftMotorPercent = -percentPower;
            rightMotorPercent = -percentPower;
        } else if (angle >= 45 && angle < 135) {
            leftMotorPercent = -percentPower;
            rightMotorPercent = percentPower;
        } else if (angle >= 135 && angle < 225) {
            leftMotorPercent = percentPower;
            rightMotorPercent = percentPower;
        } else if (angle >= 225 && angle < 315) {
            leftMotorPercent = percentPower;
            rightMotorPercent = -percentPower;
        }

        /*if (angle >= 0 && angle < 45) {
            leftMotorPercent = percentPower;
            rightMotorPercent = (percentPower / 100) * ((angle / 45 * 100) - 100);
        } else if (angle >= 45 && angle < 90) {
            leftMotorPercent = percentPower;
            rightMotorPercent = (percentPower / 100) * (((angle - 45) / 45) * 100);
        } else if (angle >= 90 && angle < 135) {
            leftMotorPercent = (percentPower / 100) * (100 - (((angle - 90) / 45) * 100));
            rightMotorPercent = percentPower;
        } else if (angle >= 135 && angle < 180) {
            leftMotorPercent = (percentPower / 100) * (-1 * (((angle - 135) / 45) * 100));
            rightMotorPercent = percentPower;
        } else if (angle >= 180 && angle < 225) {
            leftMotorPercent = (percentPower / 100) * (((angle - 180) / 45 * 100) - 100);
            rightMotorPercent = (percentPower / 100) * (-1 * (((angle - 180) / 45 * 200) - 100));
        } else if (angle >= 225 && angle < 270) {
            leftMotorPercent = (percentPower / 100) * (-1 * (((angle - 225) / 45) * 100));
            rightMotorPercent = -percentPower;
        } else if (angle >= 270 && angle < 315) {
            leftMotorPercent = -percentPower;
            rightMotorPercent = (percentPower / 100) * (((angle - 270) / 45 * 100) - 100);
        } else if (angle >= 315 && angle < 360) {
            leftMotorPercent = (percentPower / 100) * (((angle - 315) / 45 * 200) - 100);
            rightMotorPercent = (percentPower / 100) * (-1 * (((angle - 315) / 45) * 100));
        }*/

        chassis.moveWheels((int) leftMotorPercent, (int) rightMotorPercent);
    }

    public void stopChassis() {
        chassis.stopWheels();
    }

    public void extendArm(int percent) {
        if (percent >= 0) {
            arm.extendArm(percent);
        } else {
            arm.retractArm(-percent);
        }
    }

    public void stopExtend() {
        arm.stopExtendArm();
    }

    public void graspArm(int percent) {
        if (percent >= 0) {
            arm.graspArm(percent);
        } else {
            arm.releaseArm(-percent);
        }
    }

    public void stopGrasp() {
        arm.stopGraspArm();
    }

    public void turnOnLaser() {
        arm.turnLaserOn();
    }

    public void turnOffLaser() {
        arm.turnLaserOff();
    }

    private double getAngle(double x, double y) {
        double angle = Math.toDegrees(Math.atan2(y, x));
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }
}
