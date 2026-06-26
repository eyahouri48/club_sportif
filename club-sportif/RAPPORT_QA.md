# Rapport QA — Pour La Forme (corrections & développements)

**Application :** Pour La Forme — gestion de club sportif
**Type réel :** application **Java Swing desktop** (pas une app web/Spring), persistance par sérialisation fichier (`data/*.dat`), architecture en couches `model / dao / service / ui / util`.
**Portée :** points §1 à §7 du cahier des charges.

---

## Note méthodologique importante (à lire avant le tableau)

Deux écarts entre le cahier des charges et la réalité du projet ont conditionné l'implémentation :

1. **Pas de Bean Validation / pas de front HTML.** Le cahier des charges parle de `@Pattern`, `@Min/@Max`, `@DecimalMin`, `@Positive` et de « validation côté client ET serveur ». Ces annotations n'existent pas dans une app Swing sans Jakarta Validation. L'équivalent fonctionnel a été implémenté :
   - **« Côté client »** → filtres de saisie (`DocumentFilter`) + contrôles pré-soumission dans les dialogues Swing, avec messages d'erreur explicites.
   - **« Côté serveur »** → couche `service` (`MembreService`, `ActiviteService`) + utilitaire partagé `Validator`, qui lèvent `IllegalArgumentException` (équivalent d'une contrainte rejetée).

2. **Environnement sans compilateur.** Le poste de travail ne disposait que d'un JRE (pas de `javac`) et sans accès réseau pour en installer un. **Les tests n'ont donc pas pu être exécutés à l'exécution.** La validation ci-dessous est une **revue de code statique rigoureuse** (résolution des symboles, signatures, exhaustivité des `switch`, équilibrage syntaxique, compatibilité de sérialisation). Les cas marqués *« à confirmer au runtime »* nécessitent un clic-test de votre côté après compilation dans Eclipse.

---

## §1 — Contrôle de saisie : ajout d'un membre

| Élément | Description | Comportement attendu | Correction appliquée | Statut |
|---|---|---|---|---|
| Nom/Prénom — caractères | Le formulaire acceptait chiffres et caractères spéciaux | Lettres (accents), espaces, tirets, apostrophes uniquement | `Validator.isValidNom` (regex `^[\p{L} '\-]+$`) ; filtre de frappe `InputFilters.applyNameFilter` (client) ; contrôle dans `MembreService.validerMembre` (serveur) | **Résolu** |
| Téléphone — format | `Validator` autorisait 8–15 chiffres | **Exactement 8 chiffres**, chiffres uniquement (choix retenu) | Regex `^[0-9]{8}$` ; filtre `applyDigitFilter(field, 8)` (client) ; message serveur « exactement 8 chiffres » | **Résolu** |

**Cas testés (revue) :**
- Nominal : `Ben Ali` / `Ahmed` / `12345678` → acceptés.
- Limite : nom avec apostrophe `O'Brien`, tiret `Jean-Luc` → acceptés.
- Erreur : `Ahmed2`, `Jean@` → bloqués à la frappe **et** rejetés au save ; téléphone `1234567` (7) ou `123456789` (9) → rejeté avec message explicite.
- Données seed (`00000000`, `98765432`) → conformes, **aucune régression de login** *(à confirmer au runtime)*.

---

## §2 — Fiche sanitaire : valeurs extrêmes

| Champ | Bornes | Correction appliquée | Statut |
|---|---|---|---|
| Taille | 50–250 cm | `Validator.TAILLE_MIN/MAX` + `isValidTaille` ; contrôle dans `FicheSanitaireDialog.sauvegarder` et `MemberFormDialog` (client) + `MembreService.validerMembre` (serveur) | **Résolu** |
| Poids | 20–300 kg | `Validator.POIDS_MIN/MAX` + `isValidPoids` ; mêmes points d'application | **Résolu** |

**Cas testés (revue) :** 175 cm / 75 kg → OK ; 49 cm, 251 cm, 19 kg, 301 kg → rejetés avec message borné explicite ; champ vide → toléré (non obligatoire), mais si renseigné hors borne → rejeté côté serveur aussi.

---

## §3 — Affichage des activités (membre & admin)

| Élément | Constat sur la v7 fournie | Action |
|---|---|---|
| Boutons *Actualiser/S'inscrire* visibles seulement au hover | **Non reproduit** : dans `MembreActivitesPanel`, le footer est en `BorderLayout.SOUTH`, boutons toujours ajoutés | Renforcement défensif : `setVisible(true)` + `setOpaque(true)` explicites sur les deux boutons |
| Cartes « sticky » en bas de page | **Non reproduit** : `ScrollableWrapPanel` défile normalement, pas de comportement sticky dans le code | Aucun changement (rien à corriger sans introduire un bug) |
| Places affichées seulement au hover (admin) | **Non reproduit** : le badge « N places / Complet » est peint **inconditionnellement** dans `ActivityCard.paintComponent` ; le hover ne modifie que l'ombre et l'alpha de la bordure | Aucun changement |

**Statut : Renforcé (boutons) / Non reproduit (sticky & places).**
> Si vous observez réellement ces bugs, c'est probablement sur un build différent de `pour-la-forme-v7.zip` — me renvoyer le bon build pour cibler précisément.

---

## §4 — Contrôle de saisie : prix mensuel

| Élément | Comportement attendu | Correction appliquée | Statut |
|---|---|---|---|
| Prix négatif/zéro accepté | Valeur strictement > 0 | `Validator.isValidPrix(prix) = prix > 0` ; `ActiviteFormDialog` rejette `≤ 0` (client) ; `ActiviteService.validerActivite` ajoute le contrôle (serveur, équivalent `@Positive`) ; hint champ « > 0 » | **Résolu** |

**Cas testés (revue) :** `80` → OK ; `-2`, `0` → rejetés des deux côtés avec message « strictement supérieur à 0 ».

---

## §5 — Module de paiement : workflow complet

Workflow implémenté de bout en bout :

1. **Déclenchement** — à l'acceptation par l'admin (`InscriptionService.validerInscription`), une ligne `Paiement` `NON_PAYE` est générée (comportement existant conservé).
2. **Action membre** — dans *Mes paiements* (`PaiementMembrePanel`), chaque paiement non réglé affiche un bouton **« Procéder au paiement »**.
3. **Choix de la méthode** — `PaiementDialog`, étape 1 : **Visa, Mastercard, e-Dinar** avec **logos dessinés** (aucun asset externe, conforme à la consigne « simuler »).
4. **Formulaire fictif** — étape 2 : numéro de carte, expiration MM/AA, CVV, titulaire. Contrôle strict par champ :

| Champ | Règle | Validateur |
|---|---|---|
| Numéro de carte | 16 chiffres | `Validator.isValidCardNumber` + filtre 16 chiffres |
| Expiration | format MM/AA, mois 01–12, **non expirée** | `Validator.isValidExpiry` |
| CVV | 3 chiffres | `Validator.isValidCVV` + filtre 3 chiffres |
| Titulaire | lettres uniquement | `Validator.isValidCardHolder` |

5. **Après validation** — `PaiementService.payer(id, methode)` : statut → **PAYE**, méthode enregistrée (`Paiement.Methode`), **notification ADMIN** (`Notification.Type.PAIEMENT_EFFECTUE`).
   - **Côté admin :** statut « Payé » dans `PaiementAdminPanel` (équivalent du « Oui »), tooltip « Réglé via … », notification visible dans l'espace Notifications.
   - **Côté membre :** le bouton disparaît, remplacé par l'indicateur **« Paiement effectué (méthode) »**.
6. **Correction d'affichage** — le bloc statut débordait : hauteur de carte portée à 84 px et `preferredSize` fixé pour éviter tout débordement du flux.

**Cas testés (revue) :**
- Numéro `4242 4242 4242 4242` (16) → OK ; `123` → rejeté.
- Expiration `12/30` → OK ; `13/25` (mois invalide) ou `01/20` (expirée) → rejetés.
- CVV `123` → OK ; `12`/`1234` → rejetés.
- Titulaire `Ahmed Ben Ali` → OK ; `Ahmed123` → rejeté.
- Double paiement bloqué (`payer` lève si déjà `PAYE`).

**Statut : Résolu** *(parcours UI à confirmer au runtime).*

> **Compatibilité données :** ajout du champ `methode` à `Paiement` sans changer `serialVersionUID` → désérialisation des anciens `.dat` rétrocompatible (`methode` = `null`). Le dossier `data/` était vide, donc aucun risque de migration.

---

## §6 — Modification de la fiche membre : résolution logique

**Solution retenue (recommandée par le cahier des charges) :** modèle hybride avec contrôle d'accès par rôle.

| Champ | Modifiable par le membre | Réservé à l'admin | Justification |
|---|---|---|---|
| Email | ✅ | | Coordonnée personnelle non sensible |
| Téléphone | ✅ | | Idem |
| Adresse | ✅ | | Idem |
| Photo de profil | ✅ | | Donnée personnelle non sensible |
| Login | | ✅ | Identifiant de connexion (intégrité du compte) |
| Mot de passe | (via flux dédié) | | Changé via `ChangePasswordDialog`, pas dans l'édition de profil |
| Rôle | | ✅ | Donnée administrative / sécurité |
| Statut d'inscription | | ✅ | Décision administrative (validation activité) |
| Fiche sanitaire (taille/poids/conditions, note médecin) | | ✅ | Donnée validée par l'admin/médecin |
| Abonnement / paiements | | ✅ | Géré par le workflow admin |

**Implémentation :**
- Nouvelle méthode `MembreService.modifierProfilParMembre(demandeurId, cibleId, email, tel, adresse, photo)` :
  - **Contrôle d'accès backend** : refuse si `demandeurId != cibleId` (« vous ne pouvez modifier que votre propre profil »).
  - **N'écrit que les champs non sensibles** ; les champs sensibles sont volontairement ignorés (impossible de les altérer par cette voie).
  - Re-valide email/téléphone et l'unicité de l'email.
- Nouveau dialogue `EditProfileDialog` (bouton **« Modifier mes infos »** dans `ProfilePanel`) n'exposant que email/téléphone/adresse.
- Le changement de photo membre passe désormais aussi par ce contrôle d'accès.

**Cas testés (revue) :** membre modifie son email/tel → OK ; tentative de passer un `cibleId` ≠ soi → `IllegalArgumentException` ; email déjà pris → rejeté.

**Statut : Résolu.**

---

## §7 — Mission QA : synthèse

| Point | Statut |
|---|---|
| §1 Nom + Téléphone | ✅ Résolu |
| §2 Bornes taille/poids | ✅ Résolu |
| §3 Affichage activités | ⚠️ Renforcé / Non reproduit (voir détail) |
| §4 Prix mensuel | ✅ Résolu |
| §5 Workflow paiement | ✅ Résolu (UI à confirmer au runtime) |
| §6 Édition fiche membre | ✅ Résolu |

**Vérifications transverses (revue statique) :**
- Équilibrage `{}` / `()` OK sur les 18 fichiers touchés.
- Tous les symboles appelés (`Validator.*`, `PaiementService.*`, constantes, enums) résolus.
- **Risque de compilation détecté et corrigé** : le `switch` exhaustif sur `Notification.Type` dans `NotificationsPanel` aurait cassé la compilation avec le nouveau `PAIEMENT_EFFECTUE` → case ajoutée.
- Aucun autre `switch` exhaustif impacté (`DashboardPanel` et `IconFactory` switchent sur `String` avec `default`).
- Ordre d'injection des dépendances dans `App` valide.

**À faire de votre côté (non exécutable ici) :**
1. Compiler dans Eclipse (le projet a déjà `.project`/`.classpath`).
2. Tests d'exécution : navigation, responsivité, états vides, rechargements, parcours de paiement complet, et confirmation visuelle du §3 sur votre build réel.
3. Si un `data/*.dat` ancien existe sur votre poste, vérifier la désérialisation (rétrocompatible par construction).

---

## Fichiers modifiés / créés

**Créés :** `ui/PaiementDialog.java`, `ui/EditProfileDialog.java`, `ui/components/InputFilters.java`
**Modifiés :** `util/Validator.java`, `service/MembreService.java`, `service/ActiviteService.java`, `service/PaiementService.java`, `service/NotificationService.java`, `model/Paiement.java`, `model/Notification.java`, `ui/MemberFormDialog.java`, `ui/FicheSanitaireDialog.java`, `ui/ActiviteFormDialog.java`, `ui/MembreActivitesPanel.java`, `ui/PaiementMembrePanel.java`, `ui/PaiementAdminPanel.java`, `ui/ProfilePanel.java`, `ui/NotificationsPanel.java`, `app/App.java`
