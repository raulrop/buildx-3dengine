package perpro_3dengine;

import java.awt.Color;

import perpro_3dengine.perpro_3d_vertex.Vertex;

public class perpro_3d_triangle {
	static class Triangle {
		Vertex v1;
		Vertex v2; 
		Vertex v3;
		Color color;
		Triangle(Vertex v1, Vertex v2, Vertex v3, Color color) {
			this.v1 = v1;
			this.v2 = v2;
			this.v3 = v3;
			this.color = color;
		}
	}
}
