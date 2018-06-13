package hr.s1.rma.fbmapa;
import android.Manifest;
import android.app.ProgressDialog;
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
import android.util.TimeUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {


    private ProgressDialog mProfileProgress;
    private ProgressDialog mProfilePProgress;

    private static final String TAG = "*";

    //Firebase auth and Database
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseRef,mUserDatabase, mLocation, mDrives, mOffers,mOffers1,mOffers2;
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
    private ArrayList<Object>arrayList = new ArrayList<>();
    private ArrayAdapter<Object>adapter;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        current_user_Id = mAuth.getCurrentUser().getUid();

    }

    protected void showProfilePicture( ){

        mProfileProgress = new ProgressDialog(this);
        mProfileProgress.setTitle("Loading Profile");
        mProfileProgress.setMessage("Please wait while we load your profile!");
        mProfileProgress.setCanceledOnTouchOutside(false);
        mProfileProgress.show();

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
                        // Bitmap b;
                        try {
                            Log.e(TAG,"Slikica je pronađena na firebaseu");
                            /*
                            b = BitmapFactory.decodeStream(new FileInputStream(localFile));
                            profileIV.setImageBitmap(b);
*/
                            loadImageWithResize(localFile, profileIV);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        mProfileProgress.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle failed download
                Log.e(TAG,"Slikica NIJE pronađena na firebaseu");
                profileIV.setImageResource(R.drawable.search);
                mProfileProgress.dismiss();

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
        mDatabaseRef= mDatabase.getReference();
        mUserDatabase = mDatabase.getReference().child("Users");

        mLocation   = mDatabase.getReference().child("location");
        mDrives = mDatabase.getReference().child("drives");

        profileIV = findViewById(R.id.image_profile);
        showProfilePicture();

        usernameTV = (TextView)findViewById(R.id.profile_username);

        mUserDatabase.child(current_user_Id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String username = dataSnapshot.child("Username").getValue(String.class);
                String contact = dataSnapshot.child("kontakt").getValue(String.class);

                try{
                    String normalizedPhoneNumber = PhoneNumberUtils.normalizeNumber(contact);
                    String formattedNumber = PhoneNumberUtils.formatNumber(normalizedPhoneNumber, Locale.getDefault().getCountry());
                    String phoneNumber = String.format("tel: %s", formattedNumber);

                    Log.e(TAG,normalizedPhoneNumber);
                    Log.e(TAG,formattedNumber);
                    Log.e(TAG,phoneNumber);

                    contact= formattedNumber;

                }catch (Exception e){
                    Log.e(TAG,e.toString());
                }

                contactTV.setText(contact);

                Log.e(TAG, "Username u profilu " + username);
                usernameTV.setText(username);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        contactTV=findViewById(R.id.contact_profile);
        contactTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
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
                }catch (Exception e){
                    Log.e(TAG,e.toString());
                }

            }
        });

        rideslabelTV = (TextView)findViewById(R.id.rideslabel_profile);
        rideslabelTV.setText("Moje vožnje:");

        ridesLV=findViewById(R.id.rides_profile);
        adapter= new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,arrayList); //!? neznam jel radilo bez String
        ridesLV.setAdapter(adapter);


        ridesLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Dohvat odabrane stvake (objekt Student):

                try{
                    Ride r = (Ride) ridesLV.getItemAtPosition(position);
                    if(r.getStatus()==2){
                        String  OtherUsername = r.getVozac();
                        // Slanje odabranog Student objekta u novu aktivnost (za azuriranje podataka):

                        if (r != null && r.status!=1) {
                            Intent intent = new Intent(
                                    ProfileActivity.this,
                                    OtherProfileActivity.class);
                            Log.e(TAG,"prije intenta "+OtherUsername);
                            intent.putExtra("OtherUsername", OtherUsername);
                            startActivity(intent);
                        }
                    }


                }catch (Exception e){
                    Log.e(TAG,e.toString());
                    arrayList.remove(position);
                    adapter.notifyDataSetChanged();
                    otkazi_moju_voznju(0);
                }
            }

            public void otkazi_moju_voznju(int uloga){
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                String uid = currentUser.getUid();
                if(uloga==1){
                    mLocation.child(uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(ProfileActivity.this, "Successfully canceled", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    mDrives.child(uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(ProfileActivity.this, "Successfully canceled", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });

        mLocation.child(current_user_Id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {
                    DataSnapshot  c = dataSnapshot;
                    String s= c.child("start").getValue(String.class);
                    String e= c.child("end").getValue(String.class);
                    String vozac = c.child("vozac").getValue(String.class);
                    Long status = c.child("status").getValue(Long.class);

                    String contact = c.child("kontakt").getValue(String.class);
/*                String normalizedPhoneNumber = PhoneNumberUtils.normalizeNumber(contact);
                String formattedNumber = PhoneNumberUtils.formatNumber(normalizedPhoneNumber, Locale.getDefault().getCountry());
                String phoneNumber = String.format("tel: %s", formattedNumber);
                contactTV.setText(phoneNumber);*/

                    //arrayList.add(ispis);
                    Ride r = new Ride(s,e,vozac,contact,status);
                    arrayList.add(r);
                    adapter.notifyDataSetChanged();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        mDrives.child(current_user_Id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                try{
                    DataSnapshot  c = dataSnapshot;

                    String s= c.child("start").getValue(String.class);
                    String e= c.child("end").getValue(String.class);

                    String vozac = c.child("id").getValue(String.class);
                    Long status = c.child("status").getValue(Long.class);

                    String time = c.child("time").getValue(String.class);
                    DateFormat format = new SimpleDateFormat("HH:mm");
                    time = format.format(format.parse(time));

                    String contact = c.child("kontakt").getValue(String.class);

                    if(s!=null && e!=null){
                        String ispis= s + " " + e +" "+ time;
//                        String ispis= s + " " + e +" "+ time;

                        if(status<4) ispis=ispis+"\n"+"ima još mjesta";
                        else ispis=ispis+"\n"+"sve zauzeto";

                        arrayList.clear();
                        arrayList.add(ispis);
                        adapter.notifyDataSetChanged();

                    }
                }catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG,e.toString());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



/*
        mLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {
                    DataSnapshot  c = dataSnapshot.child(current_user_Id);
                    String s= c.child("start").getValue(String.class);
                    String e= c.child("end").getValue(String.class);
                    String vozac = c.child("vozac").getValue(String.class);
                    String status = c.child("status").getValue(String.class);

                    String contact = c.child("kontakt").getValue(String.class);

                    if(s!=null && e!=null){
                        String ispis= s + " " + e;

                        if(Integer.getInteger(status)==1) ispis=ispis+"\n"+"vozi me: "+ vozac;
                        else ispis=ispis+"\n"+"vožnja nije potvrđena";

                        arrayList.add(ispis);
                        adapter.notifyDataSetChanged();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        mDrives.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                try{
                    DataSnapshot  c = dataSnapshot.child(current_user_Id);

                    String s= c.child("start").getValue(String.class);
                    String e= c.child("end").getValue(String.class);

                    String vozac = c.child("vozac").getValue(String.class);
                    Long status = c.child("status").getValue(Long.class);

                    String time = c.child("time").getValue(String.class);
                    DateFormat format = new SimpleDateFormat("HH:mm");
                    Date date1 = format.parse(time);

                    String contact = c.child("kontakt").getValue(String.class);

                    if(s!=null && e!=null){
                        String ispis= s + " " + e +" "+ date1;
//                        String ispis= s + " " + e +" "+ time;

                        if(status<4) ispis=ispis+"\n"+"ima još mjesta";
                        else ispis=ispis+"\n"+"sve zauzeto";

                        arrayList.add(ispis);
                        adapter.notifyDataSetChanged();
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG,e.toString());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
*/


/*
        Query query = mDatabase.getReference().child("Offers").orderByChild("id").equalTo(current_user_Id);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e(TAG,"Sve voznje vozaca "+current_user_Id);
                Log.e(TAG,dataSnapshot.getKey());

                arrayList.add(dataSnapshot.getKey());
                arrayList.add(dataSnapshot.getKey());

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/


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

                loadImageWithResize(localFile, profileIV);

                Log.e(TAG, "imagePath "+imagePath);
                Log.e(TAG,"mStorageRef "+mStorageRef);

                //TODO: ovaj dio dugo traje, trebao bi se događat posebno uz progress dialog
                mStorageRef.putFile(uri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Toast.makeText(ProfileActivity.this, "Profile picture saved",
                                        Toast.LENGTH_SHORT).show();

                                // Get a URL to the uploaded content
                                StorageReference downloadUrl = taskSnapshot.getStorage();
                                Log.e(TAG,downloadUrl.getPath());

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


/*        mOffers1= mDatabase.getReference().child("Offers").child(current_user_Id+"_"+1);
        mOffers2= mDatabase.getReference().child("Offers").child(current_user_Id+"_"+2);

        mOffers1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                String s= dataSnapshot.child("start").getValue(String.class);
//                Log.e(TAG,"jajaja");
//                arrayList.add(s);
//                adapter.notifyDataSetChanged();

                try {
                    DataSnapshot  c = dataSnapshot;
                    String s= c.child("start").getValue(String.class);
                    String e= c.child("end").getValue(String.class);
                    String vozac = c.child("vozac").getValue(String.class);
                    String status = c.child("status").getValue(String.class);

                    String contact = c.child("kontakt").getValue(String.class);
//                String normalizedPhoneNumber = PhoneNumberUtils.normalizeNumber(contact);
//                String formattedNumber = PhoneNumberUtils.formatNumber(normalizedPhoneNumber, Locale.getDefault().getCountry());
//                String phoneNumber = String.format("tel: %s", formattedNumber);
//                contactTV.setText(phoneNumber);

                    if(s!=null && e!=null){
                        String ispis= s + " " + e;

                        if(Integer.getInteger(status)==1) ispis=ispis+"\n"+"vozi me: "+ vozac;
                        else ispis=ispis+"\n"+"vožnja nije potvrđena";

                        arrayList.add(ispis);
                        adapter.notifyDataSetChanged();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    arrayList.add("greška");
                    adapter.notifyDataSetChanged();
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/
/*

        mOffers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {
                    DataSnapshot  c = dataSnapshot.child(current_user_Id+"_"+1);
                    String s= c.child("start").getValue(String.class);
                    String e= c.child("end").getValue(String.class);
                    String vozac = c.child("vozac").getValue(String.class);
                    String status = c.child("status").getValue(String.class);

                    String contact = c.child("kontakt").getValue(String.class);
//                String normalizedPhoneNumber = PhoneNumberUtils.normalizeNumber(contact);
//                String formattedNumber = PhoneNumberUtils.formatNumber(normalizedPhoneNumber, Locale.getDefault().getCountry());
//                String phoneNumber = String.format("tel: %s", formattedNumber);
//                contactTV.setText(phoneNumber);

                    if(s!=null && e!=null){
                        String ispis= s + " " + e;

                        if(Integer.getInteger(status)==1) ispis=ispis+"\n"+"vozi me: "+ vozac;
                        else ispis=ispis+"\n"+"vožnja nije potvrđena";

                        arrayList.add(ispis);
                        adapter.notifyDataSetChanged();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                try {
                    DataSnapshot  c = dataSnapshot.child(current_user_Id+"_"+2);
                    String s= c.child("start").getValue(String.class);
                    String e= c.child("end").getValue(String.class);
                    String vozac = c.child("vozac").getValue(String.class);
                    String status = c.child("status").getValue(String.class);

                    String contact = c.child("kontakt").getValue(String.class);
//                String normalizedPhoneNumber = PhoneNumberUtils.normalizeNumber(contact);
//                String formattedNumber = PhoneNumberUtils.formatNumber(normalizedPhoneNumber, Locale.getDefault().getCountry());
//                String phoneNumber = String.format("tel: %s", formattedNumber);
//                contactTV.setText(phoneNumber);

                    if(s!=null && e!=null){
                        String ispis= s + " " + e;

                        if(Integer.getInteger(status)==1) ispis=ispis+"\n"+"vozi me: "+ vozac;
                        else ispis=ispis+"\n"+"vožnja nije potvrđena";

                        arrayList.add(ispis);
                        adapter.notifyDataSetChanged();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
*/
