package rs.ac.uns.ftn.informatika.jpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.jpa.dto.UserDTO;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmailSenderService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private Environment env;

    @Async
    public void sendVerificationEmail(UserDTO userDTO, String link) throws MailException, InterruptedException, MessagingException {

        MimeMessage mail = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mail, true);

        helper.setTo(userDTO.getEmail());
        helper.setFrom("isa.projekti@gmail.com");
        helper.setSubject("Verification email");

        String verificationUrl = link;
        String htmlMsg = "<p>Dear " + userDTO.getUsername() + ",</p>"
                + "<p>Thank you for registering on Onlybuns! Your account has been successfully created."
                + " To activate your account and get started, please verify your email address by clicking the link below::</p>"
                + "<a href='" + verificationUrl + "'>Verifikuj svoj nalog</a>"
                + "<p>Once your email is verified, you’ll be able to log in and enjoy all the features Onlybuns has to offer.</p>"
                + "Best regards,\n"
                + "The Onlybuns Team";


        // Postavite HTML sadržaj mejla
        helper.setText(htmlMsg, true);

        mailSender.send(mail);
    }
}
