package com.cat.service;

import com.cat.pojo.CuttingSignal;
import com.cat.pojo.WorkOrder;
import com.cat.pojo.message.OrderMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author CAT
 */
@Service
public class MailService {
    @Value("${smtp.from}")
    String from;
    @Value("${smtp.to}")
    String to;

    private final JavaMailSender mailSender;

    private final ExecutorService mailPool;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        this.mailPool = new ThreadPoolExecutor(3, 3, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(1),
                r -> new Thread(r, "mail-pool-thread-" + r.hashCode()));
    }

    /**
     * 发送工单无法处理邮件。
     *
     * @param msg 消息
     */
    public void sendWorkErrorMail(OrderMessage msg) {
        try {
            MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom(this.from);
            helper.setTo(this.to);
            helper.setSubject("Work thread throw exception!");
            String text = "<p>OrderId: %d.</p><p>CuttingSize: %s.</p><p>ProductSpecification: %s.</p><p>Send at: %s.</p>";
            WorkOrder order = msg.getOrder();
            CuttingSignal signal = msg.getSignal();
            String html = String.format(text, order.getId(), signal.getCuttingSize(), order.getProductSpecification(), msg.getCreatedAt());
            helper.setText(html, true);

            this.mailPool.execute(() -> mailSender.send(mimeMessage));
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
