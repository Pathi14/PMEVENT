package fr.pmevent.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import fr.pmevent.entity.EventEntity;
import fr.pmevent.entity.GuestEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class MailService {
    @Value("${sendgrid.api-key}")
    private String apiKey;

    @Value("${sendgrid.sender}")
    private String sender;

    public void sendGuestInvitationEmail(GuestEntity guest, EventEntity event) {

        String subject = "Invitation à l'évènement : " + event.getName();
        Email from = new Email(sender);
        Email to = new Email(guest.getEmail());

        // Image de l'évènement, si disponible
        String eventImageUrl = event.getImageUrl() != null ? "https://www.pmevent.com/uploads/events/" + event.getImageUrl() : "https://www.pmevent.com/uploads/events/default-event.png";

        String htmlContent = """
                <html>
                  <body style="font-family: 'Arial', sans-serif; background-color: #fff4f0; color: #5C4033; padding: 20px;">
                    <div style="max-width:600px; margin:auto; background-color:#fff0e6; border-radius:10px; border:1px solid #e6b8a2; padding:30px;">
                      <div style="text-align:center; margin-bottom:20px;">
                        <img src="https://www.pmevent.com/images/logo.png" alt="PM Event" style="width:120px;">
                      </div>
                      <h2 style="color:#D2691E;">Bonjour %s %s,</h2>
                      <p>Vous êtes invité à l'évènement suivant :</p>
                
                      <table style="width:100%%; border-collapse:collapse;">
                        <tr>
                          <!-- Colonne info -->
                          <td style="vertical-align:top; padding-right:10px;">
                            <ul style="list-style:none; padding:0;">
                              <li><strong>Nom de l'évènement :</strong> %s</li>
                              <li><strong>Date :</strong> %s</li>
                              <li><strong>Lieu :</strong> %s</li>
                              <li><strong>Nombre de places :</strong> %d</li>
                            </ul>
                          </td>
                          <!-- Colonne image -->
                          <td style="vertical-align:top; text-align:right;">
                            <img src="%s" alt="Image de l'évènement" style="width:180px; border-radius:8px;">
                          </td>
                        </tr>
                      </table>
                
                      <div style="text-align:center; margin:30px 0;">
                        <a href="#" style="background-color:#D2691E; color:white; text-decoration:none; padding:12px 25px; border-radius:5px; font-weight:bold;">
                          Confirmer ma présence
                        </a>
                      </div>
                    </div>
                  </body>
                </html>
                """.formatted(
                guest.getFirstname() != null ? guest.getFirstname() : "",
                guest.getName() != null ? guest.getName() : "",
                event.getName(),
                event.getStart_date(),
                event.getLocation(),
                guest.getNumber_places(),
                eventImageUrl
        );

        Content body = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, body);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            com.sendgrid.Response response = sg.api(request);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody());
            System.out.println("Headers: " + response.getHeaders());

        } catch (IOException e) {
            System.err.println("Erreur lors de l'envoi d'email : " + e.getMessage());
        }
    }

    public void sendGuestReminderEmail(GuestEntity guest, EventEntity event) {

        String subject = "Invitation à l'évènement : " + event.getName();
        Email from = new Email(sender);
        Email to = new Email(guest.getEmail());

        // Image de l'évènement, si disponible
        String eventImageUrl = event.getImageUrl() != null ? "https://www.pmevent.com/uploads/events/" + event.getImageUrl() : "https://www.pmevent.com/uploads/events/default-event.png";

        String htmlContent = """
                <html>
                  <body style="font-family: 'Arial', sans-serif; background-color: #fff4f0; color: #5C4033; padding: 20px;">
                    <div style="max-width:600px; margin:auto; background-color:#fff0e6; border-radius:10px; border:1px solid #e6b8a2; padding:30px;">
                      <div style="text-align:center; margin-bottom:20px;">
                        <img src="https://www.pmevent.com/images/logo.png" alt="PM Event" style="width:120px;">
                      </div>
                      <h2 style="color:#D2691E;">Bonjour %s %s,</h2>
                      <p>Vous vous informons que vous êtes invité à l'évènement suivant :</p>
                
                      <table style="width:100%%; border-collapse:collapse;">
                        <tr>
                          <!-- Colonne info -->
                          <td style="vertical-align:top; padding-right:10px;">
                            <ul style="list-style:none; padding:0;">
                              <li><strong>Nom de l'évènement :</strong> %s</li>
                              <li><strong>Date :</strong> %s</li>
                              <li><strong>Lieu :</strong> %s</li>
                              <li><strong>Nombre de places :</strong> %d</li>
                            </ul>
                          </td>
                          <!-- Colonne image -->
                          <td style="vertical-align:top; text-align:right;">
                            <img src="%s" alt="Image de l'évènement" style="width:180px; border-radius:8px;">
                          </td>
                        </tr>
                      </table>
                
                      <div style="text-align:center; margin:30px 0;">
                        <a href="#" style="background-color:#D2691E; color:white; text-decoration:none; padding:12px 25px; border-radius:5px; font-weight:bold;">
                          Confirmer ma présence
                        </a>
                      </div>
                    </div>
                  </body>
                </html>
                """.formatted(
                guest.getFirstname() != null ? guest.getFirstname() : "",
                guest.getName() != null ? guest.getName() : "",
                event.getName(),
                event.getStart_date(),
                event.getLocation(),
                guest.getNumber_places(),
                eventImageUrl
        );

        Content body = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, body);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            com.sendgrid.Response response = sg.api(request);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody());
            System.out.println("Headers: " + response.getHeaders());

        } catch (IOException e) {
            System.err.println("Erreur lors de l'envoi d'email : " + e.getMessage());
        }
    }

}
