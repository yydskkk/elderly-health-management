package com.elderly.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.elderly.health.common.Constants;
import com.elderly.health.entity.VerificationCode;
import com.elderly.health.exception.BusinessException;
import com.elderly.health.mapper.VerificationCodeMapper;
import com.elderly.health.service.EmailService;
import com.elderly.health.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 验证码服务实现类
 *
 * @author elderly-health
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final VerificationCodeMapper verificationCodeMapper;
    private final EmailService emailService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendCode(String email, String purpose) {
        // 检查1分钟内是否已发送过验证码
        LocalDateTime intervalStart = LocalDateTime.now()
                .minusMinutes(Constants.VERIFICATION_CODE_SEND_INTERVAL_MINUTES);
        Long recentCount = verificationCodeMapper.selectCount(
                new LambdaQueryWrapper<VerificationCode>()
                        .eq(VerificationCode::getEmail, email)
                        .eq(VerificationCode::getPurpose, purpose)
                        .eq(VerificationCode::getUsed, 0)
                        .ge(VerificationCode::getCreateTime, intervalStart)
        );
        if (recentCount != null && recentCount > 0) {
            throw new BusinessException("验证码发送过于频繁，请1分钟后再试");
        }

        // 生成6位数字验证码
        String code = generateCode();

        // 保存到 verification_code 表（过期时间5分钟后）
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(email);
        verificationCode.setCode(code);
        verificationCode.setPurpose(purpose);
        verificationCode.setUsed(0);
        verificationCode.setExpireTime(LocalDateTime.now()
                .plusMinutes(Constants.VERIFICATION_CODE_EXPIRE_MINUTES));
        verificationCodeMapper.insert(verificationCode);

        // 调用 EmailService 发送邮件
        emailService.sendVerificationCode(email, code, purpose);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean verifyCode(String email, String code, String purpose) {
        // 查询该邮箱+用途+未使用+未过期的最新验证码
        VerificationCode verificationCode = verificationCodeMapper.selectOne(
                new LambdaQueryWrapper<VerificationCode>()
                        .eq(VerificationCode::getEmail, email)
                        .eq(VerificationCode::getPurpose, purpose)
                        .eq(VerificationCode::getUsed, 0)
                        .gt(VerificationCode::getExpireTime, LocalDateTime.now())
                        .orderByDesc(VerificationCode::getCreateTime)
                        .last("LIMIT 1")
        );

        // 如果不存在或验证码不匹配，抛出异常
        if (verificationCode == null || !code.equals(verificationCode.getCode())) {
            throw new BusinessException("验证码错误或已过期");
        }

        // 校验通过后标记 used=1
        verificationCodeMapper.update(null,
                new LambdaUpdateWrapper<VerificationCode>()
                        .eq(VerificationCode::getId, verificationCode.getId())
                        .set(VerificationCode::getUsed, 1)
        );

        return true;
    }

    /**
     * 生成6位数字验证码
     *
     * @return 6位数字验证码字符串
     */
    private String generateCode() {
        int code = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return String.valueOf(code);
    }
}
