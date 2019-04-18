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
package com.flipkart.gjex.hibernate.config;

import com.flipkart.gjex.hibernate.SessionFactoryContext;
import com.flipkart.gjex.hibernate.internal.SessionFactoryContextImpl;
import com.flipkart.gjex.hibernate.internal.SessionFactoryManager;
import com.flipkart.gjex.hibernate.internal.TransactionInterceptor;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import org.hibernate.SessionFactory;

import javax.transaction.Transactional;
import java.util.Map;

public class HibernateModule extends AbstractModule {

    @Override
    protected void configure() {
        final TransactionInterceptor txnInterceptor = new TransactionInterceptor();
        requestInjection(txnInterceptor);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), txnInterceptor);
        bindInterceptor(Matchers.annotatedWith(Transactional.class), Matchers.any(), txnInterceptor);
    }

    @Singleton
    @Provides
    public SessionFactoryContext providesSessionFactoryContext() {
        Map<String, SessionFactory> sessionFactories = SessionFactoryManager.getInstance().getAllSessionFactories();
        return new SessionFactoryContextImpl(sessionFactories);
    }
}
