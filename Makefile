#!/usr/bin/make

default: android ios

.PHONY: android ios
android:
	cd ./android && ./gradlew assemble
ios:
	cd ./ios && xcodebuild
