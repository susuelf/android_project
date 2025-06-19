import { IsNotEmpty, IsString } from 'class-validator';

export class GithubSignUpDto {
  @IsString()
  @IsNotEmpty()
  accessToken: string;
}
