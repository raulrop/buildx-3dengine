package perpro_3dengine;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

import perpro_3dengine.perpro_3d_matrix.Matrix3;
import perpro_3dengine.perpro_3d_triangle.Triangle;
import perpro_3dengine.perpro_3d_vertex.Vertex;

public class perpro_3d_startup {
	public  static void  main(String[] args) {
		perpro_3d_listGen lis = new perpro_3d_listGen();
		
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
				for (Triangle t : lis.getList() ) {
					// Call Vertex in order to use injected method transform inside Matrix3
					Vertex v1 = transform.transform(t.v1);
					Vertex v2 = transform.transform(t.v2);
					Vertex v3 = transform.transform(t.v3);
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
						    		img.setRGB(x, y, t.color.getRGB());
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
		// Add listeners to heading and pitch such that dragging forces a redraw
		headingSlider.addChangeListener(e -> renderPanel.repaint());
		pitchSlider.addChangeListener(e -> renderPanel.repaint());
		pane.add(renderPanel, BorderLayout.CENTER);
		
		frame.setSize(400,400);
		frame.setVisible(true);
		
		}
}
