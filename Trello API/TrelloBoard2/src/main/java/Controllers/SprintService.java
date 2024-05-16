package Controllers;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import EJB.Sprint;
import EJB.User;

@Stateless
@Path("Sprint")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

public class SprintService {

	@PersistenceContext(unitName = "hello")
	private EntityManager em;
	
	@POST
	@Path("/{id}/Sprintend")
	public String endingSprint(@PathParam("id") int sprintId) {
		
		if(sprintId == 1) {
			Sprint sprint =new Sprint();
			
			TypedQuery<String> query=em.createQuery("SELECT c.name FROM Card c",String.class);
			sprint.setsprintCards(query.getResultList());	
			em.persist(sprint);
		}
		
		Sprint Sprint1=em.find(Sprint.class, sprintId);
		Sprint1.setfinished(true);

		Sprint newSprint=new Sprint();
		TypedQuery<String> query2=em.createQuery("SELECT c.name FROM Card c WHERE c.finished=:done",String.class);
		query2.setParameter("done", false);
		
		newSprint.setsprintCards(query2.getResultList());
		em.merge(Sprint1);
		em.persist(newSprint);
		return  "*A New Sprint Has Started*";
	}
	
	
	@GET
	@Path("/{id}/generateReport")
	public String generateReport(@PathParam("id") int sprintId) {
	    Sprint reportedSprint = em.find(Sprint.class, sprintId);
	    StringBuilder report = new StringBuilder();
	    report.append("Sprint ID: {").append(reportedSprint.getSprintId()).append("} ");

	    
	    List<String> Cards = reportedSprint.getsprintCards();
	    
	    if (Cards != null && !Cards.isEmpty()) {
	        report.append(" List Of Cards: (");
	        for (String task : Cards) {
	            report.append("- ").append(task).append(" ");
	        }
	        report.append(") ");
	    } else {
	        report.append("No Cards!");
	    }

	    return report.toString();
	}
	
}