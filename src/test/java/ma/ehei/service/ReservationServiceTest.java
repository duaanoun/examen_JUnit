package ma.ehei.service;

import ma.ehei.exception.ReservationException;
import ma.ehei.repository.EtudiantRepository;
import ma.ehei.repository.ProduitRepository;
import ma.ehei.repository.CommandeRepository;
import ma.ehei.model.Etudiant;
import ma.ehei.model.Produit;
import ma.ehei.model.Commande;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

class ReservationServiceTest {

    // ==================================================
    // RG01 : Vérification des données obligatoires
    // ==================================================
    // Les identifiants de l'étudiant et du produit,
    // ainsi que les dates de réservation, sont obligatoires.

    @Test
    void doitRefuserReservationSiIdEtudiantEstNull() {

        // Arrange
        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        ReservationService service = new ReservationService(
                etudiantRepository,
                produitRepository,
                commandeRepository,
                Clock.systemDefaultZone()
        );

        // Act + Assert
        assertThrows(ReservationException.class, () ->
                service.reserver(
                        null,
                        "P001",
                        java.time.LocalDate.now(),
                        java.time.LocalDate.now()
                )
        );

        // Vérifier qu'aucune commande n'a été enregistrée
        verify(commandeRepository, never()).save(any());
    }

    @Test
    void doitRefuserReservationSiIdEtudiantEstVide() {

        // Arrange
        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        ReservationService service = new ReservationService(
                etudiantRepository,
                produitRepository,
                commandeRepository,
                Clock.systemDefaultZone()
        );

        // Act + Assert
        assertThrows(ReservationException.class, () ->
                service.reserver(
                        "",
                        "P001",
                        java.time.LocalDate.now(),
                        java.time.LocalDate.now()
                )
        );

        verify(commandeRepository, never()).save(any());
    }

    @Test
    void doitRefuserReservationSiIdProduitEstNull() {
        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        ReservationService service = new ReservationService(
                etudiantRepository,
                produitRepository,
                commandeRepository,
                Clock.systemDefaultZone()
        );

        assertThrows(ReservationException.class, () ->
                service.reserver(
                        "E001",
                        null,
                        java.time.LocalDate.now(),
                        java.time.LocalDate.now()
                )
        );

        verify(commandeRepository, never()).save(any());
    }

    @Test
    void doitRefuserReservationSiIdProduitEstVide() {
        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        ReservationService service = new ReservationService(
                etudiantRepository,
                produitRepository,
                commandeRepository,
                Clock.systemDefaultZone()
        );

        assertThrows(ReservationException.class, () ->
                service.reserver(
                        "E001",
                        "",
                        java.time.LocalDate.now(),
                        java.time.LocalDate.now()
                )
        );

        verify(commandeRepository, never()).save(any());
    }

    @Test
    void doitRefuserReservationSiDateDebutEstNull() {
        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        ReservationService service = new ReservationService(
                etudiantRepository,
                produitRepository,
                commandeRepository,
                Clock.systemDefaultZone()
        );

        assertThrows(ReservationException.class, () ->
                service.reserver(
                        "E001",
                        "P001",
                        null,
                        java.time.LocalDate.now()
                )
        );

        verify(commandeRepository, never()).save(any());
    }

    @Test
    void doitRefuserReservationSiDateFinEstNull() {
        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        ReservationService service = new ReservationService(
                etudiantRepository,
                produitRepository,
                commandeRepository,
                Clock.systemDefaultZone()
        );

        assertThrows(ReservationException.class, () ->
                service.reserver(
                        "E001",
                        "P001",
                        java.time.LocalDate.now(),
                        null
                )
        );

        verify(commandeRepository, never()).save(any());
    }


    // ==================================================
    // RG02 : Vérification de la date de début
    // ==================================================
    // La date de début ne doit pas être dans le passé.
    // Une réservation peut commencer aujourd'hui ou dans le futur.

    @Test
    void doitRefuserReservationSiDateDebutEstDansLePasse() {
        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository,
                produitRepository,
                commandeRepository,
                clock
        );

        assertThrows(ReservationException.class, () ->
                service.reserver(
                        "E001",
                        "P001",
                        java.time.LocalDate.of(2026, 11, 14),
                        java.time.LocalDate.of(2026, 11, 16)
                )
        );

        verify(commandeRepository, never()).save(any());
    }

    @Test
    void doitAccepterReservationSiDateDebutEstAujourdhui() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Etudiant etudiant = new Etudiant("E001", "Alami", "Sara");
        Produit produit = new Produit(
                "P001", "Ordinateur",
                new BigDecimal("100.00"),
                new BigDecimal("200.00")
        );

        when(etudiantRepository.findById("E001"))
                .thenReturn(Optional.of(etudiant));

        when(produitRepository.findById("P001"))
                .thenReturn(Optional.of(produit));

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository,
                produitRepository,
                commandeRepository,
                clock
        );

        assertDoesNotThrow(() ->
                service.reserver(
                        "E001",
                        "P001",
                        java.time.LocalDate.of(2026, 11, 15),
                        java.time.LocalDate.of(2026, 11, 16)
                )
        );
    }

    @Test
    void doitAccepterReservationSiDateDebutEstDansLeFutur() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Etudiant etudiant = new Etudiant("E001", "Alami", "Sara");
        Produit produit = new Produit(
                "P001", "Ordinateur",
                new BigDecimal("100.00"),
                new BigDecimal("200.00")
        );

        when(etudiantRepository.findById("E001"))
                .thenReturn(Optional.of(etudiant));

        when(produitRepository.findById("P001"))
                .thenReturn(Optional.of(produit));

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository,
                produitRepository,
                commandeRepository,
                clock
        );

        assertDoesNotThrow(() ->
                service.reserver(
                        "E001",
                        "P001",
                        java.time.LocalDate.of(2026, 11, 20),
                        java.time.LocalDate.of(2026, 11, 22)
                )
        );
    }


    // ==================================================
    // RG03 : Vérification de la période de réservation
    // ==================================================
    // La date de fin doit être supérieure ou égale
    // à la date de début.

    @Test
    void doitRefuserReservationSiDateFinEstAvantDateDebut() {
        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository,
                produitRepository,
                commandeRepository,
                clock
        );

        assertThrows(ReservationException.class, () ->
                service.reserver(
                        "E001",
                        "P001",
                        java.time.LocalDate.of(2026, 11, 20),
                        java.time.LocalDate.of(2026, 11, 19)
                )
        );

        verify(commandeRepository, never()).save(any());
    }


    // ==================================================
    // RG04 : Calcul et limitation de la durée
    // ==================================================
    // La durée est calculée en jours en incluant
    // le premier et le dernier jour.
    // La durée maximale autorisée est de 7 jours.

    @Test
    void doitCalculerUneJourneeSiDateDebutEgaleDateFin() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Etudiant etudiant = new Etudiant("E001", "Alami", "Sara");

        Produit produit = new Produit(
                "P001",
                "Ordinateur",
                new java.math.BigDecimal("100.00"),
                new java.math.BigDecimal("200.00")
        );

        when(etudiantRepository.findById("E001"))
                .thenReturn(java.util.Optional.of(etudiant));

        when(produitRepository.findById("P001"))
                .thenReturn(java.util.Optional.of(produit));

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository,
                produitRepository,
                commandeRepository,
                clock
        );

        Commande commande = service.reserver(
                "E001",
                "P001",
                java.time.LocalDate.of(2026, 11, 20),
                java.time.LocalDate.of(2026, 11, 20)
        );

        assertNotNull(commande);
        assertEquals(1, commande.getDuree());

        verify(commandeRepository).save(any(Commande.class));
    }


    // ==================================================
    // RG05 : Vérification de l'existence des entités
    // ==================================================
    // L'étudiant et le produit doivent exister
    // dans leurs dépôts respectifs.

    @Test
    void doitRefuserReservationSiEtudiantIntrouvable() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        when(etudiantRepository.findById("E999"))
                .thenReturn(Optional.empty());

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository,
                produitRepository,
                commandeRepository,
                clock
        );

        assertThrows(ReservationException.class, () ->
                service.reserver(
                        "E999",
                        "P001",
                        java.time.LocalDate.of(2026, 11, 20),
                        java.time.LocalDate.of(2026, 11, 22)
                )
        );

        verify(commandeRepository, never()).save(any());
    }

    @Test
    void doitRefuserReservationSiProduitIntrouvable() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Etudiant etudiant = new Etudiant("E001", "Alami", "Sara");

        when(etudiantRepository.findById("E001"))
                .thenReturn(Optional.of(etudiant));

        when(produitRepository.findById("P999"))
                .thenReturn(Optional.empty());

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository,
                produitRepository,
                commandeRepository,
                clock
        );

        assertThrows(ReservationException.class, () ->
                service.reserver(
                        "E001",
                        "P999",
                        java.time.LocalDate.of(2026, 11, 20),
                        java.time.LocalDate.of(2026, 11, 22)
                )
        );

        verify(commandeRepository, never()).save(any());
    }


    // ==================================================
    // RG06 : Réservation avec des données valides
    // ==================================================
    // Vérifier qu'une réservation est acceptée lorsque
    // l'étudiant et le produit existent.

    @Test
    void doitAccepterReservationSiEtudiantEtProduitExistent() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Etudiant etudiant = new Etudiant("E001", "Alami", "Sara");

        Produit produit = new Produit(
                "P001", "Ordinateur",
                new BigDecimal("100.00"),
                new BigDecimal("200.00")
        );

        when(etudiantRepository.findById("E001"))
                .thenReturn(Optional.of(etudiant));

        when(produitRepository.findById("P001"))
                .thenReturn(Optional.of(produit));

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository,
                produitRepository,
                commandeRepository,
                clock
        );

        Commande commande = service.reserver(
                "E001", "P001",
                java.time.LocalDate.of(2026, 11, 20),
                java.time.LocalDate.of(2026, 11, 22)
        );

        assertNotNull(commande);
        assertEquals(etudiant, commande.getEtudiant());
        assertEquals(produit, commande.getProduit());

        verify(commandeRepository).save(any(Commande.class));

        // Couverture de EtudiantRepository
        EtudiantRepository etudiants = new EtudiantRepository();
        etudiants.save(etudiant);

        assertTrue(etudiants.findById("E001").isPresent());
        assertTrue(etudiants.findById("E999").isEmpty());
        assertEquals(1, etudiants.findAll().size());

        // Couverture de ProduitRepository
        ProduitRepository produits = new ProduitRepository();
        produits.save(produit);

        assertTrue(produits.findById("P001").isPresent());
        assertTrue(produits.findById("P999").isEmpty());
        assertEquals(1, produits.findAll().size());

        // Couverture de CommandeRepository
        CommandeRepository commandes = new CommandeRepository();

        Etudiant autreEtudiant = new Etudiant("E002", "Test", "User");

        Produit autreProduit = new Produit(
                "P002", "Clavier",
                new BigDecimal("50.00"),
                BigDecimal.ZERO
        );

        Commande autreCommande = new Commande(
                "REF002",
                autreEtudiant,
                autreProduit,
                java.time.LocalDate.of(2026, 11, 20),
                java.time.LocalDate.of(2026, 11, 21),
                2,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        commandes.save(commande);
        commandes.save(autreCommande);

        assertEquals(2, commandes.findAll().size());
        assertEquals(2, commandes.count());

        assertEquals(1, commandes.findByEtudiantId("E001").size());
        assertEquals(0, commandes.findByEtudiantId("E999").size());

        assertEquals(1, commandes.findByProduitId("P001").size());
        assertEquals(0, commandes.findByProduitId("P999").size());

        // Couverture des getters de Etudiant
        assertEquals("E001", etudiant.getId());
        assertEquals("Alami", etudiant.getNom());
        assertEquals("Sara", etudiant.getPrenom());

        // Couverture des getters de Produit
        assertEquals("P001", produit.getId());
        assertEquals("Ordinateur", produit.getLibelle());
        assertNotNull(produit.getTarifJournalier());
        assertNotNull(produit.getCaution());

        // Couverture des getters de Commande
        assertNotNull(commande.getReference());
        assertNotNull(commande.getEtudiant());
        assertNotNull(commande.getProduit());
        assertNotNull(commande.getDateDebut());
        assertNotNull(commande.getDateFin());
        assertEquals(3, commande.getDuree());
        assertNotNull(commande.getMontantLocation());
        assertNotNull(commande.getMontantTotal());
    }

    @Test
    void doitCalculerUneJourneePourUneReservationSurUnJour() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Etudiant etudiant = new Etudiant("E001", "Alami", "Sara");
        Produit produit = new Produit(
                "P001", "Ordinateur",
                new BigDecimal("100.00"),
                new BigDecimal("200.00")
        );

        when(etudiantRepository.findById("E001"))
                .thenReturn(Optional.of(etudiant));

        when(produitRepository.findById("P001"))
                .thenReturn(Optional.of(produit));

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository,
                produitRepository,
                commandeRepository,
                clock
        );

        Commande commande = service.reserver(
                "E001",
                "P001",
                java.time.LocalDate.of(2026, 11, 20),
                java.time.LocalDate.of(2026, 11, 20)
        );

        assertEquals(1, commande.getDuree());
    }

    @Test
    void doitCalculerCinqJoursDeReservation() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Etudiant etudiant = new Etudiant("E001", "Alami", "Sara");
        Produit produit = new Produit(
                "P001", "Ordinateur",
                new BigDecimal("100.00"),
                new BigDecimal("200.00")
        );

        when(etudiantRepository.findById("E001"))
                .thenReturn(Optional.of(etudiant));

        when(produitRepository.findById("P001"))
                .thenReturn(Optional.of(produit));

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository, produitRepository, commandeRepository, clock
        );

        Commande commande = service.reserver(
                "E001", "P001",
                java.time.LocalDate.of(2026, 11, 20),
                java.time.LocalDate.of(2026, 11, 24)
        );

        assertEquals(5, commande.getDuree());
    }

    @Test
    void doitAccepterReservationDeSeptJours() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Etudiant etudiant = new Etudiant("E001", "Alami", "Sara");
        Produit produit = new Produit(
                "P001", "Ordinateur",
                new BigDecimal("100.00"),
                new BigDecimal("200.00")
        );

        when(etudiantRepository.findById("E001"))
                .thenReturn(Optional.of(etudiant));

        when(produitRepository.findById("P001"))
                .thenReturn(Optional.of(produit));

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository, produitRepository, commandeRepository, clock
        );

        Commande commande = service.reserver(
                "E001", "P001",
                java.time.LocalDate.of(2026, 11, 20),
                java.time.LocalDate.of(2026, 11, 26)
        );

        assertEquals(7, commande.getDuree());
        verify(commandeRepository).save(any(Commande.class));
    }

    @Test
    void doitRefuserReservationDePlusDeSeptJours() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Etudiant etudiant = new Etudiant("E001", "Alami", "Sara");
        Produit produit = new Produit(
                "P001", "Ordinateur",
                new BigDecimal("100.00"),
                new BigDecimal("200.00")
        );

        when(etudiantRepository.findById("E001"))
                .thenReturn(Optional.of(etudiant));

        when(produitRepository.findById("P001"))
                .thenReturn(Optional.of(produit));

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository, produitRepository, commandeRepository, clock
        );

        assertThrows(ReservationException.class, () ->
                service.reserver(
                        "E001", "P001",
                        java.time.LocalDate.of(2026, 11, 20),
                        java.time.LocalDate.of(2026, 11, 27)
                )
        );

        verify(commandeRepository, never()).save(any());
    }


    // ==================================================
    // RG07 : Limitation du nombre de commandes en cours
    // ==================================================
    // Un étudiant ne peut pas avoir plus de deux commandes en cours.
    // Les commandes terminées ne sont pas comptabilisées.
    // Une commande qui se termine aujourd'hui reste active.

    @Test
    void doitAccepterReservationSiEtudiantNaAucuneCommandeEnCours() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Etudiant etudiant = new Etudiant("E001", "Alami", "Sara");
        Produit produit = new Produit(
                "P001", "Ordinateur",
                new BigDecimal("100.00"),
                new BigDecimal("200.00")
        );

        when(etudiantRepository.findById("E001"))
                .thenReturn(Optional.of(etudiant));

        when(produitRepository.findById("P001"))
                .thenReturn(Optional.of(produit));

        when(commandeRepository.findByEtudiantId("E001"))
                .thenReturn(java.util.Collections.emptyList());

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository, produitRepository, commandeRepository, clock
        );

        Commande commande = service.reserver(
                "E001", "P001",
                java.time.LocalDate.of(2026, 11, 20),
                java.time.LocalDate.of(2026, 11, 22)
        );

        assertNotNull(commande);
        verify(commandeRepository).save(any(Commande.class));
    }

    @Test
    void doitAccepterReservationSiEtudiantAUneSeuleCommandeEnCours() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Etudiant etudiant = new Etudiant("E001", "Alami", "Sara");

        Produit produit = new Produit(
                "P001", "Ordinateur",
                new BigDecimal("100.00"),
                new BigDecimal("200.00")
        );

        when(etudiantRepository.findById("E001"))
                .thenReturn(Optional.of(etudiant));

        when(produitRepository.findById("P001"))
                .thenReturn(Optional.of(produit));

        Commande ancienneCommande = new Commande(
                "REF001", etudiant, produit,
                java.time.LocalDate.of(2026, 11, 10),
                java.time.LocalDate.of(2026, 11, 16),
                7,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        when(commandeRepository.findByEtudiantId("E001"))
                .thenReturn(java.util.List.of(ancienneCommande));

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository, produitRepository, commandeRepository, clock
        );

        Commande commande = service.reserver(
                "E001", "P001",
                java.time.LocalDate.of(2026, 11, 20),
                java.time.LocalDate.of(2026, 11, 22)
        );

        assertNotNull(commande);
        verify(commandeRepository).save(any(Commande.class));
    }

    @Test
    void doitRefuserReservationSiEtudiantADejaDeuxCommandesEnCours() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Etudiant etudiant = new Etudiant("E001", "Alami", "Sara");

        Produit produit = new Produit(
                "P001", "Ordinateur",
                new BigDecimal("100.00"),
                new BigDecimal("200.00")
        );

        when(etudiantRepository.findById("E001"))
                .thenReturn(Optional.of(etudiant));

        when(produitRepository.findById("P001"))
                .thenReturn(Optional.of(produit));

        Commande commande1 = new Commande(
                "REF001", etudiant, produit,
                java.time.LocalDate.of(2026, 11, 10),
                java.time.LocalDate.of(2026, 11, 16),
                7, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );

        Commande commande2 = new Commande(
                "REF002", etudiant, produit,
                java.time.LocalDate.of(2026, 11, 12),
                java.time.LocalDate.of(2026, 11, 18),
                7, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );

        when(commandeRepository.findByEtudiantId("E001"))
                .thenReturn(java.util.List.of(commande1, commande2));

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository, produitRepository, commandeRepository, clock
        );

        assertThrows(ReservationException.class, () ->
                service.reserver(
                        "E001", "P001",
                        java.time.LocalDate.of(2026, 11, 20),
                        java.time.LocalDate.of(2026, 11, 22)
                )
        );

        verify(commandeRepository, never()).save(any());
    }

    @Test
    void doitIgnorerLesCommandesTermineesDansLeCalculDuQuota() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Etudiant etudiant = new Etudiant("E001", "Alami", "Sara");

        Produit produit = new Produit(
                "P001", "Ordinateur",
                new BigDecimal("100.00"),
                new BigDecimal("200.00")
        );

        when(etudiantRepository.findById("E001"))
                .thenReturn(Optional.of(etudiant));

        when(produitRepository.findById("P001"))
                .thenReturn(Optional.of(produit));

        Commande commande1 = new Commande(
                "REF001", etudiant, produit,
                java.time.LocalDate.of(2026, 11, 1),
                java.time.LocalDate.of(2026, 11, 5),
                5, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );

        Commande commande2 = new Commande(
                "REF002", etudiant, produit,
                java.time.LocalDate.of(2026, 11, 6),
                java.time.LocalDate.of(2026, 11, 10),
                5, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );

        when(commandeRepository.findByEtudiantId("E001"))
                .thenReturn(java.util.List.of(commande1, commande2));

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository, produitRepository, commandeRepository, clock
        );

        Commande nouvelleCommande = service.reserver(
                "E001", "P001",
                java.time.LocalDate.of(2026, 11, 20),
                java.time.LocalDate.of(2026, 11, 22)
        );

        assertNotNull(nouvelleCommande);
        verify(commandeRepository).save(any(Commande.class));
    }

    @Test
    void doitCompterUneCommandeQuiSeTermineAujourdhui() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Etudiant etudiant = new Etudiant("E001", "Alami", "Sara");

        Produit produit = new Produit(
                "P001", "Ordinateur",
                new BigDecimal("100.00"),
                new BigDecimal("200.00")
        );

        when(etudiantRepository.findById("E001"))
                .thenReturn(Optional.of(etudiant));

        when(produitRepository.findById("P001"))
                .thenReturn(Optional.of(produit));

        Commande commande1 = new Commande(
                "REF001", etudiant, produit,
                java.time.LocalDate.of(2026, 11, 10),
                java.time.LocalDate.of(2026, 11, 15),
                6, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );

        Commande commande2 = new Commande(
                "REF002", etudiant, produit,
                java.time.LocalDate.of(2026, 11, 12),
                java.time.LocalDate.of(2026, 11, 18),
                7, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );

        when(commandeRepository.findByEtudiantId("E001"))
                .thenReturn(java.util.List.of(commande1, commande2));

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository, produitRepository, commandeRepository, clock
        );

        assertThrows(ReservationException.class, () ->
                service.reserver(
                        "E001", "P001",
                        java.time.LocalDate.of(2026, 11, 20),
                        java.time.LocalDate.of(2026, 11, 22)
                )
        );

        verify(commandeRepository, never()).save(any());
    }

    @Test
    void doitIgnorerUneCommandeTermineeHierDansLeCalculDuQuota() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Etudiant etudiant = new Etudiant("E001", "Alami", "Sara");

        Produit produit = new Produit(
                "P001", "Ordinateur",
                new BigDecimal("100.00"),
                new BigDecimal("200.00")
        );

        when(etudiantRepository.findById("E001"))
                .thenReturn(Optional.of(etudiant));

        when(produitRepository.findById("P001"))
                .thenReturn(Optional.of(produit));

        // Commande terminée hier : 14/11/2026
        Commande ancienneCommande = new Commande(
                "REF001", etudiant, produit,
                java.time.LocalDate.of(2026, 11, 10),
                java.time.LocalDate.of(2026, 11, 14),
                5,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        when(commandeRepository.findByEtudiantId("E001"))
                .thenReturn(java.util.List.of(ancienneCommande));

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository, produitRepository, commandeRepository, clock
        );

        // Act
        Commande nouvelleCommande = service.reserver(
                "E001", "P001",
                java.time.LocalDate.of(2026, 11, 20),
                java.time.LocalDate.of(2026, 11, 22)
        );

        // Assert
        assertNotNull(nouvelleCommande);

        verify(commandeRepository).save(any(Commande.class));
    }


    // ==================================================
    // RG08 : Vérification de la disponibilité du produit
    // ==================================================
    // Un produit ne peut pas être réservé pendant une période
    // qui chevauche une réservation existante.
    // Les périodes successives sans chevauchement sont autorisées.

    @Test
    void doitAccepterReservationSiAucuneReservationExistantePourLeProduit() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Etudiant etudiant = new Etudiant("E001", "Alami", "Sara");

        Produit produit = new Produit(
                "P001", "Ordinateur",
                new BigDecimal("100.00"),
                new BigDecimal("200.00")
        );

        when(etudiantRepository.findById("E001"))
                .thenReturn(Optional.of(etudiant));

        when(produitRepository.findById("P001"))
                .thenReturn(Optional.of(produit));

        when(commandeRepository.findByEtudiantId("E001"))
                .thenReturn(java.util.Collections.emptyList());

        when(commandeRepository.findByProduitId("P001"))
                .thenReturn(java.util.Collections.emptyList());

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository, produitRepository, commandeRepository, clock
        );

        Commande commande = service.reserver(
                "E001", "P001",
                java.time.LocalDate.of(2026, 11, 20),
                java.time.LocalDate.of(2026, 11, 22)
        );

        assertNotNull(commande);
        verify(commandeRepository).save(any(Commande.class));
    }

    @Test
    void doitAccepterReservationSurUnePeriodeDifferente() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Etudiant etudiant = new Etudiant("E001", "Alami", "Sara");
        Produit produit = new Produit(
                "P001", "Ordinateur",
                new BigDecimal("100.00"),
                new BigDecimal("200.00")
        );

        when(etudiantRepository.findById("E001"))
                .thenReturn(Optional.of(etudiant));

        when(produitRepository.findById("P001"))
                .thenReturn(Optional.of(produit));

        Commande ancienneCommande = new Commande(
                "REF001", etudiant, produit,
                java.time.LocalDate.of(2026, 12, 1),
                java.time.LocalDate.of(2026, 12, 3),
                3, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );

        when(commandeRepository.findByEtudiantId("E001"))
                .thenReturn(java.util.Collections.emptyList());

        when(commandeRepository.findByProduitId("P001"))
                .thenReturn(java.util.List.of(ancienneCommande));

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository, produitRepository, commandeRepository, clock
        );

        Commande commande = service.reserver(
                "E001", "P001",
                java.time.LocalDate.of(2026, 12, 5),
                java.time.LocalDate.of(2026, 12, 7)
        );

        assertNotNull(commande);
        verify(commandeRepository).save(any(Commande.class));
    }

    @Test
    void doitRefuserReservationSiPeriodeSeChevaucheAvecUneReservationExistante() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Etudiant etudiant = new Etudiant("E001", "Alami", "Sara");
        Produit produit = new Produit(
                "P001", "Ordinateur",
                new BigDecimal("100.00"),
                new BigDecimal("200.00")
        );

        when(etudiantRepository.findById("E001"))
                .thenReturn(Optional.of(etudiant));

        when(produitRepository.findById("P001"))
                .thenReturn(Optional.of(produit));

        Commande ancienneCommande = new Commande(
                "REF001", etudiant, produit,
                java.time.LocalDate.of(2026, 12, 10),
                java.time.LocalDate.of(2026, 12, 15),
                6, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );

        when(commandeRepository.findByEtudiantId("E001"))
                .thenReturn(java.util.Collections.emptyList());

        when(commandeRepository.findByProduitId("P001"))
                .thenReturn(java.util.List.of(ancienneCommande));

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository, produitRepository, commandeRepository, clock
        );

        assertThrows(ReservationException.class, () ->
                service.reserver(
                        "E001", "P001",
                        java.time.LocalDate.of(2026, 12, 12),
                        java.time.LocalDate.of(2026, 12, 17)
                )
        );

        verify(commandeRepository, never()).save(any());
    }

    @Test
    void doitRefuserReservationSiDateDebutCorrespondALaDateFinExistante() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Etudiant etudiant = new Etudiant("E001", "Alami", "Sara");
        Produit produit = new Produit(
                "P001", "Ordinateur",
                new BigDecimal("100.00"),
                new BigDecimal("200.00")
        );

        when(etudiantRepository.findById("E001"))
                .thenReturn(Optional.of(etudiant));

        when(produitRepository.findById("P001"))
                .thenReturn(Optional.of(produit));

        Commande ancienneCommande = new Commande(
                "REF001", etudiant, produit,
                java.time.LocalDate.of(2026, 12, 10),
                java.time.LocalDate.of(2026, 12, 12),
                3, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );

        when(commandeRepository.findByEtudiantId("E001"))
                .thenReturn(java.util.Collections.emptyList());

        when(commandeRepository.findByProduitId("P001"))
                .thenReturn(java.util.List.of(ancienneCommande));

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository, produitRepository, commandeRepository, clock
        );

        assertThrows(ReservationException.class, () ->
                service.reserver(
                        "E001", "P001",
                        java.time.LocalDate.of(2026, 12, 12),
                        java.time.LocalDate.of(2026, 12, 15)
                )
        );

        verify(commandeRepository, never()).save(any());
    }

    @Test
    void doitRefuserReservationSiDateFinCorrespondALaDateDebutExistante() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Etudiant etudiant = new Etudiant("E001", "Alami", "Sara");
        Produit produit = new Produit(
                "P001", "Ordinateur",
                new BigDecimal("100.00"),
                new BigDecimal("200.00")
        );

        when(etudiantRepository.findById("E001"))
                .thenReturn(Optional.of(etudiant));

        when(produitRepository.findById("P001"))
                .thenReturn(Optional.of(produit));

        Commande ancienneCommande = new Commande(
                "REF001", etudiant, produit,
                java.time.LocalDate.of(2026, 12, 12),
                java.time.LocalDate.of(2026, 12, 15),
                4, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );

        when(commandeRepository.findByEtudiantId("E001"))
                .thenReturn(java.util.Collections.emptyList());

        when(commandeRepository.findByProduitId("P001"))
                .thenReturn(java.util.List.of(ancienneCommande));

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository, produitRepository, commandeRepository, clock
        );

        assertThrows(ReservationException.class, () ->
                service.reserver(
                        "E001", "P001",
                        java.time.LocalDate.of(2026, 12, 10),
                        java.time.LocalDate.of(2026, 12, 12)
                )
        );

        verify(commandeRepository, never()).save(any());
    }

    @Test
    void doitAccepterDeuxReservationsSuccessivesSansChevauchement() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Etudiant etudiant = new Etudiant("E001", "Alami", "Sara");
        Produit produit = new Produit(
                "P001", "Ordinateur",
                new BigDecimal("100.00"),
                new BigDecimal("200.00")
        );

        when(etudiantRepository.findById("E001"))
                .thenReturn(Optional.of(etudiant));

        when(produitRepository.findById("P001"))
                .thenReturn(Optional.of(produit));

        Commande ancienneCommande = new Commande(
                "REF001", etudiant, produit,
                java.time.LocalDate.of(2026, 12, 10),
                java.time.LocalDate.of(2026, 12, 12),
                3, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );

        when(commandeRepository.findByEtudiantId("E001"))
                .thenReturn(java.util.Collections.emptyList());

        when(commandeRepository.findByProduitId("P001"))
                .thenReturn(java.util.List.of(ancienneCommande));

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository, produitRepository, commandeRepository, clock
        );

        Commande commande = service.reserver(
                "E001", "P001",
                java.time.LocalDate.of(2026, 12, 13),
                java.time.LocalDate.of(2026, 12, 15)
        );

        assertNotNull(commande);
        verify(commandeRepository).save(any(Commande.class));
    }


    // ==================================================
    // RG09 : Validation du Produit
    // ==================================================
    // Le tarif journalier doit être strictement positif.
    // La caution doit être positive ou nulle.

    @Test
    void doitAccepterUnTarifJournalierPositif() {

        Produit produit = new Produit(
                "P001", "Ordinateur",
                new BigDecimal("100.00"),
                new BigDecimal("200.00")
        );

        assertEquals(
                new BigDecimal("100.00"),
                produit.getTarifJournalier()
        );
    }

    @Test
    void doitRefuserUnTarifJournalierNul() {

        assertThrows(IllegalArgumentException.class, () ->
                new Produit(
                        "P001", "Ordinateur",
                        BigDecimal.ZERO,
                        new BigDecimal("200.00")
                )
        );

        assertThrows(IllegalArgumentException.class, () ->
                new Produit(
                        "P001", "Ordinateur",
                        null,
                        new BigDecimal("200.00")
                )
        );
    }

    @Test
    void doitRefuserUnTarifJournalierNegatif() {

        assertThrows(IllegalArgumentException.class, () ->
                new Produit(
                        "P001", "Ordinateur",
                        new BigDecimal("-100.00"),
                        new BigDecimal("200.00")
                )
        );
    }

    @Test
    void doitAccepterUneCautionNulle() {

        Produit produit = new Produit(
                "P001", "Ordinateur",
                new BigDecimal("100.00"),
                BigDecimal.ZERO
        );

        assertEquals(
                new BigDecimal("0.00"),
                produit.getCaution()
        );
    }

    @Test
    void doitAccepterUneCautionPositive() {

        Produit produit = new Produit(
                "P001", "Ordinateur",
                new BigDecimal("100.00"),
                new BigDecimal("200.00")
        );

        assertEquals(
                new BigDecimal("200.00"),
                produit.getCaution()
        );
    }

    @Test
    void doitRefuserUneCautionNegative() {

        assertThrows(IllegalArgumentException.class, () ->
                new Produit(
                        "P001", "Ordinateur",
                        new BigDecimal("100.00"),
                        new BigDecimal("-200.00")
                )
        );
    }


    // ==================================================
    // RG10-RG12 : Calcul des montants
    // ==================================================
    // Le montant de location dépend du tarif journalier
    // et de la durée de réservation.
    // L'arrondi est effectué avec la méthode HALF_UP.
    // Le montant total correspond au montant de location
    // ajouté à la caution.

    @Test
    void doitCalculerLeMontantLocationSelonLaDuree() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Etudiant etudiant = new Etudiant("E001", "Alami", "Sara");
        Produit produit = new Produit(
                "P001", "Ordinateur",
                new BigDecimal("100.00"),
                new BigDecimal("200.00")
        );

        when(etudiantRepository.findById("E001"))
                .thenReturn(Optional.of(etudiant));

        when(produitRepository.findById("P001"))
                .thenReturn(Optional.of(produit));

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository, produitRepository, commandeRepository, clock
        );

        Commande commande = service.reserver(
                "E001", "P001",
                java.time.LocalDate.of(2026, 11, 20),
                java.time.LocalDate.of(2026, 11, 22)
        );

        assertEquals(
                new BigDecimal("300.00"),
                commande.getMontantLocation()
        );
    }

    @Test
    void doitArrondirLeMontantLocationAvecHalfUp() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Etudiant etudiant = new Etudiant("E001", "Alami", "Sara");
        Produit produit = new Produit(
                "P001", "Ordinateur",
                new BigDecimal("10.005"),
                new BigDecimal("20.00")
        );

        when(etudiantRepository.findById("E001"))
                .thenReturn(Optional.of(etudiant));

        when(produitRepository.findById("P001"))
                .thenReturn(Optional.of(produit));

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository, produitRepository, commandeRepository, clock
        );

        Commande commande = service.reserver(
                "E001", "P001",
                java.time.LocalDate.of(2026, 11, 20),
                java.time.LocalDate.of(2026, 11, 20)
        );

        assertEquals(
                new BigDecimal("10.01"),
                commande.getMontantLocation()
        );
    }

    @Test
    void doitCalculerLeMontantTotalAvecLaCaution() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Etudiant etudiant = new Etudiant("E001", "Alami", "Sara");
        Produit produit = new Produit(
                "P001", "Ordinateur",
                new BigDecimal("100.00"),
                new BigDecimal("200.00")
        );

        when(etudiantRepository.findById("E001"))
                .thenReturn(Optional.of(etudiant));

        when(produitRepository.findById("P001"))
                .thenReturn(Optional.of(produit));

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository, produitRepository, commandeRepository, clock
        );

        Commande commande = service.reserver(
                "E001", "P001",
                java.time.LocalDate.of(2026, 11, 20),
                java.time.LocalDate.of(2026, 11, 22)
        );

        assertEquals(
                new BigDecimal("500.00"),
                commande.getMontantTotal()
        );
    }

    @Test
    void doitCalculerLeMontantTotalSansCaution() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Etudiant etudiant = new Etudiant("E001", "Alami", "Sara");
        Produit produit = new Produit(
                "P001", "Ordinateur",
                new BigDecimal("100.00"),
                BigDecimal.ZERO
        );

        when(etudiantRepository.findById("E001"))
                .thenReturn(Optional.of(etudiant));

        when(produitRepository.findById("P001"))
                .thenReturn(Optional.of(produit));

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository, produitRepository, commandeRepository, clock
        );

        Commande commande = service.reserver(
                "E001", "P001",
                java.time.LocalDate.of(2026, 11, 20),
                java.time.LocalDate.of(2026, 11, 22)
        );

        assertEquals(
                commande.getMontantLocation(),
                commande.getMontantTotal()
        );
    }


    // ==================================================
    // RG13-RG14 : Référence et sauvegarde
    // ==================================================
    // Chaque commande doit posséder une référence unique.
    // Une réservation invalide ou impossible ne doit pas être enregistrée.
    // Une réservation valide doit être enregistrée une seule fois.

    @Test
    void doitGenererUneReferenceNonNulle() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Etudiant etudiant = new Etudiant("E001", "Alami", "Sara");
        Produit produit = new Produit(
                "P001", "Ordinateur",
                new BigDecimal("100.00"),
                new BigDecimal("200.00")
        );

        when(etudiantRepository.findById("E001"))
                .thenReturn(Optional.of(etudiant));

        when(produitRepository.findById("P001"))
                .thenReturn(Optional.of(produit));

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository, produitRepository, commandeRepository, clock
        );

        Commande commande = service.reserver(
                "E001", "P001",
                java.time.LocalDate.of(2026, 11, 20),
                java.time.LocalDate.of(2026, 11, 22)
        );

        assertNotNull(commande.getReference());
    }

    @Test
    void doitGenererDesReferencesUniques() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Etudiant etudiant = new Etudiant("E001", "Alami", "Sara");
        Produit produit = new Produit(
                "P001", "Ordinateur",
                new BigDecimal("100.00"),
                new BigDecimal("200.00")
        );

        when(etudiantRepository.findById("E001"))
                .thenReturn(Optional.of(etudiant));

        when(produitRepository.findById("P001"))
                .thenReturn(Optional.of(produit));

        when(commandeRepository.findByEtudiantId("E001"))
                .thenReturn(java.util.Collections.emptyList());

        when(commandeRepository.findByProduitId("P001"))
                .thenReturn(java.util.Collections.emptyList());

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository, produitRepository, commandeRepository, clock
        );

        Commande commande1 = service.reserver(
                "E001", "P001",
                java.time.LocalDate.of(2026, 11, 20),
                java.time.LocalDate.of(2026, 11, 22)
        );

        Commande commande2 = service.reserver(
                "E001", "P001",
                java.time.LocalDate.of(2026, 11, 23),
                java.time.LocalDate.of(2026, 11, 25)
        );

        assertNotEquals(
                commande1.getReference(),
                commande2.getReference()
        );
    }

    @Test
    void neDoitPasEnregistrerUneReservationInvalide() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        ReservationService service = new ReservationService(
                etudiantRepository, produitRepository, commandeRepository,
                Clock.systemDefaultZone()
        );

        assertThrows(ReservationException.class, () ->
                service.reserver(
                        null, "P001",
                        java.time.LocalDate.now(),
                        java.time.LocalDate.now()
                )
        );

        verify(commandeRepository, never()).save(any());
    }

    @Test
    void neDoitPasEnregistrerUneReservationPourUnProduitIndisponible() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Etudiant etudiant = new Etudiant("E001", "Alami", "Sara");
        Produit produit = new Produit(
                "P001", "Ordinateur",
                new BigDecimal("100.00"),
                new BigDecimal("200.00")
        );

        when(etudiantRepository.findById("E001"))
                .thenReturn(Optional.of(etudiant));

        when(produitRepository.findById("P001"))
                .thenReturn(Optional.of(produit));

        Commande ancienneCommande = new Commande(
                "REF001", etudiant, produit,
                java.time.LocalDate.of(2026, 12, 10),
                java.time.LocalDate.of(2026, 12, 15),
                6, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );

        when(commandeRepository.findByEtudiantId("E001"))
                .thenReturn(java.util.Collections.emptyList());

        when(commandeRepository.findByProduitId("P001"))
                .thenReturn(java.util.List.of(ancienneCommande));

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository, produitRepository, commandeRepository, clock
        );

        assertThrows(ReservationException.class, () ->
                service.reserver(
                        "E001", "P001",
                        java.time.LocalDate.of(2026, 12, 12),
                        java.time.LocalDate.of(2026, 12, 14)
                )
        );

        verify(commandeRepository, never()).save(any());
    }

    @Test
    void neDoitPasEnregistrerUneReservationSiQuotaDepasse() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Etudiant etudiant = new Etudiant("E001", "Alami", "Sara");
        Produit produit = new Produit(
                "P001", "Ordinateur",
                new BigDecimal("100.00"),
                new BigDecimal("200.00")
        );

        when(etudiantRepository.findById("E001"))
                .thenReturn(Optional.of(etudiant));

        when(produitRepository.findById("P001"))
                .thenReturn(Optional.of(produit));

        Commande commande1 = new Commande(
                "REF001", etudiant, produit,
                java.time.LocalDate.of(2026, 11, 10),
                java.time.LocalDate.of(2026, 11, 16),
                7, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );

        Commande commande2 = new Commande(
                "REF002", etudiant, produit,
                java.time.LocalDate.of(2026, 11, 12),
                java.time.LocalDate.of(2026, 11, 18),
                7, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );

        when(commandeRepository.findByEtudiantId("E001"))
                .thenReturn(java.util.List.of(commande1, commande2));

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository, produitRepository, commandeRepository, clock
        );

        assertThrows(ReservationException.class, () ->
                service.reserver(
                        "E001", "P001",
                        java.time.LocalDate.of(2026, 11, 20),
                        java.time.LocalDate.of(2026, 11, 22)
                )
        );

        verify(commandeRepository, never()).save(any());
    }

    @Test
    void doitEnregistrerExactementUneCommandePourUneReservationValide() {

        EtudiantRepository etudiantRepository = mock(EtudiantRepository.class);
        ProduitRepository produitRepository = mock(ProduitRepository.class);
        CommandeRepository commandeRepository = mock(CommandeRepository.class);

        Etudiant etudiant = new Etudiant("E001", "Alami", "Sara");
        Produit produit = new Produit(
                "P001", "Ordinateur",
                new BigDecimal("100.00"),
                new BigDecimal("200.00")
        );

        when(etudiantRepository.findById("E001"))
                .thenReturn(Optional.of(etudiant));

        when(produitRepository.findById("P001"))
                .thenReturn(Optional.of(produit));

        when(commandeRepository.findByEtudiantId("E001"))
                .thenReturn(java.util.Collections.emptyList());

        when(commandeRepository.findByProduitId("P001"))
                .thenReturn(java.util.Collections.emptyList());

        Clock clock = Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        ReservationService service = new ReservationService(
                etudiantRepository, produitRepository, commandeRepository, clock
        );

        Commande commande = service.reserver(
                "E001", "P001",
                java.time.LocalDate.of(2026, 11, 20),
                java.time.LocalDate.of(2026, 11, 22)
        );

        assertNotNull(commande);

        verify(commandeRepository, times(1)).save(any(Commande.class));
    }
}