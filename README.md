Graph Editor
==========

A library for creating and editing graph-like diagrams in JavaFX.

## Features

+ Highly customizable
+ Add your own custom skins or restyle existing ones via CSS
+ Graphical effects where connections intersect:

![Examples of how intersections look in the graph editor demo.](intersectionExamples.png)

+ Full undo / redo functionality via EMF commands
+ Selection API & cut / copy / paste
+ Alignment & snap-to-grid
+ Editing of large graphs via a panning mechanism & minimap
+ Additional skin examples provided:

![Examples of skins provided with the graph editor demo.](skinExamples.png)

## Demo

Download the demo app [here](https://github.com/tesis-dynaware/graph-editor/releases).

## Use it

Maven coordinates:

    <dependency>
        <groupId>de.tesis.dynaware</groupId>
        <artifactId>de.tesis.dynaware.grapheditor.core</artifactId>
        <version>1.3.1</version>
    </dependency>

Try the tutorials [here](https://github.com/tesis-dynaware/graph-editor/wiki).

## Build it yourself

Requires [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html), [Git](http://git-scm.com/), & [Maven](http://maven.apache.org/).

    git clone https://github.com/tesis-dynaware/graph-editor.git
    cd graph-editor
    mvn clean install
    
The demo jar can be found in ```demo/target/deploy```.
