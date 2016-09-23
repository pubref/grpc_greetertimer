package(default_visibility = ["//visibility:public"])

load("@org_pubref_rules_protobuf//java:rules.bzl", "java_proto_library", "java_proto_compile")

java_binary(
    name = "server",
    srcs = [
        "GreeterTimerServer.java",
    ],
    main_class = "org.pubref.grpc.greetertimer.GreeterTimerServer",
    runtime_deps = [
        "@org_pubref_rules_protobuf//java:netty_runtime_deps",
    ],
    deps = [
        ":timer_protos",
        "@org_pubref_rules_protobuf//examples/helloworld/proto:java",
        "@org_pubref_rules_protobuf//java:grpc_compiletime_deps",
    ],
)

java_binary(
    name = "client",
    srcs = [
        "GreeterTimerClient.java",
    ],
    main_class = "org.pubref.grpc.greetertimer.GreeterTimerClient",
    runtime_deps = [
        "@org_pubref_rules_protobuf//java:netty_runtime_deps",
    ],
    deps = [
        ":timer_protos",
        "@org_pubref_rules_protobuf//java:grpc_compiletime_deps",
    ],
)

java_proto_library(
    name = "timer_protos",
    protos = [
        "//proto:protos",
    ],
    verbose = 1,
    with_grpc = True,
)
