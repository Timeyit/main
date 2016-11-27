package timeydesktop;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class LogViewer {
	public static boolean RIGHT_TO_LEFT = false;
	
	static List<LogEntry> timeLogs = new ArrayList<LogEntry>();
	static Object[][] data = new Object[0][3];
	static String[] columnNames = {
			"Time Stamp",
            "Log Level",
            "Log Message"};
	public static void addComponentsToPane(Container pane) {

		if (!(pane.getLayout() instanceof BorderLayout)) {
			pane.add(new JLabel("Container doesn't use BorderLayout!"));
			return;
		}
		
		if (RIGHT_TO_LEFT) {
			pane.setComponentOrientation(
					java.awt.ComponentOrientation.RIGHT_TO_LEFT);
			}
	
		JTable table = new JTable(data, columnNames);
		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		pane.add(scrollPane, BorderLayout.CENTER);
	
		//button = new JButton("5 (LINE_END)");
		//pane.add(button, BorderLayout.LINE_END);
	}

	public static void createAndShowGUI() {		
		JFrame frame = new JFrame("Timey Log Viewer");
		refreshItems();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addComponentsToPane(frame.getContentPane());
		frame.pack();
		frame.setVisible(true);
	}
	
	private static void refreshItems()
	{
		timeLogs = TimeyEngine.TimeyDBHelper.GetLogEntries("");
		data = new Object[timeLogs.size()][3];
		int count = 0;
		for(LogEntry entry : timeLogs)
		{
			data[count][0] = entry.Timestamp.toString();
			data[count][1] = entry.LogLevel;
			data[count][2] = entry.LogMessage;
			count++;
		}
	}
}
