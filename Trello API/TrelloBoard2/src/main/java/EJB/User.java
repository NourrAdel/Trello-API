package EJB;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.Stateless;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
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
import javax.persistence.NoResultException;
import javax.persistence.OneToMany;
import javax.persistence.Query;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;




@Stateless
@Entity
public class User {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    
    
    private String password;
    
    @NotNull
    @Column(unique = true)
    private String email;
    
    private String title;
    private String name;
    
    private boolean teamleader;
   
    
    @ManyToMany
    @JoinTable(name = "boardXuser",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "Board_id"))
    private List<Board> boards = new ArrayList<>();
    
    @OneToMany(mappedBy = "assigneduser")
    private List<Card> assignedCard = new ArrayList<>();
    

	public User() {}

    public User(@NotNull String email,@NotNull String password, String name,String title) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.title = title;
//        this.boards = boards;
//        this.assignedCard = assignedCard;
//        this.MyBoards = MyBoards;
    }
    
    

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(@NotNull String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(@NotNull String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public List<Board> getBoards() {
        return boards;
    }

    public void setBoards(List<Board> boards) {
        this.boards = boards;
    }
    
    
 
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public void addBoard(Board b) {
    	boards.add(b);
    }
    
    public Board getBoardById(Long BoardId,List<Board> b) {
        if (BoardId == null || b == null) {
            return null;
        }
        
        for (Board board : b) {
            if (board.getId().equals(BoardId)) {
                return board;
            }
        }
        return null;
    }
    
    public void removeBoardFromList(Board b,User u) {
    	Board removeBoard = u.getBoardById(b.getId(),u.getBoards());
    	u.getBoards().remove(removeBoard);
    	
    }
    
    public List<Card> getAssignedCard() {
        return assignedCard;
    }

    public void setAssignedCard(List<Card>assignedCard) {
        this.assignedCard = assignedCard;
    }
    
   


   
    @Transactional
    public void saveUser1(EntityManager em,User u) {
        em.persist(u);
    }
    
    
    public boolean userExists(EntityManager em, String email) {
        Query query = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email");
        query.setParameter("email", email);
        Long count = (Long) query.getSingleResult();
        return count > 0;
    }

    
    public User findById(Long id, EntityManager em) {
        return em.find(User.class, id);
    }
    
    public String findByEmailAndPassword(EntityManager em, String email, String password) {
        try {
            TypedQuery<String> query = em.createQuery("SELECT u.name FROM User u WHERE u.email = :email and u.password = :password", String.class);
            query.setParameter("email", email);
            query.setParameter("password", password);
            String name = query.getSingleResult();
            return name;
        } catch (NoResultException e) {
            return null; 
        }
    }

	public boolean isTeamleader() {
		return teamleader;
	}

	public void setTeamleader(boolean teamleader) {
		this.teamleader = teamleader;
	}
    
}