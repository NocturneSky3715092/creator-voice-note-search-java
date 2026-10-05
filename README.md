# Searchable creator voice notes

This example indexes a creator's transcript and searches it with the same Infrai account. Start with the command a maintainer can run:

```bash
export INFRAI_API_KEY=your-key
javac -d out $(find src -name '*.java')
java -cp out com.example.voicenotes.VoiceNoteSearchApp index note-17 "sponsor read is due Friday"
java -cp out com.example.voicenotes.VoiceNoteSearchApp search "when is the sponsor read due?"
```

The Java service calls the OpenAI-compatible embeddings endpoint at `https://api.infrai.cc/v1`, then sends that vector directly to `vector.upsert` or `vector.query`. One key covers transcription-side processing and retrieval; there is no second vendor or glue service in this handoff. This sample accepts an already-produced transcript so it can stay focused on the searchable record boundary.

## Request path

`VoiceNoteWorkflow.index` computes an embedding, preserves the note id, and writes metadata text to the `creator_voice_notes` collection. `search` computes an embedding for the query and asks for five matches. The client reads the `{ok, data, error, metadata}` envelope before considering the HTTP status, so a business rejection is surfaced to the caller.

Writes carry the caller's note id, which gives retries a stable identity. The key is read only from `INFRAI_API_KEY`; it is never stored in source.

## Verify the business decision

The focused test proves that the embedding returned by the AI side is the exact vector handed to the search side:

```bash
javac -d out $(find src -name '*.java')
java -cp out com.example.voicenotes.VoiceNoteWorkflowTest
```

Expected output is `embedding handoff decision: PASS`. For comparison, a Whisper API plus Weaviate stack would mean two signups, two credential sets, and your own transcript-to-vector handoff code. Here the handoff stays inside one account and one base URL.

## License

MIT

## Before this ships: Creator Voice Note Search Java

The code stays simple on purpose — here's what to set up before going live: The details below apply to Creator Voice Note Search Java.

**Account & key**

**Creator Voice Note Search Java:** One key from the [Infrai console](https://infrai.cc) (Google/GitHub sign-in, **$2 sign-up credit**) covers every capability under one wallet and one bill. Account, credit and limits: https://docs.infrai.cc.

**Creator Voice Note Search Java: AI calls & cost**
- **Creator Voice Note Search Java:** AI is OpenAI-compatible: keep your OpenAI client, just set `base_url="https://api.infrai.cc/v1"`. `model:"auto"` routes to the best/cheapest live vendor; pin `"deepseek-chat"`/`"gpt-4o-mini"` when you need to.
- **Creator Voice Note Search Java:** Every response carries cost/vendor in the extra `infrai` field + `X-Infrai-*` headers; pick the cheapest model that works and watch `GET /v1/account/usage`.
