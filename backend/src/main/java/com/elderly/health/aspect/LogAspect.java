package com.elderly.health.aspect;

import cn.hutool.json.JSONUtil;
import com.elderly.health.entity.OperationLog;
import com.elderly.health.service.OperationLogService;
import com.elderly.health.utils.IpUtils;
import com.elderly.health.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 操作日志 AOP 切面
 * 拦截标注了 @OperationLog 注解的方法，记录操作日志
 *
 * @author elderly-health
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    /**
     * 用于在线程内传递方法开始执行时间
     */
    private static final ThreadLocal<Long> START_TIME = new ThreadLocal<>();

    private final OperationLogService operationLogService;

    /**
     * 切点：标注了 @OperationLog 注解的方法
     */
    @Pointcut("@annotation(com.elderly.health.annotation.OperationLog)")
    public void operationLogPointCut() {
    }

    /**
     * 方法执行前记录开始时间
     *
     * @param joinPoint 连接点
     */
    @Before("operationLogPointCut()")
    public void doBefore(JoinPoint joinPoint) {
        START_TIME.set(System.currentTimeMillis());
    }

    /**
     * 方法正常返回后记录操作日志
     *
     * @param joinPoint 连接点
     * @param result    返回值
     */
    @AfterReturning(pointcut = "operationLogPointCut()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        try {
            handleLog(joinPoint, result, null);
        } catch (Exception e) {
            log.error("记录操作日志失败：{}", e.getMessage(), e);
        } finally {
            START_TIME.remove();
        }
    }

    /**
     * 处理日志记录
     *
     * @param joinPoint 连接点
     * @param result    返回值
     * @param e         异常
     */
    private void handleLog(JoinPoint joinPoint, Object result, Exception e) {
        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        com.elderly.health.annotation.OperationLog operationLogAnnotation =
                method.getAnnotation(com.elderly.health.annotation.OperationLog.class);
        if (Objects.isNull(operationLogAnnotation)) {
            return;
        }

        // 计算耗时
        Long startTime = START_TIME.get();
        long costTime = Objects.isNull(startTime) ? 0L : (System.currentTimeMillis() - startTime);

        // 获取请求信息
        HttpServletRequest request = getRequest();

        // 构建操作日志实体
        OperationLog operationLog = new OperationLog();
        operationLog.setOperation(operationLogAnnotation.value());
        operationLog.setMethod(joinPoint.getTarget().getClass().getName() + "#" + method.getName());
        operationLog.setTime(costTime);

        // 设置操作人信息
        Long userId = SecurityUtils.getCurrentUserId();
        if (Objects.nonNull(userId)) {
            operationLog.setUserId(userId);
        }
        operationLog.setUsername(SecurityUtils.getCurrentUserEmail());

        // 设置请求信息
        if (Objects.nonNull(request)) {
            String ip = IpUtils.getIpAddress(request);
            operationLog.setIp(ip);
            operationLog.setLocation(IpUtils.getIpLocation(ip));
        }

        // 设置请求参数
        Object[] args = joinPoint.getArgs();
        if (Objects.nonNull(args) && args.length > 0) {
            try {
                operationLog.setParams(JSONUtil.toJsonStr(args));
            } catch (Exception ex) {
                log.warn("序列化请求参数失败：{}", ex.getMessage());
                operationLog.setParams("");
            }
        }

        // 异步保存日志
        operationLogService.saveLog(operationLog);
    }

    /**
     * 获取当前 HttpServletRequest
     *
     * @return HttpServletRequest
     */
    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return Objects.isNull(attributes) ? null : attributes.getRequest();
    }
}
