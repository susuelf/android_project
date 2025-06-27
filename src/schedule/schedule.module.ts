import { Module } from '@nestjs/common';
import { ScheduleService } from './schedule.service';
import { ScheduleController } from './schedule.controller';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Schedule } from './entities/schedule.entity';
import { Habit } from 'src/habit/entities/habit.entity';

@Module({
  imports: [TypeOrmModule.forFeature([Schedule, Habit])],
  controllers: [ScheduleController],
  providers: [ScheduleService],
})
export class ScheduleModule {}
