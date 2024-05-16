package Controllers;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import Messaging.Event;

import javax.ejb.MessageDriven;
import javax.ejb.ActivationConfigProperty;

@MessageDriven(name = "QueueListener", activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:/jms/queue/MyTrelloQueue"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class JMSReceiver implements MessageListener {

    @Override
    public void onMessage(Message message) {
        System.out.println("$$$$$$$$$$$$$ New Message $$$$$$$$$$");

        if (message instanceof ObjectMessage) {
            ObjectMessage objectMessage = (ObjectMessage) message;
            try {
                Event event = (Event) objectMessage.getObject();
                System.out.println("Received Event: " + event);
                // Here you can further process the received event as needed
            } catch (JMSException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException("Message must be of type ObjectMessage");
        }
    }
}

