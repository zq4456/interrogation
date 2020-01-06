package com.dinfo.robotea.controller;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.dinfo.robotea.properties.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dinfo.robotea.http.OecClient;
import com.dinfo.robotea.properties.QuestionProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *  报案问答
 */
@Controller
@RequestMapping("/report")
public class ReportController {

	private static final Logger log = LoggerFactory.getLogger(ReportController.class);

	@Autowired
	private QuestionProperties questions;

	@Autowired
	private OecClient oecClient;

	@Autowired
	private RedisUtil redisUtil;

	// 案发地址补全前缀
    private final static  String addressCompletion = "广东省佛山市顺德区";

	private final static  String question1="您好，我是佛山市顺德区公安局的报案机器人，欢迎您使用机器人报案，现在就有关案情对你进行询问，你必须如实回答，不得隐瞒事实或者作假口供，否则要依法追究你相应的法律责任，对与本案无关的问题，你有权拒绝回答，你是否清楚？";

    private final static  String question2="现根据《中华人民共和国刑事诉讼法》的相关规定，将《被害人诉讼权利义务告知书》送达给你，你是否阅读清楚该文书的内容？";
	private final static  String question3="请问您的基本情况？";

    private final static  String question4="现住址在哪？";
    private final static  String question5="文化程度？";
    private final static  String question6="哪里工作？"; // 因接口抽取不出，暂时从序列中删除
    private final static  String question7="联系电话？";
	private final static  String question8="因何事到公安机关？";

	private final static  String question09="请将被盗窃的具体过程描述一下？";

    private final static  String question10="请问您在哪里被盗窃？";
    private final static  String question11="具体什么时间被盗窃？";
    private final static  String question11_1="被盗的是什么？";
	private final static  String question12="什么品牌？"; // 被盗的（物品名称）是什么品牌？

    private final static  String question13="型号是什么？";
    private final static  String question14="什么颜色？";
	private final static  String question15="被盗的手机号码多少？";

    private final static  String question16="被盗物品价值多少？";
	private final static  String question17="你有否保留到购买被盗窃手机的购置单据？";


	private final static  String question20="现场是否有视频监控？";

	private final static  String question22="以上所说是否属实？";

	private final static  String question24 = "回到上一个问题";



	/**  调用算法接口测试
	 * @param json 2019-11-05 12:34aaa
	 * @return
	 */
	@ResponseBody
	@PostMapping(path="/getResult")
	public Object getResult(@RequestBody String json) {
		return oecClient.getResult(json);
	}

    @RequestMapping(value="/index")
    public String getIndex() {
        return "index";
    }

	/** 报案要素 githubss
	 * @return
	 */
	@PostMapping(value="/reportElement")
	@ResponseBody
	public  Object reportElement(String cookieUuid){

        String reportSummaryx = "reportSummary_"+cookieUuid;
        Map<Object, Object> map = redisUtil.hmget(reportSummaryx);
		return map;
	}

	/** 机器人问答接口
	 * @return
	 */
	@PostMapping(value="/robotReporting")
	@ResponseBody
	@SuppressWarnings({ "unchecked", "unused", "rawtypes" })
	public Object reporting(String question,HttpServletRequest request) {

        Map<String, Object> ret =new HashMap<String,Object>();

        // 模拟客户端 cookie
        String cookieUuid = request.getParameter("cookieUuid");

        String asked = "asked_"+cookieUuid;
        log.info("cookieUuid：\t"+asked);

		// 人员信息和案件要素 问题队列 start
        String questionQueueRedis = "questionQueue_"+cookieUuid;

        long questionQueueSize = redisUtil.lGetListSize(questionQueueRedis);

        // 缓存变量失效时间
        long expireTime = redisUtil.getExpire(questionQueueRedis);
        if(expireTime== -1 ){
            redisUtil.expire(questionQueueRedis,60l*60*24);
        }

        expireTime = redisUtil.getExpire(asked);
        if(expireTime== -1){
            redisUtil.expire(asked,60l*60);
        }
        // 人员信息和案件要素 问题队列 start

        List questionQueuem = redisUtil.lGet(questionQueueRedis,0,50) ;
        System.out.println("############### "+questionQueueRedis+":\t"+questionQueuem);

        Map<String,String> questionMap = new HashMap<String, String>();
        questionMap.put("question4", question4);
		questionMap.put("question5", question5);
		questionMap.put("question6", question6);
		questionMap.put("question7", question7);
		questionMap.put("question8", question8);

		questionMap.put("question10", question10);
		questionMap.put("question11", question11);
		questionMap.put("question11_1", question11_1);
		questionMap.put("question12", question12);
		questionMap.put("question13", question13);
		questionMap.put("question14", question14);
		questionMap.put("question15", question15);
		questionMap.put("question16", question16);

		questionMap.put("question17", question17);

		questionMap.put("question20", question20);
		questionMap.put("question22", question22);

		log.info("机器人提问 : "+question);

		String humanResponse="";
		String ask="";
		if(question.contains("答：")) {

			ask = question.split("答：")[0].replaceAll(" ", "").replaceAll("\n", "");
			// 报案人回答 内容,去除换行符.
			humanResponse=question.split("答：")[1].replaceAll(" ", "").replaceAll("\n", "");

			System.out.println("问题是:\t"+ask);
			System.out.println("回答是:\t"+humanResponse);

		}

		if(!StringUtils.equals(question,"回到上一个问题") && !StringUtils.contains(question,"号码格式错误")){

            String str = StringUtils.replace(ask,"问：","");
            redisUtil.lSet(asked,str);

        }


        long size1 = redisUtil.lGetListSize(asked) ;
        String lastQuestion = (String)redisUtil.lGetIndex(asked,size1-1);
        System.out.println("上一个问题是：\t"+lastQuestion);
        System.out.println("已问问题题列表:\t"+redisUtil.lGet(asked,0,20));
        System.out.println("已问问题数目:\t"+size1);

        // 案件要素
        String reportSummaryx = "reportSummary_"+cookieUuid;
        expireTime = redisUtil.getExpire(reportSummaryx);
        if(expireTime== -1){
            redisUtil.expire(reportSummaryx,60l*25);
        }
        System.out.println("案件提取要素：\t"+redisUtil.hmget(reportSummaryx));


        // 下一个问题
        String nextQuestion="";

        // 开始提问  ******************************************************************************
        if(StringUtils.contains(ask, "您好，我是佛山市顺德区公安局的报案机器人")) {


			if( StringUtils.contains(humanResponse, "不清楚") ) {
				nextQuestion=question1+"如仍不清楚，可咨询现场工作人员";
			}else {
				nextQuestion=question2;

			}



		}else if(StringUtils.contains(ask, "现根据《中华人民共和国刑事诉讼法》的相关规定")) {

			if(StringUtils.contains(humanResponse, "不清楚") ) {
				nextQuestion=question2+"如仍不清楚，可咨询现场工作人员";
			}else {
				nextQuestion=question3;

			}


		}else if(StringUtils.contains(ask, question3)) {


			Map basicSituation = new HashMap();
			basicSituation = algorithmCall(question);

			List list1 =   (List) basicSituation.get("result");
			List list2 = (List) list1.get(0);
			Map map1 = (Map) list2.get(0);
			Map<String, Object> map2 =  (Map<String, Object>) map1.get("persioninfo");

			List push = new ArrayList();

			String format = "";

			for(Map.Entry<String, Object> entry : map2.entrySet() ){
				String mapKey = entry.getKey();
				Object mapValue = entry.getValue();
//			    System.out.println(mapKey+":"+mapValue);

				if(StringUtils.equals(mapKey, "姓名")) {
					if(StringUtils.isNotBlank((String) mapValue)) {
						redisUtil.hset(reportSummaryx,"name",mapValue);
					}

				}else if(StringUtils.equals(mapKey, "居住地")) {
					if(StringUtils.isBlank((String)mapValue)) {
						push.add("question4");
					}else{
						redisUtil.hset(reportSummaryx,"residence",addressCompletion+ mapValue);

					}


				}else if(StringUtils.equals(mapKey, "受教育程度")) {
					if(StringUtils.isBlank((String)mapValue)) {
						push.add("question5");
					}else{
						redisUtil.hset(reportSummaryx,"education",mapValue);

					}

				}else if(StringUtils.equals(mapKey, "联系方式")) {
					List lianxifagnshi = (List)mapValue;
					String contact = "";

					if(lianxifagnshi!=null && !lianxifagnshi.isEmpty() ){
						contact = (String) lianxifagnshi.get(0);

					}

					if(lianxifagnshi.isEmpty()) {
						push.add("question7");
					}else if(StringUtils.contains(contact,"号码格式错误")){
						push.add("question7");
						format="("+contact+")";

					}else {
						redisUtil.hset(reportSummaryx,"contact",contact);

					}

				}else if(StringUtils.equals(mapKey, "身份证号码")) {
					List idCard = (List)mapValue;
					if(!idCard.isEmpty()){
						redisUtil.hset(reportSummaryx,"idCard",idCard.get(0));

					}


				}else if(StringUtils.equals(mapKey, "户籍地")) {
					String householRegister = (String)mapValue;
					if(StringUtils.isNotBlank(householRegister)){
						redisUtil.hset(reportSummaryx,"householRegister",householRegister);

					}


				}else if(StringUtils.equals(mapKey, "性别")) {
					String gender = (String)mapValue;
					if(StringUtils.isNotBlank(gender)){
						redisUtil.hset(reportSummaryx,"gender",gender);

					}

				}else if(StringUtils.equals(mapKey, "年龄")) {
					String age = (String)mapValue;
					if(StringUtils.isNotBlank(age)){
						redisUtil.hset(reportSummaryx,"age",age);

					}

				}else if(StringUtils.equals(mapKey, "出生日期")) {
					List birthday = (List)mapValue;
					if(!birthday.isEmpty()){
						redisUtil.hset(reportSummaryx,"birthday",birthday.get(0));

					}

				}else if(StringUtils.equals(mapKey, "民族")) {
					String nation = (String)mapValue;
					if(StringUtils.isNotBlank(nation)){
						redisUtil.hset(reportSummaryx,"nation",nation);

					}

				}




			   /*  问题6： 工作单位 单独问
			    else if(StringUtils.equals(mapKey, "工作单位")) {
			    	if(StringUtils.isBlank((String)mapValue)) {
			    		push.add("question6"); // Nones
			    	}

			    }*/



			}

			// 哪里工作？ 接口没有提取，删除
//			push.add("question6");

			if(push.isEmpty()) {
				// 如果 基本情况都被接口 提取出来了，跳到问题6
				basicSituation.put("nextQuestion", questionMap.get("question8"));
			}else {
				// 如果 存在有的 基本情况未被接口 提取出来，则将这几个问题存储到 ApplicationContext全局变量
				Collections.sort(push);// 排序
				String nexta = questionMap.get(push.get(0));
				if(StringUtils.contains(format,"号码格式错误")){
					nexta+=format;
				}

				basicSituation.put("nextQuestion",  nexta );

				push.remove(0);

				redisUtil.lSet(questionQueueRedis,push);

			}

			log.info("报案摘要：\t"+redisUtil.hmget(reportSummaryx));
			log.info("basicSituation:\t"+basicSituation);

			return basicSituation;

		}else if(StringUtils.containsAny(ask,
				question4,question5,question7)) {

			List list = questionQueuem;


			//			String question4="现住址在哪？";
			//			String question5="文化程度？";
			//			String question7="联系电话？";
			if(StringUtils.contains(question,question4)){
				Map map =this.getElementsOfCase(question);
				List lista = (List)map.get("address");
				String residence = "";
				if(lista!=null && !lista.isEmpty()){
					residence=	(String)lista.get(0);
				}

				redisUtil.hset(reportSummaryx,"residence", addressCompletion+""+ residence);
			}else if(StringUtils.contains(question,question5)){
				redisUtil.hset(reportSummaryx,"education",humanResponse);

			}

			String format = "";
			// 手机号码验证
			if(StringUtils.contains(ask,question7)) {
				Map basicSituation = new HashMap();
				basicSituation = algorithmCall(question);

				List list1 =   (List) basicSituation.get("result");
				List list2 = (List) list1.get(0);

				Map map1 = (Map) list2.get(0);
				List list3 =  (List) map1.get("联系方式");
				String message = (String) list3.get(0);
				if(StringUtils.contains(message,"号码格式错误")){
					list.add("question7");
					format="("+message+")";
				}else{
					redisUtil.hset(reportSummaryx,"contact",humanResponse);

				}

			}


			if(list ==null || list.isEmpty()) {
				nextQuestion=question8 ;
			}else {

				Collections.sort(list);
				nextQuestion = questionMap.get((String) list.get(0));

				if(StringUtils.contains(format,"号码格式错误")){
					nextQuestion +=format;

				}

				list.remove(0);

                redisUtil.del(questionQueueRedis);
                if(!list.isEmpty()){
                    redisUtil.lSet(questionQueueRedis,list);
                }

			}

			log.info("报案摘要：\t"+redisUtil.hmget(reportSummaryx));

		}else if(StringUtils.contains(ask, question8 )) {
			// 因何事到公安机关？

			Map caseClassification = new HashMap();
			caseClassification = algorithmCall(question);


			//算法接口返回结果：	{result=[[{casetime={案发开始=6月24日下午15时30分, 案发结束=6月24日晚上19时30分}, address=[北滘港员工村b栋560房], goods=[{自报价格=, 品牌=华为, 物品名称=华为p20手机, 物品数量=一台, 颜色=黑色, 物品类型=手机}], casereason=入室盗窃, basecasereason=盗窃, periodtime=白天}]], clientid=22222, messageid=111111111, resultcode=000}
			List list1 =   (List) caseClassification.get("result");
			List list2 = (List) list1.get(0);
			Map map1 = (Map) list2.get(0);

			// 案发开始 案发结束
			Map<String, Object> casetime =  (Map<String, Object>) map1.get("casetime");
			// 案发地点
			List address =  (List) map1.get("address");

			// 被盗物品信息
			List goods =  (List) map1.get("goods");

			String monitor =  (String) map1.get("monitor");
			redisUtil.hset(reportSummaryx,"monitor",monitor);

			Double moneysum = null;
			if(map1.containsKey("moneysum")) {
				moneysum = (double) map1.get("moneysum");// 被盗物品总价值
				redisUtil.hset(reportSummaryx,"moneysum",moneysum.toString());

			}


			String selfPrice = "";
			String itemType = "";
			String nameOfGoods = "";
			String brand = "";
			String quantity  = "";
			String color = "";

			// [{自报价格=3000元, 品牌=华为, 物品名称=华为p20手机, 物品数量=一台, 颜色=黑色, 物品类型=手机  }]
			if(goods != null && goods.size()>0) {
				Map good1 = (Map) goods.get(0);

				selfPrice = (String) good1.get("自报价格");
				itemType = (String) good1.get("物品类型"); // 家电
				nameOfGoods = (String) good1.get("物品名称");
				brand = (String) good1.get("品牌");
				quantity  = (String) good1.get("物品数量");
				color = (String) good1.get("颜色");


				redisUtil.hset(reportSummaryx,"selfPrice",selfPrice);
				redisUtil.hset(reportSummaryx,"itemType",itemType);
				redisUtil.hset(reportSummaryx,"nameOfGoods",nameOfGoods);
				redisUtil.hset(reportSummaryx,"brand",brand);
				redisUtil.hset(reportSummaryx,"quantity",quantity);
				redisUtil.hset(reportSummaryx,"color",color);

			}

			// 案件要素
			List caseElements = new ArrayList();

			// 案件类别
			String basecasereason = (String) map1.get("basecasereason");
			// 细分案件类别
			String casereason = (String) map1.get("casereason");
			// 作案区间
			String periodtime = (String) map1.get("periodtime");
			//被盗手机号码
			List phone = (List) map1.get("phone");

			redisUtil.hset(reportSummaryx,"basecasereason",basecasereason);
			redisUtil.hset(reportSummaryx,"casereason",casereason);

			redisUtil.hset(reportSummaryx,"periodtime",periodtime);

			if(StringUtils.isNotBlank(basecasereason)) {
				// 假如是 抢劫 或者 诈骗，下一个问题就是 ：“你被抢了什么？” 或者“你被骗了什么？”，结束
				if(StringUtils.equals(basecasereason, "诈骗")) {
					caseClassification.put("nextQuestion", "请将被诈骗的具体过程描述一下？");

					log.info("casecClassification: "+caseClassification);
					return caseClassification;

				}else if(StringUtils.equals(basecasereason, "抢劫")) {
					caseClassification.put("nextQuestion", "请将被抢劫的具体过程描述一下？");

					log.info("casecClassification: "+caseClassification);
					return caseClassification;

				}else if(StringUtils.equals(basecasereason, "抢夺")) {
					caseClassification.put("nextQuestion", "请将被抢夺的具体过程描述一下？");

					log.info("casecClassification: "+caseClassification);
					return caseClassification;

				}else if(StringUtils.contains(basecasereason, "盗窃")
						|| StringUtils.contains(casereason, "盗窃")) {

					if(address== null || address.isEmpty()) {
						caseElements.add("question10");

					}else {
						redisUtil.hset(reportSummaryx,"address",addressCompletion+""+(String) address.get(0));

					}


					if(casetime == null || casetime.isEmpty()) {
						caseElements.add("question11");
					}else {
						redisUtil.hset(reportSummaryx,"caseStart",(String) casetime.get("案发开始"));
						redisUtil.hset(reportSummaryx,"caseEnd",(String) casetime.get("案发结束"));

					}

					/*if(StringUtils.isBlank(itemType)) {
						caseElements.add("question11_1");

					}*/



					// 物品名称： 电视机
					if(StringUtils.isBlank(nameOfGoods)) {
						caseElements.add("question11_1");
					}

					/* 品牌
					 *
					 **/
					if(StringUtils.isBlank(brand)) {
						 caseElements.add("question12");
					}

					if(StringUtils.isBlank(color)) {
						caseElements.add("question14");
					}

					if(StringUtils.equals(itemType, "手机")) {
						if( (phone == null ||  StringUtils.contains((String) phone.get(0), "号码格式错误")) ) {
							caseElements.add("question15"); // 手机号码

						}else {
							redisUtil.hset(reportSummaryx,"stolenPhone",phone);
						}
					}

					if(moneysum== null) {
						caseElements.add("question16");
					}

					log.info("未抽取到\t"+caseElements.size()+"\t个案件要素");
					log.info("未抽取到的要素:\t"+caseElements);

					log.info("报案摘要：\t"+redisUtil.hmget(reportSummaryx));

				}else {

					caseClassification.put("nextQuestion", "请将作案具体过程描述一下？");

					log.info("casecClassification: "+caseClassification);
					return caseClassification;
				}



			}


			if(caseElements.isEmpty()) {
				caseClassification.put("nextQuestion", questionMap.get("question17"));

			}else {
				Collections.sort(caseElements);// 排序
				caseClassification.put("nextQuestion",  questionMap.get(caseElements.get(0)) );

				caseElements.remove(0);

                redisUtil.del(questionQueueRedis);
                redisUtil.lSet(questionQueueRedis,caseElements);

			}
			log.info("casecClassification: "+caseClassification);

			return caseClassification;

		}else if(  StringUtils.contains(ask, question10)
				|| StringUtils.contains(ask, question11)
				|| StringUtils.contains(ask, question11_1)
				|| StringUtils.contains(ask, question12)
				|| StringUtils.contains(ask, question13)
				|| StringUtils.contains(ask, question14)
				|| StringUtils.contains(ask, question15)) {


            List list = questionQueuem;

            String format = "";
			if(StringUtils.contains(ask,question15)) {
				// 手机号码验证
				Map basicSituation = new HashMap();
				basicSituation = algorithmCall(question);

				//算法接口返回结果：	{result=[[{persioninfo={联系方式=[17666212629], 身份证号码=[362422199602103037], 户籍地=, 居住地=佛山市顺德区北滘镇北滘港员工村B栋560房, 姓名=刘桃, 性别=男, 年龄=23}}]], clientid=22222, messageid=111111111, resultcode=000}
				List list1 =   (List) basicSituation.get("result");
				List list2 = (List) list1.get(0);
				Map map1 = (Map) list2.get(0);
				List list3 =  (List) map1.get("联系方式");
				String message = (String) list3.get(0);
				if(StringUtils.contains(message,"号码格式错误")){
					list.add("question15");
					format="("+message+")";

				}else{
					redisUtil.hset(reportSummaryx,"stolenPhone",message);

				}

			}else if(StringUtils.contains(ask,question11)) {

                Map elements = this.getElementsOfCase(question);

                String periodtime = (String)elements.get("periodtime");

                Map timeArea = (Map)elements.get("casetime");

                if(timeArea!=null && !timeArea.isEmpty()){

                    String start = (String)timeArea.get("案发开始");
                    String end = (String)timeArea.get("案发结束");

                    redisUtil.hset(reportSummaryx,"caseStart",start);
                    redisUtil.hset(reportSummaryx,"caseEnd",end);
                }

                redisUtil.hset(reportSummaryx,"periodtime",periodtime);

            }else if(StringUtils.contains(ask,question10)) {
                // 案发地点
                Map map =this.getElementsOfCase(question);
                List lista = (List)map.get("address");
                String address="";
                if(lista!=null && !lista.isEmpty()){
                    address = (String)lista.get(0);

                }

                redisUtil.hset(reportSummaryx,"address",addressCompletion+""+address);

            }else if(StringUtils.contains(ask,question14)) {
                // 颜色
                redisUtil.hset(reportSummaryx,"color",humanResponse);

            }else if(StringUtils.contains(ask,question12)) {
                // 品牌
                redisUtil.hset(reportSummaryx,"brand",humanResponse);

            }else if(StringUtils.contains(ask,question11_1)) {

                //  案发时间  案发区间

                Map good1 = this.getGoodsOfCase(question);

                String selfPrice = (String) good1.get("自报价格");
                String itemType = (String) good1.get("物品类型"); // 家电
                String nameOfGoods = (String) good1.get("物品名称");
                String brand = (String) good1.get("品牌");
                String quantity  = (String) good1.get("物品数量");
                String color = (String) good1.get("颜色");

                redisUtil.hset(reportSummaryx,"selfPrice",selfPrice);
                redisUtil.hset(reportSummaryx,"itemType",itemType);
                redisUtil.hset(reportSummaryx,"nameOfGoods",nameOfGoods);
                redisUtil.hset(reportSummaryx,"brand",brand);
                redisUtil.hset(reportSummaryx,"quantity",quantity);
                redisUtil.hset(reportSummaryx,"color",color);

                if(StringUtils.contains(itemType,"手机")){
                    list.add("question15");

                }



            }



            if(list == null || list.isEmpty()) {
                // 你有否保留到购买被盗窃手机的购置单据？
                nextQuestion=question17 ;
            }else {


				Collections.sort(list);
				nextQuestion = questionMap.get((String) list.get(0));

				if(StringUtils.contains(format,"号码格式错误")){
					nextQuestion+=format;
				}else if(StringUtils.contains(nextQuestion,question12)){
					//  被盗的（物品名称）是什么品牌？

                    String nameOfGoods = (String) redisUtil.hget(reportSummaryx,"nameOfGoods");

					if(StringUtils.isNotBlank(nameOfGoods)){
						nextQuestion="被盗的"+nameOfGoods+"是什么品牌？";

                    }

				}


				list.remove(0);

                redisUtil.del(questionQueueRedis);
                if(!list.isEmpty()){
                    redisUtil.lSet(questionQueueRedis,list);
                }


			}

			log.info("报案摘要：\t"+redisUtil.hmget(reportSummaryx));

		}else if(StringUtils.contains(ask, question16)) {

			// 调用接口，抽取 “被盗物品价值多少？"
			Map map1 = (Map) this.getElementsOfCase(question);
			String invoice = (String) map1.get("invoice");// 被盗物品发票
			redisUtil.hset(reportSummaryx,"invoice",invoice);

			Double moneysum = null;
			if(map1.containsKey("moneysum")) {
				moneysum = (double) map1.get("moneysum");// 被盗物品总价值
			}
			if(moneysum !=null){
				// 现价值
				redisUtil.hset(reportSummaryx,"moneysum",moneysum);
				redisUtil.hset(reportSummaryx,"selfPrice",moneysum);
			}

			log.info("报案摘要：\t"+redisUtil.hmget(reportSummaryx));

			if(StringUtils.equals(invoice,"有")) {
				// 智能识别出购置单据，直接问问题20

				// 监控
				String monitor = (String) redisUtil.hget(reportSummaryx,"monitor");

				if(StringUtils.equals(monitor,"有")){
					nextQuestion= question22;
				}else{
					nextQuestion= question20;
				}


			}else {
				// 未识别出
				nextQuestion=question17; //"你有否保留到购买被盗窃手机的购置单据？
			}


		}else if(StringUtils.contains(ask, "购置单据")) {

			String monitor =(String )redisUtil.hget(reportSummaryx,"monitor");

			Map map1 =this.getElementsOfCase(question);
			String invoice = (String)map1.get("invoice") ;
			redisUtil.hset(reportSummaryx,"invoice",invoice);


			if(StringUtils.equals(monitor,"有")){
				nextQuestion=question22; //以上所说是否属实？

			}else{
				nextQuestion=question20; //以上所说是否属实？

			}


		}


		else if(StringUtils.contains(ask, question20)) {

			Map map1 = getElementsOfCase(question);
			String monitor = (String)map1.get("monitor");
			redisUtil.hset(reportSummaryx,"monitor",monitor);
			log.info("报案摘要：\t"+redisUtil.hmget(reportSummaryx));

			nextQuestion=question22; //以上所说是否属实？


		}else if(StringUtils.contains(question,question24)) {
			// 返回上一个问题
//            nextQuestion = (String)redisUtil.lGetIndex(asked,size-1);

            nextQuestion = (String)redisUtil.rpop(asked);

            System.out.println("已问问题数目:\t"+redisUtil.lGetListSize(asked));

		}else if(StringUtils.contains(question,question22)){
			// "以上所说是否属实？
			Map reportSummary =redisUtil.hmget(reportSummaryx);
			log.info("报案摘要：\t"+reportSummary);

			ret.put("reportSummary",reportSummary);

		}


		ret.put("nextQuestion", nextQuestion);

		log.info("下一个问题是：\t"+nextQuestion);


		return ret;

	}



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
		// 答案之前必须加要给换行符
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
		Map basicSituation = oecClient.getResult(jsonStr);
		log.info(".......算法接口返回结果：\t"+basicSituation);
		return basicSituation;
	}




	/**
	 *   获取HttpServletRequest头文件信息
	 *
	 * @param request
	 * @return
	 */
	public static Map<String, String> getHeadersInfo(HttpServletRequest request) {
		Map<String, String> map = new HashMap<String, String>();
		Enumeration headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String key = (String) headerNames.nextElement();
			if(StringUtils.equals(key, "host")) {
				String value = request.getHeader(key);
//			System.out.println(key + "：" + value);
				map.put(key, value);
				break;
			}
		}
		return map;
	}

    /**
	 *   抽取案件当中的物品
	 * **/
	public Map getGoodsOfCase(String question){
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
	 *  抽取案件要素信息
	 * **/
	public Map getElementsOfCase(String question){

		Map caseClassification = new HashMap();
		caseClassification = algorithmCall(question);
		List list1 =   (List) caseClassification.get("result");
		List list2 = (List) list1.get(0);
		Map map1 = (Map) list2.get(0);

		return map1;
	}



	public static void main(String[] args) {

		String str ="问：请问您的基本情况？";
        str = str.replace("问：","");

		System.out.println(str);



	}





}
