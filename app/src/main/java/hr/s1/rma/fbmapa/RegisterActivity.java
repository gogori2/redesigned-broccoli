package hr.s1.rma.fbmapa;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText  mUsername;
    private TextInputEditText  mEmail;
    private TextInputEditText  mPassword;
    private Button mCreateAcc;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mdatabase;
    private static final String TAG = "*";

    private ProgressDialog mRegProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        mUsername = (TextInputEditText ) findViewById(R.id.reg_username);
        mEmail = (TextInputEditText )findViewById(R.id.reg_email);
        mPassword = (TextInputEditText )findViewById(R.id.reg_password);
        mCreateAcc = findViewById(R.id.reg_create_button);

        mRegProgress = new ProgressDialog(this);
        mPassword.setOnEditorActionListener(new TextInputEditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mCreateAcc.performClick();
                    return true;
                }
                return false;
            }
        });
        mCreateAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsername.getText().toString();
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) ){
                    register_user(username, email, password);
                    mRegProgress.setTitle("Registering user");
                    mRegProgress.setMessage("Please wait while we create your account !");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();
                }else{
                    Toast.makeText(RegisterActivity.this, "Something is wrong, check your form", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void register_user(final String username, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mRegProgress.dismiss();
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = currentUser.getUid();
                            //spremi login u sharedpref da moze iz logina procitati
                            /*SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("username",username);  // Saving string
                            editor.apply(); // commit changes*/
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            mdatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                            //prvo pobrisi a onda spremi u shared pref username
                            SaveSharedPreference.clearUserName(RegisterActivity.this);
                            SaveSharedPreference.setUserName(RegisterActivity.this, username);
                            HashMap<String, Object> result = new HashMap<>();
                            result.put("Username", username);
                            result.put("deviceToken", deviceToken);
                            mdatabase.setValue(result).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Intent mainIntent = new Intent(RegisterActivity.this, MapsActivity.class);
                                    //proslijedi username u mainactivity
                                    //mainIntent.putExtra("username",username);
                                    //kad stisnes back u mainactivityju ode van a ne u welcome
                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainIntent);
                                    finish();
                                }
                            });

                        } else {
                            mRegProgress.hide();
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                        // ...
                    }
                });
    }


}
