# Observabilité PFE — Prometheus, Grafana et preuve de performance

Guide complet pour démontrer **gros volume**, **temps réel**, **asynchronisme** et **tolérance aux pannes** du backend notifications (Kafka + Debezium + Firebase).

---

## Table des matières

1. [Architecture et flux mesurés](#1-architecture-et-flux-mesurés)
2. [Catalogue des métriques](#2-catalogue-des-métriques)
3. [Prérequis](#3-prérequis)
4. [Installation Prometheus et Grafana (sans Docker)](#4-installation-prometheus-et-grafana-sans-docker)
   - [4.0 Ordre de démarrage](#40-ordre-de-démarrage-des-services)
   - [4.1 Prometheus — installation](#41-prometheus--installation-détaillée-windows)
   - [4.2 Prometheus — configuration et vérification](#42-prometheus--configuration-et-vérification)
   - [4.3 Grafana — installation](#43-grafana--installation-détaillée-windows)
   - [4.4 Grafana — source de données Prometheus](#44-grafana--connecter-prometheus-comme-source)
   - [4.5 Grafana — créer le dashboard](#45-grafana--créer-le-dashboard-pas-à-pas)
5. [Procédure de preuve — étape par étape](#5-procédure-de-preuve--étape-par-étape)
6. [Récapitulatif des requêtes PromQL](#6-récapitulatif-des-requêtes-promql)
7. [Comparaison API synchrone vs pipeline Kafka](#7-comparaison-api-synchrone-vs-pipeline-kafka)
8. [Endpoint de charge `/test/{count}`](#8-endpoint-de-charge-testcount)
9. [Argumentation pour le mémoire / soutenance](#9-argumentation-pour-le-mémoire--soutenance)
10. [Dépannage](#10-dépannage)
11. [Checklist avant soutenance](#11-checklist-avant-soutenance)
12. [Fichiers du dossier `monitoring/`](#12-fichiers-du-dossier-monitoring)

---

## 1. Architecture et flux mesurés

### Pipelines métier (tag `pipeline`)

| Pipeline | Topic ingress (CDC / public) | Topic intermédiaire | Topic enrichissement | Consumer(s) |
|----------|------------------------------|---------------------|----------------------|-------------|
| **transaction** | `notifications.public.transactions` | `transaction-intermediare` | `transaction-enrechissement` | `ConsumerListener` → `ConsumerListenerInter` → `ConsumerListenerEnriche` |
| **fraude** | `notification.public.fraude` | `fraude-intermediare` | `fraude-enrechissement` | `ConsumerListenerHigh` → `ConsumerListenerInterHigh` → `ConsumerListenerEnrichCritique` |
| **carte** | `notifications.public.carte` | `card-intermediare` | `card-enrichissement` | `ConsumerListenerCardEvent` → `ConsumerListenerCardInter` → `ConsumerListenerCardEnriche` |

### Étapes (tag `stage` dans les métriques)

- **ingress** : réception événement public + filtre référentiel (`ReferentielAlerte`)
- **intermediate** : construction `NotifEnrechi` + republication
- **enrichment** : envoi Firebase + archivage (`NotificationService`)

### Parallélisme Kafka (`ConsumerConfiguration`)

- Factory standard : **3** consumers concurrents (`transaction`, `carte`)
- Factory fraude : **2** consumers concurrents
- Gauge exposée : `pfe_kafka_listener_concurrency` = **5**
- `MAX_POLL_RECORDS` = **40** (traitement par lots côté consumer)

### Chaîne technique complète (transaction)

```
INSERT transactions (API /test ou app métier)
    → Debezium CDC
    → Kafka notifications.public.transactions
    → ConsumerListener (ingress)
    → Kafka transaction-intermediare
    → ConsumerListenerInter (intermediate)
    → Kafka transaction-enrechissement
    → ConsumerListenerEnriche (enrichment)
    → Firebase FCM + table ArchivageNotification
```

---

## 2. Catalogue des métriques

> Dans Prometheus, les noms Micrometer utilisent des **underscores** : `pfe.kafka.messages` → `pfe_kafka_messages_total`.

### Métriques métier (custom)

| Nom Prometheus (typique) | Type | Tags principaux | Preuve pour le PFE |
|--------------------------|------|-----------------|-------------------|
| `pfe_kafka_messages_total` | Counter | `pipeline`, `topic`, `outcome` | Volume traité (`success` / `error` / `skipped`) |
| `pfe_kafka_processing_seconds_*` | Histogram | `pipeline`, `stage`, `topic`, `outcome` | Latence par étape (temps réel traitement) |
| `pfe_kafka_event_lag_*` | DistributionSummary (histogram) | `pipeline`, `stage`, `topic` | Délai `created_at` (CDC) → consumer en **ms** — buckets : `pfe_kafka_event_lag_bucket` (pas `*_seconds_*`) |
| `pfe_kafka_forwarded_total` | Counter | `pipeline`, `source`, `target` | Asynchronisme multi-topics |
| `pfe_kafka_skipped_total` | Counter | `pipeline`, `topic`, `reason` | Filtrage métier (alertes off, user invalide) |
| `pfe_kafka_dlt_total` | Counter | `pipeline`, `topic` | Tolérance panne (Dead Letter après retries) |
| `pfe_kafka_listener_concurrency` | Gauge | — | Capacité parallèle configurée |
| `pfe_notification_sent_total` | Counter | `outcome`, `critical` | Livraison Firebase (`success`, `error`, `no_token`, …) |
| `pfe_notification_duration_seconds_*` | Histogram | `outcome`, `critical` | Latence envoi FCM |

### Valeurs `outcome` utiles

- Messages Kafka : `success`, `error`, `skipped`
- Notifications : `success`, `error`, `no_token`, `user_not_found`

### Métriques Spring Boot (automatiques, même URL)

- `http_server_requests_seconds_*` — latence HTTP (dont `/test/{count}`)
- `jvm_memory_used_bytes`, `jvm_threads_live`, `process_cpu_usage`
- `kafka_consumer_*` (si exposées par la version Spring Kafka / Actuator)

**URL d’export** : http://localhost:8080/actuator/prometheus  
(Sécurité : endpoints `health`, `prometheus`, `info` en accès public — voir `SecurityConfig`.)

---

## 3. Prérequis

### Services

- [ ] PostgreSQL avec tables `transactions`, `users`, `ReferentielAlerte`, etc.
- [ ] Kafka (ex. localhost:9094) + topics créés (`TopicsConfig` ou auto)
- [ ] Connecteur **Debezium** actif sur `transactions` (sinon pas de flux `transaction` après `/test`)
- [ ] Backend Spring : `mvn spring-boot:run` (port **8080**)
- [ ] Firebase configuré (`InitializeFirebase`) pour `outcome="success"` sur les notifs

### Données métier minimales

```sql
-- Utilisateur existant avec token FCM
SELECT id, email, fcm_token FROM users WHERE id = 27;

-- Référentiel alertes (ex. client_id = 26 ou 27)
SELECT client_id, alerte_transaction, alerte_fraude, alerte_carte, is_active
FROM referentiel_alerte WHERE client_id = 27;

-- Si absent :
INSERT INTO referentiel_alerte (client_id, alerte_fraude, alerte_transaction, alerte_carte, is_active, is_black_list, telephone)
VALUES (27, true, true, true, true, false, '0600000000');
```

### Fichiers backend

- `src/main/java/com/pca/Backend/metrics/PipelineMetrics.java` — instrumentation
- `src/main/resources/application.properties` — export Prometheus activé
- `monitoring/prometheus-local.yml` — scrape sans Docker

---

## 4. Installation Prometheus et Grafana (sans Docker)

> **Important** : trois ports différents  
> | Port | Service | URL |
> |------|---------|-----|
> | **8080** | Backend Spring (Actuator) | `/actuator/prometheus` — export brut des métriques |
> | **9090** | Prometheus | interface de collecte et requêtes PromQL |
> | **3000** | Grafana | tableaux de bord |

---

### 4.0 Ordre de démarrage des services

À chaque session de travail ou démo, respecter cet ordre :

| Étape | Commande / action | Vérification rapide |
|-------|-------------------|---------------------|
| 1 | PostgreSQL + Kafka + Debezium (si test transaction) | Topics Kafka actifs |
| 2 | `cd Backend` puis `mvn spring-boot:run` | http://localhost:8080/actuator/health → `UP` |
| 3 | Lancer **Prometheus** (`prometheus.exe`) | http://localhost:9090 |
| 4 | Lancer **Grafana** (service ou exe) | http://localhost:3000 |
| 5 | (Optionnel) charge : `Invoke-RestMethod http://localhost:8080/test/500` | JSON + métriques qui montent |

---

### 4.1 Prometheus — installation détaillée (Windows)

#### Étape 1 — Télécharger

1. Aller sur https://prometheus.io/download/
2. Choisir **Windows** (archive `.zip`, ex. `prometheus-2.x.x.windows-amd64.zip`)
3. Télécharger et extraire dans un dossier fixe, par exemple :

   ```
   C:\prometheus\
   ```

   Contenu attendu : `prometheus.exe`, `promtool.exe`, fichier `prometheus.yml` par défaut.

#### Étape 2 — Copier la configuration du projet PFE

1. Ouvrir le fichier du backend :

   ```
   C:\Users\Rog\Desktop\PFE\Backend\monitoring\prometheus-local.yml
   ```

2. **Remplacer** (ou fusionner) le contenu de :

   ```
   C:\prometheus\prometheus.yml
   ```

3. Adapter le nom de machine si besoin. Pour trouver le vôtre :

   ```powershell
   hostname
   ```

   Si Actuator répond sur `http://desktop-cuman6h:8080` mais pas sur `127.0.0.1:8080`, éditer `prometheus.yml` et ne garder qu’une cible :

   ```yaml
   scrape_configs:
     - job_name: pfe-backend
       metrics_path: /actuator/prometheus
       static_configs:
         - targets: ["desktop-cuman6h:8080"]
   ```

   (Remplacez `desktop-cuman6h` par le résultat de `hostname`.)

#### Étape 3 — Lancer Prometheus

Ouvrir **PowerShell** :

```powershell
cd C:\prometheus
.\prometheus.exe --config.file=prometheus.yml
```

- Laisser cette fenêtre **ouverte** (Prometheus tourne au premier plan).
- Messages attendus : `Server is ready to receive web requests` et écoute sur `:9090`.

> Pour l’arrêter : `Ctrl+C` dans la fenêtre PowerShell.

#### Étape 4 — Premier accès à l’interface

1. Navigateur : **http://localhost:9090**
2. Vous arrivez sur l’UI Prometheus (barre de menu : **Alerts**, **Graph**, **Status**).

---

### 4.2 Prometheus — configuration et vérification

#### A. Vérifier que le backend exporte les métriques (port 8080)

1. Backend démarré (`mvn spring-boot:run`).
2. Ouvrir dans le navigateur (adapter le host) :

   - http://127.0.0.1:8080/actuator/prometheus  
   - ou http://**VOTRE-PC**:8080/actuator/prometheus  

3. Vous devez voir du texte commençant par :

   ```
   # HELP application_ready_time_seconds ...
   # TYPE application_ready_time_seconds gauge
   ```

4. **Ctrl+F** → rechercher `pfe_` :

   | Métrique | Présente quand |
   |----------|----------------|
   | `pfe_kafka_listener_concurrency` | Dès le démarrage du backend |
   | `pfe_kafka_messages_total` | Dès le démarrage (0), augmente après Kafka / `/test` |

Si `pfe_` est absent → redémarrer le backend après les dernières modifications du code.

#### B. Vérifier que Prometheus scrape le backend (port 9090)

1. Ouvrir **http://localhost:9090**
2. Menu **Status** (ou **Status → Status**) → onglet **Targets**
3. Ligne **pfe-backend** :

   | État | Signification |
   |------|----------------|
   | **UP** (vert) | OK — Prometheus récupère les métriques toutes les 15 s |
   | **DOWN** (rouge) | Prometheus ne peut pas joindre `host:8080` → corriger `prometheus.yml` |

4. Cliquer sur l’endpoint pour voir l’erreur si DOWN (ex. `connection refused`, `no such host`).

#### C. Première requête PromQL dans Prometheus

1. Menu **Graph** (ou **Query** selon version)
2. Dans la zone de requête, coller :

   ```promql
   up{job="pfe-backend"}
   ```

3. Cliquer **Execute** (ou **Run query**)
4. Onglet **Table** : valeur **1**
5. Onglet **Graph** : courbe à 1

Autres requêtes de test :

```promql
pfe_kafka_listener_concurrency
```

```promql
sum(rate(pfe_kafka_messages_total[1m]))
```

#### D. Déclencher des données (si tout est à 0)

```powershell
Invoke-RestMethod "http://localhost:8080/test/100"
```

Attendre 30–60 s, puis réexécuter dans Prometheus :

```promql
sum(rate(pfe_kafka_messages_total{outcome="success"}[1m]))
```

La courbe doit devenir **> 0** si Kafka + Debezium + consumers tournent.

#### E. Dépannage Prometheus rapide

| Problème | Solution |
|----------|----------|
| Target DOWN | Vérifier backend sur 8080 ; corriger `targets` dans `prometheus.yml` ; redémarrer `prometheus.exe` |
| `up` = vide | Job name différent → **Status → Targets** pour voir le vrai nom |
| Métriques `pfe_*` vides dans Prometheus mais visibles sur Actuator | Target pas UP ou mauvais scrape |
| Port 9090 déjà utilisé | Fermer l’autre instance Prometheus |

---

### 4.3 Grafana — installation détaillée (Windows)

#### Étape 1 — Télécharger

1. https://grafana.com/grafana/download?platform=windows
2. Choisir **Windows** (installer `.msi` ou archive `.zip` standalone)

#### Étape 2 — Installer (MSI recommandé)

1. Lancer l’installateur Grafana
2. Laisser les options par défaut (service Windows installé)
3. À la fin : cocher **Run Grafana** ou démarrer le service :

   ```powershell
   # Vérifier le service (PowerShell admin)
   Get-Service grafana
   Start-Service grafana
   ```

#### Étape 3 — Premier login

1. Navigateur : **http://localhost:3000**
2. Identifiants par défaut (première connexion) :
   - Login : `admin`
   - Password : `admin`
3. Grafana demande un **nouveau mot de passe** → choisir un mot de passe et le noter.

#### Étape 4 — Vérifier que Grafana répond

- Page d’accueil Grafana visible
- Menu latéral : **Home**, **Dashboards**, **Connections** (ou **Configuration** selon version)

> Si le port 3000 est occupé, Grafana peut utiliser un autre port (voir logs dans `C:\Program Files\GrafanaLabs\grafana\data\log`).

---

### 4.4 Grafana — connecter Prometheus comme source

> Prometheus doit déjà tourner sur **http://localhost:9090** avec target **UP**.

#### Grafana 10+ (menu **Connections**)

1. Menu gauche → **Connections** (icône prise)
2. **Data sources** → **Add new data source**
3. Choisir **Prometheus**
4. Remplir :
   - **Name** : `Prometheus` (ou `Prometheus PFE`)
   - **URL** : `http://localhost:9090`
   - Laisser **Access** = **Server (default)** (Grafana appelle Prometheus depuis la machine locale)
5. En bas : **Save & test**
6. Message attendu : **Successfully queried the Prometheus API** (vert)

#### Grafana 9.x (ancien menu)

1. **Configuration** (engrenage) → **Data sources**
2. **Add data source** → **Prometheus**
3. URL : `http://localhost:9090` → **Save & test**

#### Vérification dans Explore

1. Menu **Explore** (boussole)
2. Source : **Prometheus**
3. Requête :

   ```promql
   up{job="pfe-backend"}
   ```

4. **Run query** → doit afficher **1**

---

### 4.5 Grafana — créer le dashboard pas à pas

#### Créer le dashboard

1. Menu **Dashboards** → **New** → **New dashboard**
2. Titre en haut : **PFE — Pipeline notifications**
3. En haut à droite : **Save dashboard** (dossier *General*)

#### Réglage temps (important pour la démo)

- En haut à droite : intervalle **Last 15 minutes** (ou **Last 1 hour**)
- **Refresh** : `5s` ou `10s` pendant la démo live

#### Ajouter un panneau (modèle répété pour chaque graphique)

1. **Add** → **Visualization** (ou **Add visualization**)
2. Source de données : **Prometheus**
3. Mode **Code** (éditeur PromQL) et coller la requête du tableau ci-dessous
4. À droite :
   - **Panel title** : le titre indiqué
   - **Visualization** : **Time series** (sauf panneau Stat / Text)
5. **Apply** ou **Save**

---

#### Panneau 1 — Débit messages par pipeline

- **Titre** : `Débit messages / s (par pipeline)`
- **Type** : Time series
- **Requête** :

```promql
sum by (pipeline) (rate(pfe_kafka_messages_total{outcome="success"}[1m]))
```

- **Légende** : `{{pipeline}}`

---

#### Panneau 2 — Temps réel (délai CDC → consumer, p95)

- **Titre** : `Temps réel — event lag p95 (ms)`
- **Type** : Time series
- **Requête** :

```promql
histogram_quantile(0.95,
  sum by (pipeline, le) (rate(pfe_kafka_event_lag_bucket[5m]))
)
```

---

#### Panneau 3 — Latence de traitement p95

- **Titre** : `Latence traitement consumer p95`
- **Type** : Time series
- **Requête** :

```promql
histogram_quantile(0.95,
  sum by (pipeline, le) (rate(pfe_kafka_processing_seconds_bucket[5m]))
)
```

---

#### Panneau 4 — Asynchronisme (forwards)

- **Titre** : `Routage async (messages forwardés / s)`
- **Type** : Time series
- **Requête** :

```promql
sum by (pipeline) (rate(pfe_kafka_forwarded_total[1m]))
```

---

#### Panneau 5 — Notifications Firebase

- **Titre** : `Notifications Firebase / s`
- **Type** : Time series
- **Requête** :

```promql
sum(rate(pfe_notification_sent_total{outcome="success"}[1m]))
```

---

#### Panneau 6 — Erreurs

- **Titre** : `Erreurs Kafka / s`
- **Type** : Time series
- **Requête** :

```promql
sum(rate(pfe_kafka_messages_total{outcome="error"}[1m]))
```

---

#### Panneau 7 — DLT (tolérance panne)

- **Titre** : `Dead Letter Topic / s`
- **Type** : Time series
- **Requête** :

```promql
sum(rate(pfe_kafka_dlt_total[1m]))
```

---

#### Panneau 8 — Concurrence Kafka

- **Titre** : `Threads consumers Kafka`
- **Type** : **Stat**
- **Requête** :

```promql
pfe_kafka_listener_concurrency
```

---

#### Panneau 9 — État du scrape (optionnel)

- **Titre** : `Backend scrape UP`
- **Type** : Stat
- **Requête** :

```promql
up{job="pfe-backend"}
```

- Seuil visuel : 1 = vert

---

#### Organiser la mise en page

1. **Save dashboard**
2. Icône **Configure** (engrenage du dashboard) → glisser-déposer les panneaux
3. Disposition suggérée : 2 panneaux par ligne, panneaux 1–4 en haut (volume + temps réel)

#### Démo live avec le dashboard

1. Ouvrir le dashboard, refresh **10s**
2. Lancer :

   ```powershell
   Invoke-RestMethod "http://localhost:8080/test/1000"
   ```

3. Observer en direct :
   - panneau 1 (débit) qui monte
   - panneaux 2–3 (latences)
   - panneau 5 (notifications) après 1–2 min

4. **Capture d’écran** pour le mémoire (avant/après charge si possible)

---

### 4.6 Configuration backend (déjà dans le projet)

Fichier `src/main/resources/application.properties` :

```properties
management.endpoints.web.exposure.include=health,info,prometheus,metrics
management.prometheus.metrics.export.enabled=true
management.metrics.tags.application=pfe-backend
```

`SecurityConfig` autorise sans authentification :

- `/actuator/health`
- `/actuator/prometheus`
- `/actuator/info`

---

## 5. Procédure de preuve — étape par étape

Durée totale estimée : **45–60 minutes** (première fois). À refaire 1 fois avant la soutenance pour captures d’écran fraîches.

### Phase A — Sanity check (5 min)

| # | Action | Résultat attendu |
|---|--------|------------------|
| A1 | `mvn spring-boot:run` | App démarre sans erreur |
| A2 | Ouvrir http://localhost:8080/actuator/prometheus | Texte avec `pfe_kafka_messages_total` |
| A3 | Prometheus Targets UP | Scrape OK |
| A4 | PromQL : `sum(rate(pfe_kafka_messages_total[1m]))` | `0` ou petite valeur (OK avant charge) |

---

### Phase B — Preuve « gros volume » (15 min)

**Objectif** : montrer que le débit de messages traités **augmente** sous charge.

| # | Action | Résultat attendu |
|---|--------|------------------|
| B1 | Noter l’heure de début | — |
| B2 | Charge synchrone (déclenche CDC) : | JSON avec débit sync |
| | `Invoke-RestMethod "http://localhost:8080/test/1000"` | |
| B3 | Attendre **1–2 minutes** | Consumers + CDC rattrapent |
| B4 | Répéter optionnel : `/test/2000` | Courbes plus hautes |
| B5 | Grafana — panneau **Débit global** | Courbe qui monte |

**PromQL — débit global (msg/s)** :

```promql
sum(rate(pfe_kafka_messages_total{outcome="success"}[1m]))
```

**PromQL — débit par pipeline** :

```promql
sum by (pipeline) (rate(pfe_kafka_messages_total{outcome="success"}[1m]))
```

**Capture** : screenshot Grafana avec pic de débit après `/test/1000`.

**Phrase soutenance** : *« Sous injection de N transactions, le taux `rate(pfe_kafka_messages_total)` augmente : le pipeline absorbe un flux massif sans arrêt du service. »*

---

### Phase C — Preuve « temps réel » (10 min)

**Objectif** : latence de traitement **faible et stable** (secondes, pas minutes).

| # | Action | Résultat attendu |
|---|--------|------------------|
| C1 | Pendant ou après charge, ouvrir panneaux latence | Données non vides |
| C2 | Vérifier p95 event lag « raisonnable » | Ordre de grandeur : secondes |

**PromQL — latence traitement p95 (par pipeline)** :

```promql
histogram_quantile(0.95,
  sum by (pipeline, le) (rate(pfe_kafka_processing_seconds_bucket[5m]))
)
```

**PromQL — délai CDC → consumer p95 (temps réel bout-en-bout)** :

```promql
histogram_quantile(0.95,
  sum by (pipeline, le) (rate(pfe_kafka_event_lag_bucket[5m]))
)
```

> `event_lag` nécessite le champ `created_at` dans le message JSON (Debezium / transactions).

**Capture** : p95 processing + p95 event_lag sur pipeline `transaction`.

**Phrase soutenance** : *« Le p95 de `pfe_kafka_event_lag` borne le délai entre création de l’événement et son traitement — caractéristique d’un système quasi temps réel. »*

---

### Phase D — Preuve « asynchronisme » (10 min)

**Objectif** : le traitement est **découplé** en plusieurs topics (pas un seul bloc synchrone).

| # | Action | Résultat attendu |
|---|--------|------------------|
| D1 | Panneau forwards | `rate(pfe_kafka_forwarded_total)` > 0 |
| D2 | Panneau concurrency | Gauge = 5 |

**PromQL — routage multi-topics** :

```promql
sum by (pipeline) (rate(pfe_kafka_forwarded_total[1m]))
```

**PromQL — concurrence** :

```promql
pfe_kafka_listener_concurrency
```

**Schéma à dessiner au tableau** (ou slide) :

```
ingress topic → inter topic → enrichissement topic → Firebase
     ↑              ↑              ↑
  métrique       forwarded      notification_sent
  processing     forwarded
```

**Phrase soutenance** : *« Chaque étape publie vers un topic suivant (`pfe_kafka_forwarded_total`) : le producteur n’attend pas la notification mobile. »*

---

### Phase E — Comparaison API synchrone vs Kafka (15 min) — **essentiel pour le jury**

**Objectif** : prouver pourquoi le modèle événementiel est adapté au **gros volume**.

| # | Action | Mesure « API classique » | Mesure « Pipeline async » |
|---|--------|--------------------------|---------------------------|
| E1 | `Invoke-RestMethod "http://localhost:8080/test/1000"` | `durationMs`, `throughputPerSec` dans JSON | — |
| E2 | Observer Postman/navigateur | Requête HTTP **longue** (bloquante) | — |
| E3 | 1–2 min après, Grafana | — | `rate(pfe_kafka_messages_total)` > 0 |
| E4 | Grafana notifications | — | `rate(pfe_notification_sent_total{outcome="success"}[1m])` > 0 |

**Tableau à inclure dans le mémoire** (remplir avec vos chiffres réels) :

| Critère | API synchrone (`GET /test/N`) | Pipeline Kafka + métriques |
|---------|------------------------------|----------------------------|
| Modèle | Boucle JPA dans la requête HTTP | CDC → consumers → FCM |
| Blocage client | Oui, jusqu’à N inserts | Non après publication / insert |
| Débit mesuré | `throughputPerSec` (JSON) | `rate(pfe_kafka_messages_total)` |
| Scalabilité | Vertical (1 instance API) | Consumers + partitions Kafka |
| Résilience | Erreur = échec HTTP immédiat | Retries + DLT (`pfe_kafka_dlt_total`) |
| Temps réel perçu | Tout dans la latence HTTP | `event_lag` + traitement parallèle |

**Phrase soutenance** : *« `/test/N` mesure le plafond du traitement synchrone ; les métriques Kafka montrent que le traitement notification continue en arrière-plan à haut débit. »*

---

### Phase F — Tolérance aux pannes (10 min, optionnel mais valorisant)

| # | Action | Résultat attendu |
|---|--------|------------------|
| F1 | Panneau erreurs Kafka | `outcome="error"` visible si panne simulée |
| F2 | Panneau DLT | `pfe_kafka_dlt_total` après échecs répétés |
| F3 | Cas test : user sans `fcm_token` | `pfe_notification_sent_total{outcome="no_token"}` |

**PromQL — taux d’erreur** :

```promql
sum(rate(pfe_kafka_messages_total{outcome="error"}[5m]))
/
sum(rate(pfe_kafka_messages_total[5m]))
```

**PromQL — DLT** :

```promql
sum(rate(pfe_kafka_dlt_total[5m]))
```

**Phrase soutenance** : *« Les échecs sont comptabilisés ; `@RetryableTopic` et DLT évitent la perte silencieuse de messages. »*

---

## 6. Récapitulatif des requêtes PromQL

> Procédure complète d’installation et création des panneaux : **[section 4.5](#45-grafana--créer-le-dashboard-pas-à-pas)**.

| # | Usage PFE | Requête |
|---|-----------|---------|
| 1 | Target vivant | `up{job="pfe-backend"}` |
| 2 | Débit global | `sum(rate(pfe_kafka_messages_total{outcome="success"}[1m]))` |
| 3 | Débit par pipeline | `sum by (pipeline) (rate(pfe_kafka_messages_total{outcome="success"}[1m]))` |
| 4 | Temps réel (lag p95) | `histogram_quantile(0.95, sum by (pipeline, le) (rate(pfe_kafka_event_lag_bucket[5m])))` |
| 5 | Latence traitement p95 | `histogram_quantile(0.95, sum by (pipeline, le) (rate(pfe_kafka_processing_seconds_bucket[5m])))` |
| 6 | Async (forwards) | `sum by (pipeline) (rate(pfe_kafka_forwarded_total[1m]))` |
| 7 | Notifications | `sum(rate(pfe_notification_sent_total{outcome="success"}[1m]))` |
| 8 | Erreurs | `sum(rate(pfe_kafka_messages_total{outcome="error"}[1m]))` |
| 9 | DLT | `sum(rate(pfe_kafka_dlt_total[1m]))` |
| 10 | Concurrence | `pfe_kafka_listener_concurrency` |

---

## 7. Comparaison API synchrone vs pipeline Kafka

### Ce que fait `/test/{count}` (API classique)

- **Endpoint** : `GET http://localhost:8080/test/{count}` (`TextController`)
- **Comportement** : N insertions JPA **dans la même requête HTTP**
- **Réponse JSON** (exemple) :

```json
{
  "mode": "synchronous_api_jpa",
  "count": 1000,
  "durationMs": 45230,
  "durationSec": 45.23,
  "throughputPerSec": 22.1,
  "estimatedCapacityPerDay": 1909440,
  "hint": "Observer ensuite pfe_kafka_* dans Prometheus : pipeline async déclenché par CDC"
}
```

### Ce que mesurent les métriques Kafka (modèle événementiel)

- Traitement **après** la réponse HTTP (via CDC)
- **Parallélisme** (5 threads consumer)
- **Découplage** par topics (`forwarded`)
- **Observabilité** fine (lag, latence, DLT, notifications)

### Limites honnêtes (à mentionner au jury)

- Pas de benchmark contre une autre technologie (RabbitMQ, API Gateway, etc.)
- Capacité max absolue non calculée (saturation Kafka / DB non poussée jusqu’à la rupture)
- `event_lag` dépend du champ `created_at` dans le payload
- Preuve **renforcée** si Debezium + Kafka sont stables pendant toute la démo

---

## 8. Endpoint de charge `/test/{count}`

### Utilisation

```powershell
# PowerShell
Invoke-RestMethod "http://localhost:8080/test/1000"

# curl
curl http://localhost:8080/test/500
```

### Contraintes

- `count` entre **1** et **50000**
- Utilise `userId=26`, `destinataireId=2` (adapter en code si besoin pour votre jeu de test)
- Chaque insert alimente la table `transactions` → CDC → pipeline **transaction**

### Protocole rapide lié aux métriques

1. Lancer `/test/1000` → noter `throughputPerSec`
2. Attendre 2 min → lire Grafana (débit + event_lag + notifications)
3. Rédiger 1 paragraphe comparatif (section 7)

---

## 9. Argumentation pour le mémoire / soutenance

### Paragraphe type « Résultats » (à personnaliser avec vos chiffres)

> Nous avons évalué l’architecture événementielle à l’aide de métriques Prometheus exposées par le backend (`pfe_kafka_*`, `pfe_notification_*`). Sous injection de **N = 1000** transactions via l’endpoint synchrone `/test/1000`, le débit API mesuré est de **X msg/s** avec une durée HTTP de **Y secondes**, illustrant le goulot d’étranglement d’un traitement monolithique synchrone. En parallèle, les métriques Kafka indiquent un débit de traitement de **Z msg/s** (`rate(pfe_kafka_messages_total)`), un routage inter-topics attesté par `pfe_kafka_forwarded_total`, et un délai bout-en-bout p95 de **T secondes** (`pfe_kafka_event_lag`). Les notifications Firebase sont délivrées à **W msg/s** (`pfe_notification_sent_total`). Ces résultats confirment que le modèle asynchrone supporte un flux volumique découplé du temps de réponse HTTP, avec observabilité des latences, du parallélisme (5 consumers) et des mécanismes de résilience (retries, DLT).

### Messages clés en 4 points (slides)

1. **Volume** — `rate(pfe_kafka_messages_total)` monte sous charge.
2. **Temps réel** — `event_lag` et `processing` p95 restent bornés.
3. **Async** — `forwarded` + architecture multi-topics.
4. **Résilience** — retries, DLT, compteurs d’erreurs et notifications.

---

## 10. Dépannage

| Symptôme | Cause probable | Action |
|----------|----------------|--------|
| Je vois `application_*` / `jvm_*` mais pas Prometheus | Confusion Actuator (8080) vs Prometheus (9090) | Targets sur **http://localhost:9090** ; Actuator reste sur **8080** |
| Target `pfe-backend` DOWN | Mauvais host dans `prometheus.yml` | Utiliser `desktop-cuman6h:8080` ou `127.0.0.1:8080` selon ce qui répond dans le navigateur |
| Pas de `pfe_kafka_*` dans Actuator | Normal avant correctif : métriques créées au 1er message | Redémarrer le backend (catalogue `pfe_*` au démarrage) ; Ctrl+F `pfe_` |
| Pas de `pfe_kafka_*` dans Prometheus | Target DOWN | Corriger `prometheus.yml` puis **Status → Targets** = UP |
| Métriques à 0 après `/test` | Debezium / Kafka arrêté | Vérifier connecteur CDC et topic `notifications.public.transactions` |
| `skipped` élevé | Référentiel alerte | `alerte_transaction=true` pour `client_id` |
| Pas de notification | `fcm_token` vide / user absent | `users` + `/api/saveToken` |
| `user_not_found` | `user_id` Kafka ≠ `users.id` | Aligner IDs (ex. 27) |
| `event_lag` vide | Pas de `created_at` dans JSON | Normal sur certains messages carte |
| Target Prometheus DOWN | Backend arrêté ou mauvais port | Relancer Spring Boot |

### Commandes de vérification rapide

```powershell
Invoke-RestMethod "http://localhost:8080/actuator/health"
Invoke-RestMethod "http://localhost:8080/test/10"
```

---

## 11. Checklist avant soutenance

- [ ] Backend, PostgreSQL, Kafka, Debezium opérationnels
- [ ] `referentiel_alerte` + `users.fcm_token` configurés pour l’utilisateur de test
- [ ] Prometheus target **UP** sur `localhost:8080`
- [ ] Grafana datasource Prometheus OK
- [ ] Dashboard 8 panneaux créé
- [ ] Run `/test/1000` + captures Grafana (débit, lag, forwards, notifications)
- [ ] Run `/test/1000` + capture JSON (throughput sync)
- [ ] Tableau comparatif sync vs async rempli
- [ ] Paragraphe « Résultats » rédigé (section 9)
- [ ] Schéma architecture 3 pipelines sur 1 slide

---

## 12. Fichiers du dossier `monitoring/`

| Fichier | Description |
|---------|-------------|
| `METRICS.md` | Ce guide |
| `prometheus-local.yml` | Config scrape **sans Docker** (`localhost:8080`) |
| `prometheus.yml` | Variante Docker (`host.docker.internal:8080`) |
| `docker-compose.yml` | Optionnel : Prometheus + Grafana en conteneurs |
| `grafana/provisioning/datasources/prometheus.yml` | Datasource auto si vous utilisez Docker Compose |

---

*Dernière mise à jour : aligné sur les listeners `ConsumerListener*`, `PipelineMetrics`, `TextController` et `application.properties` du backend PFE.*
