package univision.com.utilities;

import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.FilenameUtils;
import com.sun.mail.smtp.SMTPTransport;

public class MailApi {

	public static void send(String body, String attachment) {
		try {
			String to = EnvirommentManager.getInstance().getProperty("EmailReportTo");
			String from = EnvirommentManager.getInstance().getProperty("EmailReportFrom");
	
			final String username = EnvirommentManager.getInstance().getProperty("EmailUsername");
			final String password = EnvirommentManager.getInstance().getProperty("EmailPassword");
	
			// Assuming you are sending email through relay.jangosmtp.net
			String host = EnvirommentManager.getInstance().getProperty("EmailHost");
	
			Properties props = new Properties();
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", host);
			props.put("mail.smtp.port", "25");
	
			// Get the Session object.
			Session session = Session.getInstance(props,
					new javax.mail.Authenticator() {
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(username, password);
						}
					});

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			message.setSubject("Univision crawler report");
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(body + "Please find the attached file\n\n");
			// Create a multipar message
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);
			if (!attachment.equals("")) {
				for(String attachmentFile : attachment.split(",")){
					// Part two is attachment
					messageBodyPart = new MimeBodyPart();
					DataSource source = new FileDataSource(attachmentFile);
					messageBodyPart.setDataHandler(new DataHandler(source));
					messageBodyPart.setFileName(FilenameUtils.getName(attachmentFile));
					multipart.addBodyPart(messageBodyPart);
				}
			}
			// Send the complete message parts
			message.setContent(multipart);

			// Send message
			SMTPTransport t = (SMTPTransport) session.getTransport("smtps");
			t.connect(EnvirommentManager.getInstance().getProperty("EmailSmtp"), username, password);
			t.sendMessage(message, message.getAllRecipients());
			System.out.println("Response: " + t.getLastServerResponse());
			t.close();

		} catch (MessagingException e) {
			//throw new RuntimeException(e);
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
