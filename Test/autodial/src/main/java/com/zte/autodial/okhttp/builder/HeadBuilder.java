package com.zte.autodial.okhttp.builder;

import com.zte.autodial.okhttp.OkHttpUtils;
import com.zte.autodial.okhttp.request.OtherRequest;
import com.zte.autodial.okhttp.request.RequestCall;


import com.zte.autodial.okhttp.OkHttpUtils;
import com.zte.autodial.okhttp.request.OtherRequest;
import com.zte.autodial.okhttp.request.RequestCall;

/**
 * Created by zhy on 16/3/2.
 */
public class HeadBuilder extends GetBuilder
{
    @Override
    public RequestCall build()
    {
        return new OtherRequest(null, null, OkHttpUtils.METHOD.HEAD, url, tag, params, headers).build();
    }
}
