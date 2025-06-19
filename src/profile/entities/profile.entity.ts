import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  OneToOne,
  JoinTable,
  ManyToMany,
} from 'typeorm';
import { User } from '../../user/entities/user.entity';
@Entity()
export class Profile {
  @PrimaryGeneratedColumn()
  id: number;

  @OneToOne(() => User, (user) => user.profile, { cascade: true })
  user: User;

  @Column({ type: 'varchar', length: 100, nullable: false, unique: true })
  username?: string;

  @Column({ type: 'text', nullable: true })
  description?: string;

  @Column({ type: 'text', nullable: true })
  profileImageUrl?: string;

  @Column({ type: 'text', nullable: true })
  coverImageUrl?: string;

  @Column({ type: 'text', nullable: true, default: '' })
  fcmToken: string;
}
