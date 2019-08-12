package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbBrand;
import entity.PageResult;


import java.util.List;
import java.util.Map;

public interface BrandService {

    public List<TbBrand> findAll();

    /*分页查询品牌信息
    * */
    public PageResult findByPage(int pageNum,int pageSize);

    //添加品牌

    void  add(TbBrand tbBrand);


    //根据id查询品牌
    TbBrand findById(long id);

    //修改品牌
    void  update(TbBrand tbBrand);

    //删除品牌
    void  delete(long[] ids);


    //条件查询
    public PageResult findByPage(TbBrand tbBrand,int pageNum,int pageSize);

    public List<Map> selectOptionList();

}
