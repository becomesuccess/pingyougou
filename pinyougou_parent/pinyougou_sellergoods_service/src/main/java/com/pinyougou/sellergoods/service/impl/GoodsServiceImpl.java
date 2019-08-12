package com.pinyougou.sellergoods.service.impl;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojogroup.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper  goodsDescMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Autowired
	private TbItemMapper tbItemMapper;

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private TbBrandMapper brandMapper;

	@Autowired
	private TbSellerMapper sellerMapper;

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		goods.getGoods().setAuditStatus("0");
		//插入商品信息
		goodsMapper.insert(goods.getGoods());

		//插入商品的扩展信息
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());//设置id，让商品信息的id和扩展信息的id匹配上
		goodsDescMapper.insert(goods.getGoodsDesc());

		//插入item信息
        saveItemList(goods);

	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){

	    //修改基本信息
		goodsMapper.updateByPrimaryKey(goods.getGoods());

		//修改详细描述
        goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());

        //先删除item表中的相关信息
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(goods.getGoods().getId());
        tbItemMapper.deleteByExample(example);

        //调取方法，存入item数据
        saveItemList(goods);
	}	

	//向item表中存入数据的方法

	private void saveItemList (Goods goods){
        if ("1".equals(goods.getGoods().getIsEnableSpec())){
            //插如雨item相关的数据。
            for (TbItem item:goods.getItemList()){

                //存入title

                String title =goods.getGoods().getGoodsName();

                String spec = item.getSpec();
                Map<String,Object> map = JSON.parseObject(spec, Map.class);
                for (String key:map.keySet()){
                    title += " " + map.get(key);
                }
                item.setTitle(title);
                //存入图片url
                String itemImages = goods.getGoodsDesc().getItemImages();
                List<Map> imageList = JSON.parseArray(itemImages, Map.class);
                if (imageList.size()>0){

                    item.setImage((String)imageList.get(0).get("url"));

                }

                //设置三级类id
                item.setCategoryid(goods.getGoods().getCategory3Id());

                //设置所属类目
                TbItemCat tbItemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
                item.setCategory(tbItemCat.getName());

                //创建时间
                item.setCreateTime(new Date());

                //更新时间
                item.setUpdateTime(new Date());

                //商品id
                item.setGoodsId(goods.getGoods().getId());

                //设置sellerid
                item.setSellerId(goods.getGoods().getSellerId());

                //设置brand
                TbBrand tbBrand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
                item.setBrand(tbBrand.getName());

                //商家名称
                TbSeller tbSeller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
                item.setSeller(tbSeller.getNickName());

                tbItemMapper.insert(item);
            }

        }else{

            //插如雨item相关的数据。
            TbItem item = new TbItem();

            //存入title

            String title =goods.getGoods().getGoodsName();
            item.setTitle(title);
            //存入图片url
            String itemImages = goods.getGoodsDesc().getItemImages();
            List<Map> imageList = JSON.parseArray(itemImages, Map.class);
            if (imageList.size()>0){

                item.setImage((String)imageList.get(0).get("url"));

            }

            //设置三级类id
            item.setCategoryid(goods.getGoods().getCategory3Id());

            //设置所属类目
            TbItemCat tbItemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
            item.setCategory(tbItemCat.getName());

            //创建时间
            item.setCreateTime(new Date());

            //更新时间
            item.setUpdateTime(new Date());

            //商品id
            item.setGoodsId(goods.getGoods().getId());

            //设置sellerid
            item.setSellerId(goods.getGoods().getSellerId());

            //设置brand
            TbBrand tbBrand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
            item.setBrand(tbBrand.getName());

            //商家名称
            TbSeller tbSeller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
            item.setSeller(tbSeller.getNickName());

            //设置价格
            item.setPrice(goods.getGoods().getPrice());

            //设置状态

            item.setStatus("1");

            //设置是否默认

            item.setIsDefault("1");

            //设置库存
            item.setNum(999);

            //设置spec
            item.setSpec("{}");

            tbItemMapper.insert(item);

        }

    }



	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods = new Goods();
		TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goods.setGoodsDesc(tbGoodsDesc);

		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		goods.setGoods(tbGoods);

		//查询itemList
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);
		List<TbItem> tbItems = tbItemMapper.selectByExample(example);
		goods.setItemList(tbItems);

		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			tbGoods.setIsDelete("1");
			goodsMapper.updateByPrimaryKey(tbGoods);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andIsDeleteIsNull();//显示非删除状态的记录
		
		if(goods!=null){			
						if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				//criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
                            criteria.andSellerIdEqualTo(goods.getSellerId());
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}
	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

    @Override
    public void updateStatus(Long[] ids, String status) {
        for (Long id :ids){

            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            tbGoods.setAuditStatus(status);
            goodsMapper.updateByPrimaryKey(tbGoods);
        }

    }

	@Override
	public List<TbItem> findItemListByGoodsIdAndStatus(Long[] ids, String status) {

		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdIn(Arrays.asList(ids));
		criteria.andStatusEqualTo(status);

		return tbItemMapper.selectByExample(example);
	}


}
