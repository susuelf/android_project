import { ForbiddenException, Injectable } from '@nestjs/common';
import { CreateUserDto } from 'src/user/dtos/create-user.dto';
import { UserService } from 'src/user/user.service';
import { AuthDto, SignInDto } from './dto';
import * as bcrypt from 'bcrypt';
import { Tokens } from './types';
import { JwtService } from '@nestjs/jwt';
import { GithubSignUpDto } from './dto/github.sign.up.dto';

@Injectable()
export class AuthService {
  constructor(
    private readonly userService: UserService,
    private readonly jwtService: JwtService,
  ) {}

  // async validateGitHubUser({ accessToken }: GithubSignUpDto) {
  //   // Fetch GitHub user data
  //   const userResponse = await fetch('https://api.github.com/user', {
  //     headers: { Authorization: `Bearer ${accessToken}` },
  //   });
  //   const userData = await userResponse.json();
  //   console.log('GitHub User', userData);
  //   const { id: githubId, login: username, avatar_url, email } = userData;

  //   let user = await this.userService.findByGithubId(githubId);
  //   console.log('user: ', user);
  //   if (!user) {
  //     // User doesn't exist, create a new one
  //     user = await this.userService.creatUser({
  //       githubId: githubId,
  //       username: username,
  //       email: email || `${username}@github.com`,
  //       password: '',
  //     });
  //   }

  //   user.profile.profileImageUrl = avatar_url;

  //   console.log('user: ', user);

  //   const tokens = await this.getTokens(user.id, user.email, user.profile.id);
  //   console.log('tokens ', tokens);
  //   return tokens;
  // }

  async findUserById(id: number) {
    return this.userService.findOneById(id);
  }

  async validateGoogleUser(googleUser: CreateUserDto) {
    const user = await this.userService.findOne(googleUser.email);
    if (user) return user;
    return this.userService.creatUser(googleUser);
  }

  async signupLocal(dto: AuthDto): Promise<Tokens> {
    const hash = await this.hashData(dto.password);

    const createdUser = await this.userService.creatUser({
      username: dto.username,
      email: dto.email,
      password: hash,
    });

    const tokens = await this.getTokens(
      createdUser.id,
      createdUser.email,
      createdUser.profile.id,
    );
    await this.updateRtHash(createdUser.id, tokens.refreshToken);
    return tokens;
  }

  async signinLocal(dto: SignInDto): Promise<Tokens> {
    const user = await this.userService.findOne(dto.email);

    if (!user) throw new ForbiddenException('Access denied');
    const checkPassword = await bcrypt.compare(dto.password, user.password);
    if (!checkPassword) throw new ForbiddenException('Acces denied');
    if (user.profile === null) {
      throw new ForbiddenException('Access denied - profile null');
    }
    const tokens = await this.getTokens(user.id, user.email, user.profile.id);
    await this.updateRtHash(user.id, tokens.refreshToken);
    return tokens;
  }

  async logout(userId: number) {
    return await this.userService.updateRefreshToken(userId, null);
  }
  async refreshTokens(userId: number, rt: string) {
    const user = await this.findUserById(userId);
    if (!user || !user.hashedRt) throw new ForbiddenException('Access denied');
    const checkRefreshTokens = await bcrypt.compare(rt, user.hashedRt);
    if (!checkRefreshTokens) throw new ForbiddenException('Access denied');
    if (user.profile === null) {
      throw new ForbiddenException('Access denied - profile null');
    }
    const tokens = await this.getTokens(userId, user.email, user.profile.id);
    await this.updateRtHash(userId, tokens.refreshToken);
    return tokens;
  }

  async updateRtHash(userId: number, refreshToken: string) {
    const hash = await this.hashData(refreshToken);
    await this.userService.updateRefreshToken(userId, hash);
  }

  hashData(data: string) {
    return bcrypt.hash(data, 10);
  }

  async getTokens(userId: number, email: string, profileId: number) {
    const jwtPayload = { sub: userId, email, profileId };
    const [at, rt] = await Promise.all([
      this.jwtService.signAsync(jwtPayload, {
        secret: process.env.AT_SECRET,
        expiresIn: '1h',
      }),
      this.jwtService.signAsync(jwtPayload, {
        secret: process.env.RT_SECRET,
        expiresIn: '3d',
      }),
    ]);

    return {
      accessToken: at,
      refreshToken: rt,
    };
  }
}
