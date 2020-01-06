package com.dinfo.robotea.service;


import com.dinfo.robotea.http.TrafficClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *  通用 工具类
 */
@Component
public class CommonUtil {


    private static final Logger log = LoggerFactory.getLogger(CommonUtil.class);


    /**  时间工具类,精确到小时, 将 "2019-11-13 20"格式 转换成 "2019年11月13日20时"
     * @param source    源时间字符串
     * @return  target  目标时间字符串
     */
    public String dateFormatAccurateTOHour(String source) {

        Date date= null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH").parse(source);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);

        String target =calendar.get(Calendar.YEAR)+"年"
                +(calendar.get(Calendar.MONTH)+1)+"月"
                + calendar.get(Calendar.DAY_OF_MONTH)+"日"
                + calendar.get(Calendar.HOUR_OF_DAY)+"时" ;

        System.out.println("\t 源日期格式: \t"+source);
        System.out.println("\t 目标日期格式: \t"+target);

        return target ;


    }



 /**  时间工具类,精确到日期, 将 "2000-11-13"格式 转换成 "2000年11月13日"
     * @param source    源时间字符串
     * @return  target  目标时间字符串
     */
    public String dateFormatAccurateToDay(String source) {

        Date date= null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(source);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);

        String target =calendar.get(Calendar.YEAR)+"年"
                +(calendar.get(Calendar.MONTH)+1)+"月"
                + calendar.get(Calendar.DAY_OF_MONTH)+"日" ;

        System.out.println("\t 源日期格式: \t"+source);
        System.out.println("\t 目标日期格式: \t"+target);

        return target ;


    }






}



