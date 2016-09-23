package(default_visibility = ["//visibility:public"])

load("@org_pubref_rules_protobuf//go:rules.bzl", "go_proto_library")

filegroup(
    name = "protos",
    srcs = [
        "greetertimer.proto",
    ],
)

go_proto_library(
    name = "go_default_library",
    protos = [
        ":protos",
    ],
    with_grpc = True,
)
