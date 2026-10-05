package com.example.voicenotes;

public final class VoiceNoteSearchApp {
    public static void main(String[] args) throws Exception {
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("Set INFRAI_API_KEY");
        if (args.length < 2) throw new IllegalArgumentException("index <id> <transcript> | search <query>");
        VoiceNoteWorkflow flow = new VoiceNoteWorkflow(new InfraiClient(key), "creator_voice_notes");
        if ("index".equals(args[0])) System.out.println(flow.index(args[1], args.length > 2 ? args[2] : args[1]));
        else if ("search".equals(args[0])) System.out.println(flow.search(args[1]));
        else throw new IllegalArgumentException("unknown command");
    }
}
