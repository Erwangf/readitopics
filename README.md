# ReadiTopics

## Présentation

[Changelog](./CHANGELOG.md)

## Installation via Eclipse
Vous devez disposer de Java 8 et d'Eclipse Neon ou une version plus récente.

Le moyen le plus simple est de cloner le répertoire quelquepart, puis d'importer un nouveau
répertoire via Eclipse.

Les fichiers de configuration d'Eclipse ( et d'autres IDE ) sont par défaut ignorés par Git
sur ce projet, donc il faudra choisir l'option "create a new project". [TODO : à documenter]

Ensuite, il faut mettre à jour les dépendances Maven d'Eclipse [TODO : à documenter]

## Installation personnalisée

### Maven
Vous devez disposer de Maven (>3.3) et de Java 8 (JDK).

Mettre à jour les dépendances Maven, depuis le répertoire du projet :

```
maven clean install
```

## Emplacement des jeux de données

Les fichiers de configuration doivent être copiés dans le répertoire *config*.
Les jeux de données doivent être copiés dans le répertoire *data*.

Un exemple de fichier de configuration est disponible dans le répertoire *config*.




## Utilisation

Plusieurs classes exécutables  sont disponibles :

### BrowseTopics

__Paramètres :__
- data (optionnel) : charge un jeu de donnée au démarrage

__Exemple :__

```
java -cp readitopics.jar exe.BrowseTopics
java -cp readitopics.jar exe.BrowseTopics monFichierData
```


__Usage :__

Utiliser la commande ``help`` pour afficher une liste de commande.

__Utilisateurs de Windows__ :

Afin de pouvoir utiliser BrowseTopic via un IDE sous Windows, il faut utiliser l'option Java suivante :

```
-Djline.WindowsTerminal.directConsole=false
```

## Auteurs

- Julien VELCIN - professeur Université Lyon 2 - [>> Site web](http://mediamining.univ-Lyon2.fr/velcin)


## Licence


