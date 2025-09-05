package rs.ac.uns.ftn.informatika.jpa.service;

import rs.ac.uns.ftn.informatika.jpa.dto.AdPostMessageDTO;

public interface AdMessageSender {

    void sendAdPost(AdPostMessageDTO message);
}
