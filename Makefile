test:
	./gradlew test
	google-chrome gpr-lib/build/reports/tests/index.html

all: test
	./gradlew build

install: build
	./gradlew installDebug

clean:
	./gradlew clean
