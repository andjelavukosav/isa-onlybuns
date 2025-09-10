package rs.ac.uns.ftn.informatika.jpa.queue;

import rs.ac.uns.ftn.informatika.jpa.model.AsylumAndVeterinarian;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ManualMessageQueue {
    private static final BlockingQueue<AsylumAndVeterinarian> queue = new LinkedBlockingQueue<>();

    public static void addMessage(AsylumAndVeterinarian msg) {
        queue.add(msg);
    }

    public static AsylumAndVeterinarian takeMessage() throws InterruptedException {
        return queue.take(); // blokira dok ne stigne nova poruka
    }

    public static boolean isEmpty() {
        return queue.isEmpty();
    }
}

