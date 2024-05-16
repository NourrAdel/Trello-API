package Messaging;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceContext;

import EJB.Card;

public class TaskDeadlineNotifier implements Runnable{
	
	@Inject
	 JMSClient jmsUtil;
	
	private Long timeDifferenceMillis;
	private Long creationTime;
	private Long deadlineTime;
	
	public TaskDeadlineNotifier(Long l1,Long l2){
		this.creationTime = l1;
		this.deadlineTime = l2;
		this.timeDifferenceMillis = l2-l1 - 60000;
	}
	
	@PersistenceContext(unitName = "hello")
	private EntityManager em;
	
	@Override
	public void run() {
		
            try {
            	
                System.out.println("******************   Startingg   ##################");
                
                Thread.sleep(timeDifferenceMillis); 
                
                System.out.println("************************DEADLINE*******************");
                
                

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

	public long getTimeDifferenceMillis() {
		return timeDifferenceMillis;
	}

	public void setTimeDifferenceMillis(long timeDifferenceMillis) {
		this.timeDifferenceMillis = timeDifferenceMillis;
	}


	public long getDeadlineTime() {
		return deadlineTime;
	}


	public void setDeadlineTime(long deadlineTime) {
		this.deadlineTime = deadlineTime;
	}


	public long getCreationTime() {
		return creationTime;
	}


	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}
	
}
