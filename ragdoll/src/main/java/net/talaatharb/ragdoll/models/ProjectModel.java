package net.talaatharb.ragdoll.models;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ProjectModel {

	public static final String DEFAULT_HOST_PATH = "http://localhost:11434/";

	private final String embeddingModelName;
	private String generationModelName;
	private String hostPath = DEFAULT_HOST_PATH;
	private final String projectPath;
	private final int vectorSize;
}
