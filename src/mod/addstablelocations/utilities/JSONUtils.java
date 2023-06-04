package mod.addstablelocations.utilities;

import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.Global;


// JSON Utilities to prevent writing JSONException everywhere
public class JSONUtils {
    public static final String MOD_PREFIX = "CreateStableLocations";

    public static JSONObject getModConfig() {
        try {
            return Global.getSettings().getJSONObject(MOD_PREFIX);
        } catch (Exception e){
            throw new RuntimeException("Fail to load " + MOD_PREFIX);
        }
    }

    public static JSONObject loadJSON(JSONObject object, String key){
        try {return object.getJSONObject(key);}
        catch (JSONException e){throw new RuntimeException("Fail to load " + key);}
    }

    public static boolean loadBoolean(JSONObject object, String key){
        try {return object.getBoolean(key);}
        catch (JSONException e){throw new RuntimeException("Fail to load " + key);}
    }

    public static int loadInt(JSONObject object, String key){
        try {return object.getInt(key);}
        catch (JSONException e){throw new RuntimeException("Fail to load " + key);}
    }
}
