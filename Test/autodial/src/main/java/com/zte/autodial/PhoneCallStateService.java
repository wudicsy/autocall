package com.zte.autodial;

import android.Manifest;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.provider.CallLog;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.zte.autodial.okhttp.OkHttpUtils;
import com.zte.autodial.okhttp.User;
import com.zte.autodial.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import jxl.Sheet;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.read.biff.BiffException;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WriteException;
import me.zhouzhuo.zzexcelcreator.ZzExcelCreator;
import me.zhouzhuo.zzexcelcreator.ZzFormatCreator;
import okhttp3.Call;
import okhttp3.MediaType;

/**
 * 该类用于执行呼叫操作，并判断呼叫结果
 */

public class PhoneCallStateService extends Service
{

    private static final String TAG = "PhoneCall";
    private int callTime = 30;
    private String mNumber;
    private String mCount;
    private int mSize;
    private int i;

    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d("PhoneCall", "onStartCommand");
        if (intent != null)
        {
            callTime = intent.getIntExtra("callTime", 30);
            mNumber = intent.getStringExtra("number");
            mCount = intent.getStringExtra("count");
            getTrainLeadsForCall(mNumber, mCount);
        }
        return START_NOT_STICKY;
    }

    public void getTrainLeadsForCall(String queueId, String queueTotal)
    {
        OkHttpUtils
                .get()//
                .url(Contant.URL + "/getTrainLeadsForCall")//
                .addParams("queueId", queueId)//
                .addParams("queueTotal", queueTotal)//
                .build()//
                .execute(new StringCallback()
                {
                    @Override
                    public void onError(Call call, Exception e)
                    {
                        Toast.makeText(MainActivity.mContext, "出错了", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response)
                    {

                        try
                        {
                            List<String[]> numbers = new ArrayList<String[]>();
                            JSONObject jsonObject = new JSONObject(response);
                            String resultCode = jsonObject.getString("resultCode");
                            String result = jsonObject.getString("resultData");
                            JSONArray jsonArray = new JSONArray(result);
                            for (int i = 0; i < jsonArray.length(); i++)
                            {
                                JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                                numbers.add(new String[]{i + "", jsonObject1.getString("phoneNumber"), jsonObject1.getString("phoneValid"), jsonObject1.getString("id")});
                            }
                            if (resultCode.equals("0"))
                            {
                                startCallThread(numbers, resultCode);//启动线程，执行呼叫
                            }
                        } catch (JSONException e)
                        {
//                            List<String[]> numbers = new ArrayList<String[]>();
//                            for (int i = 0; i < 10; i++)
//                            {
//                                numbers.add(new String[]{i + "", "1524115871", "" + i + "$", "" + i + "*"});
//                            }
                            e.printStackTrace();
                            Toast.makeText(MainActivity.mContext, "暂无数据", Toast.LENGTH_SHORT).show();
//                            startCallThread(numbers, "0");//启动线程，执行呼叫
                        }
                    }
                });
    }


    public void updateTrainLeadsPhoneValid(final String id, final String phoneValid, final String number) throws JSONException
    {
        String id1 = id;
        String ph = phoneValid;
        Map<String, String> headers = new HashMap<>();
        headers.put("id", id);
        headers.put("phoneValid", phoneValid);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("phoneValid", phoneValid);


        OkHttpUtils
                .postString()//
                .url(Contant.URL + "/updateTrainLeadsPhoneValid")//
//                .addParams("id", id)//
//                .addParams("json", jsonObject.toString())//
                .mediaType(MediaType.parse("application/json"))
                .content(jsonObject.toString())
                .build()//
                .execute(new StringCallback()
                {
                    @Override
                    public void onError(Call call, Exception e)
                    {
                        String ss = e.toString();
//                        Toast.makeText(MainActivity.mContext, "出错了", Toast.LENGTH_SHORT).show();
                        if (!new File(Contant.PATH + "上传失败列表" + ".xls").exists())
                        {
                            createExcel();
                        }
                        Workbook workbook = null;
                        try
                        {
                            workbook = Workbook.getWorkbook(new File(Contant.PATH + "上传失败列表" + ".xls"));
                            //获取第一个工作表的对象
                            Sheet sheet = workbook.getSheet(0);
                            int a = sheet.getRows();
                            insertExcel("0", a + "", number);
                            insertExcel("1", a + "", phoneValid);
                        } catch (IOException ex)
                        {
                            ex.printStackTrace();
                        } catch (BiffException ex)
                        {
                            ex.printStackTrace();
                        }


                    }

                    @Override
                    public void onResponse(String response)
                    {

                        try
                        {
                            JSONObject jsonObject = new JSONObject(response);
                            String resultCode = jsonObject.getString("resultCode");
                        } catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
    }


    //执行呼叫的线程
    private void startCallThread(final List<String[]> numbers, String resultCode)
    {
        mSize = mSize + numbers.size();
        Thread callThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
//                HandleExcel handleExcel = new HandleExcel();//初始化excel操作类

                //excel文件不存在时，直接返回，并提示用户
//                if (!handleExcel.isDataExist())
//                {
//                    EventBus.getDefault().post(new AnyEvent("file_not_exist", ""));//发送事件，通知UI刷新
//                    Log.d(TAG, "numbers.xls is not exist");
//                    return;
//                }
                //读取待拨号的号码列表，存放到LIST中，每个list元素是一个 String[] 三维数组
                // String[0]保存号码在excel中的行数，String[1]保存号码内容即A列，String[2]保存号码是否可用即B列
//                List<String[]> numbers = handleExcel.readNumber();


                //csy modify   先从服务器拉取数据


                //更新UI
                EventBus.getDefault().post(new AnyEvent("total_numbers", mSize + ""));
                ((AutoDialApp) getApplication()).setToalNumbers(mSize + "");
                ((AutoDialApp) getApplication()).setState(true);//全局标志位置为true

                for (; i < mSize; i++)
                {

                    //更新UI
                    EventBus.getDefault().post(new AnyEvent("deal_numbers", (i) + ""));
                    ((AutoDialApp) getApplication()).setDealNumbers((i) + "");
                    EventBus.getDefault().post(new AnyEvent("remain_numbers", (mSize - (i)) + ""));
                    ((AutoDialApp) getApplication()).setRemainNumbers((mSize - (i)) + "");


                    //若按了停止按钮，则跳出循环
                    if (!((AutoDialApp) getApplication()).getState())
                    {
                        break;
                    }

                    //开始呼叫，传入所需要呼叫的号码
                    Util.startCall(PhoneCallStateService.this, numbers.get(i % 10)[1]);

                    //更新UI
                    EventBus.getDefault().post(new AnyEvent("current_number", numbers.get(i % 10)[1]));
                    ((AutoDialApp) getApplication()).setCurrentNumber(numbers.get(i % 10)[1]);

                    //呼叫每个号码的时间在这里控制，callTime表示界面设置的呼叫持续时间
                    try
                    {
                        for (int j = 0; j < callTime; j++)
                        {
                            if (!((AutoDialApp) getApplication()).getState())
                            {
                                break;//等待过程中若点击停止按钮，则跳出循环
                            }
                            Thread.sleep(1000);
                        }
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }

                    //呼叫时间到达时，主动挂断电话
                    Util.endCall();

                    try
                    {
                        Thread.sleep(500);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }

                    //获取该号码是否建立通话
                    boolean state = getCallState(numbers.get(i % 10)[1]);

                    //将该号码是否建立通话的结果保存到数组，便于最后更新到excel中
                    if (state)
                    {
                        numbers.get(i % 10)[2] = "1";
                    } else
                    {
                        numbers.get(i % 10)[2] = "0";
                    }
                    //csy modify 提交回执
                    try
                    {
                        updateTrainLeadsPhoneValid(numbers.get(i % 10)[3], numbers.get(i % 10)[2], numbers.get(i % 10)[1]);
                    } catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }
                //将呼叫结果写回excel
//                handleExcel.updateNumberState(numbers);
                //所有呼叫结束后，更新UI
                EventBus.getDefault().post(new AnyEvent("deal_numbers", (i) + ""));
                ((AutoDialApp) getApplication()).setDealNumbers((i) + "");
                EventBus.getDefault().post(new AnyEvent("remain_numbers", (mSize - (i)) + ""));
                ((AutoDialApp) getApplication()).setRemainNumbers((mSize - (i)) + "");
                EventBus.getDefault().post(new AnyEvent("call_over", ""));
//                ((AutoDialApp) getApplication()).setState(false);
                if (((AutoDialApp) getApplication()).getState())
                {
                    getTrainLeadsForCall(mNumber, mCount);
                }
            }
        }, "callThread");
        callThread.start();
    }


    //该方法用于获取某个号码是否建立过通话
    private boolean getCallState(String number)
    {
        ContentResolver cr = getContentResolver();
        boolean isLink = false;
        //通过CallLog查询该号码的呼出通话记录，并按拨号时间倒序排列
        final Cursor cursor = cr.query(CallLog.Calls.CONTENT_URI,
                new String[]{CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DURATION},
                CallLog.Calls.NUMBER + "=? and " + CallLog.Calls.TYPE + "= ?",
                new String[]{number.toString(), "2"}, CallLog.Calls.DATE + " desc");

        while (cursor.moveToNext())
        {
            //取出通话记录，获取通话时间
            int durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION);
            long durationTime = cursor.getLong(durationIndex);
            Log.d("PhoneCall", "durationTime:" + durationTime);
            //大于0表示建立了通话，小于0表示未建立通话
            if (durationTime > 0)
            {
                isLink = true;
            } else
            {
                isLink = false;
            }
            break;
        }
        return isLink;
    }


    private void createExcel()
    {
        new AsyncTask<String, Void, Integer>()
        {

            @Override
            protected Integer doInBackground(String... params)
            {
                try
                {
                    ZzExcelCreator
                            .getInstance()
                            .createExcel(Contant.PATH, params[0])
                            .createSheet(params[1])
                            .close();
                    return 1;
                } catch (IOException | WriteException e)
                {
                    e.printStackTrace();
                    return 0;
                }
            }

            @Override
            protected void onPostExecute(Integer aVoid)
            {
                super.onPostExecute(aVoid);
                if (aVoid == 1)
                {
//                    Toast.makeText(MainActivity.this, "表格创建成功！请到" + Contant.PATH + "路径下查看~", Toast.LENGTH_SHORT).show();
                } else
                {
                    Toast.makeText(MainActivity.mContext, "表格创建失败！", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute("上传失败列表", "无");
    }


    private void insertExcel(final String col, final String row, final String str)
    {
        new AsyncTask<String, Void, Integer>()
        {

            @Override
            protected Integer doInBackground(String... params)
            {
                try
                {
                    WritableCellFormat format = ZzFormatCreator
                            .getInstance()
                            .createCellFont(WritableFont.ARIAL)
                            .setAlignment(Alignment.CENTRE, VerticalAlignment.CENTRE)
                            .setFontSize(14)
                            .setFontColor(Colour.DARK_GREEN)
                            .getCellFormat();
                    ZzExcelCreator
                            .getInstance()
                            .openExcel(new File(Contant.PATH + "上传失败列表" + ".xls"))
                            .openSheet(0)
                            .setColumnWidth(Integer.parseInt(col), 25)
                            .setRowHeight(Integer.parseInt(row), 400)
                            .fillContent(Integer.parseInt(col), Integer.parseInt(row), str, format)
                            .close();
                    return 1;
                } catch (IOException | WriteException | BiffException e)
                {
                    e.printStackTrace();
                    return 0;
                }
            }

            @Override
            protected void onPostExecute(Integer aVoid)
            {
                super.onPostExecute(aVoid);
                if (aVoid == 1)
                {
//                    Toast.makeText(MainActivity.this, "插入成功！", Toast.LENGTH_SHORT).show();
                } else
                {
//                    Toast.makeText(MainActivity.this, "插入失败！", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(col, row, str);
    }


}

