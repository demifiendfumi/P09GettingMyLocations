package demoshowsms.android.myapplicationdev.com.p09_gettingmylocations;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class MainActivity extends AppCompatActivity {
    Button btnStart, btnStop, btnCheckRecord;
    TextView txtRecord;
    FusedLocationProviderClient client;
    String folderLocation;
    String lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnStart = findViewById(R.id. btnStartDetector);
        btnStop = findViewById(R.id. btnStopDetector);
        btnCheckRecord = findViewById(R.id. btnCheckRecords);

        client = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        final LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setSmallestDisplacement(100);

        final LocationCallback mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult){
                if(locationResult != null){
                    Location data = locationResult.getLastLocation();
                    double lat = data.getLatitude();
                    double lng = data.getLongitude();
                }
            }
        };

        if (checkPermission() == true){
            Task<Location> task = client.getLastLocation();
            task.addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location!=null){
                        String msg = "Last known location when this Activity started:\n"
                                + "Latitude : " + location.getLatitude() + "\n" +
                                "Longitude : " + location.getLongitude();
                        lat = String.valueOf(location.getLatitude());
                        lng = String.valueOf(location.getLongitude());
                        txtRecord.setText(msg);
                    }else{
                        String msg = "No Last Known Location found";
                        txtRecord.setText(msg);
                    }
                }
            });
        }

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MyService.class);
                startService(i);
                if (checkPermission() == true){
                    client.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                    try{
                        File targetFile = new File(folderLocation, "data.txt");
                        FileWriter writer = new FileWriter(targetFile, true);
                        writer.write(lat + ", " + lng + "\n");
                        writer.flush();
                        writer.close();

                    }catch (Exception e){
                        Toast.makeText(MainActivity.this, "Failed to write!", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MyService.class);
                stopService(i);
                client.removeLocationUpdates(mLocationCallback);
            }
        });

        btnCheckRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File targetfile = new File(folderLocation, "data.txt");
                if (targetfile.exists() == true){
                    String data = "";
                    try{
                        FileReader reader = new FileReader(targetfile);
                        BufferedReader br = new BufferedReader(reader);
                        String line = br.readLine();
                        while(line != null){
                            data += line + "\n";
                            line = br.readLine();
                        }
                        br.close();
                        reader.close();

                    }catch (Exception e){
                        Toast.makeText(MainActivity.this, "Failed to read!", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                    Log.d("Content", data);
                }
            }
        });
    }

    private boolean checkPermission(){
        int permissionCheck_Coarse = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck_Coarse == PermissionChecker.PERMISSION_GRANTED || permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED){
            return true;
        }else{
            return false;
        }
    }
}
