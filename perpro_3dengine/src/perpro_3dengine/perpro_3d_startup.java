package perpro_3dengine;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

public class perpro_3d {
	public  static void  main(String[] args) {
		JFrame frame = new JFrame();
		Container pane = frame.getContentPane();
		pane.setLayout(new BorderLayout());
		
		// Slider to control horizontal rotation
		JSlider headingSlider =  new JSlider(0,360,180);
		pane.add(headingSlider,BorderLayout.SOUTH);
		
		// Slider to control vertical rotation
		JSlider pitchSlider = new  JSlider(SwingConstants.VERTICAL,-90,90,0);
		pane.add(pitchSlider, BorderLayout.EAST);
		
		// Panel to display render results
		JPanel renderPanel = new JPanel() {
			public void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setColor(Color.BLACK);
				g2.fillRect(0, 0, getWidth(), getHeight());
				
				// Add rotation matrix to pipeline
				double heading = Math.toRadians(headingSlider.getValue());
				Matrix3 headingTransform = new Matrix3(new double[] {
				// XZ rotation matrix
					Math.cos(heading), 0, Math.sin(heading),
					0, 1, 0,
					-Math.sin(heading), 0, Math.cos(heading)
				});
				double pitch = Math.toRadians(pitchSlider.getValue());
				Matrix3 pitchTransform =  new Matrix3(new double[] {
				// XY rotation matrix
					1, 0, 0,
					0, Math.cos(pitch), Math.sin(pitch),
					0, -Math.sin(pitch), Math.cos(pitch)
				});
				// Since XZ and XY rotations are separate, a multiplication is needed
				Matrix3 transform = headingTransform.multiply(pitchTransform);
				// Call the BufferedImage class to set pixels and type of coloring
				BufferedImage img =
					new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_ARGB);
				
				double[] zBuffer = new double[img.getWidth() * img.getHeight()];
				// Initialize array with extremely far away depths
				for (int q = 0; q<zBuffer.length; q++) {
					zBuffer[q] = Double.NEGATIVE_INFINITY;
				}
				
				// For ortographic projection, draw  resulting triangle excluding Z-coordinate
				for (Triangle t : getList() ) {
					// Call Vertex in order to use injected method transform inside Matrix3
					Vertex v1 = transform.transform(t.v1);
					Vertex v2 = transform.transform(t.v2);
					Vertex v3 = transform.transform(t.v3);
					// Trasnsform vertices before calculating normal
					// This is done for adding shade into shape
					Vertex ab = new Vertex(
							v2.x - v1.x, v2.y - v1.y, v2.z - v1.z
							);
					Vertex ac = new Vertex(
							v3.x - v1.x, v3.y - v1.y, v3.z - v1.z
							);
					// Using AB and AC vectors, get the cross product
					Vertex norm = new Vertex(
							ab.y*ac.z - ab.z*ac.y,
							ab.z*ac.x - ab.x*ac.z,
							ab.x*ac.y - ab.y*ac.x
							);
					double normalLength =
							Math.sqrt(	(norm.x) * (norm.x) + 
										(norm.y) * (norm.y) + 
										(norm.z) * (norm.z)
										);
					// Normalize these vectors
					norm.x /= normalLength;
					norm.y /= normalLength;
					norm.z /= normalLength;
					// Calculate cosine between triangle normal and light direction
					// In reality, absolute shouldn't be used, but it's convenient here
					// This is applied to zBuffer along with getShade method
					double angleCos = Math.abs(norm.z);
					
					// Since Graphics2D is not used anymore, translation is done manually
					v1.x += getWidth() / 2;
					v1.y += getHeight() / 2;
				    v2.x += getWidth() / 2;
				    v2.y += getHeight() / 2;
				    v3.x += getWidth() / 2;
				    v3.y += getHeight() / 2;
				    
				    // Computer rectangular bounds for triangle
				    int minX = (int) Math.max(0, Math.ceil(Math.min(v1.x, Math.min(v2.x, v3.x))));
				    int maxX = (int) Math.min(img.getWidth() - 1, 
				    					Math.floor(Math.max(v1.x, Math.max(v2.x, v3.x))));
				    int minY = (int) Math.max(0, Math.ceil(Math.min(v1.y, Math.min(v2.y, v3.y))));
				    int maxY = (int) Math.min(img.getHeight() - 1,
				    					Math.floor(Math.max(v1.y, Math.max(v2.y, v3.y))));
				    
				    double triangleArea =
				    	(v1.y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - v1.x);

				    for (int y = minY; y <= maxY; y++) {
				    	// Handle rasterization...
				    	for (int x = minX; x <= maxX; x++) {
				    		double b1 = 
				    				((y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - x)) / triangleArea;
				    		double b2 =
				    				((y - v1.y) * (v3.x - v1.x) + (v3.y - v1.y) * (v1.x - x)) / triangleArea;
				    		double b3 =
				    				((y - v2.y) * (v1.x - v2.x) + (v1.y - v2.y) * (v2.x - x)) / triangleArea;
				    		if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
				    			double depth = b1*v1.z + b2*v2.z + b3*v3.z;
						    	int zIndex = y*img.getWidth() + x;
						    	if (zBuffer[zIndex] < depth) {
						    		img.setRGB(x, y, getShade(t.color, angleCos).getRGB());
						    		zBuffer[zIndex] = depth;	
						    	}
					    	}
				    	}
				    }
				}
				
				g2.drawImage(img,0,0,null);
				// Rendering happens here
				}
			};
		pane.add(renderPanel, BorderLayout.CENTER);
		// Add listeners to heading and pitch such that dragging forces a redraw
		headingSlider.addChangeListener(e -> renderPanel.repaint());
		pitchSlider.addChangeListener(e -> renderPanel.repaint());
		
		frame.setSize(400,400);
		frame.setVisible(true);
		
		}
		// Convert each color from scaled to linear format
		// That is, from sRGB to linear RGB 
		public static Color getShade(Color color, double shade) {
			double redLinear = Math.pow(color.getRed(),2.4) * shade;
			double greenLinear = Math.pow(color.getGreen(), 2.4) * shade;
			double blueLinear = Math.pow(color.getBlue(), 2.4) * shade;
			
			int red = (int) Math.pow(redLinear, 1/2.4);
			int green = (int) Math.pow(greenLinear, 1/2.4);
			int blue = (int) Math.pow(blueLinear, 1/2.4);
			
			return new Color(red, green, blue);
		}
		
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
		
		public static List<Triangle> getList(){
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
		
		static class Vertex {
			double x;
			double y;
			double z;
			Vertex(double x, double y, double z) {
				this.x = x;
				this.y = y;
				this.z = z;
			}
		}
		
}
