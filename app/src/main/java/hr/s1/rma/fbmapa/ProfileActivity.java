package hr.s1.rma.fbmapa;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.graphics.BitmapFactory;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "*";

    //Firebase auth and Database
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mUserDatabase, mLocation;
    private String current_user_Id;

    // Profile picture
    private ImageView profileIV;
    private StorageReference mStorageRef;
    String imagePath = "";
    File localFile = null;

    //profile name and contact
    private TextView usernameTV,rideslabelTV, contactTV;

    // List of rides
    private ListView  ridesLV;
    private ArrayList<String>arrayList = new ArrayList<>();
    private ArrayAdapter<String>adapter;


    @Override
    protected void onStart() {
        super.onStart();
    }

    protected void showProfilePicture(){

        mStorageRef = FirebaseStorage.getInstance().getReference().child("images/users/"+current_user_Id+".jpg");
        Log.e(TAG,"mStorage Ref"+mStorageRef);

        try {
            localFile = File.createTempFile("images", "jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //TODO: ovaj dio dugo traje, trebao bi se događat posebno uz progress dialog
        mStorageRef.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        // Successfully downloaded data to local file
                        Bitmap b;
                        try {
                            b = BitmapFactory.decodeStream(new FileInputStream(localFile));
                            profileIV.setImageBitmap(b);
                            Log.e(TAG,"Slikica je pronađena na firebaseu");


                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        loadImageWithResize(localFile, profileIV);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle failed download
                Log.e(TAG,"Slikica NIJE pronađena na firebaseu");
                profileIV.setImageResource(R.drawable.search);
            }
        });

// set clickable
        profileIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isReadStoragePermissionGranted()){
                    Intent i = new Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, 101); // request code 101 (proizvoljno)
                }
                else{

                }
            }
        });
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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        mAuth = FirebaseAuth.getInstance();
        current_user_Id = mAuth.getCurrentUser().getUid();

        mDatabase = FirebaseDatabase.getInstance();
        mUserDatabase = mDatabase.getReference().child("Users");
        mLocation   = mDatabase.getReference().child("location");

        usernameTV = (TextView)findViewById(R.id.profile_username);
        mUserDatabase.child(current_user_Id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String username = dataSnapshot.child("Username").getValue(String.class);
                String contact = dataSnapshot.child("kontakt").getValue(String.class);

                String normalizedPhoneNumber = PhoneNumberUtils.normalizeNumber(contact);
                String formattedNumber = PhoneNumberUtils.formatNumber(normalizedPhoneNumber, Locale.getDefault().getCountry());
                String phoneNumber = String.format("tel: %s", formattedNumber);

                Log.e(TAG,normalizedPhoneNumber);
                Log.e(TAG,formattedNumber);
                Log.e(TAG,phoneNumber);

                contactTV.setText(formattedNumber);

                Log.e(TAG, "Username u profilu " + username);
                usernameTV.setText(username);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        String current_user_email = mAuth.getCurrentUser().getEmail();

        contactTV=findViewById(R.id.contact_profile);
        contactTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TextView textView = (TextView) findViewById(R.id.number_to_call);
                // Use format with "tel:" and phone number to create phoneNumber.
                String phoneNumber = String.format("tel: %s", contactTV.getText().toString());

                // Create the intent.
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                // Set the data for the intent as the phone number.
                dialIntent.setData(Uri.parse(phoneNumber));
                // If package resolves to an app, send intent.
                if (dialIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(dialIntent);
                } else {
                    Log.e(TAG, "Can't resolve app for ACTION_DIAL Intent.");
                }
            }
        });

        rideslabelTV = (TextView)findViewById(R.id.rideslabel_profile);
        rideslabelTV.setText("Moje vožnje:");
        //rideslabelTV.setText(current_user_email);

        profileIV = findViewById(R.id.image_profile);
        showProfilePicture();

        ridesLV=findViewById(R.id.rides_profile);
        adapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,arrayList); //!? neznam jel radilo bez String
        ridesLV.setAdapter(adapter);

        mLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                DataSnapshot  c = dataSnapshot.child(current_user_Id);
                String s= c.child("start").getValue(String.class);
                String e= c.child("end").getValue(String.class);
                String vozac = c.child("vozac").getValue(String.class);

                String contact = c.child("kontakt").getValue(String.class);
/*                String normalizedPhoneNumber = PhoneNumberUtils.normalizeNumber(contact);
                String formattedNumber = PhoneNumberUtils.formatNumber(normalizedPhoneNumber, Locale.getDefault().getCountry());
                String phoneNumber = String.format("tel: %s", formattedNumber);
                contactTV.setText(phoneNumber);*/
                arrayList.add(s + " " + e +"\n" +"vozi me: "+ vozac);
                adapter.notifyDataSetChanged();

            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        ridesLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String choosen = (String) ridesLV.getItemAtPosition(position);
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

                localFile = new File(imagePath);
                Uri uri = Uri.fromFile(localFile);
                Log.e(TAG, "imagePath "+imagePath);
                Log.e(TAG,"mStorageRef "+mStorageRef);

                //TODO: ovaj dio dugo traje, trebao bi se događat posebno uz progress dialog
                mStorageRef.putFile(uri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // Get a URL to the uploaded content
                                StorageReference downloadUrl = taskSnapshot.getStorage();
                                Log.e(TAG,downloadUrl.getPath());

                                Bitmap b;
                                try {
                                    b = BitmapFactory.decodeStream(new FileInputStream(localFile));
                                    profileIV.setImageBitmap(b);

                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                                loadImageWithResize(localFile, profileIV);


                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                // ...
                                Log.e(TAG,exception.getMessage());
                            }
                        });

            }
        }
    }

    private void loadImageWithResize(File f, ImageView IV){
        Picasso.get()
                .load(f)
                .fit()
                .centerCrop()
                .into(IV);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));

    }

    public  boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                    Log.v(TAG,"Permission is granted1");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked1");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted1");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 3:
                Log.d(TAG, "External storage1");
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
                    //resume tasks needing this permission
                    Intent i = new Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, 101); // request code 101 (proizvoljno)

                }else{
                   // progress.dismiss();
                    Log.v(TAG,"nema dopustenja?");
                }
                break;
        }
    }


    public void dialNumber() {
/*       // TextView textView = (TextView) findViewById(R.id.number_to_call);
        // Use format with "tel:" and phone number to create phoneNumber.
        String phoneNumber = String.format("tel: %s", contactTV.getText().toString());

        // Create the intent.
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        // Set the data for the intent as the phone number.
        dialIntent.setData(Uri.parse(phoneNumber));
        // If package resolves to an app, send intent.
        if (dialIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(dialIntent);
        } else {
            Log.e(TAG, "Can't resolve app for ACTION_DIAL Intent.");
        }*/
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
