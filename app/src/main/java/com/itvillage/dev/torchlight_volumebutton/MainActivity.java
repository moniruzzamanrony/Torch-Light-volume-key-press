package com.itvillage.dev.torchlight_volumebutton;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 50;
    Switch onoff;
    private Dialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_dialog);
        onoff = (Switch) dialog.findViewById(R.id.switch1);

        boolean isEnabled = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
        if (!isEnabled) {
            Toast.makeText(MainActivity.this, "Camera Permission Needed", Toast.LENGTH_SHORT).show();
        } else {
            onoff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        TextView text = (TextView) dialog.findViewById(R.id.text_dialog);
                        text.setText("Disable Background Services with Torch?");
                        startService(new Intent(MainActivity.this, backgroudRunningService.class));
                        //ToDo something
                    } else {
                        TextView text = (TextView) dialog.findViewById(R.id.text_dialog);
                        text.setText("Enable Background Services with Torch?");
                        stopService(new Intent(MainActivity.this, backgroudRunningService.class));
                        //ToDo something
                    }
                }
            });
        }
        if (checkServiceRunning()) {
            TextView text = (TextView) dialog.findViewById(R.id.text_dialog);
            text.setText("Disable Background Services with Torch?");
            onoff.setChecked(true);

        } else {
            TextView text = (TextView) dialog.findViewById(R.id.text_dialog);
            text.setText("Enable Background Services with Torch?");
            onoff.setChecked(false);

        }
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startService(new Intent(MainActivity.this, backgroudRunningService.class));
                    TextView text = (TextView) dialog.findViewById(R.id.text_dialog);
                    text.setText("Disable Background Services with Torch?");
                    onoff.setChecked(true);
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied for the Camera", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public boolean checkServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.itvillage.dev.torchlight_volumebutton.backgroudRunningService"
                    .equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
