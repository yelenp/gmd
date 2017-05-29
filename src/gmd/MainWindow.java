package gmd;

import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class MainWindow extends JFrame {

	public MainWindow(String title, int width, int height) {
		super(title);
		setIconImage(new ImageIcon(Configuration.get("doctorIcon")).getImage());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(width, height));
		//JPanel mainPanel = new JPanel();
		//mainPanel.setLayout(new BorderLayout());
		setContentPane(new MainPanel());
		//add(new JScrollPane(mainPanel), BorderLayout.CENTER);
		pack();
		setVisible(true);
	}
	
	public static void main(String[] args) {
		new MainWindow("DoctorAdvisor 1.0", 800, 500);
	}
}
