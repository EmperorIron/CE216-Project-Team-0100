package Classes;

import com.github.javafaker.Faker;
import java.util.Random;

public abstract class PlayerGenerator {

    protected static final Faker faker = new Faker();
    protected static final Random rand = new Random();
    protected static final RandomGenerator randGen = new RandomGenerator(); 

    // GENERAL METHOD: Generates identities for all sports branches
    
    protected static String[] generatePlayerIdentity() {
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        String country = faker.address().country();
        String id = "ID-" + faker.number().numberBetween(1000, 9999);
        
        return new String[]{firstName, lastName, country, id};
    }
}