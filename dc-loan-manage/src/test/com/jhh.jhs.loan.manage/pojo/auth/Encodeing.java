package com.jhh.jhs.loan.manage.pojo.auth;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Encodeing {

    public static void main(String[] args) {

        SimpleDateFormat yearSdf = new SimpleDateFormat("yyyy");
        SimpleDateFormat monthSdf = new SimpleDateFormat("M");
        SimpleDateFormat dateSdf = new SimpleDateFormat("MM-DD");

        Date date = new Date();

        System.out.println(monthSdf.format(date));
    }

}
