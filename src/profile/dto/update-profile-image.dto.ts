import { ApiProperty } from '@nestjs/swagger';

export class UpdateProfileImageDto {
  @ApiProperty({
    type: 'string',
    format: 'binary',
    description: 'Profile image file to upload',
  })
  profileImage: any;
}
