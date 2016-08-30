package org.pubref.grpc.greetertimer;

import com.google.common.base.Preconditions;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.pubref.rules_protobuf.examples.helloworld.GreeterGrpc;
import org.pubref.rules_protobuf.examples.helloworld.HelloRequest;
import org.pubref.rules_protobuf.examples.helloworld.HelloReply;

/**
 * Server that responds to timer check request and reports aggregated
 * responses.
 */
public class GreeterTimerServer {

  /* The port on which the server should run */
  private final int port;
  private Server server;

  public GreeterTimerServer(int port) {
    this.port = port;
  }

  public void start() throws IOException {
    server = ServerBuilder.forPort(port).addService(new GreeterTimerImpl()).build().start();
    System.out.println("GreeterTimerServer started, listening on port " + port);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override
              public void run() {
                GreeterTimerServer.this.stop();
              }
            });
  }

  public void stop() {
    System.out.println("GreeterTimerServer stopping...");

    if (server != null) {
      server.shutdown();
    }
  }

  /**
   * Await termination on the main thread since the grpc library uses daemon threads.
   */
  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  /**
   * Main launches the server from the command line.
   */
  public static void main(String[] args) throws IOException, InterruptedException {
    final GreeterTimerServer server = new GreeterTimerServer(50053);
    server.start();
    server.blockUntilShutdown();
  }

  private class BatchGreeterClient implements Runnable {

    private final TimerRequest request;
    private final StreamObserver<BatchResponse> observer;
    private final ManagedChannel channel;
    private final GreeterGrpc.GreeterBlockingStub blockingStub;

    /** Construct client connecting to HelloWorld server at {@code host:port}. */
    BatchGreeterClient(TimerRequest request, StreamObserver<BatchResponse> observer) {
      Preconditions.checkNotNull(request, "request required");
      Preconditions.checkNotNull(observer, "response observer required");
      Preconditions.checkArgument(request.getHost().length() > 0, "hostname required");
      Preconditions.checkArgument(request.getPort() > 0, "grpc port required");
      Preconditions.checkArgument(port > 0, "grpc port must be greater than zero");
      Preconditions.checkArgument(
          request.getTotalSize() > 0, "total request count must be greater than zero");
      Preconditions.checkArgument(
          request.getBatchSize() > 0, "batch request size must be greater than zero");

      this.request = request;
      this.observer = observer;

      this.channel =
          ManagedChannelBuilder.forAddress(request.getHost(), request.getPort())
              .usePlaintext(true)
              .build();

      this.blockingStub = GreeterGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
      channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /** Aggegate total time for all tests. */
    public void run() {
      int remaining = request.getTotalSize();
      int batchSize = request.getBatchSize();
      int batchCount = 0;
      int errCount = 0;
      long startTime = System.currentTimeMillis();

      while (remaining-- > 0) {
        try {
          if (batchCount++ == batchSize) {
            respond(remaining, batchCount, errCount, startTime);
            batchCount = 0;
            errCount = 0;
            startTime = System.currentTimeMillis();
          }
          blockingStub.sayHello(HelloRequest.newBuilder().setName("#" + remaining).build());
        } catch (StatusRuntimeException e) {
          errCount++;
          System.err.println("RPC failed: " + e.getStatus());
        }
      }

      if (batchCount < batchSize) {
        respond(remaining, batchCount, errCount, startTime);
      }

      try {
        shutdown();
      } catch (InterruptedException iex) {
        throw new RuntimeException(iex);
      }
    }

    private void respond(int remaining, int batchCount, int errCount, long startTime) {
      long endTime = System.currentTimeMillis();
      long batchTime = endTime - startTime;
      BatchResponse response =
          BatchResponse.newBuilder()
              .setRemaining(remaining)
              .setBatchCount(batchCount)
              .setBatchTimeMillis(batchTime)
              .setErrCount(errCount)
              .build();
      observer.onNext(response);
    }
  }

  private class GreeterTimerImpl extends GreeterTimerGrpc.GreeterTimerImplBase {

    @Override
    public void timeGreetings(TimerRequest req, StreamObserver<BatchResponse> observer) {
      try {
        System.out.println("TimerRequest recvd: " + req);
        new BatchGreeterClient(req, observer).run();
      } catch (RuntimeException rex) {
        observer.onError(rex);
      } finally {
        observer.onCompleted();
      }
    }
  }
}
