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

import com.codahale.metrics.jetty9.InstrumentedHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.flipkart.gjex.core.GJEXConfiguration;
import com.flipkart.gjex.core.config.ApiService;
import com.flipkart.gjex.core.config.DashboardService;
import com.flipkart.gjex.core.logging.Logging;
import com.flipkart.gjex.core.setup.Bootstrap;
import com.flipkart.gjex.core.setup.HealthCheckRegistry;
import com.flipkart.gjex.core.web.DashboardResource;
import com.flipkart.gjex.core.web.HealthCheckResource;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import org.apache.commons.configuration.Configuration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.freemarker.FreemarkerMvcFeature;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.inject.Named;
import javax.inject.Singleton;
import java.net.URI;
import java.net.URL;

/**
 * <code>DashboardModule</code> is a Guice {@link AbstractModule} implementation used for wiring GJEX Dashboard components.
 * 
 * @author regunath.balasubramanian
 */
public class DashboardModule extends AbstractModule implements Logging {

	private final Bootstrap bootstrap;

	public DashboardModule(Bootstrap bootstrap) {
		this.bootstrap = bootstrap;
	}

	/**
	 * Creates the Jetty server instance for the admin Dashboard and configures it with the @Named("DashboardContext").
	 *
	 * @return Jetty Server instance
	 */
	@Named("DashboardJettyServer")
	@Provides
	@Singleton
	Server getDashboardJettyServer(@Named("DashboardResourceConfig") ResourceConfig resourceConfig,
								   GJEXConfiguration gjexConfiguration, ObjectMapper objectMapper,
								   @Named("GlobalConfig") Configuration configuration) {
		DashboardService dashboardService = gjexConfiguration.getDashboardService();
		int acceptorThreads = configuration.getInt("Dashboard.acceptors");
		int port = dashboardService.getPort();
		int selectorThreads = dashboardService.getSelectors();
		int maxWorkerThreads = dashboardService.getWorkers();
		JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
		provider.setMapper(objectMapper);
		resourceConfig.register(provider);
		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setMaxThreads(maxWorkerThreads);
		Server server = new Server(threadPool);
		ServerConnector http = new ServerConnector(server, acceptorThreads, selectorThreads);
		http.setPort(port);
		server.addConnector(http);

		/** Initialize the Context and Servlet for serving static content */
		URL webRootLocation = this.getClass().getResource("/webroot/pages/dashboard.ftl");
		if (webRootLocation == null) {
			warn("Webroot location not found! Unable to find root location for Dashboard.");
		}
		ServletContextHandler context = new ServletContextHandler();
		try {
			URI webRootUri = URI
					.create(webRootLocation.toURI().toASCIIString().replaceFirst("/pages/dashboard.ftl$", "/"));
			context.setContextPath("/");
			context.setBaseResource(Resource.newResource(webRootUri));
			context.addServlet(DefaultServlet.class, "/");
		} catch (Exception e) {
			error("Unable to set resource base for Dashboard.", e);
		}
		context.getMimeTypes().addMimeMapping("txt", "text/plain;charset=utf-8");
		server.setHandler(context);

		/** Add the Servlet for serving the Dashboard resource */
		ServletHolder servlet = new ServletHolder(new ServletContainer(resourceConfig));
		context.addServlet(servlet, "/admin/*");

		/** Add the Hystrix metrics stream servlets */
		context.addServlet(HystrixMetricsStreamServlet.class, "/stream/hystrix.stream.command.local");
		context.addServlet(HystrixMetricsStreamServlet.class, "/stream/hystrix.stream.global");
		context.addServlet(HystrixMetricsStreamServlet.class, "/stream/hystrix.stream.tp.local");

		/** Add the Metrics instrumentation */
		final InstrumentedHandler handler = new InstrumentedHandler(this.bootstrap.getMetricRegistry());
		handler.setHandler(context);
		server.setHandler(handler);

		server.setStopAtShutdown(true);
		return server;
	}

	/**
	 * Creates the Jetty server instance for the GJEX API endpoint.
	 * @return Jetty Server instance
	 */
	@Named("APIJettyServer")
	@Provides
	@Singleton
	Server getAPIJettyServer(@Named("HealthCheckResourceConfig") ResourceConfig resourceConfig, GJEXConfiguration configuration,
							 ObjectMapper objectMapper) {
		ApiService apiService = configuration.getApiService();
		int acceptorThreads = apiService.getAcceptors();
		int port = apiService.getPort();
		int selectorThreads = apiService.getSelectors();
		int maxWorkerThreads = apiService.getWorkers();
		JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
		provider.setMapper(objectMapper);
		resourceConfig.register(provider);
		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setMaxThreads(maxWorkerThreads);
		Server server = new Server(threadPool);

		ServerConnector http = new ServerConnector(server, acceptorThreads, selectorThreads);
		http.setPort(port);
		server.addConnector(http);
		ServletContextHandler context = new ServletContextHandler(server, "/*");
		ServletHolder servlet = new ServletHolder(new ServletContainer(resourceConfig));
		context.addServlet(servlet, "/*");
		context.setAttribute(HealthCheckRegistry.HEALTHCHECK_REGISTRY_NAME, this.bootstrap.getHealthCheckRegistry());

		final InstrumentedHandler handler = new InstrumentedHandler(this.bootstrap.getMetricRegistry());
		handler.setHandler(context);
		server.setHandler(handler);

		server.setStopAtShutdown(true);
		return server;
	}

	@Named("HealthCheckResourceConfig")
	@Singleton
	@Provides
	public ResourceConfig getAPIResourceConfig(HealthCheckResource healthCheckResource) {
		ResourceConfig resourceConfig = new ResourceConfig();
		resourceConfig.register(healthCheckResource);
		return resourceConfig;
	}

	@Named("DashboardResourceConfig")
	@Singleton
	@Provides
	public ResourceConfig getDashboardResourceConfig(DashboardResource dashboardResource) {
		ResourceConfig resourceConfig = new ResourceConfig();
		resourceConfig.register(dashboardResource);
		resourceConfig.property(FreemarkerMvcFeature.TEMPLATES_BASE_PATH, "webroot/pages");
		resourceConfig.register(FreemarkerMvcFeature.class);
		return resourceConfig;
	}

}
