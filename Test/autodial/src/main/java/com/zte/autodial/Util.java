package com.zte.autodial;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class Util {

	//开始呼叫
	public static void startCall(Context paramContext, String paramString)
	{
		Intent intent = new Intent(Intent.ACTION_CALL);
		Uri data = Uri.parse("tel:" + paramString);
		intent.setData(data);
		intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
		paramContext.startActivity(intent);
	}

	//挂断电话
	public static void endCall() {
		try {
			Method method = Class.forName("android.os.ServiceManager")
					.getMethod("getService", String.class);
			IBinder binder = (IBinder) method.invoke(null, new Object[]{Context.TELEPHONY_SERVICE});
			ITelephony telephony = ITelephony.Stub.asInterface(binder);
			telephony.endCall();
		} catch (NoSuchMethodException e) {
		} catch (ClassNotFoundException e) {
		} catch (Exception e) {
		}
	}

	//显示toast
	public static void showToast(Context c,String s){
		Toast.makeText(c,s,Toast.LENGTH_SHORT).show();
	}


}
