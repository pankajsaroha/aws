## Multipart Upload
Multipart upload is a way to split a large file into smaller parts (chunks). These chunks can be uploaded independently and in parallel, which improves speed and reliability. If one chunk fails, only that chunk needs to be retried instead of re-uploading the entire file.

## Resumable Upload
Resumable upload is a pattern built on top of multipart upload that allows clients to reliably upload large files, even if the upload is interrupted.
* The client initiates the upload by calling an initiate request to the server. The server responds with an uploadId.
* The client uses this uploadId to upload individual chunks, each identified by a part number (chunk number). Chunks can be uploaded sequentially or in parallel.
* If the upload is interrupted, the client can resume by reusing the same uploadId and only re-uploading the missing chunks.
* Once all chunks are uploaded, the client calls a complete request, and the server merges the chunks into a single file.

Multipart upload is the low-level mechanism.
Resumable upload adds reliability on top of it (resume from failure).
