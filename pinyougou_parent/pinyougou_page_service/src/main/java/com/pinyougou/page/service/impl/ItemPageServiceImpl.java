package com.pinyougou.page.service.impl;

import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;


import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Value("${pagedir}")
    private String pageDir;

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public boolean getItemHtml(Long goodsId) {

        Configuration configuration = freeMarkerConfigurer.getConfiguration();

        try {
            Template template = configuration.getTemplate("item.ftl");

            //创建数据对象

            Map dataMap = new HashMap();
            //查询出基本信息
            TbGoods tbGood = goodsMapper.selectByPrimaryKey(goodsId);
            dataMap.put("goods",tbGood);
            //查询出详细信息
            TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            dataMap.put("goodsDesc",tbGoodsDesc);

            //查询出分类的信息
            String itemCat1 = itemCatMapper.selectByPrimaryKey(tbGood.getCategory1Id()).getName();
            String itemCat2 = itemCatMapper.selectByPrimaryKey(tbGood.getCategory2Id()).getName();
            String itemCat3 = itemCatMapper.selectByPrimaryKey(tbGood.getCategory3Id()).getName();
            dataMap.put("itemCat1",itemCat1);
            dataMap.put("itemCat2",itemCat2);
            dataMap.put("itemCat3",itemCat3);

            //将sku列表数据放入结果集中


            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andStatusEqualTo("1");
            criteria.andGoodsIdEqualTo(goodsId);
            //按照是否默认降序，将默认状态的商品放在第一个
            example.setOrderByClause("is_default desc");
            List<TbItem> tbItems = itemMapper.selectByExample(example);
            dataMap.put("itemList",tbItems);
            //创建一个写出对象
            Writer out =new FileWriter(pageDir+goodsId+".html");

            template.process(dataMap,out);

            //关流

            out.close();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delelteItemHtml(Long[] goodsIds) {

        try {
            for (Long goodsId : goodsIds) {
                    new File(pageDir+goodsId+".html").delete();
                }
                return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
