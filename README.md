# ECM2414 Coursework

A multi-threaded game where players can pickup and discard cards in a non-sequential fashion.


## How to Execute Jar File
Run this command on your command line once in submission directory

_java -jar cardgame.jar_

### How to Play

1. Enter the number of players you wish to have
2. Enter the file name of the pack you want to use (A pack is automatically generated named after the number of players e.g 4 players is pack4.txt)

## How to Execute Test Suite

### Dependencies

- Java
- JUnit 5


### Intellij

Import all files into an Intellij Project with Maven installed to manage dependencies

Import JUnit 5 from this Link: https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api/5.13.4

Run this Maven Command: _mvn test_


### Command Line

#### Install the JUnit Standalone Jar File: 

_curl https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.8.2/junit-platform-console-standalone-1.8.2.jar -OutFile junit-platform-console-standalone-1.8.2.jar_

**or**

_curl https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.8.2/junit-platform-console-standalone-1.8.2.jar --output junit-platform-console-standalone-1.8.2.jar_


#### Execute Test Files

_java -jar junit-platform-console-standalone-1.8.2.jar --class-path cardgame.jar --class-path target/test-classes/cardgame --select-package cardgame_
