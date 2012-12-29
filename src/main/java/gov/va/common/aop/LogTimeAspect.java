package gov.va.common.aop;

import gov.va.common.aop.annotations.LogExecTime;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 *
 * @author gaineys
 */
@Component
@Aspect
public class LogTimeAspect {

    @Around(value = "@annotation(annotation)")
    public Object LogExecutionTime(final ProceedingJoinPoint joinPoint, final LogExecTime annotation) throws Throwable {
        final long startMillis = System.currentTimeMillis();
        try {
//            System.out.println("Starting timed operation");
            final Object retVal = joinPoint.proceed();
            return retVal;
        } finally {
            final long duration = System.currentTimeMillis() - startMillis;
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            if (method.getDeclaringClass().isInterface()) {
                method = joinPoint.getTarget().getClass().getDeclaredMethod(joinPoint.getSignature().getName(), method.getParameterTypes());
            }
//            System.out.println("Call to " + method.getDeclaringClass().getName() + "." + method.getName() + " took " + duration + " ms");

        }
    }
}
