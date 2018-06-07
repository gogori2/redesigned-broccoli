package hr.s1.rma.fbmapa;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toolbar;

import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "*";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.e(TAG, "pokrenut Profil");
        //treba dodati toolbar i svasta da bi radila strjelica

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Log.e(TAG, "stisnut back u Profilu");
        startActivity(new Intent(ProfileActivity.this, MapsActivity.class));
        finish();
    }
}
