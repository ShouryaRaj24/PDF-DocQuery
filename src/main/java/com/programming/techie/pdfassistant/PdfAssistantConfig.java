package com.programming.techie.pdfassistant;

import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.retriever.EmbeddingStoreRetriever;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.cassandra.AstraDbEmbeddingConfiguration;
import dev.langchain4j.store.embedding.cassandra.AstraDbEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Component
public class PdfAssistantConfig {
    @Bean
    EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    AstraDbEmbeddingStore astraDbEmbeddingStore() {
        String astraToken = "AstraCS:OZBrtwhqUljvidjHDijysOTW:54249d4d91f77f6a547fb34b08f057a96300edd86c97e8eb6c4a2224a75c932d";
        String databaseId = "a023b425-42e8-4434-b77b-120fbab4c39e";

        return new AstraDbEmbeddingStore(AstraDbEmbeddingConfiguration
                .builder()
                .token(astraToken)
                .databaseId(databaseId)
                .databaseRegion("us-east1")
                .keyspace("default_keyspace")
                .table("pdfchat")
                .dimension(384)
                .build());
    }

    @Bean
    EmbeddingStoreIngestor embeddingStoreIngestor() {
        return EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(300, 0))
                .embeddingModel(embeddingModel())
                .embeddingStore(astraDbEmbeddingStore())
                .build();
    }

    @Bean
    public ConversationalRetrievalChain conversationalRetrievalChain() {
        return ConversationalRetrievalChain.builder()
                .chatLanguageModel(OpenAiChatModel.withApiKey("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"))
                .retriever(EmbeddingStoreRetriever.from(astraDbEmbeddingStore(), embeddingModel()))
                .build();
    }
}
