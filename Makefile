fmt:
	buildifier WORKSPACE
	find . -name BUILD | xargs buildifier
	go fmt go/main.go
	find java -name '*.java' | xargs java -jar ~/bin/google-java-format-0.1-alpha.jar --replace
