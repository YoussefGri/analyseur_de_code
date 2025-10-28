# TP1-Partie 2 : Analyseur de Code Java
## Analyse Statique Orientée Objet (avec extensions Spoon & Clustering)

Application Java d’analyse statique de code source permettant de calculer des métriques orientées objet, de générer un graphe d’appel des méthodes, d’analyser le couplage entre classes, et de produire un dendrogramme (clustering hiérarchique) avec identification de modules.

## Auteurs
Binôme :
- BENOMAR Fadel | N° étudiant : 22015967
- GRARI Youssef | N° étudiant : 22015973

---

## Fonctionnalités

### Exercice 1 : Calcul Statistique (Mode JDT)
- Nombre total de classes
- Nombre total de lignes de code (LOC)
- Nombre total de méthodes
- Nombre total de packages
- Nombre moyen de méthodes par classe
- Nombre moyen de lignes de code par méthode
- Nombre moyen d’attributs par classe
- Top 10% des classes avec le plus de méthodes
- Top 10% des classes avec le plus d’attributs
- Classes présentes dans les deux catégories précédentes
- Classes ayant plus de X méthodes (paramétrable)
- Top 10% des méthodes avec le plus de lignes de code
- Nombre maximum de paramètres par méthode

### Exercice 2 : Graphe d’Appel (Mode JDT)
- Construction du graphe d’appel complet
- Distinction appels internes/externes
- Visualisation multiple (textuelle, arborescente, graphique)

### Extensions TP2 : Couplage, Spoon & Clustering
- Extraction via Spoon (mode alternatif au JDT)
- Calcul de la matrice de couplage entre classes (normalisation par le total des relations)
- Visualisations du couplage:
  - Matrice (tableau) avec coloration
  - Texte (lisible et triable visuellement)
- Dendrogramme (clustering hiérarchique) des classes
- Identification de modules (groupes de classes) à partir du dendrogramme selon un seuil CP (couplage moyen minimal)
- Top couplages (classements par intensité)

---

## Modes de fonctionnement

- Mode JDT (classique): interface `AnalyseurGUI` (parsing via Eclipse JDT Core)
- Mode Spoon (extensions): interface `AnalyseurSpoonGUI` (parsing et analyse via Spoon)
- Mode CLI (texte): `analyseurdecode.ui.CliApp` (pour scripts/CI)

---

## Prérequis
- Java JDK 11 ou supérieur (JDK 17 recommandé)
- Maven 3.8+
- JAVA_HOME correctement configuré (pour JDT, le dossier `jmods` du JDK est utilisé)
- macOS, Linux ou Windows

---

## Installation

```bash
# Cloner ou se placer dans le dossier du projet
cd analyseur_de_code

# Compiler le projet
mvn clean compile

# (Optionnel) Produire le JAR
mvn -DskipTests package
```

Le JAR est généré ici: `target/analyseur_de_code-0.0.1-SNAPSHOT.jar`

---

## Lancement

### Depuis votre IDE
- Importer le projet Maven
- Mode JDT: exécuter `analyseurdecode.ui.AnalyseurLauncher` (méthode `main`)

### Avec Maven (exec)
```bash
mvn -q exec:java -Dexec.mainClass="analyseurdecode.ui.AnalyseurLauncher"


# Mode CLI (peu recommandé)
mvn -q exec:java -Dexec.mainClass="analyseurdecode.ui.CliApp" -Dexec.args="/chemin/vers/votre/src"
```

### À partir du JAR
```bash
# Lancer le JAR (si un Main-Class est configuré)
java -jar target/analyseur_de_code-0.0.1-SNAPSHOT.jar

# Ou spécifier une classe principale
java -cp target/analyseur_de_code-0.0.1-SNAPSHOT.jar analyseurdecode.ui.AnalyseurLauncher
```

---

## Utilisation (rapide)

1) Sélectionnez le dossier source Java du projet à analyser (racine contenant les `.java`).
2) (Mode JDT) Lancez l’analyse pour voir les statistiques et les graphes d’appel.
3) (Mode Spoon) Lancez l’analyse pour obtenir la matrice/graphe de couplage, le top couplages, etc.
4) Onglet « Dendrogramme » (Mode Spoon):
   - Vue « Graphe »: affiche le dendrogramme calculé (clustering hiérarchique).
   - Vue « Modules avec CP »: saisissez un seuil CP (0–1) puis « Identifier les modules » pour regrouper les classes fortement couplées.
   - Recommandations de CP:
     - 0.01: modules larges (couplage faible accepté)
     - 0.05: modules moyens
     - 0.10: modules compacts (couplage fort requis)
---

## Jeux de test (projets d’exemple)
Pour tester rapidement, vous pouvez utiliser:
- EasyBuy (dépôt principal, taille importante): https://github.com/fb2001/EasyBuy.git
- Un mini-projet fourni dans le ZIP du dépôt principal

---

## Dépannage

- JDT – erreur de configuration classpath/JRE
  - Le parseur JDT utilise `JAVA_HOME/jmods`. Vérifiez que `JAVA_HOME` pointe vers un JDK (pas un JRE) et que le dossier `jmods` existe.
  - Sous macOS: `export JAVA_HOME=$(/usr/libexec/java_home -v 17)` (ex.)

- Spoon – résolution des types
  - Le parsing Spoon est exécuté avec `noClasspath=true` pour une meilleure tolérance; certaines déclarations externes peuvent ne pas être résolues finement.

- Aucun couplage détecté
  - Il se peut que les classes n’appellent pas de méthodes d’autres classes ou que le filtrage (min méthodes) soit trop restrictif.

- Aucun module identifié
  - Essayez de réduire CP (ex. 0.01 → 0.005 → 0.001) pour former des groupes plus larges.

---

## Architecture rapide

- `analyseurdecode.parser`
  - `SourceParser` (JDT)
  - `SpoonSourceParser` (orchestrateur Spoon)
  - `SpoonParserConfig` (configuration/construction du modèle Spoon)
  - `SpoonModelExtractor` (extraction classes/méthodes/attributs + index des méthodes)
  - `MethodCallAnalyzer` (analyse des appels et enrichissement de l’index)
- `analyseurdecode.processor`
  - `StatisticsService`, `StatisticsProcessor` (métriques)
  - `HierarchicalClusteringProcessor`, `DendrogramNode` (clustering)
  - `ModuleIdentifier`, `Module` (identification de modules)
- `analyseurdecode.ui`
  - `AnalyseurGUI` (mode JDT)
  - `AnalyseurSpoonGUI` (mode Spoon)
  - `DendrogramPanel`, `CallGraphPanel` (visualisations)
  - `DendrogramModulesTabBuilder` (onglet réutilisable Dendrogramme/Modules)
  - `CliApp` (mode CLI)

---

## Licence
Projet académique – usage pédagogique.
