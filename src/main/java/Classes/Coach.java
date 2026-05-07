package Classes;

import java.util.HashMap;
import Interface.ICoach;

public abstract class Coach implements ICoach {

    private final String name;
    private final String surname;
    private final String country;
    private final String id;
    private String team;
    private final HashMap<String, Trait> traits;

    public Coach(String name, String surname, String country, String team, String id) {
        this.name = name;
        this.surname = surname;
        this.country = country;
        this.team = team;
        this.id = id;
        this.traits = new HashMap<>();
    }
    //Setters and Getters
    @Override
    public String getName() {
        return name;
    }
    @Override
    public String getSurname() {
        return surname;
    }
    @Override
    public String getCountry() {
        return country;
    }
    @Override
    public String getId() {
        return id;
    }
    @Override
    public String getTeam() {
        return team;
    }
    public void setTeam(String team) {
        this.team = team;
    }
    @Override
    public HashMap<String, Trait> getTraits() {
        return traits;
    }
    @Override
    public Trait getTrait(String traitName) {
        return this.traits.get(traitName);
    }
    @Override
    public HashMap<String, Trait> getAllTraits() {
        return this.traits;
    }
    @Override
    public String getFullName() {
        return name + " " + surname;
    }
    //////////////////////////////////////////////////////////////
     @Override
    public void addTrait(Trait trait) {
        if (trait != null) {
            this.traits.put(trait.getName(), trait);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || !(obj instanceof ICoach)) return false;
        ICoach other = (ICoach) obj;
        return this.id != null && this.id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}