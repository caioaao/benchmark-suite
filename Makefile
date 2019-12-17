install: build
	mvn org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file -Dfile=target/benchmark-suite.jar -DpomFile=pom.xml
build: target/benchmark-suite.jar
.PHONY: clean
target/benchmark-suite.jar:
	clj -A:pack mach.pack.alpha.skinny --no-libs --project-path target/benchmark-suite.jar
clean:
	rm -rf target/
