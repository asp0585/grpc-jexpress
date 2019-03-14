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
package com.flipkart.gjex.guice;

import java.util.ArrayList;
import java.util.List;

import com.codahale.metrics.health.HealthCheck;
import com.flipkart.gjex.core.Bundle;
import com.flipkart.gjex.core.Configuration;
import com.flipkart.gjex.core.filter.Filter;
import com.flipkart.gjex.core.logging.Logging;
import com.flipkart.gjex.core.service.Service;
import com.flipkart.gjex.core.setup.Bootstrap;
import com.flipkart.gjex.core.setup.Environment;
import com.flipkart.gjex.core.tracing.TracingSampler;
import com.flipkart.gjex.grpc.service.GrpcServer;
import com.flipkart.gjex.guice.module.ApiModule;
import com.flipkart.gjex.guice.module.ConfigModule;
import com.flipkart.gjex.guice.module.DashboardModule;
import com.flipkart.gjex.guice.module.ServerModule;
import com.flipkart.gjex.guice.module.TaskModule;
import com.flipkart.gjex.guice.module.TracingModule;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.palominolabs.metrics.guice.MetricsInstrumentationModule;

import io.grpc.BindableService;
import ru.vyarus.guice.validator.ImplicitValidationModule;

/**
 * A Guice GJEX Bundle implementation. Multiple Guice Modules may be added to this Bundle.
 * 
 * @author regu.b
 *
 */
public class GuiceBundle<T extends Configuration> implements Bundle<T>, Logging {

	private final List<Module> modules;
	private Injector baseInjector;
	private List<Service> services;
	@SuppressWarnings("rawtypes")
	private List<Filter> filters;
	private List<HealthCheck> healthchecks;
	private List<TracingSampler> tracingSamplers;
	
	public static class Builder<T extends Configuration> {

		private List<Module> modules = Lists.newArrayList();

		public Builder<T> addModules(Module... moreModules) {
			for (Module module : moreModules) {
				Preconditions.checkNotNull(module);
				modules.add(module);
			}
			return this;
		}
		public GuiceBundle build() {
            return new GuiceBundle(this.modules);
        }
	}
	public static Builder newBuilder() {
        return new Builder<>();
    }		
	
	private GuiceBundle(List<Module> modules) {
		Preconditions.checkNotNull(modules);
        Preconditions.checkArgument(!modules.isEmpty());
        this.modules = modules;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void initialize(Bootstrap<?> bootstrap) {
		// add the Config and Metrics MetricsInstrumentationModule
		this.modules.add( new ConfigModule());
		this.modules.add(MetricsInstrumentationModule.builder().withMetricRegistry(bootstrap.getMetricRegistry()).build());
		// add the Validation module
		this.modules.add(new ImplicitValidationModule());
		// add the Api module before Tracing module so that APIs are timed from the start of execution
		this.modules.add(new ApiModule());
		// add the Tracing module before Task module so that even Concurrent tasks can be traced
		this.modules.add(new TracingModule());
		// add the Task module
		this.modules.add(new TaskModule());
		// add the Dashboard module
		this.modules.add(new DashboardModule(bootstrap));
		// add the Grpc Server module
		this.modules.add(new ServerModule());
		this.baseInjector = Guice.createInjector(this.modules);
	}

	@Override
	public void run(T configuration, Environment environment) {
		GrpcServer grpcServer = this.baseInjector.getInstance(GrpcServer.class);
		// Add all Grpc Services to the Grpc Server
		List<BindableService> services = this.getInstances(this.baseInjector, BindableService.class);
		grpcServer.registerServices(services);
		// Add all Grpc Filters to the Grpc Server
		this.filters = this.getInstances(this.baseInjector, Filter.class);
		grpcServer.registerFilters(this.filters, services);
		// Add all Grpc Filters to the Grpc Server
		this.tracingSamplers = this.getInstances(this.baseInjector, TracingSampler.class);
		grpcServer.registerTracingSamplers(this.tracingSamplers, services);
		
		// Lookup all Service implementations
		this.services = this.getInstances(this.baseInjector, Service.class);
		// Lookup all HealthCheck implementations
		this.healthchecks = this.getInstances(this.baseInjector, HealthCheck.class);		
	}	

	@Override
	public List<Service> getServices() {		
        Preconditions.checkState(baseInjector != null,
                "Service(s) are only available after GuiceBundle.run() is called");
		return this.services;
	} 

	@SuppressWarnings("rawtypes")
	@Override
	public List<Filter> getFilters() {		
        Preconditions.checkState(baseInjector != null,
                "Filter(s) are only available after GuiceBundle.run() is called");
		return this.filters;
	} 
	
	@Override
	public List<HealthCheck> getHealthChecks() {		
        Preconditions.checkState(baseInjector != null,
                "HealthCheck(s) are only available after GuiceBundle.run() is called");
		return this.healthchecks;
	} 
	
	@Override
	public List<TracingSampler> getTracingSamplers() {
        Preconditions.checkState(baseInjector != null,
                "TracingSampler(s) is only available after GuiceBundle.run() is called");
        return this.tracingSamplers;
	}
	
	public Injector getInjector() {
        Preconditions.checkState(baseInjector != null,
                "Injector is only available after GuiceBundle.initialize() is called");
        return baseInjector;
    }	
		
    private <S> List<S> getInstances(Injector injector, Class<S> type) {
        List<S> instances = new ArrayList<S>();
        List<Binding<S>> bindings = injector.findBindingsByType(TypeLiteral.get(type));
        for(Binding<S> binding : bindings) {
            Key<S> key = binding.getKey();
            instances.add(injector.getInstance(key));
        }
        return instances;
    }

}
