.PHONY: syslog check-syslog-version clean
travis:
	sbt test it:test
default:
	sbt web/dist
clean:
	sbt clean
syslog: check-syslog-version
	sbt "project syslog-ac" 'set version := "'$(SYSLOG_VERSION)'"' "show rpm:packageBin"
check-syslog-version:
ifndef SYSLOG_VERSION
	  	$(error SYSLOG_VERSION is undefined)
endif
