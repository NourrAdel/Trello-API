package Messaging;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
public class Event implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String messageId;
    private String eventName;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date eventDate;
    
    private Long cardID;
    

    // Constructors, getters, and setters
    
    public Event() {}

    public Event(String messageId, String eventName, Date eventDate) {
        this.messageId = messageId;
        this.eventName = eventName;
        this.eventDate = eventDate;
    }

    // Getters and setters for each property

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }
    
    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", messageId='" + messageId + '\'' +
                ", eventName='" + eventName + '\'' +
                ", eventDate=" + eventDate +
                // Include other properties here
                '}';
    }

	public Long getCardID() {
		return cardID;
	}

	public void setCardID(Long cardID) {
		this.cardID = cardID;
	}

	
}
