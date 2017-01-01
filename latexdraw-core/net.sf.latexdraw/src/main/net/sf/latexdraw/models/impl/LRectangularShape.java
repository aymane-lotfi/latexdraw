/*
 * This file is part of LaTeXDraw
 * Copyright (c) 2005-2017 Arnaud BLOUIN
 * LaTeXDraw is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * LaTeXDraw is distributed without any warranty; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 */
package net.sf.latexdraw.models.impl;

import net.sf.latexdraw.models.GLibUtilities;
import net.sf.latexdraw.models.ShapeFactory;
import net.sf.latexdraw.models.interfaces.shape.IPoint;
import net.sf.latexdraw.models.interfaces.shape.IRectangularShape;

/**
 * A model of a rectangular shape.
 */
abstract class LRectangularShape extends LPositionShape implements IRectangularShape {
	LRectangularShape(final IPoint tl, final IPoint br) {
		super(tl);

		if(!(GLibUtilities.isValidPoint(tl) && GLibUtilities.isValidPoint(br) && tl.getX() < br.getX() && tl.getY() < br.getY()))
			throw new IllegalArgumentException();

		points.add(ShapeFactory.createPoint(br.getX(), tl.getY()));
		points.add(ShapeFactory.createPoint(br));
		points.add(ShapeFactory.createPoint(tl.getX(), br.getY()));
	}


	@Override
	public void mirrorHorizontal(final IPoint origin) {
		super.mirrorHorizontal(origin);

		if(getWidth() < 0) {
			final IPoint tmp = ShapeFactory.createPoint(points.get(0));
			points.get(0).setPoint(points.get(1));
			points.get(1).setPoint(tmp);
			tmp.setPoint(points.get(2));
			points.get(2).setPoint(points.get(3));
			points.get(3).setPoint(tmp);
		}
	}

	@Override
	public void mirrorVertical(final IPoint origin) {
		super.mirrorVertical(origin);
		if(getHeight() < 0) {
			final IPoint tmp = ShapeFactory.createPoint(points.get(0));
			points.get(0).setPoint(points.get(3));
			points.get(3).setPoint(tmp);
			tmp.setPoint(points.get(1));
			points.get(1).setPoint(points.get(2));
			points.get(2).setPoint(tmp);
		}
	}

	@Override
	public double getHeight() {
		return points.get(2).getY() - points.get(0).getY();
	}

	@Override
	public double getWidth() {
		return points.get(1).getX() - points.get(0).getX();
	}

	@Override
	public void setWidth(final double width) {
		if(GLibUtilities.isValidCoordinate(width) && width > 0) {
			final double xPos = points.get(points.size() - 1).getX() + width;
			points.get(1).setX(xPos);
			points.get(2).setX(xPos);
		}
	}

	@Override
	public void setHeight(final double height) {
		if(GLibUtilities.isValidCoordinate(height) && height > 0) {
			final double yPos = points.get(points.size() - 1).getY() - height;
			points.get(0).setY(yPos);
			points.get(1).setY(yPos);
		}
	}

	@Override
	public boolean isBordersMovable() {
		return true;
	}

	@Override
	public boolean isDbleBorderable() {
		return true;
	}

	@Override
	public boolean isFillable() {
		return true;
	}

	@Override
	public boolean isInteriorStylable() {
		return true;
	}

	@Override
	public boolean isLineStylable() {
		return true;
	}

	@Override
	public boolean isShadowable() {
		return true;
	}

	@Override
	public boolean isThicknessable() {
		return true;
	}
}
