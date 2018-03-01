package utah.csseniorproject.com.petentertainer;

public class ChassisDevice extends Device {
    private final static String chassisLeftMotorIdentifier = "chassis_left_motor";
    private final static String chassisRightMotorIdentifier = "chassis_right_motor";
    private final static String armYawMotorIdentifier = "arm_yaw_motor";


    public ChassisDevice() {
        super();
    }

    public void moveArmLeft(int percent) {
        sendCommand(armYawMotorIdentifier, percent);
    }

    public void moveArmRight(int percent) {
        sendCommand(armYawMotorIdentifier, -percent);
    }

    public void stopArmHorizontal() {
        sendCommand(armYawMotorIdentifier, 0);
    }

    public void moveWheels(int percentLeft, int percentRight) {
        sendCommand(chassisLeftMotorIdentifier, percentLeft);
        sendCommand(chassisRightMotorIdentifier, percentRight);
    }

    public void stopWheels() {
        sendCommand(chassisLeftMotorIdentifier, 0);
        sendCommand(chassisRightMotorIdentifier, 0);
    }
}
