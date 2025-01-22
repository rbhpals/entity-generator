package nl.pals.entity.generator

import groovy.swing.SwingBuilder

import javax.swing.ImageIcon
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.JScrollPane
import javax.swing.JTextArea
import java.awt.*
import java.awt.event.*
import nl.pals.entity.generator.EntityGenerator

def swing = new SwingBuilder()

swing.edt {
	frame(title: 'Groovy DSL Editor', size: [600, 400], show: true) {
		borderLayout()



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
			button('Run', actionPerformed: { runScript(scriptArea.text) })
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
def runScript(String script) {
	try {
		def result = new GroovyShell().evaluate(script)
		def prettyResult = EntityGenerator.toJson(result).toString()
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
	imagePath = "/json.png" //zit in de target folder in classes
	ImageIcon icon = new ImageIcon(getClass().getResource(imagePath));
	JOptionPane.showMessageDialog(null, scrollPane, "Script executed successfully", JOptionPane.INFORMATION_MESSAGE, icon)
}