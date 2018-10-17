package mycontroller;
import java.util.ArrayList;
import java.util.HashMap;
import tiles.MapTile;
import tiles.TrapTile;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;

public class StuckedWarning
{
	private Car car;
	
	private ArrayList<MapTile> temp =new ArrayList<MapTile>();
	private HashMap<Coordinate, MapTile> points = new HashMap<Coordinate, MapTile>();
	
	public StuckedWarning(Car car)
	{
		this.car=car;
	}

	public boolean stuckLocation()
	{		
		HashMap<Coordinate, MapTile> map=car.getView();
		Coordinate c = new Coordinate(car.getPosition());
		
		MapTile t1 = map.get(new Coordinate(c.x,c.y-1));
		MapTile t2 = map.get(new Coordinate(c.x,c.y+1));
		MapTile t3 = map.get(new Coordinate(c.x-1,c.y));
		MapTile t4 = map.get(new Coordinate(c.x+1,c.y));
		
		temp.add(t1);
		temp.add(t2);
		temp.add(t3);
		temp.add(t4);
		
		int count=0;
		
		for(int i=0; i<temp.size(); i++)
		{
			if(temp.get(i).isType(MapTile.Type.TRAP))
			{
				if(((TrapTile) temp.get(i)).getTrap() == "lava"||((TrapTile) temp.get(i)).getTrap() == "mud")
				{
					count++;
				}
			}
			else if(temp.get(i).isType(MapTile.Type.WALL))
			{
				count++;
				
			}
		}
		temp.clear();		
		if(count==3)
		{
			stuckPoints(c);
			return true;
		}
		return false;	
	}
	
	public HashMap<Coordinate, MapTile> stuckPoints(Coordinate c)
	{
		HashMap<Coordinate, MapTile> map=car.getView();
		points.put(c, map.get(c));
		return points;
	}	
}
