import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { SwaggerModule, DocumentBuilder } from '@nestjs/swagger';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);

  // 🔧 Swagger configuration
  const config = new DocumentBuilder()
    .setTitle('Progr3SS API')
    .setDescription('The backend API documentation')
    .setVersion('1.0')
    .addTag('test') // This should match @ApiTags('test') in the controller
    .build();

  // 📄 Create and mount the Swagger document
  const document = SwaggerModule.createDocument(app, config);
  SwaggerModule.setup('api', app, document); // Swagger UI at /api

  await app.listen(3000);
}
bootstrap();
