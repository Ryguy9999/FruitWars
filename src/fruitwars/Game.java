package fruitwars;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import entity.Entity;
import entity.Grape;
import entity.Unit;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import ui.UIComponent;
import ui.UnitSelectionBar;

public class Game extends Scene
{
	private GraphicsContext g;
	private ArrayList<Entity> entities;
	private ArrayList<Unit> selectedUnits;
	private ArrayList<UIComponent> gui;
	private boolean selecting, controlHeld;
	private Color selectionBlue = new Color(102.0 / 255, 153.0 / 255, 1, 64 / 255.0);
	private Point2D selectionCorner, mousePosition;

	public Game(Group root, GraphicsContext ctx)
	{
		super(root);
		g = ctx;
		
		entities = new ArrayList<>();
		selectedUnits = new ArrayList<>();

		selecting = false;
		selectionCorner = new Point2D.Double(0, 0);
		for (int i = 0; i < 15; i++)
			entities.add(new Grape(new Point2D.Double(200 + i, 200 + i)));
		mousePosition = new Point2D.Double();
		
		gui = new ArrayList<>();
		gui.add(new UnitSelectionBar(selectedUnits));

		addEventHandler(MouseEvent.ANY, this::mouseMove);
		addEventHandler(MouseEvent.MOUSE_PRESSED, this::mousePressed);
		addEventHandler(MouseEvent.MOUSE_RELEASED, this::mouseReleased);
		addEventHandler(KeyEvent.KEY_PRESSED, this::keyPressed);
		addEventHandler(KeyEvent.KEY_RELEASED, this::keyReleased);
	}

	public void tick(long milli)
	{
		for (int i = 0; i < entities.size(); i++)
		{
			entities.get(i).tick(milli);
			for (int j = i; j < entities.size(); j++)
				entities.get(i).separate(entities.get(j));
		}
	}

	public void draw(long milli)
	{
		g.clearRect(0, 0, FruitWars.WINDOW_WIDTH, FruitWars.WINDOW_HEIGHT);
		for (Entity e : entities)
			e.draw(g, milli);
		for (UIComponent u : gui)
			u.draw(g, milli);
		if (selecting && !mousePosition.equals(selectionCorner))
		{
			g.setStroke(Color.BLUE);
			Rectangle2D selectionRect = getSelectionRect();
			g.strokeRect(selectionRect.getX(), selectionRect.getY(), selectionRect.getWidth(),
					selectionRect.getHeight());
			g.stroke();
			g.setFill(selectionBlue);
			g.fillRect(selectionRect.getX(), selectionRect.getY(), selectionRect.getWidth(), selectionRect.getHeight());
			g.fill();
		}
	}

	public void mouseMove(MouseEvent e)
	{
		mousePosition.setLocation(e.getX(), e.getY());
	}

	public void keyPressed(KeyEvent e)
	{
		if (e.getCode() == KeyCode.CONTROL)
		{
			controlHeld = true;
		}
	}

	public void keyReleased(KeyEvent e)
	{
		switch (e.getCode())
		{
		case SPACE:
			clearSelected(); // Unselect units
			break;
		case C:
			selectedUnits.forEach(unit -> unit.setDestination(unit.location)); // Stop
			// moving
			break;
		case CONTROL:
			controlHeld = false;
			break;
		default:
			break;
		}
	}

	public void mousePressed(MouseEvent e)
	{
		boolean handled = false;
		for (UIComponent u : gui)
		{
			if (u.getBounds().contains(mousePosition))
			{
				handled = u.handlePressed(e);
				if (handled)
					break;
			}
		}
		if (!handled)
		{
			if (e.getButton() == MouseButton.PRIMARY)
			{
				selecting = true;
				selectionCorner.setLocation(mousePosition);
			}
		}
	}

	public void mouseReleased(MouseEvent e)
	{
		boolean handled = false;
		for (UIComponent u : gui)
		{
			if (u.getBounds().contains(mousePosition))
			{
				handled = u.handleReleased(e);
				if (handled)
					break;
			}
		}
		if (!handled)
		{
			if (e.getButton() == MouseButton.PRIMARY)
			{
				Rectangle2D selectionRect = getSelectionRect();
				if (!controlHeld)
					clearSelected();
				entities.stream().filter(ent -> ent instanceof Unit).map(ent -> (Unit) ent)
						.filter(unit -> selectionRect.contains(unit.location)).forEach(unit -> {
							unit.setSelected(true);
							selectedUnits.add(unit);
						});
				selecting = false;
			} else
			{
				Point2D destination = new Point2D.Double(e.getX(), e.getY());
				selectedUnits.forEach(unit -> unit.setDestination(destination));
				selecting = false;
			}
		}
	}

	private void clearSelected()
	{
		selectedUnits.forEach(unit -> unit.setSelected(false));
		selectedUnits.clear();
	}

	private Rectangle2D getSelectionRect()
	{
		double x = Math.min(selectionCorner.getX(), mousePosition.getX());
		double y = Math.min(selectionCorner.getY(), mousePosition.getY());
		double width = Math.max(selectionCorner.getX(), mousePosition.getX()) - x;
		double height = Math.max(selectionCorner.getY(), mousePosition.getY()) - y;
		return new Rectangle2D.Double(x, y, width, height);
	}
}