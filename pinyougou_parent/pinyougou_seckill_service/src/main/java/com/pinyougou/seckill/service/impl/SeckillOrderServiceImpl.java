package com.pinyougou.seckill.service.impl;
import java.util.Date;
import java.util.List;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.pojo.TbSeckillOrderExample;
import com.pinyougou.pojo.TbSeckillOrderExample.Criteria;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}
	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}


	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;

	@Autowired
	private IdWorker idWorker;

	/*
	* 提交秒杀订单
	* */
	@Override
	public void submitOrder(Long seckillId, String userId) {

		//1.从缓存中查询秒杀商品的信息
	    TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);

	    //判断查询到的信息
		if (seckillGoods==null){
			throw  new RuntimeException("商品不存在！");
		}

		if (seckillGoods.getStockCount()<=0){
			throw  new RuntimeException("商品已经被抢光啦！");
		}

		//2. 修改商品的库存
		seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
		//3.将修改后的库存更新到缓存
		if (seckillGoods.getStockCount()<=0){//如果库存已经减到了0
			//删除缓存中的该商品的记录
			redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
			//修改数据库中商品信息
			seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
		}else {//如果库存还有,修改redis缓存中的数据
			redisTemplate.boundHashOps("seckillGoods").put(seckillId,seckillGoods);
		}

		//生成订单
		long orderId = idWorker.nextId();
		TbSeckillOrder seckillOrder = new TbSeckillOrder();
		seckillOrder.setId(orderId);//存入主键id
		seckillOrder.setSeckillId(seckillId);//秒杀商品id
		seckillOrder.setCreateTime(new Date());//创建日期
		seckillOrder.setMoney(seckillGoods.getCostPrice());//设置订单价格，秒杀价格
		seckillOrder.setSellerId(seckillGoods.getSellerId());//商家id
		seckillOrder.setStatus("0");//未支付
		//将订单存入缓存中
		redisTemplate.boundHashOps("seckillOrder").put(userId,seckillOrder);
	}

	@Override
	public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
		return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
	}

	/*
	* 保存秒杀订单
	* */
	@Override
	public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {
		//1. 从缓存中查询数据
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);

		if (seckillOrder==null){
			throw  new RuntimeException("订单不存在！");
		}

		if (seckillOrder.getId().longValue()!=orderId.longValue()){
			throw new RuntimeException("订单号不符！");
		}
		//2.封装数据
        seckillOrder.setUserId(userId);
        System.out.println(seckillOrder.getUserId());

		seckillOrder.setPayTime(new Date());//设置支付时间
		seckillOrder.setTransactionId(transactionId);//设置建议流水号
		seckillOrder.setStatus("1");//状态设置为已支付

		//3.将数据存入数据库中
		seckillOrderMapper.insert(seckillOrder);
		//清楚缓存中的数据
		redisTemplate.boundHashOps("seckillOrder").delete(userId);
	}


	/*
	*订单超时从缓存中删除订单
	* */
    @Override
    public void deleteOrderFromRedis(String userId, Long orderId) {
        //1. 从缓存中查询订单数据
        TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
        if (seckillOrder!=null&&seckillOrder.getId().longValue()==orderId.longValue()){
            //从缓存中删除订单数据
            redisTemplate.boundHashOps("seckillOrder").delete(userId);

            //修改缓存中商品的库存
            TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());

            if (seckillGoods==null){//如果缓存中已经不存在该商品，应该从新像缓存中加入该商品，并将商品的库存设为1
                //从数据库中搜索该商品
				TbSeckillGoods tbSeckillGoods = seckillGoodsMapper.selectByPrimaryKey(orderId);
				tbSeckillGoods.setStockCount(1);
				redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getSellerId(),tbSeckillGoods);
			}else {//缓存中还存在该商品，将该商品的库存数量加1
                //库存数量加一
                 seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
                 //删除缓存中原来的商品
                redisTemplate.boundHashOps("seckillGoods").delete(orderId);
                redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getSellerId(),seckillGoods);
            }

        }

    }

}
