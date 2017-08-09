package com.zte.autodial;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zte.autodial.okhttp.OkHttpUtils;
import com.zte.autodial.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

/**
 * 工具主界面类，主要用于界面显示、用户交互操作
 */

public class MainActivity extends AppCompatActivity
{

    private Button button_start, button_stop;
    private EditText et_callTime, mEtNumber, mEtCount;
    public static Context mContext;
    private static final String TAG = "PhoneCall";
    private TextView tv_current_state, tv_total_numbers, tv_deal_numbers, tv_remain_numbers, tv_current_number;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        EventBus.getDefault().register(this);  //注册事件总线，eventbus是第三方库，用户UI更新
        button_start = (Button) this.findViewById(R.id.button_start);
        button_stop = (Button) this.findViewById(R.id.button_stop);
        tv_current_state = (TextView) this.findViewById(R.id.tv_current_state);
        tv_total_numbers = (TextView) this.findViewById(R.id.tv_total_numbers);
        tv_deal_numbers = (TextView) this.findViewById(R.id.tv_deal_numbers);
        tv_remain_numbers = (TextView) this.findViewById(R.id.tv_remain_numbers);
        tv_current_number = (TextView) this.findViewById(R.id.tv_current_number);
        //通道号
        mEtNumber = (EditText) this.findViewById(R.id.editText_number);
        //通道数
        mEtCount = (EditText) this.findViewById(R.id.editText_count);

        et_callTime = (EditText) findViewById(R.id.editText_calltime);
        //获取通话状态
//        TelephonyManager mTelephonyMgr = (TelephonyManager) MainActivity.mContext.getSystemService(Context.TELEPHONY_SERVICE);
//
//        mTelephonyMgr.listen(new TeleListener(), PhoneStateListener.LISTEN_CALL_STATE);
        requestWritePermissions();

        //点击开始按钮
        button_start.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                boolean state = ((AutoDialApp) getApplication()).getState(); //获取当前执行状态
                if (mEtCount.getText().toString().equals("") || mEtNumber.getText().toString().equals(""))
                {
                    Toast.makeText(MainActivity.this, "通道号和通道数不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (state)
                {
                    Util.showToast(MainActivity.this, "正在执行拨号，请先停止！");
                } else
                {
                    //启动service，该service用于执行拨号任务
                    Intent intent = new Intent();
                    intent.putExtra("callTime", Integer.parseInt(et_callTime.getText().toString()));
                    intent.putExtra("number", mEtNumber.getText().toString());
                    intent.putExtra("count", mEtCount.getText().toString());
                    intent.setClass(MainActivity.this, PhoneCallStateService.class);
                    startService(intent);
                }
            }
        });

        //点击停止按钮
        button_stop.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (((AutoDialApp) getApplication()).getState())
                {
                    ((AutoDialApp) getApplication()).setState(false);//标志位置fasle
                    Util.showToast(MainActivity.this, "呼叫已停止！");
                }
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        //以下代码用于刷新界面显示
        et_callTime.setSelection(et_callTime.getText().length());
        if (((AutoDialApp) getApplication()).getState())
        {
            tv_current_state.setText("正在拨号...");
        } else
        {
            tv_current_state.setText("空闲");
        }
        tv_total_numbers.setText(((AutoDialApp) getApplication()).getToalNumbers());
        tv_deal_numbers.setText(((AutoDialApp) getApplication()).getDealNumbers());
        tv_remain_numbers.setText(((AutoDialApp) getApplication()).getRemainNumbers());
        tv_current_number.setText(((AutoDialApp) getApplication()).getCurrentNumber());
    }

    //eventbus的回调函数，用于更新UI
    public void onEventMainThread(AnyEvent event)
    {
        String s = event.getDiscribe();
        if (s.equals("total_numbers"))
        {
            tv_total_numbers.setText(event.getContent());
        } else if (s.equals("deal_numbers"))
        {
            tv_deal_numbers.setText(event.getContent());
        } else if (s.equals("remain_numbers"))
        {
            tv_remain_numbers.setText(event.getContent());
        } else if (s.equals("current_number"))
        {
            tv_current_number.setText(event.getContent());
        } else if (s.equals("file_not_exist"))
        {
            Util.showToast(MainActivity.this, "号码文件不存在！");
        } else if (s.equals("call_over"))
        {
            tv_current_state.setText("空闲");
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case 0x01:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                }
                break;
        }
    }

    private void requestWritePermissions()
    {
        if (Build.VERSION.SDK_INT >= 23)
        {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0x01);
            } else
            {
            }
        } else
        {
        }
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        EventBus.getDefault().unregister(this); //去注册eventbus总线
    }

}
