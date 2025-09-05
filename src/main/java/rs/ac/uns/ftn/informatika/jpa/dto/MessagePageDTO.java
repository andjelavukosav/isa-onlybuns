package rs.ac.uns.ftn.informatika.jpa.dto;

import java.time.LocalDateTime;
import java.util.List;

public class MessagePageDTO {
    private List<ChatMessageDTO> messages;
    private LocalDateTime boundaryTimestamp;
    private boolean hasMore;


}
