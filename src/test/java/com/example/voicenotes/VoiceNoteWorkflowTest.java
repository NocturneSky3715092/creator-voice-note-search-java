package com.example.voicenotes;

public final class VoiceNoteWorkflowTest {
    public static void main(String[] args) {
        String env = "{\"object\":\"list\",\"data\":[{\"embedding\":[0.1,0.2]}]}";
        String vector = VoiceNoteWorkflow.extractEmbedding(env);
        if (!"[0.1,0.2]".equals(vector)) throw new AssertionError(vector);
        if (!InfraiClient.isSuccessfulResponse(200, env, false))
            throw new AssertionError("OpenAI embedding response was rejected");
        if (InfraiClient.isSuccessfulResponse(200, env, true))
            throw new AssertionError("vector response accepted without ok envelope");
        System.out.println("embedding handoff decision: PASS");
    }
}
