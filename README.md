# ReadiTopics

# Online demo
(http://mediamining.univ-lyon2.fr/readitopics/)


# Install from JAR quick starter

Download the JAR quick starter here : 
http://mediamining.univ-lyon2.fr/velcin/public/readitopics_starter.zip

Then follow the [guide](./documentation/INSTALL.pdf).


# Install from sources

## Maven
You will need Java 8, Maven 3 and Perl 5.

Update the Maven dependancies :

```
maven clean install
```


## Usage

### BrowseTopics


__Exemple :__

```
java -cp readitopics.jar exe.BrowseTopics
```


__Usage :__

Use the  ``help`` command to print a list of commands.

__Windows Users :__ 

In order to use BrowseTopic via an IDE on Windows, you must use the following Java option :

```
-Djline.WindowsTerminal.directConsole=false
```

## Authors

- Julien VELCIN - Laboratoire ERIC - [>> Website](http://mediamining.univ-Lyon2.fr/velcin)
- Antoine GOURRU - Laboratoire ERIC - [>> Website](http://antoinegourru.com)
- Erwan GIRY-FOUQUET - Laboratoire ERIC - [>> Website](http://erwangf.com)
- Christophe Gravier - Laboratory Télécom Claude Chappe -  (christophe.gravier@univ-st-etienne.fr)
- Mathieu Roche - CIRAD - (mathieu.roche@cirad.fr)
- Pascal Poncelet - LIRMM - (pascal.poncelet@lirmm.fr)


