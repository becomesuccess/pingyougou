package com.pinyougou.search.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.search.service.ItemSeachService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/itemSearchController")
public class ItemSearchController {

    @Reference
    private ItemSeachService itemSeachService;

    @RequestMapping("/itemSearch")
    public Map  itemSearch(@RequestBody Map searchMap){
        Map map = itemSeachService.itemSeach(searchMap);
        return   map;

    }

}
