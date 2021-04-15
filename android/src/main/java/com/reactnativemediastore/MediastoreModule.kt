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

    @ReactMethod
    fun readAudioVideoExternalMedias(promise: Promise) {

      data class Media(val uri: Uri,
                       val name: String,
                       val duration: Int,
                       val size: Int
      )
      val mediaList = Arguments.createArray()

      val collection =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          MediaStore.Audio.Media.getContentUri(
            MediaStore.VOLUME_EXTERNAL
          )
        } else {
          MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

      val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.DISPLAY_NAME,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.SIZE
      )

      val query = reactApplicationContext.contentResolver.query(
        collection,
        projection,
        null,
        null,
        null
      )
      val use = query?.use { cursor ->
        // Cache column indices.
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val nameColumn =
          cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
        val durationColumn =
          cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
        val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

        while (cursor.moveToNext()) {

          val item = Arguments.createMap()
          val id = cursor.getLong(idColumn)

          item.putInt("id", id.toInt())
          item.putString("name", cursor.getString(nameColumn))
          item.putInt("duration", cursor.getInt(durationColumn))
          item.putInt("size", cursor.getInt(sizeColumn))

          item.putString("contentUri", ContentUris.withAppendedId(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            id
          ).path)

          mediaList.pushMap(item)
        }
      }

      promise.resolve(mediaList)
    }
}
