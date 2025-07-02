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
import {
  ApiTags,
  ApiOperation,
  ApiBody,
  ApiResponse,
  ApiBearerAuth,
} from '@nestjs/swagger';
import { ResetPasswordDto } from './dto/reset.password.dto';

@ApiTags('Authentication')
@Controller('auth')
export class AuthController {
  constructor(
    @Inject('AUTH_SERVICE')
    private authService: AuthService,
  ) {}

  @Public()
  @Post('google')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Google Sign-In',
    description: 'Authenticate user with Google ID token.',
  })
  @ApiBody({ schema: { example: { idToken: 'eyJhbGciOiJSUzI1NiIsInR5c...' } } })
  @ApiResponse({
    status: 200,
    description: 'Successful Google login.',
    type: AuthResponseDto,
  })
  async googleAuth(@Body('idToken') idToken: string): Promise<AuthResponseDto> {
    return this.authService.handleGoogleAuth(idToken);
  }

  @Public()
  @Post('local/signup')
  @HttpCode(HttpStatus.CREATED)
  @ApiOperation({
    summary: 'Local Sign-Up',
    description: 'Register user with email and password.',
  })
  @ApiResponse({
    status: 201,
    description: 'Successful registration.',
    type: AuthResponseDto,
  })
  async signupLocal(@Body() dto: AuthDto): Promise<AuthResponseDto> {
    return this.authService.signupLocal(dto);
  }

  @Public()
  @Post('local/signin')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Local Sign-In',
    description: 'Authenticate with email and password.',
  })
  @ApiResponse({
    status: 200,
    description: 'Successful login.',
    type: AuthResponseDto,
  })
  async signinLocal(@Body() dto: SignInDto): Promise<AuthResponseDto> {
    return this.authService.signinLocal(dto);
  }

  @Post('local/logout')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Logout', description: 'Logs out the user.' })
  @ApiBearerAuth('access-token')
  @ApiResponse({ status: 200, description: 'Successfully logged out.' })
  async logout(@GetCurrentUser('sub') userId: number) {
    return this.authService.logout(userId);
  }

  @Public()
  @UseGuards(RtGuard)
  @Post('local/refresh')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Refresh Tokens',
    description: 'Refresh access and refresh tokens.',
  })
  @ApiBearerAuth('refresh-token')
  @ApiResponse({
    status: 200,
    description: 'Tokens refreshed successfully.',
    type: AuthResponseDto,
  })
  async refreshTokens(
    @GetCurrentUserId() userId: number,
    @GetCurrentUser('refreshToken') refreshToken: string,
  ) {
    return this.authService.refreshTokens(userId, refreshToken);
  }

  @Post('local/reset-password')
  @HttpCode(HttpStatus.OK)
  @ApiBearerAuth('access-token')
  @ApiOperation({
    summary: 'Reset Password',
    description:
      'Allows authenticated users to change their password by providing the current password.',
  })
  @ApiResponse({ status: 200, description: 'Password reset successful.' })
  @ApiResponse({ status: 403, description: 'Incorrect current password.' })
  @ApiResponse({ status: 404, description: 'User not found.' })
  async resetPassword(
    @GetCurrentUserId() userId: number,
    @Body() dto: ResetPasswordDto,
  ): Promise<{ message: string }> {
    const msg = await this.authService.resetPassword(userId, dto);
    return { message: msg };
  }
}
