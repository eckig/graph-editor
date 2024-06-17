Graph Editor
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
  <version>19.0.0</version>
</dependency>
```
Download the latest [Release](https://github.com/eckig/graph-editor/releases).

Try the [tutorials](https://github.com/eckig/graph-editor/wiki).

## Demo

Run the sample application after cloning the repository with maven inside the `demo` module with `mvn javafx:run`.
