package com.dinfo.robotea.http;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;

/**  交通类算法抽取接口
 * @author zhangyiming
 *
 */
@Service
@FeignClient(name="trafficUrl",url="${com.dinfo.trafficUrl}")
public interface TrafficClient {
	
    @RequestMapping(method = RequestMethod.POST,consumes="application/json",produces="application/json")
    HashMap  getResult(@RequestBody String json);
	
	
    
}
