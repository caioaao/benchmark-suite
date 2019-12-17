.PHONY: clean
clean:
	rm -rf target/
build: clean
	clj -A:pack
.PHONY: install
install: build
	mvn org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file -Dfile=target/benchmark-suite.jar -DpomFile=pom.xml
