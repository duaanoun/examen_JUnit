package ma.ehei.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Commande {

    private String reference;
    private Etudiant etudiant;
    private Produit produit;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private int duree;
    private BigDecimal montantLocation;
    private BigDecimal caution;
    private BigDecimal montantTotal;

    public Commande(String reference, Etudiant etudiant,
                    Produit produit, LocalDate dateDebut,
                    LocalDate dateFin, int duree,
                    BigDecimal montantLocation,
                    BigDecimal caution, BigDecimal montantTotal) {

        this.reference = reference;
        this.etudiant = etudiant;
        this.produit = produit;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.duree = duree;
        this.montantLocation = montantLocation;
        this.caution = caution;
        this.montantTotal = montantTotal;
    }

    public String getReference() {
        return reference;
    }

    public Etudiant getEtudiant() {
        return etudiant;
    }

    public Produit getProduit() {
        return produit;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public int getDuree() {
        return duree;
    }

    public BigDecimal getMontantLocation() {
        return montantLocation;
    }

    public BigDecimal getCaution() {
        return caution;
    }

    public BigDecimal getMontantTotal() {
        return montantTotal;
    }
}