package Interface;
import Classes.Trait;
import java.util.HashMap;

public interface ICoach {
    String getId();
    String getName();
    String getSurname();
    String getCountry();
    String getTeam(); 
    String getPreferredFormation(); 
    HashMap<String, Trait> getAllTraits();
    Trait getTrait(String traitName);
    void addTrait(Trait trait); 
    HashMap<String, Trait> getTraits();
    String getFullName();
}