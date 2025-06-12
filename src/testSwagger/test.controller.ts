import { Controller, Get } from '@nestjs/common';
import { ApiTags, ApiOperation } from '@nestjs/swagger';

@ApiTags('test')
@Controller('test')
export class TestController {
  @Get()
  @ApiOperation({ summary: 'Returns hello message' })
  getHello() {
    return { message: 'Hello Swagger!' };
  }
}
