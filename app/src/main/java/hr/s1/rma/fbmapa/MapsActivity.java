package hr.s1.rma.fbmapa;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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
    DatabaseReference myRef = database.getReference("location");
    private DatabaseReference mdatabase, mUserDatabase;

    public boolean voznja = false,prviPut=true;
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
    public int j = 0, i = 0;
    Button refresh, prijavi, obrisi;
    Marker mMarker;
    static double lats, lons, late, lone, mojStartLat, mojStartLon, mojEndLat, mojEndLon;
    private GoogleMap mMap;
    private static final String TAG = "*";
    private ArrayList<Message> messageList = new ArrayList<Message>();
    private ArrayList<Marker> mMarkerArray = new ArrayList<Marker>();
    private FirebaseAuth mAuth;
    private String username, uid;
    private TextView aStart,aEnd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("location");
//        uid = currentUser.getUid();
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
            case R.id.vozac:
                voznja = true;
                //pokazi refresh butt
                refresh.setVisibility(View.VISIBLE);
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
            case R.id.putnik:
                voznja=false;
                prijavi.setEnabled(true);obrisi.setEnabled(true);
                prijavi.setText(this.getResources().getString(R.string.zatrazi_voznju));
                obrisi.setText(this.getResources().getString(R.string.obrisi_voznju));
                refresh.setVisibility(View.GONE);
                aStart.setVisibility(View.VISIBLE);
                aEnd.setVisibility(View.VISIBLE);
                moja_voznja();
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
    public void moja_voznja() {
        mMap.clear();
        Log.e(TAG, "moja voznja");
        //da ne rusi prazan string
        aStart = findViewById(R.id.textStart);
        aEnd = findViewById(R.id.textEnd);
        if(aStart.getText().equals(null) && aEnd.getText().equals(null)){
            aStart.setText("Start");
            aEnd.setText("End");
        }

        final Marker markStart2 = mMap.addMarker(new MarkerOptions()
                .position(mojStart)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .draggable(true)
                .title("Start"));
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

    public void otkazi_moju_voznju(){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid();

        myRef.child(uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(MapsActivity.this, "Successfully canceled", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void nacrtaj_voznje(){
        mMap.clear();
        Log.e(TAG, "velicina liste:" + messageList.size());
        for(int i=0; i<messageList.size();i++){

            Message message;
            message = messageList.get(i);
            sydney2 = new LatLng(message.latitudeEnd, message.longitudeEnd);
            sydney3 = new LatLng(message.latitudeStart, message.longitudeStart);

            mMarker = mMap.addMarker(new MarkerOptions().position(sydney3)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .title("Korisnik " + message.id));

            mMap.addMarker(new MarkerOptions().position(sydney2)
                    .title("End")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_endicon)));

            spoji_linije(message.status);

            mMarker.setSnippet("Vrijeme polaska: " + message.time + "\nZašto mene:" + message.razlog + "\nKontakt:" + message.kontakt + "\nStart:" + message.start + "\nEnd:" + message.end);
            //String url = getDirectionsUrl(sydney2, sydney3);
            //DownloadTask downloadTask = new DownloadTask();
            //downloadTask.execute(url);
        }
    }
    public void preuzmi_i_crtaj_voznje(){
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("location");
        mMap.clear();
        messageList.clear();
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
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            .title("Korisnik " + message.id));

                    mMap.addMarker(new MarkerOptions().position(sydney2)
                            .title("End")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_endicon)));

                    spoji_linije(message.status);
                    mMarker.setSnippet("Vrijeme polaska: " + message.time + "\nZašto mene:" + message.razlog + "\nKontakt:" + message.kontakt + "\nStart:" + message.start + "\nEnd:" + message.end);
                    //String url = getDirectionsUrl(sydney2, sydney3);
                    //DownloadTask downloadTask = new DownloadTask();
                    //downloadTask.execute(url);
                    Log.e(TAG, "velicina liste:" + messageList.size());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MapsActivity.this, "Error connecting to Database", Toast.LENGTH_SHORT).show();
            }
        });
        //camera
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 12.0f));
    }

    private void spoji_linije(int status){
        if(status==2){
            uCrveno=true;
            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .add(sydney2,sydney3)
                    .width(5)
                    .color(Color.RED));
        }else{
            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .add(sydney2,sydney3)
                    .width(5)
                    .color(Color.GREEN));
        }
    }
    public void otkazi_prihvacenu_voznju(){
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
                                       message2.longitudeEnd, message2.latitudeEnd, 1, "ODUSTAO " + message2.vozac,
                                       message2.time, message2.kontakt, message2.razlog, message2.start, message2.end);
                               Map<String, Object> messageValues = message3.toMap();
                               Map<String, Object> childUpdates = new HashMap<>();

                               sydney4 = new LatLng(message3.latitudeEnd, message3.longitudeEnd);
                               sydney5 = new LatLng(message3.latitudeStart, message3.longitudeStart);

                               uCrveno=false;
     //                        String url = getDirectionsUrl(sydney4, sydney5);
     //                        // Dohvat json podataka s Google Directions API-a:
     //                        DownloadTask downloadTask = new DownloadTask();
//                             downloadTask.execute(url);
                               Polyline line = mMap.addPolyline(new PolylineOptions()
                                       .add(sydney4,sydney5)
                                       .width(5)
                                       .color(Color.GREEN));
                               childUpdates.put(mUid, messageValues);
                               myRef.updateChildren(childUpdates);
                               Toast.makeText(MapsActivity.this, "Drive canceled", Toast.LENGTH_SHORT).show();
                           }else if (message2.vozac.equals("ODUSTAO "+username)){
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

    public void nadi_chosen(){
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child : dataSnapshot.getChildren() ){
                    // Do magic here
                    String mUid = child.getKey();
                    Log.e(TAG, "Ovo je uid:" +  mUid);
                    Message message2 = child.getValue(Message.class);
                    Log.e(TAG, "Ovdje sam u nadi chosen");
                    if(chosenOne.latitude == message2.latitudeStart && chosenOne.longitude == message2.longitudeStart){
                            Log.e(TAG, "Ovdje sam isto "+ message2.id);
                            if (message2.status == 2){
                                    Toast.makeText(MapsActivity.this, "This drive is already taken", Toast.LENGTH_SHORT).show();
                            }else {
                                    Message message3 = new Message(message2.id, message2.longitudeStart, message2.latitudeStart,
                                                                   message2.longitudeEnd, message2.latitudeEnd, 2, username, message2.time,
                                                                   message2.kontakt, message2.razlog,message2.start, message2.end);
                                    Map<String, Object> messageValues = message3.toMap();
                                    Map<String, Object> childUpdates = new HashMap<>();
                                    Log.e(TAG, "Ovdje sam, status: " + message3.status);
                                    childUpdates.put(mUid, messageValues);
                                    myRef.updateChildren(childUpdates);
                                    sydney4 = new LatLng(message3.latitudeEnd, message3.longitudeEnd);
                                    sydney5 = new LatLng(message3.latitudeStart, message3.longitudeStart);
                                    // odabranu zacrveni
                                    Polyline line = mMap.addPolyline(new PolylineOptions()
                                            .add(sydney4, sydney5)
                                            .width(5)
                                            .color(Color.RED));
                                    uCrveno = true;
                                    // Izgradnja url-a za Directions API:
        //                        String url = getDirectionsUrl(sydney4, sydney5);
        //                        // Dohvat json podataka s Google Directions API-a:
        //                        DownloadTask downloadTask = new DownloadTask();
        //                        downloadTask.execute(url);
                                Toast.makeText(MapsActivity.this, "Voznja prijavljena", Toast.LENGTH_SHORT).show();
                        }
                    }else{

                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Toast.makeText(this, "Voznja prijavljena", Toast.LENGTH_SHORT).show();
            }
        });
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
            tekst.setText("User "+ username);
                // Stay at the current activity.
            Log.e(TAG, "Username login status TRUE " + username);
            return true;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                Log.e(TAG, "RESULT OK");
                //preuzmi_i_nacrtaj_voznje
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
                Log.e(TAG, "RESULT CANCELED");
                //preuzmi_i_nacrtaj_voznje
            }
        }
    }//onActivityResult
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        final ProgressDialog prijavaProgress;
        prijavaProgress = new ProgressDialog(this);

        TextView tekst = findViewById(R.id.textView);
        tekst.setText("User "+ username);
        prijavi = findViewById(R.id.prijavi);
        obrisi = findViewById(R.id.obrisi);
        refresh = findViewById(R.id.refresh);
        mMap=googleMap;
        //button radi na pocetku
        prijavi.setEnabled(true);
        obrisi.setEnabled(true);
        prijavi = findViewById(R.id.prijavi);
        obrisi = findViewById(R.id.obrisi);
        refresh = findViewById(R.id.refresh);
        prijavi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!voznja){
                    //prvo treba upisati vrijeme nalaska, idemo sad
                    Intent prijava = new Intent(MapsActivity.this,PrijavaVoznjeActivity.class);
                    prijava.putExtra("mS_lon", mojStartLon);
                    prijava.putExtra("mS_lat", mojStartLat);
                    prijava.putExtra("mE_lon", mojEndLon);
                    prijava.putExtra("mE_lat", mojEndLat);
                    prijava.putExtra("username", username);

                    prijavaProgress.setTitle("Sending in");
                    prijavaProgress.setMessage("Please wait while we send your data");
                    prijavaProgress.show();
                    prijavaProgress.setCanceledOnTouchOutside(false);

                    prijava.putExtra("start", aStart.getText());
                    prijava.putExtra("end", aEnd.getText());
                    startActivity(prijava);
                    prijavaProgress.dismiss();
                }else{
                    //ako vozac klikne:
                    if(klikNaInfo){
                        nadi_chosen();
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
                    otkazi_moju_voznju();
                }else{
                    //ako vozac klikne:
                    otkazi_prihvacenu_voznju();
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
                        klikNaInfo=true;
                        prijavi.setEnabled(true);
                        obrisi.setEnabled(true);
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
                    odabran.setText("ODABRANO");
                    info.addView(odabran);
                    }
                }
                return info;
            }
        });
        //ako se brzo klikne izmedu vozaca i putnika, iscrtaju se rute putnika (sto ne zelimo)
        moja_voznja();
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
        String transportMode = "mode=walking";
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
        System.out.println("***** " + result_url);
        return result_url;
    }
/*
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
    }*/
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
            System.out.println("***** " + result);

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
            lineOptions.width(2);
            if(!uCrveno){
                lineOptions.color(Color.RED);
            }else{
                lineOptions.color(Color.GREEN);
            }

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
                if(uCrveno){
                    lineOptions.color(Color.RED);
                }else{
                    lineOptions.color(Color.GREEN);
                }
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
