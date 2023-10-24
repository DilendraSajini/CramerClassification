package org.katas.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.katas.graph.SvgConstants.any;
import static org.katas.graph.SvgConstants.all;
import static org.katas.graph.SvgConstants.blue;
import static org.katas.graph.SvgConstants.category;
import static org.katas.graph.SvgConstants.circle;
import static org.katas.graph.SvgConstants.line;
import static org.katas.graph.SvgConstants.ellipse;
import static org.katas.graph.SvgConstants.color;
import static org.katas.graph.SvgConstants.decision;
import static org.katas.graph.SvgConstants.edge_No;
import static org.katas.graph.SvgConstants.edge_Yes;
import static org.katas.graph.SvgConstants.greaterThan;
import static org.katas.graph.SvgConstants.green;
import static org.katas.graph.SvgConstants.name;
import static org.katas.graph.SvgConstants.numericComparator;
import static org.katas.graph.SvgConstants.only;
import static org.katas.graph.SvgConstants.outcome;
import static org.katas.graph.SvgConstants.radius;
import static org.katas.graph.SvgConstants.rectangle;
import static org.katas.graph.SvgConstants.square;
import static org.katas.graph.SvgConstants.red;
import static org.katas.graph.SvgConstants.selection;
import static org.katas.graph.SvgConstants.shape;
import static org.katas.graph.SvgConstants.text;
import static org.katas.graph.SvgConstants.textContains;
import static org.katas.graph.SvgConstants.numOfElements;
import static org.katas.graph.SvgConstants.length;
import static org.katas.graph.SvgConstants.height;
import static org.katas.graph.SvgConstants.opacity;
import static org.katas.graph.SvgConstants.area;
import static org.katas.graph.SvgConstants.elementCount;

import org.apache.tinkerpop.gremlin.orientdb.OrientGraph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.jupiter.api.Test;

class DbServiceTest {
	@Test
	void checkConnection() {
		try (var dbService = DbService.getInstance()) {
			var db = dbService.getDb();
			var name = db.database().getName();
			assertEquals("kata1", name, "Create this database manually in remote OrientDb studio");
		}
	}

	@Test
	void buildSvgDecisionTree() {
		try (var dbService = DbService.getInstance()) {
			var db = dbService.getDb();
			var dbName = db.database().getName();
			assertEquals("kata1", dbName);

			var start = db.addVertex(SvgConstants.start);
			start.property("name", "svg1");

			var anyRedCircles = db.addVertex(decision);
			start.addEdge("entry", anyRedCircles);

			anyRedCircles.property("name", "Any red circles");
			anyRedCircles.property(selection, any);
			anyRedCircles.property(color, red);
			anyRedCircles.property(shape, circle);

			var anyRedCirclesYes = db.addVertex(outcome);
			anyRedCircles.addEdge(edge_Yes, anyRedCirclesYes);
			anyRedCirclesYes.property(category, 3);

			var onlyBlueCircles = db.addVertex(decision);
			anyRedCircles.addEdge(edge_No, onlyBlueCircles);
			onlyBlueCircles.property(name, "Only blue circles");
			onlyBlueCircles.property(selection, only);
			onlyBlueCircles.property(color, blue);
			onlyBlueCircles.property(shape, circle);

			makeOnlyBlueCirclesFalseArm(db, onlyBlueCircles);
			makeOnlyBlueCirclesTrueArm(db, onlyBlueCircles);

			db.commit();
		}
	}

	private void makeOnlyBlueCirclesFalseArm(OrientGraph db, Vertex onlyBlueCircles) {
		var anyRectangles = db.addVertex(decision);
		anyRectangles.property(name, "Any rectangles or squares");
		onlyBlueCircles.addEdge(edge_No, anyRectangles);
		anyRectangles.property(selection, any);
		anyRectangles.property(shape, rectangle);
		anyRectangles.property(shape, square);

		anyRectangles_True(db, anyRectangles);
		anyRectangles_False(db, anyRectangles);
	}

	private void anyRectangles_True(OrientGraph db, Vertex anyRectangles) {
		var anyText = anyTextNode(db);
		anyRectangles.addEdge(edge_Yes, anyText);

		var anyGreenRectangle = db.addVertex(decision);
		anyText.addEdge(edge_Yes, anyGreenRectangle);
		anyGreenRectangle.property(name, "Any green rectangle");
		anyGreenRectangle.property(selection, any);
		anyGreenRectangle.property(shape, rectangle);
		anyGreenRectangle.property(color, green);

		var anyGreenRectangleYes = db.addVertex(outcome);
		anyGreenRectangle.addEdge(edge_Yes, anyGreenRectangleYes);
		anyGreenRectangleYes.property(category, 1);
		
		var moreThanOneElement = getMoreThanOneElement(db);
		anyText_False(db, anyText, moreThanOneElement);
		anyGreenRectangle.addEdge(edge_No, moreThanOneElement);
		
	}
	
	private Vertex getMoreThanOneElement(OrientGraph db) {
		var moreThanOneElement = db.addVertex(decision);
		moreThanOneElement.property(name, "More than one element in the file?");
		moreThanOneElement.property(selection, all); 
		moreThanOneElement.property(shape, all);
		moreThanOneElement.property(numOfElements, 1);
		
		moreThanOneElement(db, moreThanOneElement);
		onlyOneElement(db, moreThanOneElement);
		return moreThanOneElement;
	}

	private void anyText_False(OrientGraph db, Vertex anyText, Vertex moreThanOneElement) {
		anyText.addEdge(edge_No, moreThanOneElement);
	}

	private void moreThanOneElement(OrientGraph db, Vertex moreThanOneElement) {
		var anyStraightLine = db.addVertex(decision);
		moreThanOneElement.addEdge(edge_Yes, anyStraightLine);
		anyStraightLine.property(name, "Any straight lines?");
		anyStraightLine.property(selection, any); 
		anyStraightLine.property(shape, line);
		
		anyStrightLine_True(db, anyStraightLine);
		anyStrightLine_False(db, anyStraightLine);
	}
	
	private void anyStrightLine_True(OrientGraph db, Vertex anyStraightLine) {
		var lenghOfLine = db.addVertex(decision);
		anyStraightLine.addEdge(edge_Yes, lenghOfLine);
		lenghOfLine.property(name, "Every line longer than 100?");
		lenghOfLine.property(selection, all); 
		lenghOfLine.property(shape, line);
		lenghOfLine.property(length, 100);
		
		lineLength_moreThan100(db, lenghOfLine);
		lineLength_lessThan100(db, lenghOfLine);
	}
	
	private void lineLength_moreThan100(OrientGraph db, Vertex lenghOfLine) {
		var lengthMoreThan100 = db.addVertex(outcome);
		lenghOfLine.addEdge(edge_Yes, lengthMoreThan100);
		lengthMoreThan100.property(category, 2);
	}
	
	private void lineLength_lessThan100(OrientGraph db, Vertex lenghOfLine) {
		var lengthLessThan100 = db.addVertex(outcome);
		lenghOfLine.addEdge(edge_No, lengthLessThan100);
		lengthLessThan100.property(category, 3);
	}
	
	private void anyStrightLine_False(OrientGraph db, Vertex anyStraightLine) {
		var anyEllipse = db.addVertex(decision);
		anyStraightLine.addEdge(edge_No, anyEllipse);
		anyEllipse.property(name, "Any ellipses?");
		anyEllipse.property(selection, any);
		anyEllipse.property(shape, ellipse);
		
		anyEllipse_True(db,anyEllipse);
		anyEllipse_False(db,anyEllipse);
	}
	
	private void anyEllipse_True(OrientGraph db, Vertex anyEllipse) {
		var ellipseHeight = db.addVertex(decision);
		anyEllipse.addEdge(edge_Yes, ellipseHeight);
		ellipseHeight.property(name, "Any ellipses with height >= 50 ");
		ellipseHeight.property(selection, any);
		ellipseHeight.property(shape, ellipse);
		ellipseHeight.property(height, 50);
		
		ellipseHeight_morethan50(db, ellipseHeight);
		ellipseHeight_lessthan50(db, ellipseHeight);
	}
	
	private void anyEllipse_False(OrientGraph db, Vertex anyEllipse) {
		var elementOpacity = db.addVertex(decision);
		anyEllipse.addEdge(edge_No, elementOpacity);
		elementOpacity.property(name, "Any elements with opacity less than 1? ");
		elementOpacity.property(selection, any);
		elementOpacity.property(shape, all);
		elementOpacity.property(opacity, 1);
		
		var opacityGreaterThan1 = db.addVertex(outcome);
		elementOpacity.addEdge(edge_No, opacityGreaterThan1);
		opacityGreaterThan1.property(category, 3);
		
		var elementsCount = db.addVertex(decision);
		elementsCount.property(name, "Total element count > 5 ? ");
		elementsCount.property(selection, all); 
		elementsCount.property(shape, all);
		elementsCount.property(elementCount, 5);

		var elementCountMorethan5 = db.addVertex(outcome);
		elementsCount.addEdge(edge_Yes, elementCountMorethan5);
		elementCountMorethan5.property(category, 1);	
		
		var elementCountLessthan5 = db.addVertex(outcome);
		elementsCount.addEdge(edge_No, elementCountLessthan5);
		elementCountLessthan5.property(category, 2);	
		
		elementOpacity.addEdge(edge_Yes, elementsCount);
	}
	
	private void ellipseHeight_morethan50(OrientGraph db, Vertex anyEllipse) {
		var heightMorethanFifty = db.addVertex(outcome);
		anyEllipse.addEdge(edge_Yes, heightMorethanFifty);
		heightMorethanFifty.property(category, 3);
	}
	
	private void ellipseHeight_lessthan50(OrientGraph db, Vertex heightMorethanFifty) {
		var rectArea = db.addVertex(decision);
		heightMorethanFifty.addEdge(edge_No, rectArea);
		rectArea.property(name, "Any rectangle with area >= 300? ");
		rectArea.property(selection, any); 
		rectArea.property(shape, rectangle);
		rectArea.property(area, 300);
		
		rectangleArea_lessthan300(db, rectArea);
		rectangleArea_morethan300(db, rectArea);
	}

	private void rectangleArea_lessthan300(OrientGraph db, Vertex rectArea) {
		var elementsCount = db.addVertex(decision);
		elementsCount.property(name, "Total element count > 5 ? ");
		elementsCount.property(selection, all);
		elementsCount.property(shape, all);
		elementsCount.property(elementCount, 5);

		var elementCountMorethan5 = db.addVertex(outcome);
		elementsCount.addEdge(edge_Yes, elementCountMorethan5);
		elementCountMorethan5.property(category, 2);
		
		var elementCountLessthan5 = db.addVertex(outcome);
		elementsCount.addEdge(edge_No, elementCountLessthan5);
		elementCountLessthan5.property(category, 3);
		
		rectArea.addEdge(edge_No, elementsCount);
	}
	
	private void rectangleArea_morethan300(OrientGraph db, Vertex rectArea) {
		var areaMorethan300 = db.addVertex(outcome);
		rectArea.addEdge(edge_Yes, areaMorethan300);
		areaMorethan300.property(category, 1);	
	}
	
	private void onlyOneElement(OrientGraph db, Vertex moreThanOneElement) {
		var lessThanOneElement = db.addVertex(outcome);
		moreThanOneElement.addEdge(edge_No, lessThanOneElement);
		lessThanOneElement.property(category, 1);
	}

	private void anyRectangles_False(OrientGraph db, Vertex anyRectangles) {
		var anyText = anyTextNode(db);
		anyRectangles.addEdge(edge_No, anyText);

		var anyTextFalse = db.addVertex(outcome);
		anyText.addEdge(edge_No, anyTextFalse);
		anyTextFalse.property(category, 2);

		var textContainsLhasa = db.addVertex(decision);
		textContainsLhasa.property("name", "Text containing the character sequence 'lhasa'");
		anyText.addEdge(edge_Yes, textContainsLhasa);
		textContainsLhasa.property(textContains, "Lhasa");
		textContainsLhasa.property(shape, text);

		var lhasaTextTrue = db.addVertex(outcome);
		textContainsLhasa.addEdge(edge_Yes, lhasaTextTrue);
		lhasaTextTrue.property(category, 1);

		var lhasaTextFalse = db.addVertex(outcome);
		textContainsLhasa.addEdge(edge_No, lhasaTextFalse);
		lhasaTextFalse.property(category, 3);
	}

	private Vertex anyTextNode(OrientGraph db) {
		var anyText = db.addVertex(decision);
		anyText.property(name, "Any text");
		anyText.property(selection, any);
		anyText.property(shape, text);
		return anyText;
	}

	public static void makeOnlyBlueCirclesTrueArm(OrientGraph db, Vertex onlyBlueCircles) {
		var largerThan50Diameter = db.addVertex(decision);
		largerThan50Diameter.property(name, "larger than 50 diameter");
		onlyBlueCircles.addEdge(edge_Yes, largerThan50Diameter);
		largerThan50Diameter.property(selection, any);
		largerThan50Diameter.property(radius, 50);
		largerThan50Diameter.property(numericComparator, greaterThan);

		var o50DiameterYes = db.addVertex(outcome).property(category, 3).element();
		largerThan50Diameter.addEdge(edge_No, o50DiameterYes);

		var largerThan100Diameter = db.addVertex(decision);
		largerThan50Diameter.addEdge(edge_Yes, largerThan100Diameter);
		largerThan100Diameter.property(name, "larger than 100 diameter");
		largerThan100Diameter.property(selection, any);
		largerThan100Diameter.property(radius, 100);
		largerThan100Diameter.property(numericComparator, greaterThan);

		var largerThan100DiameterYes = db.addVertex(outcome).property(category, 1).element();
		largerThan100Diameter.addEdge(edge_Yes, largerThan100DiameterYes);

		var largerThan100DiameterNo = db.addVertex(outcome).property(category, 2).element();
		largerThan100Diameter.addEdge(edge_No, largerThan100DiameterNo);
	}
}