package com.elderly.health.service.impl;

import com.elderly.health.common.Constants;
import com.elderly.health.exception.BusinessException;
import com.elderly.health.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 邮件服务实现类
 *
 * @author elderly-health
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    /**
     * 发件人邮箱（即 spring.mail.username）
     */
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendVerificationCode(String email, String code, String purpose) {
        String subject = buildSubject(purpose);
        String content = buildHtmlContent(code, purpose);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(mimeMessage);
            log.info("验证码邮件发送成功：email={}, purpose={}", email, purpose);
        } catch (MessagingException e) {
            log.error("验证码邮件发送失败：email={}, purpose={}", email, purpose, e);
            throw new BusinessException("邮件发送失败，请稍后重试");
        }
    }

    /**
     * 根据用途构建邮件标题
     *
     * @param purpose 用途
     * @return 邮件标题
     */
    private String buildSubject(String purpose) {
        if (Constants.VERIFICATION_PURPOSE_FORGET_PASSWORD.equals(purpose)) {
            return "【社区健康管理】密码重置验证码";
        }
        return "【社区健康管理】注册验证码";
    }

    /**
     * 构建 HTML 邮件内容
     *
     * @param code    验证码
     * @param purpose 用途
     * @return HTML 内容
     */
    private String buildHtmlContent(String code, String purpose) {
        String action = Constants.VERIFICATION_PURPOSE_FORGET_PASSWORD.equals(purpose)
                ? "重置密码" : "注册账号";

        return "<!DOCTYPE html>"
                + "<html lang=\"zh-CN\">"
                + "<head><meta charset=\"UTF-8\"></head>"
                + "<body style=\"font-family: 'Microsoft YaHei', Arial, sans-serif; background-color: #f5f5f5; margin: 0; padding: 20px;\">"
                + "<div style=\"max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1);\">"
                + "<div style=\"background-color: #4CAF50; color: #ffffff; padding: 20px; text-align: center;\">"
                + "<h2 style=\"margin: 0;\">社区健康管理平台</h2>"
                + "</div>"
                + "<div style=\"padding: 30px;\">"
                + "<p style=\"font-size: 16px; color: #333;\">您好！</p>"
                + "<p style=\"font-size: 16px; color: #333; line-height: 1.6;\">您正在进行<strong>" + action + "</strong>操作，请使用以下验证码完成验证：</p>"
                + "<div style=\"text-align: center; margin: 30px 0;\">"
                + "<span style=\"display: inline-block; font-size: 32px; font-weight: bold; color: #4CAF50; letter-spacing: 8px; padding: 15px 30px; background-color: #f0f9eb; border-radius: 6px; border: 1px dashed #4CAF50;\">"
                + code
                + "</span>"
                + "</div>"
                + "<p style=\"font-size: 14px; color: #999; line-height: 1.6;\">"
                + "验证码有效期为 <strong style=\"color: #ff9800;\">" + Constants.VERIFICATION_CODE_EXPIRE_MINUTES + " 分钟</strong>，请尽快使用，过期后需重新获取。<br/>"
                + "如果此操作并非由您本人发起，请忽略本邮件，您的账号安全不会受到影响。"
                + "</p>"
                + "</div>"
                + "<div style=\"background-color: #fafafa; padding: 15px 30px; text-align: center; font-size: 12px; color: #999; border-top: 1px solid #eeeeee;\">"
                + "此邮件由系统自动发送，请勿直接回复。<br/>"
                + "&copy; 社区健康管理平台"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
    }
}
