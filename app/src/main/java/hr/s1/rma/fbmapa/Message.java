package hr.s1.rma.fbmapa;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Message {

    public String id;
    public double longitudeEnd;
    public double latitudeEnd;
    public double longitudeStart;
    public double latitudeStart;
    public int status;
    public String vozac;
    public String time;
    public String kontakt;
    public String razlog;
    public String start;
    public String end;
    public Message() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Message(String id, double longitude, double latitude, double longitude2,
                   double latitude2, int status, String vozac, String time, String kontakt,
                   String razlog, String start, String end) {
        this.id = id;
        this.longitudeStart = longitude;
        this.latitudeStart = latitude;
        this.longitudeEnd = longitude2;
        this.latitudeEnd = latitude2;
        this.status = status;
        this.vozac = vozac;
        this.time = time;
        this.kontakt = kontakt;
        this.razlog = razlog;
        this.start = start;
        this.end = end;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("longitudeEnd", longitudeEnd);
        result.put("latitudeEnd",latitudeEnd);
        result.put("longitudeStart", longitudeStart);
        result.put("latitudeStart", latitudeStart);
        result.put("status", status);
        result.put("vozac", vozac);
        result.put("time", time);
        result.put("kontakt", kontakt);
        result.put("razlog", razlog);
        result.put("start", start);
        result.put("end", end);
        return result;
    }
}