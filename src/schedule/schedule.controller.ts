import {
  Controller,
  Get,
  Post,
  Body,
  Patch,
  Param,
  Delete,
  ParseIntPipe,
} from '@nestjs/common';
import { ScheduleService } from './schedule.service';
import { CreateScheduleDto } from './dto/create-schedule.dto';
import { UpdateScheduleDto } from './dto/update-schedule.dto';
import { Schedule } from './entities/schedule.entity';
import {
  ApiTags,
  ApiOperation,
  ApiResponse,
  ApiBearerAuth,
  ApiParam,
} from '@nestjs/swagger';
import { ScheduleResponseDto } from './dto/schedule-response.dto';

@ApiTags('Schedule')
@ApiBearerAuth('access-token')
@Controller('schedule')
export class ScheduleController {
  constructor(private readonly scheduleService: ScheduleService) {}

  @Post()
  @ApiOperation({ summary: 'Create new schedule' })
  @ApiResponse({
    status: 201,
    description: 'Schedule created',
    type: ScheduleResponseDto,
  })
  create(
    @Body() createScheduleDto: CreateScheduleDto,
  ): Promise<ScheduleResponseDto> {
    return this.scheduleService.create(createScheduleDto);
  }

  @Get()
  @ApiOperation({ summary: 'Get all schedules' })
  @ApiResponse({
    status: 200,
    description: 'List of schedules',
    type: [ScheduleResponseDto],
  })
  findAll(): Promise<ScheduleResponseDto[]> {
    return this.scheduleService.findAll();
  }

  @Get(':id')
  @ApiOperation({ summary: 'Get schedule by ID' })
  @ApiParam({ name: 'id', type: Number })
  @ApiResponse({
    status: 200,
    description: 'Schedule found',
    type: ScheduleResponseDto,
  })
  findOne(@Param('id', ParseIntPipe) id: number): Promise<ScheduleResponseDto> {
    return this.scheduleService.findOne(id);
  }

  @Patch(':id')
  @ApiOperation({ summary: 'Update schedule by ID' })
  @ApiParam({ name: 'id', type: Number })
  @ApiResponse({
    status: 200,
    description: 'Schedule updated',
    type: ScheduleResponseDto,
  })
  update(
    @Param('id', ParseIntPipe) id: number,
    @Body() updateScheduleDto: UpdateScheduleDto,
  ): Promise<ScheduleResponseDto> {
    return this.scheduleService.update(id, updateScheduleDto);
  }

  @Delete(':id')
  @ApiOperation({ summary: 'Delete schedule by ID' })
  @ApiParam({ name: 'id', type: Number })
  @ApiResponse({ status: 204, description: 'Schedule deleted' })
  async remove(@Param('id', ParseIntPipe) id: number): Promise<void> {
    await this.scheduleService.remove(id);
  }
}
