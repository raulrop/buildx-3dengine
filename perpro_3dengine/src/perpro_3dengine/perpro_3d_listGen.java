package perpro_3dengine;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import perpro_3dengine.perpro_3d_triangle.Triangle;
import perpro_3dengine.perpro_3d_vertex.Vertex;

public class perpro_3d_listGen {
	public  List<Triangle> getList(){
		List<Triangle> tris = new ArrayList<>();
		tris.add(new Triangle(new Vertex(100, 100, 100),
				new Vertex(-100, -100, 100),
				new Vertex(-100, 100, -100),
				Color.WHITE));
		tris.add(new Triangle(new Vertex(100, 100, 100),
				new Vertex(-100, -100, 100),
				new Vertex(100, -100, -100),
				Color.RED));
		tris.add(new Triangle(new Vertex(-100, 100, -100),
				new Vertex(100, -100, -100),
				new Vertex(100, 100, 100),
				Color.GREEN));
		tris.add(new Triangle(new Vertex(-100, 100, -100),
				new Vertex(100, -100, -100),
				new Vertex(-100, -100, 100),
				Color.BLUE));
		return tris;
	}
}
