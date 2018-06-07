package hr.s1.rma.fbmapa;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import hr.s1.rma.fbmapa.ExecutorValueEventListener;


import java.util.HashMap;
import java.util.Map;

public class PrijavaVoznjeActivity extends AppCompatActivity {

    private TextInputEditText mtime;
    private TextInputEditText  mkontakt;
    private TextInputEditText  mrazlog;
    private EditText mStart;
    private EditText mEnd;
    private Button mok_button,mcancel_button;
    private ProgressDialog prijavaProgress;
    private static final String TAG = "*";
    private String uid;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("location");
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_prijava_voznje);
        uid = currentUser.getUid();
        mtime = (TextInputEditText ) findViewById(R.id.time);
        mkontakt = (TextInputEditText )findViewById(R.id.kontakt);
        mrazlog = (TextInputEditText ) findViewById(R.id.razlog);
        mStart = (EditText ) findViewById(R.id.editTextStart);
        mEnd = (EditText ) findViewById(R.id.editTextEnd);
        mok_button = findViewById(R.id.ok_button);
        mcancel_button = findViewById(R.id.Cancel_button);
        prijavaProgress = new ProgressDialog(this);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        final String start = extras.getString("start");
        final String end = extras.getString("end");

        mStart.setText(start);
        mEnd.setText(end);
        mrazlog.setOnEditorActionListener(new TextInputEditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mok_button.performClick();
                    return true;
                }
                return false;
            }
        });
        mok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String time = mtime.getText().toString();
                String kontakt = mkontakt.getText().toString();
                String razlog = mrazlog.getText().toString();

                if (!TextUtils.isEmpty(time) && !TextUtils.isEmpty(kontakt) && !TextUtils.isEmpty(razlog)){
//                    prijavaProgress.setTitle("Sending in");
//                    prijavaProgress.setMessage("Please wait while we send your data");
//                    prijavaProgress.show();
//                    prijavaProgress.setCanceledOnTouchOutside(false);
                    //nadi_chosen(time,kontakt,razlog);
                    NadiChosenAsink asik = new NadiChosenAsink(time,kontakt,razlog);
                    asik.execute();
//                    Toast.makeText(PrijavaVoznjeActivity.this, "Voznja prijavljena", Toast.LENGTH_LONG).show();
//                    Intent returnIntent = new Intent();
//                    setResult(RESULT_OK, returnIntent);
//                    finish();
                }else{
                    Toast.makeText(PrijavaVoznjeActivity.this, "Something is missing", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mcancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//              Intent prijavaLink = new Intent(PrijavaVoznjeActivity.this, MapsActivity.class);
//              startActivity(prijavaLink);
                finish();
            }
        });
    }
    private class NadiChosenAsink extends AsyncTask<String, String, String>{
        private String time, kontakt, razlog;

        private NadiChosenAsink (String a, String b, String c){
            this.time=a;
            this.kontakt=b;
            this.razlog=c;
        }
        @Override
        protected String doInBackground(String... strings) {
            salji_moju_voznju(time,kontakt,razlog);
        return null;
        }
        @Override
        protected void onProgressUpdate(String... progress) {
        }
        @Override
        protected void onPostExecute(String result) {
            Log.e(TAG, "Ovo je onPostExecute");
            Intent returnIntent = new Intent();
            setResult(RESULT_OK, returnIntent);
            finish();
        }
    }

    public void salji_moju_voznju(final String time, final String kontakt, final String razlog){
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        final Double startLon = extras.getDouble("mS_lon");
        final Double startLat = extras.getDouble("mS_lat");
        final Double endLon = extras.getDouble("mE_lon");
        final Double endLat = extras.getDouble("mE_lat");
        final String username = extras.getString("username");
        final String start = extras.getString("start");
        final String end = extras.getString("end");

        Log.d(TAG, "Value in Main is: " + username);
        if(username == null){
            Toast.makeText(this, "Try Again", Toast.LENGTH_SHORT).show();
        }else{
            Message message = new Message(username, startLon, startLat, endLon, endLat, 1,
                                          username, time, kontakt, razlog, start, end);
            Map<String, Object> messageValues = message.toMap();
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put(uid, messageValues);
            myRef.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(PrijavaVoznjeActivity.this, "Successfully added your route", Toast.LENGTH_SHORT).show();
                }
            });

        }
//        mMap.clear();
    }
/*
    public void nadi_chosen(final String time, final String kontakt, final String razlog){
//        if(Looper.myLooper() == Looper.getMainLooper()){Log.e(TAG, "Ovo je UI thread"); }
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        final Double chosen_longitude = extras.getDouble("chosen_lon");
        final Double chosen_latitude = extras.getDouble("chosen_lat");
        final String user = extras.getString("username");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child : dataSnapshot.getChildren() ){
                    // Do magic here
                    String mUid = child.getKey();
                    Log.e(TAG, "Ovo je uid:" +  mUid);
                    Message message2 = child.getValue(Message.class);
                    Log.e(TAG, "Ovdje sam u nadi chosen Prijava Activity");
                    if(chosen_latitude == message2.latitudeStart && chosen_longitude == message2.longitudeStart){
                        Log.e(TAG, "Mijenjam status: "+ message2.id + " u " + message2.status);
                        if (message2.status == 2){
                            Toast.makeText(PrijavaVoznjeActivity.this, "This drive is already taken", Toast.LENGTH_LONG).show();
                        }else{
                            Message message3 = new Message(message2.id, message2.longitudeStart, message2.latitudeStart,
                                    message2.longitudeEnd, message2.latitudeEnd, 2, user, time, kontakt, razlog);
                            Map<String, Object> messageValues = message3.toMap();
                            Map<String, Object> childUpdates = new HashMap<>();
                            Log.e(TAG, "Ovdje sam, status: " + message3.status);
                            childUpdates.put(mUid, messageValues);
                            myRef.updateChildren(childUpdates);
                            //uCrveno = true;
                            // Izgradnja url-a za Directions API:
                            //                        String url = getDirectionsUrl(sydney4, sydney5);
                            //                        // Dohvat json podataka s Google Directions API-a:
                            //                        DownloadTask downloadTask = new DownloadTask();
                            //                        downloadTask.execute(url);
                        }
                    }else{
                        //bo
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Toast.makeText(this, "Voznja prijavljena", Toast.LENGTH_SHORT).show();
            }
        });
    }*/
}
