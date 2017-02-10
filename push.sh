#!/bin/bash

push() {
  git rev-parse --verify HEAD
  SHA=$(git rev-parse --verify HEAD)
  git pull origin refs/heads/master
  SHA=$SHA make deploy
}

changed_files() { \
  git diff --name-only "$SHA" "master" \
    | grep -v -E '\.md' \
    | grep -v 'web/dist/www'
}

deploy() {
  echo Changed files from "$SHA" to master:
  changed_files
  if [[ $(changed_files) == "" ]]; then
    make deploy-content;
  else
    make deploy-app;
  fi
}

deploy_content() {
  rsync --filter 'protect /home/af/web-5.0/www/assets/bower_components/' \
    --delete -av ./web/dist/www/. /home/af/web-5.0/www/.
  cd /home/af/web-5.0/www/assets/ && bower -f install
}

deploy_app() {
  sbt web/dist
  cd /home/af/ && rm -fr /home/af/web-5.0/{conf,lib,www,bin} && \
    unzip -q -o /home/af/ActionFPS/web/target/universal/web-5.0.zip
  sudo systemctl restart af-web
  cd /home/af/web-5.0/www/assets/ && bower -f install
}

git fetch
if [[ $(git diff --name-only origin/master) != "" ]]; then push; fi

