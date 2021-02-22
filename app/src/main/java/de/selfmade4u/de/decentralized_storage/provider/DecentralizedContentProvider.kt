package de.selfmade4u.de.decentralized_storage.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.DocumentsProvider
import de.selfmade4u.de.decentralized_storage.R

// https://developer.android.com/reference/android/provider/DocumentsProvider
// https://developer.android.com/guide/topics/providers/create-document-provider
// https://developer.android.com/guide/topics/providers/document-provider#overview
class DecentralizedContentProvider : DocumentsProvider() {
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
        return true;
    }

    override fun queryRoots(projection: Array<out String>?): Cursor {
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
            add(DocumentsContract.Root.COLUMN_ROOT_ID, "Root")

            // You can provide an optional summary, which helps distinguish roots
            // with the same title. You can also use this field for displaying an
            // user account name.
            add(DocumentsContract.Root.COLUMN_SUMMARY, "Roootsflidh")

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
            add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, "somerandomdocid")

            // The child MIME types are used to filter the roots and only present to the
            // user those roots that contain the desired type somewhere in their file hierarchy.
            add(DocumentsContract.Root.COLUMN_MIME_TYPES, "text/plain")
            add(DocumentsContract.Root.COLUMN_AVAILABLE_BYTES, 1)
            add(DocumentsContract.Root.COLUMN_ICON, R.drawable.ic_launcher_foreground)
        }

        return result
    }

    override fun queryDocument(documentId: String?, projection: Array<out String>?): Cursor {
        TODO("Not yet implemented")
    }

    override fun queryChildDocuments(parentDocumentId: String?, projection: Array<out String>?, sortOrder: String?): Cursor {
        TODO("Not yet implemented")
    }

    override fun openDocument(documentId: String?, mode: String?, signal: CancellationSignal?): ParcelFileDescriptor {
        TODO("Not yet implemented")
    }


}