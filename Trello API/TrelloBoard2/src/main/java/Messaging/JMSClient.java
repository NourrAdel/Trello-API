package Messaging;

import java.io.Serializable;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.TextMessage;

@Startup
@Singleton
public class JMSClient{

    @Resource(mappedName = "java:/jms/queue/MyTrelloQueue")
    private Queue myTrelloQueue;

    @Inject
    JMSContext context;

    public void sendMessage(Event event) {
        JMSProducer producer = context.createProducer();
		ObjectMessage message = context.createObjectMessage(event);
		producer.send(myTrelloQueue, message);
		System.out.println("Sent Event: " + event);
    }
    
    
    
//    public String getMessage() {
//    	
//    	JMSConsumer consumer = context.createConsumer(myTrelloQueue);
//    	try {
//            TextMessage msg = (TextMessage) consumer.receiveNoWait();
//            if(msg != null) {
//	            System.out.println("Recieved Message: "+ msg);
//	            return msg.getBody(String.class);
//            }else {
//            	return null;
//            }
//            
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        } finally {
//        	consumer.close();
//        }
//    }
}