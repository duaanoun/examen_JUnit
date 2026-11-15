package ma.ehei.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Produit {

    private String id;
    private String libelle;
    private BigDecimal tarifJournalier;
    private BigDecimal caution;

    public Produit(String id, String libelle,
                   BigDecimal tarifJournalier, BigDecimal caution) {

        if (tarifJournalier == null ||
                tarifJournalier.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Le tarif journalier doit être strictement positif");
        }

        if (caution == null ||
                caution.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "La caution doit être positive ou nulle");
        }

        this.id = id;
        this.libelle = libelle;
        this.tarifJournalier = tarifJournalier;
        this.caution = caution.setScale(2, RoundingMode.HALF_UP);
    }

    public String getId() {
        return id;
    }

    public String getLibelle() {
        return libelle;
    }

    public BigDecimal getTarifJournalier() {
        return tarifJournalier;
    }

    public BigDecimal getCaution() {
        return caution;
    }
}