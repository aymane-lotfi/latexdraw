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
package net.sf.latexdraw.actions.shape;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.sf.latexdraw.actions.DrawingActionImpl;
import net.sf.latexdraw.actions.Modifying;
import net.sf.latexdraw.actions.ShapesAction;
import net.sf.latexdraw.models.interfaces.shape.IShape;
import net.sf.latexdraw.util.LangTool;
import org.malai.undo.Undoable;

/**
 * This action removes shapes from a drawing.
 * @author Arnaud Blouin
 */
public class DeleteShapes extends DrawingActionImpl implements ShapesAction, Undoable, Modifying {
	/** The index of the deleted shapes into the original list. */
	List<Integer> positionShapes;

	/** The shapes to handle. */
	final List<IShape> shapes;


	public DeleteShapes() {
		super();
		shapes = new ArrayList<>();
	}

	@Override
	public RegistrationPolicy getRegistrationPolicy() {
		return RegistrationPolicy.LIMITED;
	}

	@Override
	protected void doActionBody() {
		final List<IShape> drawingSh = drawing.getShapes();
		positionShapes = shapes.stream().mapToInt(sh -> {
			int pos = drawingSh.indexOf(sh);
			drawing.removeShape(sh);
			return pos;
		}).boxed().collect(Collectors.toList());
		drawing.setModified(true);
	}

	@Override
	public boolean canDo() {
		return super.canDo() && !shapes.isEmpty();
	}

	@Override
	public void undo() {
		for(int i = positionShapes.size() - 1; i >= 0; i--) {
			drawing.addShape(shapes.get(i), positionShapes.get(i));
		}
		drawing.setModified(true);
	}

	@Override
	public void redo() {
		doActionBody();
	}

	@Override
	public String getUndoName() {
		return LangTool.INSTANCE.getBundle().getString("Actions.5");
	}

	@Override
	public List<IShape> getShapes() {
		return shapes;
	}
}
