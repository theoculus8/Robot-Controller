package utah.csseniorproject.com.petentertainer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button connectButton;
    private TextView armAddress;
    private TextView chassisAddress;

    public static AssetManager assetManager; // Needed in TCPConnection

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectButton = findViewById(R.id.connectButton);
        chassisAddress = findViewById(R.id.chassisAddressText);
        armAddress = findViewById(R.id.armAddressText);

        connectButton.setOnClickListener(this);

        assetManager = getResources().getAssets();

        SharedPreferences settings = getSharedPreferences("UserInfo", 0);
        chassisAddress.setText(settings.getString("chassisAddress", "Chassis IP Address"));
        armAddress.setText(settings.getString("armAddress", "Arm IP Address"));
    }

    public void onClick(View v)
    {
        String chassisIPAddress = chassisAddress.getText().toString();
        String armIPAddress = armAddress.getText().toString();
        if (chassisIPAddress.equals(""))
        {
            armAddress.setError("Enter chassis ip address.");
            return;
        } else if (armIPAddress.equals("")) {
            chassisAddress.setError("Enter arm ip address.");
            return;
        }

        SharedPreferences settings = getSharedPreferences("UserInfo", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("chassisAddress", chassisIPAddress);
        editor.putString("armAddress", armIPAddress);
        editor.commit();

        Intent intent = new Intent(getApplicationContext(), VideoActivity.class);
        intent.putExtra("chassisAddress", chassisIPAddress);
        intent.putExtra("armAddress", armIPAddress);
        startActivity(intent);
    }
}
