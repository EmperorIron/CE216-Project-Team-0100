package Classes;

import java.util.Random;

import com.github.javafaker.Faker;

public abstract class TeamGenerator {

    protected static final Faker faker = new Faker();
    protected static final Random rand = new Random();
    
    protected static final String[] SUFFIXES = {"FC", "United", "City", "Athletic", "Wanderers", "Sporting", "Spor", "İdman Yurdu", "Belediyespor", "Gençlik", "Gücü"};


    protected static String[] generateTeamIdentity() {
        String teamName;
        String customName = TeamNameImport.getNextCustomName();
        String city = faker.address().city();

        if (customName != null) {
            teamName = customName;
        } else {
            String suffix = SUFFIXES[rand.nextInt(SUFFIXES.length)];
            teamName = city + " " + suffix;
        }

        String country = faker.address().country();
        
        char firstLetter = teamName.toUpperCase().charAt(0);
        if (firstLetter == 'Ç') firstLetter = 'C';
        else if (firstLetter == 'Ğ') firstLetter = 'G';
        else if (firstLetter == 'İ' || firstLetter == 'I') firstLetter = 'I';
        else if (firstLetter == 'Ö') firstLetter = 'O';
        else if (firstLetter == 'Ş') firstLetter = 'S';
        else if (firstLetter == 'Ü') firstLetter = 'U';
        
        if (firstLetter < 'A' || firstLetter > 'Z') {
            firstLetter = (char) ('A' + rand.nextInt(26)); // Fallback if it's a number/symbol
        }
        
        String emblemPath = "images/Team" + firstLetter + ".png";
        
        return new String[]{teamName, country, emblemPath}; 
    }
}