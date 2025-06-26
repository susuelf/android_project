import { Profile } from 'src/profile/entities/profile.entity';

export class UserResponseDto {
  id: number;
  email: string;
  auth_provider: string;
  profile: Profile;
}
