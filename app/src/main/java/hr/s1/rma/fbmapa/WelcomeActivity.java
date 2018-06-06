package hr.s1.rma.fbmapa;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class WelcomeActivity extends AppCompatActivity {

    private Button myRegButton;
    private Button myLogButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        myRegButton = findViewById(R.id.reg_button);
        myLogButton = findViewById(R.id.login_button_welcome);
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
                Intent log_intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(log_intent);
            }
        });

    }
}