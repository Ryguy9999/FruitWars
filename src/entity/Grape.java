package entity;

import java.awt.Image;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class Grape extends Unit
{
	private static final double RADIUS = 16, RANGE = 5;
	private static final long COOLDOWN = 1000;
	private long coolDown;
	public final static double MAX_HEALTH = 50;
	public static ArrayList<String> grapeLastNames = loadGrapeVarieties();
	private String name;
	private static Image[][] sprite;
	private double damage = 5.0;
	
	public Grape(Point2D location, Point2D rallyPoint, boolean friendly)
	{
		super(loadSprite(), location, rallyPoint, RADIUS, 1, MAX_HEALTH, friendly);
		mass = 0.1f;
		name = "Pvt. " + getName() + " " + grapeLastNames.get((int)(Math.random() * grapeLastNames.size()));
		coolDown = 0;
	}

	private static Image[][] loadSprite()
	{
		// temp
		if(sprite == null)
		{
			sprite = new Image[1][1];
			try
			{
				System.out.println("Loading Grape Sprite...");
				sprite[0][0] = ImageIO.read(new File("assets/tempGrape.png"));
			} catch (IOException e)
			{
				System.err.println("Could not find grape sprite asset");
				e.printStackTrace();
			}
		}
		return sprite;
	}
	
	@Override
	public void tick(long millis, ArrayList<Entity> entities)
	{
		coolDown -= millis;
		if(coolDown <= 0)
		{
			for(Entity e : entities)
			{
				double radiusSum = radius + RANGE + e.radius;
				if (!(e.isFriendly() == isFriendly()) && location.distanceSq(e.location) <= radiusSum * radiusSum)
				{
					attack(e);
					coolDown = COOLDOWN;
				}
			}
		}
		super.tick(millis, entities);
	}
	
	/**
	 * Source: National Grape Registry
	 */
	private static ArrayList<String> loadGrapeVarieties()
	{
		System.out.println("Loading types of grapes...");
		ArrayList<String> grapes = new ArrayList<>();
		try(Scanner fileScan = new Scanner(new File("assets/grapeNames.txt")))
		{
			while(fileScan.hasNextLine())
				grapes.add(fileScan.nextLine());
		} catch (FileNotFoundException e)
		{
			System.err.println("Could not load a ridiculously long list of grape varieties ¯\\_(ツ)_/¯");
			e.printStackTrace();
		}
		return grapes;
	}
	
	public String toString()
	{
		String status = name + " (GRAPE)";
		while(status.length() < 50)
			status = status + ".";
		return status + "HP: " + health + "/" + MAX_HEALTH;
	}

	@Override
	public void attack(Entity enemy)
	{
		if(!(enemy.isFriendly() == isFriendly()))
		{
			//if within range
			double radiusSum = radius + RANGE + enemy.radius;
			if (location.distanceSq(enemy.location) <= radiusSum * radiusSum)
			{
				enemy.setHealth(enemy.getHealth() - damage);
			}
			else
				super.setDestination(enemy.location);
		}
	}
}
