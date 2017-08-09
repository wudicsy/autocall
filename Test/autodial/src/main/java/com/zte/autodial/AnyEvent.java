package com.zte.autodial;

/**
 * 该类是eventbus的事件模型
 */

public class AnyEvent {


    private String discribe;//事件描述，用于区分事件类型

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    private String content; //事件内容


    //构造函数
    public AnyEvent(String discribe,String c) {
        this.discribe = discribe;
        this.content = c;
    }

    //set/get方法
    public void setDiscribe(String discribe) {
        this.discribe = discribe;
    }

    public String getDiscribe() {
        return discribe;
    }
}
