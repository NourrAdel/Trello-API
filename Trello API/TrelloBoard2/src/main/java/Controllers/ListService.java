package Controllers;

import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import EJB.Board;
import EJB.BoardDTO;
import EJB.Card;
import EJB.User;
import EJB.ejbList;

@Stateless
@Path("List")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

public class ListService {
	
	@EJB
	private ejbList listService;
	
	@EJB
	private Board boardService;
	
	@PersistenceContext(unitName = "hello")
	private EntityManager em;
	
	
	
	@POST
	 @Path("/check_teamLeader")
	 public Response isTeamLeader(@QueryParam("id") Long id) {
		 User u = em.find(User.class, id);
		 
	     if (u.isTeamleader()) {
	         
	         return Response.ok().build();
	     } else {
	         
	         return Response.status(Response.Status.FORBIDDEN)
	                        .entity("Access denied - user is not a team leader.")
	                        .build();
	     }
	 }
	
	@POST
	@Path("/create")
	public String CreateList(@QueryParam ("name") String boardName, ejbList list) {
		
		if (boardName== null) {
	         return "No Board exist with this name.";
	     }
		if(list == null) {
			return "List is null!";
		}
		try {
			Board b = boardService.findBoardWithName(em, boardName);
			if(b == null) {
				return "Board not found";
			}

			if(list.getTeamLeader() != b.getTeamLeader()) {
				return "Must be Parent Board team leader to create list";
			}if(listService.ListExists(em, list.getName())) {
				return "List already Exist";
			}
			list.setTeamLeader(b.getTeamLeader());
			boardService.addList(b, list);
			
			listService.SaveList(em, list,b);
			
			if(b.getLists().contains(list) && list.getBoard().getName() == b.getName()) {
				
           	 	return "List Added";
            }
			return "Cant add List";
		}catch (NoResultException e) {
	         return "Parent Board Not found";
	     }  catch (Exception e) {
	         return "An error occurred while creating the list."; 
	     }
	}
	
	@DELETE
	@Path("DeleteList/{ListId}")
	public String DeleteList(@PathParam("ListId") Long id) {
		try {
			ejbList listToBeDelete = listService.findListById(id,em);
			
			
		      
		      if (listToBeDelete != null) {
		    	  
				  deleteListCardsByQuery(listToBeDelete);
		          return "List deleted successfully";
		          
		      } else {
		          return "List is null";
		      }
		}catch (NoResultException e) {
	         return "List does not exist";
	     } catch (Exception e) {
			e.printStackTrace();
			return "Failed to delete list";
		}
	}
	
	public void deleteListCardsByQuery(ejbList l) {
	    List<Card> cardsToDelete = l.getCards();
	    
	    if(!l.getCards().isEmpty()) {
	    	for (Card card : cardsToDelete) {
		        card.getComments().clear();
		        em.remove(card); 
		    }
		}
	    
	    delListHelp(l);
	}

	
	public void delListHelp(ejbList listToBeDelete) {
		boardService.removeList(listToBeDelete.getBoard(), listToBeDelete,em);
  	  	listService.deleteList(em,listToBeDelete);
	}

	@GET
	@Path("/{id}/alllists")
	public List<ejbList> getAllListsForBoard(@PathParam("id") Long boardId) {
	    TypedQuery<ejbList> query = em.createQuery("SELECT l FROM ejbList l JOIN l.Parentboard b WHERE b.id = :boardId", ejbList.class);
	    query.setParameter("boardId", boardId);
	    return query.getResultList();
	}
	
	
	 @GET
	    @Path("allList")
	    public List <String> getAllL() {
			return  em.createQuery("SELECT u.name FROM ejbList u", String.class).getResultList();

		}

	
	
	
}

