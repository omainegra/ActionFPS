.PHONY: syslog check-syslog-version clean
travis:
	sbt ignorePHPTests ignoreWIP test-suite/test
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
watch:
	git-watch \
		--url=https://git.watch/github/ScalaWilliam/ActionFPS \
		--push-execute='make push-%ref% || true'
push-refs/heads/master:
	git fetch
	git pull
	sbt web/dist
	cd /home/af/ && rm -fr /home/af/web-5.0/{conf,lib} && \
		unzip -q -o /home/af/ActionFPS/web/target/universal/web-5.0.zip
	sudo systemctl restart af-web
