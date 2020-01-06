package com.dinfo.robotea.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** 机器人问答配置变量
 * @author zhangyiming
 *
 */
@Component
public class QuestionProperties {


/*  @Value("${robot.question.qt1}")
    private String qt1;

    @Value("${robot.question.qt2}")
    private String qt2;*/

    @Value("${spring.redis.expire}")
    private String expire;


    public String getExpire() {
        return expire;
    }

    public void setExpire(String expire) {
        this.expire = expire;
    }



}
