// import { Inject, Injectable } from '@nestjs/common';
// import { PassportStrategy } from '@nestjs/passport';
// import { Strategy, VerifyCallback } from 'passport-google-oauth20';
// import googleOauthConfig from '../config/google-oauth.config';
// import { ConfigType } from '@nestjs/config';
// import { AuthService } from '../auth.service';

// @Injectable()
// export class GoogleStrategy extends PassportStrategy(Strategy) {
//   constructor(
//     @Inject(googleOauthConfig.KEY)
//     private googleConfiguration: ConfigType<typeof googleOauthConfig>,
//     @Inject('AUTH_SERVICE')
//     private authService: AuthService,
//   ) {
//     super({
//       clientID: googleConfiguration.clientID,
//       clientSecret: googleConfiguration.secretClientID,
//       callbackURL: googleConfiguration.callbackURL,
//       scope: ['email', 'profile'],
//     });
//   }

//   async validate(
//     accessToken: string,
//     refreshToken: string,
//     profile: any,
//     done: VerifyCallback,
//   ): Promise<any> {
//     const user = await this.authService.validateGoogleUser({
//       email: profile.emails[0].value,
//       username: profile.name.givenName,
//       password: '',
//     });
//     return user || null;
//   }
// }
