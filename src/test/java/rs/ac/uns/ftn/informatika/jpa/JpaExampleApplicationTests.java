package rs.ac.uns.ftn.informatika.jpa;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit4.SpringRunner;
import rs.ac.uns.ftn.informatika.jpa.dto.UserDTO;
import rs.ac.uns.ftn.informatika.jpa.service.UserService;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JpaExampleApplicationTests {

	@Autowired
	private UserService userService;

	@Test
	public void testSimultaneousRegistrationWithSameUsername() throws Throwable {
		ExecutorService executor = Executors.newFixedThreadPool(2);

		// Registracija prvog korisnika
		Future<?> future1 = executor.submit(new Runnable() {
			@Override
			public void run() {
				try {
					UserDTO userDTO1 = new UserDTO();
					userDTO1.setUsername("duplicateUser");
					userDTO1.setEmail("duplicateEmail1@example.com");
					userDTO1.setPassword("password123");
					userDTO1.setEnabled(true);
					userDTO1.setLastPasswordResetDate(new Date());
					userDTO1.setFirstname("Duplicate User1");
					userDTO1.setLastname("Duplicate User1");
					userService.save(userDTO1);  // Prvi thread pokreće registraciju
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		// Registracija drugog korisnika sa istim korisničkim imenom
		Future<?> future2 = executor.submit(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(100); // Čeka da se prvi thread pokrene, ali se registracija ne završava

					// Pre-Check if username exists
					if (userService.findByUsername("duplicateUser") != null) {
						System.out.println("Username 'duplicateUser' already exists, skipping second registration.");
					} else {
						UserDTO userDTO2 = new UserDTO();
						userDTO2.setUsername("duplicateUser");
						userDTO2.setEmail("duplicateEmail2@example.com");
						userDTO2.setPassword("password456");
						userDTO2.setEnabled(true);
						userDTO2.setLastPasswordResetDate(new Date());
						userDTO2.setFirstname("Duplicate User2");
						userDTO2.setLastname("Duplicate User2");
						userService.save(userDTO2);  // Drugi thread pokušava da registruje korisnika sa istim korisničkim imenom
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		// Wait for both threads to finish execution
		try {
			future1.get();  // Wait for the first thread to finish
			future2.get();  // Wait for the second thread to finish
		} catch (ExecutionException e) {
			System.out.println("Exception from thread: " + e.getCause().getClass());
			throw e.getCause();  // Re-throw original exception
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		executor.shutdown();  // Shut down executor after completion
	}

}

