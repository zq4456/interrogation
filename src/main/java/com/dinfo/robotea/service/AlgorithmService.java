package com.dinfo.robotea.service;


import com.dinfo.robotea.controller.DrunkController;
import com.dinfo.robotea.http.TrafficClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  调用算法 工具类
 */
@Component
public class AlgorithmService {


    @Autowired
    private TrafficClient trafficClient;

    private static final Logger log = LoggerFactory.getLogger(AlgorithmService.class);


    /**  算法接口调用方法包装
     * @param question 机器人问答内容
     * @return
     */
    public Map algorithmCall(String question) {

        Map param = new HashMap();
		/* 组装 json数据
		{
		"messageid": "111111111","clientid": "22222","": "false",
		"text":["问：你的个人信息？ \n 答：我叫刘磊，男，汉族，已婚，初中肄业，1984年10月26日出生，户籍所在地湖南省攸县坪阳庙乡南洋村大丰组大丰011号，现在在株洲无固定住所，身份证号码430223198410261111，手机号码18322581111，现在在株洲无业。"]
		}*/

        StringBuilder sb = new StringBuilder(question);
        // 答案之前必须 加换行符
        sb.insert(question.indexOf("答"), "\n");


        String[] arr = {sb.toString()};
        param.put("text", arr );
        param.put("messageid", "111111111");
        param.put("clientid", "22222");
        param.put("encrypt", "false");

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonStr="";
        try {
            jsonStr = objectMapper.writeValueAsString(param);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        log.info("调用算法接口的参数: "+jsonStr);

        // 调用算法接口
        Map basicSituation = trafficClient.getResult(jsonStr);
        log.info(".......算法接口返回结果：\t"+basicSituation);
        return basicSituation;
    }


    /**
     *   算法接口 抽取案件当中的物品
     * **/
    public Map algorithmGoodsOfCase(String question){
        Map caseClassification = new HashMap();
        caseClassification = algorithmCall(question);
        List list1 =   (List) caseClassification.get("result");
        List list2 = (List) list1.get(0);
        Map map1 = (Map) list2.get(0);
        // 被盗物品信息
        List goods =  (List) map1.get("goods");

        Map good1 = new HashMap();
        if(goods != null && goods.size()>0) {
            good1 = (Map) goods.get(0);
        }
        return good1;


    }


    /**
     *  算法接口抽取案件要素信息( result )
     * **/
    public Map algorithmElementsOfCase(String question){

        Map caseClassification = new HashMap();
        caseClassification = algorithmCall(question);
        List list1 =   (List) caseClassification.get("result");
        List list2 = (List) list1.get(0);
        Map map1 = (Map) list2.get(0);

        return map1;
    }

    /**
     *  算法接口 获取 个人信息
     * @param question
     * @return
     */
    public Map algorithmPersonInfo(String question){
        Map basicSituation = new HashMap();
        basicSituation = algorithmCall(question);

        List list1 =   (List) basicSituation.get("result");
        List list2 = (List) list1.get(0);
        Map map1 = (Map) list2.get(0);
        if(map1==null){
            return new HashMap();
        }

        Map<String, Object> map2 =  (Map<String, Object>) map1.get("persioninfo");
        return  map2;
    }



}



