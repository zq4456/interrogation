package com.dinfo.robotea.controller;

import com.dinfo.robotea.http.OecClient;
import com.dinfo.robotea.properties.QuestionProperties;
import com.dinfo.robotea.properties.RedisUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 *	顺德公安 智慧接审讯审讯系统
 */
@Controller
@RequestMapping("/interrogate")
public class InterrogateController {

	private static final Logger log = LoggerFactory.getLogger(InterrogateController.class);

	@Autowired
	private OecClient oecClient;

	@Autowired
	private RedisUtil redisUtil;

//	@Autowired
//	private IdentityCardVerification identityCardVerification;

	@Autowired
	private QuestionProperties questionProperties;



	// 案发地址补全前缀
    private final static  String addressCompletion = "广东省佛山市顺德区";

	private final static  String question1="我们是广东省佛山市顺德区公安局民警，现就有关案情依法对你进行讯问并进行录音录像，你必须如实回答，不得隐瞒事实或者作假口供，不得诬告及陷害他人，否则要负相应的法律责任，对与本案无关的问题你有权拒绝回答的权利，你知道吗？";

    private final static  String question2="你是否人大代表或政协委员？";
	private final static  String question3="你是否需要办案人员回避？";
    private final static  String question4="你有否受过刑事处罚、行政拘留、劳动教养、收容教养、收容教育、强制戒毒、强制隔离戒毒、社区戒毒等处罚？";
	private final static  String question5="你是否需要聘请律师为你辨护？如因经济困难可申请法律援助。";
	private final static  String question6="你有否收到《犯罪嫌疑人诉讼权利义务告知书》，对其内容是否清楚明白？";
	private final static  String question7="如实供述自己的罪行可以从轻或减轻处罚规定，你是否清楚？";


	private final static  String question8="你的个人基本情况？";

	private final static  String question09="你的姓名？";
	private final static  String question09_1="你的性别？";
	private final static  String question09_2="你的民族？";
	private final static  String question10="你的身份证号码是多少？";
	private final static  String question11="你的出生日期是什么？";
	private final static  String question12="户籍地在哪里？";
	private final static  String question13="文化程度？";
	private final static  String question14="现在住在哪里？";
	private final static  String question16="联系电话？";

	private final static  String question15="工作单位？";
	private final static  String question17="什么时候毕业？毕业学校是什么？";
	private final static  String question18="什么时候出来工作？当时在哪里工作？什么时候在现在这个单位工作？";
	private final static  String question19="你的父亲叫什么名字？几岁了？做什么？";
	private final static  String question20="你的母亲叫什么名字？几岁了？做什么？";
	private final static  String question21="你是否有兄弟姐妹？几岁，做什么？";
	private final static  String question22="你是否有配偶？几岁，做什么？";

	private final static  String question23="你因何种违法事实被问话？";
	private final static  String question24="你是否有参与盗窃的行为？";
	private final static  String question25="你描述下盗窃的具体情况？";

	private final static  String question25_1="你盗窃的什么物品？";


	private final static  String question26="具体盗窃地点是哪里？";
	private final static  String question27="具体盗窃时间？";
	private final static  String question28="你偷的物品名称是什么样子的？";
	private final static  String question29="什么品牌？";
	private final static  String question30="什么颜色？";

	private final static  String question31="你有否同伙？";
	private final static  String question31_1="你的同伙是谁？";

	private final static  String question32="你有否使用工具？";
	private final static  String question32_1="用什么工具？";


	private final static  String question33="你所偷得的物品名称是属于谁的？";
	private final static  String question34="你为何要偷物品名称？";
	private final static  String question35="盗窃的物品名称现在在哪里？";
	private final static  String question36="你有无其他的违法犯罪行为？";
	private final static  String question37="公安机关对你传唤讯问期间,有否对你进行刑讯逼供或不文明的言行？";
	private final static  String question38="公安机关在对你传唤期间,是否有保障你的饮食和必要休息时间？";
	private final static  String question39="你还有何补充？";
	private final static  String question40="你以上所讲是否属实？";

	private final static  String question54 = "回到上一个问题";

	private final static  String manualntervention="结束人工干预";




	private static HashMap<String,String> questionMap= new HashMap<String,String>();
	static{
		questionMap.put("question09", question09);
		questionMap.put("question09_1", question09_1);
		questionMap.put("question09_2", question09_2);
		questionMap.put("question10", question10);
		questionMap.put("question11", question11);
		questionMap.put("question12", question12);
		questionMap.put("question13", question13);
		questionMap.put("question14", question14);
		questionMap.put("question16", question16);

		questionMap.put("question25_1", question25_1);
		questionMap.put("question26", question26);
		questionMap.put("question27", question27);
		questionMap.put("question28", question28);
		questionMap.put("question29", question29);
		questionMap.put("question30", question30);


	}

	private final static  long limitRecord=100; // 笔录问题数目

	private  static String formatPhone = ""; // 手机格式 是否正确
	private  static String formatIdcards = ""; // 身份证号码 格式 是否正确


	/**
	 *  审讯系统测试主页
	 * @return
	 */
	@RequestMapping(value="/index")
    public String getIndex() {
        return "interrogateIndex";
    }

	/** 审讯要素 githubss
	 * @return
	 */
	@PostMapping(value="/getElement")
	@ResponseBody
	public  Object getElement(String cookieUuid){

        String reportSummaryx = "reportSummary_"+cookieUuid;
        Map<Object, Object> map = redisUtil.hmget(reportSummaryx);
		return map;
	}

	/** 审讯笔录
	 * @return
	 */
	@PostMapping(value="/getRecord")
	@ResponseBody
	public  Object getRecord(String cookieUuid){

		String wholeQuestions = "wholeQuestions_"+cookieUuid;
        List<String> list = (List)redisUtil.lGet(wholeQuestions,0,limitRecord);
		return list;


	}




	/** 机器人问答接口
	 * @return
	 */
	@PostMapping(value="/getAnswer")
	@ResponseBody
	@SuppressWarnings({ "unchecked", "unused", "rawtypes" })
	public Object getAnswer(String question,HttpServletRequest request) {

		// redis缓存失效时间  60l*60*24 24小时
        long expireTime1 = Long.valueOf(questionProperties.getExpire()) * 60;

        Map<String, Object> ret =new HashMap<String,Object>();

        // 模拟客户端 cookie
        String cookieUuid = request.getParameter("cookieUuid");

        String asked = "asked_"+cookieUuid;
        log.info("cookieUuid：\t"+asked);

         // 笔录redis变量
		String wholeQuestions = "wholeQuestions_"+cookieUuid;
		log.info("wholeQuestions：\t"+wholeQuestions);

		// 下一个问题redis变量，(人工干预返回自动问答，取值)
		String nextQuestionRedis = "nextQuestion_"+cookieUuid;

		// 人员信息和案件要素 问题队列 start
        String questionQueueRedis = "questionQueue_"+cookieUuid;

		// 记录器，身份证号码与 手机号连续 2次不通过，就不再问。
		String countRedis = "countRedis_"+cookieUuid;
		redisUtil.expire(countRedis,expireTime1);


        // 设置缓存变量失效时间
		redisUtil.expire(questionQueueRedis,expireTime1);

		long expireTime = redisUtil.getExpire(questionQueueRedis);
		System.out.println(questionQueueRedis+"\t expireTime for 人员信息和案件要素 问题队列 \t:"+expireTime);

		redisUtil.expire(asked,expireTime1);
		expireTime = redisUtil.getExpire(asked);
		System.out.println(asked+"\t expireTime for 已问问题 \t:"+expireTime);

		redisUtil.expire(wholeQuestions,expireTime1);
		expireTime = redisUtil.getExpire(wholeQuestions);
		System.out.println(wholeQuestions+"\t expireTime for 笔录 \t:"+expireTime);

		redisUtil.expire(nextQuestionRedis,expireTime1);
		expireTime = redisUtil.getExpire(nextQuestionRedis);
		System.out.println(nextQuestionRedis+"\t expireTime for 下一个问题 \t:"+expireTime);




		if(StringUtils.contains(question,question2)
		|| StringUtils.contains(question,question3)||StringUtils.contains(question,question4)
		|| StringUtils.contains(question,question5)||StringUtils.contains(question,question6)
		|| StringUtils.contains(question,question7)	){

			redisUtil.lSet(wholeQuestions,question);
//			System.out.println("******\t审讯笔录:\t"+redisUtil.lGet(wholeQuestions,0,limitRecord));
		}



        // 人员信息和案件要素 问题队列 start

        List questionQueuem = redisUtil.lGet(questionQueueRedis,0,limitRecord) ;
        System.out.println("############### "+questionQueueRedis+":\t"+questionQueuem);

//		log.info("机器人提问 : "+question);

		String humanResponse="";
		String ask="";
		if(question.contains("答：")) {

			String[] ques = question.split("答：");

			if(ques.length>1){

				ask = ques[0].replaceAll(" ", "").replaceAll("\n", "");
				// 审讯人回答 内容,去除换行符.
				humanResponse=ques[1].replaceAll(" ", "").replaceAll("\n", "");

				System.out.println("问题是:\t"+ask);
				System.out.println("回答是:\t"+humanResponse);
			}

		}

		if(	!StringUtils.equals(question,question54)
				&& !StringUtils.contains(question,manualntervention)
				&& !StringUtils.contains(question,"号码格式错误")
				){

            String str = StringUtils.replace(ask,"问：","");
            redisUtil.lSet(asked,str);

        }


        long size1 = redisUtil.lGetListSize(asked) ;
        String lastQuestion = (String)redisUtil.lGetIndex(asked,size1-1);
        System.out.println("上一个问题是：\t"+lastQuestion);
//        System.out.println("已问问题题列表:\t"+redisUtil.lGet(asked,0,20));
//        System.out.println("已问问题数目:\t"+size1);

        // 案件要素
        String reportSummaryx = "reportSummary_"+cookieUuid;
		redisUtil.expire(reportSummaryx,expireTime1);
		expireTime = redisUtil.getExpire(reportSummaryx);
		System.out.println(reportSummaryx+"\t expireTime for 案件要素 \t:"+expireTime);

		System.out.println("案件提取要素：\t"+redisUtil.hmget(reportSummaryx));


        // 下一个问题
        String nextQuestion="";

        // 开始提问  ******************************************************************************
        if(StringUtils.contains(question, StringUtils.substring(question1,0,16))) {

			// 在问第一个问题之前，初始化 redis 审讯要素变量 和  审讯笔录变量

			redisUtil.del(wholeQuestions); // 清空笔录
			redisUtil.del(reportSummaryx); // 清空要素
			redisUtil.del(questionQueueRedis);// 清空 个人信息 和 案件 细节待问问题列表
			redisUtil.del(nextQuestionRedis); // 清空 下一个问题

  			// 计数器初始化
			redisUtil.hset(countRedis,"phone",0);
			redisUtil.hset(countRedis,"idcard",0);

			redisUtil.lSet(wholeQuestions,question); // 开始记录笔录

			if( StringUtils.contains(humanResponse, "不清楚")
					|| StringUtils.contains(humanResponse, "不知道")
					|| StringUtils.contains(humanResponse, "不")) {
				nextQuestion=question1;
			}else {
				nextQuestion=question2;

			}

		}else if(StringUtils.contains(question, StringUtils.substring(question2,0,10))) {
			nextQuestion=question3;

		}else if(StringUtils.contains(question, StringUtils.substring(question3,0,10))) {
			nextQuestion=question4;

		}else if(StringUtils.contains(question, StringUtils.substring(question4,0,10))) {
			nextQuestion=question5;

		}else if(StringUtils.contains(question, StringUtils.substring(question5,0,10))) {
			nextQuestion=question6;

		}else if(StringUtils.contains(question, StringUtils.substring(question6,0,15))) {
			nextQuestion=question7;

		}else if(StringUtils.contains(question, StringUtils.substring(question7,0,15))) {
			nextQuestion=question8;

		}else if(StringUtils.contains(question, StringUtils.substring(question8,0,8))) {
			// 你的个人基本情况？
			redisUtil.del(reportSummaryx);


			Map basicSituation = new HashMap();
			basicSituation = algorithmCall(question);

			List list1 =   (List) basicSituation.get("result");
			List list2 = (List) list1.get(0);
			Map map1 = (Map) list2.get(0);
			Map<String, Object> map2 =  (Map<String, Object>) map1.get("persioninfo");

			List push = new ArrayList();

			// 性别，籍贯，出生日期 三者由 身份证号码带出来。
			String jiguan="";

			for(Map.Entry<String, Object> entry : map2.entrySet() ){
				String mapKey = entry.getKey();
				Object mapValue = entry.getValue();
//			    System.out.println(mapKey+":"+mapValue);



				if(StringUtils.equals(mapKey, "姓名")) {
					if(StringUtils.isNotBlank((String) mapValue)) {
						redisUtil.hset(reportSummaryx,"name",mapValue);
					}else {
						push.add("question09");

					}

				}else if(StringUtils.equals(mapKey, "居住地")) {
					if(StringUtils.isBlank((String)mapValue)) {
						push.add("question14");
					}else{
						redisUtil.hset(reportSummaryx,"residence",addressCompletion+ mapValue);

					}


				}else if(StringUtils.equals(mapKey, "受教育程度")) {
					if(StringUtils.isBlank((String)mapValue)) {
						push.add("question13");
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
						push.add("question16");
					}else if(StringUtils.contains(contact,"号码格式错误")){

						push.add("question16");
//						formatPhone ="("+contact+")";

						redisUtil.hset(countRedis,"phone",1);

					}else {
						redisUtil.hset(reportSummaryx,"contact",contact);

					}

				}else if(StringUtils.equals(mapKey, "身份证号码")) {
					List idCard = (List)mapValue;
					String idCardStr="";

					if(!idCard.isEmpty()){

						idCardStr = (String)idCard.get(0);

						// 身份证号码格式验证 formatIdcards
//						String msg = identityCardVerification.verification(idCardStr);
						String msg = "";
						redisUtil.hset(reportSummaryx,"idCard",idCardStr);

						if(StringUtils.isBlank(idCardStr)){
							// 格式错误
							push.add("question10");
							redisUtil.hset(countRedis,"idcard",1);

						}


					}else {

						push.add("question10");
					}


				}else if(StringUtils.equals(mapKey, "户籍地")) {
					String householRegister = (String)mapValue;
					if(StringUtils.isNotBlank(householRegister)){
						redisUtil.hset(reportSummaryx,"householRegister",householRegister);

					}else {
						push.add("question12");
					}


				}else if(StringUtils.equals(mapKey, "籍贯")) {
					String householRegister = (String)mapValue;
					if(StringUtils.isNotBlank(householRegister)){
						jiguan= householRegister;
					}

				}else if(StringUtils.equals(mapKey, "性别")) {
					String gender = (String)mapValue;
					if(StringUtils.isNotBlank(gender)){
						redisUtil.hset(reportSummaryx,"gender",gender);

					}else{
						push.add("question09_1");

					}

				}else if(StringUtils.equals(mapKey, "年龄")) {
					String age = (String)mapValue;
					if(StringUtils.isNotBlank(age)){
						redisUtil.hset(reportSummaryx,"age",age);

					}

				}else if(StringUtils.equals(mapKey, "出生日期")) {
					String birthday = (String)mapValue;
					if(StringUtils.isNotBlank(birthday)){
						redisUtil.hset(reportSummaryx,"birthday",birthday);

					}else{
						push.add("question11");
					}

				}else if(StringUtils.equals(mapKey, "民族")) {
					String nation = (String)mapValue;
					if(StringUtils.isNotBlank(nation)){
						redisUtil.hset(reportSummaryx,"nation",nation);

					}else{
						push.add("question09_2");

					}


				}



			}

			// 籍贯
			if(StringUtils.isNotBlank(jiguan)){
				redisUtil.hset(reportSummaryx,"householRegister",jiguan);
				push.remove("question12");

			}

			// 性别，户籍地，出生日期 三者将被 身份证号码带出来。所以问题列表中存在后者，前三者就没必要再问了
			/*if(push.contains("question10")){
				push.remove("question09_1");
				push.remove("question11");
				push.remove("question12");

			}*/


			if(push.isEmpty()) {
				// 如果 基本情况都被接口 提取出来了，跳到问题15
				basicSituation.put("nextQuestion", question15);
			}else {

				Collections.sort(push);// 排序
				String nexta = questionMap.get(push.get(0));

				/*if(StringUtils.contains(nexta,question16)
						&& StringUtils.contains(formatPhone,"号码格式错误")){
//					nexta+= "（号码格式错误）";

					// format复位
					formatPhone ="";

				}else if(StringUtils.contains(nexta,question10)
						&& StringUtils.isNotBlank(formatIdcards)){

//					nexta+=formatIdcards;

					// format复位
					formatIdcards= "" ;

				}*/

				basicSituation.put("nextQuestion",  nexta );

				push.remove(0);

				redisUtil.del(questionQueueRedis);
				redisUtil.lSet(questionQueueRedis,push);

			}

			log.info("审讯摘要：\t"+redisUtil.hmget(reportSummaryx));
			log.info("basicSituation:\t"+basicSituation);

			redisUtil.set(nextQuestionRedis,basicSituation.get("nextQuestion"));
			System.out.println("\t下一个问题是：\t"+(String)redisUtil.get(nextQuestionRedis));

			return basicSituation;

		}else if(StringUtils.contains(question,question09)
				|| StringUtils.contains(question,question09_1)
				|| StringUtils.contains(question,question09_2)
				|| StringUtils.contains(question,question10)
				|| StringUtils.contains(question,question11)
				|| StringUtils.contains(question,question12)
				|| StringUtils.contains(question,question13)
				|| StringUtils.contains(question,question14)
				|| StringUtils.contains(question,question16)
				) {

			List list = questionQueuem;

			Map personInfo =this.algorithmPersonInfo(question);

			if(StringUtils.contains(question,question09)){
				String name2 = (String)personInfo.get("姓名");
				redisUtil.hset(reportSummaryx,"name", name2);

			}else if(StringUtils.contains(question,question09_1)){

				String gender = (String)personInfo.get("性别");
				if(StringUtils.isBlank(gender)){
				    gender=humanResponse;
                }

				redisUtil.hset(reportSummaryx,"gender",gender);

			}else if(StringUtils.contains(question,question09_2)){

				String nation = (String)personInfo.get("民族");
				redisUtil.hset(reportSummaryx,"nation",nation);

			}else if(StringUtils.contains(question,question10)){
                // TODO  单独问身份证号时，将 性别，户籍地，出生日期 三者一起返回，用于覆盖之前说的。

				// 身份证号码去除符号
				question= StringUtils.replace(question,"，","");
				question= StringUtils.replace(question,"。","");

                Map map2 = this.algorithmElementsOfCase(question);
                String idcardStr="";
                idcardStr = (String)map2.get("身份证号");

//                String msg = identityCardVerification.verification(idcardStr);

				if(StringUtils.isNotBlank(idcardStr)){
					// 格式正确
					redisUtil.hset(reportSummaryx,"idCard",idcardStr);

				}else{
					// 格式错误
					// TODO 身份证号码号 若连续 2 次不通过，就不再问。
					int count = (int)redisUtil.hget(countRedis,"idcard");
					count++;
					redisUtil.hset(countRedis,"idcard",count);

					if(count < 3){
						list.add("question10");
					}else{
						redisUtil.hset(reportSummaryx,"idCard","");
					}


					formatIdcards="（号码格式错误）";


				}



				redisUtil.hset(reportSummaryx,"idCard",idcardStr);

			}else if(StringUtils.contains(question,question11)){

				String birthday ="";
				birthday= (String) personInfo.get("出生日期");

				/*if(!list3.isEmpty()){
				    birthday=(String)list3.get(0);
                }*/
                if(StringUtils.isBlank(birthday)){
				    birthday= humanResponse;
                }

				redisUtil.hset(reportSummaryx,"birthday",birthday);

			}else if(StringUtils.contains(question,question12)){

				String householRegister = "";

				Map map =this.algorithmElementsOfCase(question);
				List lista = (List)map.get("address");
				if(lista!=null && !lista.isEmpty()){
					householRegister = (String)lista.get(0);

				}

				redisUtil.hset(reportSummaryx,"householRegister",householRegister);

			}else if(StringUtils.contains(question,question13)){
				String education = (String) personInfo.get("受教育程度");
				redisUtil.hset(reportSummaryx,"education",education);

			}else if(StringUtils.contains(question,question14)){

				String residence="";
				residence = (String) personInfo.get("居住地");

				if(StringUtils.isBlank(residence)){
					Map map =this.algorithmElementsOfCase(question);
					List lista = (List)map.get("address");
					if(lista!=null && !lista.isEmpty()){
						residence = (String)lista.get(0);

					}

				}


				redisUtil.hset(reportSummaryx,"residence",residence);

			}else if(StringUtils.contains(question,question16)){

				// 手机号码去除符号
				question= StringUtils.replace(question,"，","");
				question= StringUtils.replace(question,"。","");



				Map map = this.algorithmElementsOfCase(question) ;
				List contact = (List) map.get("联系方式");

				String contactStr ="";
				if(!contact.isEmpty()){
					contactStr=(String)contact.get(0);
				}



				if(StringUtils.contains(humanResponse,"没")
						|| StringUtils.contains(humanResponse,"无") ){

					// 手机号可以没有
					redisUtil.hset(reportSummaryx,"contact","");

				}else if(StringUtils.contains(contactStr,"号码格式错误")){
					formatPhone ="("+contactStr+")";

					// TODO 手机号连续 2次不通过，就不再问。
					int count = (int)redisUtil.hget(countRedis,"phone");
					count++;
					redisUtil.hset(countRedis,"phone",count);

					if(count < 3){
						list.add("question16");

					}else{
						redisUtil.hset(reportSummaryx,"contact","");
					}

				}else{
					redisUtil.hset(reportSummaryx,"contact",contactStr);

				}



			}

			if(list ==null || list.isEmpty()) {
				nextQuestion=question15 ; // 工作单位 11_17
			}else {

				Collections.sort(list);
				nextQuestion = questionMap.get((String) list.get(0));

				if(StringUtils.contains(nextQuestion,question16)
						&&StringUtils.contains(formatPhone,"号码格式错误")){
					nextQuestion += "（号码格式错误）";

					// format复位
					formatPhone ="";

				}else if(StringUtils.contains(nextQuestion,question10)
						&& StringUtils.isNotBlank(formatIdcards)){

					nextQuestion+="（号码格式错误）";

					// format复位
					formatIdcards= "" ;

				}

				list.remove(0);

                redisUtil.del(questionQueueRedis);
                if(!list.isEmpty()){
                    redisUtil.lSet(questionQueueRedis,list);
                }

			}

			log.info("审讯摘要：\t"+redisUtil.hmget(reportSummaryx));

		} else  if(StringUtils.contains(question,StringUtils.substring(question15,0,4))){

			String workUnit = humanResponse;
			redisUtil.hset(reportSummaryx,"workUnit",workUnit);

			nextQuestion=question17;

			// 更新 审讯笔录
			StringBuilder  temp=new StringBuilder("\n问：你的个人基本情况？\n答：");


			temp.append("姓名：").append(redisUtil.hget(reportSummaryx,"name"));
			temp.append("，出生日期：").append(redisUtil.hget(reportSummaryx,"birthday"));
			temp.append("，身份证号码：").append(redisUtil.hget(reportSummaryx,"idCard"));
			// 性别 民族
			temp.append("，性别：").append(redisUtil.hget(reportSummaryx,"gender"));
			temp.append("，民族：").append(redisUtil.hget(reportSummaryx,"nation"));

			temp.append("，户籍地：").append(redisUtil.hget(reportSummaryx,"householRegister"));
			temp.append("，文化程度：").append(redisUtil.hget(reportSummaryx,"householRegister"));
			temp.append("，住址：").append(redisUtil.hget(reportSummaryx,"residence"));
			temp.append("，工作单位：").append(redisUtil.hget(reportSummaryx,"workUnit"));
			temp.append("，联系电话：").append(redisUtil.hget(reportSummaryx,"contact"));


			redisUtil.lSet(wholeQuestions,temp);
//			System.out.println("******\t审讯笔录:\t"+redisUtil.lGet(wholeQuestions,0,limitRecord));


		} else if(StringUtils.contains(question,question17)){
			String graduation = humanResponse;
			redisUtil.hset(reportSummaryx,"graduation",graduation);

			nextQuestion=question18;

		}else if(StringUtils.contains(question,question18)){
			String workExperience = humanResponse;
			redisUtil.hset(reportSummaryx,"workExperience",workExperience);

			// 更新 审讯笔录
			StringBuilder  temp=new StringBuilder("\n问：你的个人简历？\n答：");
			temp.append(redisUtil.hget(reportSummaryx,"graduation")).append("，");
			temp.append(redisUtil.hget(reportSummaryx,"workExperience"));

			redisUtil.lSet(wholeQuestions,temp);
//			System.out.println("******\t审讯笔录:\t"+redisUtil.lGet(wholeQuestions,0,limitRecord));



			nextQuestion=question19;

		}else if(StringUtils.contains(question,question19)){
//			String father = humanResponse;
			Map personInfo1 = new HashMap();
			personInfo1=(Map)this.algorithmPersonInfo(question);

			List family = (List)personInfo1.get("家庭");

			Map father = (Map)family.get(0);
			int fatherSize = father.size();
			if(fatherSize>1){

				redisUtil.hset(reportSummaryx,"fatherRelation",father.get("relation"));
				redisUtil.hset(reportSummaryx,"fatherName",father.get("name"));
				redisUtil.hset(reportSummaryx,"fatherAge",father.get("age"));
				redisUtil.hset(reportSummaryx,"fatherAddress",father.get("address"));
				redisUtil.hset(reportSummaryx,"fatherJob",father.get("other"));

			}



			nextQuestion=question20;

		}else if(StringUtils.contains(question,question20)){
//			String mother = humanResponse;

			Map personInfo1 = new HashMap();
			personInfo1 =	(Map)this.algorithmPersonInfo(question);

			List family = (List)personInfo1.get("家庭");
			Map mother = (Map)family.get(0);

			if(mother.size()>1){

				redisUtil.hset(reportSummaryx,"motherRelation",mother.get("relation"));
				redisUtil.hset(reportSummaryx,"motherName",mother.get("name"));
				redisUtil.hset(reportSummaryx,"motherAge",mother.get("age"));
				redisUtil.hset(reportSummaryx,"motherAddress",mother.get("address"));
				redisUtil.hset(reportSummaryx,"motherJob",mother.get("other"));

			}

			nextQuestion=question21;

		}else if(StringUtils.contains(question,question21)){
			String brothers  = humanResponse;
//			redisUtil.hset(reportSummaryx,"brothers",brothers);

			Map personInfo1 = (Map)this.algorithmPersonInfo(question);
			List family = (List)personInfo1.get("家庭");

			// 兄弟姐妹 数目
			int brotherNumber = family.size();

			Map map2 = (Map)family.get(0);
			String message = (String)map2.get("message");
			if(brotherNumber==1 && StringUtils.contains(message,"无")){
				brotherNumber=0;
			}


			redisUtil.hset(reportSummaryx,"brotherNumber",brotherNumber);

		    for(int i=0; i<brotherNumber; i++){

				Map brother = (Map)family.get(i);
				if(brother.size()>1){

					redisUtil.hset(reportSummaryx,"brotherRelation"+i,brother.get("relation"));
					redisUtil.hset(reportSummaryx,"brotherName"+i,brother.get("name"));
					redisUtil.hset(reportSummaryx,"brotherAge"+i,brother.get("age"));
					redisUtil.hset(reportSummaryx,"brotherAddress"+i,brother.get("address"));
					redisUtil.hset(reportSummaryx,"brotherJob"+i,brother.get("other"));
				}
			}



			nextQuestion=question22;

		}else if(StringUtils.contains(question,question22)){

			Map personInfo1 = (Map)this.algorithmPersonInfo(question);
			List family = (List)personInfo1.get("家庭");
			Map spouse = (Map)family.get(0);

			int spouseNumber = family.size();

			Map map2 = (Map)family.get(0);
			String message = (String)map2.get("message");
			if(spouseNumber==1 && StringUtils.contains(message,"无")){
				spouseNumber=0;
			}


			if(spouseNumber>0){

				redisUtil.hset(reportSummaryx,"spouseRelation",spouse.get("relation"));
				redisUtil.hset(reportSummaryx,"spouseName",spouse.get("name"));
				redisUtil.hset(reportSummaryx,"spouseAge",spouse.get("age"));
				redisUtil.hset(reportSummaryx,"spouseAddress",spouse.get("address"));
				redisUtil.hset(reportSummaryx,"spouseJob",spouse.get("other"));

			}



			// 更新 审讯笔录
			StringBuilder  temp=new StringBuilder("\n问：你的家庭情况？\n答：");



			// 父亲情况
//			temp.append(redisUtil.hget(reportSummaryx,"fatherRelation")).append("：");
			temp.append("父亲").append("：");

			if(StringUtils.isNotBlank((String)redisUtil.hget(reportSummaryx,"fatherName"))){
				temp.append(redisUtil.hget(reportSummaryx,"fatherName")).append("，");
			}

			if(StringUtils.isNotBlank((String)redisUtil.hget(reportSummaryx,"fatherAge"))){
				temp.append(redisUtil.hget(reportSummaryx,"fatherAge")).append("岁").append("，");

			}

			if(StringUtils.isNotBlank((String)redisUtil.hget(reportSummaryx,"fatherJob"))){
				temp.append(redisUtil.hget(reportSummaryx,"fatherJob")).append("；");

			}

			// 母亲情况
//			temp.append(redisUtil.hget(reportSummaryx,"motherRelation")).append("：");
			temp.append("母亲").append("：");
			if(StringUtils.isNotBlank((String)redisUtil.hget(reportSummaryx,"motherName"))){
				temp.append(redisUtil.hget(reportSummaryx,"motherName")).append("，");

			}

			if(StringUtils.isNotBlank((String)redisUtil.hget(reportSummaryx,"motherAge"))){
				temp.append(redisUtil.hget(reportSummaryx,"motherAge")).append("岁").append("，");

			}
			if(StringUtils.isNotBlank((String)redisUtil.hget(reportSummaryx,"motherJob"))){
				temp.append(redisUtil.hget(reportSummaryx,"motherJob")).append("；");

			}

			// 兄弟姐妹情况
			int brotherNumber = (int)redisUtil.hget(reportSummaryx,"brotherNumber");
			for(int i=0; i<brotherNumber; i++){
				temp.append(redisUtil.hget(reportSummaryx,"brotherRelation"+i)).append("：");

				if(StringUtils.isNotBlank((String)redisUtil.hget(reportSummaryx,"brotherName"+i))){
					temp.append(redisUtil.hget(reportSummaryx,"brotherName"+i)).append("，");

				}

				if(StringUtils.isNotBlank((String)redisUtil.hget(reportSummaryx,"brotherAge"+i))){
					temp.append(redisUtil.hget(reportSummaryx,"brotherAge"+i)).append("岁").append("，");

				}
				if(StringUtils.isNotBlank((String)redisUtil.hget(reportSummaryx,"brotherJob"+i))){
					temp.append(redisUtil.hget(reportSummaryx,"brotherJob"+i)).append("；");

				}

			}

			// 配偶情况

			if(StringUtils.isNotBlank((String)redisUtil.hget(reportSummaryx,"spouseRelation"))){
				temp.append(redisUtil.hget(reportSummaryx,"spouseRelation")).append("：");

			}

			if(StringUtils.isNotBlank((String)redisUtil.hget(reportSummaryx,"spouseName"))){
				temp.append(redisUtil.hget(reportSummaryx,"spouseName")).append("，");

			}

			if(StringUtils.isNotBlank((String)redisUtil.hget(reportSummaryx,"spouseAge"))){
				temp.append(redisUtil.hget(reportSummaryx,"spouseAge")).append("岁").append("，");

			}
			if(StringUtils.isNotBlank((String)redisUtil.hget(reportSummaryx,"spouseJob"))){
				temp.append(redisUtil.hget(reportSummaryx,"spouseJob"));

			}

			redisUtil.lSet(wholeQuestions,temp);
//			System.out.println("******\t审讯笔录:\t"+redisUtil.lGet(wholeQuestions,0,limitRecord));


			nextQuestion=question23;


		}else if(StringUtils.contains(question, question23)) {
        	Map map =this.algorithmElementsOfCase(question);

			// 案件类别
			String basecasereason = (String) map.get("basecasereason");
			// 细分案件类别
			String casereason = (String) map.get("casereason");

			nextQuestion=question24; // 暂时不管什么类别，直接往下走

			/*if(StringUtils.contains(basecasereason,"盗窃") || StringUtils.contains(casereason,"盗窃")){

				nextQuestion=question24;

			}else if(StringUtils.contains(basecasereason,"诈骗") || StringUtils.contains(casereason,"诈骗")){

				nextQuestion=StringUtils.replace(question24,"盗窃","诈骗");

			}else if(StringUtils.contains(basecasereason,"抢") || StringUtils.contains(casereason,"抢")){

				nextQuestion=StringUtils.replace(question24,"盗窃","抢劫");

			}else{
				nextQuestion=question23;

			}*/


			redisUtil.hset(reportSummaryx,"basecasereason",basecasereason);
			redisUtil.hset(reportSummaryx,"casereason",casereason);

			redisUtil.lSet(wholeQuestions,question);
//			System.out.println("******\t审讯笔录:\t"+redisUtil.lGet(wholeQuestions,0,limitRecord));



		}else if(StringUtils.contains(question, StringUtils.substring(question24,0,5) )) {

			redisUtil.lSet(wholeQuestions,question);
//			System.out.println("******\t审讯笔录:\t"+redisUtil.lGet(wholeQuestions,0,limitRecord));

			nextQuestion=question25;


		}else if(StringUtils.contains(question, StringUtils.substring(question25,0,5) )) {

			Map map1 = this.algorithmElementsOfCase(question);
			// 案发开始 案发结束
			Map<String, Object> casetime =  (Map<String, Object>) map1.get("casetime");
			// 案发地点
			List address =  (List) map1.get("address");
			// 作案区间
			String periodtime = (String) map1.get("periodtime");
			redisUtil.hset(reportSummaryx,"periodtime",periodtime);

			// 被盗物品信息
			List goods =  (List) map1.get("goods");
			String selfPrice = "";
			String itemType = "";
			String nameOfGoods = "";
			String brand = "";
			String quantity  = "";
			String color = "";
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
			log.info("报案摘要：\t"+redisUtil.hmget(reportSummaryx));

			// 案件要素 **************************************************
			List caseElements = new ArrayList();

			if(address== null || address.isEmpty()) {
				caseElements.add("question26");
			}else {
				redisUtil.hset(reportSummaryx,"address",(String) address.get(0));

			}


			if(casetime == null || casetime.isEmpty()) {
				caseElements.add("question27");
			}else {
				redisUtil.hset(reportSummaryx,"caseStart",(String) casetime.get("案发开始"));
				redisUtil.hset(reportSummaryx,"caseEnd",(String) casetime.get("案发结束"));

			}

			if(StringUtils.isNotBlank(nameOfGoods)
					&&StringUtils.isBlank(brand) && StringUtils.isBlank(color)) {
				caseElements.add("question28");

			}

			if(StringUtils.isBlank(nameOfGoods)){
				caseElements.add("question25_1");

			}


			if(StringUtils.isBlank(brand)) {
				caseElements.add("question29");

			}

			if(StringUtils.isBlank(color)) {
				caseElements.add("question30");

			}



			log.info("未抽取到\t"+caseElements.size()+"\t个案件要素");
			log.info("未抽取到的要素:\t"+caseElements);


			if(caseElements.isEmpty()) {
				nextQuestion=question31;

			}else {
				Collections.sort(caseElements);// 排序
				nextQuestion= questionMap.get(caseElements.get(0));
				if(StringUtils.contains(nextQuestion,question28)){
					// 物品名称 替换
					nextQuestion = StringUtils.replace(nextQuestion,"物品名称",nameOfGoods);
				}


				caseElements.remove(0);

				redisUtil.del(questionQueueRedis);
				redisUtil.lSet(questionQueueRedis,caseElements);

			}


			// ****************************************************
			redisUtil.lSet(wholeQuestions,question);
//			System.out.println("******\t审讯笔录:\t"+redisUtil.lGet(wholeQuestions,0,limitRecord));

		} else if(  StringUtils.contains(question, question26)
				|| StringUtils.contains(question, question25_1)
				|| StringUtils.contains(question, question27)
				|| StringUtils.contains(question,StringUtils.substring(question28,question28.length()-7,question28.length()))
				|| StringUtils.contains(question, question29)
				|| StringUtils.contains(question, question30)
				) {

            List list = questionQueuem;

            String format = "";
			if(StringUtils.contains(question,question27)) {

                Map elements = this.algorithmElementsOfCase(question);

                String periodtime = (String)elements.get("periodtime");

                Map timeArea = (Map)elements.get("casetime");

                if(timeArea!=null && !timeArea.isEmpty()){

                    String start = (String)timeArea.get("案发开始");
                    String end = (String)timeArea.get("案发结束");

                    redisUtil.hset(reportSummaryx,"caseStart",start);
                    redisUtil.hset(reportSummaryx,"caseEnd",end);
                }

                redisUtil.hset(reportSummaryx,"periodtime",periodtime);
            }else if(StringUtils.contains(question,question26)) {
                // 案发地点
                Map map =this.algorithmElementsOfCase(question);
                List lista = (List)map.get("address");
                String address="";
                if(lista!=null && !lista.isEmpty()){
                    address = (String)lista.get(0);

                }

                redisUtil.hset(reportSummaryx,"address",addressCompletion+""+address);

            }else if(StringUtils.contains(question,question30)) {
                // 颜色 调算法接口

                Map good1 = this.algorithmGoodsOfCase(question);
				String color = (String) good1.get("颜色");
				redisUtil.hset(reportSummaryx,"color",color);

            }else if(StringUtils.contains(question,question29)) {
                // 品牌  调算法接口

				Map good1 = this.algorithmGoodsOfCase(question);
				String brand = (String) good1.get("品牌");
//				String itemType = (String) good1.get("物品类型"); // 家电
//				String nameOfGoods = (String) good1.get("物品名称");

				redisUtil.hset(reportSummaryx,"brand",brand);


            }else if(StringUtils.contains(question,question25_1)) {
				// 物品名称  调算法接口

				Map good1 = this.algorithmGoodsOfCase(question);
				String nameOfGoods = (String) good1.get("物品名称");

				String selfPrice = (String) good1.get("自报价格");
				String itemType = (String) good1.get("物品类型");
				String brand = (String) good1.get("品牌");
				String quantity  = (String) good1.get("物品数量");
				String color = (String) good1.get("颜色");;


				redisUtil.hset(reportSummaryx,"selfPrice",selfPrice);
				redisUtil.hset(reportSummaryx,"itemType",itemType);
				redisUtil.hset(reportSummaryx,"brand",brand);
				redisUtil.hset(reportSummaryx,"quantity",quantity);
				redisUtil.hset(reportSummaryx,"color",color);
				redisUtil.hset(reportSummaryx,"nameOfGoods",nameOfGoods);

				if(StringUtils.isBlank(nameOfGoods)){

					list.add("question25_1");
				}else{
					list.remove("question25_1");

				}

				if(StringUtils.isNotBlank(brand)){
					list.remove("question29");
				}

				if(StringUtils.isNotBlank(color)){
					list.remove("question30");
				}



			}else if(StringUtils.contains(question,StringUtils.substring(question28,question28.length()-7,question28.length()))) {

                Map good1 = this.algorithmGoodsOfCase(question);

//				String nameOfGoods = (String) good1.get("物品名称");
//				String selfPrice = (String) good1.get("自报价格");
//				String itemType = (String) good1.get("物品类型"); // 家电
//                String quantity  = (String) good1.get("物品数量");

				String brand = (String) good1.get("品牌");
				String color = (String) good1.get("颜色");

//				redisUtil.hset(reportSummaryx,"nameOfGoods",nameOfGoods);
//				redisUtil.hset(reportSummaryx,"selfPrice",selfPrice);
//				redisUtil.hset(reportSummaryx,"itemType",itemType);
//                redisUtil.hset(reportSummaryx,"quantity",quantity);

				redisUtil.hset(reportSummaryx,"brand",brand);
				redisUtil.hset(reportSummaryx,"color",color);

                if(StringUtils.isNotBlank(brand)){
					list.remove("question29");
				}

				if(StringUtils.isNotBlank(color)){
					list.remove("question30");
				}


			}



            if(list == null || list.isEmpty()) {

                nextQuestion=question31 ;
            }else {


				Collections.sort(list);
				nextQuestion = questionMap.get((String) list.get(0));

				if(StringUtils.contains(nextQuestion,StringUtils.substring(question28,question28.length()-7,question28.length()))){
					String nameOfGoods=(String)redisUtil.hget(reportSummaryx,"nameOfGoods");
					if(StringUtils.isNotBlank(nameOfGoods)){
						nextQuestion = StringUtils.replace(nextQuestion,"物品名称",nameOfGoods);

					}

				}


				list.remove(0);
				redisUtil.del(questionQueueRedis);

                if(!list.isEmpty()){
                    redisUtil.lSet(questionQueueRedis,list);
                }


			}

			log.info("审讯摘要：\t"+redisUtil.hmget(reportSummaryx));

			// ****************************************************


			boolean flag = true;
			// 如果 你盗窃的什么物品？--> 没有抽取出赃物，则不保存入笔录 TODO
			String nameOfGoods = (String)redisUtil.hget(reportSummaryx,"nameOfGoods");
			if(StringUtils.contains(question,question25_1)
					&& StringUtils.isBlank(nameOfGoods)){

					flag=false;
			}

			if(flag){

				redisUtil.lSet(wholeQuestions,question);
			}

//			System.out.println("******\t审讯笔录:\t"+redisUtil.lGet(wholeQuestions,0,limitRecord));

		}else if(StringUtils.contains(question, question31)) {

			// 添加判断
        	if(StringUtils.contains(humanResponse,"没")
				||StringUtils.contains(humanResponse,"无")
				||StringUtils.contains(humanResponse,"不")		){

				nextQuestion =question32;

			}else{
				nextQuestion =question31_1;

			}


			// ****************************************************
			redisUtil.lSet(wholeQuestions,question);
//			System.out.println("******\t审讯笔录:\t"+redisUtil.lGet(wholeQuestions,0,limitRecord));

		}else if(StringUtils.contains(question, question31_1)) {

			nextQuestion =question32;

			// ****************************************************
			redisUtil.lSet(wholeQuestions,question);
//			System.out.println("******\t审讯笔录:\t"+redisUtil.lGet(wholeQuestions,0,limitRecord));

		}else if(StringUtils.contains(question, question32)) {

			// 添加判断
			if(StringUtils.contains(humanResponse,"没")
					||StringUtils.contains(humanResponse,"无")
					||StringUtils.contains(humanResponse,"不") 	){

				nextQuestion =question33;
				String nameOfGoods=(String)redisUtil.hget(reportSummaryx,"nameOfGoods");
				if(StringUtils.isNotBlank(nameOfGoods)){
					nextQuestion = StringUtils.replace(nextQuestion,"物品名称",nameOfGoods);

				}

			}else{
				nextQuestion =question32_1;

			}

			// ****************************************************
			redisUtil.lSet(wholeQuestions,question);
//			System.out.println("******\t审讯笔录:\t"+redisUtil.lGet(wholeQuestions,0,limitRecord));

		}else if(StringUtils.contains(question, question32_1)) {

			nextQuestion =question33;

			String nameOfGoods=(String)redisUtil.hget(reportSummaryx,"nameOfGoods");
			if(StringUtils.isNotBlank(nameOfGoods)){
				nextQuestion = StringUtils.replace(nextQuestion,"物品名称",nameOfGoods);

			}

			// ****************************************************
			redisUtil.lSet(wholeQuestions,question);
//			System.out.println("******\t审讯笔录:\t"+redisUtil.lGet(wholeQuestions,0,limitRecord));

		}else if(StringUtils.contains(question, StringUtils.substring(question33,question33.length()-6,question33.length()))) {

			String nameOfGoods = (String)redisUtil.hget(reportSummaryx,"nameOfGoods");
			nextQuestion=StringUtils.replace(question34,"物品名称",nameOfGoods);

			// ****************************************************
			redisUtil.lSet(wholeQuestions,question);
//			System.out.println("******\t审讯笔录:\t"+redisUtil.lGet(wholeQuestions,0,limitRecord));

		}else if(StringUtils.contains(question, StringUtils.substring(question34,0,4))) {


			String nameOfGoods = (String)redisUtil.hget(reportSummaryx,"nameOfGoods");
			nextQuestion=StringUtils.replace(question35,"物品名称",nameOfGoods);

			// ****************************************************
			redisUtil.lSet(wholeQuestions,question);
//			System.out.println("******\t审讯笔录:\t"+redisUtil.lGet(wholeQuestions,0,limitRecord));


		}else if(StringUtils.contains(question, StringUtils.substring(question35,question35.length()-6,question35.length()))) {

			nextQuestion=question36;

			// ****************************************************
			redisUtil.lSet(wholeQuestions,question);
//			System.out.println("******\t审讯笔录:\t"+redisUtil.lGet(wholeQuestions,0,limitRecord));


		}else if(StringUtils.contains(question,question36)) {

			nextQuestion=question37;


			// ****************************************************
			redisUtil.lSet(wholeQuestions,question);
//			System.out.println("******\t审讯笔录:\t"+redisUtil.lGet(wholeQuestions,0,limitRecord));

		}else if(StringUtils.contains(question,question37)) {

			nextQuestion=question38;


			// ****************************************************
			redisUtil.lSet(wholeQuestions,question);
//			System.out.println("******\t审讯笔录:\t"+redisUtil.lGet(wholeQuestions,0,limitRecord));

		}else if(StringUtils.contains(question,question38)) {

			nextQuestion=question39;


			// ****************************************************
			redisUtil.lSet(wholeQuestions,question);
//			System.out.println("******\t审讯笔录:\t"+redisUtil.lGet(wholeQuestions,0,limitRecord));

		}else if(StringUtils.contains(question,question39)) {

			nextQuestion=question40;


			// ****************************************************
			redisUtil.lSet(wholeQuestions,question);
//			System.out.println("******\t审讯笔录:\t"+redisUtil.lGet(wholeQuestions,0,limitRecord));

		}else if(StringUtils.contains(question,question40)) {
            // 最后一个问题


            Map reportSummary =redisUtil.hmget(reportSummaryx);
            log.info("报案摘要：\t"+reportSummary);
            ret.put("reportSummary",reportSummary);


            // ****************************************************
            redisUtil.lSet(wholeQuestions,question); // 记录笔录
            System.out.println("******\t审讯笔录:\t"+redisUtil.lGet(wholeQuestions,0,limitRecord));
            System.out.println("******************\t审讯完毕 \t");


			nextQuestion="审讯完毕";

//			redisUtil.del(asked); // 清空已问问题列表


			redisUtil.expire(questionQueueRedis,expireTime1);
			redisUtil.expire(asked,expireTime1);
			redisUtil.expire(wholeQuestions,expireTime1);
			redisUtil.expire(nextQuestionRedis,expireTime1);
			redisUtil.expire(reportSummaryx,expireTime1);
			redisUtil.expire(countRedis,expireTime1);



        }else if(StringUtils.contains(question,question54)) {
			// 返回上一个问题
            nextQuestion = (String)redisUtil.rpop(asked);

            System.out.println("已问问题数目:\t"+redisUtil.lGetListSize(asked));

		}else if(StringUtils.contains(question,manualntervention)){
        	// 结束人工干预,回到问题队列

			nextQuestion = (String)redisUtil.get(nextQuestionRedis);


		}else{

			// 人工干预所提的问题，不在问题列表，则下一个问题返回空值
			redisUtil.lSet(wholeQuestions,question);
			System.out.println("******\t审讯笔录:\t"+redisUtil.lGet(wholeQuestions,0,limitRecord));

			ret.put("nextQuestion", "");
			return ret;
		}



		ret.put("nextQuestion", nextQuestion);
//		log.info("下一个问题是：\t"+nextQuestion);

		// 保存下一个问题
		redisUtil.set(nextQuestionRedis,nextQuestion);
		redisUtil.expire(nextQuestionRedis,expireTime1);

		System.out.println("\t下一个问题是：\t"+(String)redisUtil.get(nextQuestionRedis));

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
	 *  算法接口抽取案件要素信息
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



	public static void main(String[] args) {

		String str="没有同伙";
		System.out.println(StringUtils.contains(str,"有"));

		/*System.out.println(StringUtils.replace(str,"，",""));
		str=StringUtils.replace(str,"，","");

		System.out.println(StringUtils.replace(str,"。",""));*/




//		String str="问：结束人工干预？答：aaaaaa";
		/*String str="问：结束人工干预？";

		String[] ques = str.split("答：");

		System.out.println("\t length:\t"+ques.length);
		if(ques.length>1){
			System.out.println("\t length:\t"+ques.length+"\n"+ques[0]+"\n"+ques[1]);

		}*/



		/*String str="问：人工干预aaaaaaaaaaaaaaaaaa不得隐瞒事实或者作假口供，否则要依法追究你相应的法律责任，对与本案无关的问题，你有权拒绝回答，你是否清楚？答：清楚。";

		StringUtils.replace(str,"问：","/r/n 问：");
		StringUtils.replace(str,"答：","/r/n问：");

		System.out.println(str);*/


//		String str ="abcdefghijklmn？";
//		System.out.println(StringUtils.substring(str,str.length()-5,str.length()));

	/*	// 根据年份计算 人的年龄
		// 只有年份
		SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//必须捕获异常
		try {
			Date date=simpleDateFormat.parse("1988-06-06 00:00:00");

			Calendar c1 = Calendar.getInstance();   //当前日期

			Calendar c2 = Calendar.getInstance();
			c2.setTime(date);   //设置为另一个时间

			int year = c1.get(Calendar.YEAR);
			int oldYear = c2.get(Calendar.YEAR);

			System.out.println(date.toLocaleString()+"\t age:\t"+(year-oldYear) );

		} catch(ParseException px) {
			px.printStackTrace();
		}*/


	}





}
