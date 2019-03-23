package io.harpseal.pomodoromobile;

import android.Manifest;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import android.preference.PreferenceFragment;

public class ConfigFragment extends PreferenceFragment {

    private final static String TAG = "MainConfigActivity";
    //ListPreference mCalendarListPref;

    Preference mPrefBuildInfo;

    Preference mPrefPomodoroCalendar;
    Preference mPrefPomodoroTags;
    Preference mPrefPomodoroTimerWork;
    Preference mPrefPomodoroTimerRelax;
    Preference mPrefPomodoroTimerLongRelex;

//    Preference mPrefTimer1;
//    Preference mPrefTimer2;
//    Preference mPrefTimer3;
//    Preference mPrefTimer4;

    private int mDataTomatoWork = TimerUtil.DEFAULT_TOMATO_WORK;
    private int mDataTomatoRelax = TimerUtil.DEFAULT_TOMATO_RELAX;
    private int mDataTomatoRelaxLong = TimerUtil.DEFAULT_TOMATO_RELAX_LONG;

    private int mDataTimer1 = TimerUtil.DEFAULT_TIMER1;
    private int mDataTimer2 = TimerUtil.DEFAULT_TIMER2;
    private int mDataTimer3 = TimerUtil.DEFAULT_TIMER3;
    private int mDataTimer4 = TimerUtil.DEFAULT_TIMER4;



    private class CalendarItem
    {
        public long id = 0;
        public String name = "";
        public String accountName = "";
        public int color = 0;
        public CalendarItem(long _id,String _name,String accName,int _color)
        {
            id = _id;
            name = _name;
            accountName = accName;
            color = _color;
        }
        @Override
        public String toString()
        {
            return name;
        }

    }

    private boolean mCalenderListUpdated = false;
    private static final ArrayList<CalendarItem> mCalendarList = new ArrayList<CalendarItem>();
    private int mSelectedCalendarListIdx = -1;
    private long mSelectedCalendarID = -1;

    private AlertDialog mTagAlertDialog;
    private DynamicListView mTagDynListView;

    private TimerUtil.PomodoroTagList mPromodoroTagList;
    private DynamicArrayAdapter mTagDynAdapter;

    //private GoogleApiClient mGoogleApiClient;
    //private String mPeerId;

    private final int mDefaultMinSec = 10;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        mPrefBuildInfo = findPreference("pref_key_build_info");

        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd.HHmm");
        Date buildDate = new Date(BuildConfig.TIMESTAMP);
        mPrefBuildInfo.setSummary(BuildConfig.VERSION_NAME + "." + BuildConfig.VERSION_CODE + "." + sdf.format(buildDate));

        mPrefPomodoroCalendar = findPreference("pref_key_pomodoro_calendar");
        mPrefPomodoroTags = findPreference("pref_key_pomodoro_tags");
        mPrefPomodoroTimerWork = findPreference("pref_key_pomodoro_work");
        mPrefPomodoroTimerRelax = findPreference("pref_key_pomodoro_relax");
        mPrefPomodoroTimerLongRelex = findPreference("pref_key_pomodoro_relax_long");
//        mPrefTimer1 = findPreference("pref_key_timer1");
//        mPrefTimer2 = findPreference("pref_key_timer2");
//        mPrefTimer3 = findPreference("pref_key_timer3");
//        mPrefTimer4 = findPreference("pref_key_timer4");




        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        mDataTomatoWork = prefs.getInt(TimerUtil.KEY_TOMATO_WORK, mDataTomatoWork);
        mPrefPomodoroTimerWork.setSummary("" + mDataTomatoWork / 60 + " min");

        mDataTomatoRelax = prefs.getInt(TimerUtil.KEY_TOMATO_RELAX, mDataTomatoRelax);
        mPrefPomodoroTimerRelax.setSummary("" + mDataTomatoRelax / 60 + " min");

        mDataTomatoRelaxLong = prefs.getInt(TimerUtil.KEY_TOMATO_RELAX_LONG, mDataTomatoRelaxLong);
        mPrefPomodoroTimerLongRelex.setSummary("" + mDataTomatoRelaxLong / 60 + " min");

        mDataTimer1 = prefs.getInt(TimerUtil.KEY_TOMATO_RELAX_LONG, mDataTimer1);
        //mPrefTimer1.setSummary("" + mDataTimer1 / 60 + " min");

        mDataTimer2 = prefs.getInt(TimerUtil.KEY_TOMATO_RELAX_LONG, mDataTimer2);
        //mPrefTimer2.setSummary("" + mDataTimer2 / 60 + " min");

        mDataTimer3 = prefs.getInt(TimerUtil.KEY_TOMATO_RELAX_LONG, mDataTimer3);
        //mPrefTimer3.setSummary("" + mDataTimer3 / 60 + " min");

        mDataTimer4 = prefs.getInt(TimerUtil.KEY_TOMATO_RELAX_LONG, mDataTimer4);
        //mPrefTimer4.setSummary("" + mDataTimer4 / 60 + " min");

        mSelectedCalendarID = prefs.getLong(TimerUtil.KEY_TOMATO_CALENDAR_ID,-1);
        String calName = prefs.getString(TimerUtil.KEY_TOMATO_CALENDAR_NAME,"");
        if (calName.length() != 0)
        {
            mPrefPomodoroCalendar.setTitle(getResources().getString(R.string.perf_item_pomodoro_calendar) + " : " + calName);
        }


        mPrefPomodoroCalendar.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showCalendarPickerDialog();
                return false;
            }
        });

        mPrefPomodoroTags.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mTagAlertDialog.show();
                return false;
            }
        });


        mPrefPomodoroTimerWork.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                displayMinutePickerDialog(false, mDataTomatoWork, 60,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {

                                Dialog dialog = (Dialog) dialogInterface;
                                NumberPicker pickerHour = (NumberPicker) dialog.findViewById(R.id.dialog_number_picker_hour);
                                NumberPicker pickerMin = (NumberPicker) dialog.findViewById(R.id.dialog_number_picker_min);
                                pickerHour.clearFocus();
                                pickerMin.clearFocus();
                                mDataTomatoWork = (pickerHour.getValue() * 60 + pickerMin.getValue()) * 60;
                                if (mDataTomatoWork == 0) mDataTomatoWork = mDefaultMinSec;
                                Log.v(TAG, "MinRes : " + pickerHour.getValue() + " : " + pickerMin.getValue() + " = " + mDataTomatoWork);
                                saveConfigInt(TimerUtil.KEY_TOMATO_WORK, mDataTomatoWork);
                                mPrefPomodoroTimerWork.setSummary("" + mDataTomatoWork / 60 + " min");

                            }
                        });
                return false;
            }
        });

        mPrefPomodoroTimerRelax.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                displayMinutePickerDialog(false, mDataTomatoRelax, 60,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {

                                Dialog dialog = (Dialog) dialogInterface;
                                NumberPicker pickerHour = (NumberPicker) dialog.findViewById(R.id.dialog_number_picker_hour);
                                NumberPicker pickerMin = (NumberPicker) dialog.findViewById(R.id.dialog_number_picker_min);
                                pickerHour.clearFocus();
                                pickerMin.clearFocus();
                                mDataTomatoRelax = (pickerHour.getValue() * 60 + pickerMin.getValue()) * 60;
                                if (mDataTomatoRelax == 0) mDataTomatoRelax = mDefaultMinSec;
                                Log.v(TAG, "MinRes : " + pickerHour.getValue() + " : " + pickerMin.getValue() + " = " + mDataTomatoRelax);
                                saveConfigInt(TimerUtil.KEY_TOMATO_RELAX, mDataTomatoRelax);
                                mPrefPomodoroTimerRelax.setSummary("" + mDataTomatoRelax / 60 + " min");

                            }
                        });
                return false;
            }
        });

        mPrefPomodoroTimerLongRelex.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                displayMinutePickerDialog(false, mDataTomatoRelaxLong, 60,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {

                                Dialog dialog = (Dialog) dialogInterface;
                                NumberPicker pickerHour = (NumberPicker) dialog.findViewById(R.id.dialog_number_picker_hour);
                                NumberPicker pickerMin = (NumberPicker) dialog.findViewById(R.id.dialog_number_picker_min);
                                pickerHour.clearFocus();
                                pickerMin.clearFocus();
                                mDataTomatoRelaxLong = (pickerHour.getValue() * 60 + pickerMin.getValue()) * 60;
                                if (mDataTomatoRelaxLong == 0) mDataTomatoRelaxLong = mDefaultMinSec;
                                Log.v(TAG, "MinRes : " + pickerHour.getValue() + " : " + pickerMin.getValue() + " = " + mDataTomatoRelaxLong);
                                saveConfigInt(TimerUtil.KEY_TOMATO_RELAX_LONG, mDataTomatoRelaxLong);
                                mPrefPomodoroTimerLongRelex.setSummary("" + mDataTomatoRelaxLong / 60 + " min");

                            }
                        });
                return false;
            }
        });
/*
        mPrefTimer1.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
//                showTimePickerDialog(mDataTimer1,
//                        new TimePickerDialog.OnTimeSetListener() {
//                            @Override
//                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//                                mDataTimer1 = (hourOfDay*60 + minute)*60;
//                                sendConfigUpdateMessage(TimerUtil.KEY_TIMER1,mDataTimer1);
//                                mPrefTimer1.setSummary("" + mDataTimer1/60 + " min");
//                            }
//                        });
//                return false;

                displayMinutePickerDialog(true,mDataTimer1,12*60,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {

                        Dialog dialog  = (Dialog) dialogInterface;
                        NumberPicker pickerHour = (NumberPicker)dialog.findViewById(R.id.dialog_number_picker_hour);
                        NumberPicker pickerMin = (NumberPicker)dialog.findViewById(R.id.dialog_number_picker_min);
                        pickerHour.clearFocus();
                        pickerMin.clearFocus();
                        mDataTimer1 = (pickerHour.getValue() * 60 + pickerMin.getValue()) * 60;
                        Log.v(TAG, "MinRes : " + pickerHour.getValue() + " : " + pickerMin.getValue() + " = " + mDataTimer1);
                        saveConfigInt(TimerUtil.KEY_TIMER1,mDataTimer1);
                        mPrefTimer1.setSummary("" + mDataTimer1 / 60 + " min");

                    }
                });
                return false;
            }
        });

        mPrefTimer2.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                displayMinutePickerDialog(true,mDataTimer2,12*60,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {

                                Dialog dialog  = (Dialog) dialogInterface;
                                NumberPicker pickerHour = (NumberPicker)dialog.findViewById(R.id.dialog_number_picker_hour);
                                NumberPicker pickerMin = (NumberPicker)dialog.findViewById(R.id.dialog_number_picker_min);
                                pickerHour.clearFocus();
                                pickerMin.clearFocus();
                                mDataTimer2 = (pickerHour.getValue() * 60 + pickerMin.getValue()) * 60;
                                Log.v(TAG, "MinRes : " + pickerHour.getValue() + " : " + pickerMin.getValue() + " = " + mDataTimer2);
                                saveConfigInt(TimerUtil.KEY_TIMER2,mDataTimer2);
                                mPrefTimer2.setSummary("" + mDataTimer2 / 60 + " min");

                            }
                        });
                return false;
            }
        });

        mPrefTimer3.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                displayMinutePickerDialog(true, mDataTimer3, 12 * 60,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {

                                Dialog dialog = (Dialog) dialogInterface;
                                NumberPicker pickerHour = (NumberPicker) dialog.findViewById(R.id.dialog_number_picker_hour);
                                NumberPicker pickerMin = (NumberPicker) dialog.findViewById(R.id.dialog_number_picker_min);
                                pickerHour.clearFocus();
                                pickerMin.clearFocus();
                                mDataTimer3 = (pickerHour.getValue() * 60 + pickerMin.getValue()) * 60;
                                Log.v(TAG, "MinRes : " + pickerHour.getValue() + " : " + pickerMin.getValue() + " = " + mDataTimer3);
                                saveConfigInt(TimerUtil.KEY_TIMER3, mDataTimer3);
                                mPrefTimer3.setSummary("" + mDataTimer3 / 60 + " min");

                            }
                        });
                return false;
            }
        });

        mPrefTimer4.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                displayMinutePickerDialog(true, mDataTimer4, 12 * 60,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {

                                Dialog dialog = (Dialog) dialogInterface;
                                NumberPicker pickerHour = (NumberPicker) dialog.findViewById(R.id.dialog_number_picker_hour);
                                NumberPicker pickerMin = (NumberPicker) dialog.findViewById(R.id.dialog_number_picker_min);
                                pickerHour.clearFocus();
                                pickerMin.clearFocus();
                                mDataTimer4 = (pickerHour.getValue() * 60 + pickerMin.getValue()) * 60;
                                Log.v(TAG, "MinRes : " + pickerHour.getValue() + " : " + pickerMin.getValue() + " = " + mDataTimer4);
                                saveConfigInt(TimerUtil.KEY_TIMER4, mDataTimer4);
                                mPrefTimer4.setSummary("" + mDataTimer4 / 60 + " min");

                            }
                        });
                return false;
            }
        });
        */

        //PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);
        //setPreferenceScreen(root);
        //populatePreferenceHierarchy(root);

        mTagDynListView = new DynamicListView(this.getContext());
        mTagDynListView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


        mPromodoroTagList = new TimerUtil.PomodoroTagList();


        //String prefTagsListString = prefs.getString(TimerUtil.KEY_TOMATO_TAG_LIST, "");
        String tagJStr = prefs.getString(TimerUtil.KEY_TOMATO_TAG_JSON_ARRAY_STRING,"");
        mPromodoroTagList.setByJsonArrayString(tagJStr);
        Log.e(TAG,"Tag load : "+mPromodoroTagList.toJsonArrayString());

//        if (prefTagsListString.length() != 0)
//        {
//            DataMap dataMap = DataMap.fromByteArray(Base64.decode(prefTagsListString, Base64.DEFAULT));
//            mPromodoroTagList.setByDataMapArray(dataMap.getDataMapArrayList(TimerUtil.KEY_TOMATO_TAG_LIST));
//        }
//        else
//            mPromodoroTagList.setByStringArray(TimerUtil.DEFAULT_TOMATO_TAGS);

        //byte[] array = ;
//        byt
//        //sendConfigUpdateMessageDataMapArray(TimerUtil.KEY_TOMATO_TAG_LIST, mPromodoroTagList.toDataMapArray());
//        mPromodoroTagList.setByDataMapArray(config.getDataMapArrayList(configKey));
//        mTagDynAdapter.setList(mPromodoroTagList);
//        mTagDynAdapter.notifyDataSetChanged();
//        uiUpdated = true;


        mTagDynAdapter = new DynamicArrayAdapter(this.getContext(), R.layout.dyn_text_view, mPromodoroTagList);

        mTagDynListView.setDynamicArrayList(mPromodoroTagList);
        mTagDynListView.setAdapter(mTagDynAdapter);
        mTagDynListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mTagDynListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ConfigFragment.this.getContext());
                builder.setTitle("Tag Editing");

                LayoutInflater inflater = ConfigFragment.this.getActivity().getLayoutInflater();
                View viewEdit = inflater.inflate(R.layout.tag_editor_dialog, null);
                final android.widget.EditText textInput = (android.widget.EditText)viewEdit.findViewById(R.id.dialog_tag_editor_editText);
                final android.widget.CheckBox checkBox = (android.widget.CheckBox)viewEdit.findViewById(R.id.dialog_tag_editor_checkBox);

                if (position>=0 && position<mPromodoroTagList.size()) {
                    textInput.setText(mPromodoroTagList.get(position).getName());
                    checkBox.setChecked(mPromodoroTagList.get(position).getIsEnableDefault());
                }
                builder.setView(viewEdit);

// Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.v(TAG, "Text input :[" + textInput.getText().toString() + "]");
                        mPromodoroTagList.get(position).setName(textInput.getText().toString());
                        mPromodoroTagList.get(position).setIsEnableDefault(checkBox.isChecked());
                        mTagDynAdapter.setList(mPromodoroTagList);
                        mTagDynAdapter.notifyDataSetChanged();
                        mTagAlertDialog.show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        mTagAlertDialog.show();
                    }
                });
                builder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                        //dialog.dismiss();
                        mPromodoroTagList.remove(position);
                        mTagDynAdapter.setList(mPromodoroTagList);
                        mTagDynAdapter.notifyDataSetChanged();
                        mTagAlertDialog.show();


                    }
                });

                builder.show();
            }
        });

        AlertDialog.Builder adBuilder = new AlertDialog.Builder(ConfigFragment.this.getContext())
                .setTitle("Tags")
                        //.setMessage("Click to schedule or view events.")
                .setView(mTagDynListView)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //sendConfigUpdateMessageDataMapArray(TimerUtil.KEY_TOMATO_TAG_LIST, mPromodoroTagList.toDataMapArray());
                        saveConfigString(TimerUtil.KEY_TOMATO_TAG_JSON_ARRAY_STRING,mPromodoroTagList.toJsonArrayString());
                        Log.e(TAG,"Tag write : "+mPromodoroTagList.toJsonArrayString());

                        String tagJStr = prefs.getString(TimerUtil.KEY_TOMATO_TAG_JSON_ARRAY_STRING,"");
                        mPromodoroTagList.setByJsonArrayString(tagJStr);
                        Log.e(TAG,"Tag reload : "+mPromodoroTagList.toJsonArrayString());

                        dialog.dismiss();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Do nothing.
                                dialog.dismiss();

                            }
                        }
                ).setNeutralButton("Add", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Do nothing.
                                //dialog.dismiss();

                                AlertDialog.Builder builder = new AlertDialog.Builder(ConfigFragment.this.getContext());
                                builder.setTitle("Create a new Tag");

                                LayoutInflater inflater = ConfigFragment.this.getActivity().getLayoutInflater();
                                View view = inflater.inflate(R.layout.tag_editor_dialog, null);
                                final android.widget.EditText textInput = (android.widget.EditText)view.findViewById(R.id.dialog_tag_editor_editText);
                                final android.widget.CheckBox checkBox = (android.widget.CheckBox)view.findViewById(R.id.dialog_tag_editor_checkBox);

// Set up the input
//                                final android.widget.EditText input = new android.widget.EditText(MainConfigActivity.this);
//// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
//                                input.setInputType(InputType.TYPE_CLASS_TEXT);// | InputType.TYPE_TEXT_VARIATION_PASSWORD
                                builder.setView(view);

// Set up the buttons
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //m_Text = input.getText().toString();
                                        Log.v(TAG, "Text input :[" + textInput.getText().toString() + "]");
                                        TimerUtil.PomodoroTag tagNew = new TimerUtil.PomodoroTag(textInput.getText().toString(),0);
                                        tagNew.setIsEnableDefault(checkBox.isChecked());
                                        mPromodoroTagList.add(tagNew);
                                        mTagDynAdapter.setList(mPromodoroTagList);

                                        mTagAlertDialog.show();
                                    }
                                });
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                                builder.show();

                            }
                        }
                );
        mTagAlertDialog = adBuilder.create();


        updateCalendarListWrapper();



//        mPeerId = getIntent().getStringExtra(WatchFaceCompanion.EXTRA_PEER_ID);
//        if (mPeerId != null)
//        {
//            mGoogleApiClient = new GoogleApiClient.Builder(this)
//                    .addConnectionCallbacks(this)
//                    .addOnConnectionFailedListener(this)
//                    .addApi(Wearable.API)
//                    .build();
//        }
//        else
//            mGoogleApiClient = null;



    }
    private void showTimePickerDialog(int timeSec, TimePickerDialog.OnTimeSetListener listener)
    {
        int timeMin = timeSec/60;
        RangeTimePickerDialog rtpDialog = new RangeTimePickerDialog(ConfigFragment.this.getContext(),
                listener, timeMin/60, timeMin%60, true);

        rtpDialog.setMax(11,59);
        rtpDialog.setMin(0,0);
        rtpDialog.show();

    }

    private void showCalendarPickerDialog()
    {
        if (!mCalenderListUpdated)
        {
            updateCalendarListWrapper();
            return;
        }
        String[] calNameArray = new String[mCalendarList.size()];
        int c=0;
        for (CalendarItem item : mCalendarList)
        {
            calNameArray[c] = item.name;
            c++;
        }

        new AlertDialog.Builder(ConfigFragment.this.getContext())
                .setTitle("Please select a calender")
                .setItems(calNameArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSelectedCalendarListIdx = which;
                        mSelectedCalendarID = mCalendarList.get(mSelectedCalendarListIdx).id;
                        mPrefPomodoroCalendar.setTitle(getResources().getString(R.string.perf_item_pomodoro_calendar) + " : " + mCalendarList.get(mSelectedCalendarListIdx).name);
                        sendCalendarConfigUpdateMessage();
                    }
                }).show();
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        updateCalendarListWrapper();
//        Log.d(TAG,"onStart....");
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//
//    }

    final static int MSG_SHOW_NO_PEER_DIALOG = 100;
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SHOW_NO_PEER_DIALOG:
                    displayNoConnectedDeviceDialog();
                    break;
            }
            super.handleMessage(msg);
        }
    };
/*
    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnected(Bundle connectionHint) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnected: " + connectionHint);
        }

        if (mPeerId != null && mGoogleApiClient != null) {
            Uri.Builder builder = new Uri.Builder();
            Uri uri = builder.scheme("wear").path(TimerUtil.PATH_WITH_FEATURE).authority(mPeerId).build();
            Log.d(TAG,"onConnected url: "+uri.toString());
            Wearable.DataApi.getDataItem(mGoogleApiClient, uri).setResultCallback(this);

        } else if (mGoogleApiClient != null){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //mGoogleApiClient.blockingConnect(100, TimeUnit.MILLISECONDS);
                    NodeApi.GetConnectedNodesResult result =
                            Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                    List<Node> nodes = result.getNodes();
                    for (Node n : nodes)
                        Log.d(TAG,"Node " + n.getId() + "  " + n.getDisplayName());
                    if (nodes.size() > 0) {
                        mPeerId = nodes.get(0).getId();
                        //if (nodes.size() > 1)
                        //    Toast.makeText(getBaseContext(), "Connected to " + nodes.get(0).getDisplayName(), Toast.LENGTH_LONG).show();
                        Uri.Builder builder = new Uri.Builder();
                        Uri uri = builder.scheme("wear").path(TimerUtil.PATH_WITH_FEATURE).authority(mPeerId).build();
                        Log.d(TAG, "onConnected url: " + uri.toString());
                        Wearable.DataApi.getDataItem(mGoogleApiClient, uri).setResultCallback(MainConfigActivity.this);
                    }
                    else {
                        mHandler.sendEmptyMessage(MSG_SHOW_NO_PEER_DIALOG);
                        //displayNoConnectedDeviceDialog();
                    }

                }
            }).start();

        }
    }*/
/*
    private void updateUiForConfigDataMap(final DataMap config) {
        boolean uiUpdated = false;
        for (String configKey : config.keySet()) {
            if (!config.containsKey(configKey)) {
                continue;
            }

            int newTime = -1;

            if (configKey.equals(TimerUtil.KEY_TOMATO_CALENDAR_ID)) {
                mSelectedCalendarID = config.getLong(configKey);uiUpdated = true;
                Log.d(TAG,"updateUiForConfigDataMap mSelectedCalendarID :" + mSelectedCalendarID);
                newTime = (int)mSelectedCalendarID;
                for (CalendarItem calItem : mCalendarList)
                {
                    if (calItem.id == mSelectedCalendarID)
                    {
                        mPrefPomodoroCalendar.setTitle(getResources().getString(R.string.perf_item_pomodoro_calendar) + " : " + calItem.name);
                        break;
                    }
                }
            }
            else if (configKey.equals(TimerUtil.KEY_TOMATO_CALENDAR_NAME)){
                Log.d(TAG,"updateUiForConfigDataMap KEY_TOMATO_CALENDAR_NAME :" + config.getString(configKey));
            } else if (configKey.equals(TimerUtil.KEY_TOMATO_TAG_LIST)) {
                mPromodoroTagList.setByDataMapArray(config.getDataMapArrayList(configKey));
                mTagDynAdapter.setList(mPromodoroTagList);
                mTagDynAdapter.notifyDataSetChanged();
                uiUpdated = true;
            } else if (configKey.equals(TimerUtil.KEY_TOMATO_WORK)) {
                mDataTomatoWork = config.getInt(configKey);
                mPrefPomodoroTimerWork.setSummary("" + mDataTomatoWork/60 + " min");
            } else if (configKey.equals(TimerUtil.KEY_TOMATO_RELAX)) {
                mDataTomatoRelax = config.getInt(configKey);
                mPrefPomodoroTimerRelax.setSummary("" + mDataTomatoRelax/60 + " min");
            } else if (configKey.equals(TimerUtil.KEY_TOMATO_RELAX_LONG)) {
                mDataTomatoRelaxLong = config.getInt(configKey);
                mPrefPomodoroTimerLongRelex.setSummary("" + mDataTomatoRelaxLong/60 + " min");
            } else if (configKey.equals(TimerUtil.KEY_TIMER1)) {
                mDataTimer1 = config.getInt(configKey);
                mPrefTimer1.setSummary("" + mDataTimer1/60 + " min");
            } else if (configKey.equals(TimerUtil.KEY_TIMER2)) {
                mDataTimer2 = config.getInt(configKey);
                mPrefTimer2.setSummary("" + mDataTimer2/60 + " min");
            } else if (configKey.equals(TimerUtil.KEY_TIMER3)) {
                mDataTimer3 = config.getInt(configKey);
                mPrefTimer3.setSummary("" + mDataTimer3/60 + " min");
            } else if (configKey.equals(TimerUtil.KEY_TIMER4)) {
                mDataTimer4 = config.getInt(configKey);
                mPrefTimer4.setSummary("" + mDataTimer4/60 + " min");
            }

//            if (configKey.equals(TimerUtil.KEY_TIMER1)) {
//                newTime = mDataTimer1 = config.getInt(configKey);uiUpdated = true;
//            } else if (configKey.equals(TimerUtil.KEY_TIMER2)) {
//                newTime =  mDataTimer2 = config.getInt(configKey);uiUpdated = true;
//            } else if (configKey.equals(TimerUtil.KEY_TIMER3)) {
//                newTime = mDataTimer3 = config.getInt(configKey);uiUpdated = true;
//            } else if (configKey.equals(TimerUtil.KEY_TIMER4)) {
//                newTime = mDataTimer4 = config.getInt(configKey);uiUpdated = true;
//            } else if (configKey.equals(TimerUtil.KEY_TOMATO_WORK)) {
//                newTime =  mDataTomatoWork = config.getInt(configKey);uiUpdated = true;
//            } else if (configKey.equals(TimerUtil.KEY_TOMATO_RELAX)) {
//                newTime =  mDataTomatoRelax = config.getInt(configKey);uiUpdated = true;
//            } else if (configKey.equals(TimerUtil.KEY_TOMATO_RELAX_LONG)) {
//                newTime = mDataTomatoRelaxLong = config.getInt(configKey);uiUpdated = true;
//            } else if (configKey.equals(TimerUtil.KEY_TOMATO_CALENDAR_ID)) {
//                mCalendarID = config.getLong(configKey);uiUpdated = true;
//            } else if (configKey.equals(TimerUtil.KEY_TOMATO_CALENDAR_COLOR)) {
//                mCalendarColor = config.getInt(configKey);uiUpdated = true;
//            } else if (configKey.equals(TimerUtil.KEY_TOMATO_CALENDAR_NAME)) {
//                mCalendarName = config.getString(configKey);uiUpdated = true;
//            } else if (configKey.equals(TimerUtil.KEY_TOMATO_TAGS)) {
//                mTomatoTags = config.getStringArray(configKey);uiUpdated = true;
//            } else if (configKey.equals(TimerUtil.KEY_TOMATO_EVENTS)) {
//                mTomatoEvents = config.getStringArray(configKey);uiUpdated = true;
//            } else if (configKey.equals(TimerUtil.KEY_TOMATO_TYPE)) {
//                mTomatoType = config.getString(configKey);uiUpdated = true;
//            } else if (configKey.equals(TimerUtil.KEY_TOMATO_DATE)) {
//                Long dateInMillis = config.getLong(configKey);uiUpdated = true;
//                Calendar cal = Calendar.getInstance();
//                cal.setTimeInMillis(dateInMillis);
//                mTomatoDate = cal.getTime();
//                uiUpdated = true;
//            } else {
//                Log.w(TAG, "Ignoring unknown config key: " + configKey);
//            }

            Log.d(TAG, "updateUiForConfigDataMap configKey:" + configKey + " sec: " + newTime);

        }


    }
    */
/*
    @Override // ResultCallback<DataApi.DataItemResult>
    public void onResult(DataApi.DataItemResult dataItemResult) {
        if (dataItemResult.getStatus().isSuccess() && dataItemResult.getDataItem() != null) {
            DataItem configDataItem = dataItemResult.getDataItem();
            DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
            DataMap config = dataMapItem.getDataMap();

            Log.d(TAG, "onResult 0");
            updateUiForConfigDataMap(config);

            //updateConfigDataItemAndUiOnStartup();
            Log.d(TAG, "onResult 1");
            //if (!mCalenderListUpdated) updateCalendarListWrapper();
            //setUpAllPickers(config);
        } else {
            // If DataItem with the current config can't be retrieved, select the default items on
            // each picker.
            //setUpAllPickers(null);
        }
    }

    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnectionSuspended(int cause) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnectionSuspended: " + cause);
        }
    }

    @Override // GoogleApiClient.OnConnectionFailedListener
    public void onConnectionFailed(ConnectionResult result) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnectionFailed: " + result);
        }
    }
*/
    private void displayMinutePickerDialog(boolean isShowHour,int secondCur,int minuteMax,DialogInterface.OnClickListener onOkLisenter)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(ConfigFragment.this.getContext());
        //builder.setTitle("Tag Editing");

        LayoutInflater inflater = ConfigFragment.this.getActivity().getLayoutInflater();
        View viewNumPicker = inflater.inflate(R.layout.dialog_minute_picker, null);
        final android.widget.NumberPicker pickerHour = (android.widget.NumberPicker)viewNumPicker.findViewById(R.id.dialog_number_picker_hour);
        final android.widget.NumberPicker pickerMin = (android.widget.NumberPicker)viewNumPicker.findViewById(R.id.dialog_number_picker_min);
        final android.widget.TextView textHour = (android.widget.TextView)viewNumPicker.findViewById(R.id.dialog_text_picker_hour);
        final android.widget.TextView textMin = (android.widget.TextView)viewNumPicker.findViewById(R.id.dialog_text_picker_min);
        pickerHour.setWrapSelectorWheel(true);
        pickerMin.setWrapSelectorWheel(true);

        pickerHour.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                //builder.setTitle("" + pickerHour.getValue() + " : " + pickerMin.getValue())
            }
        });

        int minCur = secondCur/60;
        if (isShowHour)
        {
            float hourMax = (float)Math.ceil((float)minuteMax/60.f);
            pickerHour.setMaxValue((int)hourMax);
            pickerHour.setMinValue(0);
            pickerHour.setValue(minCur/60);

            pickerMin.setMaxValue(59);
            pickerMin.setMinValue(0);
            pickerMin.setValue(minCur%60);

            textMin.setVisibility(View.GONE);
        }
        else
        {
            pickerHour.setValue(0);
            pickerHour.setVisibility(View.GONE);
            textHour.setVisibility(View.GONE);

            pickerMin.setMaxValue(minuteMax);
            pickerMin.setMinValue(0);
            pickerMin.setValue(minCur);
        }

        builder.setView(viewNumPicker);

// Set up the buttons
        builder.setPositiveButton("OK", onOkLisenter);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                //mTagAlertDialog.show();
            }
        });


        builder.show();
    }
    private void displayNoConnectedDeviceDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this.getContext());
        String messageText = getResources().getString(R.string.title_no_device_connected);
        String okText = getResources().getString(R.string.ok_no_device_connected);
        builder.setMessage(messageText)
                .setCancelable(false)
                .setPositiveButton(okText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //updateCalendarListWrapper();
                        //finish();
                    }
                });
        android.app.AlertDialog alert = builder.create();
        alert.show();
    }

    private void saveConfigInt(String configKey, int value) {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this.getContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(configKey, value);
        editor.commit();
    }

    private void saveConfigLong(String configKey, long value)
    {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this.getContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(configKey, value);
        editor.commit();

    }

//    private void sendConfigUpdateMessageLong(String configKey, long value) {
//        if (mPeerId != null && mGoogleApiClient != null) {
//            DataMap config = new DataMap();
//            config.putLong(configKey, value);
//            byte[] rawData = config.toByteArray();
//            Wearable.MessageApi.sendMessage(mGoogleApiClient, mPeerId, TimerUtil.PATH_WITH_FEATURE, rawData);
//
//            // if (Log.isLoggable(TAG, Log.DEBUG)) {
//            Log.d(TAG, "Sent watch face config message: " + configKey + " -> "
//                    + value);
//            //}
//        }
//        final SharedPreferences prefs = PreferenceManager
//                .getDefaultSharedPreferences(this);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putLong(configKey, value);
//        editor.commit();
//
//    }

    private void saveConfigString(String configKey, String value)
    {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this.getContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(configKey, value);
        editor.commit();

    }

//    private void sendConfigUpdateMessageString(String configKey, String value) {
//        if (mPeerId != null && mGoogleApiClient != null) {
//            DataMap config = new DataMap();
//            config.putString(configKey, value);
//            byte[] rawData = config.toByteArray();
//            Wearable.MessageApi.sendMessage(mGoogleApiClient, mPeerId, TimerUtil.PATH_WITH_FEATURE, rawData);
//
//
//            //if (Log.isLoggable(TAG, Log.DEBUG)) {
//            Log.d(TAG, "Sent watch face config message: " + configKey + " -> "
//                    + value);
//            //}
//        }
//
//    }


    private void saveConfigStringArray(String configKey, String[] array) {

        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this.getContext());
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> value_set = new LinkedHashSet<String>(Arrays.asList(array));
        editor.putStringSet(configKey, value_set);
        editor.commit();
    }


    private void saveConfigStringSet(String configKey, Set<String> set)
    {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this.getContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(configKey, set);
        editor.commit();
    }
//    private void sendConfigUpdateMessageDataMapArray(String configKey, ArrayList<DataMap> arrayMap) {
//        DataMap config = new DataMap();
//        config.putDataMapArrayList(configKey, arrayMap);
//        byte[] rawData = config.toByteArray();
//        if (mPeerId != null && mGoogleApiClient != null) {
//            Wearable.MessageApi.sendMessage(mGoogleApiClient, mPeerId, TimerUtil.PATH_WITH_FEATURE, rawData);
//
//            Log.d(TAG, "Sent watch face config message: " + configKey);
//            for (DataMap map : arrayMap)
//            {
//                Log.d(TAG, "[" + map.toString() + "]");
//            }
//        }
//        //String saveThis = Base64.encodeToString(array, Base64.DEFAULT);
//        //byte[] array = Base64.decode(stringFromSharedPrefs, Base64.DEFAULT);
//        final SharedPreferences prefs = PreferenceManager
//                .getDefaultSharedPreferences(this);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putString(configKey, Base64.encodeToString(rawData, Base64.DEFAULT));
//        editor.commit();
//    }

    private void sendCalendarConfigUpdateMessage()
    {
        if (mSelectedCalendarListIdx <0 ||mSelectedCalendarListIdx>=mCalendarList.size()) return;

        saveConfigLong(TimerUtil.KEY_TOMATO_CALENDAR_ID,  mCalendarList.get(mSelectedCalendarListIdx).id);
        saveConfigString(TimerUtil.KEY_TOMATO_CALENDAR_NAME,  mCalendarList.get(mSelectedCalendarListIdx).name);
        saveConfigInt(TimerUtil.KEY_TOMATO_CALENDAR_COLOR,  mCalendarList.get(mSelectedCalendarListIdx).color);
        saveConfigString(TimerUtil.KEY_TOMATO_CALENDAR_ACCOUNT_NAME,  mCalendarList.get(mSelectedCalendarListIdx).accountName);

    }


    private boolean updateEventListByDay(Date date)
    {
        if (mSelectedCalendarListIdx < 0 || mSelectedCalendarListIdx>= mCalendarList.size()) return false;

        String[] projection =
                new String[]{
                        CalendarContract.Events.CALENDAR_ID,//0
                        CalendarContract.Events.TITLE,//1
                        CalendarContract.Events.DESCRIPTION,//2
                        CalendarContract.Events.DTSTART,//3
                        CalendarContract.Events.DTEND//4
                };

        String permission;
        permission = "android.permission.READ_CALENDAR";
        int res = this.getActivity().checkCallingOrSelfPermission(permission);
        if (res == PackageManager.PERMISSION_GRANTED) {

            String selectionClause =
                    "(" + CalendarContract.Events.DTSTART + " >= ? AND " + CalendarContract.Events.DTEND + " <= ?)" + " OR " +
                            "(" + CalendarContract.Events.DTSTART + " >= ? AND " + CalendarContract.Events.ALL_DAY + " = ?)";

            selectionClause = " (" + selectionClause + ") AND (" + CalendarContract.Events.CALENDAR_ID + " = ? )";
            long dtstart,dtend;


            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            dtstart = cal.getTimeInMillis();

            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);

            dtend = cal.getTimeInMillis();

            String[] selectionsArgs = new String[]{"" + dtstart, "" + dtend, "" + dtstart, "1" , ""+mCalendarList.get(mSelectedCalendarListIdx).id };

            Cursor eventCursor = this.getActivity().getContentResolver()
                    .query(
                            CalendarContract.Events.CONTENT_URI,
                            projection,
                            selectionClause,
                            selectionsArgs, null);
            if (eventCursor.moveToFirst()) {
                do {
                    long id = eventCursor.getLong(0);//CALENDAR_ID
                    String title = eventCursor.getString(1);//TITLE
                    String description = eventCursor.getString(2);//DESCRIPTION
                    long eventStart = eventCursor.getLong(3);
                    long eventEnd = eventCursor.getLong(4);

                    cal.setTimeInMillis(eventStart);
                    String strEventStart = DateFormat.format("YY MM dd HH:mm:ss", cal).toString();

                    cal.setTimeInMillis(eventEnd);
                    String strEventEnd = DateFormat.format("YY MM dd HH:mm:ss", cal).toString();

                    //Log.i("MainActivity","event calid:" + id + "  title :" + title + "  des:" + description + "  time:" + strEventStart + " -> " + strEventEnd);

                } while (eventCursor.moveToNext());
            }

        }
        else
            return false;

        return true;
    }


    private void updateCalendarListWrapper() {

        int hasWriteContactsPermission = ContextCompat.checkSelfPermission(this.getContext(),Manifest.permission.READ_CALENDAR);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.getActivity(),new String[] {Manifest.permission.READ_CALENDAR},
                    TimerUtil.REQUEST_CODE_ASK_PERMISSIONS);
            return;
        }
        updateCalendarList();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case TimerUtil.REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    updateCalendarList();
                } else {
                    // Permission Denied
                    Toast.makeText(this.getContext(), "READ_CALENDAR Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean updateCalendarList()
    {
        mCalenderListUpdated = false;
        mCalendarList.clear();

        String[] projection =
                new String[]{
                        CalendarContract.Calendars._ID,
                        CalendarContract.Calendars.NAME,
                        CalendarContract.Calendars.ACCOUNT_NAME,
                        CalendarContract.Calendars.ACCOUNT_TYPE,
                        CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
                        CalendarContract.Calendars.CALENDAR_COLOR};

        String permission;
        permission = "android.permission.READ_CALENDAR";
        int res = this.getActivity().checkCallingOrSelfPermission(permission);
        if (res == PackageManager.PERMISSION_GRANTED) {

            mCalenderListUpdated = true;
            Cursor calCursor =
                    this.getActivity().getContentResolver().
                            query(CalendarContract.Calendars.CONTENT_URI,
                                    projection,
                                    CalendarContract.Calendars.VISIBLE + " = 1 " + "AND " +
                                            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL + " >= " + CalendarContract.Calendars.CAL_ACCESS_OVERRIDE,
                                    null,
                                    CalendarContract.Calendars._ID + " ASC");
            if (calCursor.moveToFirst()) {
                //ArrayList<DataMap> calMapList = new ArrayList<DataMap>();
                JSONArray calJsonArray = new JSONArray();
                do {
                    long id = calCursor.getLong(0);
                    String displayName = calCursor.getString(1);
                    String accName = calCursor.getString(2);
                    //String accType = calCursor.getString(3);
                    String accLevel = calCursor.getString(4);
                    int color = calCursor.getInt(5);
                    //Log.i("MainActivity","id :" + id + "  name :" + displayName + "  accLevel:" + accLevel + "  color:" + Integer.toHexString(color));

                    mCalendarList.add(new CalendarItem(id,displayName,accName,color));
                    //

                    JSONObject calObj = new JSONObject();
                    try {
                        calObj.put(CalendarContract.Calendars._ID, id);
                        calObj.put(CalendarContract.Calendars.NAME, displayName);
                        calObj.put(CalendarContract.Calendars.ACCOUNT_NAME, accName);
                        calObj.put(CalendarContract.Calendars.CALENDAR_COLOR, color);

                        calJsonArray.put(calObj);
                    }catch (org.json.JSONException je)
                    {
                        Log.e(TAG,je.toString());
                    }

                } while (calCursor.moveToNext());

            }

            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(this.getContext());
            long calID = SP.getLong(TimerUtil.KEY_TOMATO_CALENDAR_ID, 0);
            String  calName = SP.getString(TimerUtil.KEY_TOMATO_CALENDAR_NAME, "");
            String  calAccName = SP.getString(TimerUtil.KEY_TOMATO_CALENDAR_ACCOUNT_NAME, "");
            if (mCalendarList.size() !=0)
            {
                for (int c=0;c<mCalendarList.size();c++)
                {
                    if (mSelectedCalendarID == mCalendarList.get(c).id)
                    {
                        mSelectedCalendarListIdx = c;
                        mPrefPomodoroCalendar.setTitle(getResources().getString(R.string.perf_item_pomodoro_calendar) + " : " + mCalendarList.get(c).name);
                        break;
                    }
                }

            }
            if (mCalendarList.size() != 0 && mSelectedCalendarListIdx == -1)
            {
                int c=0;
                for (CalendarItem item : mCalendarList)
                {
                    if (item.id == calID &&
                            item.name.equals(calName)&&
                            item.accountName.equals(calAccName))
                    {
                        mSelectedCalendarListIdx = c;
                        SharedPreferences.Editor editor = SP.edit();
                        editor.putLong(TimerUtil.KEY_TOMATO_CALENDAR_ID,mCalendarList.get(mSelectedCalendarListIdx).id);
                        editor.putString(TimerUtil.KEY_TOMATO_CALENDAR_NAME, mCalendarList.get(mSelectedCalendarListIdx).name);
                        editor.putString(TimerUtil.KEY_TOMATO_CALENDAR_ACCOUNT_NAME, mCalendarList.get(mSelectedCalendarListIdx).accountName);

                        //mAppbar.setBackgroundColor(mCalendarList.get(mCalendarSelected).color);
                        break;
                    }
                }
                if (mSelectedCalendarListIdx == -1)
                {
                    if (mCalendarList.size() == 1) {
                        mSelectedCalendarListIdx = 0;
                    }
                    else if (false)
                    {
                        String[] calNameArray = new String[mCalendarList.size()];
                        c=0;
                        for (CalendarItem item : mCalendarList)
                        {
                            calNameArray[c] = item.name;
                            c++;
                        }

                        new AlertDialog.Builder(ConfigFragment.this.getContext())
                                .setTitle("Please select a calender")
                                .setItems(calNameArray, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mSelectedCalendarListIdx = which;
                                        mSelectedCalendarID = mCalendarList.get(mSelectedCalendarListIdx).id;
                                        sendCalendarConfigUpdateMessage();

                                        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(ConfigFragment.this.getContext());
                                        SharedPreferences.Editor editor = SP.edit();
                                        editor.putLong(TimerUtil.KEY_TOMATO_CALENDAR_ID,mCalendarList.get(mSelectedCalendarListIdx).id);
                                        editor.putString(TimerUtil.KEY_TOMATO_CALENDAR_NAME, mCalendarList.get(mSelectedCalendarListIdx).name);
                                        editor.putString(TimerUtil.KEY_TOMATO_CALENDAR_ACCOUNT_NAME, mCalendarList.get(mSelectedCalendarListIdx).accountName);


                                    }
                                }).show();

                    }
                }

            }

            return (mCalendarList.size() != 0);
        }
        else
            return false;

    }
}
