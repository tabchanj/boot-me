package hello.web;

import hello.config.crawl.CrawlConfigEnhance;
import hello.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloController.class);
    @Autowired
    private  UserService userService;

    @Autowired
    private CrawlConfigEnhance myConfig;

    @GetMapping("/hello")
    public Object sayHello(){
        return userService.findOne(2);
    }
}
