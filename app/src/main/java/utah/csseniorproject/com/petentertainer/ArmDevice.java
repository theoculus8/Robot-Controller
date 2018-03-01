package utah.csseniorproject.com.petentertainer;


public class ArmDevice extends Device {
    private final static String armPitchMotorIdentifier = "arm_pitch_motor";
    private final static String armReachMotorIdentifier = "arm_reach_motor";
    private final static String armLaserIdentifier = "laser";


    public ArmDevice() {
        super();
    }

    public void moveArmUp(int percent) {
        sendCommand(armPitchMotorIdentifier, percent);
    }

    public void moveArmDown(int percent) {
        sendCommand(armPitchMotorIdentifier, -percent);
    }

    public void stopArmVertical() {
        sendCommand(armPitchMotorIdentifier, 0);
    }

    public void extendArm(int percent) {
        sendCommand(armReachMotorIdentifier, percent);
    }

    public void retractArm(int percent) {
        sendCommand(armReachMotorIdentifier, -percent);
    }

    public void stopExtendArm() {
        sendCommand(armReachMotorIdentifier, 0);
    }

    public void turnLaserOn() {
        sendCommand(armLaserIdentifier, 100);
    }

    public void turnLaserOff() {
        sendCommand(armLaserIdentifier, 0);
    }
}
