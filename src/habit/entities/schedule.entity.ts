import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToOne,
  CreateDateColumn,
  UpdateDateColumn,
  OneToMany,
} from 'typeorm';
import { User } from '../../user/entities/user.entity';
import { Habit } from '../../habit/entities/habit.entity';
import { Progress } from './progress.entity';

export enum ScheduleStatus {
  PLANNED = 'Planned',
  COMPLETED = 'Completed',
  SKIPPED = 'Skipped',
}

@Entity()
export class Schedule {
  @PrimaryGeneratedColumn()
  id: number;

  @ManyToOne(() => User, (user) => user.schedules)
  user: User;

  @ManyToOne(() => Habit, (habit) => habit.schedules)
  habit: Habit;

  @OneToMany(() => Progress, (progress) => progress.schedule)
  progress: Progress[];

  @Column()
  start_time: Date;

  @Column({ nullable: true })
  end_time: Date;

  @Column({ type: 'enum', enum: ScheduleStatus })
  status: ScheduleStatus;

  @Column()
  date: Date;

  @Column({ default: false })
  is_custom: boolean;

  @CreateDateColumn()
  created_at: Date;

  @UpdateDateColumn()
  updated_at: Date;
}
