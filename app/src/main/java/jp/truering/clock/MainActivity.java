package jp.truering.clock;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {

    private String mMon = "";
    private String mDay = "";
    private String mHour = "";
    private String mMin = "";
    private String mSec = "";

    final DateFormat dfMon = new SimpleDateFormat("MM");
    final DateFormat dfDay = new SimpleDateFormat("dd");
    final DateFormat dfHour = new SimpleDateFormat("HH");
    final DateFormat dfMin = new SimpleDateFormat("mm");
    final DateFormat dfSec = new SimpleDateFormat("ss");

    final Object mLock = new Object();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        // レイアウト調整
        settingsLayout();

        // 日時更新スレッド
        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    // 時刻取得(文字列)
                    synchronized (mLock) {
                        // NTPで取得した時刻に調整
                        Date date = new Date(System.currentTimeMillis() + mDiffWithNtp);

                        // 表示用の文字列生成
                        mMon = dfMon.format(date);
                        mDay = dfDay.format(date);
                        mHour = dfHour.format(date);
                        mMin = dfMin.format(date);
                        mSec = dfSec.format(date);
                    }

                    // 表示している時刻を更新
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            updateTime();
                        }
                    });

                    // スリープ
                    try{
                        Thread.sleep(300);
                    }
                    catch(InterruptedException e) {}
                }
            }
        }).start();

        // 時刻更新スレッド
        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    // NTPによる時刻取得
                    syncNtp();

                    // スリープ
                    try{
                        //Thread.sleep(3000);             // 3秒 for TEST
                        Thread.sleep(3600000);        // 1時間(60 * 60 * 1000)
                    }
                    catch(InterruptedException e) {}
                }
            }
        }).start();
    }

    long mDiffWithNtp = 0;      // Android端末のシステム時間とNTPによって取得した時間の差

    /**
     * NTPによる時刻取得
     */
    private void syncNtp() {
        Log.e("syncNtp", "Sync date");
        SntpClient sntpClient = new SntpClient();

        boolean ret = false;
        if ((ret = sntpClient.requestTime("ntp.nict.jp", 3000))) {
            //mDiffWithNtp = sntpClient.getNtpTime() - System.currentTimeMillis();
            // SntpClientクラス内のコメントを見ると、↓のやり方が正しい模様。
            long now = sntpClient.getNtpTime() + SystemClock.elapsedRealtime() - sntpClient.getNtpTimeReference();
            mDiffWithNtp = now - System.currentTimeMillis();
            Log.e("syncNtp", "Diff : [" + mDiffWithNtp + "]");
        }
        //Log.e("syncNtp", "Sync date : ret [" + ret + "]");
    }

    /**
     * 表示時刻更新
     */
    private void updateTime() {
        synchronized (mLock) {
            setDateText(R.id.txtMon, mMon);
            setDateText(R.id.txtDay, mDay);
            setDateText(R.id.txtHour, mHour);
            setDateText(R.id.txtMin, mMin);
            setDateText(R.id.txtSec, mSec);
        }
    }

    /**
     * TextViewへの文字列設定
     * @param id        リソースID
     * @param value     設定文字列
     */
    private void setDateText(int id, String value) {
        TextView txtView = (TextView) findViewById(id);
        txtView.setText(value);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private void settingsLayout() {
        Configuration config = getResources().getConfiguration();

        // 画面の向き(縦横)に応じてレイアウトを変更する。(現在は無効化)
//        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            settingsLayout_Landscape();
//        } else {
            settingsLayout_Portrait();
//        }
    }

//    private void settingsLayout_Landscape() {
//        ////////////////////////////////////////////////////////////////////////////////
//        // 画面情報取得
//        ////////////////////////////////////////////////////////////////////////////////
//
//        WindowManager windowManager = getWindowManager();
//        Display display = windowManager.getDefaultDisplay();
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        display.getMetrics(displayMetrics);
//
//        int fullW = displayMetrics.widthPixels;
//        int fullH = displayMetrics.heightPixels;
//
//        int halfW = fullW / 2;
//        int halfH = fullH / 2;
//
//        TextView txtView;
//        ViewGroup.LayoutParams layout;
//        ViewGroup.MarginLayoutParams marginLayout;
//
//        ////////////////////////////////////////////////////////////////////////////////
//        // 月
//        ////////////////////////////////////////////////////////////////////////////////
//
//        txtView = (TextView)findViewById(R.id.txtMon);
//        layout = txtView.getLayoutParams();
//        marginLayout = (ViewGroup.MarginLayoutParams)layout;
//        txtView.setTextSize(TypedValue.COMPLEX_UNIT_PX, halfH);
////        marginLayout.topMargin = (int)(-(halfH / 3.5f));
////        marginLayout.leftMargin = -(halfH / 10);
//        txtView.setLayoutParams(layout);
//
//        ////////////////////////////////////////////////////////////////////////////////
//        // セパレータ(日)
//        ////////////////////////////////////////////////////////////////////////////////
//
//        txtView = (TextView)findViewById(R.id.txtSepDay);
//        layout = txtView.getLayoutParams();
//        marginLayout = (ViewGroup.MarginLayoutParams)layout;
//        txtView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fullH * 0.65f);
////        marginLayout.topMargin = (int)(-(txtView.getTextSize() / 6));
////        marginLayout.rightMargin = (int)(-(txtView.getTextSize() / 5));
//        txtView.setLayoutParams(layout);
//
//        ////////////////////////////////////////////////////////////////////////////////
//        // 日
//        ////////////////////////////////////////////////////////////////////////////////
//
//        txtView = (TextView)findViewById(R.id.txtDay);
//        layout = txtView.getLayoutParams();
//        marginLayout = (ViewGroup.MarginLayoutParams)layout;
//        txtView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fullH * 0.6f);
////        marginLayout.topMargin = (halfH / 10);
////        marginLayout.rightMargin = -(halfH / 10);
//        txtView.setLayoutParams(layout);
//
//        ////////////////////////////////////////////////////////////////////////////////
//        // 時
//        ////////////////////////////////////////////////////////////////////////////////
//
//        txtView = (TextView)findViewById(R.id.txtHour);
//        layout = txtView.getLayoutParams();
//        marginLayout = (ViewGroup.MarginLayoutParams)layout;
//        txtView.setTextSize(TypedValue.COMPLEX_UNIT_PX, halfH * 0.8f);
////        marginLayout.topMargin = halfH - (int)(txtView.getTextSize() / 2f) - (int)(txtView.getTextSize() / 20f);
////        marginLayout.leftMargin = -(halfH / 12);
//        txtView.setLayoutParams(layout);
//
//        ////////////////////////////////////////////////////////////////////////////////
//        // セパレータ(時)
//        ////////////////////////////////////////////////////////////////////////////////
//
//        txtView = (TextView)findViewById(R.id.txtSepTime);
//        layout = txtView.getLayoutParams();
//        marginLayout = (ViewGroup.MarginLayoutParams)layout;
//        txtView.setTextSize(TypedValue.COMPLEX_UNIT_PX, halfH * 0.65f);
////        marginLayout.topMargin = halfH - (int)(txtView.getTextSize() / 3f);
//        txtView.setLayoutParams(layout);
//
//        ////////////////////////////////////////////////////////////////////////////////
//        // 分
//        ////////////////////////////////////////////////////////////////////////////////
//
//        txtView = (TextView)findViewById(R.id.txtMin);
//        layout = txtView.getLayoutParams();
//        marginLayout = (ViewGroup.MarginLayoutParams)layout;
//        txtView.setTextSize(TypedValue.COMPLEX_UNIT_PX, halfH * 0.8f);
////        marginLayout.topMargin = halfH - (int)(txtView.getTextSize() / 3f);
//        txtView.setLayoutParams(layout);
//
//        ////////////////////////////////////////////////////////////////////////////////
//        // 秒
//        ////////////////////////////////////////////////////////////////////////////////
//
//        txtView = (TextView)findViewById(R.id.txtSec);
//        layout = txtView.getLayoutParams();
//        marginLayout = (ViewGroup.MarginLayoutParams)layout;
//        txtView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fullH * 0.7f);
////        marginLayout.bottomMargin = (int)(-(halfH / 2.5f));
//        txtView.setLayoutParams(layout);
//    }

    private void settingsLayout_Portrait() {
        ////////////////////////////////////////////////////////////////////////////////
        // 画面情報取得
        ////////////////////////////////////////////////////////////////////////////////

        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        int fullW = displayMetrics.widthPixels;
        int fullH = displayMetrics.heightPixels;

        int halfW = fullW / 2;
        int halfH = fullH / 2;

        TextView txtView;
        ViewGroup.LayoutParams layout;
        ViewGroup.MarginLayoutParams marginLayout;

        ////////////////////////////////////////////////////////////////////////////////
        // 月
        ////////////////////////////////////////////////////////////////////////////////

        txtView = (TextView)findViewById(R.id.txtMon);
        layout = txtView.getLayoutParams();
        marginLayout = (ViewGroup.MarginLayoutParams)layout;
        txtView.setTextSize(TypedValue.COMPLEX_UNIT_PX, halfW);
        marginLayout.topMargin = (int)(-(halfW / 3.5f));
        marginLayout.leftMargin = -(halfW / 10);
        txtView.setLayoutParams(layout);

        ////////////////////////////////////////////////////////////////////////////////
        // セパレータ(日)
        ////////////////////////////////////////////////////////////////////////////////

        txtView = (TextView)findViewById(R.id.txtSepDay);
        layout = txtView.getLayoutParams();
        marginLayout = (ViewGroup.MarginLayoutParams)layout;
        txtView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fullW * 0.65f);
        marginLayout.topMargin = (int)(-(txtView.getTextSize() / 6));
        marginLayout.rightMargin = (int)(-(txtView.getTextSize() / 5));
        txtView.setLayoutParams(layout);

        ////////////////////////////////////////////////////////////////////////////////
        // 日
        ////////////////////////////////////////////////////////////////////////////////

        txtView = (TextView)findViewById(R.id.txtDay);
        layout = txtView.getLayoutParams();
        marginLayout = (ViewGroup.MarginLayoutParams)layout;
        txtView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fullW * 0.6f);
        marginLayout.topMargin = (halfW / 10);
        marginLayout.rightMargin = -(halfW / 10);
        txtView.setLayoutParams(layout);

        ////////////////////////////////////////////////////////////////////////////////
        // 時
        ////////////////////////////////////////////////////////////////////////////////

        txtView = (TextView)findViewById(R.id.txtHour);
        layout = txtView.getLayoutParams();
        marginLayout = (ViewGroup.MarginLayoutParams)layout;
        txtView.setTextSize(TypedValue.COMPLEX_UNIT_PX, halfW * 0.8f);
        marginLayout.topMargin = halfH - (int)(txtView.getTextSize() / 2f) - (int)(txtView.getTextSize() / 20f);
        marginLayout.leftMargin = -(halfW / 12);
        txtView.setLayoutParams(layout);

        ////////////////////////////////////////////////////////////////////////////////
        // セパレータ(時)
        ////////////////////////////////////////////////////////////////////////////////

        txtView = (TextView)findViewById(R.id.txtSepTime);
        layout = txtView.getLayoutParams();
        marginLayout = (ViewGroup.MarginLayoutParams)layout;
        txtView.setTextSize(TypedValue.COMPLEX_UNIT_PX, halfW * 0.65f);
        marginLayout.topMargin = halfH - (int)(txtView.getTextSize() / 2f);
        marginLayout.leftMargin = -(int)(txtView.getTextSize() / 4f);
        txtView.setLayoutParams(layout);

        ////////////////////////////////////////////////////////////////////////////////
        // 分
        ////////////////////////////////////////////////////////////////////////////////

        txtView = (TextView)findViewById(R.id.txtMin);
        layout = txtView.getLayoutParams();
        marginLayout = (ViewGroup.MarginLayoutParams)layout;
        txtView.setTextSize(TypedValue.COMPLEX_UNIT_PX, halfW * 0.8f);
        marginLayout.topMargin = halfH - (int)(txtView.getTextSize() / 2.5f);
        txtView.setLayoutParams(layout);

        ////////////////////////////////////////////////////////////////////////////////
        // 秒
        ////////////////////////////////////////////////////////////////////////////////

        txtView = (TextView)findViewById(R.id.txtSec);
        layout = txtView.getLayoutParams();
        marginLayout = (ViewGroup.MarginLayoutParams)layout;
        txtView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fullW * 0.7f);
        marginLayout.bottomMargin = (int)(-(halfW / 2.5f));
        txtView.setLayoutParams(layout);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // レイアウト調整
        //settingsLayout();
    }
}
