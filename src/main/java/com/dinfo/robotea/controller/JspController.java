package com.dinfo.robotea.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *  Jsp页面转发器
 * */
@Controller
@RequestMapping("/jsp")
public class JspController {

    @RequestMapping("/xxx")
    public String getxxxIndex(){
        return "xxxIndex";
    }

}
