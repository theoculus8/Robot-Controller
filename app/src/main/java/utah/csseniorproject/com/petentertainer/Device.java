package utah.csseniorproject.com.petentertainer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class Device {
    private final static int COMMANDS_PORT = 1234;
    private final static int ENCODER_STEPS = 17;

    private TCPConnection tcpConnection;

    public Device() {
        tcpConnection = new TCPConnection();
    }


    public void connectToDevice(final String ipAddress) throws Exception {
        final boolean isConnected[] = new boolean[1]; // Weird way to get return value
        Thread thread = new Thread(new Runnable() {
            public void run() {
                isConnected[0] = tcpConnection.remoteConnect(ipAddress, COMMANDS_PORT);
            }
        });
        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!isConnected[0]) {
            throw new Exception("Failed to connect to " + ipAddress);
        }
    }

    public void killThreads() {
        tcpConnection.killThreads();
    }

    protected void sendCommand(String deviceString, int percent) {
        JSONArray json = new JSONArray();
        JSONObject device = new JSONObject();
        try {
            device.put("device", deviceString);

            percent = roundToMultipleOfEncoderSteps(percent);

            if (percent > 100) {
                percent = 100;
            } else if (percent < -100) {
                percent = -100;
            }

            device.put("percent", percent);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        json.put(device);

        final JSONArray threadJson = json;
        new Thread(new Runnable() {
            public void run() {
                tcpConnection.writeJsonToSocket(threadJson);
            }
        }).start();
    }

    private int roundToMultipleOfEncoderSteps(int value) {
        int count = value / ENCODER_STEPS;
        if (value >= 0) {
            count += (double) value / ENCODER_STEPS - count >= 0.5 ? 1 : 0;
        } else {
            count += (double) value / ENCODER_STEPS - count <= -0.5 ? -1 : 0;
        }
        return count * ENCODER_STEPS;
    }
}
