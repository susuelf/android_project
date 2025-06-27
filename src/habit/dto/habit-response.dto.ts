import { ApiProperty } from '@nestjs/swagger';
import { HabitCategory, HabitFrequency } from '../entities/habit.entity';

export class HabitResponseDto {
  @ApiProperty()
  id: number;

  @ApiProperty()
  name: string;

  @ApiProperty({ required: false })
  description?: string;

  @ApiProperty({ enum: HabitCategory })
  category: HabitCategory;

  @ApiProperty()
  goal: string;

  @ApiProperty({ enum: HabitFrequency })
  frequency: HabitFrequency;

  @ApiProperty()
  created_at: Date;

  @ApiProperty()
  updated_at: Date;
}
