import { Module } from '@nestjs/common';
import { HabitService } from './habit.service';
import { HabitController } from './habit.controller';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Habit } from './entities/habit.entity';

@Module({
  imports: [TypeOrmModule.forFeature([Habit])],
  controllers: [HabitController],
  providers: [HabitService],
  exports: [HabitService],
})
export class HabitModule {}
