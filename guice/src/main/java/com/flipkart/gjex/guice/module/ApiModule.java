/*
 * Copyright (c) The original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flipkart.gjex.guice.module;

import com.flipkart.gjex.core.logging.Logging;
import com.flipkart.gjex.core.service.Api;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matchers;
import io.grpc.BindableService;
import io.grpc.Context;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.configuration.Configuration;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A Guice {@link AbstractModule} for managing interception of methods annotated with {@link Api}
 * @author regu.b
 *
 */
public class ApiModule<T> extends AbstractModule implements Logging {
	
	@Override
    protected void configure() {
		ApiMethodInterceptor methodInterceptor = new ApiMethodInterceptor();
		requestInjection(methodInterceptor);
		bindInterceptor(Matchers.any(), new ApiMethodMatcher(), methodInterceptor);
	}
	
	@Named("ApiScheduledExecutor")
	@Provides
	@Singleton
//	ScheduledExecutorService getScheduledExecutorService(@Named("Api.scheduledExecutorThreadPoolSize")int threadpoolSize) {
	ScheduledExecutorService getScheduledExecutorService(com.flipkart.gjex.core.Configuration configuration) {
		return Executors.newScheduledThreadPool(configuration.getApiService().getScheduledExecutorThreadPoolSize());
	}
	
	class ApiMethodInterceptor implements MethodInterceptor {
		
		@Inject
		@Named("GlobalConfig")
		Configuration globalConfig;
		
		@Inject
		@Named("ApiScheduledExecutor")
		ScheduledExecutorService scheduledExecutorService;
		
		@Override
		public Object invoke(MethodInvocation invocation) throws Throwable {
			Context.CancellableContext cancellableContext = null;
			Context previous = null;
			Api api = invocation.getMethod().getAnnotation(Api.class);
			/*
			 * We explicitly set the deadline only when it is specified on the stub method AND there is none specified by the client.
			 * Client deadline wins over any server specified deadline
			 */
			if (api != null) { 
				String methodInvoked = (invocation.getMethod().getDeclaringClass().getSimpleName() + "." + invocation.getMethod().getName()).toLowerCase();
				// check and warn if Api is used for non BindableService classes
				if (!BindableService.class.isAssignableFrom(invocation.getMethod().getDeclaringClass())) {
					warn("Api declarations are interpreted only for sub-types of gRPC BindableService. Api declared for : " 
							+ methodInvoked + " will not be interpreted/honored");
				}
				int deadline = 0;
				if (api.deadlineConfig().length() > 0) { // check if deadline is specified as a config property
					deadline = globalConfig.getInt(api.deadlineConfig());
				}
				if (Context.current().getDeadline() == null) {
					cancellableContext = Context.current().withDeadlineAfter(deadline, TimeUnit.MILLISECONDS, scheduledExecutorService);
					previous = cancellableContext.attach(); // attach the CancellableContext and store the previous Context
				} else {
					info("Not setting API deadline as client has already specified a deadline");
				}
			}
			Object result = null;
			try {
				result = invocation.proceed();
			} finally {
				if (cancellableContext != null) {
					cancellableContext.detach(previous); // Detach the CancellableContext and restore the previous Context
					debug("Cancelled a cancellable context");
					cancellableContext.cancel(null); // we cancel the cancellable context with null (no error)
				}
			}
			return result;
		}
	}
	
	/**
	 * The Matcher that matches methods with the {@link ConcurrentApi} annotation
	 */
	class ApiMethodMatcher extends AbstractMatcher<Method> {
		@Override
	    public boolean matches(final Method method) {
	        boolean matches = false;
	        for (Annotation ann : method.getAnnotations()) {
	            final Class<? extends Annotation> annotationType = ann.annotationType();
	            if (Api.class.equals(annotationType)) {
	                matches = true;
	                break;
	            }
	        }
	        return matches;
	    }
	}	
}
