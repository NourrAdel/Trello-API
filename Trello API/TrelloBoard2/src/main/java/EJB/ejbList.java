package EJB;

import java.util.ArrayList;
import java.util.List;

//import java.util.Set;

import javax.ejb.Stateless;
import javax.persistence.CascadeType;
import javax.persistence.Column;
//import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
//import javax.persistence.JoinTable;

import javax.persistence.ManyToOne;
import javax.persistence.NoResultException;
import javax.persistence.OneToMany;
import javax.persistence.Query;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
//import javax.persistence.OneToMany;
//import javax.persistence.OneToMany;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;


@Stateless
@Entity
public class ejbList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(unique = true)
    private String name;
//
    @ManyToOne
    @JoinColumn(name = "board_id")
    private  Board Parentboard;
    
    private Long teamLeader;
    

    
    @OneToMany(mappedBy = "list",cascade = CascadeType.REMOVE)
    private List <Card> cards =new ArrayList <>();
    
    
    public List<Card> getCards() {
        return cards;
    }

    // Setter method for cards
    public void setCards(List<Card> cards) {
        this.cards = cards;
    }
    

    // Constructors
    public ejbList() {
    }

    public ejbList(String name) {// Set<Card> cards
        this.name = name;
      
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Board getBoard() {
        return Parentboard;
    }

    public void setBoard(Board board) {
        this.Parentboard = board;
    }   
    
    public boolean ListExists(EntityManager em,String name) {
    	Query query = em.createQuery("SELECT COUNT(l) FROM ejbList l WHERE l.name = :name");
        query.setParameter("name", name);
        Long count = (Long) query.getSingleResult();
        return count > 0;
    }
    @Transactional
    public void saveList(EntityManager em) {
        try {
        	
        	if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("List name is null or empty");
            }
            
           em.persist(this);
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save list: " + e.getMessage());
        }
    }
    
    public void SaveList(EntityManager em,ejbList l,Board b) {
    	em.persist(l);
    	em.merge(b);
    }

    
    public ejbList findListById(Long id,EntityManager em) {
        return em.find(ejbList.class, id);
    }
    
    
    @Transactional
    public void deleteList(EntityManager em,ejbList l) {
        try {
        	
            em.remove(l); 
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to delete list: " + e.getMessage());
        }
    }

    public ejbList findListWithName(EntityManager em,String ln) {
    	try {
    		TypedQuery<ejbList> query = em.createQuery("SELECT b FROM ejbList b WHERE b.name = :name", ejbList.class);
            query.setParameter("name", ln);
            return query.getSingleResult();
    	}catch (NoResultException e) {
	         return null;
	     } 
    	 
    }

	public Long getTeamLeader() {
		return teamLeader;
	}

	public void setTeamLeader(Long teamLeader) {
		this.teamLeader = teamLeader;
	}
	
	public void addCard(ejbList l,Card c) {
   	 l.cards.add(c);
   	 c.setList(l);
    }
    

}