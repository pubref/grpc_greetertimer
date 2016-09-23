workspace(name = "org_pubref_grpc_greetertimer")

git_repository(
    name = "io_bazel_rules_dotnet",
    commit = "b23e796dd0be27f35867590309d79ffe278d4eeb",
    remote = "https://github.com/pcj/rules_dotnet.git",
)

load("@io_bazel_rules_dotnet//dotnet:csharp.bzl", "csharp_repositories")

csharp_repositories(use_local_mono = True)

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
     commit = "f95606f514a4ca919473f74d8a6e8f9a699c6809",
     remote = "https://github.com/pubref/rules_protobuf.git",
)

load("@org_pubref_rules_protobuf//go:rules.bzl", "go_proto_repositories")

go_proto_repositories()

load("@org_pubref_rules_protobuf//java:rules.bzl", "java_proto_repositories")

java_proto_repositories()
