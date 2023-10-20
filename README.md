# Problem Domain :Cramer classification

Cramer classification is a series of yes/no questions and is used as a rough guide to how dangerous a chemical is to humans if we eat them. The output of the workflow gives a category for every compound corresponding to how likely they are to be toxic: 
* 1 for low risk
* 2 moderate
* 3 high risk
* can't classify

This is an initial Java implementation of a sample project that classify sample files based metadata conains it. SVG file describes the chemical and its attributes to a collection of shapes and objects (atoms).  We  then analyse these files and run them through a classification workflow just like the Cramer classification, inspecting different attributes to ultimately get a category for each file.
The workflow looks like this:

![Image describing the workflow](workflow.jpg)

### Requirements
* Maven 3.x
* Java 17+