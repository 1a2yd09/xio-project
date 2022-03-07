package com.cat.service;

import com.cat.mapper.MailMapper;
import com.cat.pojo.CuttingSignal;
import com.cat.pojo.MailConfig;
import com.cat.pojo.WorkOrder;
import com.cat.pojo.message.OrderMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * @author CAT
 */
//@Service
public class MailService {
    private final JavaMailSender mailSender;
    private final MailMapper mailMapper;
    private final ThreadPoolTaskExecutor executor;

    public MailService(JavaMailSender mailSender, MailMapper mailMapper, @Qualifier("mailTaskExecutor") @Autowired(required = false) ThreadPoolTaskExecutor executor) {
        this.mailSender = mailSender;
        this.mailMapper = mailMapper;
        this.executor = executor;
    }

    /**
     * 发送业务流程异常邮件。
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

            this.executor.execute(() -> this.mailSender.send(mimeMessage));
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
