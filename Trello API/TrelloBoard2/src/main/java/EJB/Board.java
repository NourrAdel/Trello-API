package EJB;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.persistence.OneToMany;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
@Stateless
@Entity
public class Board {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(unique = true)
    private String name;
    
    private Long teamLeader;
    
    
    
    
    @ManyToMany(mappedBy = "boards")
    private List<User> collaborators = new ArrayList <>();

//    @Transient
//   private List <Long> UID = new ArrayList <>();
//   
//   public List <Long> getUserID() {
//		return UID;
//	}
//
//	public void setUser(List <Long> userID) {
//		UID= userID;
//	}
	
	@Transient
	private List <String> Temp =  new ArrayList <>();
	

  @OneToMany(mappedBy = "Parentboard",cascade = CascadeType.REMOVE)
  private List<ejbList> lists = new ArrayList <>();
  
     public void addList(Board b,ejbList l) {
    	 b.lists.add(l);
    	 l.setBoard(b);
     }
     
     public void removeList(Board b,ejbList l,EntityManager em) {
    	 b.lists.remove(l);
    	 em.merge(b);
     }
  
	 public User getUserById(Long userId,List<User> c) {
	        if (userId == null || c == null) {
	            return null;
	        }
	        
	        for (User user : c) {
	            if (user.getId().equals(userId)) {
	                return user;
	            }
	        }
	        
	        return null;
	    }
    
    public Board() {
    }

    public Board(String name, Long teamLeader, Set<User> collaborators) {
        this.name = name;
        this.teamLeader = teamLeader;
//        this.collaborators = collaborators;
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

    public Long getTeamLeader() {
        return teamLeader;
    }

    public void setTeamLeader(Long teamLeader) {
        this.teamLeader = teamLeader;
    }
    
    
    public List<User> getCollaborators() {
        return collaborators;
    }
    
    
    public void setCollaborators(List <User>collaborators) {
        this.collaborators = collaborators;
    }
    
    
    public void addCollaborator(Board b,User c) {
    	b.collaborators.add(c);
//    	b.UID.add(c.getId());
    	c.getBoards().add(b);
    	
    }
    
    public void mergeCollab(Board b,User c,EntityManager em) {
    	em.merge(c);
    	em.merge(b);
    }
    
    
   public List<ejbList> getLists() {
        return lists;
   }

    public void setLists(List<ejbList> lists) {
        this.lists = lists;
    }
    
    
  
    
    public Board findBoardById(Long id,EntityManager em) {
        return em.find(Board.class, id);
    }
    

    
    public void saveBoard(EntityManager em,Board b) {
        em.persist(b);
    }
    
    
    public boolean boardExists(EntityManager em,String name) {
        Query query = em.createQuery("SELECT COUNT(b) FROM Board b WHERE b.name = :name");
        query.setParameter("name", name);
        Long count = (Long) query.getSingleResult();
        return count > 0;
    }
    
    
    
    public Board findBoardWithName(EntityManager em,String bn) {
    	try {
    		TypedQuery<Board> query = em.createQuery("SELECT b FROM Board b WHERE b.name = :name", Board.class);
            query.setParameter("name", bn);
            return query.getSingleResult();
    	}catch (NoResultException e) {
	         return null;
	     } 
    	 
    }

	public List <String> getTemp() {
		return Temp;
	}

	public void setTemp(List <String> temp) {
		Temp = temp;
	}

	
}