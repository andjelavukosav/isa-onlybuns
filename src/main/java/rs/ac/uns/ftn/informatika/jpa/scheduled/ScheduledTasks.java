package rs.ac.uns.ftn.informatika.jpa.scheduled;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import rs.ac.uns.ftn.informatika.jpa.service.UserService;

import java.time.LocalDateTime;

@Component
public class ScheduledTasks {
    private final UserService userService;

    @Autowired
    public ScheduledTasks(UserService userService) {
        this.userService = userService;
    }

    @Scheduled(cron= "${cleanup.cron}") //(cron = "0 59 23 L * ?")
    public void cleanUpInactiveUsersAccount(){
        LocalDateTime now = LocalDateTime.now();
        userService.deleteInactiveUsersOlderThan(now.minusDays(7)); // ili odmah "sad", ako hoćeš samo neaktivne
    }
}
