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
    private Integer uloga;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("location");
    DatabaseReference myCarRef = database.getReference("drives");
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
        uloga = extras.getInt("uloga");
        mStart.setText(start);
        mEnd.setText(end);
        if(uloga==2) {
            mrazlog.setHint("Imam novu škodu...");
        }else{
            mrazlog.setHint("Razlog");
        }
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
                    NadiChosenAsink asik = new NadiChosenAsink(time,kontakt,razlog);
                    asik.execute();
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
        final String start = mStart.getText().toString();
        final String end = mEnd.getText().toString();
        Log.d(TAG, "Value in Main is: " + username);
        if(username == null){
            Toast.makeText(this, "You are not signed in", Toast.LENGTH_SHORT).show();
        }else{
            if(uloga==1){
            Message message = new Message(username, startLon, startLat, endLon, endLat, 1,
                                          username, time, kontakt, razlog, start, end);
            Map<String, Object> messageValues = message.toMap();
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put(uid, messageValues);

                myRef.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
//                        prijavaProgress.dismiss();
                            Toast.makeText(PrijavaVoznjeActivity.this, "Successfully added your route", Toast.LENGTH_SHORT).show();
                        }else{
//                        prijavaProgress.hide();
                            Toast.makeText(PrijavaVoznjeActivity.this, "Something went wrong, try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }else{
                Message message = new Message(username, startLon, startLat, endLon, endLat, 3,
                        username, time, kontakt, razlog, start, end);
                Map<String, Object> messageValues = message.toMap();
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put(uid, messageValues);
                myCarRef.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
//                        prijavaProgress.dismiss();
                            Toast.makeText(PrijavaVoznjeActivity.this, "Successfully added your route", Toast.LENGTH_SHORT).show();
                        }else{
//                        prijavaProgress.hide();
                            Toast.makeText(PrijavaVoznjeActivity.this, "Something went wrong, try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
//        mMap.clear();
    }
}
