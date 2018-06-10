package hr.s1.rma.fbmapa;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.r0adkll.slidr.Slidr;

public class HelpActivity extends AppCompatActivity {
//    private Slidr slidr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Pomoć i opis ukratko");
        setContentView(R.layout.activity_help);

        Slidr.attach(this);
    }
}
