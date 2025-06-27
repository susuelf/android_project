import { Controller, Get, Post, Body } from '@nestjs/common';
import { HabitService } from './habit.service';
import { Habit } from './entities/habit.entity';

@Controller('habit')
export class HabitController {
  constructor(private habitService: HabitService) {}

  @Get()
  async findAll(): Promise<Habit[]> {
    return this.habitService.findAll();
  }

  @Post()
  async create(@Body() habitData: Partial<Habit>): Promise<Habit> {
    return this.habitService.create(habitData);
  }
}
