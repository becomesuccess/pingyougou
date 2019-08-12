package com.pinyougou.solrutil;


import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;


  public void ImportItemData(){

      TbItemExample example = new TbItemExample();
      TbItemExample.Criteria criteria = example.createCriteria();
      criteria.andStatusEqualTo("1");
      List<TbItem> tbItems = itemMapper.selectByExample(example);

      /*打印输出查询到的数据*/
      for (int i =0;i<tbItems.size();i++){
          System.out.println(tbItems.get(i).getTitle()+" "+tbItems.get(i).getBrand());
          String spec = tbItems.get(i).getSpec();
          Map map = JSON.parseObject(spec, Map.class);
          tbItems.get(i).setSpecMap(map);
      }

      solrTemplate.saveBeans(tbItems);
      solrTemplate.commit();

  }

    public static void main(String[] args) {

        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        SolrUtil solrUtil = (SolrUtil) applicationContext.getBean("solrUtil");
        solrUtil.ImportItemData();
    }

    @Test
    public void  deleteAll(){

        Query query = new SimpleQuery("*:*");
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

}
