import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Habit } from './entities/habit.entity';
import { Repository } from 'typeorm';

@Injectable()
export class HabitService {
  constructor(
    @InjectRepository(Habit)
    private habitRepo: Repository<Habit>,
  ) {}

  findAll() {
    return this.habitRepo.find();
  }

  create(habitData: Partial<Habit>) {
    const habit = this.habitRepo.create(habitData);
    return this.habitRepo.save(habit);
  }
}
