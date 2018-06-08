package hr.s1.rma.fbmapa;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class WelcomeActivity extends AppCompatActivity {

    private Button myRegButton;
    private Button myLogButton;
    private TextView button;
    private static final String TAG = "*";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Log.e(TAG, "Evo me u Welcome");

        myRegButton = findViewById(R.id.reg_button);
        myLogButton = findViewById(R.id.login_button_welcome);
        button = findViewById(R.id.zasto_text_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent zastointent = new Intent(WelcomeActivity.this, ProfileActivity.class);
                startActivity(zastointent);
            }
        });
        myRegButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent reg_intent = new Intent(WelcomeActivity.this, RegisterActivity.class);
               startActivity(reg_intent);
            }
        });
        myLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SaveSharedPreference.getUserName(WelcomeActivity.this).length() == 0){
                    //nastavi dalje, nema username zapisan
                    Intent log_intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                    startActivity(log_intent);
                }
                else{
                    //preskoci form i vrati me u Main
                    Intent mainIntent = new Intent(WelcomeActivity.this, MapsActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                }
            }
        });
    }
}
