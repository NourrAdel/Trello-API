package Controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import EJB.BoardDTO;
import EJB.Card;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.persistence.*;
import EJB.Board;
import EJB.User;
import EJB.ejbList;

@Stateless
@Path("Board")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

public class BoardService {
	
	@PersistenceContext(unitName = "hello")
	private EntityManager em;
	
	 @EJB
	 private Board BoardService;
	 
	 @EJB
	private ListService listService;
	 
	 
	 

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
	 @Path("/{id}/create")
	 public Response createBoard(Board b, @PathParam("id") Long userId) {
	     // Check if the provided board is null
	     if (b == null) {
	         return Response.status(Response.Status.BAD_REQUEST)
	                        .entity("Board object is null.")
	                        .build();
	     }

	     // Check if the provided user ID is null or negative
	     if (userId == null || userId <= 0) {
	         return Response.status(Response.Status.BAD_REQUEST)
	                        .entity("Invalid user ID.")
	                        .build();
	     }

	     try {
	         // Find the user by ID
	         User u = em.find(User.class, userId);
	         
	         // Check if the user exists
	         if (u == null) {
	             return Response.status(Response.Status.NOT_FOUND)
	                            .entity("User not found.")
	                            .build();
	         }
	         
	         // Check if the user is a team leader
	         if (!u.isTeamleader()) {
	             return Response.status(Response.Status.FORBIDDEN)
	                            .entity("User is not a TeamLeader Role")
	                            .build();
	         }
	     } catch (NoResultException e) {
	         // Handle case where user is not registered
	         return Response.status(Response.Status.NOT_FOUND)
	                        .entity("User Not registered")
	                        .build();
	     }

	     // Check if a board with the same name already exists
	     if (BoardService.boardExists(em, b.getName())) {
	         return Response.status(Response.Status.CONFLICT)
	                        .entity("Board with this name already exists.")
	                        .build();
	     } else {
	         try { 
	             // Save the board
	             b.setTeamLeader(userId);
	             User u = em.find(User.class, userId);
	             BoardService.addCollaborator(b, u);
	             BoardService.saveBoard(em, b);
	             em.merge(u);
	             if (b.getCollaborators().contains(u)) {
	                 return Response.status(Response.Status.CREATED)
	                                .entity(u.getName() + " created a Board successfully.")
	                                .build();
	             }
	             return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
	                            .entity("Failed to Create Board")
	                            .build();
	             
	         } catch (Exception e) {
	             // Handle any other exceptions
	             e.printStackTrace();
	             return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
	                            .entity("Failed to Create Board")
	                            .build();
	         }
	     }
	 }

	
	 @GET
	 @Path("/{id}/allBoards")
	 public List<BoardDTO> getAllBoardsDTO(@PathParam("id") Long id) {
	     TypedQuery<Board> query = em.createQuery(
	         "SELECT DISTINCT b FROM Board b JOIN b.collaborators u WHERE u.id = :id", Board.class);
	     query.setParameter("id", id);

	     List<Board> boards = query.getResultList();
	     return boards.stream()
	                  .map(BoardDTO::toDTO) 
	                  .collect(Collectors.toList());
	 }

	

	 @POST
	 @Path("{id1}/{id2}")
	 public Response inviteUser(@PathParam("id1") Long id, @PathParam("id2") Long id2, @QueryParam("name") String bn) {
	     if (bn == null) {
	         return Response.status(Response.Status.BAD_REQUEST).entity("No Board with this name.").build();
	     }
	     
	     if (id == null || id <= 0) {
	         return Response.status(Response.Status.BAD_REQUEST).entity("Inviter ID not valid.").build();
	     }
	     if (id2 == null || id2 <= 0) {
	         return Response.status(Response.Status.BAD_REQUEST).entity("The Invited user ID not valid.").build();
	     }
	     
	     try {
	         User u1 = em.find(User.class, id);
	         if (u1 == null) {
	             return Response.status(Response.Status.NOT_FOUND).entity("User not found.").build();
	         }
	         if (!u1.isTeamleader()) {
	             return Response.status(Response.Status.FORBIDDEN).entity("Inviter is not in a TeamLeader Role").build();
	         }
	         User u2 = em.find(User.class, id2);
	         
	         if (u2 == null) {
	             return Response.status(Response.Status.NOT_FOUND).entity("Can't find invited User.").build();
	         }
	         
	         Board inviteToBoard = BoardService.findBoardWithName(em, bn);
	         
	         BoardService.addCollaborator(inviteToBoard, u2);
	         BoardService.mergeCollab(inviteToBoard, u2, em);
	         
	         return Response.ok(BoardService.getUserById(id2, inviteToBoard.getCollaborators()).getName() + " is invited successfully").build();
	         
	     } catch (NoResultException e) {
	         return Response.status(Response.Status.NOT_FOUND).entity("Inviter Not registered").build();
	     }
	 }
	 
	 
	 @DELETE
	 @Path("DeleteBoard/{Userid}")
	 public Response deleteBoard(@PathParam("Userid") Long id, @QueryParam("name") String bn) {
	     if (bn == null) {
	         return Response.status(Response.Status.BAD_REQUEST).entity("No Board with this name.").build();
	     }
	     
	     if (id == null || id <= 0) {
	         return Response.status(Response.Status.BAD_REQUEST).entity("User ID not valid.").build();
	     }
	     
	     try {
	         User u1 = em.find(User.class, id);
	         
	         if (u1 == null) {
	             return Response.status(Response.Status.NOT_FOUND).entity("User not found.").build();
	         }
	         
	         if (!u1.isTeamleader()) {
	             return Response.status(Response.Status.FORBIDDEN).entity("User is not in a TeamLeader Role").build();
	         }
	         
	         Board deleteBoard = BoardService.findBoardWithName(em, bn);
	         
	         if (deleteBoard == null) {
	             return Response.status(Response.Status.NOT_FOUND).entity("Board not found").build();
	         }
	         
	         String deleteBoardName = deleteBoard.getName();
	         
	         if (!deleteBoard.getCollaborators().isEmpty()) {
	             for (User collaborator : deleteBoard.getCollaborators()) {
	                 collaborator.removeBoardFromList(deleteBoard, collaborator);
	                 em.merge(collaborator);
	             }
	         }
	         
	         deleteBoard.getCollaborators().clear();
	         
	         deleteBoardListsByQuery1(deleteBoard, u1);
	         
	         em.remove(deleteBoard);
	         
	         return Response.ok(deleteBoardName + " is Deleted").build();
	         
	     } catch (NoResultException e) {
	         return Response.status(Response.Status.NOT_FOUND).entity("Board not found").build();
	     } catch (Exception e) {
	         
	         e.printStackTrace();
	         return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to Delete Board").build();
	     }
	 }
	  @Transactional
	 public void deleteBoardListsByQuery1(Board b,User u) {
		  
		 List<ejbList> listsToDelete = b.getLists();
		    
		    if(!b.getLists().isEmpty()) {
		    	for (ejbList l : listsToDelete) {
		    		listService.deleteListCardsByQuery(l);
			    }
		    	
			}
	 }
	 
	 }
	

