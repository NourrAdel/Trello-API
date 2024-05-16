package Controllers;

import java.io.IOException;
import java.util.List;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.websocket.server.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import EJB.ejbList;
import EJB.User;

@Stateless
@Path("User")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

public class UserService {
	
	
@PersistenceContext(unitName = "hello")
private EntityManager em;

	
   @EJB
   private User userService;
    
    @POST
    public String CreateUser(User u) {
    	
        if(userService.userExists(em,u.getEmail())) {
            return "User with this email already exists.";
        } 
        else {
        	try {
        		userService.saveUser1(em,u);
        	
        	}catch (Exception e) {
                e.printStackTrace();
                return "Failed to Create User";
            }
            
            return "User signed up successfully.";
        }
    }
    
    @GET
    @Path("allUsers")
    public List <String> getAllUsers() {
		return  em.createQuery("SELECT u.name FROM User u", String.class).getResultList();

	}
    
    @GET
    @Path("logIn")
    public String logUserIn(@QueryParam("email") String email, @QueryParam("password") String password) {
        try {
            String n =userService.findByEmailAndPassword(em, email, password);
            if (n == null) {
                return "User doesn't exist";
            }
            return n +" is logged in";
        } catch (NoResultException e) {
            return "User doesn't exist";
        }
    }
    
    @PUT
    @Path("/update")
    public String updateProfile(
        @QueryParam("id") Long id,
        @QueryParam("email") String email,
        @QueryParam("password") String password,
        @QueryParam("name") String name
    ) {
        try {
            User u = userService.findById(id, em);
          

            if (u != null) {
                if (name != null && !name.isEmpty()) {
                    u.setName(name);
                }
                if (password != null && !password.isEmpty()) {
                    u.setPassword(password);
                }
                if (email != null && !email.isEmpty()) {
                    u.setEmail(email);
                }
                em.merge(u);
                return "Profile updated";
            } else {
                return "User doesn't exist";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
}
