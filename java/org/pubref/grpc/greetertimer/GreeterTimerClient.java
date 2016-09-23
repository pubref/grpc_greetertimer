package org.pubref.grpc.greetertimer;

import com.google.common.base.Preconditions;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A client that submits requests to the GreeterTimerServer and
 * reports the aggregated results.
 */
public class GreeterTimerClient {
  private static final Logger logger = Logger.getLogger(GreeterTimerClient.class.getName());

  private final ManagedChannel channel;
  private final GreeterTimerGrpc.GreeterTimerStub stub;

  /** Construct client connecting to GreeterTimer server at {@code host:port}. */
  public GreeterTimerClient(String host, int port) {
    this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build();
    this.stub = GreeterTimerGrpc.newStub(channel);
  }

  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  /**
   * Fire an rpc request to the GreeterTimerServer.
   */
  public void submit(TimerRequest request) throws InterruptedException {

    final CountDownLatch latch = new CountDownLatch(1);

    StreamObserver<BatchResponse> responseObserver =
        new StreamObserver<BatchResponse>() {
          @Override
          public void onNext(BatchResponse resp) {
            int count = resp.getBatchCount();
            long time = resp.getBatchTimeMillis();
            double rate = ((double) count) / time;
            float us = (time / ((float) count)) * 1000;
            info(
                "{0} requests + {4} errors in {1}ms ({2} calls/ms q {3}\u00B5s), {5} more to go...",
                count,
                time,
                rate,
                us,
                resp.getErrCount(),
                resp.getRemaining());
          }

          @Override
          public void onError(Throwable t) {
            t.printStackTrace();
            Status status = Status.fromThrowable(t);
            warn("TimerRequest Failed: {0}", status);
            latch.countDown();
          }

          @Override
          public void onCompleted() {
            info("TimerRequest Complete.");
            latch.countDown();
          }
        };

    stub.timeGreetings(request, responseObserver);

    latch.await();
  }

  private static void info(String msg, Object... params) {
    logger.log(Level.INFO, msg, params);
  }

  private static void warn(String msg, Object... params) {
    logger.log(Level.WARNING, msg, params);
  }

  /**
   * Print usage message and exit(1)
   */
  private static void usage() {
    System.err.println("Usage: java -jar {0} OPTIONS");
    System.err.println(" --timer_host HOSTNAME (GreeterTimerServer host, default: localhost)");
    System.err.println(" --timer_port PORT_NUM (GreeterTimerServer port, default: 50053)");
    System.err.println(" --greeter_host HOSTNAME (GreeterWorldServer host, default: localhost)");
    System.err.println(" --greeter_port PORT_NUM (GreeterWorldServer port, default: 50051)");
    System.err.println(" --total NUMBER (total number of rpc calls to perform, default: 10000)");
    System.err.println(" --batch NUMBER (rpc call threshold for server response, default: 1000)");
    System.exit(1);
  }

  /**
   * Utility for command line parsing.
   */
  private static int intArg(String[] args, int i) {
    Preconditions.checkElementIndex(i, args.length, "Missing argument for " + args[i - 1]);
    try {
      int val = Integer.parseInt(args[i]);
      if (val <= 0) {
        throw new IllegalArgumentException(
            "Bad argument for " + args[i - 1] + ": expected positive integer but found " + args[i]);
      }
      return val;
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException(
          "Bad argument for " + args[i - 1] + ": expected integer but found " + args[i]);
    }
  }

  /**
   * Utility for command line parsing.
   */
  private static String stringArg(String[] args, int i) {
    Preconditions.checkElementIndex(i, args.length, "Missing argument for " + args[i - 1]);
    return args[i];
  }

  /**
   * Parse command line options and run timer client.
   */
  public static void execute(String[] args) throws Exception {

    String timerHost = "localhost";
    int timerPort = 50053;

    TimerRequest.Builder builder =
        TimerRequest.newBuilder()
            .setHost("localhost")
            .setPort(50051)
            .setTotalSize(10000)
            .setBatchSize(1000);

    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      if ("--".equals(arg)) {
        break;
      } else if ("--timer_host".equals(arg)) {
        timerHost = stringArg(args, ++i);
      } else if ("--timer_port".equals(arg)) {
        timerPort = intArg(args, ++i);
      } else if ("--greeter_host".equals(arg)) {
        builder.setHost(stringArg(args, ++i));
      } else if ("--greeter_port".equals(arg)) {
        builder.setPort(intArg(args, ++i));
      } else if ("--total".equals(arg)) {
        builder.setTotalSize(intArg(args, ++i));
      } else if ("--batch".equals(arg)) {
        builder.setBatchSize(intArg(args, ++i));
      } else {
        throw new IllegalArgumentException("Unknown argument: " + args[i]);
      }
    }

    GreeterTimerClient client = new GreeterTimerClient(timerHost, timerPort);

    try {
      client.submit(builder.build());
    } finally {
      client.shutdown();
    }
  }

  public static void main(String[] args) {
    if (System.getProperty("java.util.logging.SimpleFormatter.format") == null) {
      System.setProperty(
          "java.util.logging.SimpleFormatter.format", "%1$tF %1$tT [%4$s] %5$s%6$s%n");
    }
    try {
      execute(args);
    } catch (Exception ex) {
      System.err.println("Failed: " + ex.getMessage());
      usage();
    }
  }
}
