package hr.s1.rma.fbmapa;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "*";

    private DatabaseReference mUserDatabase;
    String username;
    private FirebaseAuth mAuth;
    private TextView usernameTV,emailTV;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.e(TAG, "pokrenut Profil");
//        treba dodati toolbar i svasta da bi radila strjelica
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();
        String current_user_Id = mAuth.getCurrentUser().getUid();
        String cuurent_user_email = mAuth.getCurrentUser().getEmail();

        usernameTV = (TextView)findViewById(R.id.profile_username);
        emailTV = (TextView)findViewById(R.id.email_profile);
        emailTV.setText(cuurent_user_email);

        mUserDatabase.child(current_user_Id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                username = dataSnapshot.child("Username").getValue(String.class);
                Log.e(TAG, "Username u profilu " + username);
                usernameTV.setText(username);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

//        usernameTV.setText(username);
        Log.e(TAG, "Username u profilu " + username);

    }
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Log.e(TAG, "stisnut back u Profilu");
        //start activity je pokrene ispocetka
        //startActivity(new Intent(ProfileActivity.this, MapsActivity.class));
        finish();
    }
}
