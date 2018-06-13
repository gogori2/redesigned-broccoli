package hr.s1.rma.fbmapa;

import android.app.ProgressDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myDrives = database.getReference("drives");
    DatabaseReference myRef = database.getReference("location");
    DatabaseReference myPass = database.getReference("passenger");
    private DatabaseReference mUserDatabase, Users, Ref;
    public boolean voznja = false, prviPut=true, stisnut=true;
    public boolean klikNaInfo = false, uCrveno=false;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    static LatLng sydney = new LatLng(45.340692, 14.407214);
    static LatLng sydney2 = new LatLng(45.340737, 14.408180);
    static LatLng sydney3 = new LatLng(45.34306, 14.40917);
    static LatLng sydney4 = new LatLng(45.340737, 14.408180);
    static LatLng sydney5 = new LatLng(45.34306, 14.40917);
    static LatLng mojStart = new LatLng(45.340692, 14.407214);
    static LatLng mojEnd = new LatLng(45.340737, 14.408180);
    static LatLng chosenOne = new LatLng(0, 0);
    private FirebaseAuth mAuth;
    public int j = 0, i = 0, uloga = 0, upisaniPutnici;
    Button prijavi, obrisi;
    ImageButton refresh,rute;
    Marker mMarker;
    static double mojStartLat, mojStartLon, mojEndLat, mojEndLon;
    private GoogleMap mMap;
    private static final String TAG = "*";
    private ArrayList<Message> messageList = new ArrayList<Message>();
    private String username, uid, uidVozaca;
    private TextView aStart,aEnd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("location");
        Log.e(TAG, "Username na pocetku: " + currentUser);
        if(check_login_status()){
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            refresh = findViewById(R.id.refresh);
        }else{
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        // Check if user is signed in (non-null) and update UI accordingly.
        //check_login_status();
    }

    @Override
    public void onResume(){
        super.onResume();
        // put your code here...
        //check_login_status();
        if(uid==null){
            sendToWelcomePage();
        }else{
            if(voznja){
                preuzmi_i_crtaj_voznje();
            }
        }
    }
    private void sendToWelcomePage() {
        Log.e(TAG, "Send to welcome page");
        Intent WelcomeIntent = new Intent (MapsActivity.this, WelcomeActivity.class);
        startActivity(WelcomeIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        View refresh = findViewById(R.id.refresh);
        Button prijavi = findViewById(R.id.prijavi);
        Button obrisi = findViewById(R.id.obrisi);
        switch (item.getItemId()) {
            case R.id.putnik:
                voznja=false;
                prijavi.setEnabled(true);obrisi.setEnabled(true);
                prijavi.setText(this.getResources().getString(R.string.zatrazi_voznju));
                obrisi.setText(this.getResources().getString(R.string.obrisi_voznju));
                refresh.setVisibility(View.GONE);
                rute.setVisibility(View.GONE);
                aStart.setVisibility(View.VISIBLE);
                aEnd.setVisibility(View.VISIBLE);
                uloga = 1;
                moja_voznja(uloga);
                return true;
            case R.id.nudi_voznju:
                voznja=false;
                prijavi.setEnabled(true);obrisi.setEnabled(true);
                prijavi.setText(this.getResources().getString(R.string.ponudi_voznju));
                obrisi.setText(this.getResources().getString(R.string.obrisi_voznju));
                refresh.setVisibility(View.GONE);
                rute.setVisibility(View.GONE);
                aStart.setVisibility(View.VISIBLE);
                aEnd.setVisibility(View.VISIBLE);
                uloga=2;
                moja_voznja(uloga);
                return true;
            case R.id.vozac:
                voznja = true;
                //pokazi refresh butt
                refresh.setVisibility(View.VISIBLE);
                rute.setVisibility(View.VISIBLE);
                aStart.setVisibility(View.GONE);
                aEnd.setVisibility(View.GONE);
                prijavi.setEnabled(false); obrisi.setEnabled(false);
                prijavi.setText("Prihvati vožnju");
                obrisi.setText("Otkaži vožnju");
                if(prviPut){
                    preuzmi_i_crtaj_voznje();
                    prviPut=false;
                }else{
                    nacrtaj_voznje();
                }
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                SaveSharedPreference.clearUserName(MapsActivity.this);
                sendToWelcomePage();
                return true;
            case R.id.profil:
                Intent ProfileInt = new Intent (MapsActivity.this, ProfileActivity.class);
                startActivity(ProfileInt);
                return true;
            case R.id.about:
                Intent HelpInt = new Intent (MapsActivity.this, HelpActivity.class);
                startActivity(HelpInt);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void moja_voznja(int uloga) {
        mMap.clear();
        if(uloga==1){
            setTitle("Zatraži vožnju");
            Log.e(TAG, "moja voznja");
        }else{
            setTitle("Ponudi vožnju");
            Log.e(TAG, "ponudi voznju");
        }

        //da ne rusi prazan string
        aStart = findViewById(R.id.textStart);
        aEnd = findViewById(R.id.textEnd);
        if(aStart.getText().equals(null) && aEnd.getText().equals(null)){
            aStart.setText("Start");
            aEnd.setText("End");
        }
        final Marker markStart2;
        if(uloga==1){
            markStart2 = mMap.addMarker(new MarkerOptions()
                    .position(mojStart)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .draggable(true)
                    .title("Start"));
        }else {
            markStart2 = mMap.addMarker(new MarkerOptions()
                    .position(mojStart)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car))
                    .draggable(true)
                    .title("Start"));
        }

        final Marker markEnd2 = mMap.addMarker(new MarkerOptions()
                .position(mojEnd)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_endicon))
                .draggable(true)
                .title("End"));

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }
            @Override
            public void onMarkerDrag(Marker marker) {
            }
            @Override
            public void onMarkerDragEnd(Marker marker) {
                if (!voznja) {
                    mojStartLon = markStart2.getPosition().longitude;
                    mojStartLat = markStart2.getPosition().latitude;
                    mojStart = new LatLng(mojStartLat, mojStartLon);
                    aStart.setText(samo_ulica(getCompleteAddressString(mojStartLat, mojStartLon)));

                    mojEndLon = markEnd2.getPosition().longitude;
                    mojEndLat = markEnd2.getPosition().latitude;
                    mojEnd = new LatLng(mojEndLat, mojEndLon);
                    aEnd.setText(samo_ulica(getCompleteAddressString(mojEndLat, mojEndLon)));
                }
                else{
                }
            }
        });
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mojStart, 14.0f));
    }

    public void otkazi_moju_voznju(int uloga){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid();
        if(uloga==1){
            myRef.child(uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(MapsActivity.this, "Successfully canceled", Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            myDrives.child(uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(MapsActivity.this, "Successfully canceled", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
    public void nacrtaj_driving_rute(){
        mMap.clear();
        setTitle(getString(R.string.prihvati_label));
        Log.e(TAG, "velicina liste:" + messageList.size());

        for(int i=0; i<messageList.size();i++){

            Message message;
            message = messageList.get(i);
            sydney2 = new LatLng(message.latitudeEnd, message.longitudeEnd);
            sydney3 = new LatLng(message.latitudeStart, message.longitudeStart);

            if(message.status==3 || message.status==4){
                mMarker = mMap.addMarker(new MarkerOptions().position(sydney3)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car))
                        .title(message.id));

                mMap.addMarker(new MarkerOptions().position(sydney2)
                        .title("End")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_endicon)));
            }else{
                mMarker = mMap.addMarker(new MarkerOptions().position(sydney3)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_walkicon))
                        .title(message.id));

                mMap.addMarker(new MarkerOptions().position(sydney2)
                        .title("End")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_endicon)));
            }

            String url = getDirectionsUrl(sydney2, sydney3);
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(url);

            mMarker.setSnippet("Vrijeme polaska: " + message.time + "\nZašto mene:" + message.razlog + "\nKontakt:" + message.kontakt + "\nStart:" + message.start + "\nEnd:" + message.end + "\nbrPutnika:" + message.br_putnika);
        }


    }
    public void nacrtaj_voznje(){
        mMap.clear();
        setTitle(getString(R.string.prihvati_label));
        Log.e(TAG, "velicina liste:" + messageList.size());
        for(int i=0; i<messageList.size();i++){

            Message message;
            message = messageList.get(i);
            sydney2 = new LatLng(message.latitudeEnd, message.longitudeEnd);
            sydney3 = new LatLng(message.latitudeStart, message.longitudeStart);

            if(message.status==3 || message.status==4){
                mMarker = mMap.addMarker(new MarkerOptions().position(sydney3)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car))
                        .title(message.id));

                mMap.addMarker(new MarkerOptions().position(sydney2)
                        .title("End")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_endicon)));
            }else{
                mMarker = mMap.addMarker(new MarkerOptions().position(sydney3)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_walkicon))
                        .title(message.id));

                mMap.addMarker(new MarkerOptions().position(sydney2)
                        .title("End")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_endicon)));
            }

            if(message.status == 3 || message.status == 4){
                spoji_linije_auta(message.status,sydney3,sydney2,message.br_putnika);
            }else{
                spoji_linije(message.status,sydney3,sydney2);
            }

            mMarker.setSnippet("Vrijeme polaska: " + message.time + "\nZašto mene:" + message.razlog + "\nKontakt:"
                    + message.kontakt + "\nStart:" + message.start + "\nEnd:" + message.end+"\nbrPutnika:" + message.br_putnika);
        }
    }
    public void preuzmi_i_crtaj_voznje(){
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("location");
        mMap.clear();
        messageList.clear();
        setTitle(getString(R.string.prihvati_label));
        Log.e(TAG, "Preuzmi voznje");

            mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot child : dataSnapshot.getChildren() ){
                        Message message = child.getValue(Message.class);
                        Log.e(TAG, "Dodan: " + message.id);
                        messageList.add(message);

                        sydney2 = new LatLng(message.latitudeEnd, message.longitudeEnd);
                        sydney3 = new LatLng(message.latitudeStart, message.longitudeStart);

                        mMarker = mMap.addMarker(new MarkerOptions().position(sydney3)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_walkicon))
                                .title(message.id));

                        mMap.addMarker(new MarkerOptions().position(sydney2)
                                .title("End")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_endicon)));
                        if(message.status == 3 || message.status == 4){
                            spoji_linije_auta(message.status,sydney3,sydney2,message.br_putnika);
                        }else{
                            spoji_linije(message.status,sydney3,sydney2);
                        }
                        mMarker.setSnippet("Vrijeme polaska: " + message.time + "\nZašto mene:" + message.razlog + "\nKontakt:" +
                                            message.kontakt + "\nStart:" + message.start + "\nEnd:" + message.end + "\nbrPutnika:" + message.br_putnika);
                        Log.e(TAG, "velicina liste:" + messageList.size());
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(MapsActivity.this, "Error connecting to Database", Toast.LENGTH_SHORT).show();
                }
            });

            myDrives.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot child : dataSnapshot.getChildren() ){
                        Message message = child.getValue(Message.class);
                        Log.e(TAG, "Dodan: " + message.id);
                        messageList.add(message);

                        sydney2 = new LatLng(message.latitudeEnd, message.longitudeEnd);
                        sydney3 = new LatLng(message.latitudeStart, message.longitudeStart);

                        mMarker = mMap.addMarker(new MarkerOptions().position(sydney3)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car))
                                .title(message.id));

                        mMap.addMarker(new MarkerOptions().position(sydney2)
                                .title("End")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_endicon)));

                        spoji_linije_auta(message.status,sydney3,sydney2,message.br_putnika);
                        mMarker.setSnippet("Vrijeme polaska: " + message.time + "\nZašto mene:" + message.razlog + "\nKontakt:"
                                + message.kontakt + "\nStart:" + message.start + "\nEnd:" + message.end + "\nSlobodnih mjesta:" + message.br_putnika);
                        Log.e(TAG, "velicina liste:" + messageList.size());
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(MapsActivity.this, "Error connecting to Database", Toast.LENGTH_SHORT).show();
                }
            });
        //camera
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 14.0f));
    }
    private void spoji_linije_auta(int status, LatLng start, LatLng end, int brPutnika){
        //zauzeto
        if(brPutnika==4){
            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .add(start,end)
                    .width(5)
                    .color(Color.GREEN));
        }else if (brPutnika==3){
            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .add(start,end)
                    .width(5)
                    .color(Color.BLUE));
        }else if (brPutnika==2){
            Log.e(TAG, "Crtam Orange");
            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .add(start,end)
                    .width(5)
                    .color(MapsActivity.this.getResources().getColor(R.color.yellow)));
        } else if (brPutnika==1){
        Polyline line = mMap.addPolyline(new PolylineOptions()
                .add(start,end)
                .width(5)
                .color(MapsActivity.this.getResources().getColor(R.color.orange)));
        }else{
            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .add(start,end)
                    .width(5)
                    .color(Color.RED));
        }
    }
    private void spoji_linije(int status, LatLng start, LatLng end){
        //zauzeto
        if(status==2 || status==4){
            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .add(start,end)
                    .width(5)
                    .color(Color.RED));
        }else{
        //slobodno
            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .add(start,end)
                    .width(5)
                    .color(Color.GREEN));
        }
    }
    public void odustani_od_voznje(){
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot2) {
                for(DataSnapshot child : dataSnapshot2.getChildren() ){
                    String mUid = child.getKey();
                    Message message2 = child.getValue(Message.class);
                    Log.e(TAG, "Ovo je username:" + username);
                    Log.e(TAG, "Ovo je vozac:" +  message2.vozac);
                    if(chosenOne.latitude == message2.latitudeStart && chosenOne.longitude==message2.longitudeStart){
                        //pripaziti da ne bude prazno polje vozac jer takva provjera rusi app
                           if(message2.vozac.equals(username)){
                               Log.e(TAG, "Ovo je username:" + username);
                               Log.e(TAG, "Ovo je vozac:" +  message2.vozac);
                               Message message3 = new Message(message2.id, message2.longitudeStart, message2.latitudeStart,
                                       message2.longitudeEnd, message2.latitudeEnd, 1, "Nedredeno",
                                       message2.time, message2.kontakt, message2.razlog, message2.start, message2.end, message2.br_putnika);
                               Map<String, Object> messageValues = message3.toMap();
                               Map<String, Object> childUpdates = new HashMap<>();

                               sydney5 = new LatLng(message3.latitudeStart, message3.longitudeStart);
                               sydney4 = new LatLng(message3.latitudeEnd, message3.longitudeEnd);
                               spoji_linije(1,sydney5,sydney4);

                               childUpdates.put(mUid, messageValues);
                               myRef.updateChildren(childUpdates);
                               refresh.performClick();
                               mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(chosenOne, 14.0f));
                               Toast.makeText(MapsActivity.this, "Drive canceled", Toast.LENGTH_SHORT).show();
                           }else if (message2.vozac.equals("Nedredeno")){
                               Toast.makeText(MapsActivity.this, "You can not cancel what is not cancelable", Toast.LENGTH_SHORT).show();
                           }else{
                               Toast.makeText(MapsActivity.this, "You can not cancel one's drive", Toast.LENGTH_SHORT).show();
                           }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        myDrives.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot2) {
                for(DataSnapshot child : dataSnapshot2.getChildren() ){
                    String mUid = child.getKey();
                    Message message2 = child.getValue(Message.class);
                    Log.e(TAG, "Ovo je username:" + username);
                    Log.e(TAG, "Ovo je vozac:" +  message2.vozac);
                    if(chosenOne.latitude == message2.latitudeStart && chosenOne.longitude==message2.longitudeStart){
                        //pripaziti da ne bude prazno polje vozac jer takva provjera rusi app
                        //vozac je ustvari Putnik!!!!!!!!!!!
                        //if putnik == ja
                        if(message2.vozac.equals(username)){
                            Log.e(TAG, "Ovo je username:" + username);
                            Log.e(TAG, "Ovo je vozac:" +  message2.vozac);
                            SharedPreferences prefs = getSharedPreferences("SPrefbrojPutnika", MODE_PRIVATE);
                            SharedPreferences.Editor myeditor = prefs.edit();
                            Integer restoredText = prefs.getInt("brojPutnika", 0);
                            int bPutnika=0;
                            if (restoredText != null) {
                                bPutnika = prefs.getInt("brojPutnika", 0); //0 is the default value.
                                //pobrisi da ne moze vise "dodavati mjesta"
                                myeditor.clear();
                                myeditor.apply();
                            }
                            Message message3 = new Message(message2.id, message2.longitudeStart, message2.latitudeStart,
                                    message2.longitudeEnd, message2.latitudeEnd, 3, "Neodredeno",
                                    message2.time, message2.kontakt, message2.razlog, message2.start, message2.end, message2.br_putnika+bPutnika);
                            Map<String, Object> messageValues = message3.toMap();
                            Map<String, Object> childUpdates = new HashMap<>();

                            sydney5 = new LatLng(message3.latitudeStart, message3.longitudeStart);
                            sydney4 = new LatLng(message3.latitudeEnd, message3.longitudeEnd);
                            spoji_linije_auta(message3.status,sydney5,sydney4,message2.br_putnika);

                            childUpdates.put(mUid, messageValues);
                            myDrives.updateChildren(childUpdates);
                            refresh.performClick();
                            if(bPutnika==0){
                                Toast.makeText(MapsActivity.this, "You can not cancel what is not cancelable", Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(MapsActivity.this, "Drive canceled", Toast.LENGTH_SHORT).show();
                            }
                        }else if (message2.br_putnika==4){
                            Toast.makeText(MapsActivity.this, "You can not cancel what is not cancelable", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(MapsActivity.this, "You can not cancel one's drive", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    public boolean provjeri_jel_to_auto(){
        Log.e(TAG, "velicina liste:" + messageList.size());
        for(int i=0; i<messageList.size();i++){
            Message message;
            message = messageList.get(i);
                if(chosenOne.latitude == message.latitudeStart && chosenOne.longitude == message.longitudeStart && (message.status==4 || message.status==3)){
                        if (message.status == 4){
                            Toast.makeText(MapsActivity.this, "This drive is already taken", Toast.LENGTH_SHORT).show();
                            return false;
                        }else{
                            return true;
                        }
                }else{
                }
        }
    return false;
    }
    //prihvaćavanje vožnje ili putnika
    public void nadi_chosen(){
        //ako vozac prihvaća putnika
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot child : dataSnapshot.getChildren() ){
                        // Do magic here
                        String mUid = child.getKey();
                        //Log.e(TAG, "Ovo je uid:" +  mUid);
                        Message message2 = child.getValue(Message.class);
                        Log.e(TAG, "Ovdje sam u nadi chosen");
                        Log.e(TAG, "lat start: "+ message2.latitudeStart);
                        if(chosenOne.latitude == message2.latitudeStart && chosenOne.longitude == message2.longitudeStart){
                            Log.e(TAG, "Ovdje sam isto "+ message2.id);
                            Log.e(TAG, "lat start: "+ message2.latitudeStart);
                            if (message2.status == 2){
                                Toast.makeText(MapsActivity.this, "This drive is already full", Toast.LENGTH_SHORT).show();
                            }else {
                                Message message3 = new Message(message2.id, message2.longitudeStart, message2.latitudeStart,
                                        message2.longitudeEnd, message2.latitudeEnd, 2, username, message2.time,
                                        message2.kontakt, message2.razlog, message2.start, message2.end, message2.br_putnika);
                                Map<String, Object> messageValues = message3.toMap();
                                Map<String, Object> childUpdates = new HashMap<>();
                                Log.e(TAG, "Ovdje sam, status: " + message3.status);
                                childUpdates.put(mUid, messageValues);
                                myRef.updateChildren(childUpdates);

                                sydney5 = new LatLng(message3.latitudeStart, message3.longitudeStart);
                                sydney4 = new LatLng(message3.latitudeEnd, message3.longitudeEnd);
                                spoji_linije(2,sydney5,sydney4);
                                Toast.makeText(MapsActivity.this, "Vožnja prijavljena", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(MapsActivity.this, "Greška pri povezivanju s bazom", Toast.LENGTH_SHORT).show();
                }
            });
        //ako putnik prihvaća vozača
        myDrives.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot){
                    Log.e(TAG, "nadi chosen drives");
                    for(DataSnapshot child : dataSnapshot.getChildren() ){
                        // Do magic here
                        uidVozaca = child.getKey();
                        //ne izvrsava se kako treba
//                        spremiPutnika();
                        Message message2 = child.getValue(Message.class);
                        if(chosenOne.latitude == message2.latitudeStart && chosenOne.longitude == message2.longitudeStart){
                            Log.e(TAG, "nadi chosen: key:" + uidVozaca);
                            if (message2.status == 4){
                                Toast.makeText(MapsActivity.this, "This drive is already full", Toast.LENGTH_SHORT).show();
                            }else {
                                int broj=message2.br_putnika;
                                broj=broj-upisaniPutnici;
                                if(broj<0){
                                    broj=broj+upisaniPutnici;
                                    Toast.makeText(MapsActivity.this, "Not enough room", Toast.LENGTH_SHORT).show();
                                }else{
                                int status = 3;
                                if(broj==0) status=4;
                                String putnik;
//                                putnik = dodaj_putnika(message2.vozac,status);
                                Message message3 = new Message(message2.id, message2.longitudeStart, message2.latitudeStart,
                                        message2.longitudeEnd, message2.latitudeEnd, status, username, message2.time,
                                        message2.kontakt, message2.razlog, message2.start, message2.end, broj);
                                Map<String, Object> messageValues = message3.toMap();
                                Map<String, Object> childUpdates = new HashMap<>();
                                childUpdates.put(uidVozaca, messageValues);
                                myDrives.updateChildren(childUpdates);
                                sydney5 = new LatLng(message3.latitudeStart, message3.longitudeStart);
                                sydney4 = new LatLng(message3.latitudeEnd, message3.longitudeEnd);
                                spoji_linije_auta(message3.status,sydney5,sydney4,message3.br_putnika);
                                Toast.makeText(MapsActivity.this, "Vožnja prijavljena", Toast.LENGTH_SHORT).show();
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(chosenOne, 14.0f));
                                refresh.performClick();
                                }
                            }
                        }else{
                            //
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(MapsActivity.this, "Greška pri povezivanju s bazom", Toast.LENGTH_LONG).show();
                }
            });

    }
    private void spremiPutnika(){
        myPass = FirebaseDatabase.getInstance().getReference().child("passenger");
        myPass.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child : dataSnapshot.getChildren() ){
                    String uid = child.getKey();
                    Log.e(TAG, "SPREMI PUTNIKA-uid-" + uid);
                    Log.e(TAG, "SPREMI PUTNIKA-uidVozaca-" + uidVozaca);
//                    Integer username = dataSnapshot.child(uid).child("statis").getValue(Integer.class);
                    Log.e(TAG, "SPREMI PUTNIKA-username " + username);
//                    Ref = FirebaseDatabase.getInstance().getReference().child("passenger").child(uidVozaca).child("statis");
                    if(uid==uidVozaca){
//                        myPass.child(uidVozaca).child("statis").setValue("230");
                        myPass.child(uidVozaca).child("statis").setValue("KAKICA");
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }
    private String dodaj_putnika(String p, int status){
        String rez;
        if(p != null && !p.isEmpty()){
            rez = p + username + "-";
        }else if(status==4){
            rez = username;
        }else{
            rez = username + "-";
        }
        Log.e(TAG, "rez");
        return rez;
    }
    private String oduzmi_putnika(String p){
        String rez="";
        String[] nPolje = new String[p.length()];
        Integer izbacen=0;
        String polje[]= p.split("-");

        for (int i=0; i<polje.length;i++){
            Log.e(TAG, polje[i]);
        }
        for (int i=0; i<polje.length;i++){
            if(polje[i].equals(username)){
                izbacen=i;
            }
        }
        Log.e(TAG, String.valueOf(izbacen));
        for (int i=0; i<polje.length;i++){
            if(i==izbacen){
                nPolje[i]="1";
            }else {
                nPolje[i]=polje[i];
            }
        }
        for (int i=0; i<nPolje.length;i++){
            if(i==nPolje.length-1){
                rez=rez+nPolje[i];
            }else{
                rez=rez+nPolje[i]+"-";
            }
        }
        Log.e(TAG, rez);
        return rez;
    }

    private boolean check_login_status(){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(SaveSharedPreference.getUserName(MapsActivity.this).length() == 0){
                // call Login Activity
            Log.e(TAG, "Username login status FALSE " + username);
            sendToWelcomePage();
            return false;
        }else{
            uid = currentUser.getUid();
            username=SaveSharedPreference.getUserName(MapsActivity.this);
            TextView tekst = findViewById(R.id.textView);
            tekst.setText("> "+ username);
                // Stay at the current activity.
            Log.e(TAG, "Username login status TRUE " + username);
            return true;
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        final ProgressDialog prijavaProgress;
        prijavaProgress = new ProgressDialog(this);
        TextView tekst = findViewById(R.id.textView);
        tekst.setText("> " + username);
        prijavi = findViewById(R.id.prijavi);
        obrisi = findViewById(R.id.obrisi);
        refresh = findViewById(R.id.refresh);
        rute=findViewById(R.id.rute);
        mMap=googleMap;
        //button radi na pocetku
        prijavi.setEnabled(true);
        obrisi.setEnabled(true);
        prijavi = findViewById(R.id.prijavi);
        obrisi = findViewById(R.id.obrisi);
        refresh = findViewById(R.id.refresh);

        //ako se brzo klikne izmedu vozaca i putnika, iscrtaju se rute putnika (sto nije toliko loše)
        moja_voznja(1);
        rute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(stisnut){
                    nacrtaj_driving_rute();
                }else{
                    nacrtaj_voznje();
                }
                stisnut=!stisnut;
            }
        });
        prijavi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //onaj koji zatrazuje
                if (!voznja){
                    //prvo treba upisati vrijeme nalaska, idemo sad
                    Intent prijava = new Intent(MapsActivity.this,PrijavaVoznjeActivity.class);
                    prijava.putExtra("mS_lon", mojStartLon);
                    prijava.putExtra("mS_lat", mojStartLat);
                    prijava.putExtra("mE_lon", mojEndLon);
                    prijava.putExtra("mE_lat", mojEndLat);
                    prijava.putExtra("username", username);
                    prijava.putExtra("uloga", uloga);

                    prijavaProgress.setTitle("Sending in");
                    prijavaProgress.setMessage("Please wait while we send your data");
                    prijavaProgress.show();
                    prijavaProgress.setCanceledOnTouchOutside(false);

                    prijava.putExtra("start", aStart.getText());
                    prijava.putExtra("end", aEnd.getText());
                    startActivity(prijava);
                    prijavaProgress.dismiss();
                }else{
                    //ako vozac klikne na info snippet:
                    if(klikNaInfo){
                        //je li klikno na voznju
                        if(provjeri_jel_to_auto()){
                            final EditText putniciET = new EditText(MapsActivity.this);
                            //samo brojevi
                            putniciET.setInputType(InputType.TYPE_CLASS_NUMBER);
                            putniciET.setText("1");
                            putniciET.setGravity(Gravity.CENTER);

                            AlertDialog.Builder alert = new AlertDialog.Builder(MapsActivity.this);
                            alert.setTitle("Odaberi broj putnika");
                            alert.setMessage("Pazi jesi li osvježio stanje!");
                            alert.setView(putniciET);
                            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    upisaniPutnici = Integer.parseInt(putniciET.getText().toString());
                                    SharedPreferences.Editor editor = getSharedPreferences("SPrefbrojPutnika", MODE_PRIVATE).edit();
                                    editor.putInt("brojPutnika", upisaniPutnici);
                                    editor.putString("chosenLat",Double.toString(chosenOne.latitude));
                                    editor.putString("chosenLon",Double.toString(chosenOne.longitude));
                                    editor.apply();
                                    nadi_chosen();
                                }
                            }).setNegativeButton("Odustani", null).show();
                        }else{
                            //jako bitno 1:11@A.M.
                            nadi_chosen();
                        }
                    }else{
                        Toast.makeText(MapsActivity.this, "Please click on Start position", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        obrisi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!voznja){
                    //prvo treba upisati vrijeme nalaska
                    otkazi_moju_voznju(uloga);
                }else{
                    //ako vozac klikne:
                    odustani_od_voznje();
                }
            }
        });
        //vidljivo samo vozacu
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preuzmi_i_crtaj_voznje();
            }
        });
        //googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 14.0f));
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(LatLng poz) {
                if(voznja) {
                    //ponisti odabir na klik sa strane KOD VOZACA
                    TextView tekst = findViewById(R.id.textView);
                    tekst.setText("odabran: ");
                    prijavi.setEnabled(false);
                    obrisi.setEnabled(false);
                    klikNaInfo = false;
                }
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                return false;
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(final Marker mark) {
                if(!voznja){
                    // putniku ne treba nista zeleno
                }else {
                    if(!mark.getTitle().equals("End")){
                        TextView tekst = findViewById(R.id.textView);
                        tekst.setText("odabran: " + mark.getTitle());
                        //zapamti na koji je klikno
                        chosenOne=new LatLng(mark.getPosition().latitude, mark.getPosition().longitude);
                        Log.e(TAG,String.valueOf(mark.getPosition().longitude));
                        Log.e(TAG,"Uloga:" + uloga);
                        klikNaInfo=true;
                        prijavi.setEnabled(true);
                        obrisi.setEnabled(true);
                        //ako je auto ponudi alert dialog
                      if(provjeri_jel_to_auto()){
//                            final EditText putniciET = new EditText(MapsActivity.this);
//                            //samo brojevi
//                            putniciET.setInputType(InputType.TYPE_CLASS_NUMBER);
//                            putniciET.setText("1");
//                            AlertDialog.Builder alert = new AlertDialog.Builder(MapsActivity.this);
//                            alert.setTitle("Odaberi broj putnika");
//                            alert.setMessage("Pazi jesi li osvježio stanje! Ako želiš otkazati vožnju, pritisni odustani, a nakon toga Otkaži vožnju");
//                            alert.setView(putniciET);
//                            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    upisaniPutnici = Integer.parseInt(putniciET.getText().toString());
//                                    SharedPreferences.Editor editor = getSharedPreferences("SPrefbrojPutnika", MODE_PRIVATE).edit();
//                                    editor.putInt("brojPutnika", upisaniPutnici);
//                                    editor.putString("chosenLat",Double.toString(chosenOne.latitude));
//                                    editor.putString("chosenLon",Double.toString(chosenOne.longitude));
//                                    editor.apply();
//                                    prijavi.performClick();
//                                }
//                            }).setNegativeButton("Odustani", null).show();
                        }
                    }else{
                        klikNaInfo=false;
                    }
                }
            }
        });

        // Prilagodjeni 'info window' za marker (omogucava snippet teksta kroz vise redaka)
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }
            @Override
            public View getInfoContents(Marker marker) {
                //Log.e(TAG, "ULAZ 2");
                LinearLayout info = new LinearLayout(MapsActivity.this);
                info.setOrientation(LinearLayout.VERTICAL);
                // Title:
                TextView title = new TextView(MapsActivity.this);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());
                // Snippet:
                info.addView(title);
                if(voznja){
                    if(marker.getTitle().equals("End")){
                    }else{
                    TextView snippet = new TextView(MapsActivity.this);
                    snippet.setTextColor(Color.GRAY);
                    snippet.setText(marker.getSnippet());
                    info.addView(snippet);

                    TextView odabran = new TextView(MapsActivity.this);
                    odabran.setTextColor(Color.GREEN);
                    odabran.setGravity(Gravity.CENTER);
                    odabran.setText("KLIKNI ZA ODABIR");
                    info.addView(odabran);
                    }
                }
                return info;
            }
        });

    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.w("location address", strReturnedAddress.toString());
            } else {
                Log.w("location address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("loction address", "Canont get Address!");
        }
        return strAdd;
    }
    private String samo_ulica(String string){
        String ulica="";
        String[] namesList = string.split(",");
        ulica = namesList[0];
        return ulica;
    }
    // Stvaranje Directions API url-a na temljeu dvije geo-lokacije
    // (od 'origin' do 'dest'):
    private String getDirectionsUrl(LatLng origin, LatLng dest){
        // Origin:
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination:
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode of Transport => WALKING!
        // napomena: defaults je "driving"!
        String transportMode = "mode=driving";
        // Sensor
        String sensor = "sensor=false";
        // API key
        // String key = "key=" + getString(R.string.google_maps_key);
        // Parametri zahtjeva za web uslugu:
        String parameters = str_origin + "&" + str_dest + "&" + transportMode + "&" + sensor;
        // Output format:
        String output = "json";
        // Rezultantni URL:
        String result_url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
       // System.out.println("***** " + result_url);
        return result_url;
    }
    // Eksplicitno pitanje (korisniku) za dozvolu koristenja Location usluge:
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Prikaz dijaloga korisniku, u asinkronom (ne-blokirajucem) nacinu:
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission," +
                                " please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Prikazi UI za upravljanje dozvolama (nakon pomocnog dijaloga):
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();
            } else {
                // Prikazi UI za upravljanje dozvolama (bez pomocnog dijaloga):
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }
    // Handle za korisnikov eksplicitni odgovr oko dozvole za koristenje Location usluga:
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // App ima dozvolu
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        //get_last_location();
                        if (mMap != null) {
                            mMap.setMyLocationEnabled(true);
                        }
                    }
                } else {

                    // App nema dozvolu;
                    // (treba blokirati sve funkcije koje zahtijevaju tu dozvolu)
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////
    // Asinkroni zadatak za dohvat informacija s Directions API-a:
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Dohvat podataka u zasebnoj dretvi (non-UI):
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                // Dohvat podataka s prethodno pripremljenog url-a:
                data = downloadUrl(url[0]);
            } catch(Exception e) {
                // error
            }
            return data;
        }

        // Obavlja se u UI dretvi, nakon sto zavrsi asinkroni zadatak dohvata podataka:
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
//            System.out.println("***** " + result);

            // Opet zadavanje asinkronog zadatka, ovog puta za parsiranje JSON ruta:
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }
    // Konkretni zadatak koji se obavlja u izdvojenoj (non-UI) dretvi:
    // dohvat podataka sa Directions API-a (niz instrukcija kako
    // pjeske doci od lokacije A do lokacije B) - u JSON formatu.
    // Koristi se OKHTTP.
    private String downloadUrl(String strUrl) {
        String data;
        try {
            URL url = new URL(strUrl);
            final OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            Response response = client.newCall(request).execute();
            data = response.body().string();
        } catch (Exception ex) {
            data = "";
        }
        return data;    // JSON s rutama / direkcijama
    }
    // Asinkroni zadatak za parsiranje JSON ruta (direkcija dobivenih od Directions API-a):
    private class ParserTask extends AsyncTask<String, Integer, jsonParsedData>
    {
        // Parsiranje podataka u pozadinskoj dretvi:
        @Override
        protected jsonParsedData doInBackground(String... jsonData) {

            JSONObject jObject;
            //List<List<HashMap<String, String>>> routes = null;
            //String[] walkData = new String[2];
            List<List<HashMap<String, String>>> routes;
            String[] walkData;
            jsonParsedData allData = null;

            try {
                jObject = new JSONObject(jsonData[0]);

                // Inicijalizacija parsera:
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Parsiranje jsona:
                routes = parser.parse(jObject);

                // Izvlacenje podataka o estimiranoj udaljenosti i vremenu hoda:
                walkData = parser.decodeWalkingDurationAndWalkingDistance(jObject);

                // Objedinjeni podaci:
                allData = new jsonParsedData(routes, walkData);
            } catch(Exception e) {
                // e.printStackTrace();
            }
            return allData;
        }

        // Izvrsava se u UI dretvi, nakon sto se parsira JSON i izvuku sve potrebne informacije.
        // Liste tocaka koriste se za iscrtavanje rute na mapi, a podaci o udaljenosti
        // i vremnu trajanja hoda prikazat ce se u info windowu markera:
        @Override
        protected void onPostExecute(jsonParsedData completeResult) {
            ArrayList<LatLng> points = new ArrayList<LatLng>();;
            PolylineOptions lineOptions = new PolylineOptions();;
            lineOptions.width(3);
            lineOptions.color(Color.BLACK);
//            if(uCrveno){
//                lineOptions.color(Color.RED);
//            }else{
//                lineOptions.color(Color.GREEN);
//            }

            List<List<HashMap<String, String>>> result = completeResult.get_routeToDraw();
            String[] infoWalking = completeResult.get_walkingData();

            // Prolazak kroz cijelu rutu:
            for(int i=0; i < result.size(); i++)
            {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Dohvat i-tog (u ovom slucaju - jedinog) dijela rute:
                List<HashMap<String, String>> path = result.get(i);

                // Dohvat svih tocaka u i-tom dijelu rute:
                for(int j=0; j < path.size(); j++)
                {
                    HashMap<String,String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }

                // Dodavanje svih tocaka rute u LineOptions objekt:
                lineOptions.addAll(points);
                lineOptions.width(5);
                lineOptions.color(Color.DKGRAY);
//                if(uCrveno){
//                    lineOptions.color(Color.RED);
//                }else{
//                    lineOptions.color(Color.GREEN);
//                }
            }

            // Vizualizacija rute na google mapi:
            mMap.addPolyline(lineOptions);

            uCrveno=false;
            // Vizualizacija informacija o ruti u info windowu markera:
            /*mMarker.setSnippet("Distance: " + infoWalking[0] + "\n" +
                    "Duration: " + infoWalking[1]);
            mMarker.showInfoWindow();*/
        }
    }
    // Razred koji sadrzi parsirane podatke iz Directions API odgovora.
    // Pri tome, dva su glavna dijela objedinjenih informacija:
    // (1) Lista listi tocaka -> sluzi za iscrtavanje rute na Google mapi
    // (2) Podaci o hodu rutom (udaljenost i procijenjeno trajanje hodanja)
    private class jsonParsedData
    {
        List<List<HashMap<String,String>>> routeToDraw;
        String[] walkingData;

        // konstruktor:
        private jsonParsedData(List<List<HashMap<String,String>>> routeToDraw, String[] walkingData)
        {
            this.routeToDraw = routeToDraw;
            this.walkingData = walkingData;
        }

        // getteri:
        public List<List<HashMap<String,String>>> get_routeToDraw(){
            return this.routeToDraw;
        }

        public String[] get_walkingData(){
            return this.walkingData;
        }
    }
}
