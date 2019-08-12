package com.pinyougou.manager.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {

    @Reference
    private BrandService brandService;

    @RequestMapping("/findAll")
    public List<TbBrand> findAll(){
        return brandService.findAll();
    }

    //分页查询品牌信息

    @RequestMapping("/findByPage")
    public PageResult findByPage(int page,int rows){
        return brandService.findByPage(page,rows);
    }

    @RequestMapping("/add")
    public Result add(@RequestBody TbBrand tbBrand){

        try {
            brandService.add(tbBrand);
            return new Result(true,"新增成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"新增失败");
        }

    }


    @RequestMapping("/update")
    public Result update(@RequestBody TbBrand tbBrand){

        try {
            brandService.update(tbBrand);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }

    }

    //通过id查询
    @RequestMapping("/findById")
    public TbBrand findById(long id){
        return brandService.findById(id);
    }

    //同通过id删除

    @RequestMapping("/delete")
    public Result delete(long[] ids){

        try {
            brandService.delete(ids);
            return new Result(true,"删除成功");

        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }


    //分页条件查询品牌信息

    @RequestMapping("/search")
    public PageResult serch(@RequestBody TbBrand tbBrand,int page,int rows){
        return brandService.findByPage(tbBrand,page,rows);
    }

    //查询品牌列表返回select2个数数据
    @RequestMapping("/selectOptionList")
    public List<Map>  selectOptionList(){
        return brandService.selectOptionList();
    }
}
