package(default_visibility = ["//visibility:public"])

load("@io_bazel_rules_go//go:def.bzl", "go_binary")
load(
    "@org_pubref_rules_protobuf//bzl:go/class.bzl",
    GO = "CLASS",
)

go_binary(
    name = "client",
    srcs = [
        "main.go",
    ],
    deps = [
        "//proto:go_default_library",
    ] + GO.grpc.compile_deps,
)
