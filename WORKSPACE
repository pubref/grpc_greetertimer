workspace(name = "org_pubref_grpc_greetertimer")

# ================================================================

git_repository(
    name = "io_bazel_rules_go",
    remote = "https://github.com/bazelbuild/rules_go.git",
    tag = "0.0.4",
)

load("@io_bazel_rules_go//go:def.bzl", "go_repositories")

go_repositories()

# ================================================================

git_repository(
    name = "org_pubref_rules_protobuf",
    commit = "e21c8e3d51d9ecc8942595bb2683fe6d32e17117",
    remote = "https://github.com/pubref/rules_protobuf.git",
)

# local_repository(
#     name = "org_pubref_rules_protobuf",
#     path = "/Users/pcj/github/rules_protobuf",
# )

load("@org_pubref_rules_protobuf//bzl:rules.bzl", "protobuf_repositories")

protobuf_repositories(
    verbose = 0,
    with_go = True,
    with_java = True,
)
