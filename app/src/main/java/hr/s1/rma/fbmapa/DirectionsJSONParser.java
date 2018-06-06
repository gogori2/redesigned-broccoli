package hr.s1.rma.fbmapa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.android.gms.maps.model.LatLng;


public class DirectionsJSONParser {

    // Prima JSONObject, a vraca listu lista koje sadrze (Lat, Lon) podatke
    public List<List<HashMap<String,String>>> parse(JSONObject jObject){

        List<List<HashMap<String, String>>> routes = new ArrayList<>();
        JSONArray jRoutes;
        JSONArray jLegs;
        JSONArray jSteps;

        try {
            jRoutes = jObject.getJSONArray("routes");

            // Buduci da se ne koriste 'waypoint'-ovi u zahtjevu,
            // ruta se sastoji od samo jednog 'leg'-a. Taj jedan 'leg' sadrzi
            // 'step'-ove - atomarne jedinice na ruti:
            jLegs = ((JSONObject)jRoutes.get(0)).getJSONArray("legs");
            jSteps = ((JSONObject)jLegs.get(0)).getJSONArray("steps");
            List<HashMap<String, String>> path = new ArrayList<>();

            // Prolazak kroz sve steps-cvorove za leg/rutu:
            // Pojedini 'step', izmedju ostaloga, sadrzi:
            // start_location, end_location, polyline, duration, html_instructions...
            for(int k=0; k < jSteps.length(); k++)
            {
                // dohvat polyline podatka za svaki step:
                String polyline;
                polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
                // dekodiranje polyline:
                List<LatLng> list = decodePoly(polyline);

                // Prolazak kroz cijeli polyline: izdvajanje tocaka (lat, lon) i dodavanje u path:
                for(int l=0; l < list.size(); l++)
                {
                    HashMap<String, String> hm = new HashMap<>();
                    hm.put("lat", Double.toString((list.get(l)).latitude));
                    hm.put("lng", Double.toString((list.get(l)).longitude));
                    path.add(hm);
                }
            }

            routes.add(path);

            // Kada bi Directions API vratio alternativne rute, onda je ovako moguce
            // proci kroz sve njih:
            /*
            for(int i=0; i < jRoutes.length(); i++) {
                jLegs = ((JSONObject)jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<HashMap<String, String>>();

                // Traversing all legs:
                for(int j=0; j < jLegs.length(); j++){
                    jSteps = ((JSONObject)jLegs.get(j)).getJSONArray("steps");

                    // Traversing all steps:
                    for(int k=0; k < jSteps.length(); k++){
                        String polyline = "";
                        polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
                        List<LatLng> list = decodePoly(polyline);

                        // Traversing all points:
                        for(int l=0;l<list.size();l++){
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("lat", Double.toString(((LatLng)list.get(l)).latitude) );
                            hm.put("lng", Double.toString(((LatLng)list.get(l)).longitude) );
                            path.add(hm);
                        }
                    }
                    routes.add(path);
                }
            }
            */

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return routes;
    }


    // Dekodiranje polyline tocaka.
    // Courtesy of:
    // http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }


    // Podaci o ukupnoj udaljenosti i vremenu trajanja hoda mogu se dobiti
    // "kumulativno" u parse metodi, medjutim moze i na ovaj, "cisci" nacin:
    public String[] decodeWalkingDurationAndWalkingDistance(JSONObject jObject){
        JSONArray jRoutes;
        JSONArray jLegs;
        String[] myStringArray = new String[2];

        try {
            // API response: 1 ruta, 1 leg
            // Estimated walking duration and walking distance ==> u leg informaciji!
            jRoutes = jObject.getJSONArray("routes");
            jLegs = ((JSONObject)jRoutes.get(0)).getJSONArray("legs");
            myStringArray[0] = (String)((JSONObject)((JSONObject)jLegs.get(0)).get("distance")).get("text");
            myStringArray[1] = (String)((JSONObject)((JSONObject)jLegs.get(0)).get("duration")).get("text");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return myStringArray;
    }

}
