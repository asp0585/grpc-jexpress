package com.flipkart.grpc.jexpress.service;

import com.flipkart.gjex.core.filter.MethodFilters;
import com.flipkart.gjex.core.logging.Logging;
import com.flipkart.grpc.jexpress.HudsonServiceGrpc;
import com.flipkart.grpc.jexpress.IteratorRequest;
import com.flipkart.grpc.jexpress.IteratorResponse;
import com.flipkart.grpc.jexpress.SampleConfiguration;
import com.flipkart.grpc.jexpress.SmartClassify;
import com.flipkart.grpc.jexpress.filter.GetHudsonLoggingFilter;
import io.grpc.stub.StreamObserver;

import javax.inject.Inject;
import javax.inject.Named;

@Named("HudsonService")
public class HudsonService extends HudsonServiceGrpc.HudsonServiceImplBase implements Logging {

    private final SampleConfiguration sampleConfiguration;

    @Inject
    public HudsonService(SampleConfiguration sampleConfiguration) {
        this.sampleConfiguration = sampleConfiguration;
    }

    @Override
    @MethodFilters({GetHudsonLoggingFilter.class})
    public void iterator(IteratorRequest request,
                         StreamObserver<IteratorResponse> responseObserver) {
        info("anand");
        IteratorResponse response = IteratorResponse.newBuilder().setSmartClassify(SmartClassify.newBuilder().setName("@anand").build()).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
