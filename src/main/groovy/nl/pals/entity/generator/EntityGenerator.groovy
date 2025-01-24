package nl.pals.entity.generator
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

/**
 * Convert an entity to a json structure
 * @param entity the entity
 * @return the json structure
 */
static def toJson(Entity entity) {
	toJson(entity, false)
}

/**
 * Convert an entity to a json structure (with indent and linefeeds)
 * @param entity the entity
 * @param prettyPrint indicator (true|false) to indent the structure and add linefeeds to make it better readable
 * @return the json structure 
 */
static def toJson(Entity entity, boolean prettyPrint) {
	// Convert the entity structure to a Map and then to JSON
	def rootEntityMap = entity.toMap()
	def jsonOutput = JsonOutput.toJson(rootEntityMap)

	if (prettyPrint) {
		// return the JSON output
		return JsonOutput.prettyPrint(jsonOutput)
	} else {
		return jsonOutput
	}
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




