package ma.ehei.service;

import ma.ehei.exception.ReservationException;
import ma.ehei.repository.EtudiantRepository;
import ma.ehei.repository.ProduitRepository;
import ma.ehei.repository.CommandeRepository;
import ma.ehei.model.Commande;
import ma.ehei.model.Etudiant;
import ma.ehei.model.Produit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

public class ReservationService {

    private final EtudiantRepository etudiantRepository;
    private final ProduitRepository produitRepository;
    private final CommandeRepository commandeRepository;
    private final Clock clock;

    public ReservationService(
            EtudiantRepository etudiantRepository,
            ProduitRepository produitRepository,
            CommandeRepository commandeRepository,
            Clock clock) {

        this.etudiantRepository = etudiantRepository;
        this.produitRepository = produitRepository;
        this.commandeRepository = commandeRepository;
        this.clock = clock;
    }

    public Commande reserver(
            String etudiantId,
            String produitId,
            LocalDate dateDebut,
            LocalDate dateFin) {

        if (etudiantId == null || etudiantId.isBlank()) {
            throw new ReservationException(
                    "L'identifiant de l'étudiant est obligatoire.");
        }

        if (produitId == null || produitId.isBlank()) {
            throw new ReservationException(
                    "L'identifiant du produit est obligatoire.");
        }

        if (dateDebut == null) {
            throw new ReservationException(
                    "La date de début est obligatoire.");
        }

        if (dateFin == null) {
            throw new ReservationException(
                    "La date de fin est obligatoire.");
        }

        LocalDate aujourdhui = LocalDate.now(clock);

        if (dateDebut.isBefore(aujourdhui)) {
            throw new ReservationException(
                    "La date de début ne peut pas être antérieure à aujourd'hui.");
        }

        if (dateFin.isBefore(dateDebut)) {
            throw new ReservationException(
                    "La date de fin ne peut pas être antérieure à la date de début.");
        }

        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() ->
                        new ReservationException("Étudiant introuvable."));

        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() ->
                        new ReservationException("Produit introuvable."));

        int duree = (int) ChronoUnit.DAYS.between(dateDebut, dateFin) + 1;

        if (duree > 7) {
            throw new ReservationException(
                    "La durée maximale de réservation est de 7 jours.");
        }

        // Vérifier le quota de réservations de l'étudiant
        List<Commande> commandesEtudiant =
                commandeRepository.findByEtudiantId(etudiantId);

        long nombreCommandesActives = commandesEtudiant.stream()
                .filter(c -> !c.getDateFin().isBefore(aujourdhui))
                .count();

        if (nombreCommandesActives >= 2) {
            throw new ReservationException(
                    "L'étudiant ne peut pas avoir plus de deux réservations en cours ou futures.");
        }

        // Vérifier la disponibilité du produit
        List<Commande> commandesProduit =
                commandeRepository.findByProduitId(produitId);

        boolean produitIndisponible = commandesProduit.stream()
                .anyMatch(c ->
                        !dateDebut.isAfter(c.getDateFin())
                                && !dateFin.isBefore(c.getDateDebut())
                );

        if (produitIndisponible) {
            throw new ReservationException(
                    "Le produit est déjà réservé pendant cette période.");
        }

        // Calculer le montant de location
        BigDecimal montantLocation = produit.getTarifJournalier()
                .multiply(BigDecimal.valueOf(duree))
                .setScale(2, RoundingMode.HALF_UP);

        // Récupérer la caution
        BigDecimal caution = produit.getCaution()
                .setScale(2, RoundingMode.HALF_UP);

        // Calculer le montant total
        BigDecimal montantTotal = montantLocation
                .add(caution)
                .setScale(2, RoundingMode.HALF_UP);

        // Créer la commande
        Commande commande = new Commande(
                UUID.randomUUID().toString(),
                etudiant,
                produit,
                dateDebut,
                dateFin,
                duree,
                montantLocation,
                caution,
                montantTotal
        );

        // Enregistrer la commande
        commandeRepository.save(commande);

        return commande;
    }
}