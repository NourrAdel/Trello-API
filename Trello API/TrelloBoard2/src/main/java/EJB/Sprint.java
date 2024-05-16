package EJB;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Stateless
@Entity
public class Sprint {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	
	private boolean finished;
	
	@ElementCollection
	private List<String> sprintCards = new ArrayList<>();
	
	public Sprint() {
		id=0;
		finished=false;
	}
	// Setter for pendingTasks
    public void setsprintCards(List<String> s) {
        this.sprintCards = s;
    }

    public boolean getfinished() {
		return finished;
	}
	public void setfinished(boolean f) {
		this.finished = f;
	}
    // Getter for pendingTasks
    public List<String> getsprintCards() {
        return sprintCards;
    }
	
	public int getSprintId() {
		return id;
	}
	public void setSprintId(int sprintId) {
		this.id=sprintId;
	}
	
		
	
}