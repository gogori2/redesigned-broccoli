package hr.s1.rma.fbmapa;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "*";

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;

    private DatabaseReference mUserDatabase, mLocation;

    private String username;

    private TextView usernameTV,emailTV;
    private ImageView profileIV;
    private ListView  ridesLV;

    private ArrayList<String>arrayList = new ArrayList<>();
    private ArrayAdapter<String>adapter;


    private String current_user_Id;
    byte[] default_image;
    int default_image_length=0;


    String imagePath = "";

    @Override
    protected void onStart() {
        super.onStart();
    }

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

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        current_user_Id = mAuth.getCurrentUser().getUid();
        String cuurent_user_email = mAuth.getCurrentUser().getEmail();
        usernameTV = (TextView)findViewById(R.id.profile_username);
        //usernameTV.setText() je u onDataChange
        emailTV = (TextView)findViewById(R.id.email_profile);
        emailTV.setText(cuurent_user_email);

        mUserDatabase = mDatabase.getReference().child("Users");
        mLocation   = mDatabase.getReference().child("location");

        profileIV = findViewById(R.id.image_profile);

        profileIV = findViewById(R.id.image_profile);

        if (default_image_length != 0) {
            Glide.with(this)
                    .load(default_image)
                    .asBitmap()
                    .into(profileIV);
        } else {
            Glide.with(this)
                    .load(R.drawable.search)
                    .asBitmap()
                    .into(profileIV);
        }

        ridesLV=findViewById(R.id.rides_profile);
        adapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,arrayList); //!?
        ridesLV.setAdapter(adapter);

        ridesLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String choosen = (String) ridesLV.getItemAtPosition(position);


            }
        });

        mLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                DataSnapshot  c = dataSnapshot.child(current_user_Id);
                String s= c.child("start").getValue(String.class);
                String e= c.child("end").getValue(String.class);
                String vozac = c.child("vozac").getValue(String.class);

                String contact = c.child("kontakt").getValue(String.class);

                arrayList.add(s + " " + e +"\n" +"vozi me: "+ vozac);
                adapter.notifyDataSetChanged();

            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });


/*        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child : dataSnapshot.getChildren() ){
                    Log.e(TAG, "uid u profilu " + child.getKey());
                    String uid = child.getKey();
                    String username = dataSnapshot.child(uid).child("Username").getValue(String.class);
                    Log.e(TAG, "Username u profilu " + username);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });*/








        profileIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                if(checkFilePermissions())

                    Intent i = new Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, 101); // request code 101 (proizvoljno)



            }
        });


    }

/*    private boolean checkFilePermissions() {

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){

            int permissionCheck = ProfileActivity.this.checkSelfPermission("Manifest.permission.READ_EXTERNAL_STORAGE");

            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1001); //Any number
                return true;
            }else return false;
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
            return true;
        }
    }*/

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

                Glide.with(this).load(imagePath).into(profileIV);
            }
        }
    }

    // Pomocna metoda za ucitavanje slike u formatu oktetnog niza.
    // Pozicija slikovne datoteke cuva se u globalnoj varijabli imagePath.
    // Osigurano je (programski) da poziv ove metode ide isklucivo u slucaju kada imagePath NIJE "".
    public byte[] generateByteArrayImage()
            throws ExecutionException, InterruptedException {
        int width = profileIV.getWidth();
        int height = profileIV.getHeight();

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
        startActivity(new Intent(ProfileActivity.this, MapsActivity.class));
        finish();
    }

}
