import { registerAs } from '@nestjs/config';

export default registerAs('googleOAuth', () => ({
  clientID: process.env.GOOGLE_WEB_CLIENT_ID,
  secretClientID: process.env.GOOGLE_SECRET_CLIENT_ID,
  callbackURL: process.env.GOOGLE_CALLBACK_URL,
}));
