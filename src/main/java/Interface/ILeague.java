package Interface;

import java.util.List;
// import Classes.Team; SİLİNDİ! Artık doğrudan sınıflara bağımlı değiliz.

public interface ILeague {

    String getName();
    String getCountry();
    List<ITeam> getTeamRanking();// Sadece arayüzleri (ITeam) döndürüyoruz
    void addTeam(ITeam team);// Takım eklerken de sadece arayüz (ITeam) kabul ediyoruz
}