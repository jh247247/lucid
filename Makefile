

.DEFAULT_GOAL := install

export JAVA8_HOME=/usr/lib/jvm/java-8-openjdk-amd64
export JAVA_HOME=$(readlink -f /usr/bin/javac | sed "s:/bin/javac::")

EMULATOR := /opt/android/sdk/tools/emulator
AVD-DEVICE := Marshmallow-dev
AVD-RUNNING := $(shell ps aux | grep -v "grep" | grep $(AVD-DEVICE))

build:
	./gradlew build

test:
	-./gradlew test
	google-chrome gpr-lib/build/reports/tests/index.html

install: build
	./gradlew installDebug

clean:
	./gradlew clean

start-avd:
ifeq ($(strip $(AVD-RUNNING)),)
	@echo "Emulator not running! Starting..."
	@$(EMULATOR) -avd $(AVD-DEVICE) -gpu on &\
else
	@echo "Emulator already running!"
endif
