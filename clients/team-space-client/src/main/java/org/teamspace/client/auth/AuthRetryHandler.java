package org.teamspace.client.auth;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.teamspace.client.common.BaseClient;

/**
 * Created by shpilb on 08/04/2017.
 */
@Aspect
public class AuthRetryHandler {

    @Around("@annotation(org.teamspace.client.auth.Retryable)")
    public Object executeOperation(ProceedingJoinPoint pjp) throws Throwable {
            try {
                return pjp.proceed();
            }
            catch(Exception ex) {
                return handleAuthAndRetry(pjp);
            }
    }

    Object handleAuthAndRetry(ProceedingJoinPoint pjp) throws Throwable {
        BaseClient baseClient = (BaseClient)pjp.getTarget();
        baseClient.acquireToken();
        return pjp.proceed();
    }
}
