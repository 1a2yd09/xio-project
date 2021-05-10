package com.cat.service;

import com.cat.mapper.MailMapper;
import com.cat.pojo.CuttingSignal;
import com.cat.pojo.MailConfig;
import com.cat.pojo.WorkOrder;
import com.cat.pojo.message.OrderMessage;
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
    private final JavaMailSender mailSender;
    private final MailMapper mailMapper;

    private static final ExecutorService MAIL_POOL;

    static {
        MAIL_POOL = new ThreadPoolExecutor(3, 3, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(1),
                r -> new Thread(r, "mail-pool-thread-" + r.hashCode()));
    }

    public MailService(JavaMailSender mailSender, MailMapper mailMapper) {
        this.mailSender = mailSender;
        this.mailMapper = mailMapper;
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
            MailConfig config = this.mailMapper.getLatestMailConfig();

            helper.setFrom(config.getSendFrom());
            helper.setTo(config.getSendTo());
            helper.setSubject("Work thread throw exception!");
            String text = "<p>OrderId: %d.</p><p>CuttingSize: %s.</p><p>ProductSpecification: %s.</p><p>Send at: %s.</p>";
            WorkOrder order = msg.getOrder();
            CuttingSignal signal = msg.getSignal();
            String html = String.format(text, order.getId(), signal.getCuttingSize(), order.getProductSpecification(), msg.getCreatedAt());
            helper.setText(html, true);

            MAIL_POOL.execute(() -> mailSender.send(mimeMessage));
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
