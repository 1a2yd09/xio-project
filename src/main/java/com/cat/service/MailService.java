package com.cat.service;

import com.cat.entity.message.OrderErrorMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * @author CAT
 */
@Service
public class MailService {
    @Value("${smtp.from}")
    String from;
    @Value("${smtp.to}")
    String to;

    @Autowired
    JavaMailSender mailSender;

    /**
     * 发送工单无法处理邮件。
     *
     * @param msg 消息
     */
    public void sendOrderErrorMail(OrderErrorMsg msg) {
        try {
            MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom(this.from);
            helper.setTo(this.to);
            helper.setSubject("Work order processing blocked");
            String text = "<p>OrderId: %d.</p><p>CuttingSize: %s.</p><p>ProductSpecification: %s.</p><p>Send at: %s.</p>";
            String html = String.format(text, msg.getOrderId(), msg.getCuttingSize(), msg.getProductSpecification(), msg.getCreatedAt());
            helper.setText(html, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
