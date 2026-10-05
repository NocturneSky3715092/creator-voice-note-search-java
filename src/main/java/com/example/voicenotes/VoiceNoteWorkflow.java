package com.example.voicenotes;

import java.io.IOException;

public final class VoiceNoteWorkflow {
    private final InfraiClient client;
    private final String collection;

    public VoiceNoteWorkflow(InfraiClient client, String collection) {
        this.client = client; this.collection = collection;
    }

    public String index(String noteId, String transcript) throws IOException, InterruptedException {
        String embedding = extractEmbedding(client.embeddings(transcript));
        return client.upsert(collection, noteId, embedding, transcript);
    }

    public String search(String query) throws IOException, InterruptedException {
        String embedding = extractEmbedding(client.embeddings(query));
        return client.query(collection, embedding, 5);
    }

    static String extractEmbedding(String envelope) {
        int marker = envelope.indexOf("\"embedding\"");
        if (marker < 0) throw new IllegalArgumentException("embedding missing from response");
        int start = envelope.indexOf('[', marker), end = envelope.indexOf(']', start);
        if (start < 0 || end < 0) throw new IllegalArgumentException("invalid embedding response");
        return envelope.substring(start, end + 1);
    }
}
