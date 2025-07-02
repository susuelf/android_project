import { ApiProperty } from '@nestjs/swagger';
import { IsString, MinLength } from 'class-validator';

export class ResetPasswordDto {
  @ApiProperty({ example: 'OldPassword123' })
  @IsString()
  oldPassword: string;

  @ApiProperty({ example: 'NewSecurePassword123' })
  @IsString()
  @MinLength(6)
  newPassword: string;
}
