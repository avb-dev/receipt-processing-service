package application.mail;

import application.exceptions.MailException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${MAIL_FROM}")
    private String mailFrom;

    //Отправка текста
    public void sendText(String to, String subject, String text) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailFrom);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(text);
            mailSender.send(msg);
        } catch (Exception exception) {
            log.info("Сообщение не отправлено, проблемы с почтой: {}", exception.getMessage());
        }
    }

    //Отправка html
    public void sendHtml(String to, String subject, String printUrl) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject(subject);

            HtmlBlank html = new HtmlBlank(printUrl);
            helper.setText(html.getHtml(), true);

            mailSender.send(message);

        } catch (Exception exception) {
            throw new MailException(exception.getMessage());
        }
    }
}
