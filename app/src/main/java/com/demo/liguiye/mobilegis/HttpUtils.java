package com.demo.liguiye.mobilegis;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


public class HttpUtils {
    public static void upload(final String url){
        //新建一个多线程的方法
        new Thread(new Runnable(){
            @Override
            public void run() {
                HttpURLConnection connection;
                try {
                    //打开url
                    connection = (HttpURLConnection)new URL(url).openConnection();
                    //设置请求方式
                    connection.setRequestMethod("GET");
                    connection.getInputStream();

                } catch (IOException E) {
                    E.printStackTrace();
                }
            }
        }).start();

    }

}
