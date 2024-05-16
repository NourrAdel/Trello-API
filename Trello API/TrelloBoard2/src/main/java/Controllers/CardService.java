package Controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import EJB.Board;
import EJB.Card;
import EJB.User;
import EJB.ejbList;
import Messaging.Event;
import Messaging.JMSClient;
import Messaging.TaskDeadlineNotifier;



@Stateless
@Path("Card")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

public class CardService{
	
	@PersistenceContext(unitName = "hello")
	private EntityManager em;
	
	 @EJB
	 private ejbList listService;
	 
	 @EJB
	 private Card cardService;
	 
	 
	 
	 @Inject
	 JMSClient jmsUtil;
	
	@POST
	@Path("{id}/create")
	public String CreateCard(@QueryParam ("name") String ListName, Card card,@QueryParam ("Comments") String FirstComment,@PathParam("id")Long id) {
		
		if (ListName== null) {
	         return "No List exist with this name.";
	     }
		
		if(card == null) {
			return "Card is null!";
		}
		if(id == null) {
			return "Cant create card without User id";
		}
		
		
		try {
			
			ejbList l = listService.findListWithName(em, ListName);
			if(l == null) {
				return "Cant find list with that name";
			}if(cardService.cardExists(em, card.getName())) {
				return "Card already Exist";
			}
			
			User u = em.find(User.class, id);
			if(u == null) {
				return "User doesnt exist";
			}
			if(!u.getBoards().contains(l.getBoard())) {
				return "User Must be collaborator on the card Board";
			}
			try {
				card.getComments().add(FirstComment);
				card.getUserWhoMadeComments().add(id);
				card.setStatus(l);
				card.setReporterName(u.getName());
				card.setFinished(false);
			}catch (Exception e) {
	            e.printStackTrace();
	            return "null Comment List " +e.getMessage();
	        }
			
			
			listService.addCard(l, card);
			
			cardService.SaveCard(em, l, card);
			
			Date cDate = card.getCreationDate();
			Date deadlineDate = card.getDeadLine();
			
			
			
			Long creationTime = cDate.getTime();
			Long deadlineTime = deadlineDate.getTime();
		 
			
			System.out.println("Creation Time: " + creationTime);
    	    System.out.println("Deadline Time: " + deadlineTime);
			
			TaskDeadlineNotifier p = new TaskDeadlineNotifier(creationTime,deadlineTime);
			new Thread(p).start();
			
			///
			if(l.getCards().contains(card) && card.getList().getName() == l.getName()) {
           	 	return "User "+ card.getReporterName()+ " Created Card " + card.getName()+" with comment "  + card.getComments().get(0) +" With status " + card.getStatus() + " and Creation Date " + card.getCreationDate();
			}
			return "Cant create card";
			
		}catch (NoResultException e) {
	         return "List Not Found";
	     }	 catch (Exception e) {
            e.printStackTrace();
            return "Failed to Create Card " +e.getMessage();
        }
		
	}
	
	@PUT
	@Path("{id1}/{id2}/move")
	public String MoveCard(@QueryParam ("name") String l2,@PathParam ("id1")Long cardId,@PathParam ("id2")Long userId) {
		
		if(l2 == null) {
			return "the after list is NULL";
		}
		if (cardId == null || cardId <= 0) {
	         return "Invalid Card ID.";
	     }
		
		try {
			Card c = em.find(Card.class, cardId);
			if(c == null) {
				return "Card Doesnt exist";
			}
			
			ejbList l1 = c.getList();
			if(l1 == null) {
				return "Original list not found";
			}
			
			User u = em.find(User.class, userId);
			if(u == null) {
				return "User doesnt exist";
			}
			
			if(!u.getBoards().contains(l1.getBoard())) {
				return "User Must be collaborator on the card Board";
			}
			
			ejbList afterList = listService.findListWithName(em, l2);
			
			if(afterList == null) {
				return "after list not found";
			}
			
			if(l1.getBoard() != afterList.getBoard()) {
				return "The two lists are not on the same board";
			}
			
			if(afterList.getName().equals("Done")) {
				c.setFinished(true);
			}
			
			cardService.MoveCard1(l1, afterList, c);
			cardService.MoveCardPersist(l1, afterList, c, em);
			
			Event event = new Event();
            event.setMessageId(UUID.randomUUID().toString());
            event.setEventName( "Card '" + c.getName() + "' has been moved from list "+l1.getName() +" to list "+ afterList.getName());
            event.setEventDate(new Date());
            event.setCardID(cardId);
          
            // Set any additional information in the event if needed
            
            jmsUtil.sendMessage(event);
			
            em.persist(event);
            
			
			if(!l1.getCards().contains(c) && afterList.getCards().contains(c) && c.getList().getName() == afterList.getName()) {
           	 	return c.getName() + " Moved from " + l1.getName() + " to List " + afterList.getName() +" with status "+ c.getStatus();
			}
			return "Cant Move card";
			
		}catch (NoResultException e) {
	         return "List Not Found";
	     }	 catch (Exception e) {
           e.printStackTrace();
           return "Failed to Create Card " +e.getMessage();
       }
		
	}
	
	@PUT
	@Path("{id1}/{id2}/assign")
	public String assignCard(@PathParam ("id1") Long cardId,@PathParam("id2")Long userId) {
		
		if(cardId==null) {
			return "Card is null";
		}
		if (userId == null || userId <= 0) {
	         return "Invalid user ID.";
	     }
		
		try {
			Card c = em.find(Card.class, cardId);
			if(c == null) {
				return "Card Doesnt exist";
			}
			User u = em.find(User.class, userId);
			if(u == null) {
				return "User doesnt exist";
			}
			if(!u.getBoards().contains(c.getList().getBoard())) {
				return "User Must be collaborator on the card Board";
			}
			
			if (c.getassignee() != null && c.getassignee().getId().equals(userId)) {
	            return "Card is already assigned to the user";
	        }
			c.setAssigneeName(u.getName());
			cardService.AssignCard(u, c);
			cardService.AssignCardPersist(u, c, em);
			em.merge(c);
			
			Event event = new Event();
            event.setMessageId(UUID.randomUUID().toString());
            event.setEventName( "Card '" + c.getDescription() + "' assigned to User '" + u.getName() + "'");
            event.setEventDate(new Date());
            event.setCardID(cardId);
            
            
            jmsUtil.sendMessage(event);
            
            em.persist(event);
			
			if(u.getAssignedCard().contains(c) && c.getassignee().getName() == u.getName()) {
				return "Card " + c.getName() + " is successfully Assigned to " + c.getassignee().getName();
			}
			
			return "Cant assign card";
			
		}catch (NoResultException e) {
	         return "List Not Found";
	     }	 catch (Exception e) {
          e.printStackTrace();
          return "Failed to Create Card " +e.getMessage();
      }	
	}
	
	@PUT
	@Path("{id1}/{id2}/addDescription")
	public String addDesToCard(@PathParam("id1") Long cardId,@PathParam("id2") Long userId,@QueryParam("Des") String d,@QueryParam("Comment") String Comment) {
		if(cardId==null) {
			return "Card is null";
		}
		if (userId == null || userId <= 0) {
	         return "Invalid user ID.";
	     }
		if(d == null && Comment == null) {
			return "No Comment or Description to be added";
		}
		
		try {
			Card c = em.find(Card.class, cardId);
			if(c == null) {
				return "Card Doesnt exist";
			}
			User u = em.find(User.class, userId);
			if(u == null) {
				return "User doesnt exist";
			}
			if(!u.getBoards().contains(c.getList().getBoard())) {
				return "User Must be collaborator on the card Board";
			}
			String DD = c.getDescription();
			
			if (d != null && !d.isEmpty()) {
			    c.setDescription(d);
			}

			if (Comment != null && !Comment.isEmpty()) {
			    c.getComments().add(Comment);
			    c.getUserWhoMadeComments().add(userId);
			}
			em.merge(c);
			
			Event event = new Event();
            event.setMessageId(UUID.randomUUID().toString());
            event.setEventName( "Card '" + c.getName() + "' Has been changed by the User '" + u.getName() + "'");
            event.setCardID(cardId);
            event.setEventDate(new Date());
           
            
            em.persist(event);
            
            jmsUtil.sendMessage(event);
			
			return "Description changed from "+DD +" to "+ c.getDescription();
			
	}catch (Exception e) {
        e.printStackTrace();
        return "Failed to add Description/Comment to card: " + e.getMessage();
    }}
	
	
	
	@GET
	@Path("advanceSearch")
	public String advancedSearch2(@QueryParam("name") String name,
	        @QueryParam("description") String description,
	        @QueryParam("reporterName") String cardReporter,
	        @QueryParam("assigneeName") String assigneeName,
	        @QueryParam("status") String status,
	        @QueryParam("creationDate") String creationDate) {
	    
	    List<String> conditions = new ArrayList<>();
	    Date date;
	    
	    
	    String queryString = "SELECT c.name FROM Card c WHERE ";
	    
	    
	    if (name != null && !name.isEmpty()) {
	        conditions.add("c.name LIKE :name");
	    }
	    if (description != null && !description.isEmpty()) {
	        conditions.add("c.description LIKE :description");
	    }
	    if (cardReporter != null && !cardReporter.isEmpty()) {
	        conditions.add("c.reporterName LIKE :reporterName");
	    }
	    if (assigneeName != null && !assigneeName.isEmpty()) {
	        conditions.add("c.assigneeName LIKE :assigneeName");
	    }
	    if (status != null && !status.isEmpty()) {
	        conditions.add("c.status LIKE :status");
	    }
	    if (creationDate != null && !creationDate.isEmpty()) {
	        
	        conditions.add("c.creationDate Like :creationDate");
	    }
	    
	    
	    queryString += String.join(" AND ", conditions);
	    
	    
	    TypedQuery<String> query = em.createQuery(queryString, String.class);
	    
	    
	    if (name != null && !name.isEmpty()) {
	        query.setParameter("name", "%" + name + "%");
	    }
	    if (description != null && !description.isEmpty()) {
	        query.setParameter("description", "%" + description + "%");
	    }
	    if (cardReporter != null && !cardReporter.isEmpty()) {
	        query.setParameter("reporterName", "%" + cardReporter + "%");
	    }
	    if (assigneeName != null && !assigneeName.isEmpty()) {
	        query.setParameter("assigneeName", "%" + assigneeName + "%");
	    }
	    if (status != null && !status.isEmpty()) {
	        query.setParameter("status", "%" + status + "%");
	    }
	    if (creationDate != null && !creationDate.isEmpty()) {
	    	date = parseDateString(creationDate);
	        // Set the parameter with the parsed Date object
	        query.setParameter("creationDate", date);
	    }

	    
	    
	    List<String> results = query.getResultList();
	    
	    return String.join(", ", results);
	}

	private Date parseDateString(String dateString) {
	    
	    try {
	        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	        return formatter.parse(dateString);
	    } catch (ParseException e) {
	        e.printStackTrace();
	        return null;
	    }
	}


	
	@GET
	@Path("/getEvents1")
	public List<Event> getEvents1(@QueryParam("userId") Long userId, @QueryParam("cardId") Long cardId) {
	    try {
	        
	        Card card = em.find(Card.class, cardId);
	        if (card == null) {
	            
	            return null; 
	        }
	        
	        
	        if (card.getassignee().getId().equals(userId) || card.getUserWhoMadeComments().contains(userId)) {
	            
	            List<Event> events = em.createQuery("SELECT e FROM Event e WHERE e.cardID = :c", Event.class)
	                .setParameter("c", cardId)
	                .getResultList();
	            return events;
	        } else {
	            
	            return null; 
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        
	        return null;
	    }
	}

	
	@GET
    @Path("allcards")
    public List <String> getAllL() {
		return  em.createQuery("SELECT u.name FROM Card u", String.class).getResultList();
	}
}	
	