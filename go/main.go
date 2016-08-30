package main

import (
	"flag"
	"fmt"
	gt "github.com/pubref/grpc_greetertimer/proto"
	"golang.org/x/net/context"
	"google.golang.org/grpc"
	"io"
	"log"
)

func connect(timerHost *string, timerAddress *int) (gt.GreeterTimerClient, *grpc.ClientConn, error) {
	address := fmt.Sprintf("%s:%d", *timerHost, *timerAddress)
	conn, err := grpc.Dial(address, grpc.WithInsecure())
	if err != nil {
		log.Fatalf("did not connect: %v", err)
		return nil, nil, err
	}
	client := gt.NewGreeterTimerClient(conn)
	return client, conn, nil
}

func submit(client gt.GreeterTimerClient, request *gt.TimerRequest) error {
	stream, err := client.TimeGreetings(context.Background(), request)
	if err != nil {
		log.Fatalf("could not submit request: %v", err)
	}

	for {
		batchResponse, err := stream.Recv()
		if err == io.EOF {
			log.Printf("EOF: %s", batchResponse)
			return nil
		}
		if err != nil {
			log.Fatalf("bad batch recv: %v", err)
			return err
		}
		reportBatchResult(batchResponse)
	}
}

func reportBatchResult(b *gt.BatchResponse) {
	time := float32(b.BatchTimeMillis)
	count := float32(b.BatchCount)
	rate := count / time
	q := (time / count) * 1000
	fmt := "%d hellos (%d errs, %d remaining): %.1f hellos/ms or ~%.0f\u00B5s per hello"
	log.Printf(fmt, b.BatchCount, b.ErrCount, b.Remaining, rate, q)
}

func main() {
	timerHost := flag.String("timer_host", "localhost",
		"hostname where greeterTimer service is running")
	timerPort := flag.Int("timer_port", 50053,
		"port where greeterTimer service is running")
	helloHost := flag.String("hello_host", "localhost",
		"hostname where hello service is running")
	helloPort := flag.Int("hello_port", 50051,
		"port where hello service is running")
	totalSize := flag.Int("total_size", 10000,
		"total number of messages size")
	batchSize := flag.Int("batch_size", 1000,
		"stream batch size")

	flag.Parse()

	timerRequest := &gt.TimerRequest{
		Host:      *helloHost,
		Port:      int32(*helloPort),
		TotalSize: int32(*totalSize),
		BatchSize: int32(*batchSize),
	}

	client, conn, err := connect(timerHost, timerPort)
	defer conn.Close()

	if err != nil {
		log.Fatalf("did not connect: %v", err)
	}

	submit(client, timerRequest)
}
