package net.talaatharb.ragdoll.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.IntStream;

import org.sqlite.SQLiteConnection;
import org.sqlite.core.DB;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.talaatharb.ragdoll.models.ProjectModel;

@Slf4j
@RequiredArgsConstructor
public class ProjectManagementService {

	private static final int CHUNK_SIZE = 10;

	private final ModelManagmentService modelManagmentService;

	@Getter
	@Setter
	private String hostPath = ProjectModel.DEFAULT_HOST_PATH;

	private ProjectModel currentProject;

	public ProjectModel createNewProject(String projectPath, String embeddingModel, String generationModel) {
		final int vectorSize = modelManagmentService.generateEmbeddings(embeddingModel, "test").size();
		currentProject = new ProjectModel(embeddingModel, projectPath, vectorSize);
		currentProject.setGenerationModelName(generationModel);

		createProjectFile(currentProject);

		return currentProject;
	}

	public ProjectModel openExistingProject(String projectPath) {
		// open sqlite file and read model deatils

		return currentProject;
	}

	private void createProjectFile(ProjectModel model) {

		String url = "jdbc:sqlite:" + model.getProjectPath();
		try (Connection conn = DriverManager.getConnection(url)) {

			// Load the extension
			DB db = conn.unwrap(SQLiteConnection.class).getDatabase();
			db.enable_load_extension(true);
			Statement stmt = conn.createStatement();
			stmt.execute("select load_extension('./lib/vec0.so');");

			// Create a table with vector data
			stmt.execute("create virtual table vectors using vec0(id INTEGER PRIMARY KEY, embedding float[%d]);".formatted(model.getVectorSize()));

			stmt.execute("""
					CREATE TABLE documents
					(id INTEGER PRIMARY KEY,
					data Text NOT NULL);
					""".formatted(model.getVectorSize()));

			// Create project table
			stmt.execute("""
					CREATE TABLE project (
					    id INTEGER PRIMARY KEY,
					    embeddingModel TEXT NOT NULL,
					    vectorSize TEXT NOT NULL,
					    generationModel TEXT NOT NULL,
					    projectPath TEXT NOT NULL,
					    hostPath TEXT NOT NULL
					);
					    """);

			String insertProjectStatment = """
					INSERT INTO project
					      (embeddingModel, vectorSize, generationModel, projectPath, hostPath)
					      VALUES ("%s", %d, "%s", "%s", "%s");
					""".formatted(model.getEmbeddingModelName(), model.getVectorSize(), model.getGenerationModelName(),
					model.getProjectPath(), model.getHostPath());

			stmt.execute(insertProjectStatment);

		} catch (SQLException e) {
			log.error(e.getMessage());
		}
		log.info("File created");
	}

	private List<List<Double>> generateEmbeddingsForData(List<String> strings) {
		return strings.stream()
				.map(s -> modelManagmentService.generateEmbeddings(currentProject.getEmbeddingModelName(), s)).toList();
	}

	public void saveData(List<String> strings) {
		var listSize = strings.size();
		String url = "jdbc:sqlite:" + currentProject.getProjectPath();
		try (Connection conn = DriverManager.getConnection(url)) {
			DB db = conn.unwrap(SQLiteConnection.class).getDatabase();
			db.enable_load_extension(true);
			Statement stmt = conn.createStatement();
			stmt.execute("select load_extension('./lib/vec0.so');");

			IntStream.range(0, (listSize + CHUNK_SIZE - 1) / CHUNK_SIZE)
					.mapToObj(i -> strings.subList(i * CHUNK_SIZE, Math.min(listSize, (i + 1) * CHUNK_SIZE)))
					.forEach(chunk -> {
						var embeddings = generateEmbeddingsForData(chunk);
						// batch save logic

						var insertVectorsStatment = new StringBuilder("""
								INSERT INTO vectors
								      (embedding)
								      VALUES
								                       """);
						var insertDocumentsStatment = new StringBuilder("""
								INSERT INTO documents
								      (data)
								      VALUES
								                       """);
						var chunkSize = chunk.size();
						for (int i = 0; i < chunkSize - 1; i++) {
							insertVectorsStatment.append("('");
							insertVectorsStatment.append(convertEmbeddingToFloatArrayString(embeddings.get(i)));
							insertVectorsStatment.append("'),");
							
							insertDocumentsStatment.append("(\"");
							insertDocumentsStatment.append(chunk.get(i));
							insertDocumentsStatment.append("\"),");
						}
						insertVectorsStatment.append("('");
						insertVectorsStatment
								.append(convertEmbeddingToFloatArrayString(embeddings.get(chunkSize - 1)));
						insertVectorsStatment.append("');");
						
						insertDocumentsStatment.append("(\"");
						insertDocumentsStatment.append(chunk.get(chunkSize - 1));
						insertDocumentsStatment.append("\");");

						try {
							stmt.execute(insertVectorsStatment.toString());
							stmt.execute(insertDocumentsStatment.toString());
						} catch (SQLException e) {
							log.error(e.getMessage());
						}
					});
		} catch (SQLException e) {
			log.error(e.getMessage());
		}
		
		log.info("Save complete");

		queryForContext("Why a PAN card is important?", 5);
	}

	public List<String> queryForContext(String question, int numberOfResults) {
		var embedding = convertEmbeddingToFloatArrayString(generateEmbeddingsForData(List.of(question)).get(0));

		String url = "jdbc:sqlite:" + currentProject.getProjectPath();

		try (Connection conn = DriverManager.getConnection(url)) {

			// Load the extension
			DB db = conn.unwrap(SQLiteConnection.class).getDatabase();
			db.enable_load_extension(true);
			Statement stmt = conn.createStatement();
			stmt.execute("select load_extension('./lib/vec0.so');");

			var rs = stmt.executeQuery("""
					select
					  id,
					  distance
					from vectors
					where embedding match '%s'
					order by distance
					limit %d;
					""".formatted(embedding, numberOfResults));

			while (rs.next()) {
				log.info("\nID: {} \ndistance: {}\n", rs.getInt("id"), rs.getFloat("distance"));
			}

		} catch (SQLException e) {
			log.error(e.getMessage());
		}

		return List.of();
	}

	private String convertEmbeddingToFloatArrayString(List<Double> embeddingVector) {
		int size = embeddingVector.size();
		var floatArrayString = new StringBuilder("[");

		for (int i = 0; i < size - 1; i++) {
			floatArrayString.append(embeddingVector.get(i).floatValue());
			floatArrayString.append(", ");
		}
		floatArrayString.append(embeddingVector.get(size - 1).floatValue());
		floatArrayString.append("]");

		return floatArrayString.toString();
	}

}
