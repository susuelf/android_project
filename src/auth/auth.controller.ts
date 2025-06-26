import {
  Body,
  Controller,
  Post,
  HttpStatus,
  UseGuards,
  Inject,
  HttpCode,
} from '@nestjs/common';
import { AuthService } from './auth.service';
import { AuthDto, AuthResponseDto, SignInDto } from './dto';
import { RtGuard } from './common/guards';
import { GetCurrentUser, GetCurrentUserId, Public } from './common/decorators';


@Controller('auth')
export class AuthController {
  constructor(
    @Inject('AUTH_SERVICE')
    private authService: AuthService,
  ) {}

  // @Public()
  // @Post('github')
  // @HttpCode(HttpStatus.OK)
  // async githubAuth(@Body() dto: GithubSignUpDto): Promise<Tokens> {
  //   console.log('dto', dto);
  //   return this.authService.validateGitHubUser(dto);
  // }
  @Public()
  @Post('google')
  @HttpCode(HttpStatus.OK)
  async googleAuth(@Body('idToken') idToken: string): Promise<AuthResponseDto> {
    return this.authService.handleGoogleAuth(idToken);
  }

  @Public()
  @Post('local/signup')
  @HttpCode(HttpStatus.CREATED)
  async signupLocal(@Body() dto: AuthDto): Promise<AuthResponseDto> {
    return this.authService.signupLocal(dto);
  }
  @Public()
  @Post('local/signin')
  @HttpCode(HttpStatus.OK)
  async signinLocal(@Body() dto: SignInDto): Promise<AuthResponseDto> {
    return this.authService.signinLocal(dto);
  }

  @Post('local/logout')
  @HttpCode(HttpStatus.OK)
  async logout(@GetCurrentUser('sub') userId: number) {
    return this.authService.logout(userId);
  }
  @Public()
  @UseGuards(RtGuard)
  @Post('local/refresh')
  @HttpCode(HttpStatus.OK)
  async refreshTokens(
    @GetCurrentUserId() userId: number,
    @GetCurrentUser('refreshToken') refreshToken: string,
  ) {
    return this.authService.refreshTokens(userId, refreshToken);
  }
}
