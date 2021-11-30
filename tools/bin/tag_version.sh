#!/usr/bin/env bash

set -e

BRANCH=$(git rev-parse --abbrev-ref HEAD)
if [[ "$BRANCH" != "community" ]]; then
  echo 'Script has to be run from community branch after release_version.sh!';
  exit 1;
fi

[[ ! -z "$(git status --porcelain)" ]] && echo "Outstanding changes in repo!" && exit 1

# rebase with main
git pull --rebase

VERSION=$(cat .env | grep VERSION= | cut -d= -f 2)
[[ -z "$VERSION" ]] && echo "Couldn't find version in env file!" && exit 1

VER_TAG="v$VERSION"
git tag -a "$VER_TAG" -m "version $VERSION"
git push origin "$VER_TAG"
