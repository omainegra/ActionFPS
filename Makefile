.PHONY: \
	default \
	it \


GEOCITY_DAT := GeoLiteCityv6.dat
GEOCITY_SOURCE := http://geolite.maxmind.com/download/geoip/database/GeoLiteCityv6-beta/GeoLiteCityv6.dat.gz

default: reference-data
	sbt test 'show dist'

travis: reference-data
	sbt clean test it:test 'show dist'

reference-data: \
	journals/games.tsv \
	journals/journal.tsv \
	journals/sample-journal.tsv \
	web/geoip-resources/$(GEOCITY_DAT) \

web/geoip-resources/$(GEOCITY_DAT):
	mkdir -p web/geoip-resources
	curl $(GEOCITY_SOURCE) | gunzip > web/geoip-resources/$(GEOCITY_DAT)

it: reference-data
	sbt it:test

journals/journal.tsv:
	mkdir -p journals
	touch journals/journal.tsv

journals/sample-journal.tsv:
	mkdir -p journals
	wget --continue -O journals/sample-journal.tsv https://gist.github.com/ScalaWilliam/ebff0a56f57a7966a829/raw/732629d6bfb01a39dffe57ad22a54b3bad334019/gistfile1.txt

journals/games.tsv:
	mkdir -p journals
	curl https://actionfps.com/all/ | head -n 500 > journals/games.tsv
