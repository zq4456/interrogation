package com.dinfo.robotea;

import com.dinfo.robotea.mapper.CaseClassificationMapper;
import com.dinfo.robotea.properties.RedisUtil;
import com.dinfo.robotea.service.CommonUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = RoboteaApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationTest {


    @Autowired
    private RedisUtil redisUtil;

    /**
     *  redis hash类型操作
     *  hget 是 取 key下item的值， hmget是取整个 key下所有item的键值对
     */
    @Test
    public void testRedis(){
//        Assert.assertTrue(redisUtil.hasKey("lily"));
//        String name = "曾志伟" ;
//        Assert.assertTrue( redisUtil.hset("reportSummary","name",name ) );

        // reids 中 值 非空判断
        Assert.assertNotNull(redisUtil.hmget("reportSummary"));

        // 修改 reids中的 HashMap类型数据
//        redisUtil.hset("reportSummary","name","chengXiaoChun");



        // 取 reids中的 HashMap类型数据
        Object obj =  redisUtil.hget("reportSummary_084b8f25-7c6f-43b8-8b4c-2ea8de34d2b9","name");
        System.out.println("hget:\t"+obj);

        //  hget 是 取 key下item的值， hmget是取整个 key下所有item的键值对
        Map map =redisUtil.hmget("reportSummary_084b8f25-7c6f-43b8-8b4c-2ea8de34d2b9");
        System.out.println("hmget:\t"+map);
//        redisUtil.hset("reportSummary","name","Jim");
       /* redisUtil.del("reportSummary");

        redisUtil.hset("reportSummary","name","Sam");
        Map map1 =redisUtil.hmget("reportSummary");
        System.out.println(map1);*/


    }

    /**
     * reids List读取操作
     * **/
    @Test
    public void redisListTest() throws InterruptedException {

        // redis的 Map类型变量里面，可以存储List类型数据
        List listx = new ArrayList();
        listx.add("aaa");
        listx.add("bbb");
        listx.add("ccc");

        redisUtil.hset("reportSummaryx_12_12","motherJob",listx );

        List listRet = (List) redisUtil.hget("reportSummaryx_12_12","motherJob" );
        System.out.println(listRet);


//        String key = "questionQueue_2eceb841-83ec-44e3-bcc0-4edda84f8ce7";
//        redisUtil.del(key);
//
//        redisUtil.lSet(key,"question1");
//        redisUtil.lSet(key,"question2");
//        redisUtil.lSet(key,"question3");
//
//        for(int i=0  ; i<10; i++){
//            System.out.println(key +":\t"+redisUtil.lGet(key,0,10) );
//        }

        // -1(永不失效) -2(不存在或已经失效)
//        System.out.println(redisUtil.getExpire("myset1"));

      /*  String asked = "9edd190b-43be-4694-9658-24cb9f142e74" ;

        long size = redisUtil.lGetListSize(asked);

        System.out.println("1已问问题题列表:\t"+redisUtil.lGet(asked,0,20));
        System.out.println("1已问问题数目:\t"+size);

        // 设置 key 的过期时间，key 过期后将不再可用。单位以秒计
        redisUtil.expire(asked,60l);

        System.out.println(redisUtil.lGetIndex(asked,size-1));*/


//        redisUtil.lSet("runoob",new ArrayList());
     /*   List<Object> list = redisUtil.lGet(asked,0,20);
        System.out.println(list.get(list.size()-1));

        System.out.println(redisUtil.rpop(asked) );*/


//        Thread.sleep(3000l);
//
//        System.out.println("2已问问题题列表:\t"+redisUtil.lGet(asked,0,20));
//        System.out.println("2已问问题数目:\t"+redisUtil.lGetListSize(asked));

//        System.out.println(redisUtil.rpop(asked));



    }



    @Test
    public  void testTryCatch(){
        int[] inta = {0,1,2,3,4,5};
        List lista = Arrays.asList(inta);

        try{
            System.out.println(inta[10]);

            System.out.println(200);

        }catch(Exception e){
//            e.printStackTrace();
            System.out.println(100);
        }


    }



    /**
     * reids 获取酒驾 家庭信息
     * **/
    @Test
    public void redisDrunkFamilyTest()  {

        String reportSummaryx = "reportSummary_drive_aaaa";

        System.out.println(

            redisUtil.hget(reportSummaryx,"governmentAndInstitutions")+"\n"+

            redisUtil.hget(reportSummaryx,"fatherName")+"\t"+
            redisUtil.hget(reportSummaryx,"fatherAge")+"\t"+
            redisUtil.hget(reportSummaryx,"fatherJob")+"\n"+

            redisUtil.hget(reportSummaryx,"motherName")+"\t"+
            redisUtil.hget(reportSummaryx,"motherAge")+"\t"+
            redisUtil.hget(reportSummaryx,"motherJob")+"\n"+

            redisUtil.hget(reportSummaryx,"spouseRelation")+"\t"+
            redisUtil.hget(reportSummaryx,"spouseName")+"\t"+
            redisUtil.hget(reportSummaryx,"spouseAge")+"\t"+
            redisUtil.hget(reportSummaryx,"spouseJob")+"\n"+

            redisUtil.hget(reportSummaryx,"brotherInfo")+"\n"+
            redisUtil.hget(reportSummaryx,"childrenInfo")


        );




    }


    @Autowired
    private CommonUtil commonUtil;
    /**
     * 通用工具类测试
     * */
    @Test
    public  void commonUtilTest(){
        String dateString = "2019-11-13 20:35:54";
        commonUtil.dateFormatAccurateTOHour(dateString);
//        String dateString = "1967-10-13";
//        commonUtil.dateFormatAccurateToDay(dateString);

    }

    @Autowired
    private CaseClassificationMapper caseClassificationMapper;

    /**
     * Mybatis 插入测试 aaa
     * */
    @Test
    public void testCaseClassificationMapper(){

        List list = new ArrayList();
        Map map1 = new HashMap();
        map1.put("id","207");
        map1.put("name","haha7");
        map1.put("update_time","2020-01-05 10:17:23");
        map1.put("update_user","aa7");
        list.add(map1);

    /*    Map map2 = new HashMap();
        map2.put("id","204");
        map2.put("name","haha5");
        map2.put("update_time","2020-01-05 10:12:33");
        map2.put("update_user","aa2");
        list.add(map2);

        Map map3 = new HashMap();
        map3.put("id","205");
        map3.put("name","haha6");
        map3.put("update_time","2020-01-05 10:15:43");
        map3.put("update_user","aa3");
        list.add(map3);*/


        int num = caseClassificationMapper.insertCaseClassification(list);
        System.out.println("\t插入了新数据：\t"+num);


    }


}
