package rs.ac.uns.ftn.informatika.jpa.queue;

import rs.ac.uns.ftn.informatika.jpa.dto.AdPostMessageDTO;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AdPostMessageQueue {
    private static final BlockingQueue<AdPostMessageDTO> queue = new LinkedBlockingQueue<>();

    public static void addMessage(AdPostMessageDTO msg) {
        queue.add(msg);
    }

    public static AdPostMessageDTO takeMessage() throws InterruptedException {
        return queue.take();
    }
}
