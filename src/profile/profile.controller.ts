import {
  Controller,
  Get,
  Post,
  Body,
  Patch,
  Param,
  Delete,
  HttpCode,
  ParseIntPipe,
  Query,
} from '@nestjs/common';
import { ProfileService } from './profile.service';
import { CreateProfileDto } from './dto/create-profile.dto';
import { UpdateProfileDto } from './dto/update-profile.dto';
import { GetCurrentUser } from 'src/auth/common/decorators';

@Controller('profile')
export class ProfileController {
  constructor(private readonly profileService: ProfileService) {}

  @Post()
  async create(@Body() createProfileDto: CreateProfileDto) {
    return await this.profileService.create(createProfileDto);
  }

  @Get('/all')
  async findAll() {
    return await this.profileService.findAll();
  }
  @Get()
  async findMe(@GetCurrentUser('profileId') id: number) {
    return await this.profileService.findOne(id, id);
  }

  @Get(':id')
  async findOne(
    @Param('id', ParseIntPipe) id: number,
    @GetCurrentUser('profileId') currentUserId: number,
  ) {
    return await this.profileService.findOne(id, currentUserId);
  }

  @Patch()
  async update(
    @GetCurrentUser('profileId') id: number,
    @Body() updateProfileDto: UpdateProfileDto,
  ) {
    return await this.profileService.update(id, updateProfileDto);
  }

  @Delete(':id')
  @HttpCode(204)
  async remove(@Param('id', ParseIntPipe) id: number) {
    await this.profileService.remove(id);
  }

  @Post('update-fcm-token')
  async updateFcmToken(
    @GetCurrentUser('profileId') id: number,
    @Body() token: { token: string },
  ) {
    return await this.profileService.updateFcmToken(id, token);
  }
}
