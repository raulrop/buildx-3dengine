package perpro_3dengine;

import perpro_3dengine.perpro_3d_vertex.Vertex;

public class perpro_3d_matrix {
	// This matrix will handle matrix-matrix and vector-matrix multiplication
	static class Matrix3 {
		// Instance variable to use (Matrix)
		double[] values;
		Matrix3(double[] values) {
			this.values = values;
		}
		// Create the  multiply method
		Matrix3 multiply(Matrix3 other) {
			double[] result = new double[9];
			// Three iterations are needed for a matrix multiplication
			// (Row->Column->RowsInColumn)
			for (int row = 0; row <  3; row++) {
				for (int col = 0; col < 3; col++) {
					for (int i = 0; i < 3; i++) {
						result[row*3 + col] +=
							this.values[row*3 + i]*other.values[i*3 + col];
					}
				}	
			}
			return new Matrix3(result);
		}
		// Call given vertex to multiply it by set Matrix
		Vertex transform(Vertex in) {
			return new Vertex(
				in.x*values[0] + in.y*values[3] + in.z*values[6],
				in.x * values[1] + in.y * values[4] + in.z * values[7],
	            in.x * values[2] + in.y * values[5] + in.z * values[8]
			);
		}
	}
}
