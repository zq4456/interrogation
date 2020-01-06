package com.dinfo.robotea.controller;

import com.dinfo.robotea.http.TrafficClient;
import com.dinfo.robotea.properties.QuestionProperties;
import com.dinfo.robotea.properties.RedisUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 *	顺德公安 酒驾、无证驾驶及伪造车牌号码审讯
 */
@Controller
@RequestMapping("/drive")
public class DriveController {

	private static final Logger log = LoggerFactory.getLogger(DriveController.class);

	@Autowired
	private TrafficClient trafficClient;

	@Autowired
	private RedisUtil redisUtil;


	@Autowired
	private QuestionProperties questionProperties;



	// 案发地址补全前缀
    private final static  String addressCompletion = "广东省佛山市顺德区";

	private static  String question1="我们是佛山市顺德区公安局登录民警账号所属单位的民警，现就有关案情依法对你进行询问，你应当如实回答，故意作伪证或者隐匿证据会负相应的法律责任，对案件无关问题，你有拒绝回答的权利，你有要求办案人员或者公安机关负责人回避的权利，有陈述和申辩的权利，以上权利义务告知，你听清楚了吗？";
	private final static  String question2="你今天因何事主动前来佛山市顺德区公安局";
	private final static  String question3="办案民警有否将《行政案件权利义务告知书》送达给你？你是否阅读过《行政案件权利义务告知书》上的内容？";

	private final static  String question4="是否申请有关人员回避？";
	private final static  String question4_1="就是申请其他警员来处理你这案子，请问是否确定需要申请有关人员回避？";

	private final static  String question5="请确认这些个人基本信息是否正确？";
	private final static  String question5_01="哪项个人基本信息不正确？";

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
    private final static  String question8_01="哪项家庭状况信息不正确？";
    private final static  String question8_02="你的父亲叫什么名字，多少岁，在哪里工作？";
    private final static  String question8_03="你的母亲叫什么名字，多少岁，在哪里工作？";
    private final static  String question8_04="有没有兄弟姐妹，叫什么名字，多少岁，在哪里工作？";
    private final static  String question8_05="有没有配偶，叫什么名字，多少岁，在哪里工作？";
    private final static  String question8_06="有没有子女，叫什么名字，多少岁？";

    private final static  String question09="请确认这些个人简历信息是否正确？";
    private final static  String question09_01="哪项个人简历信息不正确？";

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

	private final static  String question17="执勤民警现场是否有对你进行呼气酒精测试？";

	private final static  String question17_1="执勤民警现场对你进行呼气酒精测试结果如何？";
	private final static  String question17_1_1="你的血液检查结果是多少？";
	private final static  String question17_1_2="你对该呼气酒精检测结果有没异议？";
	private final static  String question17_1_2_1="当时执勤民警有没有带你抽血？";
	private final static  String question17_1_2_1_1="当时现场呼气酒精测试结果检测单的“无异议”签名是否你本人签的？";
	private final static  String question17_1_2_1_2="你的血液检查结果是多少？";

    private final static  String question17_2="执勤民警现场是否有带你去进行抽血？";
    private final static  String question17_2_1="你的血液检查结果是多少？";
    private final static  String question17_2_1_1="当时现场呼气酒精测试结果检测单的“无异议”签名是否你本人签的？";


	private final static  String question18="你所驾驶的这辆车是不是用来营运的？";

	private final static  String question19="你之前是否有因饮酒后驾驶机动车被公安机关处罚过？";
	private final static  String question19_1="你是什么时候在哪里因饮酒后驾驶被处罚？";

	private final static  String question20="你有没有考取过驾驶证？";
	private final static  String question20_1="你的驾驶证什么时候在哪里考取，准驾车型是什么？";

	private final static  String question21="你开的是什么车，车牌号码多少？";
	private final static  String question22="车牌是怎么来的？";

	private final static  String question23="号牌字体、颜色、字体间距等参数均与《中华人民共和国机动车号牌》（G36-2014）不相符，认定你所使用的是伪造的机动车号牌，你有没有异议？";
	private final static  String question23_1="有什么异议？";

	private final static  String question24="你机动车有没有经过公安机关登记注册？";
	private final static  String question24_1="你登记的号牌是什么？";


    private final static  String question25="车主是谁的，和你什么关系？";
    private final static  String question26="你将当时的违法经过讲一下？";
    private final static  String question27="你还有其他交通违法行为吗？";
    private final static  String question27_1="什么交通违法行为？";

    private final static  String question28="在公安机关调查期间，是否有保障你必要的饮食情况及作息时间？";
    private final static  String question29="在调查期间有没有对你使用不文明行为和言语？";

    private final static  String question30="你还有什么需要补充？";
    private final static  String question30_1="你要补充什么？";

    private final static  String question31="你以上所讲是否属实？";

	private final static  String question54 = "回到上一个问题";

//	private final static  String manualntervention="结束人工干预";




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
        return "driveIndex";
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
			log.info("酒驾问答接口参数：\t"+paramMap);
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




		/*if(StringUtils.contains(question,question2)
			|| StringUtils.contains(question,question3)||StringUtils.contains(question,question4)
			|| StringUtils.contains(question,question5)||StringUtils.contains(question,question6)
			|| StringUtils.contains(question,question7)	){

			redisUtil.lSet(wholeQuestions,question);
//			System.out.println("******\t审讯笔录:\t"+redisUtil.lGet(wholeQuestions,0,limitRecord));
		}*/



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

			}





			redisUtil.lSet(wholeQuestions,question); // 笔录


		}else if(StringUtils.contains(question,question5_01 )){
        	// "question":"问：哪项信息不正确？答：手机号和户籍地址",
			// 	"text":["\n问：51_哪项信息不正确？\n 答：手机号和户籍地址"]
			question = StringUtils.replace(question,question5_01,"51_哪项信息不正确？");

			Map map = algorithmElementsOfCase(question);
			List<String> listError = (List) map.get("wahsterror");
			System.out.println(listError);

			List push = questionQueuem;
//			List push = new ArrayList();

			for(String key: listError){

				if(StringUtils.equals(key,"姓名")){
					push.add("question5_02");
				}else if(StringUtils.equals(key,"曾用名")){
					push.add("question5_03");
				}else if(StringUtils.equals(key,"性别")){
					push.add("question5_04");
				}else if(StringUtils.equals(key,"出生日期")){
					push.add("question5_05");
				}else if(StringUtils.equals(key,"身份证号码")){
					push.add("question5_06");
				}else if(StringUtils.equals(key,"民族")){
					push.add("question5_07");
				}else if(StringUtils.equals(key,"户籍地")){
					push.add("question5_10");
				}else if(StringUtils.equals(key,"居住地")){
					push.add("question5_11");
				}else if(StringUtils.equals(key,"工作单位")){
					push.add("question5_12");
				}else if(StringUtils.equals(key,"联系方式")){
					push.add("question5_13");
				}
				/*else if(StringUtils.equals(key,"文化程度")){
					push.add("question5_08");
				}else if(StringUtils.equals(key,"婚姻状况")){
					push.add("question5_09");
				}*/

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

			Map personInfo =this.algorithmPersonInfo(question);

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
				if(StringUtils.isNotBlank(variablex)){
					redisUtil.hset(reportSummaryx,"suspectBirthday", variablex);

				}

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

				List list1 = (List)personInfo.get("联系方式");
				String variablex = (String) list1.get(0);

				if( !StringUtils.contains(variablex,"号码格式错误")){
					redisUtil.hset(reportSummaryx,"suspectContact", variablex);

				}else{
					list.add("question5_13_1");
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

			Map map = this.algorithmPersonInfo(question);
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

			Map map = this.algorithmPersonInfo(question);
			String val = (String) map.get("是否人大代表或政协委员");

			if(StringUtils.isNotBlank(val)){
				redisUtil.hset(reportSummaryx,"rendadaibiao",val);
			}else{
				redisUtil.hset(reportSummaryx,"rendadaibiao","");

			}

			redisUtil.lSet(wholeQuestions,question); // 笔录

			nextQuestion=question7;

		}else if(StringUtils.contains(question, question6_2)) {
			Map map = this.algorithmPersonInfo(question);
			String val = (String) map.get("是否人大代表或政协委员");

			if(StringUtils.isNotBlank(val)){
				redisUtil.hset(reportSummaryx,"zhengxieweiyuan",val);
			}else{
				redisUtil.hset(reportSummaryx,"zhengxieweiyuan","");

			}

			redisUtil.lSet(wholeQuestions,question); // 笔录
			nextQuestion=question7;

		}else if(StringUtils.contains(question, question6_3)) {

			Map map = this.algorithmPersonInfo(question);
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

			String question8Temp = this.generateQuestion8(paramMap,reportSummaryx);

			redisUtil.hset(reportSummaryx,"governmentAndInstitutions","");


			Map map = this.algorithmPersonInfo(question);
			String work = (String) map.get("工作");

			if(StringUtils.isNotBlank(work)){
				if(StringUtils.equals(work,"否")){
					nextQuestion = question8Temp ;
				}else if(StringUtils.equals(work,"是")){
					nextQuestion = question7_1;
				}else{
					nextQuestion = question8Temp ;
					redisUtil.hset(reportSummaryx,"governmentAndInstitutions",work);

				}


			}

			redisUtil.lSet(wholeQuestions,question); // 笔录

		}else if(StringUtils.contains(question, question7_1)) {

			String question8Temp = this.generateQuestion8(paramMap,reportSummaryx);

			Map map = this.algorithmPersonInfo(question);
			String work = (String) map.get("工作");

			redisUtil.hset(reportSummaryx,"governmentAndInstitutions",work);

			redisUtil.lSet(wholeQuestions,question); // 笔录
			nextQuestion = question8Temp ;


		}else if(StringUtils.contains(question, question8)) {
            //  请确认这些信息是否正确？
            List push = new ArrayList();

            if(  StringUtils.contains(humanResponse, "不") ) {
                // 回答“不正确“等，则提问问题8.1 “哪项信息不正确？
                push.add("question8_01");

            }


            for (Map.Entry<String,Object> entry: paramMap.entrySet()){
                String key = entry.getKey();
                Object value =  entry.getValue();

                // "brotherInfo","childrenInfo" 兄弟姐妹和子女信息 是 数组，单独判断
                if(!StringUtils.containsAny(key,"brotherInfo","childrenInfo")){
                    if(StringUtils.isBlank((String)value) ){

                        if(StringUtils.equals(key,"fatherName")){
                            push.add("question8_02");
                        }

                        if(StringUtils.equals(key,"motherName")){
                            push.add("question8_03");
                        }

                        if(StringUtils.equals(key,"spouseRelation")){
                            push.add("question8_05");
                        }

                    }

                }else if(StringUtils.containsAny(key,"brotherInfo","childrenInfo")){

                    List info = (List) value;
                    if(info==null || info.isEmpty() ){
                        if(StringUtils.equals(key,"brotherInfo")){
                            push.add("question8_04");
                        }

                        if(StringUtils.equals(key,"childrenInfo")){
                            push.add("question8_06");
                        }



                    }

                }

            }



            if(push.isEmpty()){

                String question9Temp = this.generateQuestion9(paramMap,reportSummaryx);

                nextQuestion=question9Temp;

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

			Map map = algorithmElementsOfCase(question);
			List<String> familyErr = (List) map.get("wahsterror");

			List push = questionQueuem;

			for(String key: familyErr){

				if(StringUtils.equals(key,"父亲")){
					push.add("question8_02");
				}else if(StringUtils.equals(key,"母亲")){
					push.add("question8_03");
				}else if(StringUtils.equals(key,"姐姐")){
					push.add("question8_04");
				}else if(StringUtils.equals(key,"妹妹")){
					push.add("question8_04");
				}else if(StringUtils.equals(key,"哥哥")){
					push.add("question8_04");
				}else if(StringUtils.equals(key,"弟弟")){
					push.add("question8_04");
				}else if(StringUtils.equals(key,"妻子")){
					push.add("question8_05");
				}else if(StringUtils.equals(key,"丈夫")){
					push.add("question8_05");
				}else if(StringUtils.equals(key,"儿子")){
					push.add("question8_06");
				}else if(StringUtils.equals(key,"女儿")){
					push.add("question8_06");
				}

			}
			// 去重
			List unique = (List) push.stream().distinct().collect(Collectors.toList());


			if(unique.isEmpty()){
				String question9Temp = this.generateQuestion9(paramMap,reportSummaryx);
				nextQuestion=question9Temp;


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

            Map personInfo =this.algorithmPersonInfo(question);
            List<Map> family = (List)personInfo.get("家庭");
            Map<String,String> map1 = family.get(0);

            if(StringUtils.contains(question,question8_02)){

                if(family.size() == 1 && StringUtils.equals(map1.get("message"),"无")){
                    list.add("question8_02");
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
                    list.add("question8_03");
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
                    list.add("question8_05");
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
                    list.add("question8_04");
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
                    list.add("question8_06");
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

                String question9Temp = this.generateQuestion9(paramMap,reportSummaryx);
                nextQuestion=question9Temp;

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


			for (Map.Entry<String,Object> entry: paramMap.entrySet()){

				String key = entry.getKey();
				Object value = (Object) entry.getValue();

				// 排除掉子女和兄弟姐妹的信息
				if(!StringUtils.containsAny(key,"childrenInfo","brotherInfo") ){

					if(StringUtils.isBlank((String) value)){
						if(StringUtils.equals(key,"graduationDate") || StringUtils.equals(key,"graduationSchool")){
							push.add("question09_02");
						}else if(StringUtils.equals(key,"startWorkingTime") || StringUtils.equals(key,"startWorkingCompany")
								|| StringUtils.equals(key,"nowWorkingTime") || StringUtils.equals(key,"nowWorkingCompany")){
							push.add("question09_03");
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

			redisUtil.lSet(wholeQuestions,question); // 笔录

        }else if(StringUtils.contains(question, question09_01)) {

			question = StringUtils.replace(question,question09_01,"91_哪项信息不正确？");

			Map map = algorithmElementsOfCase(question);
			List<String> familyErr = (List) map.get("wahsterror");

			List push = questionQueuem;

			for(String key: familyErr) {

				if (StringUtils.equals(key, "工作")) {
					push.add("question09_03");
				}else if (StringUtils.equals(key, "教育经历")) {
					push.add("question09_02");
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

			Map personInfo =this.algorithmPersonInfo(question);

			if(StringUtils.contains(question,question09_02)){

				Map data= (Map) personInfo.get("school");

				String date = (String) data.get("date");
				String school = (String) data.get("school");


				if(StringUtils.isBlank(date) && StringUtils.isBlank(school)){
					list.add("question09_02");
				}else {

					redisUtil.hset(reportSummaryx,"graduationDate",StringUtils.isBlank(date)?"":date);
					redisUtil.hset(reportSummaryx,"graduationSchool",StringUtils.isBlank(school)?"":school);

				}



			}else if(StringUtils.contains(question,question09_03)){

				Map<String,String> data= (Map) personInfo.get("工作经历");

				if(data.isEmpty()){
					list.add("question09_03");
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
			Map result =this.algorithmElementsOfCase(question);
			Map family_phone = (Map) result.get("family_phone");
			String phone = (String) family_phone.get("phone");
			redisUtil.hset(reportSummaryx,"familyTelephone",StringUtils.isBlank(phone)?"":phone);


			/*if(StringUtils.containsAny(humanResponse, "不","没")
					|| StringUtils.isBlank(phone)){

				int countquestion10 = 0;
				boolean bool =  redisUtil.hHasKey(countRedis,"question10");

				if(!bool){
					redisUtil.hset(countRedis,"question10",1);
				}else{
					countquestion10 = (int) redisUtil.hget(countRedis,"question10");
					redisUtil.hset(countRedis,"question10",++countquestion10);

				}

				System.out.println("不清楚/不回复次数：question10\t"+redisUtil.hget(countRedis,"question10"));


				if((int)redisUtil.hget(countRedis,"question10") > 1){

					// 重复两遍还是不清楚/不回复，则提示人工干预。 人工干预以后，回到下一个问题
					nextQuestion=question11;
					intervention = "1";
					// 复位
					redisUtil.hset(countRedis,"question10",0);

				}else{
					nextQuestion="号码格式错误！"+question10;

				}


			}else{
				nextQuestion=question11;

			}*/


			nextQuestion=question11;

			redisUtil.lSet(wholeQuestions,question); // 记录笔录

		}else if(StringUtils.contains(question, question11)) {
			// 前科
			String criminalRecord="";

			Map result =this.algorithmElementsOfCase(question);
			List list = (List) result.get("criminalpunishment");

			if(list==null || list.isEmpty()){
				criminalRecord="";
			}else{
				Map map = (Map) list.get(0);
				criminalRecord= (String) map.get("罪名");
			}


			redisUtil.hset(reportSummaryx,"criminalRecord",StringUtils.isBlank(criminalRecord)?"":criminalRecord);


  			if(StringUtils.containsAny(humanResponse,"没","否")
					|| StringUtils.isNotBlank(criminalRecord)){

  				nextQuestion=question12;

			}else {
				nextQuestion=question11_1;

			}


			redisUtil.lSet(wholeQuestions,question); // 记录笔录

		}else if(StringUtils.contains(question,question11_1)){
			// 前科
			String criminalRecord="";

			Map result =this.algorithmElementsOfCase(question);
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

            Map map = algorithmElementsOfCase(question);
            List<String> illegalActivities = (List) map.get("casereason");
            //  [ "酒驾","未取得驾驶证驾驶","驾驶与准驾车型不符合的车辆","使用伪造牌证"]
//            List<String> illegalActivities = Arrays.asList(new String[]{"酒驾","未取得驾驶证驾驶","驾驶与准驾车型不符合的车辆","使用伪造牌证"});

            if(!illegalActivities.isEmpty()){
                boolean drunkDriving = false ; // 酒驾
                boolean withoutLicense = false ; // 未取得驾驶证驾驶
                boolean notConformToBeApproved= false ; // 驾驶与准驾车型不符合的车辆
                boolean forgedCard = false ; // 使用伪造牌证

                for(String activity: illegalActivities){
                    if(StringUtils.isNotBlank(activity)){
                        if(StringUtils.equals(activity,"酒驾")){
                            drunkDriving = true;
                        }else if(StringUtils.equals(activity,"未取得驾驶证驾驶")){
                            withoutLicense = true;
                        }else if(StringUtils.equals(activity,"驾驶与准驾车型不符合的车辆")){
                            notConformToBeApproved = true;
                        }else if(StringUtils.equals(activity,"使用伪造牌证")){
                            forgedCard = true;
                        }

                    }
                }


                log.info("\t酒驾\t"+drunkDriving+"\t未取得驾驶证驾驶\t"+withoutLicense
                        +"\t驾驶与准驾车型不符合的车辆\t"+notConformToBeApproved+"\t使用伪造牌证\t"+forgedCard);


                if(  (drunkDriving && !withoutLicense && !notConformToBeApproved && !forgedCard  )
                    || (drunkDriving && withoutLicense && !notConformToBeApproved && !forgedCard )
                    ||(drunkDriving && notConformToBeApproved  &&  !forgedCard && !withoutLicense )
                    ||(drunkDriving && withoutLicense && notConformToBeApproved && !forgedCard) ){

                    log.info("\t 提问问题17-20.1：");
                        /*   提问问题17-20.1：
                            酒驾
                            酒驾、未取得驾驶证驾驶
                            酒驾、驾驶与准驾车型不符合的车辆
                            酒驾、未取得驾驶证驾驶、驾驶与准驾车型不符合的车辆  */

                    redisUtil.hset(reportSummaryx,"trafficQuestinosRange","17-20.1");
                    nextQuestion=question17 ;


                }else if( ( withoutLicense && !notConformToBeApproved && !forgedCard && !drunkDriving)
                        || ( withoutLicense && notConformToBeApproved && !forgedCard && !drunkDriving)){

                    log.info("\t 提问问题20-20.1：");
                        /*     提问问题20-20.1：
                        未取得驾驶证驾驶
                        未取得驾驶证驾驶、驾驶与准驾车型不符合的车辆*/

                    redisUtil.hset(reportSummaryx,"trafficQuestinosRange","20-20.1");
                    nextQuestion=question20 ;




                }else if(notConformToBeApproved && !forgedCard && !drunkDriving && !withoutLicense){
                    log.info("\t 提问问题20.1:");

                   /* 提问问题20.1:
                        驾驶与准驾车型不符合的车辆  */


                    redisUtil.hset(reportSummaryx,"trafficQuestinosRange","20.1");
                    nextQuestion=question20_1 ;



                }else if( (withoutLicense && forgedCard && !drunkDriving && !notConformToBeApproved)
                        ||(withoutLicense && notConformToBeApproved && forgedCard  && !drunkDriving)){

                    log.info("\t 提问问题20-24:");
                   /* 提问问题20-24:
                        未取得驾驶证驾驶、使用伪造牌证
                        未取得驾驶证驾驶、驾驶与准驾车型不符合的车辆、使用伪造牌证*/

                    redisUtil.hset(reportSummaryx,"trafficQuestinosRange","20-24");
                    nextQuestion=question20 ;


                }else if(notConformToBeApproved && forgedCard && !drunkDriving && !withoutLicense){
                    log.info("\t 提问问题20.1-24:");
                   /* 提问问题20.1-24:
                        驾驶与准驾车型不符合的车辆、使用伪造牌证*/

                    redisUtil.hset(reportSummaryx,"trafficQuestinosRange","20.1-24");
                    nextQuestion=question20_1 ;



                }else if(forgedCard && !drunkDriving && !withoutLicense && !notConformToBeApproved){
                    log.info("\t 提问问题21-24:");

                   /* 提问问题21-24:
                        使用伪造牌证*/

                    redisUtil.hset(reportSummaryx,"trafficQuestinosRange","21-24");
                    nextQuestion=question21 ;


                }else if((drunkDriving && forgedCard && !withoutLicense && !notConformToBeApproved)
                        ||(drunkDriving && withoutLicense && forgedCard  && !notConformToBeApproved)
                        ||(drunkDriving && notConformToBeApproved && forgedCard && !withoutLicense)
                        ||(drunkDriving && withoutLicense && notConformToBeApproved && forgedCard)){
                    log.info("\t 提问问题17-24:");

                   /* 提问问题17-24:
                        酒驾、使用伪造牌证
                        酒驾、未取得驾驶证驾驶、使用伪造牌证
                        酒驾、驾驶与准驾车型不符合的车辆、使用伪造牌证
                        酒驾、未取得驾驶证驾驶、驾驶与准驾车型不符合的车辆、使用伪造牌证*/


                    redisUtil.hset(reportSummaryx,"trafficQuestinosRange","17-24");
                    nextQuestion=question17 ;

                }


            }else{

                int countquestion16 = 0;
                boolean bool =  redisUtil.hHasKey(countRedis,"question16");

                if(!bool){
                    redisUtil.hset(countRedis,"question16",1);
                }else{
                    countquestion16 = (int) redisUtil.hget(countRedis,"question16");
                    redisUtil.hset(countRedis,"question16",++countquestion16);

                }

                System.out.println("不清楚/不回复次数：question16\t"+redisUtil.hget(countRedis,"question16"));


                if((int)redisUtil.hget(countRedis,"question16") > 1){

                    // 重复两遍还是不清楚/不回复，则提示人工干预。 人工干预以后，回到本问题
                    nextQuestion=question16;
                    intervention = "1";
                    // 复位
                    redisUtil.hset(countRedis,"question16",0);

                }else{
                    nextQuestion=question16;

                }




            }

            // 酒驾 提问问题范围
            String trafficQuestinosRange = (String) redisUtil.hget(reportSummaryx,"trafficQuestinosRange");
            log.info("\t 酒驾 提问问题范围： \t"+trafficQuestinosRange);


		}else if(StringUtils.contains(question,question17)){
            redisUtil.lSet(wholeQuestions,question); // 记录笔录

            if(StringUtils.contains(humanResponse,"没")){
                nextQuestion=question17_2;

            }else{
                nextQuestion=question17_1;

            }

        }else if(StringUtils.contains(question,question17_1)){
            redisUtil.lSet(wholeQuestions,question); // 记录笔录

            Map map = algorithmElementsOfCase(question);
            String temp = (String) map.get("alcoholdetect");

            // 呼气酒精含量
            int expiratoryAlcoholContent = Integer.valueOf(StringUtils.isBlank(temp)?"1":temp);

            if(expiratoryAlcoholContent >= 80){
                nextQuestion = question17_1_1 ;
            }else if(expiratoryAlcoholContent>=20 && expiratoryAlcoholContent<80){
                nextQuestion = question17_1_2 ;
            }else{
                nextQuestion = question18 ;
                intervention = "1";
            }


        }else if(StringUtils.contains(question,question17_1_1)){
            redisUtil.lSet(wholeQuestions,question); // 记录笔录

            if(StringUtils.containsAny(humanResponse,"不","不知道")){
                intervention = "1";
                nextQuestion = question18;

            }else{
                nextQuestion = question18 ;

            }

        }else if(StringUtils.contains(question,question17_1_2)){
            redisUtil.lSet(wholeQuestions,question); // 记录笔录

            if(StringUtils.containsAny(humanResponse,"没有","没")){
                nextQuestion = question18 ;
            }else {
                nextQuestion = question17_1_2_1 ;

            }


        }else if(StringUtils.contains(question,question17_1_2_1)){
            redisUtil.lSet(wholeQuestions,question); // 记录笔录

            if(StringUtils.containsAny(humanResponse,"没有","没")){
                nextQuestion = question17_1_2_1_1 ;
            }else {
                nextQuestion = question17_1_2_1_2 ;


            }

        }else if(StringUtils.contains(question,question17_1_2_1_1)){
            redisUtil.lSet(wholeQuestions,question); // 记录笔录

            if(StringUtils.containsAny(humanResponse,"不","没") ){
                intervention = "1";
                nextQuestion =  question18 ;

            }else{
                nextQuestion =  question18 ;
            }


        }else if(StringUtils.contains(question,question17_1_2_1_2)){
            redisUtil.lSet(wholeQuestions,question); // 记录笔录

            if(StringUtils.containsAny(humanResponse,"不","不知道")){

                intervention = "1";
                nextQuestion =  question18 ;
            }else{
                nextQuestion =  question18 ;

            }

        }else if(StringUtils.contains(question,question17_2)){
            redisUtil.lSet(wholeQuestions,question); // 记录笔录

            if(StringUtils.containsAny(humanResponse,"没有","没")){
               nextQuestion = question17_2_1_1 ;

            }else{
                nextQuestion =  question17_2_1 ;

            }

        }else if(StringUtils.contains(question,question18)){
            redisUtil.lSet(wholeQuestions,question); // 记录笔录

            redisUtil.hset(reportSummaryx,"question18Answer",humanResponse);

            nextQuestion = question19;

        }else if(StringUtils.contains(question,question19)){
            redisUtil.lSet(wholeQuestions,question); // 记录笔录

            String question18Answer = (String) redisUtil.hget(reportSummaryx,"question18Answer");

            if(StringUtils.containsAny(humanResponse,"不","没")){
                //  如回答 问题18 和 问题19 同时回答“否”的情况，提示人工干预
                if(StringUtils.containsAny(question18Answer,"不","没")){
                    intervention = "1";
                }

                nextQuestion = question20;

            }else{
                // 如回答“有”等，跳转到问题19.1
                nextQuestion = question19_1 ;

            }


        }else if(StringUtils.contains(question,question19_1)){
            redisUtil.lSet(wholeQuestions,question); // 记录笔录

            nextQuestion = question20;
        }else if(StringUtils.contains(question,question20)){
            redisUtil.lSet(wholeQuestions,question); // 记录笔录


			if(StringUtils.containsAny(humanResponse,"没","没有")){

				String trafficQuestinosRange = (String)redisUtil.hget(reportSummaryx,"trafficQuestinosRange");

				if(StringUtils.endsWith(trafficQuestinosRange,"20.1") ){
					// 如果没有包含 ”使用伪造牌证“，直接跳转到 问题25
					nextQuestion = question25 ;

				}else if(StringUtils.endsWith(trafficQuestinosRange,"24") ){
					// 如果包含 ”使用伪造牌证“，跳转到 问题21
					nextQuestion = question21 ;

				}



            }else{

                nextQuestion = question20_1 ;

            }


        }else if(StringUtils.contains(question,question20_1)){
            redisUtil.lSet(wholeQuestions,question); // 记录笔录

            if(StringUtils.contains(humanResponse,"不")){
                intervention ="1";
            }

			String trafficQuestinosRange = (String)redisUtil.hget(reportSummaryx,"trafficQuestinosRange");

			if(StringUtils.endsWith(trafficQuestinosRange,"20.1") ){
				// 如果没有包含 ”使用伪造牌证“，直接跳转到 问题25
				nextQuestion = question25 ;

			}else{
				// 如果包含 ”使用伪造牌证“，跳转到 问题21
				nextQuestion = question21 ;

			}


        }else if(StringUtils.contains(question,question21)){
            redisUtil.lSet(wholeQuestions,question); // 记录笔录

            if(StringUtils.contains(humanResponse,"不")){
                intervention ="1";
            }

            nextQuestion = question22 ;

        }else if(StringUtils.contains(question,question22)){
            redisUtil.lSet(wholeQuestions,question); // 记录笔录

            nextQuestion = this.generateQuestion23(paramMap) ;

        }else if(StringUtils.contains(question,question23)){
            redisUtil.lSet(wholeQuestions,question); // 记录笔录

            if(StringUtils.contains(humanResponse,"没")){
                // 没有异议
                nextQuestion = question24 ;
            }else{
                // 有异议
                nextQuestion = question23_1 ;
            }


        }else if(StringUtils.contains(question,question23_1)){
            redisUtil.lSet(wholeQuestions,question); // 记录笔录

            nextQuestion = question24 ;

        }else if(StringUtils.contains(question,question24)){
            redisUtil.lSet(wholeQuestions,question); // 记录笔录

            if(StringUtils.contains(humanResponse,"没")){
                nextQuestion = question25 ;
            }else{

                nextQuestion = question24_1 ;

            }


        }else if(StringUtils.contains(question,question24_1)){
            redisUtil.lSet(wholeQuestions,question); // 记录笔录

            nextQuestion = question25 ;


        }else if(StringUtils.contains(question,question25)){
            redisUtil.lSet(wholeQuestions,question); // 记录笔录

            nextQuestion=question26;
		}else if(StringUtils.contains(question,question26)){
			redisUtil.lSet(wholeQuestions,question); // 记录笔录

            nextQuestion=question27;
		}else if(StringUtils.contains(question,question27)){
			redisUtil.lSet(wholeQuestions,question); // 记录笔录

            // 其他交通违法行为
            Map map = algorithmElementsOfCase(question);
            List<String> illegalActivities = (List) map.get("casereason");

            if(StringUtils.containsAny(humanResponse,"没","无")
                    || !illegalActivities.isEmpty()){

                nextQuestion = question28 ;

            }else {

                nextQuestion = question27_1 ;
            }


		}else if(StringUtils.contains(question,question27_1)) {
            redisUtil.lSet(wholeQuestions,question); // 记录笔录

//            Map map = algorithmElementsOfCase(question);
//            List<String> illegalActivities = (List) map.get("casereason");
//
//            // 调用算法接口 抽取 ”其他交通违法行为“
//            String otherTrafficViolations="无证驾驶";
//
//            redisUtil.hset(reportSummaryx,"otherTrafficViolations",StringUtils.isBlank(otherTrafficViolations)?"":otherTrafficViolations);

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

            String supplementaryContent = "补充内容";

            if(StringUtils.containsAny(humanResponse,"没","无")
                    || StringUtils.isNotBlank(supplementaryContent)){
                nextQuestion = question31 ;

            }else{
                intervention="1"; // 人工干预
                nextQuestion = question30 ;

            }

        }else if(StringUtils.contains(question,question31)) {
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

	/**
	 *  由 请求参数 生成 问题8（ 家庭状况 信息）,并将其值存入 redis
	 * */
	public String generateQuestion8(Map<String,Object> paramMap,String reportSummaryx){

		// 父亲
		StringBuilder familyInfo = new StringBuilder("");

		String fatherName = (String)paramMap.get("fatherName");
		String fatherAge = (String)paramMap.get("fatherAge");
		String fatherJob = (String)paramMap.get("fatherJob");

		String motherName = (String)paramMap.get("motherName");
		String motherAge = (String)paramMap.get("motherAge");
		String motherJob = (String)paramMap.get("motherJob");

		String spouseRelation = (String)paramMap.get("spouseRelation");
		String spouseName = (String)paramMap.get("spouseName");
		String spouseAge = (String)paramMap.get("spouseAge");
		String spouseJob = (String)paramMap.get("spouseJob");

		redisUtil.hset(reportSummaryx,"fatherName",StringUtils.isBlank(fatherName)?"":fatherName);
		redisUtil.hset(reportSummaryx,"fatherAge",StringUtils.isBlank(fatherAge)?"":fatherAge);
		redisUtil.hset(reportSummaryx,"fatherJob",StringUtils.isBlank(fatherJob)?"":fatherJob);

		redisUtil.hset(reportSummaryx,"motherName",StringUtils.isBlank(motherName)?"":motherName);
		redisUtil.hset(reportSummaryx,"motherAge",StringUtils.isBlank(motherAge)?"":motherAge);
		redisUtil.hset(reportSummaryx,"motherJob",StringUtils.isBlank(motherJob)?"":motherJob);

		redisUtil.hset(reportSummaryx,"spouseRelation",spouseRelation);
		redisUtil.hset(reportSummaryx,"spouseName",spouseName);
		redisUtil.hset(reportSummaryx,"spouseAge",spouseAge);
		redisUtil.hset(reportSummaryx,"spouseJob",spouseJob);


		if(StringUtils.isNotBlank(fatherName)){

			familyInfo.append("你的父亲名字是").append(fatherName).append("，");

			if(StringUtils.isNotBlank(fatherAge)){
				familyInfo.append(fatherAge).append("岁，");
			}

			if(StringUtils.isNotBlank(fatherJob)){
				familyInfo.append("在").append(fatherJob).append("工作；");
			}
		}

		// 母亲
		if(StringUtils.isNotBlank(motherName)){
			familyInfo.append("你的母亲名字是").append(motherName).append("，");
			if(StringUtils.isNotBlank(motherAge)){
				familyInfo.append(motherAge).append("岁，");
			}

			if(StringUtils.isNotBlank(motherJob)){
				familyInfo.append("在").append(motherJob).append("工作；");
			}
		}

		//  兄弟姐妹
		List<Map> brotherInfo = (List) paramMap.get("brotherInfo");

		redisUtil.hset(reportSummaryx,"brotherInfo",brotherInfo);

		if(brotherInfo!=null && brotherInfo.size()>0){
			// 有一个哥哥名字是XXX，XXX岁，在XXX工作；
			for(Map<String,String> brother: brotherInfo){
				if(StringUtils.isNotBlank(brother.get("brotherName"))){
					familyInfo.append(brother.get("brotherRelation")).append("名字是").append(brother.get("brotherName")).append("，");
					if(StringUtils.isNotBlank(brother.get("brotherAge"))){
						familyInfo.append(brother.get("brotherAge")).append("岁，");
					}

					if(StringUtils.isNotBlank(brother.get("brotherJob"))){
						familyInfo.append("在").append(brother.get("brotherJob")).append("工作；");
					}

				}
			}
		}

		// 配偶情况
		if(StringUtils.isNotBlank(spouseRelation)){
			familyInfo.append("你的").append(spouseRelation).append("名字是").append(spouseName).append("，");
			if(StringUtils.isNotBlank(spouseAge)){
				familyInfo.append(spouseAge).append("岁，");
			}
			if(StringUtils.isNotBlank(spouseJob)){
				familyInfo.append("在").append(spouseJob).append("工作；");
			}

		}

		// 子女情况
		List<Map> childrenInfo = (List) paramMap.get("childrenInfo");

		redisUtil.hset(reportSummaryx,"childrenInfo",childrenInfo);

		if(childrenInfo!=null && childrenInfo.size()>0){
			for(Map<String,String> child: childrenInfo){
				if(StringUtils.isNotBlank((String)child.get("childrenRelation"))){
					familyInfo.append("有一个").append((String)child.get("childrenRelation")).append("，");
				}

				if(StringUtils.isNotBlank((String)child.get("childrenName"))){
					familyInfo.append("名字是").append((String)child.get("childrenName")).append("，");
				}

				if(StringUtils.isNotBlank((String)child.get("childrenAge"))){
					familyInfo.append((String)child.get("childrenAge")).append("岁。");
				}

			}
		}


		familyInfo.append(question8);


		return  familyInfo.toString();

	}


    /**
     * 经核查你机动车被查获时所悬挂的*****号牌字体、颜色、字体间距等参数均与《中华人民共和国机动车号牌》（G36-2014）不相符，
     * 认定你所使用的是伪造的机动车号牌，你有没有异议？
     *
     *  由 请求参数（车牌） 生成 问题23
     * */
    public String generateQuestion23(Map<String,Object> paramMap){

        String vehicleNumber = (String) paramMap.get("vehicleNumber");

        StringBuilder question23Temp = new StringBuilder("经核查你机动车被查获时所悬挂的");
        question23Temp.append(vehicleNumber);
        question23Temp.append(question23);

        return question23Temp.toString();


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

		if(StringUtils.isNotBlank(vehicleNumber) ){
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

       /* "graduationDate":"2016年7月10日","graduationSchool":"四川大学",
                "startWorkingTime":"2017年7月10日","startWorkingCompany":"广州日报馆",
                "nowWorkingTime":"2018年5月10日","nowWorkingCompany":"体坛周报"*/

        String graduationDate = (String) paramMap.get("graduationDate");
        String graduationSchool = (String) paramMap.get("graduationSchool");
        String startWorkingTime = (String) paramMap.get("startWorkingTime");
        String startWorkingCompany = (String) paramMap.get("startWorkingCompany");
        String nowWorkingTime = (String) paramMap.get("nowWorkingTime");
        String nowWorkingCompany = (String) paramMap.get("nowWorkingCompany");

        redisUtil.hset(reportSummaryx,"graduationDate",StringUtils.isBlank(graduationDate)?"":graduationDate);
        redisUtil.hset(reportSummaryx,"graduationSchool",StringUtils.isBlank(graduationSchool)?"":graduationSchool);
        redisUtil.hset(reportSummaryx,"startWorkingTime",StringUtils.isBlank(startWorkingTime)?"":startWorkingTime);
        redisUtil.hset(reportSummaryx,"startWorkingCompany",StringUtils.isBlank(startWorkingCompany)?"":startWorkingCompany);
        redisUtil.hset(reportSummaryx,"nowWorkingTime",StringUtils.isBlank(nowWorkingTime)?"":nowWorkingTime);
        redisUtil.hset(reportSummaryx,"nowWorkingCompany",StringUtils.isBlank(nowWorkingCompany)?"":nowWorkingCompany);

        if(StringUtils.isNotBlank(graduationDate) ){
                question9Temp.append("你是").append(graduationDate);
                if(StringUtils.isNotBlank(graduationSchool)){
                    question9Temp.append("在").append(graduationSchool).append("毕业，");
                }else{
                    question9Temp.append("毕业，");
                }
        }


        if(StringUtils.isNotBlank(startWorkingTime)){
            question9Temp.append("从").append(startWorkingTime).append("时候开始");
            if(StringUtils.isNotBlank(startWorkingCompany)){
                question9Temp.append("在").append(startWorkingCompany).append("工作，");
            }else{
                question9Temp.append("工作，");
            }

        }


        if(StringUtils.isNotBlank(nowWorkingTime)){
            question9Temp.append(nowWorkingTime).append("开始");
            if(StringUtils.isNotBlank(nowWorkingCompany)){
                question9Temp.append("在").append(nowWorkingCompany).append("工作到现在。");
            }else{
                question9Temp.append("工作到现在。");

            }

        }

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
			question5Temp.append("无曾用名，");
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
			question5Temp.append(suspectNation).append("，");
		}

		if(StringUtils.isNotBlank(suspectEducation)){
			question5Temp.append(suspectEducation).append("，");
		}

		if(StringUtils.isNotBlank(suspectMarriage)){
			question5Temp.append(suspectMarriage).append("，");
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


		String trafficQuestinosRange ="17-24";
		System.out.println(StringUtils.endsWith(trafficQuestinosRange,"24") );

//		String str="没";
//		String str2="不是";

//		System.out.println(StringUtils.containsAny(str2,"是","是的"));
//		System.out.println(StringUtils.equalsAny(str2,"是","是的"));
//		System.out.println(StringUtils.containsAny(str2,"是","是的"));

	/*	List push  = new ArrayList();
		push.add("aaa");
		push.add("aaa");
		push.add("bbb");
		push.add("ccc");

		List unique = (List) push.stream().distinct().collect(Collectors.toList());
		System.out.println(unique);*/

//		String question="问：身份证号码格式有误，请重新回复\n" +
//				"答：34012，319840。929，0395";
//		String humanResponse = "34012，319840。929，0395";
//		String response = StringUtils.replace(humanResponse,"，","");
//		response = StringUtils.replace(response,"。","");
//		System.out.println(response);
//
//		question = StringUtils.replace(question,humanResponse,response);
//		System.out.println(question);



//        String str="childrenInfo";
//        System.out.println(StringUtils.containsAny(str,"brotherInfo","childrenInfo") );


//		Map map = new HashMap();
//		map.put("aaa","aaa");
//		map.put("bbb","bbb");
//		System.out.println(map.get("aaa")+"\t"+map.get("ccc"));

//		String question90="你今天因何事主动前来佛山市顺德区公安局";

//		String str = StringUtils.join(question90,"交通警察大队机动中队");
//		String str = StringUtils.substring(question90,0,19);
//		System.out.println(str);
//		String source="问：身份证号码格式有误，请重新回复 答：340123198409290395" ;
//		String str = StringUtils.substringBetween(source, "问：","答");
//		str = StringUtils.replace(source,str,"身份证号码是什么？");
//
//		System.out.println(str);






	}





}
