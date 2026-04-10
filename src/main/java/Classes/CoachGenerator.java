package Classes;

import com.github.javafaker.Faker;
import java.util.Random;

public abstract class CoachGenerator {

    protected static final Faker faker = new Faker();
    protected static final Random rand = new Random();
    protected static final RandomGenerator randGen = new RandomGenerator();

    
    protected static String[] generateCoachIdentity() {
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        String country = faker.address().country();
        String id = "CID-" + faker.number().numberBetween(100, 999); 

        return new String[]{firstName, lastName, country, id};
    }
}
