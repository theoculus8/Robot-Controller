package utah.csseniorproject.com.petentertainer;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.HashMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class TCPConnection {
    private static final String TAG = TCPConnection.class.getSimpleName();

    private SSLContext context;
    private SSLSocket socket;
    private DataOutputStream outputStream;

    private HashMap<String, String> commandCache;

    public TCPConnection() {
        InputStream certificateStream = null;
        try {
            certificateStream = MainActivity.assetManager.open("server.crt");
        } catch (IOException e) {
            Log.e(TAG, "exception", e);
        }
        CertificateFactory certificateFactory = null;
        Certificate certificate = null;
        try {
            certificateFactory = CertificateFactory.getInstance("X.509");
            certificate = certificateFactory.generateCertificate(certificateStream);
        } catch (CertificateException e) {
            Log.e(TAG, "exception", e);
        }

        try {
            certificateStream.close();
        } catch (IOException e) {
            Log.e(TAG, "exception", e);
        }

        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keystore = null;
        try {
            keystore = KeyStore.getInstance(keyStoreType);
        } catch (KeyStoreException e) {
            Log.e(TAG, "exception", e);
        }
        try {
            keystore.load(null, null);
        } catch (IOException e) {
            Log.e(TAG, "exception", e);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "exception", e);
        } catch (CertificateException e) {
            Log.e(TAG, "exception", e);
        }
        try {
            keystore.setCertificateEntry("ca", certificate);
        } catch (KeyStoreException e) {
            Log.e(TAG, "exception", e);
        }

        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = null;
        try {
            tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "exception", e);
        }
        try {
            tmf.init(keystore);
        } catch (KeyStoreException e) {
            Log.e(TAG, "exception", e);
        }

        try {
            context = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "exception", e);
        }
        try {
            context.init(null, tmf.getTrustManagers(), null);
        } catch (KeyManagementException e) {
            Log.e(TAG, "exception", e);
        }

        commandCache = new HashMap<>();
    }

    public void remoteConnect(String ipAddress, int port) {
        SSLSocketFactory factory = context.getSocketFactory();
        try {
            socket = (SSLSocket) factory.createSocket(ipAddress, port);
        } catch (IOException e) {
            Log.e(TAG, "Failed to connect to address: " + ipAddress + ", port: " + port, e);
        }
        try {
            socket.startHandshake();
        } catch (IOException e) {
            Log.e(TAG, "Handshake failed", e);
        }
        try {
            outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        } catch (IOException e) {
            Log.e(TAG, "exception", e);
        }
    }

    public synchronized void writeJsonToSocket(JSONArray json) {
        String jsonString = json.toString();

        try {
            JSONObject object = json.getJSONObject(0);
            String device = object.getString("device");
            if (commandCache.containsKey(device)) {
                if (commandCache.get(device).equals(jsonString)) {
                    return;
                }
            }
            commandCache.put(device, jsonString);
        } catch (JSONException e) {
            Log.e(TAG, "exception", e);
        }

        try {
            outputStream.writeBytes(jsonString);
            outputStream.flush();
        } catch (IOException e) {
            Log.e(TAG, "exception", e);
        }
    }

    public boolean isConnected() {
        return socket.isConnected();
    }
}
