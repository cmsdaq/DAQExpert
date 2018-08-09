package rcms.utilities.daqexpert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.persistence.StructureSerializer;

import java.io.File;
import java.io.IOException;

/** class to build DAQ objects from 'json path like' specifications, e.g.
 *
 *   /hltInfo/cpuLoad = 0.5
 *
 *   mainly used to build test cases from actual occurrences of problems in
 *   the production system but only with items in the snapshot needed
 *   to reproduce the problem
 *
 */
public class TestSnapshotBuilder {


	private final ObjectMapper mapper = new ObjectMapper();

	private ObjectNode snapshotData;

	public TestSnapshotBuilder() {

		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		reset();
	}

	public void reset() {
		snapshotData = mapper.createObjectNode();
	}

	/** convenience method for this special item */
	public void setLastUpdate(long lastUpdate) {
		setItem("/lastUpdate", lastUpdate);
	}


	private String[] splitPath(String path) {
		if (! path.startsWith("/")) {
			throw new IllegalArgumentException("path must start with /");
		}

		String parts[] = path.substring(1).split("/");

		if (parts.length < 1) {
			throw new IllegalArgumentException("path too short");
		}

		return parts;
	}

	/** @return the ObjectNode to insert the a value to */
	private ObjectNode getLeafNode(String pathParts[]) {

		JsonNode node = snapshotData;
		for (int i = 0; i < pathParts.length - 1; i++) {

			String part = pathParts[i];

			// find the corresponding child
			if (node instanceof ObjectNode) {

				ObjectNode objNode = (ObjectNode) node;

				JsonNode childNode = objNode.get(part);

				if (childNode == null) {
					// create this node, for the moment we do not
					// support array nodes
					childNode = mapper.createObjectNode();
					objNode.set(part, childNode);
				}

				node = childNode;

			} else {
				// TODO: support array indexing with numbers

				throw new Error("internal error");
			}
		}

		return (ObjectNode) node;
	}

	/** @param path is of the form /item1/item2
	 */
	public void setItem(String path, Float value) {

		String parts[] = splitPath(path);
		ObjectNode leafNode = this.getLeafNode(parts);
		leafNode.put(parts[parts.length-1], value);
	}

	public void setItem(String path, Double value) {

		String parts[] = splitPath(path);
		ObjectNode leafNode = this.getLeafNode(parts);
		leafNode.put(parts[parts.length-1], value);
	}

	public void setItem(String path, Integer value) {

		String parts[] = splitPath(path);
		ObjectNode leafNode = this.getLeafNode(parts);
		leafNode.put(parts[parts.length-1], value);
	}

	public void setItem(String path, Long value) {

		String parts[] = splitPath(path);
		ObjectNode leafNode = this.getLeafNode(parts);
		leafNode.put(parts[parts.length-1], value);
	}

	public void setItem(String path, String value) {

		String parts[] = splitPath(path);
		ObjectNode leafNode = this.getLeafNode(parts);
		leafNode.put(parts[parts.length-1], value);
	}

	/** mostly for debugging: produces a json text representation of the data stored so far */
	@Override
	public String toString() {
		try {
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this.snapshotData);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return "???";
		}
	}

	/** produces the snapshot from the intermediate (json) representation
	 *
	 * */
	public DAQ build() throws IOException {

		// TODO: refactor StructureSerializer to accept generic InputStreams
		//       as opposed to only file names (this will avoid creating
		//       a temporary file)

		// create a temporary file
		File out = File.createTempFile("snapshot", ".json");

		// dump json
		ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
		writer.writeValue(out, snapshotData);

		StructureSerializer serializer = new StructureSerializer();

		return serializer.deserialize(out.getAbsolutePath());
	}



}
