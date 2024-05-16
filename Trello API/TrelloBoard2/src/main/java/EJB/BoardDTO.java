package EJB;

public class BoardDTO {
    private Long id;
    private String name;
    private Long teamLeader;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getTeamLeader() {
		return teamLeader;
	}
	public void setTeamLeader(Long teamLeader) {
		this.teamLeader = teamLeader;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public static BoardDTO toDTO(Board board) {
	    BoardDTO dto = new BoardDTO();
	    dto.setId(board.getId());
	    dto.setName(board.getName());
	    dto.setTeamLeader(board.getTeamLeader());
	    return dto;
	}
    
}