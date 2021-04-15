package com.reactnativemediastore

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.query
import com.facebook.react.bridge.*
import java.util.concurrent.TimeUnit

class MediastoreModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String {
        return "Mediastore"
    }

    private fun mapFiles(
      collection: Uri,
      externalContentUri: Uri,
      idColumn: String,
      nameColumn: String,
      durationColumn: String,
      sizeColumn: String,
      mimeColumn: String,
      titleColumn: String,
      albumColumn: String,
      artistColumn: String
    ): Array<WritableMap> {

      val files = mutableListOf<WritableMap>()

      val projection = arrayOf(
        idColumn,
        nameColumn,
        durationColumn,
        sizeColumn,
        mimeColumn,
        titleColumn,
        albumColumn,
        artistColumn
      )

      val query = reactApplicationContext.contentResolver.query(
        collection,
        projection,
        null,
        null,
        null
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

        while (cursor.moveToNext()) {

          val item = Arguments.createMap()
          val id = cursor.getLong(idColumn)

          item.putInt("id", id.toInt())
          item.putString("name", cursor.getString(nameColumn))
          item.putInt("duration", cursor.getInt(durationColumn))
          item.putInt("size", cursor.getInt(sizeColumn))
          item.putString("mime", cursor.getString(mimeColumn))
          item.putString("title", cursor.getString(titleColumn))
          item.putString("album", cursor.getString(albumColumn))
          item.putString("artist", cursor.getString(artistColumn))

          item.putString("contentUri", "content://media" + externalContentUri.path + "/" + id)

          files += item
        }
      }

      return files.toTypedArray()
    }

    @ReactMethod
    fun readAudioVideoExternalMedias(promise: Promise) {

      data class Media(
        val uri: Uri,
        val name: String,
        val duration: Int,
        val size: Int
      )
      val mediaList = Arguments.createArray()

      mapFiles(
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
        MediaStore.Audio.Media.ARTIST
      ).forEach { file ->
        mediaList.pushMap(file)
      }

      mapFiles(
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
        MediaStore.Video.Media.ARTIST
      ).forEach { file ->
        mediaList.pushMap(file)
      }

      promise.resolve(mediaList)
    }
}
