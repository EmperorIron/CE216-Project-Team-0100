package Classes;

import java.util.Random;

public class RandomGenerator {
    
    // Random nesnesini de static yapıyoruz ki static metot içinde kullanabilelim
    private static final Random rand = new Random();
    
    // Başına "public static" ekledik
    public static int generateBellCurveStat(int mean, int standardDeviation, int min, int max) {
        double val = (rand.nextGaussian() * standardDeviation) + mean;
        return (int) Math.max(min, Math.min(max, val));
    }
}