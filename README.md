# Resume Builder CLI

A local-first Java CLI that tailors resume bullet points to a pasted job description using LangChain4j + Ollama.

## Features

- Uses local Ollama models (no cloud dependency required for generation).
- Retrieves relevant resume chunks with embeddings before generation.
- Produces 4 tailored bullet points grounded in your resume context.

## Project Structure

- `src/main/java/ResumeBuilderCLI.java` - CLI entrypoint
- `src/main/resources/master_resume.txt` - source resume text used for retrieval
- `pom.xml` - Maven configuration and dependencies

## Prerequisites

- Java 17+
- Maven 3.8+
- [Ollama](https://ollama.com/) running locally on `http://localhost:11434`
- Models pulled locally:
  - `nomic-embed-text`
  - `qwen3.5:0.8b`

## Setup

1. Put your complete resume content inside `src/main/resources/master_resume.txt`.
2. Ensure Ollama is running and models are available.

## Run

```bash
mvn compile exec:java
```

Paste a job description at the prompt. Type `exit` to quit.

## License

This project is licensed under the MIT License.
