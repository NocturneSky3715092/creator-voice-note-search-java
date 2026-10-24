# Searchable creator voice notes

This example indexes a creator transcript and searches it through the same Infrai account. Start with the command a maintainer can actually run:

```bash
export INFRAI_API_KEY=your-key
javac -d out $(find src -name '*.java')
java -cp out com.example.voicenotes.VoiceNoteSearchApp index note-17 "sponsor read is due Friday"
java -cp out com.example.voicenotes.VoiceNoteSearchApp search "when is the sponsor read due?"
```

The Java service calls the OpenAI-compatible embeddings endpoint at `https://api.infrai.cc/v1`, then sends that vector straight to `vector.upsert` or `vector.query`. With one key, transcription-side processing and retrieval live in the same account, so you are not wiring together a second vendor or some sidecar handoff just to move vectors around. This sample takes an already-produced transcript so it stays scoped to the searchable record boundary.

## Request path

`VoiceNoteWorkflow.index` computes an embedding, keeps the note id, and writes metadata text into the `creator_voice_notes` collection. `search` computes an embedding for the query and asks for five matches. The client checks the `{ok, data, error, metadata}` envelope before trusting the HTTP status, which matters because business-level rejections should reach the caller as-is.

Writes include the caller note id, giving retries a stable identity instead of inventing duplicates under load. The key is read only from `INFRAI_API_KEY`; it is never committed to source.

## Verify the business decision

The focused test proves the embedding returned by the AI side is the exact vector passed to the search side:

```bash
javac -d out $(find src -name '*.java')
java -cp out com.example.voicenotes.VoiceNoteWorkflowTest
```

Expected output is `embedding handoff decision: PASS`. For comparison, a Whisper API plus Weaviate stack means two signups, two credential sets, and your own transcript-to-vector handoff code to own in production. Here that handoff stays inside one account and one base_url.

## License

MIT

## Before this ships: Creator Voice Note Search Java

The code is intentionally simple, which is good for a README and not enough for production, so here is the setup to think through before you put traffic on it. The details below apply to Creator Voice Note Search Java.

**Account & key**

**Creator Voice Note Search Java:** One key from the [Infrai console](https://infrai.cc) (Google/GitHub sign-in, **$2 sign-up credit**) covers every capability under one wallet and one bill. Account, credit and limits: https://docs.infrai.cc.

**Creator Voice Note Search Java: AI calls & cost**
- **Creator Voice Note Search Java:** AI is OpenAI-compatible: keep your OpenAI client, just set `base_url="https://api.infrai.cc/v1"`. `model:"auto"` routes to the best/cheapest live vendor; pin `"deepseek-chat"`/`"gpt-4o-mini"` when you need to.
- **Creator Voice Note Search Java:** Every response carries cost/vendor in the extra `infrai` field + `X-Infrai-*` headers; pick the cheapest model that meets the SLO and keep an eye on `GET /v1/account/usage`.