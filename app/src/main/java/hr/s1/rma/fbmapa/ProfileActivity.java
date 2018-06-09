package hr.s1.rma.fbmapa;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.bumptech.glide.Glide;

import java.util.concurrent.ExecutionException;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "*";

    private DatabaseReference mUserDatabase;
    String username;
    private FirebaseAuth mAuth;
    private TextView usernameTV,emailTV;
    private ImageView myIV;
    private String uid;


    byte[] default_image;
    int default_image_length=0;


    String imagePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.e(TAG, "pokrenut Profil");
//        treba dodati toolbar i svasta da bi radila strjelica
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (this.getSupportActionBar() != null) {
            this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            this.getSupportActionBar().setHomeButtonEnabled(true);
            this.getSupportActionBar().setTitle("Moj Profil");
        }

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();
        String current_user_Id = mAuth.getCurrentUser().getUid();
        String cuurent_user_email = mAuth.getCurrentUser().getEmail();

        usernameTV = (TextView)findViewById(R.id.profile_username);
        emailTV = (TextView)findViewById(R.id.email_profile);
        emailTV.setText(cuurent_user_email);
        myIV = findViewById(R.id.image_profile);

        myIV = findViewById(R.id.image_profile);
        if (default_image_length != 0) {
            Glide.with(this)
                    .load(default_image)
                    .asBitmap()
                    .into(myIV);
        } else {
            Glide.with(this)
                    .load(R.drawable.search)
                    .asBitmap()
                    .into(myIV);
        }

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

        myIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 101); // request code 101 (proizvoljno)

            }
        });
    }

    // Preuzimanje (odabrane) slike iz galerije i njezino pozicioniranje / prikazivanje
    // u elementu ImageView:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Kao povratni rezultat iz galerije dolazi Uri odabrane slike (data):
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();

            // Pretraga Media content privider-a po Uri-u:
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = null;
            if (selectedImage != null) {
                cursor = getContentResolver().query(
                        selectedImage,
                        filePathColumn,
                        null, null, null);
            }
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imagePath = cursor.getString(columnIndex);
                cursor.close();
            }

            if (!imagePath.equals("")) {

                Glide.with(this).load(imagePath).into(myIV);
            }
        }
    }

    // Pomocna metoda za ucitavanje slike u formatu oktetnog niza.
    // Pozicija slikovne datoteke cuva se u globalnoj varijabli imagePath.
    // Osigurano je (programski) da poziv ove metode ide isklucivo u slucaju kada imagePath NIJE "".
    public byte[] generateByteArrayImage()
            throws ExecutionException, InterruptedException {
        int width = myIV.getWidth();
        int height = myIV.getHeight();

        return Glide.with(this).load(this.imagePath)
                .asBitmap() //najnovija verzija bumptheh glide ne podržava
                .toBytes(Bitmap.CompressFormat.JPEG, 70)    // proizvoljna razina kompresiranja
                .centerCrop()
                .into(width, height)
                .get();
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
/*
package hr.s1.rma.fbmapa;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

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

        mUserDatabase.child(current_user_Id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                username = dataSnapshot.child("Username").getValue(String.class);
                Log.e(TAG, "Username u profilu " + username);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
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
}*/
