package rs.ac.uns.ftn.informatika.jpa;
import java.util.concurrent.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;

import rs.ac.uns.ftn.informatika.jpa.controller.AuthenticationController;
import rs.ac.uns.ftn.informatika.jpa.controller.UserController;
import rs.ac.uns.ftn.informatika.jpa.dto.UserDTO;
import rs.ac.uns.ftn.informatika.jpa.mapper.UserDTOMapper;
import rs.ac.uns.ftn.informatika.jpa.model.User;
import rs.ac.uns.ftn.informatika.jpa.service.UserService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JpaExampleApplicationTests {

	@Autowired
	private UserService userService;

	@Autowired
	private AuthenticationController userController; // Injektovanje UserController-a

	@Test(expected = IllegalArgumentException.class)
	public void testConcurrentUserRegistration() throws Throwable {
		CountDownLatch latch = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(2);

		Future<?> future1 = executor.submit(() -> {
			try {
				latch.await();
				UserDTO userToRegister = new UserDTO();
				userToRegister.setUsername("testUser");
				userToRegister.setEmail("test1@example.com");
				userToRegister.setPassword("password123");
				userService.save(userToRegister);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});

		Future<?> future2 = executor.submit(() -> {
			latch.countDown();
			try {
				Thread.sleep(200);
				UserDTO userToRegister = new UserDTO();
				userToRegister.setUsername("testUser");
				userToRegister.setEmail("test2@example.com");
				userToRegister.setPassword("password456");
				userService.save(userToRegister);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});

		try {
			future1.get();
			future2.get();
		} catch (ExecutionException e) {
			System.out.println("Exception: " + e.getCause());
			throw e.getCause();
		}

		executor.shutdown();
	}



}
