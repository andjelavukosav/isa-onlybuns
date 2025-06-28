package rs.ac.uns.ftn.informatika.jpa;

import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import rs.ac.uns.ftn.informatika.jpa.dto.UserDTO;
import rs.ac.uns.ftn.informatika.jpa.service.FollowService;
import rs.ac.uns.ftn.informatika.jpa.service.UserService;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SpringBootTest
@ActiveProfiles("test")
public class TxExampleApplicationTests {

   /* @Autowired
    private FollowService followService;

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(TxExampleApplicationTests.class);

    @BeforeEach
    public void setUp() throws  Exception{
        userService.save(new UserDTO(1, "user1", "1234", "Marko", "Pertovic", "marko@gmail.com"));
        userService.save(new UserDTO(2, "user2", "12345", "Marina", "Smiljanic", "marina@gmail.com"));
        userService.save(new UserDTO(3, "user3", "123456", "Filip", "Ostojic", "filip@gmail.com"));
    }

    @Test()
    public void testOptimisticLockingScenario() throws Throwable{

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<?> future1 = executor.submit( () ->{
            try{
                System.out.println("Startovan Thread 1");
                followService.followUser(1, 2);
                System.out.println(" Thread 1 je zavrsio transakciju.");
                Thread.sleep(1000);

            }catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        });

        Future<?> future2 = executor.submit(() -> {
            try {
                System.out.println("Startovan Thread 2");
                followService.followUser(3, 2); // Korisnik 3 pokušava da zapati korisnika 2
                System.out.println(" Thread 2 je zavrsio transakciju.");

            } catch (Exception e) {
                System.out.println("Exception in Thread 2: " + e.getClass());
                throw e;
            }
        });

        try{
            future1.get();

        }catch (ExecutionException e) {
            Assertions.assertTrue(e.getCause() instanceof ObjectOptimisticLockingFailureException,
                    "Expected ObjectOptimisticLockingFailureException but got: " + e.getCause().getClass());
            System.out.println("Caught expected exception: " + e.getCause().getClass());
        } catch (InterruptedException e) {
            logger.error("Thread was interrupted", e);
            Thread.currentThread().interrupt(); // Važno da se ponovo postavi flag prekida
        }
        executor.shutdown();
    }

*/
}
