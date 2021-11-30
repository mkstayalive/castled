#!/usr/bin/env bash

set -e

if [[ -z "${DOCKER_USER}" ]]; then
  echo 'DOCKER_USER not set!';
  exit 1;
fi

if [[ -z "${DOCKER_PASSWORD}" ]]; then
  echo 'DOCKER_PASSWORD not set!';
  exit 1;
fi

docker login -u "$DOCKER_USER" -p "$DOCKER_PASSWORD"

OLD_VERSION=$(grep VERSION .env | cut -d"=" -f2)

[[ -z "$PART_TO_BUMP" ]] && echo "Usage ./tools/bin/release_version.sh (major|minor|patch)" && exit 1

# .bumpversion.cfg has all the files with version number
pip install bumpversion
bumpversion "$PART_TO_BUMP"
git status

NEW_VERSION=$(grep VERSION .env | cut -d"=" -f2)
GIT_REVISION=$(git rev-parse HEAD)
[[ -z "$GIT_REVISION" ]] && echo "Couldn't get the git revision..." && exit 1

echo "Version bumped from ${OLD_VERSION} to ${NEW_VERSION}"
echo "Building and publishing docker images $NEW_VERSION for git revision $GIT_REVISION..."

VERSION=$NEW_VERSION GIT_REVISION=$GIT_REVISION docker-compose -f docker-compose.build.yaml build
VERSION=$NEW_VERSION GIT_REVISION=$GIT_REVISION docker-compose -f docker-compose.build.yaml push
echo "Completed building and publishing images..."
docker logout
