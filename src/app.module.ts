import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { TestModule } from './testSwagger/test.module';

@Module({
  imports: [TestModule],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}
