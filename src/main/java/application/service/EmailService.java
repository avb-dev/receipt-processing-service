package application.service;

import application.config.AppConfigProperties;
import application.entity.HtmlBlank;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailService {

    private final JavaMailSender mailSender;
    private final String mailFrom;

    public EmailService(JavaMailSender mailSender, AppConfigProperties appConfigProperties) {
        this.mailSender = mailSender;
        this.mailFrom = appConfigProperties.getMailAdmin();
    }

    public void sendText(String to, String subject, String text) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailFrom);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(text);
            mailSender.send(msg);
        } catch (Exception exception) {
            log.warn("Сообщение не отправлено, проблемы с почтой: {}", exception.getMessage());
        }
    }

    public void sendHtml(String to, String subject, String printUrl) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        helper.setFrom(mailFrom);
        helper.setTo(to);
        helper.setSubject(subject);

        HtmlBlank html = new HtmlBlank(printUrl);
        helper.setText(html.getHtml(), true);

        mailSender.send(message);
        log.info("Успешная отправка покупателю " + to + " письма: " + printUrl);
    }
}
