package(default_visibility = ["//visibility:public"])

load("@org_pubref_rules_protobuf//bzl:java/rules.bzl", "java_proto_library", "java_proto_compile")
load("@org_pubref_rules_protobuf//bzl:go/rules.bzl", "go_proto_library", "go_proto_compile")
load(
    "@org_pubref_rules_protobuf//bzl:java/class.bzl",
    JAVA = "CLASS",
)

java_binary(
    name = "server",
    srcs = [
        "GreeterTimerServer.java",
    ],
    main_class = "org.pubref.grpc.greetertimer.GreeterTimerServer",
    runtime_deps = [
    ] + JAVA.grpc.netty_runtime_deps,
    deps = [
        ":timer_protos",
        "@org_pubref_rules_protobuf//examples/helloworld/proto:java",
    ] + JAVA.grpc.compile_deps,
)

java_binary(
    name = "client",
    srcs = [
        "GreeterTimerClient.java",
    ],
    main_class = "org.pubref.grpc.greetertimer.GreeterTimerClient",
    runtime_deps = [
    ] + JAVA.grpc.netty_runtime_deps,
    deps = [
        ":timer_protos",
    ] + JAVA.grpc.compile_deps,
)

java_proto_library(
    name = "timer_protos",
    protos = [
        "//proto:protos",
    ],
    with_grpc = True,
)
