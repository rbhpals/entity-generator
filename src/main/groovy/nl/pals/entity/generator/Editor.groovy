package nl.pals.entity.generator

import java.awt.*
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.List
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.JTextArea
import javax.swing.table.DefaultTableModel

import groovy.json.JsonOutput
import groovy.swing.SwingBuilder

def swing = new SwingBuilder()

swing.edt {
	frame(title: 'Groovy DSL Editor', size: [600, 400], show: true) {
		borderLayout()
		// Create a menu bar
		menuBar {
			menu('File') {
				menuItem('Exit', actionPerformed: { System.exit(0) })
			}
			menu('Help') {
				menuItem('About', actionPerformed: {
					showHelpDialog()
				})
			}
		}


		// Text area for editing the script
		textArea(id: 'scriptArea', lineWrap: true, wrapStyleWord: true) {
			preferredSize = [580, 300]
		}

		// Wrap the JTextArea in a JScrollPane for scrolling capability
		JScrollPane myScrollPanel = new JScrollPane(scriptArea)
		myScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
		myScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED)

		myScrollPanel.setPreferredSize(new Dimension(580, 300))
		scrollPane(myScrollPanel, constraints: BorderLayout.CENTER)


		// Panel for buttons
		panel(constraints: BorderLayout.SOUTH) {
			button('Load', actionPerformed: { loadFile(scriptArea) })
			button('Save', actionPerformed: { saveFile(scriptArea) })
			button('Dry Run', actionPerformed: { dryRun(scriptArea.text) })
			button('Final Run', actionPerformed: { finalRun(scriptArea.text) })
			button('View Results', actionPerformed: { viewResult() })
		}
	}
}

// Function to load a file into the textarea
def loadFile(JTextArea textArea) {
	def fileChooser = new JFileChooser()
	if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
		def file = fileChooser.selectedFile
		textArea.text = file.text
	}
}

// Function to save the content of the textarea to a file
def saveFile(JTextArea textArea) {
	def fileChooser = new JFileChooser()
	if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
		def file = fileChooser.selectedFile
		file.write(textArea.text)
	}
}

// Function to run the script in the textarea
def dryRun(String script) {
	try {
		def result = new GroovyShell().evaluate(script)
		def prettyResult = EntityGenerator.toJson(result, true).toString()
		//	JOptionPane.showMessageDialog(null, "Script executed successfully:\n$prettyResult")
		showMessageWithScroll(prettyResult)
	} catch (Exception e) {
		JOptionPane.showMessageDialog(null, "Error executing script:\n${e.message}", "Error", JOptionPane.ERROR_MESSAGE)
	}
}

def showMessageWithScroll(String message) {
	// Create a JTextArea to hold the message
	JTextArea textArea = new JTextArea(message)
	textArea.setEditable(false) // Make it read-only
	textArea.setLineWrap(true)
	textArea.setWrapStyleWord(true)

	// Create a JScrollPane to add scrolling capability
	JScrollPane scrollPane = new JScrollPane(textArea)
	scrollPane.setPreferredSize(new Dimension(400, 300)) // Set preferred size

	// Show the JOptionPane with the scrollable JTextArea
	imagePath = "/json.png"
	ImageIcon icon = new ImageIcon(getClass().getResource(imagePath));
	JOptionPane.showMessageDialog(null, scrollPane, "Script executed successfully", JOptionPane.INFORMATION_MESSAGE, icon)
}

// Function to select a number and run the script multiple times
def finalRun(String script) {
	String input = JOptionPane.showInputDialog("Enter a number:")
	if (input != null && input.isInteger()) {
		int number = input.toInteger()

		// Prompt for folder to save results
		def folderChooser = new JFileChooser()
		folderChooser.setDialogTitle("Select Folder to Save Results")
		folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
		if (folderChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			def selectedFolder = folderChooser.selectedFile

			// Prompt for filename
			String fileName = JOptionPane.showInputDialog("Enter a filename (without extension):")
			if (fileName != null && !fileName.trim().isEmpty()) {
				fileName = fileName + "_" + LocalDateTime.now().toEpochSecond(ZoneOffset.systemDefault())
				File resultsFile = new File(selectedFolder, "${fileName}.txt")
				StringBuilder results = new StringBuilder()

				for (int i = 1; i <= number; i++) {
					try {
						def result = new GroovyShell().evaluate(script)
						result = EntityGenerator.toJson(result).toString()
						results.append("Result for run ${i}: ${result}\n")
					} catch (Exception e) {
						results.append("Error executing script for run ${i}:\n${e.message}\n")
					}
				}

				// Save all results to the specified file
				saveResultsToFile(resultsFile, results.toString())
			} else {
				JOptionPane.showMessageDialog(null, "Please enter a valid filename.", "Input Error", JOptionPane.ERROR_MESSAGE)
			}
		}
	} else {
		JOptionPane.showMessageDialog(null, "Please enter a valid integer.", "Input Error", JOptionPane.ERROR_MESSAGE)
	}
}



// Function to save results to a specified file
def saveResultsToFile(File resultsFile, String results) {
	try {
		resultsFile.write(results)
		JOptionPane.showMessageDialog(null, "Results saved to ${resultsFile.absolutePath}")
	} catch (IOException e) {
		JOptionPane.showMessageDialog(null, "Error saving results:\n${e.message}", "Error", JOptionPane.ERROR_MESSAGE)
	}
}

def showHelpDialog() {
	imagePath = "/generator.png"
	ImageIcon icon = new ImageIcon(getClass().getResource(imagePath));

	String helpMessage = """
    Help Information

    This program generates an entity with variables that can be chosen yourself. It can also create relationships under an entity with your own chosen variables.

    Features:
    - Load a dsl that defines the entities
    - Edit the dsl 
    - Save the dsl
    - Dry run to test of the dsl is working (a generated JSON text based format representing structured data,  will be displayed)
    - Final run (multiple JSON structured data sets will be generated and will be saved to a file).

    Purpose:
    The purpose of this program is to generate multiple JSON data sets which can be used as a base to generate (soap)messages.
    """

	JOptionPane.showMessageDialog(null, helpMessage, 'Help', JOptionPane.INFORMATION_MESSAGE, icon)
}



def viewResult() {
	Path filePath = null
	def fileChooser = new JFileChooser()

	if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
		def file = fileChooser.selectedFile
		filePath = file.toPath()
	}

	// Read lines from the file
	def lines = readLinesFromFile(filePath)
	createTable(lines)
}

// Function to read lines from a file als bestand te groot is, dan bufferedReader
//def readLinesFromFile(String filePath) {
//	return Files.readAllLines(Paths.get(filePath))
//}
/**
 * Buffered reader omdat bestand groot kan zijn!!!!!!!
 * @param filePath absolute naam van het bestand
 * @return elke regel in het bestand als een lijst 
 */
def readLinesFromFile(Path filePath) {
	def lines = []
	try {
		// Use BufferedReader to read the file line by line
		Files.newBufferedReader(filePath).withCloseable { reader ->
			String line
			while ((line = reader.readLine()) != null) {
				lines << line
			}
		}
	} catch (IOException e) {
		println e
		println "Error reading file: ${e.message}"
	}
	return lines
}

// Function to create and show a new window with the content of the selected row
def showContentWindow(String content) {
	def contentFrame = new JFrame("Json data structure")
	contentFrame.setLayout(new BorderLayout())
	contentFrame.setSize(300, 200)

	def textArea = new JTextArea(content)
	textArea.setEditable(false)
	contentFrame.add(new JScrollPane(textArea), BorderLayout.CENTER)

	def closeButton = new JButton("Close")
	closeButton.addActionListener { contentFrame.dispose() }
	contentFrame.add(closeButton, BorderLayout.SOUTH)

	contentFrame.setVisible(true)
	return contentFrame
}

def updateContentWindow(JFrame contentFrame, String content) {
	// Update the content of the existing window
	JTextArea textArea = (JTextArea) ((JScrollPane) contentFrame.getContentPane().getComponent(0)).getViewport().getView()
	textArea.setText(content)
}

class Paginator {
	int currentPage = 0
	int rowsPerPage = 25
	List<String> data

	Paginator(List<String> data) {
		this.data = data
	}

	List<String> getCurrentPageData() {
		int start = currentPage * rowsPerPage
		int end = Math.min(start + rowsPerPage, data.size())
		return data.subList(start, end)
	}

	boolean hasNext() {
		return (currentPage + 1) * rowsPerPage < data.size()
	}

	boolean hasPrevious() {
		return currentPage > 0
	}

	void nextPage() {
		if (hasNext()) {
			currentPage++
		}
	}

	void previousPage() {
		if (hasPrevious()) {
			currentPage--
		}
	}
}

def createTable(List<String> lines) {
	def paginator = new Paginator(lines)
	def frame = new JFrame("Generated Entities")
	frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
	frame.setLayout(new BorderLayout())

	def tableModel = new DefaultTableModel()
	tableModel.addColumn("Content")
	def table = new JTable(tableModel)

	def updateTable = {
		tableModel.setRowCount(0) // Clear existing rows
		paginator.getCurrentPageData().each { line ->
			tableModel.addRow([line] as Object[])
		}
	}

	updateTable()

	// Pagination controls
	def paginationPanel = new JPanel()
	def previousButton = new JButton("Previous")
	def nextButton = new JButton("Next")

	previousButton.addActionListener {
		paginator.previousPage()
		updateTable()
	}

	nextButton.addActionListener {
		paginator.nextPage()
		updateTable()
	}

	paginationPanel.add(previousButton)
	paginationPanel.add(nextButton)

	// Add table and pagination to frame
	frame.add(new JScrollPane(table), BorderLayout.CENTER)
	frame.add(paginationPanel, BorderLayout.SOUTH)

	// Content window reference
	JFrame contentFrame = null

	// Row click listener
	table.addMouseListener(new MouseAdapter() {
				@Override
				void mouseClicked(MouseEvent e) {
					int row = table.getSelectedRow()
					if (row != -1) {
						//String content = table.getValueAt(row, 1).toString()
						// content alle tekens tot aan { verwijderen bijv Result voor run 1: {"Contract.. wordt {"Contract....
						String content = table.getValueAt(row, 0).toString().replaceFirst(".*?\\{", "{")
						content = JsonOutput.prettyPrint(content)
						if (contentFrame == null || !contentFrame.isDisplayable()) {
							contentFrame = showContentWindow(content)
						} else {
							updateContentWindow(contentFrame, content)
						}
						//showContentWindow(JsonOutput.prettyPrint(content))
					}
				}
			})

	// Key listener for arrow down key
	table.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP) {
						int row = table.getSelectedRow()
						if (row != -1) {
							//String content = table.getValueAt(row, 1).toString()
							// content alle tekens tot aan { verwijderen bijv Result voor run 1: {"Contract.. wordt {"Contract....
							String content = table.getValueAt(row, 0).toString().replaceFirst(".*?\\{", "{")
							content = JsonOutput.prettyPrint(content)
							if (contentFrame == null || !contentFrame.isDisplayable()) {
								contentFrame = showContentWindow(content)
							} else {
								updateContentWindow(contentFrame, content)
							}
						}
					}
				}
			})

	frame.setSize(400, 300)
	frame.setVisible(true)
}


