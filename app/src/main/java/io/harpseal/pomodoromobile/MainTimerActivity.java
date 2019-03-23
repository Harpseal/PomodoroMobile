package io.harpseal.pomodoromobile;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.res.ResourcesCompat;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainTimerActivity extends Activity implements PopupMenu.OnMenuItemClickListener {

    final String TAG = MainTimerActivity.class.getName();
    enum ProgressMode
    {
        NORMAL,WARNING,ERROR,IDLE
    }

    private Button mBtnSettings;
    private Button mBtnPrevious;
    private Button mBtnPlay;
    private Button mBtnStop;
    private Button mBtnNext;

    private TextView mTextCal;
    private TextView mTextCalPrefix;
    private TextView mTextMin;
    private TextView mTextSec;
    private TextView mTextCenter;

    //private TextView mTextStatus;
    private Button mBtnStatusMode;
    private Button mBtnStatusReset;
    private Button mBtnStatusTimer;
    private long mStatusTimer = 0;
    private boolean mStatusTimerSwap = false;


    private boolean mIsForceScrAlwaysOn;
    private boolean mIsScrAlwaysOn;
    private boolean mIsScrAlwaysOnCharging;
    private long mDataTomatoPowerUpdated;
    private ImageView mImageScrAlwaysOnPower;
    private ImageView mImageScrAlwaysOnSun;
    private ImageView mImageScrAlwaysOnLock;
    private LinearLayout mLayoutScrAlwaysOn;
    //private TextView mTextMinAndSec;

    ProgressBar mProgressBar;

    private String mTomatoType = TimerUtil.DEFAULT_TOMATO_TYPE;

    private int mDataTomatoWork = TimerUtil.DEFAULT_TOMATO_WORK;
    private int mDataTomatoRelax = TimerUtil.DEFAULT_TOMATO_RELAX;
    private int mDataTomatoRelaxLong = TimerUtil.DEFAULT_TOMATO_RELAX_LONG;

    private long mDataTomatoTimeInMillisPre = System.currentTimeMillis();
    private long mDataTomatoDateStart = TimerUtil.DEFAULT_TIMER_ZERO;
    private long mDataTomatoDateEnd = TimerUtil.DEFAULT_TIMER_ZERO;

    public String mSelectedCalendarName = "";
    public String mSelectedCalendarAccountName = "";
    public int mSelectedCalendarColor = 0;
    private long mSelectedCalendarID = -1;

    private boolean mActivityShowed = false;
    private boolean mActivityPostDelayRunning = false;

    AlarmManager mAlarmManager = null;
    NotificationManager mNotificationManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_main);

        mBtnSettings = findViewById(R.id.button_settings);
        mBtnPrevious = findViewById(R.id.button_previous);
        mBtnPlay = findViewById(R.id.button_play);
        mBtnStop = findViewById(R.id.button_stop);
        mBtnNext = findViewById(R.id.button_next);

        //registerForContextMenu(mBtnSettings);

        mTextCal = findViewById(R.id.textview_calendar);
        mTextCalPrefix = findViewById(R.id.textview_calendar_prefix);
        mTextMin = findViewById(R.id.textview_time_min);
        mTextSec = findViewById(R.id.textview_time_sec);
        //mTextMinAndSec = findViewById(R.id.textview_time_min_sec);

        //mTextStatus = findViewById(R.id.textview_status_mode);
        mBtnStatusReset = findViewById(R.id.button_status_reset);
        mBtnStatusMode = findViewById(R.id.button_status_mode);
        mBtnStatusTimer = findViewById(R.id.button_status_timer);

        mTextCenter = findViewById(R.id.textview_center_2);

        mIsScrAlwaysOn = false;
        mIsForceScrAlwaysOn = false;
        mIsScrAlwaysOnCharging = false;
        mDataTomatoPowerUpdated = System.currentTimeMillis();
        mImageScrAlwaysOnPower = findViewById(R.id.image_power);
        mImageScrAlwaysOnSun = findViewById(R.id.image_scr_always_on);
        mImageScrAlwaysOnLock = findViewById(R.id.image_scr_always_on_lock);
        mLayoutScrAlwaysOn = findViewById(R.id.layout_scr_always_on);
        mLayoutScrAlwaysOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsForceScrAlwaysOn = !mIsForceScrAlwaysOn;
                //updatePowerStatus();
                updateScrAlwaysOnStateStatus();

                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainTimerActivity.this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(TimerUtil.KEY_TOMATO_SCR_ALWAYS_ON, mIsForceScrAlwaysOn);
                editor.commit();

                Toast.makeText(MainTimerActivity.this,"ScreenAlwaysOn " + (mIsForceScrAlwaysOn?"Enabled.":"Disabled."),Toast.LENGTH_SHORT).show();
            }
        });


        LinearLayout layoutTime = findViewById(R.id.layout_time);
        layoutTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mStatusTimer != 0)
                {
                    mStatusTimer = System.currentTimeMillis();
                    updateStatusTimer();
                }

            }
        });

        mProgressBar = findViewById(R.id.circularProgressBar);
        mProgressBar.setProgress(0);


        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //getActionBar().hide();

//        LayerDrawable progDrawable= (LayerDrawable)progressBar.getProgressDrawable();
//        if (progDrawable != null)
//        {
//            //progDrawable.findDrawableByLayerId(R.)
//            LayerDrawable shape = (LayerDrawable) getResources().getDrawable(R.drawable.progressbar);
//            shape.setColor(Color.Black); // changing to black color
//        }


        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View view = findViewById(android.R.id.content);
//            int flags = view.getSystemUiVisibility();
//            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
//            view.setSystemUiVisibility(flags);
//            this.getWindow().setStatusBarColor(Color.WHITE);
            int flags = view.getSystemUiVisibility();
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            this.getWindow().setStatusBarColor(0xFF000000);
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
            w.setStatusBarColor(0xFF000000);
        }
        */
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_options, menu);
//
//        //Hard code workaround..... @_@
//        MenuItem item;
//        SpannableString s;
//        item = menu.getItem(0);
//        s = new SpannableString(getString(R.string.option_menu_settings_text));
//        s.setSpan(new ForegroundColorSpan(Color.GRAY), 0, s.length(), 0);
//        item.setTitle(s);
//
//        item = menu.getItem(1);
//        s = new SpannableString(getString(R.string.option_menu_hide_statusbar_text));
//        s.setSpan(new ForegroundColorSpan(Color.GRAY), 0, s.length(), 0);
//        item.setTitle(s);
//
//        return true;
//    }



    private boolean mIsShowStatusBar = true;
    private boolean handleItemSelected(int id)
    {
        switch (id)
        {
            case R.id.option_menu_settings:
                mIsShowChildActivity = true;

                Intent myIntent = new Intent(MainTimerActivity.this, ConfigActivity.class);
                //myIntent.putExtra("key", value); //Optional parameters
                MainTimerActivity.this.startActivity(myIntent);
                return true;
            case R.id.option_menu_hide_statusbar:
                mIsShowStatusBar = !mIsShowStatusBar;
                if (mIsShowStatusBar)
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                else
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

                return true;
        }
        return false;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (handleItemSelected(item.getItemId()))
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (handleItemSelected(item.getItemId()))
            return true;
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return handleItemSelected(item.getItemId());
    }

    private void updateTimer(boolean isUpdateProgressBar, boolean enableProgressBarAnimation)
    {

        Long timeInMillis = System.currentTimeMillis();
        final float progressBase = 100;
        float progressPre = mProgressBar.getProgress();
        float progressCur = progressPre;
        long timeSecTotal = 0;
        if (mDataTomatoDateStart == 0 || mDataTomatoDateEnd == 0 || mDataTomatoDateEnd == mDataTomatoDateStart)
        {
//            time_min = mDataTomatoWork % 60;
//            time_sec = mDataTomatoWork / 60;


            Date dateNow = new Date(timeInMillis);
            Calendar calendar = Calendar.getInstance(); // creates a new calendar instance
            calendar.setTime(dateNow);   // assigns calendar to given date
            int timeSec = calendar.get(Calendar.SECOND);
            int timeMin = calendar.get(Calendar.MINUTE);
            timeSecTotal = calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60 + timeMin * 60 + timeSec; // gets hour in 24h format

            changeProgressBarMode(ProgressMode.IDLE);
            progressCur = ((float)timeSec*100.f)/59.f;
            if (progressCur >=100.f)
                progressCur = 100.f;
            if ((timeMin & 1) != 0)
                progressCur = 100.f - progressCur;

            updateTimerText((int)timeSecTotal,true);
            if (mStatusTimer != 0)
            {
                int timeSecStatus = (int)((System.currentTimeMillis() - mStatusTimer)/1000);
                updateStatusTimerText(timeSecStatus);
            }
        }
        else
        {
            long time_min = 0,time_sec = 0;
            if (timeInMillis < mDataTomatoDateStart) timeInMillis = mDataTomatoDateStart;

            timeSecTotal = (mDataTomatoDateEnd - timeInMillis) / 1000;
            if (timeInMillis>=mDataTomatoDateEnd) {
                if (mDataTomatoTimeInMillisPre < mDataTomatoDateEnd) {
                    if (mTomatoType.equals(TimerUtil.KEY_TOMATO_WORK))
                        changeProgressBarMode(ProgressMode.WARNING);
                    else
                        changeProgressBarMode(ProgressMode.ERROR);
                }
                progressCur = 100.f;
                updateModeText("","(" + getResources().getString(R.string.text_timer_mode_out_of_time) + ")");
            }
            else
            {
                long diffTime = (mDataTomatoDateEnd - timeInMillis)/1000;
//                if (diffTime < 1000)
//                    progressCur = 0;
//                else {
//                    long totalTime = mDataTomatoDateEnd - mDataTomatoDateStart;
//                    progressCur = (int) ((diffTime * 100) / (totalTime));
//                }
                long totalTime = (mDataTomatoDateEnd - mDataTomatoDateStart)/1000;
                progressCur = (((float)diffTime * 100.f) / ((float)totalTime));
            }

            if (progressCur>100) progressCur = 100;
            if (timeSecTotal<0) timeSecTotal = -timeSecTotal;

            int timeSecStatus = (int)((System.currentTimeMillis() - mStatusTimer)/1000);
            if (mStatusTimerSwap) {
                updateTimerText(timeSecStatus,false);
                updateStatusTimerText((int)timeSecTotal);
            }
            else {
                updateTimerText((int)timeSecTotal,false);
                updateStatusTimerText(timeSecStatus);
            }
        }

        progressCur = progressCur*progressBase;

        float progressDiff = Math.abs(progressPre - progressCur);

        if (isUpdateProgressBar && progressCur != progressPre)
        {
            if ((progressDiff < 98*progressBase || progressDiff>=progressBase) && enableProgressBarAnimation)
            {
                ObjectAnimator animation = ObjectAnimator.ofInt (mProgressBar, "progress", (int)progressPre, (int)progressCur); // see this max value coming back here, we animale towards that value
                animation.setDuration (800); //in milliseconds
                animation.setInterpolator (new DecelerateInterpolator());
                animation.start ();
            }
            else
                mProgressBar.setProgress((int)progressCur);
        }

        mDataTomatoTimeInMillisPre = timeInMillis;

    }

    private void updatePowerStatus()
    {
        boolean isScrAlwaysOnChargingPre = mIsScrAlwaysOnCharging;
        mIsScrAlwaysOnCharging = isPowerConnected(this);
        Log.d(TAG,"isPowerConnected " + mIsScrAlwaysOnCharging);
//        if (isScreenOn) {
//            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//            //mImageScrAlwaysOnPower.setVisibility(View.VISIBLE);
//        }
//        else {
//            mIsScrAlwaysOnCharging
//            //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//            //mImageScrAlwaysOnPower.setVisibility(View.INVISIBLE);
//        }
        mDataTomatoPowerUpdated = System.currentTimeMillis();
        if (isScrAlwaysOnChargingPre != mIsScrAlwaysOnCharging)
            updateScrAlwaysOnStateStatus();
    }

    private void updateScrAlwaysOnStateStatus()
    {
        mImageScrAlwaysOnPower.setVisibility(mIsScrAlwaysOnCharging?View.VISIBLE:View.GONE);
        //mImageScrAlwaysOnLock.setVisibility(mIsForceScrAlwaysOn?View.VISIBLE:View.GONE);
        mImageScrAlwaysOnLock.setVisibility(View.GONE);
        mImageScrAlwaysOnPower.setVisibility(View.GONE);

        if (mIsScrAlwaysOnCharging || mIsForceScrAlwaysOn)
        {
            if (!mIsScrAlwaysOn)
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            mImageScrAlwaysOnSun.setAlpha(0.6f);
            mIsScrAlwaysOn = true;
        }
        else
        {
            if (mIsScrAlwaysOn)
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            mImageScrAlwaysOnSun.setAlpha(0.2f);
            mIsScrAlwaysOn = false;
        }
    }

    private void changeProgressBarMode(ProgressMode mode)
    {
        Drawable new_drawable = null;
        switch (mode)
        {
            case ERROR:
                new_drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.progressbar_red, null);
                mTextCalPrefix.setTextColor(0xFFF44336);
                break;
            case WARNING:
                new_drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.progressbar_yellow, null);
                mTextCalPrefix.setTextColor(0xFFFFEB3B);
                break;
            case IDLE:
                new_drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.progressbar_idle, null);
                mTextCalPrefix.setTextColor(0xFFFFFFFF);
                break;
            case NORMAL:
            default:
                new_drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.progressbar, null);
                mTextCalPrefix.setTextColor(mSelectedCalendarID!=-1?mSelectedCalendarColor:0xFF009688);
        }
        if (new_drawable != null)
        {
            mProgressBar.setProgressDrawable(new_drawable);
        }
    }

    private static final int SEND_EVENT=123;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG,"onActivityResult "+requestCode + " " + resultCode + " " + (data!=null ? data.toString(): "null"));
        //data.getExtras().getString("B")
        if(resultCode == Activity.RESULT_CANCELED) return;
        switch(requestCode){
            case SEND_EVENT:
                onButtonClick(mBtnPrevious);
                break;

        }
    }

    private void updateStatusTimer()
    {
        if (mStatusTimer == 0)
        {
            if (mBtnStatusMode.getVisibility() != View.VISIBLE) {
                mStatusTimerSwap = false;
                mBtnStatusReset.setVisibility(View.GONE);
                mBtnStatusMode.setVisibility(View.VISIBLE);
                mBtnStatusTimer.setVisibility(View.GONE);
            }
        }
        else
        {

            if (mBtnStatusTimer.getVisibility() != View.VISIBLE) {
                mStatusTimerSwap = false;
                mBtnStatusReset.setVisibility(View.VISIBLE);
                mBtnStatusMode.setVisibility(View.GONE);
                mBtnStatusTimer.setVisibility(View.VISIBLE);
            }
            int timeDiffSec = (int)((System.currentTimeMillis() - mStatusTimer)/1000);
            if (mStatusTimerSwap)
                updateTimerText(timeDiffSec,false);
            else
                updateStatusTimerText(timeDiffSec);
        }
    }

    private void updateTimerText(int timeSec,boolean isShowInMin)
    {
        if ((timeSec & 1) != 0)
            mTextCenter.setAlpha(0.5f);
        else
            mTextCenter.setAlpha(1.0f);

        int timeHigh;
        int timeLow = isShowInMin?timeSec/60:timeSec;

        if (timeLow>999*60+59)
        {
            timeHigh = 999;
            timeLow = 59;
        }
        else {
            timeHigh = timeLow / 60;
            timeLow = timeLow % 60;
        }

        mTextMin.setText(String.format("%2d", timeHigh));
        mTextSec.setText(String.format("%02d", timeLow));
    }

    private void updateStatusTimerText(int timeSec)
    {

        if (timeSec > (99*60 + 59))//max 1 hour
            timeSec = (99*60 + 59);

        String timeText = String.format("%2d", timeSec/60) + ((timeSec&1)!=0?" ":":") + String.format("%02d", timeSec%60);
        mBtnStatusTimer.setText(timeText);
    }

    public void onButtonClick(View v)
    {
        if (v == mBtnStatusReset)
        {
            mStatusTimer = 0;
            updateStatusTimer();
        }
        else if (v == mBtnStatusMode)
        {
            if (mStatusTimer != 0)
                mStatusTimerSwap = !mStatusTimerSwap;
            else
                mStatusTimer = System.currentTimeMillis();
            updateStatusTimer();
        }
        else if (v == mBtnStatusTimer)
        {
            if (mStatusTimer != 0)
                mStatusTimerSwap = !mStatusTimerSwap;
            else
                mStatusTimer = System.currentTimeMillis();
            updateStatusTimer();
        }
        else if (v == mBtnSettings)
        {
            PopupMenu popup = new PopupMenu(this, v);
            popup.setOnMenuItemClickListener(this);
            popup.inflate(R.menu.menu_options);
            popup.show();
        }
        else if (v == mBtnPlay && false)//
        {
            mIsShowChildActivity = true;
            Long timeInMillis = System.currentTimeMillis();
            Intent i = new Intent(getApplicationContext(), TomatoBuilderActivity.class);
            i.putExtra(TimerUtil.KEY_TOMATO_DATE_START,timeInMillis);
            i.putExtra(TimerUtil.KEY_TOMATO_DATE_END,timeInMillis + 30*60*1000);
            i.putExtra(TimerUtil.KEY_TOMATO_CALENDAR_COLOR,mSelectedCalendarColor);
            i.putExtra(TimerUtil.KEY_TOMATO_CALENDAR_NAME,mSelectedCalendarName);
            i.putExtra(TimerUtil.KEY_TOMATO_CALENDAR_ID, mSelectedCalendarID);
            //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(i,SEND_EVENT);
        }
        else
        {
            Long timeInMillis = System.currentTimeMillis();
            boolean isModifiedConfig = false;


            long dtstart,dtend;
            dtstart = 0;
            dtend = 0;

            if (mTomatoType.equals(TimerUtil.KEY_TOMATO_WORK) && mDataTomatoDateEnd!=0 && timeInMillis>=mDataTomatoDateEnd)
            {
                dtstart = mDataTomatoDateStart;
                dtend = timeInMillis;
            }

            final int startDelay = 1100;
            if (v == mBtnStop)
            {
                mDataTomatoDateStart = mDataTomatoDateEnd = 0;
                mTomatoType = TimerUtil.KEY_TOMATO_IDLE;
                isModifiedConfig = true;

                mStatusTimer = 0;
                updateStatusTimer();

                mBtnPlay.setVisibility(View.VISIBLE);
                mBtnStop.setVisibility(View.GONE);
                mBtnPrevious.setVisibility(View.GONE);
                mBtnNext.setVisibility(View.GONE);
                changeProgressBarMode(ProgressMode.IDLE);

            }
            else if (v == mBtnPlay && (mDataTomatoDateStart == 0 || mDataTomatoDateEnd == 0 || mTomatoType.equals(TimerUtil.KEY_TOMATO_IDLE)))
            {
                changeProgressBarMode(ProgressMode.NORMAL);
                mDataTomatoDateStart = timeInMillis + startDelay;
                mDataTomatoDateEnd = mDataTomatoDateStart+ (mDataTomatoWork) *1000;
                mTomatoType = TimerUtil.KEY_TOMATO_WORK;
                isModifiedConfig = true;
                mBtnPlay.setVisibility(View.GONE);
                mBtnStop.setVisibility(View.VISIBLE);
                mBtnPrevious.setVisibility(View.VISIBLE);
                mBtnNext.setVisibility(View.VISIBLE);

            }
            else if (v == mBtnPrevious)
            {
                long diffPre = (mDataTomatoDateEnd - mDataTomatoDateStart)/1000;
                if (!mTomatoType.equals(TimerUtil.KEY_TOMATO_IDLE) && diffPre > 0) {
                    if (mTomatoType.equals(TimerUtil.KEY_TOMATO_WORK))
                        changeProgressBarMode(ProgressMode.NORMAL);
                    else
                        changeProgressBarMode(ProgressMode.WARNING);
                    mDataTomatoDateStart = timeInMillis + startDelay;
                    mDataTomatoDateEnd = mDataTomatoDateStart + (diffPre) * 1000;
                    isModifiedConfig = true;
                }
            }
            else if (v == mBtnNext)//out of time
            {
                if (mTomatoType.equals(TimerUtil.KEY_TOMATO_WORK))
                {
                    changeProgressBarMode(ProgressMode.WARNING);
                    mDataTomatoDateStart = timeInMillis + startDelay;
                    mDataTomatoDateEnd = mDataTomatoDateStart+ (mDataTomatoRelax) *1000;
                    mTomatoType = TimerUtil.KEY_TOMATO_RELAX;
                    isModifiedConfig = true;
                }
                else // relax , long relax
                {
                    changeProgressBarMode(ProgressMode.NORMAL);
                    mDataTomatoDateStart = timeInMillis + startDelay;
                    mDataTomatoDateEnd = mDataTomatoDateStart+ (mDataTomatoWork) *1000;
                    mTomatoType = TimerUtil.KEY_TOMATO_WORK;
                    isModifiedConfig = true;
                }
            }
//            else
//            {
//                if (mTouchCoordinateY<mTouchHeight/2)
//                {
//                    mDataTomatoDateStart = timeInMillis;
//                    mDataTomatoDateEnd = mDataTomatoDateStart+ mDataTomatoWork *1000;
//                    mTomatoType = TimerUtil.KEY_TOMATO_WORK;
//                    isModifiedConfig = true;
//                }
//                else if (mTomatoType.equals(TimerUtil.KEY_TOMATO_WORK))
//                {
//                    mDataTomatoDateStart = mDataTomatoDateEnd = 0;
//                    mTomatoType = TimerUtil.KEY_TOMATO_IDLE;
//                    isModifiedConfig = true;
//                }
//                //canvas.drawText("Start to work!", centerX, centerY-height/6, mInteractionTextPaint);
//                //canvas.drawText("Cancel", centerX-width/4, centerY+height/4,mInteractionTextPaint);
//                //canvas.drawText("Idle", centerX+width/4, centerY+height/4,mInteractionTextPaint);
//            }
            if (isModifiedConfig)
            {

                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(TimerUtil.KEY_TOMATO_DATE_START, mDataTomatoDateStart);
                editor.putLong(TimerUtil.KEY_TOMATO_DATE_END, mDataTomatoDateEnd);
                editor.putString(TimerUtil.KEY_TOMATO_TYPE,mTomatoType);
                editor.commit();
                updateModeText("","");
                updateTimer(false, false);


                if (mDataTomatoDateEnd != 0 && !mTomatoType.equals(TimerUtil.KEY_TOMATO_IDLE)) {
                    Intent intent = new Intent(this, AlarmReceiver.class);
                    intent.putExtra("msg",
                            mTomatoType.equals(TimerUtil.KEY_TOMATO_WORK) ? "play_tomato_warning" : "play_tomato_alarm");

                    PendingIntent pi = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                    //AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    mAlarmManager.set(AlarmManager.RTC_WAKEUP, mDataTomatoDateEnd, pi);

                    //if (dtstart==0 || dtend<dtstart)
                    //    updateNotification();
                }
                else
                {
                    Intent updateServiceIntent = new Intent(this, AlarmReceiver.class);
                    //PendingIntent pendingUpdateIntent = PendingIntent.getService(this, 0, updateServiceIntent, 0);
                    PendingIntent pendingUpdateIntent = PendingIntent.getBroadcast(this, 1, updateServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);


                    // Cancel alarms
                    try {
                        mAlarmManager.cancel(pendingUpdateIntent);
                        Log.d(TAG, "AlarmManager update was canceled. ");
                    } catch (Exception e) {
                        Log.e(TAG, "AlarmManager update was not canceled. " + e.toString());
                    }
                    mNotificationManager.cancelAll();
                }



            }

            if (mSelectedCalendarID != -1 && dtstart!=0 && dtend>=dtstart)
            {
                mIsShowChildActivity = true;
                Intent i = new Intent(getApplicationContext(), TomatoBuilderActivity.class);
                i.putExtra(TimerUtil.KEY_TOMATO_DATE_START,dtstart);
                i.putExtra(TimerUtil.KEY_TOMATO_DATE_END,dtend);
                i.putExtra(TimerUtil.KEY_TOMATO_CALENDAR_COLOR,mSelectedCalendarColor);
                i.putExtra(TimerUtil.KEY_TOMATO_CALENDAR_NAME,mSelectedCalendarName);
                i.putExtra(TimerUtil.KEY_TOMATO_CALENDAR_ID, mSelectedCalendarID);
                //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(i,SEND_EVENT);

//                Intent i = new Intent(getApplicationContext(), TomatoBuilderActivity.class);
//                i.putExtra(CalendarContract.Events.DTSTART,dtstart);
//                i.putExtra(CalendarContract.Events.DTEND,timeInMillis);
//                i.putExtra(CalendarContract.Events.CALENDAR_ID,mCalendarID);
//                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(i);
            }

        }
    }

    public static boolean isPowerConnected(Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        //return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
        return plugged == BatteryManager.BATTERY_PLUGGED_AC ||
                plugged == BatteryManager.BATTERY_PLUGGED_USB ||
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS);

    }

    private boolean mIsShowChildActivity = false;
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");

        mActivityShowed = true;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSelectedCalendarName = prefs.getString(TimerUtil.KEY_TOMATO_CALENDAR_NAME,"");
        mSelectedCalendarAccountName = prefs.getString(TimerUtil.KEY_TOMATO_CALENDAR_ACCOUNT_NAME,"");
        mSelectedCalendarColor = prefs.getInt(TimerUtil.KEY_TOMATO_CALENDAR_COLOR,0);
        mSelectedCalendarID = prefs.getLong(TimerUtil.KEY_TOMATO_CALENDAR_ID,-1);

        mDataTomatoWork = prefs.getInt(TimerUtil.KEY_TOMATO_WORK,mDataTomatoWork);
        mDataTomatoRelax = prefs.getInt(TimerUtil.KEY_TOMATO_RELAX,mDataTomatoRelax);
        mDataTomatoRelaxLong = prefs.getInt(TimerUtil.KEY_TOMATO_RELAX_LONG,mDataTomatoRelaxLong);

        mDataTomatoDateStart = prefs.getLong(TimerUtil.KEY_TOMATO_DATE_START, mDataTomatoDateStart);
        mDataTomatoDateEnd = prefs.getLong(TimerUtil.KEY_TOMATO_DATE_END, mDataTomatoDateEnd);

        mIsForceScrAlwaysOn = prefs.getBoolean(TimerUtil.KEY_TOMATO_SCR_ALWAYS_ON,mIsForceScrAlwaysOn);
        updateScrAlwaysOnStateStatus();

        mTomatoType = prefs.getString(TimerUtil.KEY_TOMATO_TYPE,mTomatoType);

        mStatusTimer = prefs.getLong(TimerUtil.KEY_TIMER1, mStatusTimer);

        if (mTomatoType.equals(TimerUtil.KEY_TOMATO_WORK))
            changeProgressBarMode(ProgressMode.NORMAL);
        else if (mTomatoType.equals(TimerUtil.KEY_TOMATO_IDLE))
            changeProgressBarMode(ProgressMode.IDLE);
        else // relax , long relax
            changeProgressBarMode(ProgressMode.WARNING);

        if (mDataTomatoDateStart == 0 || mDataTomatoDateEnd == 0 || mTomatoType.equals(TimerUtil.KEY_TOMATO_IDLE))
        {
            mBtnPlay.setVisibility(View.VISIBLE);
            mBtnStop.setVisibility(View.GONE);
            mBtnPrevious.setVisibility(View.GONE);
            mBtnNext.setVisibility(View.GONE);

            mDataTomatoDateStart = 0;
            mDataTomatoDateEnd = 0;
        }
        else
        {
            mDataTomatoTimeInMillisPre = mDataTomatoDateStart;
            mBtnPlay.setVisibility(View.GONE);
            mBtnStop.setVisibility(View.VISIBLE);
            mBtnPrevious.setVisibility(View.VISIBLE);
            mBtnNext.setVisibility(View.VISIBLE);
        }

        if (mSelectedCalendarID != -1 && mSelectedCalendarName.length()!=0) {
            mTextCal.setText(mSelectedCalendarName);
        }
        else
        {
            mTextCal.setText("--");
        }
        updateModeText("","");
        //updateTimer(true, false);
        //updateTimer(false, false);

//        if (!mActivityPostDelayRunning) {
//            mActivityPostDelayRunning = true;
//            final View rootView = getWindow().getDecorView().getRootView();
//            rootView.postDelayed(new Runnable() {
//                public void run() {
//                    if (mActivityShowed) {
//                        updateTimer(true, true);
//                        rootView.postDelayed(this, 1000);
//                    }
//                    else {
//                        mActivityPostDelayRunning = false;
//                        Log.d(TAG,"mActivityPostDelayRunning = false");
//                    }
//                }
//            }, 1000);
//        }
        if (mTimer != null)
        {
            mTimer.cancel();
            mTimer.purge();
            mTimer	= null;
        }
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            public void run() {
                mTimerHandler.obtainMessage(1).sendToTarget();
            }

        }, 0, 1000);

        mIsShowChildActivity = false;
        mNotificationManager.cancelAll();
    }



    private Timer mTimer = null;
    private Handler mTimerHandler = new Handler() {
        public void handleMessage(Message msg) {
            updateTimer(true, true);
        }
    };

    @Override
    public void onPause() {
        Log.d(TAG,"onPause");
        super.onPause();
        mActivityShowed = false;
        mIsScrAlwaysOn = false;
        mIsScrAlwaysOnCharging = false;

        if (mTimer != null)
        {
            mTimer.cancel();
            mTimer.purge();
            mTimer	= null;
        }

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainTimerActivity.this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(TimerUtil.KEY_TIMER1, mStatusTimer);
        editor.commit();

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Toast.makeText(this,"mIsShowChildActivity " + mIsShowChildActivity, Toast.LENGTH_SHORT).show();
        if (!mIsShowChildActivity && mTomatoType != TimerUtil.KEY_TOMATO_IDLE)
            updateNotification();
    }
    @Override
    public void onStop() {
        super.onStop();

    }

    private boolean isNotificationVisible(int checkid) {
        StatusBarNotification[] notifications = mNotificationManager.getActiveNotifications();
        for (StatusBarNotification notification : notifications) {
            if (notification.getId() == checkid) {
                return true;
            }
        }
        return false;
    }

    private void updateNotification()
    {
        if (mDataTomatoDateStart == mDataTomatoDateEnd) return;

        int statTextID =
                mTomatoType.equals(TimerUtil.KEY_TOMATO_WORK)? R.string.text_timer_mode_work :
                mTomatoType.equals(TimerUtil.KEY_TOMATO_RELAX) ? R.string.text_timer_mode_relax :
                mTomatoType.equals(TimerUtil.KEY_TOMATO_RELAX_LONG) ? R.string.text_timer_mode_relax : R.string.text_timer_mode_idle;

        Long timeInMillis = System.currentTimeMillis();
        SimpleDateFormat timeTxtFormat = new SimpleDateFormat("HH:mm:ss");
        String noTitle, noText;
        int iconID;
        if (mDataTomatoDateEnd<timeInMillis)
        {
            iconID = R.mipmap.icon_tomato_color;
            noTitle = getResources().getString(statTextID) + "(" + getResources().getString(R.string.text_timer_mode_out_of_time) + ")";
            noText = getResources().getString(R.string.text_builder_time_end) + " @ " + timeTxtFormat.format(new Date(mDataTomatoDateEnd));
        }
        else {
            iconID = R.mipmap.icon_tomato_color_light;
            noTitle = getResources().getString(statTextID);
            noText = getResources().getString(R.string.text_builder_time_start) + " @ " + timeTxtFormat.format(new Date(mDataTomatoDateStart)) + "  ⇨  " +
                    getResources().getString(R.string.text_builder_time_end) + " @ " + timeTxtFormat.format(new Date(mDataTomatoDateEnd));
        }


        updateNotification(noTitle,noText,iconID,false,false);
    }

    private void updateNotification(String title, String text, int iconID, boolean isVirbrate, boolean isSound)
    {
        Intent timerIntent = new Intent(this, io.harpseal.pomodoromobile.MainTimerActivity.class);
        timerIntent.setAction(Intent.ACTION_MAIN);
        timerIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                timerIntent, 0);

//        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.custom_notification);
//        contentView.setImageViewResource(R.id.image, iconID);
//        contentView.setTextViewText(R.id.title, noTitle);
//        contentView.setTextViewText(R.id.text, noText);

        Notification.Builder notificationBuilder = new Notification.Builder(this)
                //.setPriority(NotificationManager.IMPORTANCE_HIGH)
                //.setCustomContentView(contentView)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(iconID)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),iconID))

                .setPriority(Notification.PRIORITY_HIGH);

        int settings = Notification.DEFAULT_LIGHTS;
        if (isVirbrate)
            settings |= Notification.DEFAULT_VIBRATE;
        else
            notificationBuilder.setVibrate(new long[]{0L}); // Passing null here silently fails
        if (isSound)
            settings |= Notification.DEFAULT_SOUND;
        notificationBuilder.setDefaults(settings);


        mNotificationManager.cancel(TimerUtil.ID_TOMATO_NOTIFICATION_ID, 0);
        mNotificationManager.notify(
                TimerUtil.ID_TOMATO_NOTIFICATION_ID, 1,  // <-- Place your notification id here
                notificationBuilder.build());
    }


    private void updateModeText(String prefix, String postfix){
        switch (mTomatoType)
        {

            case TimerUtil.KEY_TOMATO_WORK:
                mTextCal.setVisibility(View.VISIBLE);
                mTextCalPrefix.setVisibility(View.VISIBLE);
                mBtnStatusMode.setText(prefix +
                        getResources().getString(R.string.text_timer_mode_work) + postfix);
                break;
            case TimerUtil.KEY_TOMATO_RELAX:
                mTextCal.setVisibility(View.VISIBLE);
                mTextCalPrefix.setVisibility(View.VISIBLE);
                mBtnStatusMode.setText(prefix + getResources().getString(R.string.text_timer_mode_relax) + postfix);
                break;
            case TimerUtil.KEY_TOMATO_RELAX_LONG:
                mTextCal.setVisibility(View.VISIBLE);
                mTextCalPrefix.setVisibility(View.VISIBLE);
                mBtnStatusMode.setText(prefix + getResources().getString(R.string.text_timer_mode_relax_long) + postfix);
                break;

            case TimerUtil.KEY_TOMATO_IDLE:
            default:
                mTextCal.setVisibility(View.GONE);
                mTextCalPrefix.setVisibility(View.GONE);
                //mTextStatus.setText(prefix + getResources().getString(R.string.text_timer_mode_idle)+ postfix);
                mBtnStatusMode.setText("         ");
                break;
        }
    }
}
