package ma.enset.eisbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendTicketNotification(String toEmail, String employeeName, String description) {
        log.info("Sending ticket notification email to {}...", toEmail);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("🚨 Alerte de Suivi RH - Nouveau Ticket Créé");
            message.setText("Bonjour " + employeeName + ",\n\n"
                    + "Un ticket de suivi vient d'être généré automatiquement par l'agent d'analyse de présence :\n\n"
                    + "Détail : " + description + "\n\n"
                    + "Veuillez contacter votre responsable RH pour régulariser votre situation.\n"
                    + "Cordialement,\nL'Équipe RH.");
            mailSender.send(message);
            log.info("E-mail de notification envoyé avec succès à : {}", toEmail);
        } catch (Exception e) {
            log.error("Échec de l'envoi de l'e-mail à {} via SMTP: {}. Simulation d'impression de l'e-mail dans les logs.", toEmail, e.getMessage());
            log.info("\n=== [SIMULATION E-MAIL] ===\n"
                    + "Destinataire: {}\n"
                    + "Objet: 🚨 Alerte de Suivi RH - Nouveau Ticket Créé\n"
                    + "Contenu:\n"
                    + "Bonjour {},\n\n"
                    + "Un ticket de suivi vient d'être généré automatiquement par l'agent d'analyse de présence :\n\n"
                    + "Détail : {}\n\n"
                    + "Veuillez contacter votre responsable RH pour régulariser votre situation.\n"
                    + "Cordialement,\nL'Équipe RH.\n"
                    + "===========================", toEmail, employeeName, description);
        }
    }
}
