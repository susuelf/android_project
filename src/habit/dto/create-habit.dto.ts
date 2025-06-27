import { IsEnum, IsNotEmpty, IsOptional, IsString } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';
import { HabitCategory, HabitFrequency } from '../entities/habit.entity';

export class CreateHabitDto {
  @ApiProperty({ example: 'Morning Run' })
  @IsString()
  @IsNotEmpty()
  name: string;

  @ApiProperty({ example: 'Run 2km every morning', required: false })
  @IsString()
  @IsOptional()
  description?: string;

  @ApiProperty({ enum: HabitCategory, example: HabitCategory.EXERCISE })
  @IsEnum(HabitCategory)
  category: HabitCategory;

  @ApiProperty({ example: 'Run 10 times in 2 weeks' })
  @IsString()
  @IsNotEmpty()
  goal: string;

  @ApiProperty({ enum: HabitFrequency, example: HabitFrequency.DAILY })
  @IsEnum(HabitFrequency)
  frequency: HabitFrequency;
}
