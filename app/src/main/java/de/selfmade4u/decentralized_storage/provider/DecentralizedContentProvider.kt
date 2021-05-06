package de.selfmade4u.decentralized_storage.provider

import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.CancellationSignal
import android.os.Handler
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.DocumentsProvider
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import de.selfmade4u.decentralized_storage.BuildConfig
import de.selfmade4u.decentralized_storage.R
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.UnsupportedOperationException

// https://developer.android.com/reference/android/provider/DocumentsProvider
// https://developer.android.com/guide/topics/providers/create-document-provider
// https://developer.android.com/guide/topics/providers/document-provider#overview
class DecentralizedContentProvider : DocumentsProvider() {
    private val ROOT = "de.selfmade4u.decentralized_storage.documents.root"
    private val TAG = "de.selfmade4u.decentralized_storage"

    private val DEFAULT_ROOT_PROJECTION: Array<String> = arrayOf(
        DocumentsContract.Root.COLUMN_ROOT_ID,
        DocumentsContract.Root.COLUMN_MIME_TYPES,
        DocumentsContract.Root.COLUMN_FLAGS,
        DocumentsContract.Root.COLUMN_ICON,
        DocumentsContract.Root.COLUMN_TITLE,
        DocumentsContract.Root.COLUMN_SUMMARY,
        DocumentsContract.Root.COLUMN_DOCUMENT_ID,
        DocumentsContract.Root.COLUMN_AVAILABLE_BYTES
    )
    private val DEFAULT_DOCUMENT_PROJECTION: Array<String> = arrayOf(
        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
        DocumentsContract.Document.COLUMN_MIME_TYPE,
        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        DocumentsContract.Document.COLUMN_LAST_MODIFIED,
        DocumentsContract.Document.COLUMN_FLAGS,
        DocumentsContract.Document.COLUMN_SIZE
    )

    override fun onCreate(): Boolean {
        return true
    }

    fun getBaseDir(): File? {
        return context!!.filesDir;
    }

    override fun queryRoots(projection: Array<out String>?): Cursor {
        // Use a MatrixCursor to build a cursor
        // with either the requested fields, or the default
        // projection if "projection" is null.
        val result = MatrixCursor(resolveRootProjection(projection))

        // If user is not logged in, return an empty root cursor.  This removes our
        // provider from the list entirely.
        if (!isUserLoggedIn()) {
            return result
        }

        // It's possible to have multiple roots (e.g. for multiple accounts in the
        // same app) -- just add multiple cursor rows.
        result.newRow().apply {
            add(DocumentsContract.Root.COLUMN_ROOT_ID, ROOT)

            // You can provide an optional summary, which helps distinguish roots
            // with the same title. You can also use this field for displaying an
            // user account name.
            add(DocumentsContract.Root.COLUMN_SUMMARY, context!!.getString(R.string.root_summary))

            // FLAG_SUPPORTS_CREATE means at least one directory under the root supports
            // creating documents. FLAG_SUPPORTS_RECENTS means your application's most
            // recently used documents will show up in the "Recents" category.
            // FLAG_SUPPORTS_SEARCH allows users to search all documents the application
            // shares.
            add(
                DocumentsContract.Root.COLUMN_FLAGS,
                DocumentsContract.Root.FLAG_SUPPORTS_CREATE or
                        DocumentsContract.Root.FLAG_SUPPORTS_RECENTS or
                        DocumentsContract.Root.FLAG_SUPPORTS_SEARCH or
                        DocumentsContract.Root.FLAG_SUPPORTS_IS_CHILD // for ACTION_OPEN_DOCUMENT_TREE
            )

            // COLUMN_TITLE is the root title (e.g. Gallery, Drive).
            add(DocumentsContract.Root.COLUMN_TITLE, context!!.getString(R.string.title))

            // This document id cannot change after it's shared.
            add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, getDocIdForFile(getBaseDir()))

            // The child MIME types are used to filter the roots and only present to the
            // user those roots that contain the desired type somewhere in their file hierarchy.
            add(DocumentsContract.Root.COLUMN_MIME_TYPES, null) // allow all types of files in this root
            add(DocumentsContract.Root.COLUMN_AVAILABLE_BYTES, 1000_000_000)
            add(DocumentsContract.Root.COLUMN_ICON, R.mipmap.ic_launcher)
        }

        return result
    }

    override fun isChildDocument(parentDocumentId: String?, documentId: String?): Boolean {
        val pn = getFileForDocId(parentDocumentId).toPath()
        val cn = getFileForDocId(documentId).toPath()
        return cn.nameCount > pn.nameCount && cn.startsWith(pn)
    }

    private fun getChildMimeTypes(file: File?): String? {
        return if (file!!.isDirectory) DocumentsContract.Document.MIME_TYPE_DIR else MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(
            file.toUri().toString()
        ))
    }

    private fun getDocIdForFile(file: File?): String? {
        return file!!.absolutePath
    }

    private fun getFileForDocId(docId: String?): File {
        return File(docId!!);
    }

    private fun isUserLoggedIn(): Boolean {
        return true
    }

    private fun resolveRootProjection(projection: Array<out String>?): Array<out String>? {
        return projection ?: DEFAULT_ROOT_PROJECTION
    }

    private fun includeFile(result: MatrixCursor, docId: String?, file: File?) {
        result.newRow().apply {

            // You can provide an optional summary, which helps distinguish roots
            // with the same title. You can also use this field for displaying an
            // user account name.
            add(DocumentsContract.Document.COLUMN_SUMMARY, context!!.getString(R.string.root_summary))

            // FLAG_SUPPORTS_CREATE means at least one directory under the root supports
            // creating documents. FLAG_SUPPORTS_RECENTS means your application's most
            // recently used documents will show up in the "Recents" category.
            // FLAG_SUPPORTS_SEARCH allows users to search all documents the application
            // shares.
            add(
                DocumentsContract.Document.COLUMN_FLAGS,
                DocumentsContract.Document.FLAG_SUPPORTS_DELETE or DocumentsContract.Document.FLAG_DIR_SUPPORTS_CREATE
            )

            // This document id cannot change after it's shared.
            add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, docId ?: getDocIdForFile(file))

            add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, (file ?: getFileForDocId(docId)).name);

            // The child MIME types are used to filter the roots and only present to the
            // user those roots that contain the desired type somewhere in their file hierarchy.
            add(DocumentsContract.Document.COLUMN_MIME_TYPE, getChildMimeTypes(file ?: getFileForDocId(docId)))
            add(DocumentsContract.Document.COLUMN_ICON, R.mipmap.ic_launcher)
        }
    }

    override fun queryChildDocuments(
        parentDocumentId: String?,
        projection: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        return MatrixCursor(resolveDocumentProjection(projection)).apply {
            val parent: File = getFileForDocId(parentDocumentId)
            parent.listFiles()!!
                .forEach { file ->
                    includeFile(this, null, file)
                }
        }
    }

    private fun resolveDocumentProjection(projection: Array<out String>?): Array<out String> {
        return projection ?: DEFAULT_DOCUMENT_PROJECTION
    }

    override fun queryDocument(documentId: String?, projection: Array<out String>?): Cursor {
        // Create a cursor with the requested projection, or the default projection.
        return MatrixCursor(resolveDocumentProjection(projection)).apply {
            includeFile(this, documentId, null)
        }
    }

    override fun createDocument(
        parentDocumentId: String?,
        mimeType: String?,
        displayName: String?
    ): String {
        if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
            val file = File(getFileForDocId(parentDocumentId), displayName!!);
            file.mkdir()

            val docId = getDocIdForFile(file)!!

            // TODO FIXME doesn't work
            val rootsUri: Uri = DocumentsContract.buildChildDocumentsUri(BuildConfig.DOCUMENTS_AUTHORITY, parentDocumentId)
            context!!.contentResolver.notifyChange(rootsUri, null)

            return docId
        } else {
            val file = File(getFileForDocId(parentDocumentId), displayName!!);
            file.createNewFile()

            val docId = getDocIdForFile(file)!!
            return docId
        }
    }

    override fun openDocument(
        documentId: String?,
        mode: String?,
        signal: CancellationSignal?
    ): ParcelFileDescriptor {
        Log.v(TAG, "openDocument, mode: $mode")
        // It's OK to do network operations in this method to download the document,
        // as long as you periodically check the CancellationSignal. If you have an
        // extremely large file to transfer from the network, a better solution may
        // be pipes or sockets (see ParcelFileDescriptor for helper methods).

        val file: File = getFileForDocId(documentId)
        val accessMode: Int = ParcelFileDescriptor.parseMode(mode)

        val isWrite: Boolean = mode!!.contains("w")
        return if (isWrite) {
            val handler = Handler(context!!.mainLooper)
            // Attach a close listener if the document is opened in write mode.
            try {
                ParcelFileDescriptor.open(file, accessMode, handler) {
                    // Update the file with the cloud server. The client is done writing.
                    Log.i(TAG, "A file with id $documentId has been closed! Time to update the server.")
                }
            } catch (e: IOException) {
                throw FileNotFoundException(
                    "Failed to open document with id $documentId and mode $mode"
                )
            }
        } else {
            ParcelFileDescriptor.open(file, accessMode)
        }
    }

}