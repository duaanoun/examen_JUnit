package ma.ehei.repository;

import ma.ehei.model.Commande;
import java.util.ArrayList;
import java.util.List;

public class CommandeRepository {

    private List<Commande> commandes = new ArrayList<>();

    public void save(Commande commande) {
        commandes.add(commande);
    }

    public List<Commande> findAll() {
        return new ArrayList<>(commandes);
    }

    public List<Commande> findByEtudiantId(String etudiantId) {
        return commandes.stream()
                .filter(c -> c.getEtudiant().getId().equals(etudiantId))
                .toList();
    }

    public List<Commande> findByProduitId(String produitId) {
        return commandes.stream()
                .filter(c -> c.getProduit().getId().equals(produitId))
                .toList();
    }

    public int count() {
        return commandes.size();
    }
}