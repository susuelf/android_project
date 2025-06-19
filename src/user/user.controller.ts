import { Controller, Get, Body, Post } from '@nestjs/common';
import { UserService } from './user.service';
import { CreateUserDto } from './dtos/create-user.dto';

@Controller('users')
export class UserController {
  constructor(private userService: UserService) {}

  @Get()
  async listUsers() {
    return this.userService.findAll();
  }
  @Post()
  async createUser(@Body() body: CreateUserDto) {
    return this.userService.creatUser(body);
  }
}
