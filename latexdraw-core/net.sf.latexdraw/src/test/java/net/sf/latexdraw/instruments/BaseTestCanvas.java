package net.sf.latexdraw.instruments;

import java.util.concurrent.TimeoutException;
import javafx.application.Platform;
import javafx.scene.Group;
import net.sf.latexdraw.models.ShapeFactory;
import net.sf.latexdraw.models.interfaces.shape.IRectangle;
import net.sf.latexdraw.view.jfx.Canvas;
import net.sf.latexdraw.view.latex.DviPsColors;
import org.junit.After;
import org.junit.Before;
import org.malai.action.ActionsRegistry;

abstract class BaseTestCanvas extends TestLatexdrawGUI {
	static final long SLEEP = 400L;

	Pencil pencil;
	Hand hand;
	Canvas canvas;

	IRectangle addedRec;

	final GUIVoidCommand addRec = () -> Platform.runLater(() -> {
		addedRec = ShapeFactory.INST.createRectangle(ShapeFactory.INST.createPoint(-Canvas.ORIGIN.getX()+50, -Canvas.ORIGIN.getY()+50), 200, 100);
		addedRec.setFilled(true);
		addedRec.setFillingCol(DviPsColors.APRICOT);
		canvas.getDrawing().addShape(addedRec);
	});

	final GUIVoidCommand addRec2 = () -> Platform.runLater(() -> {
		IRectangle rec = ShapeFactory.INST.createRectangle(ShapeFactory.INST.createPoint(-Canvas.ORIGIN.getX()+300,
			-Canvas.ORIGIN.getY()+300), 100, 100);
		rec.setFilled(true);
		rec.setFillingCol(DviPsColors.APRICOT);
		canvas.getDrawing().addShape(rec);
	});

	@Override
	public String getFXMLPathFromLatexdraw() {
		return "/fxml/Canvas.fxml";
	}

	@Override
	@Before
	public void setUp() {
		super.setUp();
		pencil = (Pencil) injectorFactory.call(Pencil.class);
		hand = (Hand) injectorFactory.call(Hand.class);
		canvas = (Canvas) injectorFactory.call(Canvas.class);

		Platform.runLater(() -> {
			final int width = 800;
			final int height = 600;
			stage.minHeightProperty().unbind();
			stage.minWidthProperty().unbind();
			canvas.setMaxWidth(width);
			canvas.setMaxHeight(height);
			canvas.getScene().getWindow().setWidth(width);
			canvas.getScene().getWindow().setHeight(height);
			stage.setMaxWidth(width);
			stage.setMaxHeight(height);
			stage.setMinWidth(width);
			stage.setMinHeight(height);
			stage.centerOnScreen();
			stage.toFront();
		});
	}

	@Override
	@After
	public void tearDown() throws TimeoutException {
		super.tearDown();
		ActionsRegistry.INSTANCE.removeHandler(canvas);
	}

	Group getPane() {
		return (Group) canvas.getChildren().get(2);
	}
}
