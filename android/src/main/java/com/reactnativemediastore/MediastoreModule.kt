package com.reactnativemediastore

import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.facebook.react.bridge.*
import java.util.*


class MediastoreModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String {
        return "Mediastore"
    }

    private fun mapFiles(
      path: String,
      existingFolders: MutableMap<String, Boolean>,
      collection: Uri,
      externalContentUri: Uri,
      idColumn: String,
      nameColumn: String,
      durationColumn: String,
      sizeColumn: String,
      mimeColumn: String,
      titleColumn: String,
      albumColumn: String,
      artistColumn: String,
      relPathColumn: String,
      dateTakenColumn: String
    ): Pair<Array<WritableMap>, MutableMap<String, Boolean>> {

      var searchPath: String = path

      if (searchPath.isNotEmpty() && searchPath.first() == '/')
        path.drop(1).also { searchPath = it }

      if (searchPath.isNotEmpty() && searchPath.last() != '/')
        "$searchPath/".also { searchPath = it }

      val searchPathSplits = searchPath.split('/').filter { x -> x.isNotEmpty() }

      val items = mutableListOf<WritableMap>()

      val projection = arrayOf(
        idColumn,
        nameColumn,
        durationColumn,
        sizeColumn,
        mimeColumn,
        titleColumn,
        albumColumn,
        artistColumn,
        relPathColumn
      )

      val selection = if (searchPath != "") "$relPathColumn = ?" else null
      val arguments = if (searchPath != "") arrayOf(searchPath) else null

      val query = reactApplicationContext.contentResolver.query(
        collection,
        projection,
        null,
        null,
        "$dateTakenColumn DESC"
      )

      query?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(idColumn)
        val nameColumn = cursor.getColumnIndexOrThrow(nameColumn)
        val durationColumn = cursor.getColumnIndexOrThrow(durationColumn)
        val sizeColumn = cursor.getColumnIndexOrThrow(sizeColumn)
        val mimeColumn = cursor.getColumnIndexOrThrow(mimeColumn)
        val titleColumn = cursor.getColumnIndexOrThrow(titleColumn)
        val albumColumn = cursor.getColumnIndexOrThrow(albumColumn)
        val artistColumn = cursor.getColumnIndexOrThrow(artistColumn)
        val relPathColumn = cursor.getColumnIndexOrThrow(relPathColumn)

        while (cursor.moveToNext()) {
          val itemPath = cursor.getString(relPathColumn)

          if (itemPath != null && itemPath.isNotEmpty()) {
            val itemPathSplits = itemPath.split('/').filter { x -> x.isNotEmpty() }

            if (itemPath == searchPath) {
              val item = Arguments.createMap()
              val id = cursor.getLong(idColumn)

              item.putString("contentUri", "content://media" + externalContentUri.path + "/" + id)
              item.putInt("id", id.toInt())
              item.putBoolean("isDirectory", false)
              item.putString("name", cursor.getString(nameColumn))
              item.putInt("duration", cursor.getInt(durationColumn))
              item.putInt("size", cursor.getInt(sizeColumn))
              item.putString("mime", cursor.getString(mimeColumn))
              item.putString("title", cursor.getString(titleColumn))
              item.putString("album", cursor.getString(albumColumn))
              item.putString("artist", cursor.getString(artistColumn))
              item.putString("path", itemPath)

              items += item
            } else if(itemPathSplits.size >= searchPathSplits.size + 1 && itemPathSplits.filterIndexed {
                index, s -> if (searchPathSplits.size > index) (searchPathSplits[index] != s) else false
            }.isEmpty()) {
              val id = itemPathSplits[searchPathSplits.size]

              if (existingFolders[id] != true) {
                val item = Arguments.createMap()
                val path = itemPathSplits.take(searchPathSplits.size + 1).joinToString(separator = "/", postfix = "/")

                item.putBoolean("isDirectory", true)
                item.putString("name", id)
                item.putString("path", path)

                existingFolders[id] = true
                items += item
              }
            }
          }
        }
      }

      return items.toTypedArray() to existingFolders
    }

    @ReactMethod
    fun readAudioVideoExternalMedias(path: String = "", promise: Promise) {

      data class Media(
        val uri: Uri,
        val name: String,
        val duration: Int,
        val size: Int
      )

      val mediaList = Arguments.createArray()
      val folders = mutableMapOf<String, Boolean>()

      val (audioFiles, audioFolders) = mapFiles(
        path,
        folders,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
          MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        },
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.DISPLAY_NAME,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.SIZE,
        MediaStore.Audio.Media.MIME_TYPE,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.RELATIVE_PATH,
        MediaStore.Audio.Media.DATE_TAKEN
      )

      audioFiles.forEach { mediaList.pushMap(it) }
      folders += audioFolders

      val (videoFiles, videoFolders) = mapFiles(
        path,
        folders,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
          MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        },
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.DURATION,
        MediaStore.Video.Media.SIZE,
        MediaStore.Video.Media.MIME_TYPE,
        MediaStore.Video.Media.TITLE,
        MediaStore.Video.Media.ALBUM,
        MediaStore.Video.Media.ARTIST,
        MediaStore.Video.Media.RELATIVE_PATH,
        MediaStore.Video.Media.DATE_TAKEN
      )

      videoFiles.forEach { mediaList.pushMap(it) }
      folders += videoFolders

      promise.resolve(mediaList)
    }
}
