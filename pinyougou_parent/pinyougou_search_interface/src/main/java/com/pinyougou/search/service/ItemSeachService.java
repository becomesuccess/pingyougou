package com.pinyougou.search.service;

import com.pinyougou.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSeachService  {
    //商品搜索的方法
    public  Map  itemSeach(Map map);

    //跟新索引库的方法

    public void importItem(List<TbItem> list);

    //根据id删除索引库中的记录
    public void deleteById(List ids);

}
