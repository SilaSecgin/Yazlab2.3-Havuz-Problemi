
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import utilities.MaxFlowMinCut;

public class Frame {
	static JPanel ta = new JPanel();
	static JPanel panel = new JPanel();
	static JTextField matrixSizeTf, startTf, endTf;
	static Integer matrixSize = 0;
	static JTextField[][] MatrixArray = new JTextField[20][20];
	static JLabel maxFlowLabel, maxFlowValue, minCutLabel;
	static JLabel[] minCutValue = new JLabel[20];

	/* Frame elemanlarýnýn yaratýlmasý */
	public static void main(String[] args) {

		// Creating the Frame
		JFrame frame = new JFrame("Yazlab2-3");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(990, 990);

		// Creating the MenuBar and adding components
		JMenuBar mb = new JMenuBar();
		JMenu m1 = new JMenu("File");
		mb.add(m1);
		JMenuItem m22 = new JMenuItem("Exit");
		m1.add(m22);

		m22.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				System.exit(0);
			}
		});

		JLabel startLabel = new JLabel("Baslangic Dugumu");
		startTf = new JTextField(10); // accepts upto 10 characters
		JLabel endLabel = new JLabel("Cikis Dugumu");
		endTf = new JTextField(10); // accepts upto 10 characters
		JButton showGraph = new JButton("Hesapla ve Goster");
		showGraph.addActionListener(new ButtonListener2());

		panel.add(startLabel); // Components Added using Flow Layout
		panel.add(startTf);
		panel.add(endLabel); // Components Added using Flow Layout
		panel.add(endTf);
		panel.add(showGraph);

		// Adding Components to the frame.
		frame.getContentPane().add(BorderLayout.SOUTH, panel);
		frame.getContentPane().add(BorderLayout.NORTH, mb);
		frame.getContentPane().add(BorderLayout.CENTER, ta);
		frame.setVisible(true);

		// Get Matrix Size
		JLabel matrixLabel = new JLabel("Matrix Boyutu ");
		matrixLabel.setBounds(10, 10, 200, 40);
		matrixSizeTf = new JTextField(10);
		ta.add(matrixLabel);
		ta.add(matrixSizeTf);

		// Show Matrix
		JButton showMatrixBtn = new JButton();
		showMatrixBtn.setText("Matrix Yarat");
		showMatrixBtn.setBounds(10, 10, 200, 40);
		showMatrixBtn.addActionListener(new ButtonListener());
		ta.add(showMatrixBtn);
	}

	// Create Matrix
	static class ButtonListener implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			if (!matrixSizeTf.getText().equals("")) {
				ta.setLayout(null);
				// Remove all on panel
				ta.removeAll();

				maxFlowLabel = new JLabel("Max flow is : ");
				maxFlowLabel.setBounds(10, 10, 200, 40);
				maxFlowLabel.setVisible(false);
				maxFlowValue = new JLabel();
				maxFlowValue.setVisible(false);
				maxFlowValue.setBounds(100, 10, 200, 40);

				ta.add(maxFlowLabel);
				ta.add(maxFlowValue);

				minCutLabel = new JLabel("Min cut : ");
				minCutLabel.setBounds(10, 50, 200, 40);
				minCutLabel.setVisible(false);

				ta.add(minCutLabel);

				matrixSize = Integer.parseInt(matrixSizeTf.getText());

				// Create number on matrix y
				int yShowSpace = 0;
				for (int i = 0; i < matrixSize; i++) {
					JLabel yShow = new JLabel(String.valueOf(i + 1));
					yShow.setBounds(210 + yShowSpace, 180, 35, 20);
					ta.add(yShow);
					yShowSpace = yShowSpace + 50;
				}

				// Create matrix and create number on matrix x
				int ySpace = 0;
				int xSpace = 0;
				for (int i = 0; i < matrixSize; i++) {
					JLabel xShow = new JLabel(String.valueOf(i + 1));
					xShow.setBounds(180, 200 + ySpace, 35, 20);
					ta.add(xShow);
					for (int j = 0; j < matrixSize; j++) {
						MatrixArray[i][j] = new JTextField();
						MatrixArray[i][j].setText("");
						MatrixArray[i][j].setBounds(200 + xSpace, 200 + ySpace, 35, 20);
						ta.add(MatrixArray[i][j]);
						xSpace = xSpace + 50;
						if (i == j) {
							MatrixArray[i][j].setEnabled(false);
						}
					}
					xSpace = 0;
					ySpace = ySpace + 30;
				}

				// Update all on panel
				ta.updateUI();

			} else {
				JOptionPane.showMessageDialog(null, "Matrix Boyutu Bos Gecilemez!");
			}
		}

	}

	/* Matrise girilen baðlantý ve aðýrlýklarýn alýnmasý */
	static class ButtonListener2 implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			if (!startTf.getText().equals("") || !endTf.getText().equals("")) {
				write();
				NetworkDisruptor networkDisruptor = new NetworkDisruptor();
				networkDisruptor.start(Integer.parseInt(startTf.getText()), Integer.parseInt(endTf.getText()));

				maxFlowLabel.setVisible(true);
				maxFlowValue.setVisible(true);
				maxFlowValue.setText(String.valueOf(networkDisruptor.fordFulkersonAlgorithm.getMaximumFlow()));
				minCutLabel.setVisible(true);

				int ySpace = 0;

				for (int i = 0; i < 20; i++) {
					minCutValue[i] = new JLabel();
					minCutValue[i].setVisible(true);
					minCutValue[i].setText(networkDisruptor.minCutList2[i]);
					minCutValue[i].setBounds(80, 50 + ySpace, 200, 40);
					ta.add(minCutValue[i]);
					ySpace = ySpace + 30;
				}

				ta.updateUI();
			} else {
				JOptionPane.showMessageDialog(null, "Baslangic Dugumu veya Cikis Dugumu Bos Gecilemez!");
			}
		}
	}

	/* Matrise girilen baðlantý ve aðýrlýklarýn nodes.dgs ye yazýlmasý */
	public static void write() {
		try {
			File homedir = new File(System.getProperty("user.home"));
			File fileToRead = new File(homedir, "Desktop/170201087/Yazlab2-3/data/nodes.dgs");
			FileWriter writer = new FileWriter(fileToRead, false);
			BufferedWriter bufferedWriter = new BufferedWriter(writer);

			bufferedWriter.write("DGS003");
			bufferedWriter.newLine();
			bufferedWriter.write("\"Tutorial 2\" 0 0");
			bufferedWriter.newLine();
			for (int i = 0; i < matrixSize; i++) {
				bufferedWriter.write("an \"" + String.valueOf(i + 1) + "\"");
				bufferedWriter.newLine();
			}

			bufferedWriter.newLine();

			for (int i = 0; i < matrixSize; i++) {
				for (int j = 0; j < matrixSize; j++) {
					if (!MatrixArray[i][j].getText().equals("")) {
						bufferedWriter.write("ae \"" + String.valueOf(i + 1) + String.valueOf(j + 1) + "\" " + "\""
								+ String.valueOf(i + 1) + "\" > " + "\"" + String.valueOf(j + 1) + "\"");
						bufferedWriter.newLine();
					}
				}
			}

			bufferedWriter.newLine();

			for (int i = 0; i < matrixSize; i++) {
				for (int j = 0; j < matrixSize; j++) {
					if (!MatrixArray[i][j].getText().equals("")) {
						bufferedWriter.write("ce \"" + String.valueOf(i + 1) + String.valueOf(j + 1) + "\" " + "weight:"
								+ MatrixArray[i][j].getText());
						bufferedWriter.newLine();
					}
				}
			}

			bufferedWriter.newLine();
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}