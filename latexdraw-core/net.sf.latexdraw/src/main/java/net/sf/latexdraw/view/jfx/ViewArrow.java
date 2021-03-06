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
package net.sf.latexdraw.view.jfx;

import java.util.Objects;
import javafx.beans.binding.Bindings;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Rotate;
import net.sf.latexdraw.models.MathUtils;
import net.sf.latexdraw.models.interfaces.shape.ArrowStyle;
import net.sf.latexdraw.models.interfaces.shape.IArrow;
import net.sf.latexdraw.models.interfaces.shape.ILine;
import net.sf.latexdraw.models.interfaces.shape.IPoint;

/**
 * The JFX view of an arrow.
 * @author Arnaud Blouin
 */
public class ViewArrow extends Group {
	public static final String ID = "arrow";

	final IArrow arrow;
	final Path path;
	final Ellipse ellipse;
	final Arc arc;

	/**
	 * Creates the view.
	 * @param model The arrow. Cannot be null.
	 * @throws NullPointerException if the given arrow is null.
	 */
	ViewArrow(final IArrow model) {
		super();
		setId(ID);
		arrow = Objects.requireNonNull(model);
		path = new Path();
		ellipse = new Ellipse();
		arc = new Arc();
		getChildren().add(path);
		getChildren().add(ellipse);
		getChildren().add(arc);
		enableShape(false, false, false);
		arc.strokeProperty().bind(Bindings.createObjectBinding(() -> arrow.getShape().getLineColour().toJFX(), arrow.getShape().lineColourProperty()));
		arc.strokeWidthProperty().bind(arrow.getShape().thicknessProperty());
		ellipse.strokeProperty().bind(Bindings.createObjectBinding(() -> arrow.getShape().getLineColour().toJFX(), arrow.getShape().lineColourProperty()));
		path.strokeProperty().bind(Bindings.createObjectBinding(() -> arrow.getShape().getLineColour().toJFX(), arrow.getShape().lineColourProperty()));
	}


	/**
	 * Method to enable some of the JFX shapes that represent arrows.
	 */
	private void enableShape(final boolean enablePath, final boolean enableArc, final boolean enableEll) {
		path.setVisible(enablePath);
		path.setDisable(!enablePath);
		ellipse.setVisible(enableEll);
		ellipse.setDisable(!enableEll);
		arc.setVisible(enableArc);
		arc.setDisable(!enableArc);
	}


	private void updatePathDiskCircleEnd(final double x, final double y) {
		final double lineWidth = arrow.getShape().getFullThickness();
		final double arrowRadius = arrow.getRoundShapedArrowRadius();
		ellipse.setCenterX(x);
		ellipse.setCenterY(y);
		ellipse.setRadiusX(arrowRadius - lineWidth / 2d);
		ellipse.setRadiusY(arrowRadius - lineWidth / 2d);
		ellipse.setStrokeWidth(lineWidth);
		setStrokeFillDiskCircle();
		enableShape(false, false, true);
	}


	private void updatePathDiskCircleIn(final IPoint pt1, final IPoint pt2) {
		final double arrowRadius = arrow.getRoundShapedArrowRadius();
		final double lineWidth = arrow.getShape().getFullThickness();
		ellipse.setCenterX(pt1.getX() + (isArrowInPositiveDirection(pt1, pt2) ? arrowRadius : -arrowRadius));
		ellipse.setCenterY(pt1.getY());
		ellipse.setRadiusX(arrowRadius - lineWidth / 2d);
		ellipse.setRadiusY(arrowRadius - lineWidth / 2d);
		setStrokeFillDiskCircle();
		enableShape(false, false, true);
	}


	private void setStrokeFillDiskCircle() {
		ellipse.fillProperty().unbind();

		if(arrow.getArrowStyle() == ArrowStyle.CIRCLE_IN || arrow.getArrowStyle() == ArrowStyle.CIRCLE_END) {
			if(arrow.getShape().isFillable()) {
				ellipse.fillProperty().bind(Bindings.createObjectBinding(() -> arrow.getShape().getFillingCol().toJFX(), arrow.getShape().fillingColProperty()));
			}else {
				ellipse.setFill(Color.WHITE);
			}
		}else {
			ellipse.fillProperty().bind(Bindings.createObjectBinding(() -> arrow.getShape().getLineColour().toJFX(), arrow.getShape().lineColourProperty()));
		}
	}


	private boolean shouldInvertArrow(final IPoint pt1, final IPoint pt2) {
		return arrow.isInverted() == isArrowInPositiveDirection(pt1, pt2);
	}


	private void updatePathRightLeftSquaredBracket(final IPoint pt1, final IPoint pt2) {
		final double[] xs = new double[2];
		final double[] ys = new double[2];
		final double lineWidth = arrow.getShape().getFullThickness();
		double lgth = arrow.getBracketShapedArrowLength() + arrow.getShape().getFullThickness() / 2d;

		if(shouldInvertArrow(pt1, pt2)) {
			lgth *= -1d;
		}

		updatePathBarIn(pt1, pt2, xs, ys);

		final double x3 = xs[0] + lgth;
		final double x4 = xs[1] + lgth;

		path.setStrokeLineCap(StrokeLineCap.BUTT);
		path.getElements().add(new MoveTo(xs[0], ys[0] + lineWidth / 2d));
		path.getElements().add(new LineTo(x3, ys[0] + lineWidth / 2d));
		path.getElements().add(new MoveTo(xs[1], ys[1] - lineWidth / 2d));
		path.getElements().add(new LineTo(x4, ys[1] - lineWidth / 2d));
		enableShape(true, false, false);
	}


	private void updatePathBarIn(final IPoint pt1, final IPoint pt2, final double[] xs, final double[] ys) {
		final double width = arrow.getBarShapedArrowWidth();
		final double lineWidth = arrow.getShape().getThickness();
		final double dec = isArrowInPositiveDirection(pt1, pt2) ? lineWidth / 2d : -lineWidth / 2d;
		final double x = pt1.getX();
		final double y = pt1.getY();
		xs[0] = x + dec;
		xs[1] = x + dec;
		ys[0] = y - width / 2d;
		ys[1] = y + width / 2d;

		path.getElements().add(new MoveTo(xs[0], ys[0]));
		path.getElements().add(new LineTo(xs[1], ys[1]));
		path.strokeWidthProperty().bind(arrow.getShape().thicknessProperty());
		enableShape(true, false, false);
	}


	private void updatePathBarEnd(final double x, final double y) {
		final double width = arrow.getBarShapedArrowWidth();

		path.getElements().add(new MoveTo(x, y - width / 2d));
		path.getElements().add(new LineTo(x, y + width / 2d));
		path.strokeWidthProperty().bind(arrow.getShape().thicknessProperty());
		enableShape(true, false, false);
	}


	private void updatePathArrow(final double x1, final double y1, final double x2, final double y2, final double x3, final double y3,
								 final double x4, final double y4) {
		path.getElements().add(new MoveTo(x1, y1));
		path.getElements().add(new LineTo(x2, y2));
		path.getElements().add(new LineTo(x3, y3));
		path.getElements().add(new LineTo(x4, y4));
		path.getElements().add(new ClosePath());
		enableShape(true, false, false);
	}


	private void updatePathRightLeftArrow(final IPoint pt1, final IPoint pt2) {
		final double width = arrow.getArrowShapedWidth();
		double length = arrow.getArrowLength() * width;
		double inset = arrow.getArrowInset() * length;
		double x = pt1.getX();
		final double y = pt1.getY();

		if(arrow.isInverted()) {
			x += isArrowInPositiveDirection(pt1, pt2) ? length : -length;
		}

		if(shouldInvertArrow(pt1, pt2)) {
			length *= -1d;
			inset *= -1d;
		}

		updatePathArrow(x, y, x + length, y - width / 2d, x + length - inset, y, x + length, y + width / 2d);
		path.fillProperty().bind(Bindings.createObjectBinding(() -> arrow.getShape().getLineColour().toJFX(), arrow.getShape().lineColourProperty()));
	}


	private boolean isArrowInPositiveDirection(final IPoint pt1, final IPoint pt2) {
		return pt1.getX() < pt2.getX() || (MathUtils.INST.equalsDouble(pt1.getX(), pt2.getX()) && pt1.getY() < pt2.getY());
	}


	private void updatePathRoundLeftRightBracket(final IPoint pt1, final IPoint pt2) {
		final boolean invert = arrow.isInverted();
		final double width = arrow.getBarShapedArrowWidth();
		final double lgth = arrow.getRBracketNum() * width;
		final double x = pt1.getX();
		final double y = pt1.getY();
		final double widtharc = lgth * 2d + (invert ? arrow.getShape().getThickness() / 2d : 0d);
		final double xarc = shouldInvertArrow(pt1, pt2) ? x - widtharc / 2d : x + widtharc / 2d;
		final double angle = shouldInvertArrow(pt1, pt2) ? -50d : 130d;

		arc.setCenterX(xarc);
		arc.setCenterY(y);
		arc.setRadiusX(widtharc / 2d);
		arc.setRadiusY(width / 2d);
		arc.setStartAngle(angle);
		arc.setLength(100d);
		arc.setFill(null);
		enableShape(false, true, false);
	}


	private void updatePathDoubleLeftRightArrow(final IPoint pt1, final IPoint pt2) {
		final double width = arrow.getArrowShapedWidth();
		double length = arrow.getArrowLength() * width;
		double inset = arrow.getArrowInset() * length;
		double x = pt1.getX();
		final double y = pt1.getY();

		if(arrow.isInverted() != arrow.isLeftArrow()) {
			x += isArrowInPositiveDirection(pt1, pt2) ? 2d * length : -2d * length;
		}

		if(shouldInvertArrow(pt1, pt2)) {
			length *= -1d;
			inset *= -1d;
		}

		updatePathArrow(x, y, x + length, y - width / 2d, x + length - inset, y, x + length, y + width / 2d);
		updatePathArrow(x + length, y, x + 2d * length, y - width / 2d, x + 2d * length - inset, y, x + 2d * length, y + width / 2d);
		final double x2 = x + length - inset;
		final double x2bis = x + 2d * length - inset;

		path.getElements().add(new LineTo(x2, y));
		path.getElements().add(new MoveTo(x2bis, y));
		path.fillProperty().bind(Bindings.createObjectBinding(() -> arrow.getShape().getLineColour().toJFX(), arrow.getShape().lineColourProperty()));
	}


	private void updatePathSquareRoundEnd(final IPoint pt1, final IPoint pt2) {
		final double x = pt1.getX();
		final double y = pt1.getY();
		path.getElements().add(new MoveTo(x, y));
		path.getElements().add(new LineTo(x < pt2.getX() ? x + 1d : x - 1d, y));
		enableShape(true, false, false);
	}


	private void updatePathRoundIn(final IPoint pt1, final IPoint pt2) {
		final double lineWidth = isArrowInPositiveDirection(pt1, pt2) ? arrow.getShape().getFullThickness() : -arrow.getShape().getFullThickness();
		final double x = pt1.getX() + lineWidth / 2d;
		final double y = pt1.getY();

		path.getElements().add(new MoveTo(x, y));
		path.getElements().add(new LineTo(x, y));
		enableShape(true, false, false);
	}


	public void updatePath() {
		path.getElements().clear();
		path.fillProperty().unbind();
		path.strokeWidthProperty().unbind();
		path.getTransforms().clear();
		ellipse.getTransforms().clear();
		arc.getTransforms().clear();

		final ILine arrowLine = arrow.getArrowLine();

		if(arrow.getArrowStyle() == ArrowStyle.NONE || arrowLine == null || !arrow.hasStyle()) {
			return;
		}

		final double lineAngle = arrowLine.getLineAngle();
		final IPoint pt1 = arrowLine.getPoint1();
		final IPoint pt2 = arrowLine.getPoint2();

		switch(arrow.getArrowStyle()) {
			case BAR_END:
				updatePathBarEnd(pt1.getX(), pt1.getY());
				break;
			case BAR_IN:
				updatePathBarIn(pt1, pt2, new double[2], new double[2]);
				break;
			case CIRCLE_END:
			case DISK_END:
				updatePathDiskCircleEnd(pt1.getX(), pt1.getY());
				break;
			case CIRCLE_IN:
			case DISK_IN:
				updatePathDiskCircleIn(pt1, pt2);
				break;
			case RIGHT_ARROW:
			case LEFT_ARROW:
				updatePathRightLeftArrow(pt1, pt2);
				break;
			case RIGHT_DBLE_ARROW:
			case LEFT_DBLE_ARROW:
				updatePathDoubleLeftRightArrow(pt1, pt2);
				break;
			case RIGHT_ROUND_BRACKET:
			case LEFT_ROUND_BRACKET:
				updatePathRoundLeftRightBracket(pt1, pt2);
				break;
			case LEFT_SQUARE_BRACKET:
			case RIGHT_SQUARE_BRACKET:
				updatePathRightLeftSquaredBracket(pt1, pt2);
				break;
			case SQUARE_END:
			case ROUND_END:
				updatePathSquareRoundEnd(pt1, pt2);
				break;
			case ROUND_IN:
				updatePathRoundIn(pt1, pt2);
				break;
			case NONE:
				break;
		}

		if(!MathUtils.INST.equalsDouble(lineAngle % (Math.PI * 2d), 0d)) {
			final Rotate rotate = new Rotate(Math.toDegrees(lineAngle), pt1.getX(), pt1.getY());
			path.getTransforms().add(rotate);
			ellipse.getTransforms().add(rotate);
			arc.getTransforms().add(rotate);
		}
	}


	public void flush() {
		path.strokeProperty().unbind();
		path.fillProperty().unbind();
		path.strokeWidthProperty().unbind();
		arc.strokeProperty().unbind();
		arc.strokeWidthProperty().unbind();
		ellipse.strokeProperty().unbind();
		ellipse.fillProperty().unbind();
		getChildren().clear();
	}
}
