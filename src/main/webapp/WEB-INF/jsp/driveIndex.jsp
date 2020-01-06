<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>  
<%@page import="org.springframework.context.ApplicationContext"%> 
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>酒驾、无证驾驶及伪造车牌号码审讯</title>

<meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no">
<meta name="format-detection" content="telephone=no">
<meta name="apple-mobile-web-app-capable" content="yes">
<meta name="apple-mobile-web-app-status-bar-style" content="black">

<link rel="stylesheet" href="/styles/style.css">

<link rel="stylesheet" type="text/css"  href="/jquery-easyui-1.8.2/themes/default/easyui.css">
<link rel="stylesheet" type="text/css" 	href="/jquery-easyui-1.8.2/themes/icon.css">
<link rel="stylesheet" type="text/css" 	href="/jquery-easyui-1.8.2/demo/demo.css">

<script src="/jquery-easyui-1.8.2/jquery.min.js" charset="utf-8"></script>
<script src="/jquery-easyui-1.8.2/jquery.easyui.min.js" charset="utf-8"></script>
<script type="text/javascript" src="/jquery-easyui-1.8.2/locale/easyui-lang-zh_CN.js"></script>
<script>

    function uuid() {
        var s = [];
        var hexDigits = "0123456789abcdef";
        for (var i = 0; i < 36; i++) {
            s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
        }
        s[14] = "4"; // bits 12-15 of the time_hi_and_version field to 0010
        s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1); // bits 6-7 of the clock_seq_hi_and_reserved to 01
        s[8] = s[13] = s[18] = s[23] = "-";

        var uuid = s.join("");

        uuid= uuid.substr(0,10);

        uuid= "aaaa";


        return uuid;
    }

    // 模拟 cookie
    var cookieUuid = uuid();


</script>



</head>
<body>
	
<%  
ServletContext context = request.getSession().getServletContext();  
ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);  
%>  

	   <!-- 案情模块 START  -->

    	<div class="easyui-layout" data-options="fit:true">

           <div region="north" split="true" title="审讯摘要" style="width:240px;height:150px">

                <!--   根据抽取要素填充至模板，如没有抽取出价值，则不显示“价值约为”  -->
                <input id="reportSummary" class="easyui-textbox" data-options="multiline:true"
                       style="width:1500px;height:110px">
            </div>


            <div region="west" split="true" title="审讯要素信息" style="width:400px;height:100px">
                <!-- 展示全部已问的问题及答案   -->
                <input id="reportElement" class="easyui-textbox" data-options="multiline:true"
                        style="width:400px;height:500px">
            </div>

           	<div region="center" split="true" title="审讯笔录内容"  style="width:840px;height:400px">
                  <!-- 展示全部已问的问题及答案   -->
                <input id="reportRecord" class="easyui-textbox" data-options="multiline:true"
                     style="width:1300px;height:500px">
           	</div>

        </div>

    <!-- 案情模块 END -->



<div class="dialogue-wrapper">

    <div id="btn_open" class="dialogue-support-btn" style="height:130px">
        <i class="dialogue-support-icon"></i>
        <i class="dialogue-support-line"></i> 
        <span class="dialogue-support-text">开始审讯</span>
    </div>
    <div class="dialogue-main">
        <div class="dialogue-header">
            <i id="btn_close" class="dialogue-close">></i>
            <div class="dialogue-service-info">
                <i class="dialogue-service-img">头像</i>
                <div class="dialogue-service-title">
                    <p class="dialogue-service-name">鼎富-伊雪松</p>
                    <p class="dialogue-service-detail">酒驾、无证驾驶及伪造车牌号码审讯</p>
                </div>
            </div>
        </div>
        <div id="dialogue_contain" class="dialogue-contain">
            <p class="dialogue-service-contain">
            <span class="dialogue-text dialogue-service-text">我们是佛山市顺德区公安局交通警察大队机动中队的民警，现就有关案情依法对你进行询问，你应当如实回答，故意作伪证或者隐匿证据会负相应的法律责任，对案件无关问题，你有拒绝回答的权利，你有要求办案人员或者公安机关负责人回避的权利，有陈述和申辩的权利，以上权利义务告知，你听清楚了吗？
            </span>
            </p>
            <!-- <p class="dialogue-customer-contain"><span class="dialogue-text dialogue-customer-text">我有个问题</span></p> -->
        </div>
        <div class="dialogue-submit">
            <p id="dialogue_hint" class="dialogue-hint"><span class="dialogue-hint-icon">!</span><span class="dialogue-hint-text">发送内容不能为空</span></p>
            <textarea id="dialogue_input" class="dialogue-input-text" placeholder="请输入您的问题，按Enter键提交（shift+Enter换行）"></textarea>
            <div class="dialogue-input-tools">

            </div>
                <a  href="javascript:void(0)" onclick="getServiceText('回到上一个问题')" class="easyui-linkbutton" plain="true" iconCls="icon-back">上一个问题</a>
        </div>
    </div>
</div>

<script>

        var reportRecord =""; //审讯笔录内容

        // 显示审讯对话框
         $('.dialogue-support-btn').css({'display': 'none'});
         $('.dialogue-main').css({'display': 'inline-block'});
         $('.dialogue-main').animate({'height': '600px','width':'600px'})

	var ask1="我们是广东省佛山市顺德区公安局民警，现就有关案情依法对你进行讯问并进行录音录像，你必须如实回答，不得隐瞒事实或者作假口供，不得诬告及陷害他人，否则要负相应的法律责任，对与本案无关的问题你有权拒绝回答的权利，你知道吗？" ;
	
	
    var doc = document;
    // 模拟一些后端传输数据
    var serviceData = {
        'robot': {
            'name': 'robot001',
            'dialogue': ['模拟回复1', '模拟回复2', '模拟回复3'],
            'welcome': '您好，robot001为您服务'
        }
    };

    var dialogueInput = doc.getElementById('dialogue_input'),
        dialogueContain = doc.getElementById('dialogue_contain'),
        dialogueHint = doc.getElementById('dialogue_hint'),
        btnOpen = doc.getElementById('btn_open'),
        btnClose = doc.getElementById('btn_close'),
        timer,
        timerId,
        shiftKeyOn = false;  // 辅助判断shift键是否按住

    // 点击"我要审讯"
    btnOpen.addEventListener('click', function(e) {
        $('.dialogue-support-btn').css({'display': 'none'});
        $('.dialogue-main').css({'display': 'inline-block'});
        $('.dialogue-main').animate({'height': '600px','width':'600px'})
    })

    btnClose.addEventListener('click', function(e) {
        $('.dialogue-main').animate({'height': '0'}, function() {
            $('.dialogue-main').css({'display': 'none'});
            $('.dialogue-support-btn').css({'display': 'inline-block'});
        });
    })

    dialogueInput.addEventListener('keydown', function(e) {
        var e = e || window.event;
        if (e.keyCode == 16) {
            shiftKeyOn = true;
        }
        if (shiftKeyOn) {
            return true;
        } else if (e.keyCode == 13 && dialogueInput.value == '') {
            // console.log('发送内容不能为空');
            // 多次触发只执行最后一次渐隐
            setTimeout(function() {
                fadeIn(dialogueHint);
                clearTimeout(timerId)
                timer = setTimeout(function() {
                    fadeOut(dialogueHint)
                }, 2000);
            }, 10);
            timerId = timer;
            return true;
        } else if (e.keyCode == 13) {
            var nodeP = doc.createElement('p'),
                nodeSpan = doc.createElement('span');
            nodeP.classList.add('dialogue-customer-contain');
            nodeSpan.classList.add('dialogue-text', 'dialogue-customer-text');
            nodeSpan.innerHTML = dialogueInput.value;
            nodeP.appendChild(nodeSpan);
            dialogueContain.appendChild(nodeP);
            dialogueContain.scrollTop = dialogueContain.scrollHeight;
            submitCustomerText(dialogueInput.value);
        }
    });

    dialogueInput.addEventListener('keyup', function(e) {
        var e = e || window.event;
        if (e.keyCode == 16) {
            shiftKeyOn = false;
            return true;
        }
        if (!shiftKeyOn && e.keyCode == 13) {
            dialogueInput.value = null;
        }
    });

    function submitCustomerText(text) {
        console.log(text)
        // code here 向后端发送text内容

        // 模拟后端回复
        /* var num = Math.random() * 10;
        console.log(num);
        if (num <= 7) {
            getServiceText(serviceData);
        } */
        
        getServiceText(text);
        
    }



    // 问答响应 
    function getServiceText(answer) {
       /*  var serviceText = data.robot.dialogue,
            i = Math.floor(Math.random() * serviceText.length);
            
        var ask = $("span.dialogue-text").text();    
        console.log(ask);     */
        
     	 //获取当前问题
        var currentQuestion = $("p.dialogue-service-contain").last().children().text();
        // console.log("currentQuestion:   "+currentQuestion);

        if(currentQuestion.indexOf("问：") !=-1){
            var splitFirst = currentQuestion.split('问：');
            currentQuestion = splitFirst[1];

        }
        
        var question="";
        if(answer=="回到上一个问题"){
        	question=answer;
        }else{
        	
	        question = "\n问："+currentQuestion+"\n答："+answer;
        }
        console.log("question: "+question);

        // reportRecord+=question;





        
        // 问答接口响应
        $.ajax({
            url: '${ctx}/drive/getAnswer',
            type: "POST",
            contentType: 'application/json',
            dataType: "json",
            data:JSON.stringify({
                   question: question,
                   cookieUuid: cookieUuid,

                   policeDept:"交通警察大队机动中队",
                   policePhone:"15665417412",

                   suspectName:"李明",
                   suspectUsedName:"王刚",
                   suspectGender:"男",
                   suspectBirthday:"1967年10月25日",
                   suspectAge:"77",
                   suspectIdCard:"340123198409290395",
                   suspectNation:"汉族",
                   suspectEducation:"初中",
                   suspectMarriage:"已婚",
                   suspectHouseholRegister:"广东省佛山市顺德区延年路11号",
                   suspectResidence:"广东省深圳市福田区侨城东路34号",
                   suspectWorkunit:"合肥市体育局",
                   suspectContact:"15010608983",

                   fatherName:"王明",
                   fatherAge:"60",
                   fatherJob:"合肥市水务局",

                   motherName:"黄梅",
                   motherAge:"55",
                   motherJob:"合肥市城市管理局",

                   brotherInfo:[
                        {brotherRelation:"哥哥",brotherName:"王老大",brotherAge:"31",brotherJob:"合肥市审计局"},
                        {brotherRelation:"弟弟",brotherName:"王老二",brotherAge:"27",brotherJob:"合肥市教育局"},
                        {brotherRelation:"姐姐",brotherName:"王三姐",brotherAge:"21",brotherJob:"合肥市民政局"} ],

                   spouseRelation:"妻子",
                   spouseName:"徐梦维",
                   spouseAge:"18",
                   spouseJob:"合肥市交通运输局",

                   childrenInfo:[ {childrenRelation:"儿子",childrenName:"王甲",childrenAge:"8"},
                          {childrenRelation:"女儿",childrenName:"王乙",childrenAge:"9"}	],

                   graduationDate:"2016年7月10日",
                   graduationSchool:"四川大学",
                   startWorkingTime:"2017年7月10日",
                   startWorkingCompany:"合肥市财务局",
                   nowWorkingTime:"2018年5月10日",
                   nowWorkingCompany:"合肥市商务局",

                   familyTelephone:"15665417488",

                   TimeOfCrime : "2019年11月13日20点35分",
                   happeningPlace : "大良镇清晖路150号",
                   vehicleNumber : "粤E11111",
                   vehicleType : "小型载客汽车",

                   // manualntervention:"开始人工干预"
                   manualntervention:""


             }),

            success: function (result) {

                console.log("cookieUuid: "+ cookieUuid );

            	var nextQ = result.nextQuestion;
            	var intervention = result.intervention;

                if ( nextQ && nextQ!="审讯完毕" ) {
                    if (nextQ !== null || nextQ !== undefined || nextQ !== '') {
                        console.log("下一个问题是： "+nextQ);
                        console.log("人工干预： "+intervention);

                        var serviceText = nextQ;
				       
                        var nodeP = doc.createElement('p'),
				        nodeSpan = doc.createElement('span');
				        nodeP.classList.add('dialogue-service-contain');
				        nodeSpan.classList.add('dialogue-text', 'dialogue-service-text');
				        nodeSpan.innerHTML = serviceText;
				        nodeP.appendChild(nodeSpan);
				        dialogueContain.appendChild(nodeP);
				        dialogueContain.scrollTop = dialogueContain.scrollHeight;

                        //  刷新 ”审讯笔录“模块  TODO
                        $.ajax({
                                url: '${ctx}/drive/getRecord',
                                type: "POST",
                                dataType: "json",
                                data: {
                                    cookieUuid: cookieUuid
                                },
                                success: function (data3) {
                                    var temp="";
                                    if (data3.length > 0) {
                                        $.each(data3, function (i, item) {
                                            temp+=item;
                                        });

                                    }

                                    $('#reportRecord').textbox("setValue",temp);



                                }
                         });





                        //  刷新 ”审讯要素“模块
                         $.ajax({
                                    url: '${ctx}/drive/getElement',
                                    type: "POST",
                                    dataType: "json",
                                    data: {
                                    	cookieUuid: cookieUuid
                                    },
                                    success: function (resultm) {

                                          // 人员信息
                                         var name = (resultm.name=='' || resultm.name==undefined || resultm.name==null)?"":resultm.name;
                                         var residence = (resultm.residence=='' || resultm.residence==undefined || resultm.residence==null)?"":resultm.residence;
                                         var education = (resultm.education=='' || resultm.education==undefined || resultm.education==null)?"":resultm.education;
                                         var contact = (resultm.contact=='' || resultm.contact==undefined || resultm.contact==null)?"":resultm.contact;
                                         var idCard = (resultm.idCard=='' || resultm.idCard==undefined || resultm.idCard==null)?"":resultm.idCard;
                                         var householRegister = (resultm.householRegister=='' || resultm.householRegister==undefined || resultm.householRegister==null)?"":resultm.householRegister;
                                         var gender = (resultm.gender=='' || resultm.gender==undefined || resultm.gender==null)?"":resultm.gender;
                                         var age = (resultm.age=='' || resultm.age==undefined || resultm.age==null)?"":resultm.age;
                                         var birthday = (resultm.birthday=='' || resultm.birthday==undefined || resultm.birthday==null)?"":resultm.birthday;
                                         var nation = (resultm.nation=='' || resultm.nation==undefined || resultm.nation==null)?"":resultm.nation;
                                         var workUnit = (resultm.workUnit=='' || resultm.workUnit==undefined || resultm.workUnit==null)?"":resultm.workUnit;



                                         // 案件要素
                                         var caseStart = (resultm.caseStart=='' || resultm.caseStart==undefined || resultm.caseStart==null)?"":resultm.caseStart;
                                         var caseEnd = (resultm.caseEnd=='' || resultm.caseEnd==undefined || resultm.caseEnd==null)?"":resultm.caseEnd;

                                         var address = (resultm.address=='' || resultm.address==undefined || resultm.address==null)?"":resultm.address;
                                         var periodtime = (resultm.periodtime=='' || resultm.periodtime==undefined || resultm.periodtime==null)?"":resultm.periodtime;

                                         var selfPrice = (resultm.selfPrice=='' || resultm.selfPrice==undefined || resultm.selfPrice==null)?"":resultm.selfPrice;
                                         var itemType = (resultm.itemType=='' || resultm.itemType==undefined || resultm.itemType==null)?"":resultm.itemType;
                                         var quantity = (resultm.quantity=='' || resultm.quantity==undefined || resultm.quantity==null)?"":resultm.quantity;
                                         var color = (resultm.color=='' || resultm.color==undefined || resultm.color==null)?"":resultm.color;
                                         var brand = (resultm.brand=='' || resultm.brand==undefined || resultm.brand==null)?"":resultm.brand;
                                         var nameOfGoods = (resultm.nameOfGoods=='' || resultm.nameOfGoods==undefined || resultm.nameOfGoods==null)?"":resultm.nameOfGoods;

                                         var moneysum = (resultm.moneysum=='' || resultm.moneysum==undefined || resultm.moneysum==null)?"":resultm.moneysum;
                                         var invoice = (resultm.invoice=='' || resultm.invoice==undefined || resultm.invoice==null)?"":resultm.invoice;

                                         var basecasereason = (resultm.basecasereason=='' || resultm.basecasereason==undefined || resultm.basecasereason==null)?"":resultm.basecasereason;

                                         var casereason = (resultm.casereason=='' || resultm.casereason==undefined || resultm.casereason==null)?"":resultm.casereason;

                                         var stolenPhone = ( resultm.stolenPhone==undefined || resultm.stolenPhone==null)?"":resultm.stolenPhone;

                                         var monitor = (resultm.monitor=='' || resultm.monitor==undefined || resultm.monitor==null)?"":resultm.monitor;

                                         var tempp="";
                                         tempp = "审讯人姓名：\t"+name
                                               +"\n居住地：\t"+residence
                                               +"\n民族：\t\t"+nation
                                               +"\n受教育程度：\t"+education
                                               +"\n联系方式：\t"+contact
                                               +"\n身份证号码：\t"+idCard
                                               +"\n户籍地：\t"+householRegister
                                               +"\n性别：\t\t"+gender
                                               +"\n年龄：\t\t"+age
                                               +"\n出生日期：\t"+birthday
                                               +"\n工作单位：\t"+workUnit



                                              +"\n案件类型：\t"+basecasereason
                                              +"\n具体类型：\t"+casereason

                                              +"\n案发时间区间：\t"+periodtime
                                              +"\n案发开始时间：\t"+caseStart
                                              +"\n案发结束时间：\t"+caseEnd
                                              +"\n案发地址：\t"+address

                                              +"\n物品类型：\t"+itemType
                                              +"\n物品数量：\t"+quantity
                                              +"\n自报价格：\t"+selfPrice
                                              +"\n物品颜色：\t"+color
                                              +"\n物品品牌：\t"+brand
                                              +"\n物品名称：\t"+nameOfGoods

                                              +"\n现价值：\t"+moneysum
                                              +"\n物品发票：\t"+invoice
                                              +"\n现场监控：\t"+monitor

                                              ;

                                         if(stolenPhone!=''){
                                            tempp+="\n被盗手机号码：\t"+stolenPhone ;

                                         }

                                         $('#reportElement').textbox("setValue",tempp );


                                    }
                          });


                    } else {
                        $.messager.alert('提示','没有数据！','error');

                    }
                } else {

                        //  审讯摘要更新
                        $.messager.alert('提示','问答完毕！','info');

                        var reportSummary = result.reportSummary;

                        // 填充 “审讯摘要”模块
                        var namex = (reportSummary.name=='' || reportSummary.name==undefined || reportSummary.name==null)?"":reportSummary.name;
                        var caseStart = (reportSummary.caseStart=='' || reportSummary.caseStart==undefined || reportSummary.caseStart==null)?"":reportSummary.caseStart;
                        var caseEnd = (reportSummary.caseEnd=='' || reportSummary.caseEnd==undefined || reportSummary.caseEnd==null)?"":reportSummary.caseEnd;
                        var address = (reportSummary.address=='' || reportSummary.address==undefined || reportSummary.address==null)?"":reportSummary.address;

                        var quantity = (reportSummary.quantity=='' || reportSummary.quantity==undefined || reportSummary.quantity==null)?"":reportSummary.quantity;

                        var color = (reportSummary.color=='' || reportSummary.color==undefined || reportSummary.color==null)?"":reportSummary.color;
                        var brand = (reportSummary.brand=='' || reportSummary.brand==undefined || reportSummary.brand==null)?"":reportSummary.brand;
                        var nameOfGoods = (reportSummary.nameOfGoods=='' || reportSummary.nameOfGoods==undefined || reportSummary.nameOfGoods==null)?"":reportSummary.nameOfGoods;

                        var temp = namex+"于"+caseStart+"至"+caseEnd+"，在"+address+"盗窃了"+quantity+""+color+""+brand+""+nameOfGoods ;

                        if(reportSummary.moneysum!='' && reportSummary.moneysum!=undefined || reportSummary.moneysum!=null){
                            temp+=",价值约为：";
                            temp+=reportSummary.moneysum ;

                        }

                        $('#reportSummary').textbox("setValue", temp);



                }
            }
        });
        
        
       

            
            
        
        
    }

    // 渐隐
    function fadeOut(obj) {
        var n = 100;
        var time = setInterval(function() {
            if (n > 0) {
                n -= 10;
                obj.style.opacity = '0.' + n;
            } else if (n <= 30) {
                obj.style.opacity = '0';
                clearInterval(time);
            }
        }, 10);
        return true;
    }

    // 渐显
    function fadeIn(obj) {
        var n = 30;
        var time = setInterval(function() {
            if (n < 90) {
                n += 10;
                obj.style.opacity = '0.' + n;
            } else if (n >= 80) {
                
                obj.style.opacity = '1';
                clearInterval(time);
            }
        }, 100);
        return true;
    }
</script>		
	
</body>
</html>