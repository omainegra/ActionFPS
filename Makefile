.PHONY: syslog check-syslog-version clean
travis:
	sbt ignoreWIP test-suite/test
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
	git rev-parse --verify HEAD; \
	SHA=$$(git rev-parse --verify HEAD); \
	git pull origin refs/heads/master; \
	SHA=$$SHA make deploy
deploy:
	changed_files() { \
	    git diff --name-only "$$SHA" "master" \
	    | grep -v -E '\.md' \
	    | grep -v 'web/dist/www'; }; \
	echo Changed files from "$$SHA" to master:; \
	changed_files; \
	if [ $$(changed_files | wc -l) = "0" ]; then \
		make deploy-content; \
	else \
		make deploy-app; \
	fi
deploy-content:
	rsync --delete -av ./web/dist/www/. /home/af/web-5.0/www/.
deploy-app:
	sbt web/dist
	cd /home/af/ && rm -fr /home/af/web-5.0/{conf,lib} && \
		unzip -q -o /home/af/ActionFPS/web/target/universal/web-5.0.zip
	sudo systemctl restart af-web
