#!/usr/bin/env bash

# This is a hack to get around the issue mentioned here => https://github.com/vercel/next.js/issues/21888.
# We would want the flexibility to inject API_BASE_URL env variable at runtime to our nextjs webapp.

set -e

DEFAULT_API_BASE_URL=$(grep API_BASE_URL .buildenv | cut -d'=' -f2)

if [[ -z "${API_BASE_URL}" ]]; then
  echo 'API_BASE_URL env var not set!';
  exit 1;
fi

sed -i.bak "s#${DEFAULT_API_BASE_URL}#${API_BASE_URL}#g" .next/routes-manifest.json
