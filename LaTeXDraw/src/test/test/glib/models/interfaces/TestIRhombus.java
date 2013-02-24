package test.glib.models.interfaces;


import net.sf.latexdraw.glib.models.interfaces.IRhombus;

import org.junit.Test;

public abstract class TestIRhombus<T extends IRhombus> extends TestIPositionShape<T> {
	@Override
	@Test
	public void testGetBottomLeftPoint() {
		shape.setPosition(0, 5);
		shape.setWidth(10);
		shape.setHeight(10);

		assertNotNull(shape.getBottomLeftPoint());
		assertEquals(0., shape.getBottomLeftPoint().getX());
		assertEquals(5., shape.getBottomLeftPoint().getY());
	}


	@Override
	@Test
	public void testGetBottomRightPoint() {
		shape.setPosition(0, 5);
		shape.setWidth(10);
		shape.setHeight(10);

		assertNotNull(shape.getBottomRightPoint());
		assertEquals(10., shape.getBottomRightPoint().getX());
		assertEquals(5., shape.getBottomRightPoint().getY());
	}



	@Override
	@Test
	public void testGetTopLeftPoint() {
		shape.setPosition(0, 5);
		shape.setWidth(10);
		shape.setHeight(10);

		assertNotNull(shape.getTopLeftPoint());
		assertEquals(0., shape.getTopLeftPoint().getX());
		assertEquals(-5., shape.getTopLeftPoint().getY());
	}



	@Override
	@Test
	public void testGetTopRightPoint() {
		shape.setPosition(0, 5);
		shape.setWidth(10);
		shape.setHeight(10);

		assertNotNull(shape.getTopRightPoint());
		assertEquals(10., shape.getTopRightPoint().getX());
		assertEquals(-5., shape.getTopRightPoint().getY());
	}



	@Override
	@Test
	public void testMirrorHorizontal() {
		shape.setPosition(0, 5);
		shape.setWidth(10);
		shape.setHeight(10);
		shape.mirrorHorizontal(shape.getGravityCentre());

		assertEquals(10., shape.getPtAt(0).getX());
		assertEquals(0., shape.getPtAt(0).getY());
		assertEquals(5., shape.getPtAt(1).getX());
		assertEquals(-5., shape.getPtAt(1).getY());
		assertEquals(5., shape.getPtAt(3).getX());
		assertEquals(5., shape.getPtAt(3).getY());
		assertEquals(0., shape.getPtAt(2).getX());
		assertEquals(0., shape.getPtAt(2).getY());
	}



	@Override
	@Test
	public void testMirrorVertical() {
		shape.setPosition(0, 5);
		shape.setWidth(10);
		shape.setHeight(10);
		shape.mirrorVertical(shape.getGravityCentre());

		assertEquals(0., shape.getPtAt(0).getX());
		assertEquals(0., shape.getPtAt(0).getY());
		assertEquals(5., shape.getPtAt(1).getX());
		assertEquals(5., shape.getPtAt(1).getY());
		assertEquals(5., shape.getPtAt(3).getX());
		assertEquals(-5., shape.getPtAt(3).getY());
		assertEquals(10., shape.getPtAt(2).getX());
		assertEquals(0., shape.getPtAt(2).getY());
	}


//
//	@Override
//	@Test
//	public void testScale() {
//		shape.setPosition(0, 2);
//		shape.setRight(2);
//		shape.setTop(0);
//
//		IPoint tl1 = shape.getTopLeftPoint();
//		IPoint br1 = shape.getBottomRightPoint();
//
//		shape.scale(1.5, 1, Position.EAST);
//		IPoint tl2 = shape.getTopLeftPoint();
//		IPoint br2 = shape.getBottomRightPoint();
//
//		assertEquals((br1.getX()-tl1.getX())*1.5, br2.getX()-tl2.getX());
//		shape.scale(1, 1.5, Position.SOUTH);
//		tl2 = shape.getTopLeftPoint();
//		br2 = shape.getBottomRightPoint();
//		assertEquals((br1.getY()-tl1.getY())*1.5, br2.getY()-tl2.getY());
//
//		tl1 = shape.getTopLeftPoint();
//		br1 = shape.getBottomRightPoint();
//		shape.scale(1.5, 1, Position.WEST);
//		tl2 = shape.getTopLeftPoint();
//		br2 = shape.getBottomRightPoint();
//		assertEquals((br1.getX()-tl1.getX())*1.5, br2.getX()-tl2.getX());
//
//		tl1 = shape.getTopLeftPoint();
//		br1 = shape.getBottomRightPoint();
//		shape.scale(1, 1.5, Position.NORTH);
//		tl2 = shape.getTopLeftPoint();
//		br2 = shape.getBottomRightPoint();
//		assertEquals((br1.getY()-tl1.getY())*1.5, br2.getY()-tl2.getY());
//	}

	
	@Override
	@Test
	public void testTranslate() {
		shape.setPosition(0, 0);
		shape.setWidth(20);
		shape.setHeight(10);
		shape.translate(100, 50);

		assertEquals(100., shape.getPtAt(0).getX());
		assertEquals(45., shape.getPtAt(0).getY());
		assertEquals(110., shape.getPtAt(1).getX());
		assertEquals(40., shape.getPtAt(1).getY());
		assertEquals(120., shape.getPtAt(2).getX());
		assertEquals(45., shape.getPtAt(2).getY());
		assertEquals(110., shape.getPtAt(3).getX());
		assertEquals(50., shape.getPtAt(3).getY());
	}
}