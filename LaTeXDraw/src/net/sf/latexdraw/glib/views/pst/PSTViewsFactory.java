package net.sf.latexdraw.glib.views.pst;

import net.sf.latexdraw.glib.models.interfaces.IArc;
import net.sf.latexdraw.glib.models.interfaces.IAxes;
import net.sf.latexdraw.glib.models.interfaces.IBezierCurve;
import net.sf.latexdraw.glib.models.interfaces.ICircle;
import net.sf.latexdraw.glib.models.interfaces.IDot;
import net.sf.latexdraw.glib.models.interfaces.IEllipse;
import net.sf.latexdraw.glib.models.interfaces.IFreehand;
import net.sf.latexdraw.glib.models.interfaces.IGrid;
import net.sf.latexdraw.glib.models.interfaces.IGroup;
import net.sf.latexdraw.glib.models.interfaces.IPicture;
import net.sf.latexdraw.glib.models.interfaces.IPolygon;
import net.sf.latexdraw.glib.models.interfaces.IPolyline;
import net.sf.latexdraw.glib.models.interfaces.IRectangle;
import net.sf.latexdraw.glib.models.interfaces.IRhombus;
import net.sf.latexdraw.glib.models.interfaces.IShape;
import net.sf.latexdraw.glib.models.interfaces.IText;
import net.sf.latexdraw.glib.models.interfaces.ITriangle;
import net.sf.latexdraw.glib.views.CreateViewCmd;

/**
 * Defines a generator that generates PSTricks views from given models.<br>
 *<br>
 * This file is part of LaTeXDraw<br>
 * Copyright (c) 2005-2012 Arnaud BLOUIN<br>
 *<br>
 *  LaTeXDraw is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.<br>
 *<br>
 *  LaTeXDraw is distributed without any warranty; without even the
 *  implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 *  PURPOSE. See the GNU General Public License for more details.<br>
 *<br>
 * 04/15/08<br>
 * @author Arnaud BLOUIN
 * @since 3.0
 * @version 3.0
 */
public final class PSTViewsFactory {
	/** The singleton. */
	public static final PSTViewsFactory INSTANCE = new PSTViewsFactory();

	/** The chain of responsibility used to reduce the complexity of the factory. */
	private CreateViewPSTCmd createCmd;

	private PSTViewsFactory() {
		super();
		initCommands();
	}

	/**
	 * Creates a view from a shape.
	 * @param shape The shape used to create the view.
	 * @return The created view or null.
	 * @since 3.0
	 */
	public PSTShapeView<?> createView(final IShape shape) {
		return shape==null ? null : createCmd.execute(shape);
	}


	/**
	 * Initialises the chain of responsibility.
	 */
	private void initCommands() {
		CreateViewPSTCmd cmd = new CreateViewPSTCmd(null, IPicture.class) { @Override public PSTShapeView<?> create(final IShape shape) { return new PSTPictureView((IPicture)shape); } };
		cmd = new CreateViewPSTCmd(cmd, IFreehand.class) 	{ @Override public PSTShapeView<?> create(final IShape shape) { return new PSTFreeHandView((IFreehand)shape); } };
		cmd = new CreateViewPSTCmd(cmd, IDot.class) 		{ @Override public PSTShapeView<?> create(final IShape shape) { return new PSTDotView((IDot)shape); } };
		cmd = new CreateViewPSTCmd(cmd, IGrid.class)		{ @Override public PSTShapeView<?> create(final IShape shape) { return new PSTGridView((IGrid)shape); } };
		cmd = new CreateViewPSTCmd(cmd, IAxes.class)		{ @Override public PSTShapeView<?> create(final IShape shape) { return new PSTAxesView((IAxes)shape); } };
		cmd = new CreateViewPSTCmd(cmd, IBezierCurve.class){ @Override public PSTShapeView<?> create(final IShape shape) { return new PSTBezierCurveView((IBezierCurve)shape); } };
		cmd = new CreateViewPSTCmd(cmd, IPolygon.class) 	{ @Override public PSTShapeView<?> create(final IShape shape) { return new PSTPolygonView((IPolygon)shape); } };
		// All the commands of the chain of responsibility are chained together.
		cmd = new CreateViewPSTCmd(cmd, IPolyline.class) 	{ @Override public PSTShapeView<?> create(final IShape shape) { return new PSTLinesView((IPolyline)shape); } };
		cmd = new CreateViewPSTCmd(cmd, IRhombus.class) 	{ @Override public PSTShapeView<?> create(final IShape shape) { return new PSTRhombusView((IRhombus)shape); } };
		cmd = new CreateViewPSTCmd(cmd, ITriangle.class) 	{ @Override public PSTShapeView<?> create(final IShape shape) { return new PSTTriangleView((ITriangle)shape); } };
		cmd = new CreateViewPSTCmd(cmd, IGroup.class) 		{ @Override public PSTShapeView<?> create(final IShape shape) { return new PSTGroupView((IGroup)shape); } };
		cmd = new CreateViewPSTCmd(cmd, IEllipse.class) 	{ @Override public PSTShapeView<?> create(final IShape shape) { return new PSTEllipseView((IEllipse)shape); } };
		cmd = new CreateViewPSTCmd(cmd, IArc.class) 		{ @Override public PSTShapeView<?> create(final IShape shape) { return new PSTArcView((IArc)shape); } };
		cmd = new CreateViewPSTCmd(cmd, ICircle.class) 	{ @Override public PSTShapeView<?> create(final IShape shape) { return new PSTCircleView((ICircle)shape); } };
		cmd = new CreateViewPSTCmd(cmd, IText.class) 		{ @Override public PSTShapeView<?> create(final IShape shape) { return new PSTTextView((IText)shape); } };
		// The last created command is the first element of the chain.
		createCmd = new CreateViewPSTCmd(cmd, IRectangle.class) 	{ @Override public PSTShapeView<?> create(final IShape shape) { return new PSTRectView((IRectangle)shape); } };
	}


	/**
	 * This class is a mix of the design patterns Command and Chain of responsibility.
	 * The goal is to find the command which can create the PST view of the given shape.
	 */
	private abstract class CreateViewPSTCmd extends CreateViewCmd<IShape, PSTShapeView<?>, CreateViewPSTCmd> {
		/**
		 * Creates the command.
		 * @param next The next command in the chain of responsibility. Can be null.
		 * @param classShape The type of the shape supported by the command.
		 * @since 3.0
		 */
		public CreateViewPSTCmd(final CreateViewPSTCmd next, final Class<? extends IShape> classShape) {
			super(next, classShape);
		}
	}
}
