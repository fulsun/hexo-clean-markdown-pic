package pers.fulsun.cleanup.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    /**
     * 首页
     */
    @GetMapping(value = {"/", "/index"})
    public String index() {
        return "index";
    }
}
