import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Habit } from './entities/habit.entity';
import { Repository } from 'typeorm';
import { HabitResponseDto } from './dto/habit-response.dto';

@Injectable()
export class HabitService {
  constructor(
    @InjectRepository(Habit)
    private habitRepo: Repository<Habit>,
  ) {}

  async findAll(): Promise<HabitResponseDto[]> {
    const habits = await this.habitRepo.find();
    return habits.map(this.mapToResponseDto);
  }

  async create(habitData: Partial<Habit>): Promise<HabitResponseDto> {
    const habit = this.habitRepo.create(habitData);
    const saved = await this.habitRepo.save(habit);
    return this.mapToResponseDto(saved);
  }

  async findByUserId(userId: number): Promise<HabitResponseDto[]> {
    const habits = await this.habitRepo.find({
      where: { user: { id: userId } },
      relations: ['user'],
    });
    return habits.map(this.mapToResponseDto);
  }

  async findById(habitId: number): Promise<HabitResponseDto> {
    const habit = await this.habitRepo.findOne({
      where: { id: habitId },
    });
    return this.mapToResponseDto(habit);
  }

  private mapToResponseDto(habit: Habit): HabitResponseDto {
    return {
      id: habit.id,
      name: habit.name,
      description: habit.description,
      category: habit.category,
      goal: habit.goal,
      frequency: habit.frequency,
      created_at: habit.created_at,
      updated_at: habit.updated_at,
    };
  }
}
