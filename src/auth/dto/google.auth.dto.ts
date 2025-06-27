import { IsEmail, IsNotEmpty, IsString } from 'class-validator';
import { AuthProvider } from '../enums';
import { ApiProperty } from '@nestjs/swagger';

export class GoogleAuthDto {
  @ApiProperty()
  @IsString()
  @IsNotEmpty()
  username: string;

  @ApiProperty()
  @IsEmail()
  @IsNotEmpty()
  email: string;

  @ApiProperty()
  @IsString()
  @IsNotEmpty()
  profileImageUrl: string;

  @ApiProperty()
  @IsString()
  @IsNotEmpty()
  authProviderId: string; // Google 'sub' értéke

  @ApiProperty()
  @IsString()
  @IsNotEmpty()
  authProvider: AuthProvider; // pl. 'google'
}
