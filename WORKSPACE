workspace(name = "org_pubref_grpc_greetertimer")

# This is necessary as the java proto target pulls in rules_protobuf
# example/helloworld/proto:BUILD file that has a reference to
# csharp_proto_library.  csharp target aren't actually used in this
# repo directly.
git_repository(
    name = "io_bazel_rules_dotnet",
    commit = "f8950cbf9456df79920514325c17139355d13671",
    remote = "https://github.com/bazelbuild/rules_dotnet.git",
)

load("@io_bazel_rules_dotnet//dotnet:csharp.bzl", "csharp_repositories")

csharp_repositories(use_local_mono = True)

git_repository(
    name = "org_pubref_rules_node",
    commit = "d93a80ac4920c52da8adccbca66a3118a27018fd",  # Oct 2, 2016
    remote = "https://github.com/pubref/rules_node.git",
)

load("@org_pubref_rules_node//node:rules.bzl", "node_repositories")

node_repositories()

# ================================================================

http_archive(
    name = "io_bazel_rules_closure",
    sha256 = "59498e75805ad8767625729b433b9409f80d0ab985068d513f880fc1928eb39f",
    strip_prefix = "rules_closure-0.3.0",
    url = "http://bazel-mirror.storage.googleapis.com/github.com/bazelbuild/rules_closure/archive/0.3.0.tar.gz",
)

load("@io_bazel_rules_closure//closure:defs.bzl", "closure_repositories")

closure_repositories()

# ================================================================

git_repository(
    name = "io_bazel_rules_go",
    remote = "https://github.com/bazelbuild/rules_go.git",
    tag = "0.2.0",
)

load("@io_bazel_rules_go//go:def.bzl", "go_repositories")

go_repositories()

# ================================================================

git_repository(
    name = "org_pubref_rules_protobuf",
    tag = "v0.6.3",
    remote = "https://github.com/pubref/rules_protobuf.git",
)

load("@org_pubref_rules_protobuf//go:rules.bzl", "go_proto_repositories")

go_proto_repositories()

load("@org_pubref_rules_protobuf//java:rules.bzl", "java_proto_repositories")

java_proto_repositories()
