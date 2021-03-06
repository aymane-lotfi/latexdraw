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

import java.util.Objects;
import net.sf.latexdraw.badaboom.BadaboomCollector;
import net.sf.latexdraw.models.MathUtils;
import net.sf.latexdraw.models.interfaces.shape.ArrowStyle;
import net.sf.latexdraw.models.interfaces.shape.IArrow;
import net.sf.latexdraw.models.interfaces.shape.IShape;
import net.sf.latexdraw.parsers.svg.CSSColors;
import net.sf.latexdraw.parsers.svg.SVGAttributes;
import net.sf.latexdraw.parsers.svg.SVGCircleElement;
import net.sf.latexdraw.parsers.svg.SVGDocument;
import net.sf.latexdraw.parsers.svg.SVGElement;
import net.sf.latexdraw.parsers.svg.SVGElements;
import net.sf.latexdraw.parsers.svg.SVGMarkerElement;
import net.sf.latexdraw.parsers.svg.SVGNodeList;
import net.sf.latexdraw.parsers.svg.SVGPathElement;
import net.sf.latexdraw.parsers.svg.path.SVGPathSeg;
import net.sf.latexdraw.parsers.svg.path.SVGPathSegClosePath;
import net.sf.latexdraw.parsers.svg.path.SVGPathSegCurvetoCubic;
import net.sf.latexdraw.parsers.svg.path.SVGPathSegLineto;
import net.sf.latexdraw.parsers.svg.path.SVGPathSegLinetoVertical;
import net.sf.latexdraw.parsers.svg.path.SVGPathSegList;
import net.sf.latexdraw.parsers.svg.path.SVGPathSegMoveto;
import net.sf.latexdraw.util.LNamespace;

/**
 * An SVG generator for arrows.
 * @author Arnaud BLOUIN
 */
class LArrowSVGGenerator {
	/** The arrowhead generated or used to generate the SVG-arrowd */
	IArrow arrow;

	/**
	 * Creates an SVG arrow generator.
	 * @param arr The arrow. Must not be null.
	 */
	LArrowSVGGenerator(final IArrow arr) {
		super();
		arrow = Objects.requireNonNull(arr);
	}


	/**
	 * Initialises the arrow using an SVGMarkerElement.
	 * @param elt The SVGMarkerElement uses to initialise the arrow.
	 * @param owner The figure the has the arrow.
	 */
	void setArrow(final SVGMarkerElement elt, final IShape owner, final String svgMarker) {
		SVGNodeList nl = elt.getChildren(SVGElements.SVG_PATH);

		if(nl.getLength() == 0) {
			nl = elt.getChildren(SVGElements.SVG_CIRCLE);

			if(nl.getLength() > 0) {
				setArrow((SVGCircleElement) nl.item(0), elt, owner);
			}
		}else {
			setArrow((SVGPathElement) nl.item(0), elt, owner, svgMarker);
		}
	}

	private double parseDoubleArrowValue(final String value) {
		if(value == null) {
			return 1d;
		}
		try {
			return Double.parseDouble(value);
		}catch(final NumberFormatException ex) {
			BadaboomCollector.INSTANCE.add(ex);
			return 1d;
		}
	}

	/**
	 * Initialises the arrowhead using a circle arrow.
	 * @param circle The circle element.
	 * @param elt The arrowhead element.
	 * @param owner The shape that has the arrow.
	 */
	void setArrow(final SVGCircleElement circle, final SVGMarkerElement elt, final IShape owner) {
		final double radius = circle.getR();
		final double dotSizeDim;
		final double dotSizeNum = parseDoubleArrowValue(circle.getAttribute(LNamespace.LATEXDRAW_NAMESPACE + ':' + LNamespace.XML_ARROW_DOT_SIZE_NUM));
		final double lineWidth = owner.hasDbleBord() ? owner.getDbleBordSep() + 2d * owner.getThickness() : owner.getThickness();

		if(circle.getStroke() == null) {
			arrow.setArrowStyle(MathUtils.INST.equalsDouble(elt.getRefX(), 0d) ? ArrowStyle.DISK_END : ArrowStyle.DISK_IN);
			dotSizeDim = radius * lineWidth * 2d - dotSizeNum * lineWidth;
		}else {
			arrow.setArrowStyle(MathUtils.INST.equalsDouble(elt.getRefX(), 0d) ? ArrowStyle.CIRCLE_END : ArrowStyle.CIRCLE_IN);
			dotSizeDim = (radius * lineWidth + lineWidth / 2d) * 2d - dotSizeNum * lineWidth;
		}

		if(MathUtils.INST.equalsDouble(dotSizeDim, 0d)) {
			arrow.setArrowStyle(ArrowStyle.ROUND_IN);
		}else {
			arrow.setDotSizeDim(dotSizeDim);
			arrow.setDotSizeNum(dotSizeNum);
		}
	}


	private void setArrowBarBracket(final SVGPathElement path, final SVGPathSegMoveto m, final double lineWidth, final SVGPathSeg seg,
									final SVGMarkerElement elt, final SVGPathSegList list, final String svgMarker) {
		final double tbarNum = parseDoubleArrowValue(path.getAttribute(LNamespace.LATEXDRAW_NAMESPACE + ':' + LNamespace.XML_ARROW_TBAR_SIZE_NUM));
		double y = Math.abs(m.getY());
		final boolean isStartArrow = SVGAttributes.SVG_MARKER_START.equals(svgMarker);

		arrow.setTBarSizeNum(tbarNum);
		arrow.setTBarSizeDim(y * lineWidth * 2d - tbarNum * lineWidth);

		if((seg instanceof SVGPathSegLineto && MathUtils.INST.equalsDouble(((SVGPathSegLineto) seg).getX(), m.getX())) || seg instanceof SVGPathSegLinetoVertical) {
			arrow.setArrowStyle(MathUtils.INST.equalsDouble(m.getX(), 0d) ? ArrowStyle.BAR_IN : ArrowStyle.BAR_END);
			return;
		}
		if(seg instanceof SVGPathSegCurvetoCubic) {
			final double width = (arrow.getTBarSizeDim() + arrow.getTBarSizeNum() * lineWidth) / lineWidth;
			final double rBrack = (Math.abs(m.getX()) - 0.5) / width;

			arrow.setArrowStyle(MathUtils.INST.equalsDouble(Math.abs(m.getX()), 0.5) ? ArrowStyle.RIGHT_ROUND_BRACKET : ArrowStyle.LEFT_ROUND_BRACKET);
			if(!isStartArrow) {
				arrow.setArrowStyle(arrow.getArrowStyle().getOppositeArrowStyle());
			}
			arrow.setRBracketNum(rBrack);
			return;
		}
		// It may be a bracket.
		if(list.size() == 4 && seg instanceof SVGPathSegLineto && list.get(2) instanceof SVGPathSegLineto && list.get(3) instanceof SVGPathSegLineto) {
			final double lgth = Math.abs(m.getX() - ((SVGPathSegLineto) seg).getX());

			y += m.getY() > 0d ? -0.5 : 0.5;
			arrow.setTBarSizeDim(y * lineWidth * 2d - tbarNum * lineWidth);
			arrow.setBracketNum((lgth - 0.5) * lineWidth / (arrow.getTBarSizeDim() / IShape.PPC + arrow.getTBarSizeNum() * lineWidth));
			arrow.setArrowStyle(elt.getRefX() > 0d ? ArrowStyle.RIGHT_SQUARE_BRACKET : ArrowStyle.LEFT_SQUARE_BRACKET);
		}
	}


	private void setArrowArrow(final SVGPathElement path, final SVGPathSegMoveto m, final double lineWidth, final SVGPathSeg seg, final SVGPathSegList list, 
							   final String svgMarker) {
		if(!(seg instanceof SVGPathSegLineto && list.get(2) instanceof SVGPathSegLineto && list.get(3) instanceof SVGPathSegLineto &&
			list.get(4) instanceof SVGPathSegClosePath)) {
			return;
		}

		final double arrNum = parseDoubleArrowValue(path.getAttribute(LNamespace.LATEXDRAW_NAMESPACE + ':' + LNamespace.XML_ARROW_SIZE_NUM));
		final double arrDim;
		final double lgth = Math.abs(((SVGPathSegLineto) seg).getX() - m.getX());
		final boolean moveIs0 = MathUtils.INST.equalsDouble(m.getX(), 0d) && MathUtils.INST.equalsDouble(m.getY(), 0d);
		final boolean isStartArrow = SVGAttributes.SVG_MARKER_START.equals(svgMarker);

		if(list.size() == 10) {
			arrow.setArrowStyle(moveIs0 ? ArrowStyle.LEFT_DBLE_ARROW : ArrowStyle.RIGHT_DBLE_ARROW);
		}else {
			arrow.setArrowStyle(moveIs0 ? ArrowStyle.LEFT_ARROW : ArrowStyle.RIGHT_ARROW);
		}

		if(!isStartArrow) {
			arrow.setArrowStyle(arrow.getArrowStyle().getOppositeArrowStyle());
		}

		arrDim = lineWidth * (((SVGPathSegLineto) seg).getY() * 2d - arrNum);
		arrow.setArrowLength(lgth / ((arrNum * lineWidth + arrDim) / lineWidth));
		arrow.setArrowSizeDim(arrDim);
		arrow.setArrowSizeNum(arrNum);
		arrow.setArrowInset(Math.abs(((SVGPathSegLineto) seg).getX() - ((SVGPathSegLineto) list.get(2)).getX()) / lgth);
	}


	/**
	 * Initialises the arrowhead using a path arrow.
	 * @param path The path element.
	 * @param elt The arrowhead element.
	 * @param owner The shape that has the arrow.
	 */
	void setArrow(final SVGPathElement path, final SVGMarkerElement elt, final IShape owner, final String svgMarker) {
		final SVGPathSegList list = path.getSegList();
		final SVGPathSegMoveto m = (SVGPathSegMoveto) list.get(0);
		final double lineWidth = owner.hasDbleBord() ? owner.getDbleBordSep() + 2d * owner.getThickness() : owner.getThickness();

		// It may be a bar or a bracket
		if(list.size() == 2 || list.size() == 4) {
			setArrowBarBracket(path, m, lineWidth, list.get(1), elt, list, svgMarker);
		}else {
			// It may be an arrow or a double arrow
			if(list.size() == 5 || list.size() == 10) {
				setArrowArrow(path, m, lineWidth, list.get(1), list, svgMarker);
			}
		}
	}


	private double toSVGCircle(final SVGDocument doc, final double lineWidth, final boolean isShadow, final SVGElement marker) {
		final IShape shape = arrow.getShape();
		final SVGElement circle = new SVGCircleElement(doc);
		final double r = (arrow.getDotSizeDim() + arrow.getDotSizeNum() * lineWidth) / 2d - lineWidth / 2d;

		circle.setAttribute(LNamespace.LATEXDRAW_NAMESPACE + ':' + LNamespace.XML_ARROW_DOT_SIZE_NUM, String.valueOf(arrow.getDotSizeNum()));
		circle.setAttribute(SVGAttributes.SVG_R, String.valueOf(r / lineWidth));
		circle.setAttribute(SVGAttributes.SVG_FILL, CSSColors.INSTANCE.getColorName(shape.getFillingCol(), true));
		circle.setAttribute(SVGAttributes.SVG_STROKE, CSSColors.INSTANCE.getColorName(isShadow ? shape.getShadowCol() : shape.getLineColour(), true));
		circle.setStrokeWidth(1);
		marker.appendChild(circle);

		if(arrow.getArrowStyle() == ArrowStyle.CIRCLE_IN) {
			return lineWidth * (arrow.isLeftArrow() ? -1d : 1d);
		}
		return 0d;
	}


	private double toSVGDisk(final SVGDocument doc, final double lineWidth, final boolean isShadow, final SVGElement marker) {
		final IShape shape = arrow.getShape();
		final SVGElement circle = new SVGCircleElement(doc);
		final double r = (arrow.getDotSizeDim() + arrow.getDotSizeNum() * lineWidth) / 2d;

		circle.setAttribute(LNamespace.LATEXDRAW_NAMESPACE + ':' + LNamespace.XML_ARROW_DOT_SIZE_NUM, String.valueOf(arrow.getDotSizeNum()));
		circle.setAttribute(SVGAttributes.SVG_R, String.valueOf(r / lineWidth));
		circle.setAttribute(SVGAttributes.SVG_FILL, CSSColors.INSTANCE.getColorName(isShadow ? shape.getShadowCol() : shape.getLineColour(), true));
		marker.appendChild(circle);

		if(arrow.getArrowStyle() == ArrowStyle.DISK_IN) {
			return lineWidth * (arrow.isLeftArrow() ? -1d : 1d);
		}
		return 0d;
	}


	private void toSVGBar(final SVGDocument doc, final double lineWidth, final boolean isShadow, final SVGElement marker) {
		final IShape shape = arrow.getShape();
		final SVGPathElement bar = new SVGPathElement(doc);
		final double width = arrow.getTBarSizeDim() + arrow.getTBarSizeNum() * lineWidth;
		final SVGPathSegList path = new SVGPathSegList();
		final double x;

		if(arrow.getArrowStyle() == ArrowStyle.BAR_IN) {
			x = arrow.isLeftArrow() ? 0.5 : -0.5;
		}else {
			x = 0d;
		}

		bar.setAttribute(LNamespace.LATEXDRAW_NAMESPACE + ':' + LNamespace.XML_ARROW_TBAR_SIZE_NUM, String.valueOf(arrow.getTBarSizeNum()));
		path.add(new SVGPathSegMoveto(x, -width / (lineWidth * 2d), false));
		path.add(new SVGPathSegLineto(x, width / (lineWidth * 2d), false));
		bar.setPathData(path);
		bar.setAttribute(SVGAttributes.SVG_STROKE, CSSColors.INSTANCE.getColorName(isShadow ? shape.getShadowCol() : shape.getLineColour(), true));
		bar.setAttribute(SVGAttributes.SVG_FILL, SVGAttributes.SVG_VALUE_NONE);
		bar.setPathData(path);
		bar.setStrokeWidth(1d);
		marker.appendChild(bar);
	}


	private void toSVGSquareBracket(final SVGDocument doc, final double lineWidth, final boolean isShadow, final SVGElement marker) {
		final IShape shape = arrow.getShape();
		final SVGPathElement bar = new SVGPathElement(doc);
		final double width = arrow.getTBarSizeDim() + arrow.getTBarSizeNum() * lineWidth;
		final SVGPathSegList path = new SVGPathSegList();
		final double lgth = arrow.getBracketNum() * (arrow.getTBarSizeDim() / IShape.PPC + arrow.getTBarSizeNum() * lineWidth) / lineWidth;
		final boolean isInverted = arrow.isInverted(); //FIXME shape.PPC

		if(arrow.getArrowStyle() == ArrowStyle.LEFT_SQUARE_BRACKET) {
			final double lgth2 = isInverted ? -lgth : 0d;
			path.add(new SVGPathSegMoveto(lgth + lgth2 + 0.5, -width / (lineWidth * 2d) + 0.5, false));
			path.add(new SVGPathSegLineto(lgth2, -width / (lineWidth * 2) + 0.5, false));
			path.add(new SVGPathSegLineto(lgth2, width / (lineWidth * 2) - 0.5, false));
			path.add(new SVGPathSegLineto(lgth + lgth2 + 0.5, width / (lineWidth * 2d) - 0.5, false));
		}else {
			final double lgth2 = isInverted ? lgth : 0d;
			path.add(new SVGPathSegMoveto(-lgth + lgth2 - 0.5, -width / (lineWidth * 2d) + 0.5, false));
			path.add(new SVGPathSegLineto(lgth2, -width / (lineWidth * 2d) + 0.5, false));
			path.add(new SVGPathSegLineto(lgth2, width / (lineWidth * 2d) - 0.5, false));
			path.add(new SVGPathSegLineto(-lgth + lgth2 - 0.5, width / (lineWidth * 2d) - 0.5, false));
		}

		marker.appendChild(bar);
		bar.setAttribute(LNamespace.LATEXDRAW_NAMESPACE + ':' + LNamespace.XML_ARROW_TBAR_SIZE_NUM, String.valueOf(arrow.getTBarSizeNum()));
		bar.setAttribute(SVGAttributes.SVG_STROKE, CSSColors.INSTANCE.getColorName(isShadow ? shape.getShadowCol() : shape.getLineColour(), true));
		bar.setAttribute(SVGAttributes.SVG_FILL, SVGAttributes.SVG_VALUE_NONE);
		bar.setPathData(path);
		bar.setAttribute(SVGAttributes.SVG_STROKE_WIDTH, "1"); //$NON-NLS-1$
	}


	private void toSVGArrow(final SVGDocument doc, final double lineWidth, final boolean isShadow, final SVGElement marker) {
		final IShape shape = arrow.getShape();
		final SVGElement arrowSVG = new SVGPathElement(doc);
		final double width = (arrow.getArrowSizeNum() * lineWidth + arrow.getArrowSizeDim()) / lineWidth;
		double length = arrow.getArrowLength() * width;
		double inset = arrow.getArrowInset() * length;
		final SVGPathSegList path = new SVGPathSegList();

		if(arrow.getArrowStyle() == ArrowStyle.LEFT_ARROW) {
			length *= -1d;
			inset *= -1d;
		}

		final double lgth2 = arrow.isInverted() ? length : 0d;
		path.add(new SVGPathSegMoveto(lgth2, 0d, false));
		path.add(new SVGPathSegLineto(-length + lgth2, width / 2d, false));
		path.add(new SVGPathSegLineto(-length + inset + lgth2, 0d, false));
		path.add(new SVGPathSegLineto(-length + lgth2, -width / 2d, false));
		path.add(new SVGPathSegClosePath());

		marker.appendChild(arrowSVG);
		arrowSVG.setAttribute(LNamespace.LATEXDRAW_NAMESPACE + ':' + LNamespace.XML_ARROW_SIZE_NUM, String.valueOf(arrow.getArrowSizeNum()));
		arrowSVG.setAttribute(SVGAttributes.SVG_FILL, CSSColors.INSTANCE.getColorName(isShadow ? shape.getShadowCol() : shape.getLineColour(), true));
		arrowSVG.setAttribute(SVGAttributes.SVG_D, path.toString());
	}


	private void toSVGRoundBracket(final SVGDocument doc, final double lineWidth, final boolean isShadow, final SVGElement marker) {
		final IShape shape = arrow.getShape();
		final SVGPathElement rbracket = new SVGPathElement(doc);
		final double width = (arrow.getTBarSizeDim() + arrow.getTBarSizeNum() * lineWidth) / lineWidth;
		double lgth = arrow.getRBracketNum() * width;
		final SVGPathSegList path = new SVGPathSegList();
		double gap = 0.5;

		if(arrow.getArrowStyle() == ArrowStyle.LEFT_ROUND_BRACKET) {
			lgth *= -1d;
			gap *= -1d;
		}

		final double lgth2 = arrow.isInverted() ? lgth : 0d;
		path.add(new SVGPathSegMoveto(-lgth + lgth2 - gap, width / 2d, false));
		path.add(new SVGPathSegCurvetoCubic(-lgth + lgth2 - gap, -width / 2d, 0d, width / 2d, 0d, -width / 2d, false));

		marker.appendChild(rbracket);
		rbracket.setAttribute(SVGAttributes.SVG_STROKE, CSSColors.INSTANCE.getColorName(isShadow ? shape.getShadowCol() : shape.getLineColour(), true));
		rbracket.setAttribute(SVGAttributes.SVG_FILL, SVGAttributes.SVG_VALUE_NONE);
		rbracket.setAttribute(LNamespace.LATEXDRAW_NAMESPACE + ':' + LNamespace.XML_ARROW_TBAR_SIZE_NUM, String.valueOf(arrow.getTBarSizeNum()));
		rbracket.setPathData(path);
		rbracket.setStrokeWidth(1d);
	}


	private void toSVGDoubleArrow(final SVGDocument doc, final double lineWidth, final boolean isShadow, final SVGElement marker) {
		final IShape shape = arrow.getShape();
		final SVGElement arrowSVG = new SVGPathElement(doc);
		final double width = (arrow.getArrowSizeNum() * lineWidth + arrow.getArrowSizeDim()) / lineWidth;
		double length = arrow.getArrowLength() * width;
		double inset = arrow.getArrowInset() * length;
		final SVGPathSegList path = new SVGPathSegList();

		if(arrow.getArrowStyle() == ArrowStyle.LEFT_DBLE_ARROW) {
			inset *= -1d;
			length *= -1d;
		}

		final double lgth2 = arrow.isInverted() ? length * 2d : 0d;
		path.add(new SVGPathSegMoveto(lgth2, 0d, false));
		path.add(new SVGPathSegLineto(-length + lgth2, width / 2d, false));
		path.add(new SVGPathSegLineto(-length + inset + lgth2, 0d, false));
		path.add(new SVGPathSegLineto(-length + lgth2, -width / 2d, false));
		path.add(new SVGPathSegClosePath());
		path.add(new SVGPathSegMoveto(-length + lgth2, 0d, false));
		path.add(new SVGPathSegLineto(-length * 2d + lgth2, width / 2d, false));
		path.add(new SVGPathSegLineto(-length * 2d + inset + lgth2, 0d, false));
		path.add(new SVGPathSegLineto(-length * 2d + lgth2, -width / 2d, false));
		path.add(new SVGPathSegClosePath());

		marker.appendChild(arrowSVG);
		arrowSVG.setAttribute(LNamespace.LATEXDRAW_NAMESPACE + ':' + LNamespace.XML_ARROW_SIZE_NUM, String.valueOf(arrow.getArrowSizeNum()));
		arrowSVG.setAttribute(SVGAttributes.SVG_FILL, CSSColors.INSTANCE.getColorName(isShadow ? shape.getShadowCol() : shape.getLineColour(), true));
		arrowSVG.setAttribute(SVGAttributes.SVG_D, path.toString());
	}


	private void toSVGRoundIn(final SVGDocument doc, final boolean isShadow, final SVGElement marker) {
		final IShape shape = arrow.getShape();
		final SVGElement circle = new SVGCircleElement(doc);
		circle.setAttribute(SVGAttributes.SVG_R, "0.5"); //$NON-NLS-1$
		circle.setAttribute(SVGAttributes.SVG_FILL, CSSColors.INSTANCE.getColorName(isShadow ? shape.getShadowCol() : shape.getLineColour(), true));
		marker.appendChild(circle);
	}


	/**
	 * Return the SVG tree of the arrowhead or null if this arrowhead has no style.
	 * @param doc The document used to create elements.
	 * @param isShadow True: this operation is call to create the SVG shadow of the shape.
	 * @return The SVG tree of the arrowhead or null if doc is null.
	 */
	public SVGElement toSVG(final SVGDocument doc, final boolean isShadow) {
		if(doc == null || !arrow.hasStyle()) return null;

		final ArrowStyle arrowStyle = arrow.getArrowStyle();
		final SVGElement marker = new SVGMarkerElement(doc);
		final double lineWidth = arrow.getLineThickness();

		marker.setAttribute(SVGAttributes.SVG_OVERFLOW, SVGAttributes.SVG_VALUE_VISIBLE);
		marker.setAttribute(SVGAttributes.SVG_ORIENT, SVGAttributes.SVG_VALUE_AUTO);

		if(arrowStyle.isCircleDisk()) {
			final double gapPostion = arrowStyle == ArrowStyle.DISK_END || arrowStyle == ArrowStyle.DISK_IN ? toSVGDisk(doc, lineWidth, isShadow, marker) : 
				toSVGCircle(doc, lineWidth, isShadow, marker);
			marker.setAttribute(SVGAttributes.SVG_REF_X, String.valueOf(gapPostion / lineWidth));
		}

		if(arrowStyle.isBar()) {
			toSVGBar(doc, lineWidth, isShadow, marker);
		}

		if(arrowStyle.isSquareBracket()) {
			toSVGSquareBracket(doc, lineWidth, isShadow, marker);
		}

		if(arrowStyle == ArrowStyle.RIGHT_ARROW || arrowStyle == ArrowStyle.LEFT_ARROW) {
			toSVGArrow(doc, lineWidth, isShadow, marker);
		}

		if(arrowStyle.isRoundBracket()) {
			toSVGRoundBracket(doc, lineWidth, isShadow, marker);
		}

		if(arrowStyle == ArrowStyle.LEFT_DBLE_ARROW || arrowStyle == ArrowStyle.RIGHT_DBLE_ARROW) {
			toSVGDoubleArrow(doc, lineWidth, isShadow, marker);
		}

		if(arrowStyle == ArrowStyle.ROUND_IN) {
			toSVGRoundIn(doc, isShadow, marker);
		}

		return marker;
	}
}
