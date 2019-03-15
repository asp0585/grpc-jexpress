package com.flipkart.grpc.jexpress;

import com.flipkart.gjex.core.Application;
import com.flipkart.gjex.core.setup.Bootstrap;
import com.flipkart.gjex.core.setup.Environment;
import com.flipkart.gjex.guice.GuiceBundle;
import com.flipkart.grpc.jexpress.module.SampleModule;

public class SampleApplication extends Application<SampleConfiguration> {

    @Override
    public String getName() {
        return "Sample JExpress Application";
    }

    @Override
    public void run(SampleConfiguration configuration, Environment environment) throws Exception {

    }

    @Override
    public void initialize(Bootstrap<SampleConfiguration> bootstrap) {
        SampleModule sampleModule= new SampleModule();
        GuiceBundle<SampleConfiguration> guiceBundle = GuiceBundle.newBuilder()
                .addModules(sampleModule)
                .build();
        bootstrap.addBundle(guiceBundle);
    }

    public static void main(String [] args) throws Exception {
        SampleApplication app = new SampleApplication();
        app.run(args);
    }
}
