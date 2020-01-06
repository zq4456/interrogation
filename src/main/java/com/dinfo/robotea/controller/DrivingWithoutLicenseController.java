package com.dinfo.robotea.controller;

import com.dinfo.robotea.properties.QuestionProperties;
import com.dinfo.robotea.properties.RedisUtil;
import com.dinfo.robotea.service.AlgorithmService;
import com.dinfo.robotea.service.CommonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

/**
 *	顺德公安 <font> 无证驾驶 </font> 审讯
 */
@Controller
@RequestMapping("/drivingWithoutLicense")
public class DrivingWithoutLicenseController {

	private static final Logger log = LoggerFactory.getLogger(DrivingWithoutLicenseController.class);



	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private QuestionProperties questionProperties;

	@Autowired
	private AlgorithmService algorithmService;




	// 案发地址补全前缀
    private final static  String addressCompletion = "广东省佛山市顺德区";

	private static  String question1="我们是佛山市顺德区公安局登录民警账号所属单位的民警，现就有关案情依法对你进行询问，你应当如实回答，故意作伪证或者隐匿证据会负相应的法律责任，对案件无关问题，你有拒绝回答的权利，你有要求办案人员或者公安机关负责人回避的权利，有陈述和申辩的权利，以上权利义务告知，你听清楚了吗？";
	private final static  String question2="你今天因何事主动前来佛山市顺德区公安局";
	private final static  String question3="办案民警有否将《行政案件权利义务告知书》送达给你？你是否阅读过《行政案件权利义务告知书》上的内容？";

	private final static  String question4="是否申请有关人员回避？";
	private final static  String question4_1="就是申请其他警员来处理你这案子，请问是否确定需要申请有关人员回避？";

	private final static  String question5="请确认这些个人基本信息是否正确？";
	private final static  String question5_01="哪项个人基本信息不正确？正确信息是什么？";

	private final static  String question5_02="你的名字是什么？";
	private final static  String question5_03="有没有曾用名，有的话是什么？";
	private final static  String question5_04="什么性别？";
	private final static  String question5_05="出生日期是什么？";
	private final static  String question5_06="身份证号码是什么？";
	private final static  String question5_06_1="身份证号码格式有误，请重新回复";

	private final static  String question5_07="什么民族？";
	private final static  String question5_08="什么文化程度？";
	private final static  String question5_09="什么婚姻状况？";
	private final static  String question5_10="户籍地址是哪里？";
	private final static  String question5_11="现住址是哪里？";
	private final static  String question5_12="工作单位是什么？";
	private final static  String question5_13="联系电话是什么？";
	private final static  String question5_13_1="电话号码格式有误，请重新回复";


	private final static  String question6="你是否是人大代表或政协委员？";
	private final static  String question6_1="是哪里的人大代表？";
	private final static  String question6_2="是哪里的政协委员？";
	private final static  String question6_3="是哪里的人大代表和政协委员？";

	private final static  String question7="你是否国家机关或事业单位工作人员？";
	private final static  String question7_1="单位名称是什么？";

	private final static  String question8="请确认这些家庭状况信息是否正确？";
    private final static  String question8_01="哪项家庭状况信息不正确？正确信息是什么？";
    private final static  String question8_02="你的父亲叫什么名字，多少岁，在哪里工作？";
    private final static  String question8_03="你的母亲叫什么名字，多少岁，在哪里工作？";
    private final static  String question8_04="有没有兄弟姐妹，叫什么名字，多少岁，在哪里工作？";
    private final static  String question8_05="有没有配偶，叫什么名字，多少岁，在哪里工作？";
    private final static  String question8_06="有没有子女，叫什么名字，多少岁？";

    private final static  String question09="请确认这些个人简历信息是否正确？";
    private final static  String question09_01="哪项个人简历信息不正确？正确信息是什么？";

	private final static  String question09_02="什么时候毕业？毕业学校是什么？";
	private final static  String question09_03="什么时候出来工作？当时在哪里工作？什么时候在现在这个单位工作？";

	private final static  String question10="你本人自动投案，我队需通知你的家属，你提供下联系方式？";
	private final static  String question11="你是否曾受过刑事处罚或者行政拘留、劳动教养、收容教育、社区戒毒、强制戒毒、收容教养？";
	private final static  String question11_1="有什么前科？";

	private final static  String question12="你目前身体有何不适？";
	private final static  String question13="你曾患过何种疾病？";
	private final static  String question14="你有没有外伤史？";

	private final static  String question15="类型的机动车因交通违法行为被公安交警部门抓获？";
	private final static  String question16="你当时是有什么交通违法行为？";

	private final static  String question20="你有没有考取过驾驶证？";
	private final static  String question20_1="你的驾驶证什么时候在哪里考取，准驾车型是什么？";

    private final static  String question25="当时所驾驶的机动车情况？什么颜色，什么品牌，车主是谁，和你什么关系？";
    private final static  String question25_1="车是怎么来？";
    private final static  String question25_1_1="向谁借的？他知不知道你未考取驾驶证？";

    private final static  String question25_2="当时所驾驶的机动车有没有悬挂号牌？";
    private final static  String question25_2_1="经检测，你所驾驶的车辆为普通二轮摩托车，你对鉴定结论是否有异议？";


	private final static  String question26="你将当时的违法经过讲一下？";
    private final static  String question27="你还有其他交通违法行为吗？";
    private final static  String question27_1="什么交通违法行为？";

    private final static  String question28="在公安机关调查期间，是否有保障你必要的饮食情况及作息时间？";
    private final static  String question29="在调查期间有没有对你使用不文明行为和言语？";

    private final static  String question30="你还有什么需要补充？";
    private final static  String question30_1="你要补充什么？";

    private final static  String question31="你以上所讲是否属实？";

	private final static  String question54 = "回到上一个问题";


	private static HashMap<String,String> questionMap= new HashMap<String,String>();
	static{
		questionMap.put("question5_01", question5_01);
		questionMap.put("question5_02", question5_02);
		questionMap.put("question5_03", question5_03);
		questionMap.put("question5_04", question5_04);
		questionMap.put("question5_05", question5_05);

		questionMap.put("question5_06", question5_06);
		questionMap.put("question5_06_1", question5_06_1);

		questionMap.put("question5_07", question5_07);
		questionMap.put("question5_08", question5_08);
		questionMap.put("question5_09", question5_09);
		questionMap.put("question5_10", question5_10);
		questionMap.put("question5_11", question5_11);
		questionMap.put("question5_12", question5_12);

		questionMap.put("question5_13", question5_13);
		questionMap.put("question5_13_1", question5_13_1);

        questionMap.put("question8_01", question8_01);
        questionMap.put("question8_02", question8_02);
        questionMap.put("question8_03", question8_03);
        questionMap.put("question8_04", question8_04);
        questionMap.put("question8_05", question8_05);
        questionMap.put("question8_06", question8_06);

		questionMap.put("question09_01", question09_01);
		questionMap.put("question09_02", question09_02);
		questionMap.put("question09_03", question09_03);

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
        return "drivingWithoutLicense";
    }

	/** 审讯要素 githubss
	 * @return
	 */
	@PostMapping(value="/getElement")
	@ResponseBody
	public  Object getElement(String cookieUuid){

        String reportSummaryx = "reportSummary_drive_"+cookieUuid;
        Map<Object, Object> map = redisUtil.hmget(reportSummaryx);
		return map;
	}

	/** 审讯笔录
	 * @return
	 */
	@PostMapping(value="/getRecord")
	@ResponseBody
	public  Object getRecord(String cookieUuid){

		String wholeQuestions = "wholeQuestions_drive_"+cookieUuid;
        List<String> list = (List)redisUtil.lGet(wholeQuestions,0,limitRecord);
		return list;


	}




	/** 问答接口
	 * @return
	 */
	@PostMapping(value="/getAnswer",consumes="application/json",produces="application/json")
	@ResponseBody
	@SuppressWarnings({ "unchecked", "unused", "rawtypes" })
	public Object getAnswer(@RequestBody String json) {

		ObjectMapper objectMapper = new ObjectMapper();
		Map<String,Object> paramMap = new HashMap();
		try {
			paramMap = objectMapper.readValue(json,Map.class);
			log.info("/t ****** 无证驾驶 问答接口参数：\t"+paramMap);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		String question = (String) paramMap.get("question");
		String cookieUuid = (String) paramMap.get("cookieUuid");

        String manualntervention = (String) paramMap.get("manualntervention");


		String policeDept = (String) paramMap.get("policeDept"); // 民警账号所属单位
		String policePhone = (String) paramMap.get("policePhone"); // 审讯民警单位联系电话

		// redis缓存失效时间  60l*60*24 24小时
		long expireTime1 = Long.valueOf(questionProperties.getExpire()) * 60;

		Map<String, Object> ret =new HashMap<String,Object>();


        String asked = "asked_drive_"+cookieUuid;
        log.info("cookieUuid：\t"+asked);

         // 笔录redis变量
		String wholeQuestions = "wholeQuestions_drive_"+cookieUuid;
		log.info("wholeQuestions：\t"+wholeQuestions);

		// 下一个问题redis变量，(人工干预返回自动问答，取值)
		String nextQuestionRedis = "nextQuestion_drive_"+cookieUuid;

		// 人员信息和案件要素 问题队列 start
        String questionQueueRedis = "questionQueue_drive_"+cookieUuid;

		// 记录器，身份证号码与 手机号连续 2次不通过，就不再问。
		String countRedis = "countRedis_drive_"+cookieUuid;
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
				&& !StringUtils.contains(manualntervention,"人工干预")
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
        String reportSummaryx = "reportSummary_drive_"+cookieUuid;
		redisUtil.expire(reportSummaryx,expireTime1);
		expireTime = redisUtil.getExpire(reportSummaryx);
		System.out.println(reportSummaryx+"\t expireTime for 案件要素 \t:"+expireTime);

		System.out.println("案件提取要素：\t"+redisUtil.hmget(reportSummaryx));


        // 下一个问题
        String nextQuestion="";

        // 是否人工干预, 0，不干预；1，干预 。
		String intervention ="0";

        // 开始提问 ******************************************************************************
        if(StringUtils.contains(manualntervention,"结束人工干预")){
            redisUtil.lSet(wholeQuestions,question); // 笔录

            System.out.println("******\t审讯笔录:\t"+redisUtil.lGet(wholeQuestions,0,limitRecord));

            // 结束人工干预,回到问题队列
            nextQuestion = (String)redisUtil.get(nextQuestionRedis);

        }else if(StringUtils.contains(manualntervention,"开始人工干预") ){

            redisUtil.lSet(wholeQuestions,question);

			System.out.println("******\t审讯笔录:\t"+redisUtil.lGet(wholeQuestions,0,limitRecord));

			// 人工干预所提的问题，不在问题列表，则下一个问题返回空值
			ret.put("nextQuestion", "");
            return ret;
        }else if(StringUtils.contains(question, StringUtils.substring(question1,question1.length()-25,question1.length()))) {

			// 单位名称“交通警察大队机动中队”取自审讯系统登录民警账号所属单位
			String question2Tmmp = StringUtils.join(question2,policeDept,"？");

			// 在问第一个问题之前，初始化 redis 审讯要素变量 和  审讯笔录变量

			redisUtil.del(wholeQuestions); // 清空笔录
			redisUtil.del(reportSummaryx); // 清空要素
			redisUtil.del(questionQueueRedis);// 清空 个人信息 和 案件 细节待问问题列表
			redisUtil.del(nextQuestionRedis); // 清空 下一个问题


			redisUtil.hset(reportSummaryx,"policeDept",StringUtils.isBlank(policeDept)?"":policeDept);
  			// 计数器初始化
			redisUtil.hset(countRedis,"phone",0);
			redisUtil.hset(countRedis,"idcard",0);

			redisUtil.lSet(wholeQuestions,question); // 开始记录笔录

			if(  StringUtils.contains(humanResponse, "不") || StringUtils.isBlank(humanResponse)) {

				int countquestion1 = 0;
				boolean bool =  redisUtil.hHasKey(countRedis,"question1");
				if(!bool){
					redisUtil.hset(countRedis,"question1",1);
				}else{
					countquestion1 = (int) redisUtil.hget(countRedis,"question1");
					redisUtil.hset(countRedis,"question1",++countquestion1);

				}

				System.out.println("不清楚/不回复次数：countquestion1\t"+redisUtil.hget(countRedis,"question1"));

				if((int)redisUtil.hget(countRedis,"question1") > 1){

					// 重复两遍还是不清楚/不回复，则提示人工干预。 人工干预以后，回到下一个问题
					nextQuestion=question2Tmmp;
					intervention = "1";
					// 复位
					redisUtil.hset(countRedis,"question1",0);

				}else{
					// 替换 "登录民警账号所属单位"
					question1 = StringUtils.replace(question1,"登录民警账号所属单位",policeDept);
					nextQuestion=question1;

				}


			}else {
				nextQuestion=question2Tmmp;

			}

		}else if(StringUtils.contains(question, StringUtils.substring(question2,0,19))) {

			if(  StringUtils.contains(humanResponse, "不") || StringUtils.isBlank(humanResponse)) {

				int countquestion2 = 0;
				boolean bool =  redisUtil.hHasKey(countRedis,"question2");
				if(!bool){
					redisUtil.hset(countRedis,"question2",1);
				}else{
					countquestion2 = (int) redisUtil.hget(countRedis,"question2");
					redisUtil.hset(countRedis,"question2",++countquestion2);

				}

				System.out.println("countquestion2 \t 不清楚/不回复次数：\t"+redisUtil.hget(countRedis,"question2"));

				if((int)redisUtil.hget(countRedis,"question2") > 1){

					// 重复两遍还是不清楚/不回复，则提示人工干预。 人工干预以后，回到下一个问题
					nextQuestion=question3;
					intervention = "1";
					// 复位
					redisUtil.hset(countRedis,"question2",0);

				}else{
					// 替换 "登录民警账号所属单位"
					String question2Tmmp = StringUtils.join(question2,policeDept,"？");
					nextQuestion=question2Tmmp;

				}


			}else {
				nextQuestion=question3;

			}

			redisUtil.lSet(wholeQuestions,question); // 笔录

		}else if(StringUtils.contains(question, question3) ){

			if(  StringUtils.containsAny(humanResponse, "不","没") || StringUtils.isBlank(humanResponse)) {

				int countquestion3 = 0;
				boolean bool =  redisUtil.hHasKey(countRedis,"question3");
				if(!bool){
					redisUtil.hset(countRedis,"question3",1);
				}else{
					countquestion3 = (int) redisUtil.hget(countRedis,"question3");
					redisUtil.hset(countRedis,"question3",++countquestion3);

				}

				System.out.println("countquestion3 \t 不清楚/不回复次数：\t"+redisUtil.hget(countRedis,"question3"));

				if((int)redisUtil.hget(countRedis,"question3") > 1){

					// 重复两遍还是不清楚/不回复，则提示人工干预。 人工干预以后，回到下一个问题
					nextQuestion=question4;
					intervention = "1";
					// 复位
					redisUtil.hset(countRedis,"question3",0);

				}else{

					nextQuestion=question3;

				}


			}else {
				nextQuestion=question4;

			}

			redisUtil.lSet(wholeQuestions,question); // 笔录


		}else if(StringUtils.contains(question, question4)) {

			String question5Temp = this.generateQuestion5(paramMap,reportSummaryx);

			System.out.println(question5Temp);

			if(  StringUtils.contains(humanResponse, "不") || StringUtils.isBlank(humanResponse)) {
				// 如回答“不需要”等，跳转到问题5
				nextQuestion=question5Temp;

			}else if(StringUtils.contains(humanResponse, "什么")){
				//	如回答“什么是回避？/什么意思”等，则回复及提问题4.1
				nextQuestion=question4_1;

			}else{
				// 如回复“需要”等，则提示人工干预
				intervention = "1";
				nextQuestion=question5Temp;

			}

			redisUtil.lSet(wholeQuestions,question); // 笔录


		}else if(StringUtils.contains(question,question4_1 )) {

			String question5Temp = this.generateQuestion5(paramMap,reportSummaryx);

			System.out.println(question5Temp);

			if(  StringUtils.contains(humanResponse, "不") || StringUtils.isBlank(humanResponse)) {
				// 问题4.1如回复“不需要”等，跳转到问题5
				nextQuestion=question5Temp;
			}else{
				// 问题4.1如回复“需要”等，则提示人工干预
				intervention = "1";
				nextQuestion=question5Temp;

			}

			redisUtil.lSet(wholeQuestions,question); // 笔录

		}else if(StringUtils.contains(question, question5)) {
			// xxxxx, 请确认这些信息是否正确？

			// List<String>  <---  不要加上泛型，绝对不能这么写
			List push = new ArrayList();

			if(  StringUtils.contains(humanResponse, "不") ) {
				// 回答“不正确“等，则提问问题5.1 “哪项信息不正确？
//				nextQuestion=question5_01;
				push.add("question5_01");

			}



			//	如回答“正确”等，如果要素完备，则 跳转到问题6
			// 	不完备，根据所缺要素分别提问以下问题

			for (Map.Entry<String,Object> entry: paramMap.entrySet()){

				String key = entry.getKey();
				Object value = (Object) entry.getValue();

				// 排除掉子女和兄弟姐妹的信息
				if(!StringUtils.containsAny(key,"childrenInfo","brotherInfo") ){

					if(StringUtils.isBlank((String) value)){
						if(StringUtils.equals(key,"fatherName")){
							push.add("question5_02");
						}else if(StringUtils.equals(key,"suspectUsedName")){
							push.add("question5_03");
						}else if(StringUtils.equals(key,"suspectGender")){
							push.add("question5_04");
						}else if(StringUtils.equals(key,"suspectBirthday")){
							push.add("question5_05");
						}else if(StringUtils.equals(key,"suspectIdCard")){
							push.add("question5_06");
						}else if(StringUtils.equals(key,"suspectNation")){
							push.add("question5_07");
						}else if(StringUtils.equals(key,"suspectEducation")){
							push.add("question5_08");
						}else if(StringUtils.equals(key,"suspectMarriage")){
							push.add("question5_09");
						}else if(StringUtils.equals(key,"suspectHouseholRegister")){
							push.add("question5_10");
						}else if(StringUtils.equals(key,"suspectResidence")){
							push.add("question5_11");
						}else if(StringUtils.equals(key,"suspectWorkunit")){
							push.add("question5_12");
						}else if(StringUtils.equals(key,"suspectContact")){
							push.add("question5_13");
						}



					}


				}





			}


			if(push.isEmpty()){
				nextQuestion=question6;

			}else{
				Collections.sort(push);// 排序
				nextQuestion = questionMap.get(push.get(0));
				push.remove(0);

				redisUtil.del(questionQueueRedis);
				redisUtil.lSet(questionQueueRedis,push);

				log.info("审讯摘要：\t"+redisUtil.hmget(reportSummaryx));

				redisUtil.set(nextQuestionRedis,nextQuestion);
				log.info("\t下一个问题是：\t"+(String)redisUtil.get(nextQuestionRedis));

				// 嫌疑人联系方式 初始化
				redisUtil.hset(countRedis,"suspectContact",0);

			}





			redisUtil.lSet(wholeQuestions,question); // 笔录


		}else if(StringUtils.contains(question,question5_01 )){
        	// "question":"问：哪项信息不正确？答：手机号和户籍地址",
			// 	"text":["\n问：51_哪项信息不正确？\n 答：手机号和户籍地址"]
			question = StringUtils.replace(question,question5_01,"51_哪项信息不正确？");

			Map map = algorithmService.algorithmElementsOfCase(question);
			Map<String,String> mapError = (Map<String,String>) map.get("wahsterror");
			System.out.println(mapError);

			List push = questionQueuem;

			for(Map.Entry<String,String> entry: mapError.entrySet()){

				String key = entry.getKey();
				String value = entry.getValue();

				if(StringUtils.equals(key,"姓名")){
					push.remove("question5_02");
					redisUtil.hset(reportSummaryx,"suspectName", StringUtils.isBlank(value)?"":value);

				}else if(StringUtils.equals(key,"曾用名")){
					push.remove("question5_03");
					redisUtil.hset(reportSummaryx,"suspectUsedName", StringUtils.isBlank(value)?"":value);

				}else if(StringUtils.equals(key,"性别")){
					push.remove("question5_04");
					redisUtil.hset(reportSummaryx,"suspectGender", StringUtils.isBlank(value)?"":value);

				}else if(StringUtils.equals(key,"出生日期")){
					push.remove("question5_05");
					redisUtil.hset(reportSummaryx,"suspectBirthday", StringUtils.isBlank(value)?"":value);

				}else if(StringUtils.equals(key,"身份证号码")){
					push.remove("question5_06");
					redisUtil.hset(reportSummaryx,"suspectIdCard", StringUtils.isBlank(value)?"":value);

				}else if(StringUtils.equals(key,"民族")){
					push.remove("question5_07");
					redisUtil.hset(reportSummaryx,"suspectNation", StringUtils.isBlank(value)?"":value);

				}else if(StringUtils.equals(key,"户籍地")){
					push.remove("question5_10");
					redisUtil.hset(reportSummaryx,"suspectHouseholRegister", StringUtils.isBlank(value)?"":value);

				}else if(StringUtils.equals(key,"居住地")){
					push.remove("question5_11");
					redisUtil.hset(reportSummaryx,"suspectResidence", StringUtils.isBlank(value)?"":value);

				}else if(StringUtils.equals(key,"工作单位")){
					push.remove("question5_12");
					redisUtil.hset(reportSummaryx,"suspectWorkunit", StringUtils.isBlank(value)?"":value);

				}else if(StringUtils.equals(key,"联系方式")){
					push.remove("question5_13");
					redisUtil.hset(reportSummaryx,"suspectContact", StringUtils.isBlank(value)?"":value);

				}else if(StringUtils.equals(key,"文化程度")){
					push.remove("question5_08");
					redisUtil.hset(reportSummaryx,"suspectEducation", StringUtils.isBlank(value)?"":value);

				}else if(StringUtils.equals(key,"婚姻状况")){
					push.remove("question5_09");
					redisUtil.hset(reportSummaryx,"suspectMarriage", StringUtils.isBlank(value)?"":value);

				}

			}

			if(push.isEmpty()){
					nextQuestion=question6;
				// 根据所缺要素分别提问以下问题


			}else{
				Collections.sort(push);// 排序
				nextQuestion = questionMap.get(push.get(0));
				push.remove(0);

				redisUtil.del(questionQueueRedis);
				redisUtil.lSet(questionQueueRedis,push);

				log.info("审讯摘要：\t"+redisUtil.hmget(reportSummaryx));

				redisUtil.set(nextQuestionRedis,nextQuestion);
				log.info("\t下一个问题是：\t"+(String)redisUtil.get(nextQuestionRedis));

			}


			question = StringUtils.replace(question,"51_哪项信息不正确？",question5_01);
			redisUtil.lSet(wholeQuestions,question); // 笔录

		}else if(StringUtils.contains(question,question5_02)
				|| StringUtils.contains(question,question5_03)
				|| StringUtils.contains(question,question5_04)
				|| StringUtils.contains(question,question5_05)
				|| StringUtils.contains(question,question5_06)
				|| StringUtils.contains(question,question5_06_1)
				|| StringUtils.contains(question,question5_07)
				|| StringUtils.contains(question,question5_08)
				|| StringUtils.contains(question,question5_09)
				|| StringUtils.contains(question,question5_10)
				|| StringUtils.contains(question,question5_11)
				|| StringUtils.contains(question,question5_12)
				|| StringUtils.contains(question,question5_13)
				|| StringUtils.contains(question,question5_13_1)
				) {

			List list = questionQueuem;

			// 身份证号码 和 电话号码 去除符号
			if(StringUtils.contains(question,question5_06)
				|| StringUtils.contains(question,question5_06_1)
				|| StringUtils.contains(question,question5_13)
				|| StringUtils.contains(question,question5_13_1)){

				// 问：身份证号码格式有误，请重新回复 /t 答：34012，31984。0929，03。95
				String response= StringUtils.replace(humanResponse,"，","");
				response= StringUtils.replace(response,"。","");

				question = StringUtils.replace(question,humanResponse,response);

			}

			if(StringUtils.contains(question,question5_06_1)
					|| StringUtils.contains(question,question5_13_1)){

				String searchStr=StringUtils.substringBetween(question, "问：","答");
				if(StringUtils.contains(question,question5_06_1)){
					question =StringUtils.replace(question,searchStr,"身份证号码是什么？");
				}else{
					question =StringUtils.replace(question,searchStr,"联系电话是什么？");

				}
			}

			Map personInfo =algorithmService.algorithmPersonInfo(question);

			if(StringUtils.contains(question,question5_02)){
				String variablex = (String)personInfo.get("姓名");
				if(StringUtils.isNotBlank(variablex)){
					redisUtil.hset(reportSummaryx,"suspectName", variablex);

				}

			}else  if(StringUtils.contains(question,question5_03)){
				String variablex = (String)personInfo.get("曾用名");
				if(StringUtils.isNotBlank(variablex)){
					redisUtil.hset(reportSummaryx,"suspectUsedName", variablex);

				}

			}else  if(StringUtils.contains(question,question5_04)){
				String variablex = (String)personInfo.get("性别");
				if(StringUtils.isNotBlank(variablex)){
					redisUtil.hset(reportSummaryx,"suspectGender", variablex);

				}

			}else  if(StringUtils.contains(question,question5_05)){
				String variablex = (String)personInfo.get("出生日期");

				redisUtil.hset(reportSummaryx,"suspectBirthday", StringUtils.isBlank(variablex)?"":variablex);

			}else  if(StringUtils.contains(question,question5_06)){

				String variablex = (String)personInfo.get("身份证号码");
				if(StringUtils.isNotBlank(variablex)){
					redisUtil.hset(reportSummaryx,"suspectIdCard", variablex);

				}else{
					// 回复的身份证号码格式不正确的话，则提问问题5.6.1 “身份证号码格式有误，请重新回复”
					list.add("question5_06_1");

				}

			}else  if(StringUtils.contains(question,question5_07)){
				String variablex = (String)personInfo.get("民族");
				if(StringUtils.isNotBlank(variablex)){
					redisUtil.hset(reportSummaryx,"suspectNation", variablex);

				}

			}else  if(StringUtils.contains(question,question5_08)){
				String variablex = (String)personInfo.get("受教育程度");
				if(StringUtils.isNotBlank(variablex)){
					redisUtil.hset(reportSummaryx,"suspectEducation", variablex);

				}

			}else  if(StringUtils.contains(question,question5_09)){
				String variablex = (String)personInfo.get("婚姻状况");
				if(StringUtils.isNotBlank(variablex)){
					redisUtil.hset(reportSummaryx,"suspectMarriage", variablex);

				}

			}else  if(StringUtils.contains(question,question5_10)){
				String variablex = (String)personInfo.get("户籍地");
				if(StringUtils.isNotBlank(variablex)){
					redisUtil.hset(reportSummaryx,"suspectHouseholRegister", variablex);

				}

			}else  if(StringUtils.contains(question,question5_11)){
				String variablex = (String)personInfo.get("居住地");
				if(StringUtils.isNotBlank(variablex)){
					redisUtil.hset(reportSummaryx,"suspectResidence", variablex);

				}

			}else  if(StringUtils.contains(question,question5_12)){
				String variablex = (String)personInfo.get("工作");
				if(StringUtils.isNotBlank(variablex)){
					redisUtil.hset(reportSummaryx,"suspectWorkunit", variablex);

				}

			}else  if(StringUtils.contains(question,question5_13)){

				// TODO 嫌疑人联系方式 若连续 2 次不通过，就不再问。
				int count = (int)redisUtil.hget(countRedis,"suspectContact");

				count++;
				redisUtil.hset(countRedis,"suspectContact",count);

				List list1 = (List)personInfo.get("联系方式");
				String variablex = (String) list1.get(0);


				if( !StringUtils.contains(variablex,"号码格式错误")){
					redisUtil.hset(reportSummaryx,"suspectContact", variablex);

				}else{
					if(count < 2){
						list.add("question5_13_1");
					}else{
						redisUtil.hset(reportSummaryx,"suspectContact",0);
					}

				}



			}



			if(list ==null || list.isEmpty()) {
				nextQuestion=question6 ;


			}else {
				Collections.sort(list);

				nextQuestion = questionMap.get((String) list.get(0));
				list.remove(0);

				redisUtil.del(questionQueueRedis);
				if(!list.isEmpty()){
					redisUtil.lSet(questionQueueRedis,list);
				}
			}


			System.out.println("案件提取要素：\t"+redisUtil.hmget(reportSummaryx));


			redisUtil.lSet(wholeQuestions,question); // 笔录

		}else if(StringUtils.contains(question, question6)) {

			Map map = algorithmService.algorithmPersonInfo(question);
			Map<String,String> mapVal = (Map) map.get("是否人大代表或政协委员");

			redisUtil.hset(reportSummaryx,"zhengxieweiyuan","");
			redisUtil.hset(reportSummaryx,"rendadaibiao","");

			if( StringUtils.equals( mapVal.get("value"),"否") ){
				nextQuestion = question7;

				redisUtil.hset(reportSummaryx,"zhengxieweiyuan","");
				redisUtil.hset(reportSummaryx,"rendadaibiao","");
			} else {

				if(mapVal.containsKey("政协委员") && mapVal.containsKey("人大代表")){

					if(StringUtils.isNotBlank( mapVal.get("政协委员")) &&  StringUtils.isNotBlank( mapVal.get("人大代表"))  ){
						nextQuestion=question7;
					}else{
						nextQuestion=question6_3;
					}

					redisUtil.hset(reportSummaryx,"zhengxieweiyuan",mapVal.get("政协委员"));
					redisUtil.hset(reportSummaryx,"rendadaibiao",mapVal.get("人大代表"));


				}else if(mapVal.containsKey("政协委员") || mapVal.containsKey("人大代表")){

					for (Map.Entry<String,String> entry: mapVal.entrySet()) {
						String key = entry.getKey();
						String value = entry.getValue();

						if(StringUtils.equals(key,"政协委员")){
							if(StringUtils.isBlank(value)){
								nextQuestion=question6_2;
							}else{
								nextQuestion=question7;
								redisUtil.hset(reportSummaryx,"zhengxieweiyuan",value);
							}
						}else if(StringUtils.equals(key,"人大代表")){
							if(StringUtils.isBlank(value)){
								nextQuestion=question6_1;

							}else{
								nextQuestion=question7;
								redisUtil.hset(reportSummaryx,"rendadaibiao",value);

							}
						}

					}


				}




			}


			redisUtil.lSet(wholeQuestions,question); // 笔录

		}else if(StringUtils.contains(question, question6_1)) {

			Map map = algorithmService.algorithmPersonInfo(question);
			String val = (String) map.get("是否人大代表或政协委员");

			if(StringUtils.isNotBlank(val)){
				redisUtil.hset(reportSummaryx,"rendadaibiao",val);
			}else{
				redisUtil.hset(reportSummaryx,"rendadaibiao","");

			}

			redisUtil.lSet(wholeQuestions,question); // 笔录

			nextQuestion=question7;

		}else if(StringUtils.contains(question, question6_2)) {
			Map map = algorithmService.algorithmPersonInfo(question);
			String val = (String) map.get("是否人大代表或政协委员");

			if(StringUtils.isNotBlank(val)){
				redisUtil.hset(reportSummaryx,"zhengxieweiyuan",val);
			}else{
				redisUtil.hset(reportSummaryx,"zhengxieweiyuan","");

			}

			redisUtil.lSet(wholeQuestions,question); // 笔录
			nextQuestion=question7;

		}else if(StringUtils.contains(question, question6_3)) {

			Map map = algorithmService.algorithmPersonInfo(question);
			String val = (String) map.get("是否人大代表或政协委员");

			if(StringUtils.isNotBlank(val)){
				redisUtil.hset(reportSummaryx,"rendadaibiao",val);
				redisUtil.hset(reportSummaryx,"zhengxieweiyuan",val);
			}else{
				redisUtil.hset(reportSummaryx,"rendadaibiao","");
				redisUtil.hset(reportSummaryx,"zhengxieweiyuan","");

			}

			redisUtil.lSet(wholeQuestions,question); // 笔录
			nextQuestion=question7;

		}else if(StringUtils.contains(question, question7)) {


			Map map = algorithmService.algorithmPersonInfo(question);
			String work = (String) map.get("工作");

			if(StringUtils.isNotBlank(work)){

				if(StringUtils.equals(work,"是")){
					// 是政府工作人员,但是没有具体回答
					nextQuestion = question7_1;
				}else{
					// 不是政府工作人员 或者 已经 在某政府部门就职

					// 如果家庭信息为空,跳转到  "你的父亲叫什么名字，多少岁，在哪里工作？"
					String familyInfoa = (String)paramMap.get("familyInfo");

					if(StringUtils.isBlank(familyInfoa)){
						List push = new ArrayList();

						push.add("question8_02");
						push.add("question8_03");
						push.add("question8_04");
						push.add("question8_05");
						push.add("question8_06");

						Collections.sort(push);

						nextQuestion = questionMap.get((String) push.get(0));
						push.remove(0);

						redisUtil.del(questionQueueRedis);
						if(!push.isEmpty()){
							redisUtil.lSet(questionQueueRedis,push);
						}



					}else{

						String question8Temp = this.generateQuestion8(paramMap,reportSummaryx);
						nextQuestion = question8Temp ;

						redisUtil.hset(reportSummaryx,"governmentAndInstitutions",work);

					}





				}


			}


			redisUtil.lSet(wholeQuestions,question); // 笔录




		}else if(StringUtils.contains(question, question7_1)) {

			Map map = algorithmService.algorithmPersonInfo(question);
			String work = (String) map.get("工作");

			redisUtil.hset(reportSummaryx,"governmentAndInstitutions",work);

			redisUtil.lSet(wholeQuestions,question); // 笔录


			// 如果家庭信息为空,跳转到  "你的父亲叫什么名字，多少岁，在哪里工作？"
			String familyInfoa = (String)paramMap.get("familyInfo");
			if(StringUtils.isBlank(familyInfoa)){
				List push = new ArrayList();

				push.add("question8_02");
				push.add("question8_03");
				push.add("question8_04");
				push.add("question8_05");
				push.add("question8_06");

				Collections.sort(push);

				nextQuestion = questionMap.get((String) push.get(0));
				push.remove(0);

				redisUtil.del(questionQueueRedis);
				if(!push.isEmpty()){
					redisUtil.lSet(questionQueueRedis,push);
				}


			}else{

				String question8Temp = this.generateQuestion8(paramMap,reportSummaryx);
				nextQuestion = question8Temp ;


			}



		}else if(StringUtils.contains(question, question8)) {
            //  请确认这些信息是否正确？
            List push = new ArrayList();

            if(  StringUtils.contains(humanResponse, "不") ) {
                // 回答“不正确“等，则提问问题8.1 “哪项信息不正确？
                push.add("question8_01");

            }


			String familyInfo = (String)paramMap.get("familyInfo");
			if(StringUtils.isBlank(familyInfo)){
				push.add("question8_02");
				push.add("question8_03");
				push.add("question8_04");
				push.add("question8_05");
				push.add("question8_06");

			}



            if(push.isEmpty()){

				String resumeInfo = (String) paramMap.get("resumeInfo");
				if(StringUtils.isBlank(resumeInfo)){
					nextQuestion=question09_02;
					push.add("question09_03");

					redisUtil.del(questionQueueRedis);
					redisUtil.lSet(questionQueueRedis,push);

					redisUtil.set(nextQuestionRedis,nextQuestion);
					log.info("\t下一个问题是：\t"+(String)redisUtil.get(nextQuestionRedis));

				}else{
					String question9Temp = this.generateQuestion9(paramMap,reportSummaryx);
					nextQuestion=question9Temp;

				}


			}else{
                Collections.sort(push);// 排序
                nextQuestion = questionMap.get(push.get(0));
                push.remove(0);

                redisUtil.del(questionQueueRedis);
                redisUtil.lSet(questionQueueRedis,push);

                log.info("审讯摘要：\t"+redisUtil.hmget(reportSummaryx));

                redisUtil.set(nextQuestionRedis,nextQuestion);
                log.info("\t下一个问题是：\t"+(String)redisUtil.get(nextQuestionRedis));

            }

			redisUtil.lSet(wholeQuestions,question); // 笔录

		}else if(StringUtils.contains(question,question8_01)){

			question = StringUtils.replace(question,question8_01,"81_哪项信息不正确？");

			Map map = algorithmService.algorithmElementsOfCase(question);
			Map<String,Object> familyErr = (Map<String, Object>) map.get("wahsterror");

			List push = questionQueuem;

			if(familyErr!=null){

				List<Map<String,String>> brother = (List<Map<String,String>>) redisUtil.hget(reportSummaryx,"brotherInfo");;
				List<Map<String,String>> childrenInfo = (List<Map<String,String>>) redisUtil.hget(reportSummaryx,"childrenInfo");;

				for(Map.Entry<String,Object> entry: familyErr.entrySet()){

					String key = entry.getKey();
					Map<String,String> map1 = (Map<String, String>) entry.getValue();

					if(StringUtils.equals(key,"父亲")){
						push.remove("question8_02");
						String fatherName = map1.get("name");
						String fatherAge = map1.get("age");
						String fatherJob = map1.get("company");

						redisUtil.hset(reportSummaryx,"fatherName",StringUtils.isBlank(fatherName)?"":fatherName);
						redisUtil.hset(reportSummaryx,"fatherAge",StringUtils.isBlank(fatherAge)?"":fatherAge);
						redisUtil.hset(reportSummaryx,"fatherJob",StringUtils.isBlank(fatherJob)?"":fatherJob);

					}else if(StringUtils.equals(key,"母亲")){
						push.remove("question8_03");

						String motherName = map1.get("name");
						String motherAge = map1.get("age");
						String motherJob = map1.get("company");

						redisUtil.hset(reportSummaryx,"motherName",StringUtils.isBlank(motherName)?"":motherName);
						redisUtil.hset(reportSummaryx,"motherAge",StringUtils.isBlank(motherAge)?"":motherAge);
						redisUtil.hset(reportSummaryx,"motherJob",StringUtils.isBlank(motherJob)?"":motherJob);

					}else if(StringUtils.equalsAny(key,"姐姐","妹妹","哥哥","弟弟")){
						push.remove("question8_04");

						String brotherRelation = (String) map1.get("relation");
						String brotherName = (String) map1.get("name");
						String brotherAge = (String) map1.get("age");
						String brotherJob = (String) map1.get("company");

						Map map2 = new HashMap();
						map2.put("brotherRelation",StringUtils.isBlank(brotherRelation)?"":brotherRelation);
						map2.put("brotherName",StringUtils.isBlank(brotherName)?"":brotherName);
						map2.put("brotherAge",StringUtils.isBlank(brotherAge)?"":brotherAge);
						map2.put("brotherJob",StringUtils.isBlank(brotherJob)?"":brotherJob);

						// 先删除
                        Iterator<Map<String, String>> iterator=brother.iterator();
                        while (iterator.hasNext()) {
                            Map<String,String> map3 = iterator.next();
                            String brotherRelation1 = map3.get("brotherRelation") ;

                            if (StringUtils.equals(brotherRelation1,brotherRelation)) {
                                iterator.remove();
                            }


                        }


						// 后添加
						brother.add(map2);

					}else if(StringUtils.equalsAny(key,"妻子","丈夫")){
						push.remove("question8_05");

						String spouseRelation = map1.get("relation");
						String spouseName = map1.get("name");
						String spouseAge = map1.get("age");
						String spouseJob = map1.get("company");

						redisUtil.hset(reportSummaryx,"spouseRelation",spouseRelation);
						redisUtil.hset(reportSummaryx,"spouseName",spouseName);
						redisUtil.hset(reportSummaryx,"spouseAge",spouseAge);
						redisUtil.hset(reportSummaryx,"spouseJob",spouseJob);

					}else if(StringUtils.equalsAny(key,"儿子","女儿")){
						push.remove("question8_06");

						String childrenRelation = (String) map1.get("relation");
						String childrenName = (String) map1.get("name");
						String childrenAge = (String) map1.get("age");

						Map map2 = new HashMap();
						map2.put("childrenRelation",StringUtils.isBlank(childrenRelation)?"":childrenRelation);
						map2.put("childrenName",StringUtils.isBlank(childrenName)?"":childrenName);
						map2.put("childrenAge",StringUtils.isBlank(childrenAge)?"":childrenAge);


                        Iterator<Map<String, String>> iterator=childrenInfo.iterator();
                        while (iterator.hasNext()) {
                            Map<String,String> map3 = iterator.next();
                            String childrenRelation1 = map3.get("childrenRelation") ;
                            // 先删除
                            if (StringUtils.equals(childrenRelation1,childrenRelation)) {
                                iterator.remove();
                            }


                        }

                        // 后添加
						childrenInfo.add(map2);


					}

				}

				redisUtil.hset(reportSummaryx,"brotherInfo",brother);
				redisUtil.hset(reportSummaryx,"childrenInfo",childrenInfo);


			}
			// 去重
			List unique = (List) push.stream().distinct().collect(Collectors.toList());


			if(unique.isEmpty()){
				String resumeInfo = (String) paramMap.get("resumeInfo");
				if(StringUtils.isBlank(resumeInfo)){
					nextQuestion=question09_02;
					push.add("question09_03");

					redisUtil.del(questionQueueRedis);
					redisUtil.lSet(questionQueueRedis,push);

					redisUtil.set(nextQuestionRedis,nextQuestion);
					log.info("\t下一个问题是：\t"+(String)redisUtil.get(nextQuestionRedis));

				}else{
					String question9Temp = this.generateQuestion9(paramMap,reportSummaryx);
					nextQuestion=question9Temp;

				}


			}else{
				Collections.sort(unique);// 排序
				nextQuestion = questionMap.get(unique.get(0));
				unique.remove(0);

				redisUtil.del(questionQueueRedis);
				redisUtil.lSet(questionQueueRedis,unique);

				log.info("审讯摘要：\t"+redisUtil.hmget(reportSummaryx));

				redisUtil.set(nextQuestionRedis,nextQuestion);
				log.info("\t下一个问题是：\t"+(String)redisUtil.get(nextQuestionRedis));

			}

			question = StringUtils.replace(question,"81_哪项信息不正确？",question8_01);
			redisUtil.lSet(wholeQuestions,question); // 笔录

		}else if(StringUtils.contains(question,question8_02)
                || StringUtils.contains(question,question8_03)
                || StringUtils.contains(question,question8_04)
                || StringUtils.contains(question,question8_05)
                || StringUtils.contains(question,question8_06)
                ){

            List list = questionQueuem;

            Map personInfo =algorithmService.algorithmPersonInfo(question);
            List<Map> family = (List)personInfo.get("家庭");
            Map<String,String> map1 = family.get(0);

            if(StringUtils.contains(question,question8_02)){

                if(family.size() == 1 && StringUtils.equals(map1.get("message"),"无")){
//                    list.add("question8_02");
                }else {

                    String fatherName = map1.get("name");
                    String fatherAge = map1.get("age");
                    String fatherJob = map1.get("company");

                    redisUtil.hset(reportSummaryx,"fatherName",StringUtils.isBlank(fatherName)?"":fatherName);
                    redisUtil.hset(reportSummaryx,"fatherAge",StringUtils.isBlank(fatherAge)?"":fatherAge);
                    redisUtil.hset(reportSummaryx,"fatherJob",StringUtils.isBlank(fatherJob)?"":fatherJob);

                }
            }else if(StringUtils.contains(question,question8_03)){

                if(family.size() == 1 && StringUtils.equals(map1.get("message"),"无")){
//                    list.add("question8_03");
                }else {

                    String motherName = map1.get("name");
                    String motherAge = map1.get("age");
                    String motherJob = map1.get("company");

                    redisUtil.hset(reportSummaryx,"motherName",StringUtils.isBlank(motherName)?"":motherName);
                    redisUtil.hset(reportSummaryx,"motherAge",StringUtils.isBlank(motherAge)?"":motherAge);
                    redisUtil.hset(reportSummaryx,"motherJob",StringUtils.isBlank(motherJob)?"":motherJob);

                }

            }else if(StringUtils.contains(question,question8_05)){

                if(family.size() == 1 && StringUtils.equals(map1.get("message"),"无")){
//                    list.add("question8_05");
                }else {

                    String spouseRelation = map1.get("relation");
                    String spouseName = map1.get("name");
                    String spouseAge = map1.get("age");
                    String spouseJob = map1.get("company");

                    redisUtil.hset(reportSummaryx,"spouseRelation",spouseRelation);
                    redisUtil.hset(reportSummaryx,"spouseName",spouseName);
                    redisUtil.hset(reportSummaryx,"spouseAge",spouseAge);
                    redisUtil.hset(reportSummaryx,"spouseJob",spouseJob);

                }


            }else if(StringUtils.contains(question,question8_04)){

                if(family.size() == 1 && StringUtils.equals(map1.get("message"),"无")){
//                    list.add("question8_04");
                }else {
                    List brother = new ArrayList();


                    for (Map<String,String> obj: family){

                        String brotherRelation = (String) obj.get("relation");
						String brotherName = (String) obj.get("name");
						String brotherAge = (String) obj.get("age");
						String brotherJob = (String) obj.get("company");

						Map map = new HashMap();
						map.put("brotherRelation",StringUtils.isBlank(brotherRelation)?"":brotherRelation);
                        map.put("brotherName",StringUtils.isBlank(brotherName)?"":brotherName);
                        map.put("brotherAge",StringUtils.isBlank(brotherAge)?"":brotherAge);
                        map.put("brotherJob",StringUtils.isBlank(brotherJob)?"":brotherJob);

                        brother.add(map);
                    }


                    redisUtil.hset(reportSummaryx,"brotherInfo",brother);


                }



            }else if(StringUtils.contains(question,question8_06)){


                if(family.size() == 1 && StringUtils.equals(map1.get("message"),"无")){
//                    list.add("question8_06");
                }else {

                    List childrenInfo = new ArrayList();


                    for (Map<String,String> obj:family){

                        String childrenRelation = (String) obj.get("relation");
						String childrenName = (String) obj.get("name");
						String childrenAge = (String) obj.get("age");

						Map map = new HashMap();
						map.put("childrenRelation",StringUtils.isBlank(childrenRelation)?"":childrenRelation);
                        map.put("childrenName",StringUtils.isBlank(childrenName)?"":childrenName);
                        map.put("childrenAge",StringUtils.isBlank(childrenAge)?"":childrenAge);

                        childrenInfo.add(map);
                    }


                    redisUtil.hset(reportSummaryx,"childrenInfo",childrenInfo);


                }


            }

            if(list ==null || list.isEmpty()) {

				String resumeInfo = (String) paramMap.get("resumeInfo");
				if(StringUtils.isBlank(resumeInfo)){
					List push = questionQueuem;

					nextQuestion=question09_02;
					push.add("question09_03");

					redisUtil.del(questionQueueRedis);
					redisUtil.lSet(questionQueueRedis,push);

					redisUtil.set(nextQuestionRedis,nextQuestion);
					log.info("\t下一个问题是：\t"+(String)redisUtil.get(nextQuestionRedis));

				}else{
					String question9Temp = this.generateQuestion9(paramMap,reportSummaryx);
					nextQuestion=question9Temp;

				}

            }else {
                Collections.sort(list);

                nextQuestion = questionMap.get((String) list.get(0));
                list.remove(0);

                redisUtil.del(questionQueueRedis);
                if(!list.isEmpty()){
                    redisUtil.lSet(questionQueueRedis,list);
                }
            }

            System.out.println("案件提取要素：\t"+redisUtil.hmget(reportSummaryx));

			redisUtil.lSet(wholeQuestions,question); // 笔录

        }else if(StringUtils.contains(question,question09)){
            // 请确认这些个人简历信息是否正确？

			List push = new ArrayList();

			if(StringUtils.contains(question,"不")){
				push.add("question09_01");
			}


			if(push.isEmpty()){
				nextQuestion=question10;

			}else{
				Collections.sort(push);// 排序
				nextQuestion = questionMap.get(push.get(0));
				push.remove(0);

				redisUtil.del(questionQueueRedis);
				redisUtil.lSet(questionQueueRedis,push);

				log.info("审讯摘要：\t"+redisUtil.hmget(reportSummaryx));

				redisUtil.set(nextQuestionRedis,nextQuestion);
				log.info("\t下一个问题是：\t"+(String)redisUtil.get(nextQuestionRedis));

			}

			redisUtil.lSet(wholeQuestions,question); // 笔录

        }else if(StringUtils.contains(question, question09_01)) {

			question = StringUtils.replace(question,question09_01,"91_哪项信息不正确？");

			Map map = algorithmService.algorithmElementsOfCase(question);
			Map<String,Object> mapErr = (Map<String, Object>) map.get("wahsterror");

			List push = questionQueuem;

			for(Map.Entry<String,Object> entry: mapErr.entrySet()) {

				String key = entry.getKey(); // 工作/教育经历
				Map<String,String> map1 = (Map<String, String>) entry.getValue();

				if (StringUtils.equals(key, "工作")) {
					push.remove("question09_03");

					int i =0;

					for(Map.Entry<String,String> entry1: map1.entrySet()) {
						String key1 = entry1.getKey();
						String value1 = entry1.getValue();
						if(i>0){

							redisUtil.hset(reportSummaryx,"nowWorkingCompany",StringUtils.isBlank(key1)?"":key1);
							redisUtil.hset(reportSummaryx,"nowWorkingTime",StringUtils.isBlank(value1)?"":value1);
						}else{
							redisUtil.hset(reportSummaryx,"startWorkingCompany",StringUtils.isBlank(key1)?"":key1);
							redisUtil.hset(reportSummaryx,"startWorkingTime",StringUtils.isBlank(value1)?"":value1);

						}

						i++;
					}



				}else if (StringUtils.equals(key, "教育经历")) {
					push.remove("question09_02");

					for(Map.Entry<String,String> entry1: map1.entrySet()) {
						String key1 = entry1.getKey(); // date
						String value1 = entry1.getValue();// 2015

						if(StringUtils.equals(key1,"date")){
							redisUtil.hset(reportSummaryx,"graduationDate",StringUtils.isBlank(value1)?"":value1);

						}else if(StringUtils.equals(key1,"school")){
							redisUtil.hset(reportSummaryx,"graduationSchool",StringUtils.isBlank(value1)?"":value1);
						}



					}
				}

			}


			if(push.isEmpty()){
				nextQuestion=question10;

			}else{
				Collections.sort(push);// 排序
				nextQuestion = questionMap.get(push.get(0));
				push.remove(0);

				redisUtil.del(questionQueueRedis);
				redisUtil.lSet(questionQueueRedis,push);

				log.info("审讯摘要：\t"+redisUtil.hmget(reportSummaryx));

				redisUtil.set(nextQuestionRedis,nextQuestion);
				log.info("\t下一个问题是：\t"+(String)redisUtil.get(nextQuestionRedis));

			}

			question = StringUtils.replace(question,"91_哪项信息不正确？",question09_01);
			redisUtil.lSet(wholeQuestions,question); // 笔录

		}else if(StringUtils.contains(question, question09_02)
				|| StringUtils.contains(question, question09_03)) {

        	List list = questionQueuem;

			Map personInfo =algorithmService.algorithmPersonInfo(question);

			if(StringUtils.contains(question,question09_02)){

				Map data= (Map) personInfo.get("school");

				String date = (String) data.get("date");
				String school = (String) data.get("school");


				if(StringUtils.isBlank(date) && StringUtils.isBlank(school)){
//					list.add("question09_02");
				}else {

					redisUtil.hset(reportSummaryx,"graduationDate",StringUtils.isBlank(date)?"":date);
					redisUtil.hset(reportSummaryx,"graduationSchool",StringUtils.isBlank(school)?"":school);

				}



			}else if(StringUtils.contains(question,question09_03)){

				Map<String,String> data= (Map) personInfo.get("工作经历");

				if(data.isEmpty()){
//					list.add("question09_03");
				}else {
					int i =0;
					for (Map.Entry<String, String> entry : data.entrySet()) {

//						"工作经历": {
//							"美的集团": "2015年-",
//							"健力宝集团": "2017年-"
//						}
						String mapKey = entry.getKey();
						String mapValue = entry.getValue();

						System.out.println(mapKey+":"+mapValue);

						if(i>0){
							redisUtil.hset(reportSummaryx,"nowWorkingCompany",StringUtils.isBlank(mapKey)?"":mapKey);
							redisUtil.hset(reportSummaryx,"nowWorkingTime",StringUtils.isBlank(mapValue)?"":mapValue);

						}else{

							redisUtil.hset(reportSummaryx,"startWorkingCompany",StringUtils.isBlank(mapKey)?"":mapKey);
							redisUtil.hset(reportSummaryx,"startWorkingTime",StringUtils.isBlank(mapValue)?"":mapValue);

						}

						i++;
					}


				}
			}


			if(list ==null || list.isEmpty()) {

				nextQuestion=question10;

			}else {
				Collections.sort(list);

				nextQuestion = questionMap.get((String) list.get(0));
				list.remove(0);

				redisUtil.del(questionQueueRedis);
				if(!list.isEmpty()){
					redisUtil.lSet(questionQueueRedis,list);
				}
			}

			System.out.println("案件提取要素：\t"+redisUtil.hmget(reportSummaryx));

			redisUtil.lSet(wholeQuestions,question); // 笔录

		}else if(StringUtils.contains(question, question10)) {

        	// 电话号码抽取
			Map result =algorithmService.algorithmElementsOfCase(question);
			Map family_phone = (Map) result.get("family_phone");
			String phone = (String) family_phone.get("phone");
			redisUtil.hset(reportSummaryx,"familyTelephone",StringUtils.isBlank(phone)?"":phone);

			nextQuestion=question11;

			redisUtil.lSet(wholeQuestions,question); // 记录笔录

		}else if(StringUtils.contains(question, question11)) {
			// 前科
			String criminalRecord="";

			Map result =algorithmService.algorithmElementsOfCase(question);
			List list = (List) result.get("criminalpunishment");

			if(list==null || list.isEmpty()){
				criminalRecord="";
			}else{
				Map map = (Map) list.get(0);
				criminalRecord= (String) map.get("罪名");
			}


			redisUtil.hset(reportSummaryx,"criminalRecord",StringUtils.isBlank(criminalRecord)?"":criminalRecord);


  			if(StringUtils.containsAny(humanResponse,"没","无")
					|| StringUtils.isNotBlank(criminalRecord)){

  				nextQuestion=question12;

			}else {
				nextQuestion=question11_1;

			}


			redisUtil.lSet(wholeQuestions,question); // 记录笔录

		}else if(StringUtils.contains(question,question11_1)){
			// 前科
			String criminalRecord="";

			Map result =algorithmService.algorithmElementsOfCase(question);
			List list = (List) result.get("criminalpunishment");

			if(list==null || list.isEmpty()){
				criminalRecord="";
			}else{
				Map map = (Map) list.get(0);
				criminalRecord= (String) map.get("罪名");
			}

			redisUtil.hset(reportSummaryx,"criminalRecord",StringUtils.isBlank(criminalRecord)?"":criminalRecord);


			redisUtil.lSet(wholeQuestions,question); // 记录笔录

			nextQuestion=question12;

		}else if(StringUtils.contains(question,question12)){
			redisUtil.lSet(wholeQuestions,question); // 记录笔录

			nextQuestion=question13;
		}else if(StringUtils.contains(question,question13)){
			redisUtil.lSet(wholeQuestions,question); // 记录笔录

			nextQuestion=question14;
		}else if(StringUtils.contains(question,question14)){
			redisUtil.lSet(wholeQuestions,question); // 记录笔录

			String question15Temp = generateQuestion15(paramMap,reportSummaryx);

			nextQuestion=question15Temp;

		}else if(StringUtils.contains(question,question15)){
			redisUtil.lSet(wholeQuestions,question); // 记录笔录

			humanResponse= StringUtils.replace(humanResponse,"，","");
			humanResponse= StringUtils.replace(humanResponse,"。","");

			// 你是否在 2019年11月13日20点35分 在 大良镇清晖路150号 驾驶 粤E11111号牌、小型载客汽车类型的机动车因交通违法行为被公安交警部门抓获？
			if(StringUtils.containsAny(humanResponse,"不是","没有")){
				intervention="1";
				nextQuestion=question16 ;

			}else if(StringUtils.containsAny(humanResponse,"是","是的","有","有的")){
				nextQuestion=question16 ;

			}else {


				int countquestion15 = 0;

				boolean bool =  redisUtil.hHasKey(countRedis,"question15");

				if(!bool){
					redisUtil.hset(countRedis,"question15",1);
				}else{
					countquestion15 = (int) redisUtil.hget(countRedis,"question15");
					redisUtil.hset(countRedis,"question15",++countquestion15);

				}

				System.out.println("不清楚/不回复次数：question15\t"+redisUtil.hget(countRedis,"question15"));

				if((int)redisUtil.hget(countRedis,"question15") > 1){

					// 重复两遍还是不清楚/不回复，则提示人工干预。 人工干预以后，回到下一个问题
					nextQuestion=question16;
					intervention = "1";
					// 复位
					redisUtil.hset(countRedis,"question15",0);

				}else{

					String question15Temp = generateQuestion15(paramMap,reportSummaryx);
					nextQuestion=question15Temp;

				}



			}



		}else if(StringUtils.contains(question,question16)){
			redisUtil.lSet(wholeQuestions,question); // 记录笔录
			nextQuestion=question20 ;

		}else if(StringUtils.contains(question,question20)){
            redisUtil.lSet(wholeQuestions,question); // 记录笔录


			if(StringUtils.containsAny(humanResponse,"没","没有")){

				// 没有考取过驾驶证
				redisUtil.hset(reportSummaryx,"drivingLicense","0");
				nextQuestion = question25 ;

			}else if(StringUtils.containsAny(humanResponse,"不知道")){
				// 不知道，人工干预
				intervention = "1";
				redisUtil.hset(reportSummaryx,"drivingLicense","0");

				nextQuestion = question25 ;
			}else{
				// 有考取过驾驶证
				redisUtil.hset(reportSummaryx,"drivingLicense","1");

				nextQuestion = question20_1 ;

            }


        }else if(StringUtils.contains(question,question20_1)){
            redisUtil.lSet(wholeQuestions,question); // 记录笔录

            if(StringUtils.contains(humanResponse,"不")){
                intervention ="1";
            }

			nextQuestion = question25 ;


        }else if(StringUtils.contains(question,question25)){
            redisUtil.lSet(wholeQuestions,question); // 记录笔录

			String drivingLicense = (String) redisUtil.hget(reportSummaryx,"drivingLicense");

			Map carowner = algorithmService.algorithmElementsOfCase(question);
			Map owner = (Map) carowner.get("carowner");
			String relation = (String) owner.get("relation");
			// 有没有考取过驾驶证
			redisUtil.hset(reportSummaryx,"owner",relation);

			if(StringUtils.equals(drivingLicense,"1") || StringUtils.equals(relation,"本人") ){
				// 如 考取驾驶证 或者 车主是本人情况
				nextQuestion=question25_2;

			}else {
				nextQuestion=question25_1;


			}

		}else if(StringUtils.contains(question,question25_1)){
			redisUtil.lSet(wholeQuestions,question); // 记录笔录

			if(StringUtils.contains(humanResponse,"借")){
				nextQuestion=question25_1_1;
			}else{
				nextQuestion=question25_2;

			}


		}else if(StringUtils.contains(question,question25_1_1)){
			redisUtil.lSet(wholeQuestions,question); // 记录笔录
			nextQuestion=question25_2;


		}else if(StringUtils.contains(question,question25_2)){
			redisUtil.lSet(wholeQuestions,question); // 记录笔录

			if(StringUtils.containsAny(humanResponse,"没","无")){
				nextQuestion = question25_2_1;

			}else{
				nextQuestion = question26;

			}


		}else if(StringUtils.contains(question,question25_2_1)){
			redisUtil.lSet(wholeQuestions,question); // 记录笔录

			if(!StringUtils.containsAny(humanResponse,"没","无")){
				intervention="1";
			}

			nextQuestion = question26;


		}else if(StringUtils.contains(question,question26)){
			redisUtil.lSet(wholeQuestions,question); // 记录笔录

            nextQuestion=question27;
		}else if(StringUtils.contains(question,question27)){
			redisUtil.lSet(wholeQuestions,question); // 记录笔录

            // 其他交通违法行为
            Map map = algorithmService.algorithmElementsOfCase(question);
            List<String> illegalActivities = (List) map.get("casereason");

            if(StringUtils.containsAny(humanResponse,"没","无")
                    || !illegalActivities.isEmpty()){

                nextQuestion = question28 ;

            }else {

                nextQuestion = question27_1 ;
            }


		}else if(StringUtils.contains(question,question27_1)) {
            redisUtil.lSet(wholeQuestions,question); // 记录笔录

            nextQuestion = question28 ;

        } else if(StringUtils.contains(question,question28)) {
            redisUtil.lSet(wholeQuestions,question); // 记录笔录

            if(StringUtils.contains(humanResponse,"没")){
                intervention="1"; // 人工干预
                nextQuestion = question29 ;

            }else{
                nextQuestion = question29 ;

            }

        }else if(StringUtils.contains(question,question29)) {
            redisUtil.lSet(wholeQuestions,question); // 记录笔录

            if(StringUtils.contains(humanResponse,"没")){
                nextQuestion = question30 ;

            }else{
                intervention="1"; // 人工干预
                nextQuestion = question30 ;

            }

        }else if(StringUtils.contains(question,question30)) {
            redisUtil.lSet(wholeQuestions,question); // 记录笔录

            if(StringUtils.containsAny(humanResponse,"没","无")){
                nextQuestion = question31 ;

            }else{
                intervention="1"; // 人工干预
                nextQuestion = question30_1 ;

            }

        } else if(StringUtils.contains(question,question30_1)) {
			redisUtil.lSet(wholeQuestions,question); // 记录笔录
			nextQuestion = question31 ;

		} else if(StringUtils.contains(question,question31)) {
            redisUtil.lSet(wholeQuestions,question); // 记录笔录

            if(StringUtils.containsAny(humanResponse,"不","不属实")){
                intervention="1"; // 人工干预
                nextQuestion="审讯完毕";

            }else{
                nextQuestion="审讯完毕";

            }

            log.info("******************\t审讯完毕 \t");

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

		}



		ret.put("nextQuestion", nextQuestion);
		ret.put("intervention", intervention);

//		log.info("下一个问题是：\t"+nextQuestion);

		// 保存下一个问题
		redisUtil.set(nextQuestionRedis,nextQuestion);
		redisUtil.expire(nextQuestionRedis,expireTime1);

		System.out.println("\t下一个问题是：\t"+(String)redisUtil.get(nextQuestionRedis));

		return ret;

	}




	/**
	 *  由 请求参数 生成 问题8（ 家庭状况 信息）,并将其值存入 redis
	 * */
	public String generateQuestion8(Map<String,Object> paramMap,String reportSummaryx){

		StringBuilder familyInfo = new StringBuilder("");

        String familyInfoa = (String)paramMap.get("familyInfo");
        if(StringUtils.isNotBlank(familyInfoa)){
            familyInfo.append(familyInfoa);
        }
        redisUtil.hset(reportSummaryx,"familyInfo",StringUtils.isBlank(familyInfoa)?"":familyInfoa);


        familyInfo.append(question8);


		return  familyInfo.toString();

	}






        /**
         * 你是否在XX（案发时间）在xxx（案发地点）驾驶xxx号牌、xxx类型的机动车因交通违法行为被公安交警部门抓获？
         *
         *  由 请求参数 生成 问题15（案件时间，地点，车辆）,并将其值存入 redis
         * */
	public String generateQuestion15(Map<String,Object> paramMap,String reportSummaryx){

		StringBuilder question15Temp = new StringBuilder("你是否在");

		/*		TimeOfCrime : "2019年11月13日20点35分",
				happeningPlace : "大良镇清晖路150号",
				vehicleType : "小型载客汽车",
				vehicleNumber : "粤E11111"*/

		String TimeOfCrime = (String) paramMap.get("TimeOfCrime");
		String happeningPlace = (String) paramMap.get("happeningPlace");
		String vehicleNumber = (String) paramMap.get("vehicleNumber");
		String vehicleType = (String) paramMap.get("vehicleType");

		if(StringUtils.isNotBlank(TimeOfCrime)){
			TimeOfCrime = commonUtil.dateFormatAccurateTOHour(TimeOfCrime);

		}

		redisUtil.hset(reportSummaryx,"TimeOfCrime",StringUtils.isBlank(TimeOfCrime)?"":TimeOfCrime);
		redisUtil.hset(reportSummaryx,"happeningPlace",StringUtils.isBlank(happeningPlace)?"":happeningPlace);
		redisUtil.hset(reportSummaryx,"vehicleNumber",StringUtils.isBlank(vehicleNumber)?"":vehicleNumber);
		redisUtil.hset(reportSummaryx,"vehicleType",StringUtils.isBlank(vehicleType)?"":vehicleType);

		if(StringUtils.isNotBlank(TimeOfCrime) ){
			question15Temp.append(TimeOfCrime);
		}
		if(StringUtils.isNotBlank(happeningPlace) ){
			question15Temp.append("在").append(happeningPlace);
		}
		if(StringUtils.isNotBlank(vehicleNumber) ){
			question15Temp.append("驾驶").append(vehicleNumber).append("号牌、");
		}

		if(StringUtils.isNotBlank(vehicleType) ){
			question15Temp.append(vehicleType);
		}


		question15Temp.append(question15);
		return question15Temp.toString();
	}

    /**
     *  由 请求参数 生成 问题9（个人简历信息）,并将其值存入 redis
     * */
    public String generateQuestion9(Map<String,Object> paramMap,String reportSummaryx){
		StringBuilder question9Temp = new StringBuilder("");

        String resumeInfo = (String) paramMap.get("resumeInfo");
        if(StringUtils.isNotBlank(resumeInfo)){
            question9Temp.append(resumeInfo);

        }
        redisUtil.hset(reportSummaryx,"resumeInfo",StringUtils.isBlank(resumeInfo)?"":resumeInfo);


        question9Temp.append(question09);

        return question9Temp.toString();

    }


	/**
	 *  由 请求参数 生成 问题5（嫌疑人信息）,并将其值存入 redis
	 * */
	public String generateQuestion5(Map<String,Object> paramMap,String reportSummaryx){

		String suspectName = (String) paramMap.get("suspectName"); // 嫌疑人姓名
		String suspectUsedName = (String) paramMap.get("suspectUsedName"); // 嫌疑人曾用名
		String suspectGender = (String) paramMap.get("suspectGender"); // 嫌疑人性别
		String suspectBirthday = (String) paramMap.get("suspectBirthday"); // 嫌疑人出生日期
		String suspectAge = (String) paramMap.get("suspectAge"); // 年龄
		String suspectIdCard = (String) paramMap.get("suspectIdCard"); // 身份证号码
		String suspectNation = (String) paramMap.get("suspectNation"); // 民族

		String suspectContact = (String) paramMap.get("suspectContact"); // 联系电话
		String suspectEducation = (String) paramMap.get("suspectEducation"); // 教育程度
		String suspectMarriage = (String) paramMap.get("suspectMarriage"); // 婚姻状态
		String suspectHouseholRegister = (String) paramMap.get("suspectHouseholRegister"); //户籍地
		String suspectResidence = (String) paramMap.get("suspectResidence"); // 现住址
		String suspectWorkunit = (String) paramMap.get("suspectWorkunit"); // 工作单位


		if(StringUtils.isNotBlank(suspectBirthday)){
			suspectBirthday = commonUtil.dateFormatAccurateToDay(suspectBirthday);

		}



		redisUtil.hset(reportSummaryx,"suspectName",StringUtils.isBlank(suspectName)?"":suspectName);
		redisUtil.hset(reportSummaryx,"suspectUsedName",StringUtils.isBlank(suspectUsedName)?"":suspectUsedName);
		redisUtil.hset(reportSummaryx,"suspectGender",StringUtils.isBlank(suspectGender)?"":suspectGender);
		redisUtil.hset(reportSummaryx,"suspectBirthday",StringUtils.isBlank(suspectBirthday)?"":suspectBirthday);
		redisUtil.hset(reportSummaryx,"suspectAge",StringUtils.isBlank(suspectAge)?"":suspectAge);
		redisUtil.hset(reportSummaryx,"suspectIdCard",StringUtils.isBlank(suspectIdCard)?"":suspectIdCard);
		redisUtil.hset(reportSummaryx,"suspectNation",StringUtils.isBlank(suspectNation)?"":suspectNation);
		redisUtil.hset(reportSummaryx,"suspectContact",StringUtils.isBlank(suspectContact)?"":suspectContact);
		redisUtil.hset(reportSummaryx,"suspectEducation",StringUtils.isBlank(suspectEducation)?"":suspectEducation);
		redisUtil.hset(reportSummaryx,"suspectMarriage",StringUtils.isBlank(suspectMarriage)?"":suspectMarriage);
		redisUtil.hset(reportSummaryx,"suspectHouseholRegister",StringUtils.isBlank(suspectHouseholRegister)?"":suspectHouseholRegister);
		redisUtil.hset(reportSummaryx,"suspectResidence",StringUtils.isBlank(suspectResidence)?"":suspectResidence);
		redisUtil.hset(reportSummaryx,"suspectWorkunit",StringUtils.isBlank(suspectWorkunit)?"":suspectWorkunit);


		StringBuilder question5Temp = new StringBuilder("");

		if(StringUtils.isNotBlank(suspectName)){
			question5Temp.append("你的名字叫").append(suspectName).append("，");
		}

		if(StringUtils.isNotBlank(suspectUsedName)){
			question5Temp.append("曾用名：").append(suspectUsedName).append("，");
		}else{
//			question5Temp.append("无曾用名，");
		}

		if(StringUtils.isNotBlank(suspectGender)){
			question5Temp.append(suspectGender).append("，");

		}

		if(StringUtils.isNotBlank(suspectBirthday)){
			question5Temp.append(suspectBirthday).append("出生，");

		}

		if(StringUtils.isNotBlank(suspectAge)){
			question5Temp.append("今年").append(suspectAge).append("岁，");

		}

		if(StringUtils.isNotBlank(suspectIdCard)){
			question5Temp.append("居民身份证号码：").append(suspectIdCard).append("，");

		}

		if(StringUtils.isNotBlank(suspectNation)){
			question5Temp.append("民族:").append(suspectNation).append("，");
		}

		if(StringUtils.isNotBlank(suspectEducation)){
			question5Temp.append("教育程度:").append(suspectEducation).append("，");
		}

		if(StringUtils.isNotBlank(suspectMarriage)){
			question5Temp.append("婚姻状况:").append(suspectMarriage).append("，");
		}

		if(StringUtils.isNotBlank(suspectHouseholRegister)){
			question5Temp.append("户籍所在地：").append(suspectHouseholRegister).append("，");
		}

		if(StringUtils.isNotBlank(suspectResidence)){
			question5Temp.append("现住址：").append(suspectResidence).append("，");
		}

		if(StringUtils.isNotBlank(suspectWorkunit)){
			question5Temp.append("工作单位：").append(suspectWorkunit).append("，");
		}

		if(StringUtils.isNotBlank(suspectContact)){
			question5Temp.append("联系电话：").append(suspectContact).append("，");
		}

		question5Temp.append(question5);

		return question5Temp.toString();

	}



	public static void main(String[] args) {

	}





}
