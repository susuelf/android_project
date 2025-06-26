import { IsEmail, IsNotEmpty, IsOptional, IsString } from 'class-validator';
import { AuthProvider } from '../enums';

export class AuthDto {
  @IsString()
  @IsNotEmpty()
  username: string;

  @IsEmail()
  @IsNotEmpty()
  @IsString()
  email: string;

  @IsNotEmpty()
  @IsString()
  password: string;
}
