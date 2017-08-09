package com.zte.autodial;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * 该类处理excel
 */

public class HandleExcel {

    private static final String TAG = "PhoneCall";
    private Workbook mWorkbook;
    private Sheet mSheet1;
    private List<String[]> mNumbers;
    private File file;

    public HandleExcel() {
        init();
    }

    private void init() {
        mNumbers = new ArrayList<>();
    }

    //判断numbers.xls是否存在
    public boolean isDataExist(){
        String storageDir = Environment.getExternalStorageDirectory().toString();
        file = new File(storageDir+File.separator+"numbers.xls");
        if(file.exists()){
            return true;
        }else {
            return false;
        }
    }

    //读取第一个sheet页的号码
    public List<String[]> readNumber() {
        try {
            mWorkbook = Workbook.getWorkbook(file);
            mSheet1 = mWorkbook.getSheet(0);

            //循环读取所有行
            for (int i = 0; i < mSheet1.getRows(); i++) {
                Cell TagCell = mSheet1.getCell(1, i);//获取各行B列的内容
                String tag = TagCell.getContents();
                if(tag.equals("0")){//如果B列为0，则读取号码，放入List中待处理；若B列为1，则跳过
                    Cell NumberCell = mSheet1.getCell(0, i);
                    String number = NumberCell.getContents();
                    Log.d(TAG, "number = " + number);
                    mNumbers.add(new String[]{i+"",number,"0"});  //字符串数组：[0]行数、[1]号码、[2]状态
                }

            }
            mWorkbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }
        return mNumbers;
    }

    //更新excel中各个号码的状态（即B列内容）
    public void updateNumberState(List<String[]> numbers){
        try {
            Workbook wb = Workbook.getWorkbook(file);
            WritableWorkbook book  =  Workbook.createWorkbook(file,wb);
            //get一个工作表
            WritableSheet sheet  =  book.getSheet(0);
            for(int i = 0;i<numbers.size();i++){
                //numbers.get(i)[0] 表示行数，numbers.get(i)[2]表示呼叫结果（0或1）
                sheet.addCell(new Label(1,Integer.parseInt(numbers.get(i)[0]),numbers.get(i)[2]));
            }
            book.write();
            book.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        } catch (RowsExceededException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        }

    }

}
