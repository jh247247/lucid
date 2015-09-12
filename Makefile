

.DEFAULT_GOAL := install


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
