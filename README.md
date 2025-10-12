
# TP1-Partie 2 : Analyseur de Code Java
## Analyse Statique Orientée Objet

**Description du Projet**  
Application Java d’analyse statique de code source permettant de calculer des métriques orientées objet et de générer un graphe d’appel des méthodes. Ce projet a été développé dans le cadre du TP1 Partie 2.

**Auteurs**  
Binôme :
- BENOMAR Fadel | N° étudiant : 22015967
- GRARI Youssef | N° étudiant : 22015973

###  Fonctionnalités

**Exercice 1 : Calcul Statistique**
- Nombre total de classes
- Nombre total de lignes de code
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

**Exercice 2 : Graphe d’Appel**
- Construction du graphe d’appel complet
- Distinction appels internes/externes
- Visualisation multiple (textuelle, arborescente, graphique)

###  Technologies Utilisées
- Java (JDK 8 ou supérieur)
- Maven (gestion des dépendances)
- Eclipse JDT Core 3.32.0 (parsing du code source)
- JGraphX 4.2.2 (visualisation graphique)
- Swing (interface graphique)

###  Prérequis
- JDK 8
- Maven installé et configuré
- Variable d’environnement JAVA_HOME correctement définie

###  Installation et Lancement

**Étape 1 : Cloner ou télécharger le projet**
```bash
cd analyseur_de_code
```

**Étape 2 : Compiler le projet avec Maven**
```bash 
mvn clean compile 
```

**Étape 3 : Les projets pour tester**  
Pour récupérer les projets, téléchargez le dépôt principal depuis GitHub (taille importante) :  
[https://github.com/fb2001/EasyBuy.git](https://github.com/fb2001/EasyBuy.git)  
Remarque : Le deuxième mini-projet se trouve également le fichier ZIP du dépôt.

**Étape 4 : Lancer l’application**

*Option A : Depuis votre IDE*
- Importez le projet Maven dans votre IDE (Eclipse, IntelliJ IDEA, etc.)
- Localisez la classe `AnalyseurGUI.java` dans `src/main/java/analyseurdecode/ui/`
- Exécutez la méthode main()

*Option B : Avec Maven*
```bash
mvn exec:java -Dexec.mainClass="analyseurdecode.ui.AnalyseurGUI"
```