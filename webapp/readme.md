Install NodeJS - https://nodejs.org/en/download/

Create `.env` file in the root with following contents

```
API_BASE=/backend
APP_BASE_URL=http://localhost:3000
API_BASE_URL=https://test.castled.io
DEBUG=false
```

To install and start the project, in project directory, you can run:

```
yarn && yarn build && yarn start
```

Runs the app in the development mode.<br />
Open [http://localhost:3000](http://localhost:3000) to view it in the browser.

For development mode, use `yarn dev` instead of `yarn start`


