package de.selfmade4u.decentralized_storage.provider

import android.database.Cursor
import android.database.MatrixCursor
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.DocumentsContract.Root.FLAG_SUPPORTS_RECENTS
import android.provider.DocumentsProvider
import de.selfmade4u.decentralized_storage.R

// https://developer.android.com/reference/android/provider/DocumentsProvider
// https://developer.android.com/guide/topics/providers/create-document-provider
// https://developer.android.com/guide/topics/providers/document-provider#overview
class DecentralizedContentProvider : DocumentsProvider() {
    private val ROOT = "de.selfmade4u.decentralized_storage.documents.root"
    private val baseDir = "."

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
            add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, getDocIdForFile(baseDir))

            // The child MIME types are used to filter the roots and only present to the
            // user those roots that contain the desired type somewhere in their file hierarchy.
            add(DocumentsContract.Root.COLUMN_MIME_TYPES, getChildMimeTypes(baseDir))
            add(DocumentsContract.Root.COLUMN_AVAILABLE_BYTES, 1000_000_000)
            add(DocumentsContract.Root.COLUMN_ICON, R.mipmap.ic_launcher)
        }

        return result
    }

    private fun getChildMimeTypes(baseDir: String): Any? {
        return "text/plain"
    }

    private fun getDocIdForFile(baseDir: String): Any? {
        return "test"
    }

    private fun isUserLoggedIn(): Boolean {
        return true
    }

    private fun resolveRootProjection(projection: Array<out String>?): Array<out String>? {
        return projection ?: DEFAULT_ROOT_PROJECTION
    }

    override fun queryDocument(documentId: String?, projection: Array<out String>?): Cursor {
        // Use a MatrixCursor to build a cursor
        // with either the requested fields, or the default
        // projection if "projection" is null.
        val result = MatrixCursor(DEFAULT_ROOT_PROJECTION)

        // If user is not logged in, return an empty root cursor.  This removes our
        // provider from the list entirely.
        //if (!isUserLoggedIn()) {
        //    return result
        //}

        // It's possible to have multiple roots (e.g. for multiple accounts in the
        // same app) -- just add multiple cursor rows.
        result.newRow().apply {
            // FLAG_SUPPORTS_CREATE means at least one directory under the root supports
            // creating documents. FLAG_SUPPORTS_RECENTS means your application's most
            // recently used documents will show up in the "Recents" category.
            // FLAG_SUPPORTS_SEARCH allows users to search all documents the application
            // shares.
            add(
                    DocumentsContract.Document.COLUMN_FLAGS,
                    DocumentsContract.Document.FLAG_SUPPORTS_DELETE
            )

            // COLUMN_TITLE is the root title (e.g. Gallery, Drive).
            add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, "Your Public Key")

            // This document id cannot change after it's shared.
            add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, "de.selfmade4u.decentralized_storage.documents.root.public-key")

            // The child MIME types are used to filter the roots and only present to the
            // user those roots that contain the desired type somewhere in their file hierarchy.
            add(DocumentsContract.Document.COLUMN_MIME_TYPE, "image/png")
            add(DocumentsContract.Document.COLUMN_SIZE, 1_000_000)
            add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, 0)
        }

        return result
    }

    override fun queryChildDocuments(parentDocumentId: String?, projection: Array<out String>?, sortOrder: String?): Cursor {
        // Use a MatrixCursor to build a cursor
        // with either the requested fields, or the default
        // projection if "projection" is null.
        val result = MatrixCursor(DEFAULT_DOCUMENT_PROJECTION)

        // If user is not logged in, return an empty root cursor.  This removes our
        // provider from the list entirely.
        //if (!isUserLoggedIn()) {
        //    return result
        //}

        // It's possible to have multiple roots (e.g. for multiple accounts in the
        // same app) -- just add multiple cursor rows.
        result.newRow().apply {
            add(DocumentsContract.Root.COLUMN_ROOT_ID, "Profile")

            // You can provide an optional summary, which helps distinguish roots
            // with the same title. You can also use this field for displaying an
            // user account name.
            add(DocumentsContract.Root.COLUMN_SUMMARY, "Personal Decentralized Storage")

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
            add(DocumentsContract.Root.COLUMN_TITLE, context?.getString(R.string.app_name))

            // This document id cannot change after it's shared.
            add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, "de.selfmade4u.decentralized_storage.documents.root")

            // The child MIME types are used to filter the roots and only present to the
            // user those roots that contain the desired type somewhere in their file hierarchy.
            add(DocumentsContract.Root.COLUMN_MIME_TYPES, "image/png")
            add(DocumentsContract.Root.COLUMN_AVAILABLE_BYTES, 1000_000_000)
            add(DocumentsContract.Root.COLUMN_ICON, R.drawable.ic_launcher_foreground)
        }

        return result
    }

    override fun openDocument(documentId: String?, mode: String?, signal: CancellationSignal?): ParcelFileDescriptor {
        TODO("Not yet implemented")
    }


}