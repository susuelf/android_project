import { Tokens } from '../types';
import { UserResponseDto } from './user.response.dto';

export class AuthResponseDto {
  message: string;
  user: UserResponseDto;
  tokens: Tokens;
}
