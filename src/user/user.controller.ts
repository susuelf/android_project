import { Controller, Get, Body, Post } from '@nestjs/common';
import { UserService } from './user.service';
import { CreateUserDto } from './dtos/create-user.dto';
import {
  ApiTags,
  ApiOperation,
  ApiResponse,
  ApiBearerAuth,
  ApiExcludeEndpoint,
} from '@nestjs/swagger';
import { UserResponseDto } from './dtos/user-response.dto';

@ApiTags('Users')
@ApiBearerAuth('access-token')
@Controller('users')
export class UserController {
  constructor(private userService: UserService) {}

  @Get()
  @ApiOperation({ summary: 'List all users' })
  @ApiResponse({
    status: 200,
    description: 'Returns an array of users',
    type: [UserResponseDto],
  })
  async listUsers(): Promise<UserResponseDto[]> {
    return this.userService.findAll();
  }

  @Post()
  @ApiExcludeEndpoint()
  @ApiOperation({ summary: 'Create a new user' })
  @ApiResponse({
    status: 201,
    description: 'User successfully created',
    type: UserResponseDto,
  })
  async createUser(@Body() body: CreateUserDto): Promise<UserResponseDto> {
    return this.userService.createUser(body);
  }
}
