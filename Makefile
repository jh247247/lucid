


all:
	./gradlew build

test:
	-./gradlew test
	google-chrome gpr-lib/build/reports/tests/index.html

install: build
	./gradlew installDebug

clean:
	./gradlew clean
