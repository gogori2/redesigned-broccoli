package hr.s1.rma.fbmapa;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText  LoginEmail;
    private TextInputEditText  LoginPassword;
    private Button loginButton;
    private ProgressDialog LoginProgress;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    private static final String TAG = "*";
    private String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Prijava korisnika");
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        LoginEmail = (TextInputEditText) findViewById(R.id.log_email);
        LoginPassword = (TextInputEditText)findViewById(R.id.log_password);
        loginButton = findViewById(R.id.log_button);
        LoginProgress = new ProgressDialog(this);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        //da enter postane submit!
        LoginPassword.setOnEditorActionListener(new TextInputEditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginButton.performClick();
                    return true;
                }
                return false;
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = LoginEmail.getText().toString();
                String password = LoginPassword.getText().toString();
                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
                    LoginProgress.setTitle("Logging in");
                    LoginProgress.setMessage("Please wait while we check your credentials");
                    LoginProgress.setCanceledOnTouchOutside(false);
                    LoginProgress.show();
                    login_user(email, password);
                }else{
                    Toast.makeText(LoginActivity.this, "Something is missing",
                    Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    public void finish(){
        super.finish();
        overridePendingTransition(R.anim.swipe_in_left, R.anim.swipe_out_right);

    }
    private void login_user(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            /*SharedPreferences myPref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                            String username=myPref.getString("username", null);         // getting String*/
                            LoginProgress.dismiss();
                            // Sign in success, update UI with the signed-in user's information
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            String current_user_Id = mAuth.getCurrentUser().getUid();
                            mUserDatabase.child(current_user_Id).child("deviceToken").setValue(deviceToken);
                            mUserDatabase.child(current_user_Id).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    username = dataSnapshot.child("Username").getValue(String.class);
                                    Log.e(TAG, "Username u loginu " + username);
                                    //zapisi u shared pref
                                    SaveSharedPreference.setUserName(LoginActivity.this,username);
                                    Log.e(TAG, "Value is: " + username);
                                    Log.e(TAG, "signInWithEmail:success");
                                    logiran_sam_vratiMeuMain();
                                }
                                @Override
                                public void onCancelled(DatabaseError error) {
                                    Log.w(TAG, "Failed to read value.", error.toException());
                                }
                            });
                        // ovdje se nista ne izvrsava !!!!! IPAK SE IZVRŠAVA ALI PRIJE SVEGA
                            Log.e(TAG, "signInWithEmail:NIKAD");
                        } else {
                            LoginProgress.hide();
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed. Please check the form and try again ",
                            Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void logiran_sam_vratiMeuMain (){
        Log.e(TAG, "logiran_sam_vratiMeuMain");
        Intent mainIntent = new Intent(LoginActivity.this, MapsActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
