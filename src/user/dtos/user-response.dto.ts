import { ApiProperty } from '@nestjs/swagger';
import { ProfileResponseDto } from 'src/profile/dto/profile-response.dto';

export class UserResponseDto {
  @ApiProperty()
  id: number;

  @ApiProperty()
  email: string;

  @ApiProperty()
  auth_provider: string;

  @ApiProperty({ type: () => ProfileResponseDto })
  profile: ProfileResponseDto;
}
