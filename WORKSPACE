workspace(name = "org_pubref_grpc_greetertimer")

# This is necessary as the java proto target pulls in rules_protobuf
# example/helloworld/proto:BUILD file that has a reference to
# csharp_proto_library.  csharp target aren't actually used in this
# repo directly.
git_repository(
    name = "io_bazel_rules_dotnet",
    commit = "1a6ca96fe05bca83782464453ac4657fb8ed8379",
    remote = "https://github.com/bazelbuild/rules_dotnet.git",
)

load("@io_bazel_rules_dotnet//dotnet:csharp.bzl", "csharp_repositories")

csharp_repositories(use_local_mono = True)

git_repository(
    name = "org_pubref_rules_node",
    commit = "1c60708c599e6ebd5213f0987207a1d854f13e23",  # Mar 12, 2018
    remote = "https://github.com/pubref/rules_node.git",
)

load("@org_pubref_rules_node//node:rules.bzl", "node_repositories")

node_repositories()

# ================================================================

http_archive(
    name = "io_bazel_rules_closure",
    sha256 = "b29a8bc2cb10513c864cb1084d6f38613ef14a143797cea0af0f91cd385f5e8c",
    strip_prefix = "rules_closure-0.8.0",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/rules_closure/archive/0.8.0.tar.gz",
        "https://github.com/bazelbuild/rules_closure/archive/0.8.0.tar.gz",
    ],
)
load("@io_bazel_rules_closure//closure:defs.bzl", "closure_repositories")
closure_repositories()

# ================================================================

git_repository(
    name = "io_bazel_rules_go",
    remote = "https://github.com/bazelbuild/rules_go.git",
    tag = "0.10.3",
)

load("@io_bazel_rules_go//go:def.bzl", "go_rules_dependencies", "go_register_toolchains")
go_rules_dependencies()
go_register_toolchains()

# ================================================================

git_repository(
    name = "org_pubref_rules_protobuf",
    #tag = "v0.8.2",
    commit = "5cae42382b620aa1e347ecf30b3e92fd0d97998c", # Jun 23, 2018
    remote = "https://github.com/pubref/rules_protobuf.git",
)

load("@org_pubref_rules_protobuf//go:rules.bzl", "go_proto_repositories")

go_proto_repositories()

load("@org_pubref_rules_protobuf//java:rules.bzl", "java_proto_repositories")

java_proto_repositories()
