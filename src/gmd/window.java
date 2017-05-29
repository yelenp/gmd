package gmd;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class window extends JFrame {
	public window(ArrayList<String> Disease){
		this.setTitle("Disease information");
		this.setSize(1600,900);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		Object rowData[][] = new Object[Disease.size() -2][1];
		String title[] = {"Disease"};
		for (int i = 2; i < Disease.size(); i++)
		{
			rowData[i-2][0] = Disease.get(i);
		}
		
		JTable tab = new JTable(rowData,title);
		JScrollPane scroll = new JScrollPane(tab, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		//tab.setPreferredSize(new Dimension(500,500));
		this.getContentPane().add(scroll);
		
		this.setVisible(true);
	}
}