# Base on offical Node.js Alpine image
FROM node:16.13-alpine as webapp

ARG ARG_API_BASE
ARG ARG_API_BASE_URL
ARG ARG_APP_BASE_URL

ENV API_BASE=${ARG_API_BASE}
ENV API_BASE_URL=${ARG_API_BASE_URL}
ENV APP_BASE_URL=${ARG_APP_BASE_URL}

# Set working directory
WORKDIR /usr/webapp

RUN apk add --no-cache --upgrade bash

# Copy package.json and package-lock.json before other files
# Utilise Docker cache to save re-installing dependencies if unchanged
COPY package.json yarn.lock ./

# Install dependencies
RUN yarn --no-cache install

# Copy all files
COPY . .

# Hack: will be replaced by runtime API_BASE_URL
RUN echo "API_BASE_URL=$ARG_API_BASE_URL" >> .buildenv

RUN yarn build

# Expose the listening port
EXPOSE 3000

# Hack: set_api_url.sh will update the API_BASE_URL at runtime
RUN chown -R node .next

# Run container as non-root (unprivileged) user
# The node user is provided in the Node.js Alpine base image
USER node

CMD tools/bin/set_api_url.sh && yarn start
