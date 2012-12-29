package gov.va.common.aop;

import gov.va.common.aop.annotations.LogExecTime;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.javasimon.Stopwatch;
import org.springframework.stereotype.Component;

/**
 *
 * @author gaineys
 */
@Component
@Aspect
public class MonitorAspect {

    @Around(value = "@annotation(annotation)")
    public Object LogExecutionTime(final ProceedingJoinPoint joinPoint, final LogExecTime annotation) throws Throwable {
        
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        if (method.getDeclaringClass().isInterface()) {
            method = joinPoint.getTarget().getClass().getDeclaredMethod(joinPoint.getSignature().getName(), method.getParameterTypes());
        }

        Split split = SimonManager.getStopwatch(method.getName()).start();
        
        try {
            final Object retVal = joinPoint.proceed();
            return retVal;
        } finally {
            split.stop();
        }
    }
}
