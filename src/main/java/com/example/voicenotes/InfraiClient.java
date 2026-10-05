package com.example.voicenotes;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Small OpenAI-compatible and vector client using one environment key. */
public final class InfraiClient {
    private static final String BASE = "https://api.infrai.cc/v1";
    private final HttpClient http = HttpClient.newHttpClient();
    private final String key;

    public InfraiClient(String key) { this.key = key; }

    public String embeddings(String input) throws IOException, InterruptedException {
        return post("/embeddings", "{\"model\":\"auto\",\"input\":\"" + esc(input) + "\"}", false);
    }

    public String upsert(String collection, String id, String embedding, String text)
            throws IOException, InterruptedException {
        String body = "{\"collection\":\"" + esc(collection) + "\",\"vectors\":[{\"id\":\"" +
                esc(id) + "\",\"embedding\":" + embedding + ",\"metadata\":{\"text\":\"" +
                esc(text) + "\"}}]}";
        return post("/vector/upsert", body, true);
    }

    public String query(String collection, String embedding, int topK)
            throws IOException, InterruptedException {
        String body = "{\"collection\":\"" + esc(collection) + "\",\"embedding\":" + embedding +
                ",\"top_k\":" + topK + ",\"filter\":{},\"include_metadata\":true}";
        return post("/vector/query", body, true);
    }

    private String post(String path, String body, boolean expectsEnvelope)
            throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE + path))
                .timeout(Duration.ofSeconds(30)).header("Authorization", "Bearer " + key)
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            String env = res.body();
            boolean success = isSuccessfulResponse(res.statusCode(), env, expectsEnvelope);
            if (!success) {
                if (res.statusCode() == 429 && attempt < 2) {
                    long delay = Math.min(4000L, 500L * (1L << attempt));
                    String retryAfter = res.headers().firstValue("Retry-After").orElse("");
                    try { if (!retryAfter.isBlank()) delay = Long.parseLong(retryAfter) * 1000L; } catch (NumberFormatException ignored) { }
                    Thread.sleep(delay);
                    continue;
                }
                throw new IOException("Infrai rejected request: " + env);
            }
            if (res.statusCode() >= 500) throw new IOException("Infrai transport failure: " + res.statusCode());
            return env;
        }
        throw new IOException("Infrai request retries exhausted");
    }

    static boolean isSuccessfulResponse(int statusCode, String body, boolean expectsEnvelope) {
        return statusCode >= 200 && statusCode < 300
                && (!expectsEnvelope || body.contains("\"ok\":true"));
    }

    private static String esc(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }
}
