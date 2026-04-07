package Classes;

import java.util.Random;

import com.github.javafaker.Faker;

public abstract class TeamGenerator {

    protected static final Faker faker = new Faker();
    protected static final Random rand = new Random();
    
    protected static final String[] SUFFIXES = {"FC", "United", "City", "Athletic", "Wanderers", "Sporting", "Spor", "İdman Yurdu", "Belediyespor", "Gençlik", "Gücü"};


    protected static String[] generateTeamIdentity() {
        String city = faker.address().city();
        String suffix = SUFFIXES[rand.nextInt(SUFFIXES.length)];
        
        String teamName = city + " " + suffix;
        String country = faker.address().country();
        
        return new String[]{teamName, country}; 
    }
}