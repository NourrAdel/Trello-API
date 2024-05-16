package EJB;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.ejb.Stateless;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NoResultException;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;

@Entity
@Stateless
public class Card  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(unique = true)
    private String name;
    
    private boolean finished;
    
    @ManyToOne
    @JoinColumn(name = "assignee")
    private User assigneduser;
    
    @ManyToOne
    @JoinColumn(name = "list")
    private ejbList list;
    
    private String reporterName;
    
    private String assigneeName;
    
    
    public void AssignCard(User u,Card c) {
    	u.getAssignedCard().add(c);
    	c.setassignee(u);
    }
    
    public void AssignCardPersist(User u,Card c,EntityManager em) {
    	em.merge(c);
    	em.merge(u);
    }
    
    @Transactional
    public void MoveCard1(ejbList l1,ejbList l2,Card c) {
    	l1.getCards().remove(c);
    	l2.getCards().add(c);
    	c.setList(l2);
    	c.setStatus(l2);
    }
    @Transactional
    public void MoveCardPersist(ejbList l1,ejbList l2,Card c,EntityManager em) {
    	em.merge(l1);
    	em.merge(l2);
    	em.merge(c);
    }
    
    
    @ElementCollection
    private List<Long> userWhoMadeComments = new ArrayList<>();
    
    private String description;
    
    @Temporal(TemporalType.DATE)
    private Date deadLine;
    
    @Temporal(TemporalType.DATE)
    private Date creationDate;
    
    private String status;
    
    @ElementCollection
    private List<String> Comments = new ArrayList<>();
    
    public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getDeadLine() {
		return deadLine;
	}

	public void setDeadLine(Date deadLine) {
		this.deadLine = deadLine;
	}

	public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }
    
    public User getassignee() {
        return assigneduser;
    }

    public void setassignee(User user) {
        this.assigneduser = user;
    }

    public ejbList getList() {
        return list;
    }

    public void setList(ejbList list) {
        this.list = list;
    }
    
    public void SaveCard(EntityManager em,ejbList l,Card c) {
    	em.persist(c);
    	em.merge(l);
    }
    public boolean cardExists(EntityManager em , String name) {
    	 Query query = (Query) em.createQuery("SELECT COUNT(c) FROM Card c WHERE c.name = :name");
         query.setParameter("name", name);
         Long count = (Long)  query.getSingleResult();
         return count > 0;
    }
    
    public Card findById(Long id, EntityManager em) {
        return em.find(Card.class, id);
    }

	public List<String> getComments() {
		return Comments;
	}

	public void setComments(List<String> comments) {
		Comments = comments;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(ejbList l) {
		this.status = l.getName();
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getReporterName() {
		return reporterName;
	}

	public void setReporterName(String reporterName) {
		this.reporterName = reporterName;
	}

	public String getAssigneeName() {
		return assigneeName;
	}

	public void setAssigneeName(String assigneeName) {
		this.assigneeName = assigneeName;
	}

	public List<Long> getUserWhoMadeComments() {
		return userWhoMadeComments;
	}

	public void setUserWhoMadeComments(List<Long> userWhoMadeComments) {
		this.userWhoMadeComments = userWhoMadeComments;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}
}