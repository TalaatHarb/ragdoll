package net.talaatharb.ragdoll.services;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Consumer;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.generate.OllamaStreamHandler;
import io.github.ollama4j.models.response.Model;
import io.github.ollama4j.models.response.OllamaResult;
import io.github.ollama4j.utils.OptionsBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.talaatharb.ragdoll.exceptions.ModelRelatedException;

@Slf4j
@RequiredArgsConstructor
public class ModelManagmentService {

	private final OllamaAPI ollamaAPI;
	
	public ModelManagmentService() {
		ollamaAPI = new OllamaAPI();
	}

	public boolean isModelSeriveWorking() {
		return ollamaAPI.ping();
	}

	public List<String> listModelNames()
			throws OllamaBaseException, IOException, InterruptedException, URISyntaxException {
		return ollamaAPI.listModels().stream().map(Model::getName).toList();
	}

	public void downloadModel(String modelName)
			throws OllamaBaseException, IOException, URISyntaxException, InterruptedException {
		ollamaAPI.pullModel(modelName);
	}

	public boolean isModelAvailable(String modelName)
			throws OllamaBaseException, IOException, InterruptedException, URISyntaxException {
		return listModelNames().contains(modelName);
	}

	public List<Double> generateEmbeddings(String modelName, String text) {
		List<Double> embeddings = null;
		try {
			embeddings = ollamaAPI.generateEmbeddings(modelName, text);
		} catch (IOException | InterruptedException | OllamaBaseException e) {
			log.error("Unable to generate embeddings using model {}, for the text: {}", modelName, text);
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			throw new ModelRelatedException();
		}
		return embeddings;
	}

	public String generateGivenPrompt(String modelName, String prompt) {
		OllamaResult result;
		try {
			result = ollamaAPI.generate(modelName, prompt, false, new OptionsBuilder().build());
		} catch (OllamaBaseException | IOException | InterruptedException e) {
			log.error("Unable to generate text using model {}, for the propmpt: {}", modelName, prompt);
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			throw new ModelRelatedException();
		}

		return result.getResponse();
	}

	public String generateGivenPromptWithMomentaryResult(String modelName, String prompt, Consumer<String> handler)
			throws OllamaBaseException, IOException, InterruptedException {
		final OllamaStreamHandler streamHandler = handler::accept;

		final OllamaResult result = ollamaAPI.generate(modelName, prompt, false, new OptionsBuilder().build(),
				streamHandler);

		return result.getResponse();
	}
}
