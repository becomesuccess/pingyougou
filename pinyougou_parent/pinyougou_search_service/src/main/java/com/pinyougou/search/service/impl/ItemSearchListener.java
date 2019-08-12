package com.pinyougou.search.service.impl;


import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSeachService;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;


@Component
public class ItemSearchListener implements MessageListener{

    private ItemSeachService itemSeachService;


    @Override
    public void onMessage(Message message) {
        TextMessage textMessage= (TextMessage) message;

        //获取到消息中的消息体
        try {
            String text = textMessage.getText();
            System.out.println("get the message:"+text);
            List<TbItem> tbItems = JSON.parseArray(text, TbItem.class);
            itemSeachService.importItem(tbItems);
            System.out.println("import message successful");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
