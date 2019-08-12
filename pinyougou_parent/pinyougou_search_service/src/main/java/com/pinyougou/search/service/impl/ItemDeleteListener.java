package com.pinyougou.search.service.impl;

import com.pinyougou.search.service.ItemSeachService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.Serializable;
import java.util.Arrays;

@Component
public class ItemDeleteListener implements MessageListener {

    @Autowired
    private ItemSeachService itemSeachService;

    @Override
    public void onMessage(Message message) {

        ObjectMessage object = (ObjectMessage) message;

        try {
            Long[] ids = (Long[]) object.getObject();
            itemSeachService.deleteById(Arrays.asList(ids));
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
