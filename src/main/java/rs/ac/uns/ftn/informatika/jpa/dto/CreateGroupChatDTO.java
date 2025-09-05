package rs.ac.uns.ftn.informatika.jpa.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

public class CreateGroupChatDTO {

    @NotEmpty(message = "Group name must not be empty")
    private String name;

    @NotEmpty(message = "Member list must not be empty")
    @Size(min = 1, message = "At least one member must be added")
    private List<Integer> memberIds;

    public CreateGroupChatDTO() {}

    public String getName() { return name; }
    public void setName(String groupName) { this.name = groupName; }

    public List<Integer> getMemberIds() { return memberIds; }
    public void setMemberIds(List<Integer> memberIds) { this.memberIds = memberIds; }
}
