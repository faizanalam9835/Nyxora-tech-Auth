package Auth.service.service.recruitment;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * 1. Application Received Email (Jab candidate pehli baar add hota hai)
     */
    @Async
    public void sendConfirmationEmail(String toEmail, String name, String jobTitle) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Application Received: " + jobTitle);

            String emailBody = String.format(
                    "Dear %s,\n\n" +
                            "Thank you for your interest in the %s position at our company.\n\n" +
                            "We have successfully received your application. Our recruitment team is currently " +
                            "reviewing your profile against our requirements. If your qualifications match " +
                            "our needs, we will reach out to you for the next steps.\n\n" +
                            "Best Regards,\n" +
                            "Talent Acquisition Team",
                    name, jobTitle
            );

            message.setText(emailBody);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Confirmation Email failed for " + toEmail + ": " + e.getMessage());
        }
    }

    /**
     * 2. Status Update Email (Jab Shortlist, Reject ya Select hota hai)
     */
    @Async
    public void sendStatusUpdateEmail(String toEmail, String name, String jobTitle, String newStatus) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Update on your application for " + jobTitle);

            // Professional messages for different stages
            String statusMessage = switch (newStatus.toUpperCase()) {
                case "SHORTLISTED" -> "Great news! Your profile has been shortlisted for the next round of interviews.";
                case "REJECTED" -> "Thank you for your interest. After careful consideration, we will not be moving forward with your application at this time.";
                case "SELECTED" -> "Congratulations! You have been selected for the position. Our team will share the formal offer details soon.";
                default -> "The status of your application for the " + jobTitle + " position has been updated to: " + newStatus;
            };

            String emailBody = String.format(
                    "Dear %s,\n\n" +
                            "This is to inform you that there is an update regarding your application for the %s position.\n\n" +
                            "%s\n\n" +
                            "Best Regards,\n" +
                            "Talent Acquisition Team",
                    name, jobTitle, statusMessage
            );

            message.setText(emailBody);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Status Email failed for " + toEmail + ": " + e.getMessage());
        }
    }
}