package de.selfmade4u.decentralized_storage.provider

import android.database.Cursor
import android.database.MatrixCursor
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.DocumentsProvider
import de.selfmade4u.decentralized_storage.R
import java.io.File
import java.lang.UnsupportedOperationException

// https://developer.android.com/reference/android/provider/DocumentsProvider
// https://developer.android.com/guide/topics/providers/create-document-provider
// https://developer.android.com/guide/topics/providers/document-provider#overview
class DecentralizedContentProvider : DocumentsProvider() {
    private val ROOT = "de.selfmade4u.decentralized_storage.documents.root"

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
                        DocumentsContract.Root.FLAG_SUPPORTS_SEARCH
            )

            // COLUMN_TITLE is the root title (e.g. Gallery, Drive).
            add(DocumentsContract.Root.COLUMN_TITLE, context!!.getString(R.string.title))

            // This document id cannot change after it's shared.
            add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, getDocIdForFile(getBaseDir()))

            // The child MIME types are used to filter the roots and only present to the
            // user those roots that contain the desired type somewhere in their file hierarchy.
            add(DocumentsContract.Root.COLUMN_MIME_TYPES, getChildMimeTypes(getBaseDir()))
            add(DocumentsContract.Root.COLUMN_AVAILABLE_BYTES, 1000_000_000)
            add(DocumentsContract.Root.COLUMN_ICON, R.mipmap.ic_launcher)
        }

        result.newRow().apply {
            add(DocumentsContract.Root.COLUMN_ROOT_ID, ROOT+"2")

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
                        DocumentsContract.Root.FLAG_SUPPORTS_SEARCH
            )

            // COLUMN_TITLE is the root title (e.g. Gallery, Drive).
            add(DocumentsContract.Root.COLUMN_TITLE, context!!.getString(R.string.title)+"2")

            // This document id cannot change after it's shared.
            add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, getDocIdForFile(getBaseDir())+"2")

            // The child MIME types are used to filter the roots and only present to the
            // user those roots that contain the desired type somewhere in their file hierarchy.
            add(DocumentsContract.Root.COLUMN_MIME_TYPES, getChildMimeTypes(getBaseDir()))
            add(DocumentsContract.Root.COLUMN_AVAILABLE_BYTES, 1000_000_000)
            add(DocumentsContract.Root.COLUMN_ICON, R.mipmap.ic_launcher)
        }

        return result
    }

    private fun getChildMimeTypes(file: File?): String? {
        return if (file!!.isDirectory) DocumentsContract.Document.MIME_TYPE_DIR else "text/plain"
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

            add(DocumentsContract.Document.COLUMN_DISPLAY_NAME,file ?: getFileForDocId(docId).name);

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
            return getDocIdForFile(file)!!
        } else {
            throw UnsupportedOperationException("Create not supported")
        }
    }

    override fun openDocument(documentId: String?, mode: String?, signal: CancellationSignal?): ParcelFileDescriptor {
        TODO("Not yet implemented")
    }
}