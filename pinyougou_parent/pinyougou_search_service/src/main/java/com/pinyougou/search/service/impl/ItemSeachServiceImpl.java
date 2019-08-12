package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSeachService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(timeout = 5000)
public class ItemSeachServiceImpl implements ItemSeachService {

    @Autowired
    private SolrTemplate  solrTemplate;

    @Override
    public Map itemSeach(Map searchMap) {

        String keuwords = (String) searchMap.get("keywords");
        searchMap.put("keywords",keuwords.replace(" ",""));

        Map map = new HashMap();


        //获取不带高亮的原声结果
       /* Query qurry = new SimpleQuery();
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        qurry.addCriteria(criteria);
        ScoredPage<TbItem> tbItems = solrTemplate.queryForPage(qurry, TbItem.class);

        map.put("rows",tbItems.getContent());*/
       //1、查询商品列表
       map.putAll(searItemList(searchMap));

       //2、商品分组查询的结果
        List<String> list = searchCategroyList(searchMap);
        map.put("categoryList",list);

        //3、将条件查询的结果的相关品牌和规格列表存入

        if ("".equals(searchMap.get("category"))){
            if (list.size()>0) {
                map.putAll(searchBrandAndSpec(list.get(0)));
            }
        }else {
                map.putAll(searchBrandAndSpec((String) searchMap.get("category")));
        }


        return map;
    }


    /*
    * 查询商品列表的方法，加上高亮显示
    * */
    private Map  searItemList(Map searchMap){
        Map map = new HashMap();


        //获取高亮结果的写法

        HighlightQuery query = new SimpleHighlightQuery();
        //设置高亮查询的选项
        HighlightOptions highlightOptions = new HighlightOptions();
        highlightOptions.addField("item_title");//设置高亮的域
        highlightOptions.setSimplePrefix("<em style='color:red'>");//设置前缀
        highlightOptions.setSimplePostfix("</em>");//设置后缀
        query.setHighlightOptions(highlightOptions);

        //1.1关键字条件查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //1.2按照分类列表进行过滤
        if (!"".equals(searchMap.get("category"))){
            FilterQuery filterQuevry = new SimpleFilterQuery();
            Criteria categoryCriteria = new Criteria("item_category").is(searchMap.get("category"));
            filterQuevry.addCriteria(categoryCriteria);
            query.addFilterQuery(filterQuevry);
        }

        //1.3按照品牌进行过滤
        if (!"".equals(searchMap.get("brand"))){
            FilterQuery filterQuevry = new SimpleFilterQuery();
            Criteria brandCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            filterQuevry.addCriteria(brandCriteria);
            query.addFilterQuery(filterQuevry);
        }

        //1.4对规格进行过滤
        if (searchMap.get("spec")!=null){

            Map<String,String> specMap = (Map<String, String>) searchMap.get("spec");

            for (String  key:specMap.keySet()){
                FilterQuery filterQuevry = new SimpleFilterQuery();
                Criteria specCriteria = new Criteria("item_spec_"+key).is(specMap.get(key));
                filterQuevry.addCriteria(specCriteria);
                query.addFilterQuery(filterQuevry);
            }

        }


        //1.5对价格进行过滤

        if(!"".equals(searchMap.get("price"))){

            String[] prices = ((String) searchMap.get("price")).split("-");

            if (!"0".equals(prices[0])){
                FilterQuery filterQuevry = new SimpleFilterQuery();
                Criteria specCriteria = new Criteria("item_price").greaterThanEqual(prices[0]);
                filterQuevry.addCriteria(specCriteria);
                query.addFilterQuery(filterQuevry);
            }

            if (!"*".equals(prices[1])){//有最大价格
                FilterQuery filterQuevry = new SimpleFilterQuery();
                Criteria specCriteria = new Criteria("item_price").lessThanEqual(prices[1]);
                filterQuevry.addCriteria(specCriteria);
                query.addFilterQuery(filterQuevry);
            }
        }


        //1.6分页查询
        Integer pageNum = (Integer) searchMap.get("pageNum");
        if (searchMap.get("pageNum")==null){
            pageNum=1;
        }
        Integer pageSize=(Integer)searchMap.get("pageSize");
        if (searchMap.get("pageSize")==null){
            pageSize=20;
        }
        query.setOffset((pageNum-1)*pageSize);//设置起始值

        query.setRows(pageSize);//设置每页记录数

        //1.7添加排序的方法
        String  sortField=(String)searchMap.get("sortField");
        String  sort = (String) searchMap.get("sort");//ASC,DESC

        if (sort!=null&&sort!=""){
            if ("ASC".equals(sort)){
                Sort searchSort = new Sort(Sort.Direction.ASC,"item_"+sortField);
                query.addSort(searchSort);
            }
            if ("DESC".equals(sort)){
                Sort searchSort = new Sort(Sort.Direction.DESC,"item_"+sortField);
                query.addSort(searchSort);
            }
        }





        /* ***********获取高亮结果集**************/
        HighlightPage<TbItem> tbItems = solrTemplate.queryForHighlightPage(query, TbItem.class);

        //获取到高亮显示的集合
        List<HighlightEntry<TbItem>> entryList = tbItems.getHighlighted();

        for (HighlightEntry<TbItem> entry : entryList) {
            TbItem tbItem = entry.getEntity();
            if (entry.getHighlights().size()>0&&entry.getHighlights().get(0).getSnipplets().size()>0){
                tbItem.setTitle(entry.getHighlights().get(0).getSnipplets().get(0));
            }

        }
        map.put("rows",tbItems.getContent());
        map.put("totalPages",tbItems.getTotalPages());
        map.put("total",tbItems.getTotalElements());

        return map;
    }


    /*
    * 分组查询的方法
    * */
    public List<String>  searchCategroyList(Map searchMap){

        List<String> list = new ArrayList<>();

        //简单的条件查询
        Query query = new SimpleQuery();
        //条件查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //加上分类查询
        GroupOptions groupOptions =new GroupOptions();
        //加上分类条件
        groupOptions.addGroupByField("item_category");
        query.setGroupOptions(groupOptions);


        //得到分组页
        GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage(query, TbItem.class);

        //此时直接用getcontent方法得到得是一个空值，需要调用它得另一个方法来得到分页得结果集
        GroupResult<TbItem> groupResult = groupPage.getGroupResult("item_category");
        //得到分页结果集的入口,是一个页面对象
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //得到分组结果的入口
        List<GroupEntry<TbItem>> entryList = groupEntries.getContent();
        for (GroupEntry<TbItem> entry : entryList) {
            list.add(entry.getGroupValue());
        }

        return list;

    }

    @Autowired
    private RedisTemplate redisTemplate;

    //3、查询的结果的相关品牌和规格列表

    public Map searchBrandAndSpec(String categoryName){

        Map map = new HashMap();

        //根据分类名查询模板id

       Long templateID = (Long) redisTemplate.boundHashOps("itemCat").get(categoryName);

       if (templateID != null){
           //根据模板id拆线呢品牌列表
           List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(templateID);
           map.put("brandList",brandList);

           //将规格列表存入
           List specList = (List) redisTemplate.boundHashOps("templateList").get(templateID);
           map.put("specList",specList);
       }

        return map;
    }

    //更新索引库的方法

    @Override
    public void importItem(List<TbItem> list) {

        solrTemplate.saveBeans(list);
        solrTemplate.commit();

    }

    @Override
    public void deleteById(List ids) {

        Query query = new SimpleQuery("*:*");
        Criteria criteria = new Criteria("item_goodsid").in(ids);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();

    }

}
