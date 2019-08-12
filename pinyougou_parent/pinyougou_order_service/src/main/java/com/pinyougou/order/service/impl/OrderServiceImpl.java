package com.pinyougou.order.service.impl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;
import com.pinyougou.order.service.OrderService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;

	@Autowired
	private TbPayLogMapper payLogMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}


	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private IdWorker idWorker;

	@Autowired
	private TbOrderItemMapper orderItemMapper;
	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {

		List<Cart> cartList  = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());

		Double total_money = 0.00;

		List orderList= new ArrayList();

		/*要根据商家创建订单，一个商家创建一个订单*/
		for (Cart cart : cartList) {
			//生成订单id
			long orderId = idWorker.nextId();

			//将生成的订单id存入集合中
			orderList.add(orderId);

			System.out.println(orderId);
			System.out.println("sellerId:"+cart.getSellerId());
			//一个商家新建一个订单
			TbOrder tbOrder = new TbOrder();
			tbOrder.setOrderId(orderId);//设置订单id
			tbOrder.setPaymentType(order.getPaymentType());//设置支付方式
			tbOrder.setStatus("1");//订单状态为未支付状态
			tbOrder.setCreateTime(new Date());//设置订单创建时间
			tbOrder.setUpdateTime(new Date());//设置订单修改时间
			tbOrder.setUserId(order.getUserId());//设置用户id
			tbOrder.setReceiverAreaName(order.getReceiverAreaName());//设置收获地址
			tbOrder.setReceiverMobile(order.getReceiverMobile());//设置收货人电话
			tbOrder.setReceiver(order.getReceiver());//设置收货人姓名
			tbOrder.setSourceType(order.getSourceType());//设置订单来源
			tbOrder.setSellerId(cart.getSellerId());//设置商家id；

			Double money = 0.00;
			//设置商品价格，合计所有商品的价格
			for (TbOrderItem orderItem : cart.getTbOrderItemList()) {

				orderItem.setId(idWorker.nextId());//设置主键
				orderItem.setOrderId(orderId);//设置订单id
				orderItem.setSellerId(cart.getSellerId());//设置卖家id
				orderItemMapper.insert(orderItem);//将订单商品存入
				money+=orderItem.getTotalFee().doubleValue();
			}
			tbOrder.setPayment(new BigDecimal(money));
			orderMapper.insert(tbOrder);
			total_money+=money;
		}

		if ("1".equals(order.getPaymentType())){//如果是在线支付，那就存入日志

			TbPayLog payLog = new TbPayLog();
			payLog.setCreateTime(new Date());//创建时间
			payLog.setTotalFee((long)(total_money*100));//金额
			payLog.setUserId(order.getUserId());//用户id
			payLog.setOutTradeNo(idWorker.nextId()+"");//支付订单号
			payLog.setOrderList(orderList.toString().replace("[","").replace("]",""));//订单号列
			payLog.setTradeState("0");
			//将数据存入日志
			payLogMapper.insert(payLog);
			//将数据存入redis缓存中
			redisTemplate.boundHashOps("payLog").put(order.getUserId(),payLog);
		}

		//清楚redis中的缓存
		redisTemplate.boundHashOps("cartList").delete(order.getUserId());

	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			orderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

    @Override
    public TbPayLog searchPayLogFromRedis(String username) {
        return (TbPayLog) redisTemplate.boundHashOps("payLog").get(username);
    }

}
