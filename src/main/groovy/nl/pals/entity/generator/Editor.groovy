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
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.JTextArea
import javax.swing.SwingConstants
import javax.swing.SwingUtilities
import javax.swing.table.DefaultTableModel

import groovy.json.JsonOutput
import groovy.swing.SwingBuilder


class Editor {
	private SwingBuilder swing
	private JFrame frame
	private JTextArea textArea
	private JButton dryRunButton
	private JButton runButton
	private JMenuItem saveMenuItem
	private JMenuItem saveAsMenuItem
	private File currentFile // To keep track of the currently loaded file

	Editor() {
		swing = new SwingBuilder()
		createUI()
	}

	private void createUI() {
		frame = new JFrame('Groovy DSL Editor')
		frame.setSize(600, 400)
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
		frame.setLayout(new BorderLayout())

		// Create menu bar
		JMenuBar menuBar = new JMenuBar()
		JMenu fileMenu = new JMenu('File')
		JMenu viewMenu = new JMenu('View')
		JMenu helpMenu = new JMenu("Help")
		
		JMenuItem loadMenuItem = new JMenuItem('Load')
		loadMenuItem.addActionListener({ loadFile() })
		saveMenuItem = new JMenuItem('Save')
		saveMenuItem.addActionListener({ saveFile() })
		saveMenuItem.setEnabled(false)
		saveAsMenuItem = new JMenuItem('Save As')
		saveAsMenuItem.addActionListener({ saveFileAs() })
		saveAsMenuItem.setEnabled(false)
		JMenuItem exitMenuItem = new JMenuItem('Exit')
		exitMenuItem.addActionListener({ System.exit(0) })

		fileMenu.add(loadMenuItem)
		fileMenu.add(saveMenuItem)
		fileMenu.add(saveAsMenuItem)
		fileMenu.add(exitMenuItem)

		// Result menu item
		JMenuItem viewItem = new JMenuItem("View Results")
		viewItem.addActionListener({ viewResult() })
		viewMenu.add(viewItem)
		
		// Help menu item
		JMenuItem helpItem = new JMenuItem("About")
		helpItem.addActionListener({ showAbout() })
		helpMenu.add(helpItem)
		
		menuBar.add(fileMenu)
		menuBar.add(viewMenu)
		menuBar.add(helpMenu)
		
		frame.setJMenuBar(menuBar)
		// Create title label
		JLabel titleLabel = new JLabel('DSL Content')
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER)
		frame.add(titleLabel, BorderLayout.NORTH)

		// Create text area
		textArea = new JTextArea()
		textArea.setLineWrap(true)
		textArea.setWrapStyleWord(true)
		JScrollPane scrollPane = new JScrollPane(textArea)
		frame.add(scrollPane, BorderLayout.CENTER)

		// Create panel for the Run button
		JPanel buttonPanel = new JPanel()
		dryRunButton = new JButton('Dry Run')
		dryRunButton.addActionListener({ dryRunScript() })
		dryRunButton.setEnabled(false)
		buttonPanel.add(dryRunButton)
		runButton = new JButton('Run')
		runButton.addActionListener({ finalRun() })
		runButton.setVisible(false) // Initially hidden
		buttonPanel.add(runButton)

		frame.add(buttonPanel, BorderLayout.SOUTH)

		// Add document listener to the text area
		textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
					void insertUpdate(javax.swing.event.DocumentEvent e) {
						updateControls()
					}
					void removeUpdate(javax.swing.event.DocumentEvent e) {
						updateControls()
					}
					void changedUpdate(javax.swing.event.DocumentEvent e) {
						updateControls()
					}
				})

		frame.setVisible(true)
	}


	private void loadFile() {
		JFileChooser fileChooser = new JFileChooser()
		int returnValue = fileChooser.showOpenDialog(frame)
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			currentFile = fileChooser.selectedFile
			textArea.text = currentFile.text
			updateControls() // Update controls after loading the file
		}
	}

	private void saveFile() {
		if (currentFile) {
			currentFile.write(textArea.text) // Overwrite the existing file
		} else {
			saveFileAs() // If no current file, prompt for Save As
		}
	}

	private void saveFileAs() {
		JFileChooser fileChooser = new JFileChooser()
		int returnValue = fileChooser.showSaveDialog(frame)
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			currentFile = fileChooser.selectedFile
			currentFile.write(textArea.text) // Save to the new file
			updateControls() // Update controls after saving
		}
	}

	private void dryRunScript() {
		String script = textArea.text
		try {
			def binding = new Binding()
			def shell = new GroovyShell(binding)
			def result = shell.evaluate(script)
			def prettyResult = EntityGenerator.toJson(result, true).toString()
			showResultWindow(prettyResult)

			// Show the repeat button if the script executed successfully
			runButton.setVisible(true)
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame, "Error running script: ${e.message}", "Error", JOptionPane.ERROR_MESSAGE)
		}
	}

	private void updateControls() {
		boolean hasText = !textArea.text.trim().isEmpty()
		saveMenuItem.enabled = hasText
		dryRunButton.setVisible(hasText) // Show or hide the Run button
		dryRunButton.enabled = hasText // Enable or disable the Run button
		runButton.setVisible(false)
	}


	private void showResultWindow(String message) {
		// Create a JTextArea to hold the message
		JTextArea textArea = new JTextArea(message)
		textArea.setEditable(false) // Make it read-only
		textArea.setLineWrap(true)
		textArea.setWrapStyleWord(true)

		// Create a JScrollPane to add scrolling capability
		JScrollPane scrollPane = new JScrollPane(textArea)
		scrollPane.setPreferredSize(new Dimension(400, 300)) // Set preferred size

		// Show the JOptionPane with the scrollable JTextArea
		String imagePath = "/json.png"
		ImageIcon icon = new ImageIcon(getClass().getResource(imagePath));
		JOptionPane.showMessageDialog(null, scrollPane, "Script executed successfully", JOptionPane.INFORMATION_MESSAGE, icon)
	}

	private void finalRun() {
		String script = textArea.text
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
	private void saveResultsToFile(File resultsFile, String results) {
		try {
			resultsFile.write(results)
			JOptionPane.showMessageDialog(null, "Results saved to ${resultsFile.absolutePath}")
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Error saving results:\n${e.message}", "Error", JOptionPane.ERROR_MESSAGE)
		}
	}

	private showAbout() {
		String imagePath = "/generator.png"
		ImageIcon icon = new ImageIcon(getClass().getResource(imagePath));

		String aboutMessage = """
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

		JOptionPane.showMessageDialog(null, aboutMessage, 'About', JOptionPane.INFORMATION_MESSAGE, icon)
	}

	
	private viewResult() {
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
	
	
	/**
	 * Buffered reader omdat bestand groot kan zijn!!!!!!!
	 * @param filePath absolute naam van het bestand
	 * @return elke regel in het bestand als een lijst
	 */
	private readLinesFromFile(Path filePath) {
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
	private showContentWindow(String content) {
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
	
	private updateContentWindow(JFrame contentFrame, String content) {
		// Update the content of the existing window
		JTextArea textArea = (JTextArea) ((JScrollPane) contentFrame.getContentPane().getComponent(0)).getViewport().getView()
		textArea.setText(content)
	}
	
	
	private createTable(List<String> rows) {
		def paginator = new Pagination(rows)
	}
	
	static void main(String[] args) {
		SwingUtilities.invokeLater {
			new Editor()
		}
	}
}

