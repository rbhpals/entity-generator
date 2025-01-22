package nl.pals.entity.generator
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.SwingUtilities
import groovy.json.JsonOutput

class Entity extends Randomizer {
	String name
	Map<String, Object> properties = [:]
	Map<String, Entity> relations = [:]

	// Method to add properties to the entity
	def property(String key, value) {
		properties[key] = value
	}

	def propertyMissing(String name, value) {
		//properties[name] = value
	}

	def methodMissing(String name, args) {
		properties[name] = args[0]
	}

	// Method to add a relation (child entity) to the entity
	def relation(String name, Closure closure) {
		Entity childEntity = new Entity(name: name)
		closure.delegate = childEntity
		closure.resolveStrategy = Closure.DELEGATE_FIRST
		closure()
		relations[name] = childEntity
	}





	@Override
	String toString() {
		def propsStr = properties.collect { "${it.key}: ${it.value}" }.join(", ")
		def relsStr = relations.collect { "${it.key}: ${it.value}" }.join(", ")
		return "Entity(name: '$name', properties: { $propsStr }, relations: { $relsStr })"
	}

	// Convert the entity structure into a Map (for JSON conversion)
	Map toMap() {
		def entityMap = [name: name, properties: properties]
		// For relations, recursively call toMap() on each child entity
		if (relations) {
			entityMap.relations = relations.collectEntries { key, childEntity ->
				[(key): childEntity.toMap()]
			}
		}
		return entityMap
	}
}

static def toJson(Entity entity) {
	// Convert the entity structure to a Map and then to JSON
	def rootEntityMap = entity.toMap()
	def jsonOutput = JsonOutput.toJson(rootEntityMap)
	
	// return the JSON output
	return JsonOutput.prettyPrint(jsonOutput)
}
// DSL Builder for creating entities
static def entity(String name, Closure closure) {
	Entity newEntity = new Entity(name: name)
	closure.delegate = newEntity
	closure()
	return newEntity
}

//// Function to load and evaluate a DSL file
//def loadDslFile(String fileName) {
//	def dslFile = new File(fileName)
//	if (!dslFile.exists()) {
//		throw new FileNotFoundException("DSL file not found: $fileName")
//	}
//	def dslContent = dslFile.text
//	return evaluate(dslContent)
//}



//// Create the Swing GUI
//def createAndShowGUI() {
//	JFrame frame = new JFrame("DSL Loader")
//	frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
//	frame.setSize(400, 300)
//
//	JPanel panel = new JPanel()
//	panel.setLayout(new FlowLayout())
//
//	JButton loadButton = new JButton("Load DSL File")
//	JTextArea textArea = new JTextArea(50, 30)
//	textArea.setEditable(false)
//
//	loadButton.addActionListener(new ActionListener() {
//				@Override
//				void actionPerformed(ActionEvent e) {
//					JFileChooser fileChooser = new JFileChooser(new File("/Users/rbhpals/dev/repo/git/entity-generator/src/main/scripts"))
//					int returnValue = fileChooser.showOpenDialog(frame)
//					if (returnValue == JFileChooser.APPROVE_OPTION) {
//						File selectedFile = fileChooser.selectedFile
//						try {
//							def app = loadDslFile(selectedFile.absolutePath)
//							//textArea.setText(app.toString())
//							textArea.setText(toJson(app).toString())
//							
//						} catch (Exception ex) {
//							textArea.setText("Error: ${ex.message}")
//						}
//					}
//				}
//			})
//
//	panel.add(loadButton)
//	panel.add(new JScrollPane(textArea))
//
//	frame.getContentPane().add(panel)
//	frame.setVisible(true)
//}
//
//// Run the GUI
//SwingUtilities.invokeLater {
//	createAndShowGUI()
//}
