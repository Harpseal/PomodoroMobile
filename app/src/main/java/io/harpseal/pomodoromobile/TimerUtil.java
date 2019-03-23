package io.harpseal.pomodoromobile;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;

//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.common.api.ResultCallback;
//import com.google.android.gms.wearable.DataApi;
//import com.google.android.gms.wearable.DataItem;
//import com.google.android.gms.wearable.DataMap;
//import com.google.android.gms.wearable.DataMapItem;
//import com.google.android.gms.wearable.NodeApi;
//import com.google.android.gms.wearable.PutDataMapRequest;
//import com.google.android.gms.wearable.Wearable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Harpseal on 15/10/17.
 */
public final class TimerUtil {
    private static final String TAG = "DigitalWatchFaceUtil";

    public static final String KEY_TIMER1 = "KEY_TIMER1";//type: int,sec
    public static final String KEY_TIMER2 = "KEY_TIMER2";//type: int,sec
    public static final String KEY_TIMER3 = "KEY_TIMER3";//type: int,sec
    public static final String KEY_TIMER4 = "KEY_TIMER4";//type: int,sec

    public static final String KEY_TIMER_DATE_START = "KEY_TIMER_DATE_START";//type: Long
    public static final String KEY_TIMER_DATE_END = "KEY_TIMER_DATE_END";//type: Long

    public static final String KEY_TOMATO_WORK = "KEY_TOMATO_WORK";//type: int,sec
    public static final String KEY_TOMATO_RELAX = "KEY_TOMATO_RELAX";//type: int,sec
    public static final String KEY_TOMATO_RELAX_LONG = "KEY_TOMATO_RELAX_LONG";//type: int,sec
    public static final String KEY_TOMATO_IDLE = "KEY_TOMATO_IDLE";//type: int,sec

    public static final String KEY_TOMATO_TYPE = "KEY_TOMATO_TYPE";//WORK,RELAX,RELAX_LONG,IDLE
    public static final String KEY_TOMATO_DATE_START = "KEY_TOMATO_DATE_START";//type: Long
    public static final String KEY_TOMATO_DATE_END = "KEY_TOMATO_DATE_END";//type: Long

    //public static final String KEY_TOMATO_TAG_LIST = "KEY_TOMATO_TAG_LIST";//type: ArrayList<DataMap>
    public static final String KEY_TOMATO_TAG_JSON_ARRAY_STRING = "KEY_TOMATO_TAG_JSON_ARRAY_STRING";//type: ArrayList<DataMap>
    public static final String KEY_TOMATO_TAG_NAME = "KEY_TOMATO_TAG_NAME";//string
    public static final String KEY_TOMATO_TAG_FLAG = "KEY_TOMATO_TAG_FLAG";//int

    //public static final String KEY_TOMATO_EVENTS = "KEY_TOMATO_EVENTS";//type: Long
    public static final String KEY_TOMATO_EVENT_QUEUE = "KEY_TOMATO_EVENT_QUEUE";//type: ArrayList<DataMap>
    public static final String KEY_TOMATO_CALENDAR_LIST = "KEY_TOMATO_CALENDAR_LIST";//type: ArrayList<DataMap>

    public static final String KEY_TOMATO_CALENDAR_ID = "KEY_TOMATO_CALENDAR_ID"; //Long
    public static final String KEY_TOMATO_CALENDAR_NAME = "KEY_TOMATO_CALENDAR_NAME"; //string
    public static final String KEY_TOMATO_CALENDAR_COLOR = "KEY_TOMATO_CALENDAR_COLOR"; //int
    public static final String KEY_TOMATO_CALENDAR_ACCOUNT_NAME = "KEY_TOMATO_CALENDAR_ACCOUNT_NAME";//string
    //public static final String KEY_TOMATO_CALENDAR_ACCOUNT_LIST = "KEY_TOMATO_CALENDAR_ACCOUNT_NAME";//string[]

    public static final String KEY_TOMATO_PHONE_BATTERY = "KEY_TOMATO_PHONE_BATTERY";//int
    public static final String KEY_TOMATO_SCR_ALWAYS_ON = "KEY_TOMATO_SCR_ALWAYS_ON";//int

    public static final String ID_TOMATO_NOTIFICATION_ID = "ID_TOMATO_NOTIFICATION_ID";//int

    public enum WatchControlState
    {
        WCS_IDLE,
        WCS_TOMATO,
        WCS_TIMER
    }


    public static final int DEFAULT_TOMATO_WORK = 30*60;
    public static final int DEFAULT_TOMATO_RELAX = 5*60;
    public static final int DEFAULT_TOMATO_RELAX_LONG = 10*60;

    public static final int DEFAULT_TIMER1 = 15*60;
    public static final int DEFAULT_TIMER2 = 0*60;
    public static final int DEFAULT_TIMER3 = 30*60;
    public static final int DEFAULT_TIMER4 = 40*60;
    public static final long DEFAULT_TIMER_ZERO = 0;

    public static final String DEFAULT_TOMATO_TYPE = KEY_TOMATO_IDLE;
    public static final long DEFAULT_TOMATO_DATE = 0;

    //public static final int DEFAULT_TOMATO_PHONE_BATTERY = -100;
    public static final int DEFAULT_TOMATO_PHONE_BATTERY_WAIT_SHORT = 5*60*1000;
    public static final int DEFAULT_TOMATO_PHONE_BATTERY_WAIT_LONG = 15*60*1000;

    public static final String[] DEFAULT_TOMATO_TAGS = {/*"[30]",*/"[N]"," Coding"," Doc"," Sport"," Reading"};
    //public static final String[] DEFAULT_TOMATO_EVENTS = {};

    public static final int TIME_PICKER_REQUEST_CODE_TIMER = 1234;
    public static final int TIME_PICKER_REQUEST_CODE_TOMATO = 5678;

    public static final long DEFAULT_TOMATO_CALENDAR_ID = -1; //Long
    public static final String DEFAULT_TOMATO_CALENDAR_NAME = ""; //string
    public static final int DEFAULT_TOMATO_CALENDAR_COLOR = 0; //int
    public static final String DEFAULT_TOMATO_CALENDAR_ACCOUNT_NAME = "";//string

    public static final String PATH_WITH_FEATURE = "/watch_face_config/PomodoroWear";
    public static final String PATH_WITH_MESSAGE = "/watch_face_config/PomodoroMsg";

    public static final String RECEIVER_ACTION_WATCH_FACE = "io.harpseal.pomodorowear.wear.RECEIVER";
    public static final String RECEIVER_ACTION_MOBILE= "io.harpseal.pomodorowear.mobile.RECEIVER";


    /**
     * Message Package structure
     * key                    value
     * MSG_ID_KEY             long: time in ms generated by sender
     * MSG_SENDER_KEY         string: MSG_SENDER_WATCH_FACE,MSG_SENDER_PHONE
     * MSG_TYPE_KEY           int: MSG_TYPE_BATTERY,MSG_TYPE_CREATE_EVENT
     * MSG_STATE_KEY          int: MSG_RESULT_UNKNOWN_ERROR,MSG_RESULT_OK,MSG_RESULT_NO_CONNECTED_NODE,MSG_RESULT_TIME_OUT
     *                        only included in return msg.
     *
     * optional
     * MSG_RESULT_INT_KEY     int
     * MSG_RESULT_STRING_KEY  string
     */


    public static final String MSG_ID_KEY = "MSG_ID_KEY";

    public static final String MSG_SENDER_KEY = "MSG_SENDER_KEY";
    public static final String MSG_SENDER_WATCH_FACE = "watch_face";
    public static final String MSG_SENDER_EVENT_BUILDER = "event_builder";
    public static final String MSG_SENDER_PHONE = "mobile";

    public static final String MSG_TYPE_KEY = "MSG_TYPE_KEY";
    public static final int MSG_TYPE_BATTERY = 0; //return value : int
    public static final int MSG_TYPE_CREATE_EVENT = 1;//return value : int
    public static final int MSG_TYPE_UPDATE_CALENDAR_LIST = 2;//return value : int

    public static final String MSG_RESULT_INT_KEY = "MSG_RESULT_INT_KEY";
    public static final String MSG_RESULT_STRING_KEY = "MSG_RESULT_STRING_KEY";


    public static final String MSG_STATE_KEY = "MSG_STATE_KEY";
    public static final int MSG_STATE_UNKNOWN_ERROR = -1;
    public static final int MSG_STATE_OK = 0;
    public static final int MSG_STATE_NO_CONNECTED_NODE = 1;
    public static final int MSG_STATE_TIME_OUT = 2;
    public static final int MSG_STATE_OPERATION_FAILED = 3;



    public static final int REQUEST_CODE_ASK_PERMISSIONS = 123;


    /**
     * Callback interface to perform an action with the current config {@link DataMap} for
     * {@link MainWatchFace}.
     */

//    public interface FetchConfigDataMapCallback {
//        /**
//         * Callback invoked with the current config {@link DataMap} for
//         * {@link MainWatchFace}.
//         */
//        void onConfigDataMapFetched(DataMap config);
//    }

    private static int parseColor(String colorName) {
        return Color.parseColor(colorName.toLowerCase());
    }

    /**
     * Asynchronously fetches the current config {@link DataMap} for {@link MainWatchFace}
     * and passes it to the given callback.
     * <p>
     * If the current config {@link DataItem} doesn't exist, it isn't created and the callback
     * receives an empty DataMap.
     */
//    public static void fetchConfigDataMap(final GoogleApiClient client,
//                                          final FetchConfigDataMapCallback callback) {
//        Wearable.NodeApi.getLocalNode(client).setResultCallback(
//                new ResultCallback<NodeApi.GetLocalNodeResult>() {
//                    @Override
//                    public void onResult(NodeApi.GetLocalNodeResult getLocalNodeResult) {
//                        String localNode = getLocalNodeResult.getNode().getId();
//                        Uri uri = new Uri.Builder()
//                                .scheme("wear")
//                                .path(WatchFaceUtil.PATH_WITH_FEATURE)
//                                .authority(localNode)
//                                .build();
//                        Wearable.DataApi.getDataItem(client, uri)
//                                .setResultCallback(new DataItemResultCallback(callback));
//                    }
//                }
//        );
//    }

    /**
     * Overwrites (or sets, if not present) the keys in the current config {@link DataItem} with
     * the ones appearing in the given {@link DataMap}. If the config DataItem doesn't exist,
     * it's created.
     * <p>
     * It is allowed that only some of the keys used in the config DataItem appear in
     * {@code configKeysToOverwrite}. The rest of the keys remains unmodified in this case.
     */
//    public static void overwriteKeysInConfigDataMap(final GoogleApiClient googleApiClient,
//                                                    final DataMap configKeysToOverwrite) {
//
//        WatchFaceUtil.fetchConfigDataMap(googleApiClient,
//                new FetchConfigDataMapCallback() {
//                    @Override
//                    public void onConfigDataMapFetched(DataMap currentConfig) {
//                        DataMap overwrittenConfig = new DataMap();
//                        overwrittenConfig.putAll(currentConfig);
//                        overwrittenConfig.putAll(configKeysToOverwrite);
//                        WatchFaceUtil.putConfigDataItem(googleApiClient, overwrittenConfig);
//                    }
//                }
//        );
//    }

    /**
     * Overwrites the current config {@link DataItem}'s {@link DataMap} with {@code newConfig}.
     * If the config DataItem doesn't exist, it's created.
     */
//    public static void putConfigDataItem(GoogleApiClient googleApiClient, DataMap newConfig) {
//        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(PATH_WITH_FEATURE);
//        DataMap configToPut = putDataMapRequest.getDataMap();
//        configToPut.putAll(newConfig);
//        Wearable.DataApi.putDataItem(googleApiClient, putDataMapRequest.asPutDataRequest())
//                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
//                    @Override
//                    public void onResult(DataApi.DataItemResult dataItemResult) {
//                        if (Log.isLoggable(TAG, Log.DEBUG)) {
//                            Log.d(TAG, "putDataItem result status: " + dataItemResult.getStatus());
//                        }
//                    }
//                });
//    }

//    private static class DataItemResultCallback implements ResultCallback<DataApi.DataItemResult> {
//
//        private final FetchConfigDataMapCallback mCallback;
//
//        public DataItemResultCallback(FetchConfigDataMapCallback callback) {
//            mCallback = callback;
//        }
//
//        @Override
//        public void onResult(DataApi.DataItemResult dataItemResult) {
//            if (dataItemResult.getStatus().isSuccess()) {
//                if (dataItemResult.getDataItem() != null) {
//                    DataItem configDataItem = dataItemResult.getDataItem();
//                    DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
//                    DataMap config = dataMapItem.getDataMap();
//                    mCallback.onConfigDataMapFetched(config);
//                } else {
//                    mCallback.onConfigDataMapFetched(new DataMap());
//                }
//            }
//        }
//    }
//
//    private WatchFaceUtil() { }


    /***
     * Android L (lollipop, API 21) introduced a new problem when trying to invoke implicit intent,
     * "java.lang.IllegalArgumentException: Service Intent must be explicit"
     *
     * If you are using an implicit intent, and know only 1 target would answer this intent,
     * This method will help you turn the implicit intent into the explicit form.
     *
     * Inspired from SO answer: http://stackoverflow.com/a/26318757/1446466
     * @param context
     * @param implicitIntent - The original implicit intent
     * @return Explicit Intent created from the implicit original intent
     */
    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }

    public static JSONArray TagStringArrayToJArray(String[] strArray)
    {
        JSONArray array = new JSONArray();
        for (String str : strArray)
            array.put(str);
        return array;
    }

    public static JSONArray TagStringToJArray(String str)
    {
        try
        {
            return new JSONArray(str);
        }catch (org.json.JSONException je)
        {
            return TagStringArrayToJArray(TimerUtil.DEFAULT_TOMATO_TAGS);
        }
    }

    public static final class PomodoroTagList extends ArrayList<PomodoroTag>
    {
        public void setByJsonArrayString(String jsonArray)
        {
            clear();

            try {
                JSONArray jarray = TagStringToJArray(jsonArray);
                for (int i = 0; i < jarray.length(); i++) {
                    add(new PomodoroTag(jarray.getString(i), 0));
                }
            }catch (org.json.JSONException je)
            {
                clear();
                for (String str : TimerUtil.DEFAULT_TOMATO_TAGS)
                    add(new PomodoroTag(str, 0));
            }

        }

        public void setByStringArray(String [] array)
        {
            clear();
            for (String str : array)
            {
                add(new PomodoroTag(str,0));
            }
        }

        public void setByStringSet(Set<String> set)
        {
            clear();
            for (String str : set)
            {
                add(new PomodoroTag(str,0));
            }
        }

        public void setByArray(String [] arrayName,int[] arrayFlag)
        {
            if (arrayName.length != arrayFlag.length) return;
            clear();
            for (int i=0;i<arrayName.length;i++)
            {
                add(new PomodoroTag(arrayName[i],arrayFlag[i]));
            }
        }

//        public void setByDataMapArray(ArrayList<DataMap> arrayMap)
//        {
//            clear();
//            for (DataMap map:arrayMap)
//            {
//                add(new PomodoroTag(map));
//            }
//        }

        public String[] toStringArray()
        {
            if (size() == 0) return null;
            String[] array = new String[size()];
            for (int i=0;i<size();i++)
            {
                array[i] = get(i).name;
            }
            return  array;
        }

        public Set<String> toStringSet()
        {
            if (size() == 0) return null;
            Set<String> set = new LinkedHashSet<>();
            for (int i=0;i<size();i++)
            {
                set.add(get(i).name);
            }
            return  set;
        }

        public JSONArray toJsonArray()
        {
            if (size() == 0) return null;
            JSONArray jarray = new JSONArray();

            for (int i=0;i<size();i++)
            {
                jarray.put(get(i).name);
            }
            return jarray;

        }

        public String toJsonArrayString()
        {
            if (size() == 0) return null;
            JSONArray jarray = toJsonArray();
            if (jarray != null) return jarray.toString();
            return null;
        }

//        public ArrayList<DataMap> toDataMapArray()
//        {
//            ArrayList<DataMap> arrayMap = new ArrayList<DataMap>();
//            for (PomodoroTag tag : this)
//            {
//                arrayMap.add(tag.toDataMap());
//            }
//            return arrayMap;
//        }
    }

    public static class PomodoroTag
    {
        final int FLAG_ENABLE_DEFAULT = 0x1;
        final int FLAG_AUTO_ENABLE_BY_DESCRIPTION = 0x2;
        private String name = "";
        private int flag = 0;

//        public PomodoroTag(DataMap map)
//        {
//            setDataMap(map);
//        }


        public PomodoroTag(String _name,int _flag)
        {
            name = _name;
            flag = _flag;
        }

        public String toString(){return name;}

        public String getName(){return name;}
        public void setName(String _name){name = _name;}

        public boolean getIsEnableDefault(){
            return (flag & FLAG_ENABLE_DEFAULT) != 0;
        }

        public void setIsEnableDefault(boolean isEnable)
        {
            if (isEnable)
                flag |= FLAG_ENABLE_DEFAULT;
            else
                flag &= (~FLAG_ENABLE_DEFAULT);
        }

        public boolean getIsAutoEnableDescription(){
            return (flag & FLAG_AUTO_ENABLE_BY_DESCRIPTION) != 0;
        }

        public void setIsAutoEnableDescription(boolean isEnable)
        {
            if (isEnable)
                flag |= FLAG_AUTO_ENABLE_BY_DESCRIPTION;
            else
                flag &= (~FLAG_AUTO_ENABLE_BY_DESCRIPTION);
        }

//        public DataMap toDataMap()
//        {
//            DataMap map = new DataMap();
//            map.putString(KEY_TOMATO_TAG_NAME,name);
//            map.putInt(KEY_TOMATO_TAG_FLAG,flag);
//            return map;
//        }
//
//        public boolean setDataMap(DataMap map)
//        {
//            if (map.containsKey(KEY_TOMATO_TAG_NAME))
//                name = map.getString(KEY_TOMATO_TAG_NAME,"");
//            else
//                return false;
//            if (map.containsKey(KEY_TOMATO_TAG_FLAG))
//                flag = map.getInt(KEY_TOMATO_TAG_FLAG,0);
//            else
//                return false;
//            return true;
//        }
    }
}
