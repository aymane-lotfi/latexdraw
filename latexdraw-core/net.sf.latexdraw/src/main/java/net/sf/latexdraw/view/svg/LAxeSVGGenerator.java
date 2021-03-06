/*
 * This file is part of LaTeXDraw.
 * Copyright (c) 2005-2017 Arnaud BLOUIN
 * LaTeXDraw is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 * LaTeXDraw is distributed without any warranty; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 */
package net.sf.latexdraw.view.svg;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;
import net.sf.latexdraw.badaboom.BadaboomCollector;
import net.sf.latexdraw.models.ShapeFactory;
import net.sf.latexdraw.models.interfaces.shape.AxesStyle;
import net.sf.latexdraw.models.interfaces.shape.BorderPos;
import net.sf.latexdraw.models.interfaces.shape.IArrow;
import net.sf.latexdraw.models.interfaces.shape.IAxes;
import net.sf.latexdraw.models.interfaces.shape.IPoint;
import net.sf.latexdraw.models.interfaces.shape.IPolyline;
import net.sf.latexdraw.models.interfaces.shape.IRectangle;
import net.sf.latexdraw.models.interfaces.shape.IShape;
import net.sf.latexdraw.models.interfaces.shape.PlottingStyle;
import net.sf.latexdraw.models.interfaces.shape.TicksStyle;
import net.sf.latexdraw.parsers.svg.SVGAttributes;
import net.sf.latexdraw.parsers.svg.SVGDocument;
import net.sf.latexdraw.parsers.svg.SVGElement;
import net.sf.latexdraw.parsers.svg.SVGElements;
import net.sf.latexdraw.parsers.svg.SVGGElement;
import net.sf.latexdraw.parsers.svg.SVGNodeList;
import net.sf.latexdraw.parsers.svg.parsers.SVGPointsParser;
import net.sf.latexdraw.util.LNamespace;

/**
 * An SVG generator for an shape.
 * @author Arnaud BLOUIN
 */
class LAxeSVGGenerator extends LShapeSVGGenerator<IAxes> {

	protected LAxeSVGGenerator(final IAxes shape) {
		super(shape);
	}


	/**
	 * Creates axes from a latexdraw-SVG element.
	 * @param elt The source element.
	 * @param withTransformation If true, the SVG transformations will be applied.
	 */
	protected LAxeSVGGenerator(final SVGGElement elt, final boolean withTransformation) {
		this(ShapeFactory.INST.createAxes(ShapeFactory.INST.createPoint()));

		if(elt == null) {
			throw new IllegalArgumentException();
		}

		setSVGParameters(elt);

		List<Point2D> values;
		final String pref = LNamespace.LATEXDRAW_NAMESPACE + ':';
		final String str;

		shape.setShowOrigin(Boolean.parseBoolean(elt.getAttribute(pref + LNamespace.XML_AXE_SHOW_ORIGIN)));
		shape.setAxesStyle(AxesStyle.getStyle(elt.getAttribute(pref + LNamespace.XML_STYLE)));
		shape.setTicksDisplayed(PlottingStyle.getStyle(elt.getAttribute(pref + LNamespace.XML_AXE_SHOW_TICKS)));
		shape.setTicksStyle(TicksStyle.getStyle(elt.getAttribute(pref + LNamespace.XML_AXE_TICKS_STYLE)));
		shape.setLabelsDisplayed(PlottingStyle.getStyle(elt.getAttribute(pref + LNamespace.XML_AXE_LABELS_STYLE)));
		str = elt.getAttribute(pref + LNamespace.XML_AXE_TICKS_SIZE);

		if(str != null) {
			try {
				shape.setTicksSize(Double.parseDouble(str));
			}catch(final NumberFormatException e) { /* */ }
		}

		values = SVGPointsParser.getPoints(elt.getAttribute(pref + LNamespace.XML_GRID_END));

		if(values != null && !values.isEmpty()) {
			shape.setGridEndX((int) values.get(0).getX());
			shape.setGridEndY((int) values.get(0).getY());
		}

		values = SVGPointsParser.getPoints(elt.getAttribute(pref + LNamespace.XML_GRID_START));

		if(values != null && !values.isEmpty()) {
			shape.setGridStartX((int) values.get(0).getX());
			shape.setGridStartY((int) values.get(0).getY());
		}

		values = SVGPointsParser.getPoints(elt.getAttribute(pref + LNamespace.XML_GRID_ORIGIN));

		if(values != null && !values.isEmpty()) {
			shape.setOriginX((int) values.get(0).getX());
			shape.setOriginY((int) values.get(0).getY());
		}

		values = SVGPointsParser.getPoints(elt.getAttribute(pref + LNamespace.XML_AXE_INCREMENT));

		if(values != null && !values.isEmpty()) {
			shape.setIncrementX(values.get(0).getX());
			shape.setIncrementY(values.get(0).getY());
		}

		values = SVGPointsParser.getPoints(elt.getAttribute(pref + LNamespace.XML_AXE_DIST_LABELS));

		if(values != null && !values.isEmpty()) {
			shape.setDistLabelsX(values.get(0).getX());
			shape.setDistLabelsY(values.get(0).getY());
		}

		/* Looking for the two axe in order to get the position of the axes. */
		final SVGNodeList nl = elt.getChildren(SVGElements.SVG_G);
		int i = 0;
		final int size = nl.getLength();
		SVGGElement l1 = null;
		SVGGElement l2 = null;
		SVGElement element;

		while((l1 == null || l2 == null) && i < size) {
			element = nl.item(i);

			if(element instanceof SVGGElement) {
				if(l1 == null) {
					l1 = (SVGGElement) element;
				}else {
					l2 = (SVGGElement) element;
				}
			}

			i++;
		}

		if(l1 != null && l2 != null) {
			try {
				final IPolyline la = new LPolylinesSVGGenerator(l1, false).shape;
				final IPolyline lb = new LPolylinesSVGGenerator(l2, false).shape;

				shape.setPosition(ShapeFactory.INST.createPoint(lb.getPtAt(0).getX(), la.getPtAt(0).getY()));
				shape.setLineStyle(la.getLineStyle());
				shape.getArrowAt(1).setArrowStyle(la.getArrowAt(0).getArrowStyle());
				shape.getArrowAt(3).setArrowStyle(la.getArrowAt(1).getArrowStyle());
				shape.getArrowAt(0).setArrowStyle(lb.getArrowAt(0).getArrowStyle());
				shape.getArrowAt(2).setArrowStyle(lb.getArrowAt(1).getArrowStyle());
			}catch(final IllegalArgumentException ex) {
				BadaboomCollector.INSTANCE.add(ex);
			}
		}

		homogeniseArrows(shape.getArrowAt(0), shape.getArrowAt(1));
		homogeniseArrows(shape.getArrowAt(1), shape.getArrowAt(2));
		homogeniseArrows(shape.getArrowAt(2), shape.getArrowAt(3));
		applyTransformations(elt);
	}


	@Override
	public SVGElement toSVG(final SVGDocument doc) {
		if(doc == null) {
			return null;
		}

		final SVGElement root = new SVGGElement(doc);
		final String pref = LNamespace.LATEXDRAW_NAMESPACE + ':';

		setSVGAttributes(doc, root, false);

		root.setAttribute(pref + LNamespace.XML_STYLE, shape.getAxesStyle().toString());
		root.setAttribute(pref + LNamespace.XML_GRID_START, shape.getGridStartX() + " " + shape.getGridStartY()); //$NON-NLS-1$
		root.setAttribute(pref + LNamespace.XML_GRID_END, shape.getGridEndX() + " " + shape.getGridEndY()); //$NON-NLS-1$
		root.setAttribute(pref + LNamespace.XML_GRID_ORIGIN, shape.getOriginX() + " " + shape.getOriginY()); //$NON-NLS-1$
		root.setAttribute(pref + LNamespace.XML_AXE_INCREMENT, shape.getIncrementX() + " " + shape.getIncrementY()); //$NON-NLS-1$
		root.setAttribute(pref + LNamespace.XML_AXE_DIST_LABELS, shape.getDistLabelsX() + " " + shape.getDistLabelsY()); //$NON-NLS-1$
		root.setAttribute(pref + LNamespace.XML_AXE_TICKS_SIZE, String.valueOf(shape.getTicksSize()));
		root.setAttribute(pref + LNamespace.XML_AXE_SHOW_ORIGIN, String.valueOf(shape.isShowOrigin()));
		root.setAttribute(pref + LNamespace.XML_AXE_SHOW_TICKS, shape.getTicksDisplayed().toString());
		root.setAttribute(pref + LNamespace.XML_AXE_LABELS_STYLE, shape.getLabelsDisplayed().toString());
		root.setAttribute(pref + LNamespace.XML_AXE_TICKS_STYLE, shape.getTicksStyle().toString());
		root.setAttribute(LNamespace.LATEXDRAW_NAMESPACE + ':' + LNamespace.XML_TYPE, LNamespace.XML_TYPE_AXE);
		root.setAttribute(SVGAttributes.SVG_ID, getSVGID());
		createSVGAxe(root, doc);
		setSVGRotationAttribute(root);

		return root;
	}


	private void createArrows(final SVGElement elt, final SVGDocument document) {
		if(shape.getAxesStyle().supportsArrows() && shape.getNbArrows() == 4) {
			final double posX = shape.getPosition().getX();
			final double posY = shape.getPosition().getY();
			final IArrow arr0 = shape.getArrowAt(1);
			final IArrow arr1 = shape.getArrowAt(3);
			final double arr0Reduction = arr0.getArrowStyle().needsLineReduction() ? arr0.getArrowShapedWidth() : 0.;
			final double arr1Reduction = arr1.getArrowStyle().needsLineReduction() ? arr1.getArrowShapedWidth() : 0.;
			final IPolyline xLine = ShapeFactory.INST.createPolyline(Arrays.asList(ShapeFactory.INST.createPoint(posX + shape.getGridStartX() * 
				IShape.PPC + arr0Reduction, posY), ShapeFactory.INST.createPoint(posX + shape.getGridEndX() * IShape.PPC - arr1Reduction, posY)));
			final IPolyline yLine = ShapeFactory.INST.createPolyline(Arrays.asList(ShapeFactory.INST.createPoint(posX, posY - shape.getGridStartY() * 
				IShape.PPC - arr0Reduction), ShapeFactory.INST.createPoint(posX, posY - shape.getGridEndY() * IShape.PPC + arr1Reduction)));


			xLine.getArrowAt(0).copy(arr0);
			xLine.getArrowAt(1).copy(arr1);
			yLine.getArrowAt(0).copy(shape.getArrowAt(0));
			yLine.getArrowAt(1).copy(shape.getArrowAt(2));
			final SVGElement eltX = new LPolylinesSVGGenerator(xLine).toSVG(document);
			final SVGElement eltY = new LPolylinesSVGGenerator(yLine).toSVG(document);

			elt.appendChild(eltX);
			elt.appendChild(eltY);
		}
	}


	private void createFrame(final SVGElement elt, final SVGDocument document) {
		final double gridEndx = shape.getGridEndX();
		final double gridEndy = shape.getGridEndY();

		if(gridEndx > 0 || gridEndy > 0) {
			final double positionx = shape.getPosition().getX();
			final double positiony = shape.getPosition().getY();
			final double xMax = positionx + gridEndx * IShape.PPC;
			final double yMax = positiony - gridEndy * IShape.PPC;
			final IPoint pos = ShapeFactory.INST.createPoint(positionx, gridEndy > 0 ? yMax : positiony);
			final IRectangle r = ShapeFactory.INST.createRectangle(pos, Math.abs(pos.getX() - (gridEndx > 0 ? xMax : positionx)), Math.abs(pos.getY() - positiony));

			r.setBordersPosition(BorderPos.MID);
			r.setLineColour(shape.getLineColour());
			r.setLineStyle(shape.getLineStyle());
			r.setThickness(shape.getThickness());
			elt.appendChild(new LRectangleSVGGenerator(r).toSVG(document));
		}
	}


	private void createSVGAxe(final SVGElement elt, final SVGDocument document) {
		switch(shape.getAxesStyle()) {
			case AXES:
				createArrows(elt, document);
				break;
			case FRAME:
				createFrame(elt, document);
				break;
			case NONE:
				break;
		}
	}
}
