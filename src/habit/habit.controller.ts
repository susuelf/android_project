import {
  Controller,
  Get,
  Param,
  ParseIntPipe,
  Post,
  Body,
} from '@nestjs/common';
import { HabitService } from './habit.service';
import { HabitResponseDto } from './dto/habit-response.dto';
import { CreateHabitDto } from './dto/create-habit.dto';
import {
  ApiTags,
  ApiOperation,
  ApiResponse,
  ApiBearerAuth,
  ApiParam,
} from '@nestjs/swagger';

@ApiTags('Habit')
@ApiBearerAuth('access-token')
@Controller('habit')
export class HabitController {
  constructor(private habitService: HabitService) {}

  @Get()
  @ApiOperation({ summary: 'List all habits' })
  @ApiResponse({ status: 200, type: [HabitResponseDto] })
  async findAll(): Promise<HabitResponseDto[]> {
    return this.habitService.findAll();
  }

  @Post()
  @ApiOperation({ summary: 'Create a new habit' })
  @ApiResponse({ status: 201, type: HabitResponseDto })
  async create(@Body() habitData: CreateHabitDto): Promise<HabitResponseDto> {
    return this.habitService.create(habitData);
  }

  @Get('user/:userId')
  @ApiOperation({ summary: 'Find habits by user ID' })
  @ApiParam({ name: 'userId', type: Number })
  @ApiResponse({ status: 200, type: [HabitResponseDto] })
  async findByUserId(
    @Param('userId', ParseIntPipe) userId: number,
  ): Promise<HabitResponseDto[]> {
    return this.habitService.findByUserId(userId);
  }

  @Get(':habitId')
  @ApiOperation({ summary: 'Find habit by habit ID' })
  @ApiParam({ name: 'habitId', type: Number })
  @ApiResponse({ status: 200, type: HabitResponseDto })
  async findById(
    @Param('habitId', ParseIntPipe) habitId: number,
  ): Promise<HabitResponseDto> {
    return this.habitService.findById(habitId);
  }
}
