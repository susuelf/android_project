import { DataSource } from 'typeorm';
import { User } from '../user/entities/user.entity';
import { Habit } from '../habit/entities/habit.entity';
import { HabitCategory } from '../habit/entities/habit-category.entity';

/**
 * Seed Script - Test Habits
 * 
 * Ez a script teszt habit-eket hoz létre a Create Schedule teszteléséhez.
 * 
 * Futtatás: npm run seed:habits
 */

async function seedHabits() {
  console.log('🌱 Seed script started - Creating test habits...\n');

  // DataSource konfiguráció (használd a .env fájlból az adatokat)
  const dataSource = new DataSource({
    type: 'postgres',
    host: process.env.DB_HOST || 'localhost',
    port: parseInt(process.env.DB_PORT || '5432'),
    username: process.env.DB_USERNAME || 'postgres',
    password: process.env.DB_PASSWORD || 'postgres',
    database: process.env.DB_DATABASE || 'habittracker',
    entities: [User, Habit, HabitCategory],
    synchronize: false,
  });

  try {
    await dataSource.initialize();
    console.log('✅ Database connected\n');

    const habitRepo = dataSource.getRepository(Habit);
    const userRepo = dataSource.getRepository(User);
    const categoryRepo = dataSource.getRepository(HabitCategory);

    // 1. Ellenőrizzük, hogy van-e már habit
    const existingHabits = await habitRepo.count();
    if (existingHabits > 0) {
      console.log(`✅ Already have ${existingHabits} habits. Skipping seed.\n`);
      await dataSource.destroy();
      return;
    }

    // 2. Keressünk egy teszt felhasználót
    const testUser = await userRepo.findOne({
      where: [
        { email: 'test@example.com' },
        { email: 'test@test.com' },
      ],
    });

    if (!testUser) {
      console.error('❌ No test user found.');
      console.log('   Please register a user first via: POST /auth/register');
      console.log('   Example: email=test@example.com, password=Test1234!\n');
      await dataSource.destroy();
      return;
    }

    console.log(`✅ Found test user: ${testUser.email} (ID: ${testUser.id})\n`);

    // 3. Keressünk kategóriákat
    const categories = await categoryRepo.find();
    if (categories.length === 0) {
      console.error('❌ No habit categories found.');
      console.log('   Please seed categories first.\n');
      await dataSource.destroy();
      return;
    }

    console.log(`✅ Found ${categories.length} categories\n`);

    // 4. Hozzunk létre teszt habit-eket
    const testHabits = [
      {
        name: 'Reggeli futás',
        description: '30 perces kocogás reggel',
        goal: 'Minden nap',
        user: testUser,
        category: categories[0] || null,
      },
      {
        name: 'Olvasás',
        description: 'Legalább 20 oldal naponta',
        goal: '1 könyv havonta',
        user: testUser,
        category: categories[1] || categories[0],
      },
      {
        name: 'Meditáció',
        description: '10 perc mindfulness gyakorlat',
        goal: 'Napi szinten',
        user: testUser,
        category: categories[0] || null,
      },
      {
        name: 'Vízivás',
        description: '2 liter víz naponta',
        goal: '8 pohár víz',
        user: testUser,
        category: categories[2] || categories[0],
      },
      {
        name: 'Tanulás',
        description: 'Egyetemi anyagok áttekintése',
        goal: 'Heti 5x 1 óra',
        user: testUser,
        category: categories[1] || categories[0],
      },
    ];

    console.log('Creating habits...\n');

    for (const habitData of testHabits) {
      const habit = habitRepo.create(habitData);
      await habitRepo.save(habit);
      console.log(`  ✅ Created habit: ${habit.name} (ID: ${habit.id})`);
    }

    console.log(`\n🎉 Seed completed successfully! Created ${testHabits.length} habits.`);
    console.log('   You can now test the Create Schedule screen in the app.\n');

    await dataSource.destroy();
  } catch (error) {
    console.error('❌ Seed failed:', error);
    if (dataSource.isInitialized) {
      await dataSource.destroy();
    }
    process.exit(1);
  }
}

// Futtatás
seedHabits();
