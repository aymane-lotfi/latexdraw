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
package net.sf.latexdraw.models.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import net.sf.latexdraw.actions.ExportFormat;
import net.sf.latexdraw.badaboom.BadaboomCollector;
import net.sf.latexdraw.models.MathUtils;
import net.sf.latexdraw.models.ShapeFactory;
import net.sf.latexdraw.models.interfaces.shape.IPicture;
import net.sf.latexdraw.models.interfaces.shape.IPoint;
import net.sf.latexdraw.models.interfaces.shape.IShape;
import net.sf.latexdraw.util.LPath;
import net.sf.latexdraw.view.pst.PSTricksConstants;
import org.sourceforge.jlibeps.epsgraphics.EpsGraphics2D;

/**
 * A model of a picture.
 * @author Arnaud Blouin
 */
class LPicture extends LPositionShape implements IPicture {
	/** The buffered image. */
	private Image image;
	/** The path of the eps image. */
	private String pathTarget;
	/** The path of the source image. */
	private String pathSource;


	/**
	 * Creates a picture and the corresponding EPS picture.
	 * @param pt The position of the top-left point of the picture.
	 * @throws IllegalArgumentException If the given picture path is not valid.
	 */
	LPicture(final IPoint pt) {
		super(pt);
	}


	/**
	 * Loads the image using the source path and creates the eps picture.
	 * @throws IOException If the picture cannot be loaded.
	 * @since 3.0
	 */
	private void loadImage() throws IOException {
		if(image != null) {
			new File(pathTarget).delete();
		}

		image = new Image(new File(pathSource).toURI().toString());
		createEPSImage();
	}


	@Override
	public void copy(final IShape sh) {
		super.copy(sh);

		if(sh instanceof IPicture) {
			try {
				setPathSource(((IPicture) sh).getPathSource());
			}catch(final IOException ex) {
				BadaboomCollector.INSTANCE.add(ex);
			}
		}
	}

	@Override
	public IPicture duplicate() {
		final IPicture pic = ShapeFactory.INST.createPicture(getPosition());
		pic.copy(this);
		return pic;
	}

	@Override
	public void mirrorVertical(final double y) {
		final IPoint gc = getGravityCentre();
		if(MathUtils.INST.isValidCoord(y) && !MathUtils.INST.equalsDouble(y, gc.getY())) {
			translate(0d, gc.verticalSymmetry(y).getY() - gc.getY());
		}
	}

	@Override
	public void mirrorHorizontal(final double x) {
		final IPoint gc = getGravityCentre();
		if(MathUtils.INST.isValidCoord(x) && !MathUtils.INST.equalsDouble(x, gc.getX())) {
			translate(gc.horizontalSymmetry(x).getX() - gc.getX(), 0d);
		}
	}


	/**
	 * Creates an EPS image from the source one.
	 * @throws IOException If a problem while reading/writing files occurs.
	 * @since 2.0.0
	 */
	private void createEPSImage() throws IOException {
		if(pathSource == null || image == null) return;

		final int indexName = pathSource.lastIndexOf(File.separator) + 1;
		final String name = pathSource.substring(indexName, pathSource.lastIndexOf('.')) + ExportFormat.EPS_LATEX.getFileExtension();
		final String dirPath = pathSource.substring(0, indexName);
		pathTarget = dirPath + name;
		File file = new File(pathTarget);
		boolean created;

		try {// We create the output file that will contains the eps picture.
			created = file.createNewFile();
		}catch(final IOException ex) { created = false; }

		// If created is false, it may mean that the file already exist.
		if(!created && !file.canWrite()) {
			pathTarget = LPath.PATH_CACHE_DIR + File.separator + name;
			file = new File(pathTarget);
		}

		// Within jlibeps, graphics are defined using 72 DPI (72/2.54=28,3465 PPC), but latexdraw uses 50 PPC.
		// That's why, we need the scale the graphics to have a 50 PPC eps picture.
		final double scale = 72.0 / PSTricksConstants.INCH_VAL_CM / IShape.PPC;// 72 DPI / 2.54 / 50 PPC
		try(FileOutputStream finalImage = new FileOutputStream(file)) {
			final EpsGraphics2D g = new EpsGraphics2D("LaTeXDrawPicture", finalImage, 0, 0, (int) (getWidth() * scale), (int) (getHeight() * scale));//$NON-NLS-1$
			g.scale(scale, scale);
			BufferedImage buff = SwingFXUtils.fromFXImage(image, null);
			g.drawImage(buff, 0, 0, null);
			g.flush();
			g.close();
			buff.flush();
		}
	}


	@Override
	public IPoint getPosition() {
		return getPtAt(0);
	}


	@Override
	public IPoint getTopRightPoint() {
		final IPoint pos = getPtAt(0);
		return ShapeFactory.INST.createPoint(pos.getX() + getWidth(), pos.getY());
	}


	@Override
	public IPoint getFullBottomRightPoint() {
		return getBottomRightPoint();
	}


	@Override
	public IPoint getFullTopLeftPoint() {
		return getTopLeftPoint();
	}


	@Override
	public IPoint getBottomRightPoint() {
		final IPoint pos = getPtAt(0);
		return ShapeFactory.INST.createPoint(pos.getX() + getWidth(), pos.getY() + getHeight());
	}


	@Override
	public IPoint getBottomLeftPoint() {
		final IPoint pos = getPtAt(0);
		return ShapeFactory.INST.createPoint(pos.getX(), pos.getY() + getHeight());
	}


	@Override
	public double getHeight() {
		return image == null ? 0.0 : image.getHeight();
	}


	@Override
	public Image getImage() {
		return image;
	}


	@Override
	public String getPathSource() {
		return pathSource;
	}


	@Override
	public String getPathTarget() {
		return pathTarget;
	}


	@Override
	public double getWidth() {
		return image == null ? 0d : image.getWidth();
	}


	@Override
	public void setPathSource(final String path) throws IOException {
		pathSource = path;
		image = null;
		if(pathSource != null) {
			loadImage();
		}
	}

	@Override
	public boolean isColourable() {
		return false;
	}

	@Override
	public boolean isFillable() {
		return false;
	}

	@Override
	public boolean isInteriorStylable() {
		return false;
	}

	@Override
	public boolean isShadowable() {
		return false;
	}

	@Override
	public boolean isThicknessable() {
		return false;
	}
}
