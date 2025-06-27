import { ApiProperty } from '@nestjs/swagger';
import { Tokens } from '../types';
import { UserResponseDto } from 'src/user/dtos/user-response.dto';

export class AuthResponseDto {
  @ApiProperty()
  message: string;

  @ApiProperty({ type: () => UserResponseDto })
  user: UserResponseDto;

  @ApiProperty({ type: () => Tokens })
  tokens: Tokens;
}
