Graph Editor <a href="https://foojay.io/today/works-with-openjdk"><img align="right" src="https://github.com/foojayio/badges/raw/main/works_with_openjdk/Works-with-OpenJDK.png" width="100"></a>
==========

A library for creating and editing graph-like diagrams in JavaFX.

*This project is a fork of [tesis-dynaware/graph-editor 1.3.1](https://github.com/tesis-dynaware/graph-editor), which is no longer being maintained.*

## Features

+ Highly customizable
+ Add your own custom skins or restyle existing ones via CSS
+ Graphical effects where connections intersect:

![Examples of how intersections look in the graph editor demo.](intersectionExamples.png)

+ Full undo / redo functionality via EMF commands
+ Selection API with cut, copy, paste
+ Alignment (optionally with snap-to-grid)
+ Editing of large graphs via a panning mechanism & minimap
+ Additional skin examples provided:

![Examples of skins provided with the graph editor demo.](skinExamples.png)

Example of a fully customized implementation:
![Demo of a fully customized application.](demo.gif)

## Use it

Maven coordinates:
```
<dependency>
  <groupId>io.github.eckig.grapheditor</groupId>
  <artifactId>grapheditor-core</artifactId>
  <version>18.0.1</version>
</dependency>
```
Download the latest [Release](https://github.com/eckig/graph-editor/releases).

Try the tutorials [here](https://github.com/eckig/graph-editor/wiki).

## Demo

Download self-executable installer from the [releases](https://github.com/eckig/graph-editor/releases).

Or compile and run it yourself:

 1. Requires [Java 17](https://adoptium.net/), [Git](http://git-scm.com/) and [Maven](http://maven.apache.org/).
 2. Clone the project (`git clone https://github.com/eckig/graph-editor.git`
 3. Import into your favorite IDE
 4. Navigate to the main class `GraphEditorDemo` inside the `demo` project and launch it.

## Thanks to

 - Jetbrains for the [IntelliJ IDEA](https://www.jetbrains.com/idea/) licenses
