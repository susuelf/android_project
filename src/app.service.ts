import { Injectable } from '@nestjs/common';

@Injectable()
export class AppService {
  getHello(): string {
    return 'Progr3ss Backend A.I. 2025';
  }
}
