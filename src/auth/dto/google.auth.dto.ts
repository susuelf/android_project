import { IsEmail, IsNotEmpty, IsString } from 'class-validator';
import { AuthProvider } from '../enums';

export class GoogleAuthDto {
  @IsString()
  @IsNotEmpty()
  username: string;

  @IsEmail()
  @IsNotEmpty()
  email: string;

  @IsString()
  @IsNotEmpty()
  profileImageUrl: string;

  @IsString()
  @IsNotEmpty()
  authProviderId: string; // Google 'sub' értéke

  @IsString()
  @IsNotEmpty()
  authProvider: AuthProvider; // pl. 'google'
}
