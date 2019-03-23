package org.jim.server.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/index")
public class IMIndexController {

    @RequestMapping()
    public String index(){
        return "index";
    }

}
