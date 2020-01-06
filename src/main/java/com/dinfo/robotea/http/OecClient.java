package com.dinfo.robotea.http;

import java.util.HashMap;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**  案件分类 和 属性提取 接口客户端
 * @author zhangyiming
 *
 */
@Service
@FeignClient(name="algorithmUrl",url="${com.dinfo.algorithmUrl}")
public interface OecClient {
	
    @RequestMapping(method = RequestMethod.POST,consumes="application/json",produces="application/json")
    HashMap  getResult(@RequestBody String json);
	
	
    
}
