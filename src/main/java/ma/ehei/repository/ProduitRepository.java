package ma.ehei.repository;

import ma.ehei.model.Produit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProduitRepository {

    private List<Produit> produits = new ArrayList<>();

    public void save(Produit produit) {
        produits.add(produit);
    }

    public Optional<Produit> findById(String id) {
        return produits.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    public List<Produit> findAll() {
        return new ArrayList<>(produits);
    }
}