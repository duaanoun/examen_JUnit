package ma.ehei.repository;

import ma.ehei.model.Etudiant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EtudiantRepository {

    private List<Etudiant> etudiants = new ArrayList<>();

    public void save(Etudiant etudiant) {
        etudiants.add(etudiant);
    }
    //Optional : permet de représenter une recherche qui peut ne rien trouver.
    public Optional<Etudiant> findById(String id) {
        return etudiants.stream()
                .filter(e -> e.getId().equals(id))
                .findFirst();
    }

    public List<Etudiant> findAll() {
        return new ArrayList<>(etudiants);
    }
}