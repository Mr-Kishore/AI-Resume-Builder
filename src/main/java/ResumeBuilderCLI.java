import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Scanner;

public class ResumeBuilderCLI {

    interface ResumeTailor {
        @SystemMessage({
            "Task: Write exactly 4 resume bullet points that match the Job Description.",
            "Rule: You must ONLY use information from the provided Context. Do not invent anything."
        })
        String generateBulletPoints(String jobDescription);
    }

    public static void main(String[] args) {
        System.out.println("[*] Booting up local Ollama models...");

        EmbeddingModel embeddingModel = OllamaEmbeddingModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("nomic-embed-text")
                .timeout(Duration.ofMinutes(2))
                .build();

        ChatLanguageModel chatModel = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("qwen3.5:0.8b")
                .temperature(0.2)
                .timeout(Duration.ofMinutes(5))
                .build();

        System.out.println("[*] Parsing src/main/resources/master_resume.txt...");
        Document document = FileSystemDocumentLoader.loadDocument(
                Paths.get("src/main/resources/master_resume.txt"),
                new TextDocumentParser()
        );

        List<TextSegment> segments = DocumentSplitters
                .recursive(500, 50)
                .split(document);

        System.out.println("[*] Generating embeddings (this takes a moment on first run)...");
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingStore.addAll(embeddingModel.embedAll(segments).content(), segments);

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(5)
                .build();

        ResumeTailor tailor = AiServices.builder(ResumeTailor.class)
                .chatLanguageModel(chatModel)
                .contentRetriever(contentRetriever)
                .build();

        System.out.println("[*] System Ready!\n");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("--------------------------------------------------");
            System.out.println("Paste the Job Description (or type 'exit' to quit):");
            System.out.print("> ");

            String jd = scanner.nextLine();
            if (jd.equalsIgnoreCase("exit")) break;

            if (jd.trim().isEmpty()) continue;

            System.out.println("\n[*] Searching your experience and tailoring bullet points...\n");
            String tailoredResume = tailor.generateBulletPoints(jd);

            System.out.println("=== GENERATED BULLET POINTS ===");
            System.out.println(tailoredResume);
            System.out.println("\n");
        }

        scanner.close();
    }
}
