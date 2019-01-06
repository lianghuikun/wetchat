package com.wetchat.wetchat.aop;

import com.alibaba.fastjson.JSON;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Aspect
@Component
public class LoggerAspect {
    public Logger log = LoggerFactory.getLogger(LoggerAspect.class);

    @Pointcut("execution(public * com.wetchat.wetchat.*.*.*(..))")
    public void log() {

    }


    //请求method前打印内容
    @Before(value = "log()")
    public void methodBefore(JoinPoint joinPoint) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();

        //打印请求内容
        log.info("===============请求内容===============");
        log.info("请求地址:" + request.getRequestURL().toString());
        log.info("请求方式:" + request.getMethod());
        log.info("请求类方法:" + joinPoint.getSignature());
        log.info("请求类方法参数:" + Arrays.toString(joinPoint.getArgs()));
        log.info("===============请求内容===============");
    }


    //在方法执行完结后打印返回内容
    @AfterReturning(returning = "o", pointcut = "log()")
    public void methodAfterReturing(Object o) {
        log.info("--------------返回内容----------------");
        if (o != null)
            log.info("Response内容:" + JSON.toJSONString(o));
        else {
            log.info("nothing:");
        }
        log.info("--------------返回内容----------------");
    }

}
