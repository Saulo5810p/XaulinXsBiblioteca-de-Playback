package com.example.database

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class PlaybackDao_Impl(
  __db: RoomDatabase,
) : PlaybackDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfTrackEntity: EntityInsertAdapter<TrackEntity>

  private val __insertAdapterOfPlaylistEntity: EntityInsertAdapter<PlaylistEntity>

  private val __insertAdapterOfPlaylistTrackCrossRef: EntityInsertAdapter<PlaylistTrackCrossRef>

  private val __deleteAdapterOfPlaylistEntity: EntityDeleteOrUpdateAdapter<PlaylistEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfTrackEntity = object : EntityInsertAdapter<TrackEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `tracks` (`id`,`title`,`artist`,`album`,`durationMs`,`dataPath`,`albumArtUri`,`isFavorite`,`playCount`,`dateAdded`,`codec`,`bitrateKbps`,`sampleRateHz`,`bitsPerSample`,`lyrics`,`folderPath`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: TrackEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.artist)
        statement.bindText(4, entity.album)
        statement.bindLong(5, entity.durationMs)
        statement.bindText(6, entity.dataPath)
        val _tmpAlbumArtUri: String? = entity.albumArtUri
        if (_tmpAlbumArtUri == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpAlbumArtUri)
        }
        val _tmp: Int = if (entity.isFavorite) 1 else 0
        statement.bindLong(8, _tmp.toLong())
        statement.bindLong(9, entity.playCount.toLong())
        statement.bindLong(10, entity.dateAdded)
        statement.bindText(11, entity.codec)
        statement.bindLong(12, entity.bitrateKbps.toLong())
        statement.bindLong(13, entity.sampleRateHz.toLong())
        statement.bindLong(14, entity.bitsPerSample.toLong())
        val _tmpLyrics: String? = entity.lyrics
        if (_tmpLyrics == null) {
          statement.bindNull(15)
        } else {
          statement.bindText(15, _tmpLyrics)
        }
        statement.bindText(16, entity.folderPath)
      }
    }
    this.__insertAdapterOfPlaylistEntity = object : EntityInsertAdapter<PlaylistEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `playlists` (`id`,`name`,`createdAt`) VALUES (nullif(?, 0),?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PlaylistEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindLong(3, entity.createdAt)
      }
    }
    this.__insertAdapterOfPlaylistTrackCrossRef = object :
        EntityInsertAdapter<PlaylistTrackCrossRef>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `playlist_track_cross_ref` (`playlistId`,`trackId`,`orderPosition`) VALUES (?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PlaylistTrackCrossRef) {
        statement.bindLong(1, entity.playlistId)
        statement.bindLong(2, entity.trackId)
        statement.bindLong(3, entity.orderPosition.toLong())
      }
    }
    this.__deleteAdapterOfPlaylistEntity = object : EntityDeleteOrUpdateAdapter<PlaylistEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `playlists` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: PlaylistEntity) {
        statement.bindLong(1, entity.id)
      }
    }
  }

  public override suspend fun insertTracks(tracks: List<TrackEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfTrackEntity.insert(_connection, tracks)
  }

  public override suspend fun insertPlaylist(playlist: PlaylistEntity): Long =
      performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfPlaylistEntity.insertAndReturnId(_connection, playlist)
    _result
  }

  public override suspend fun addTrackToPlaylist(crossRef: PlaylistTrackCrossRef): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfPlaylistTrackCrossRef.insert(_connection, crossRef)
  }

  public override suspend fun deletePlaylist(playlist: PlaylistEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfPlaylistEntity.handle(_connection, playlist)
  }

  public override fun getAllTracks(): Flow<List<TrackEntity>> {
    val _sql: String = "SELECT * FROM tracks ORDER BY title ASC"
    return createFlow(__db, false, arrayOf("tracks")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfArtist: Int = getColumnIndexOrThrow(_stmt, "artist")
        val _columnIndexOfAlbum: Int = getColumnIndexOrThrow(_stmt, "album")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfDataPath: Int = getColumnIndexOrThrow(_stmt, "dataPath")
        val _columnIndexOfAlbumArtUri: Int = getColumnIndexOrThrow(_stmt, "albumArtUri")
        val _columnIndexOfIsFavorite: Int = getColumnIndexOrThrow(_stmt, "isFavorite")
        val _columnIndexOfPlayCount: Int = getColumnIndexOrThrow(_stmt, "playCount")
        val _columnIndexOfDateAdded: Int = getColumnIndexOrThrow(_stmt, "dateAdded")
        val _columnIndexOfCodec: Int = getColumnIndexOrThrow(_stmt, "codec")
        val _columnIndexOfBitrateKbps: Int = getColumnIndexOrThrow(_stmt, "bitrateKbps")
        val _columnIndexOfSampleRateHz: Int = getColumnIndexOrThrow(_stmt, "sampleRateHz")
        val _columnIndexOfBitsPerSample: Int = getColumnIndexOrThrow(_stmt, "bitsPerSample")
        val _columnIndexOfLyrics: Int = getColumnIndexOrThrow(_stmt, "lyrics")
        val _columnIndexOfFolderPath: Int = getColumnIndexOrThrow(_stmt, "folderPath")
        val _result: MutableList<TrackEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TrackEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpArtist: String
          _tmpArtist = _stmt.getText(_columnIndexOfArtist)
          val _tmpAlbum: String
          _tmpAlbum = _stmt.getText(_columnIndexOfAlbum)
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpDataPath: String
          _tmpDataPath = _stmt.getText(_columnIndexOfDataPath)
          val _tmpAlbumArtUri: String?
          if (_stmt.isNull(_columnIndexOfAlbumArtUri)) {
            _tmpAlbumArtUri = null
          } else {
            _tmpAlbumArtUri = _stmt.getText(_columnIndexOfAlbumArtUri)
          }
          val _tmpIsFavorite: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsFavorite).toInt()
          _tmpIsFavorite = _tmp != 0
          val _tmpPlayCount: Int
          _tmpPlayCount = _stmt.getLong(_columnIndexOfPlayCount).toInt()
          val _tmpDateAdded: Long
          _tmpDateAdded = _stmt.getLong(_columnIndexOfDateAdded)
          val _tmpCodec: String
          _tmpCodec = _stmt.getText(_columnIndexOfCodec)
          val _tmpBitrateKbps: Int
          _tmpBitrateKbps = _stmt.getLong(_columnIndexOfBitrateKbps).toInt()
          val _tmpSampleRateHz: Int
          _tmpSampleRateHz = _stmt.getLong(_columnIndexOfSampleRateHz).toInt()
          val _tmpBitsPerSample: Int
          _tmpBitsPerSample = _stmt.getLong(_columnIndexOfBitsPerSample).toInt()
          val _tmpLyrics: String?
          if (_stmt.isNull(_columnIndexOfLyrics)) {
            _tmpLyrics = null
          } else {
            _tmpLyrics = _stmt.getText(_columnIndexOfLyrics)
          }
          val _tmpFolderPath: String
          _tmpFolderPath = _stmt.getText(_columnIndexOfFolderPath)
          _item =
              TrackEntity(_tmpId,_tmpTitle,_tmpArtist,_tmpAlbum,_tmpDurationMs,_tmpDataPath,_tmpAlbumArtUri,_tmpIsFavorite,_tmpPlayCount,_tmpDateAdded,_tmpCodec,_tmpBitrateKbps,_tmpSampleRateHz,_tmpBitsPerSample,_tmpLyrics,_tmpFolderPath)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getFavoriteTracks(): Flow<List<TrackEntity>> {
    val _sql: String = "SELECT * FROM tracks WHERE isFavorite = 1 ORDER BY title ASC"
    return createFlow(__db, false, arrayOf("tracks")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfArtist: Int = getColumnIndexOrThrow(_stmt, "artist")
        val _columnIndexOfAlbum: Int = getColumnIndexOrThrow(_stmt, "album")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfDataPath: Int = getColumnIndexOrThrow(_stmt, "dataPath")
        val _columnIndexOfAlbumArtUri: Int = getColumnIndexOrThrow(_stmt, "albumArtUri")
        val _columnIndexOfIsFavorite: Int = getColumnIndexOrThrow(_stmt, "isFavorite")
        val _columnIndexOfPlayCount: Int = getColumnIndexOrThrow(_stmt, "playCount")
        val _columnIndexOfDateAdded: Int = getColumnIndexOrThrow(_stmt, "dateAdded")
        val _columnIndexOfCodec: Int = getColumnIndexOrThrow(_stmt, "codec")
        val _columnIndexOfBitrateKbps: Int = getColumnIndexOrThrow(_stmt, "bitrateKbps")
        val _columnIndexOfSampleRateHz: Int = getColumnIndexOrThrow(_stmt, "sampleRateHz")
        val _columnIndexOfBitsPerSample: Int = getColumnIndexOrThrow(_stmt, "bitsPerSample")
        val _columnIndexOfLyrics: Int = getColumnIndexOrThrow(_stmt, "lyrics")
        val _columnIndexOfFolderPath: Int = getColumnIndexOrThrow(_stmt, "folderPath")
        val _result: MutableList<TrackEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TrackEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpArtist: String
          _tmpArtist = _stmt.getText(_columnIndexOfArtist)
          val _tmpAlbum: String
          _tmpAlbum = _stmt.getText(_columnIndexOfAlbum)
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpDataPath: String
          _tmpDataPath = _stmt.getText(_columnIndexOfDataPath)
          val _tmpAlbumArtUri: String?
          if (_stmt.isNull(_columnIndexOfAlbumArtUri)) {
            _tmpAlbumArtUri = null
          } else {
            _tmpAlbumArtUri = _stmt.getText(_columnIndexOfAlbumArtUri)
          }
          val _tmpIsFavorite: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsFavorite).toInt()
          _tmpIsFavorite = _tmp != 0
          val _tmpPlayCount: Int
          _tmpPlayCount = _stmt.getLong(_columnIndexOfPlayCount).toInt()
          val _tmpDateAdded: Long
          _tmpDateAdded = _stmt.getLong(_columnIndexOfDateAdded)
          val _tmpCodec: String
          _tmpCodec = _stmt.getText(_columnIndexOfCodec)
          val _tmpBitrateKbps: Int
          _tmpBitrateKbps = _stmt.getLong(_columnIndexOfBitrateKbps).toInt()
          val _tmpSampleRateHz: Int
          _tmpSampleRateHz = _stmt.getLong(_columnIndexOfSampleRateHz).toInt()
          val _tmpBitsPerSample: Int
          _tmpBitsPerSample = _stmt.getLong(_columnIndexOfBitsPerSample).toInt()
          val _tmpLyrics: String?
          if (_stmt.isNull(_columnIndexOfLyrics)) {
            _tmpLyrics = null
          } else {
            _tmpLyrics = _stmt.getText(_columnIndexOfLyrics)
          }
          val _tmpFolderPath: String
          _tmpFolderPath = _stmt.getText(_columnIndexOfFolderPath)
          _item =
              TrackEntity(_tmpId,_tmpTitle,_tmpArtist,_tmpAlbum,_tmpDurationMs,_tmpDataPath,_tmpAlbumArtUri,_tmpIsFavorite,_tmpPlayCount,_tmpDateAdded,_tmpCodec,_tmpBitrateKbps,_tmpSampleRateHz,_tmpBitsPerSample,_tmpLyrics,_tmpFolderPath)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTrackById(id: Long): TrackEntity? {
    val _sql: String = "SELECT * FROM tracks WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfArtist: Int = getColumnIndexOrThrow(_stmt, "artist")
        val _columnIndexOfAlbum: Int = getColumnIndexOrThrow(_stmt, "album")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfDataPath: Int = getColumnIndexOrThrow(_stmt, "dataPath")
        val _columnIndexOfAlbumArtUri: Int = getColumnIndexOrThrow(_stmt, "albumArtUri")
        val _columnIndexOfIsFavorite: Int = getColumnIndexOrThrow(_stmt, "isFavorite")
        val _columnIndexOfPlayCount: Int = getColumnIndexOrThrow(_stmt, "playCount")
        val _columnIndexOfDateAdded: Int = getColumnIndexOrThrow(_stmt, "dateAdded")
        val _columnIndexOfCodec: Int = getColumnIndexOrThrow(_stmt, "codec")
        val _columnIndexOfBitrateKbps: Int = getColumnIndexOrThrow(_stmt, "bitrateKbps")
        val _columnIndexOfSampleRateHz: Int = getColumnIndexOrThrow(_stmt, "sampleRateHz")
        val _columnIndexOfBitsPerSample: Int = getColumnIndexOrThrow(_stmt, "bitsPerSample")
        val _columnIndexOfLyrics: Int = getColumnIndexOrThrow(_stmt, "lyrics")
        val _columnIndexOfFolderPath: Int = getColumnIndexOrThrow(_stmt, "folderPath")
        val _result: TrackEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpArtist: String
          _tmpArtist = _stmt.getText(_columnIndexOfArtist)
          val _tmpAlbum: String
          _tmpAlbum = _stmt.getText(_columnIndexOfAlbum)
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpDataPath: String
          _tmpDataPath = _stmt.getText(_columnIndexOfDataPath)
          val _tmpAlbumArtUri: String?
          if (_stmt.isNull(_columnIndexOfAlbumArtUri)) {
            _tmpAlbumArtUri = null
          } else {
            _tmpAlbumArtUri = _stmt.getText(_columnIndexOfAlbumArtUri)
          }
          val _tmpIsFavorite: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsFavorite).toInt()
          _tmpIsFavorite = _tmp != 0
          val _tmpPlayCount: Int
          _tmpPlayCount = _stmt.getLong(_columnIndexOfPlayCount).toInt()
          val _tmpDateAdded: Long
          _tmpDateAdded = _stmt.getLong(_columnIndexOfDateAdded)
          val _tmpCodec: String
          _tmpCodec = _stmt.getText(_columnIndexOfCodec)
          val _tmpBitrateKbps: Int
          _tmpBitrateKbps = _stmt.getLong(_columnIndexOfBitrateKbps).toInt()
          val _tmpSampleRateHz: Int
          _tmpSampleRateHz = _stmt.getLong(_columnIndexOfSampleRateHz).toInt()
          val _tmpBitsPerSample: Int
          _tmpBitsPerSample = _stmt.getLong(_columnIndexOfBitsPerSample).toInt()
          val _tmpLyrics: String?
          if (_stmt.isNull(_columnIndexOfLyrics)) {
            _tmpLyrics = null
          } else {
            _tmpLyrics = _stmt.getText(_columnIndexOfLyrics)
          }
          val _tmpFolderPath: String
          _tmpFolderPath = _stmt.getText(_columnIndexOfFolderPath)
          _result =
              TrackEntity(_tmpId,_tmpTitle,_tmpArtist,_tmpAlbum,_tmpDurationMs,_tmpDataPath,_tmpAlbumArtUri,_tmpIsFavorite,_tmpPlayCount,_tmpDateAdded,_tmpCodec,_tmpBitrateKbps,_tmpSampleRateHz,_tmpBitsPerSample,_tmpLyrics,_tmpFolderPath)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTrackByPath(path: String): TrackEntity? {
    val _sql: String = "SELECT * FROM tracks WHERE dataPath = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, path)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfArtist: Int = getColumnIndexOrThrow(_stmt, "artist")
        val _columnIndexOfAlbum: Int = getColumnIndexOrThrow(_stmt, "album")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfDataPath: Int = getColumnIndexOrThrow(_stmt, "dataPath")
        val _columnIndexOfAlbumArtUri: Int = getColumnIndexOrThrow(_stmt, "albumArtUri")
        val _columnIndexOfIsFavorite: Int = getColumnIndexOrThrow(_stmt, "isFavorite")
        val _columnIndexOfPlayCount: Int = getColumnIndexOrThrow(_stmt, "playCount")
        val _columnIndexOfDateAdded: Int = getColumnIndexOrThrow(_stmt, "dateAdded")
        val _columnIndexOfCodec: Int = getColumnIndexOrThrow(_stmt, "codec")
        val _columnIndexOfBitrateKbps: Int = getColumnIndexOrThrow(_stmt, "bitrateKbps")
        val _columnIndexOfSampleRateHz: Int = getColumnIndexOrThrow(_stmt, "sampleRateHz")
        val _columnIndexOfBitsPerSample: Int = getColumnIndexOrThrow(_stmt, "bitsPerSample")
        val _columnIndexOfLyrics: Int = getColumnIndexOrThrow(_stmt, "lyrics")
        val _columnIndexOfFolderPath: Int = getColumnIndexOrThrow(_stmt, "folderPath")
        val _result: TrackEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpArtist: String
          _tmpArtist = _stmt.getText(_columnIndexOfArtist)
          val _tmpAlbum: String
          _tmpAlbum = _stmt.getText(_columnIndexOfAlbum)
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpDataPath: String
          _tmpDataPath = _stmt.getText(_columnIndexOfDataPath)
          val _tmpAlbumArtUri: String?
          if (_stmt.isNull(_columnIndexOfAlbumArtUri)) {
            _tmpAlbumArtUri = null
          } else {
            _tmpAlbumArtUri = _stmt.getText(_columnIndexOfAlbumArtUri)
          }
          val _tmpIsFavorite: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsFavorite).toInt()
          _tmpIsFavorite = _tmp != 0
          val _tmpPlayCount: Int
          _tmpPlayCount = _stmt.getLong(_columnIndexOfPlayCount).toInt()
          val _tmpDateAdded: Long
          _tmpDateAdded = _stmt.getLong(_columnIndexOfDateAdded)
          val _tmpCodec: String
          _tmpCodec = _stmt.getText(_columnIndexOfCodec)
          val _tmpBitrateKbps: Int
          _tmpBitrateKbps = _stmt.getLong(_columnIndexOfBitrateKbps).toInt()
          val _tmpSampleRateHz: Int
          _tmpSampleRateHz = _stmt.getLong(_columnIndexOfSampleRateHz).toInt()
          val _tmpBitsPerSample: Int
          _tmpBitsPerSample = _stmt.getLong(_columnIndexOfBitsPerSample).toInt()
          val _tmpLyrics: String?
          if (_stmt.isNull(_columnIndexOfLyrics)) {
            _tmpLyrics = null
          } else {
            _tmpLyrics = _stmt.getText(_columnIndexOfLyrics)
          }
          val _tmpFolderPath: String
          _tmpFolderPath = _stmt.getText(_columnIndexOfFolderPath)
          _result =
              TrackEntity(_tmpId,_tmpTitle,_tmpArtist,_tmpAlbum,_tmpDurationMs,_tmpDataPath,_tmpAlbumArtUri,_tmpIsFavorite,_tmpPlayCount,_tmpDateAdded,_tmpCodec,_tmpBitrateKbps,_tmpSampleRateHz,_tmpBitsPerSample,_tmpLyrics,_tmpFolderPath)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTracksByFileName(fileName: String): List<TrackEntity> {
    val _sql: String = "SELECT * FROM tracks WHERE dataPath LIKE '%' || ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, fileName)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfArtist: Int = getColumnIndexOrThrow(_stmt, "artist")
        val _columnIndexOfAlbum: Int = getColumnIndexOrThrow(_stmt, "album")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfDataPath: Int = getColumnIndexOrThrow(_stmt, "dataPath")
        val _columnIndexOfAlbumArtUri: Int = getColumnIndexOrThrow(_stmt, "albumArtUri")
        val _columnIndexOfIsFavorite: Int = getColumnIndexOrThrow(_stmt, "isFavorite")
        val _columnIndexOfPlayCount: Int = getColumnIndexOrThrow(_stmt, "playCount")
        val _columnIndexOfDateAdded: Int = getColumnIndexOrThrow(_stmt, "dateAdded")
        val _columnIndexOfCodec: Int = getColumnIndexOrThrow(_stmt, "codec")
        val _columnIndexOfBitrateKbps: Int = getColumnIndexOrThrow(_stmt, "bitrateKbps")
        val _columnIndexOfSampleRateHz: Int = getColumnIndexOrThrow(_stmt, "sampleRateHz")
        val _columnIndexOfBitsPerSample: Int = getColumnIndexOrThrow(_stmt, "bitsPerSample")
        val _columnIndexOfLyrics: Int = getColumnIndexOrThrow(_stmt, "lyrics")
        val _columnIndexOfFolderPath: Int = getColumnIndexOrThrow(_stmt, "folderPath")
        val _result: MutableList<TrackEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TrackEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpArtist: String
          _tmpArtist = _stmt.getText(_columnIndexOfArtist)
          val _tmpAlbum: String
          _tmpAlbum = _stmt.getText(_columnIndexOfAlbum)
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpDataPath: String
          _tmpDataPath = _stmt.getText(_columnIndexOfDataPath)
          val _tmpAlbumArtUri: String?
          if (_stmt.isNull(_columnIndexOfAlbumArtUri)) {
            _tmpAlbumArtUri = null
          } else {
            _tmpAlbumArtUri = _stmt.getText(_columnIndexOfAlbumArtUri)
          }
          val _tmpIsFavorite: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsFavorite).toInt()
          _tmpIsFavorite = _tmp != 0
          val _tmpPlayCount: Int
          _tmpPlayCount = _stmt.getLong(_columnIndexOfPlayCount).toInt()
          val _tmpDateAdded: Long
          _tmpDateAdded = _stmt.getLong(_columnIndexOfDateAdded)
          val _tmpCodec: String
          _tmpCodec = _stmt.getText(_columnIndexOfCodec)
          val _tmpBitrateKbps: Int
          _tmpBitrateKbps = _stmt.getLong(_columnIndexOfBitrateKbps).toInt()
          val _tmpSampleRateHz: Int
          _tmpSampleRateHz = _stmt.getLong(_columnIndexOfSampleRateHz).toInt()
          val _tmpBitsPerSample: Int
          _tmpBitsPerSample = _stmt.getLong(_columnIndexOfBitsPerSample).toInt()
          val _tmpLyrics: String?
          if (_stmt.isNull(_columnIndexOfLyrics)) {
            _tmpLyrics = null
          } else {
            _tmpLyrics = _stmt.getText(_columnIndexOfLyrics)
          }
          val _tmpFolderPath: String
          _tmpFolderPath = _stmt.getText(_columnIndexOfFolderPath)
          _item =
              TrackEntity(_tmpId,_tmpTitle,_tmpArtist,_tmpAlbum,_tmpDurationMs,_tmpDataPath,_tmpAlbumArtUri,_tmpIsFavorite,_tmpPlayCount,_tmpDateAdded,_tmpCodec,_tmpBitrateKbps,_tmpSampleRateHz,_tmpBitsPerSample,_tmpLyrics,_tmpFolderPath)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getPlaylistByName(name: String): PlaylistEntity? {
    val _sql: String = "SELECT * FROM playlists WHERE name = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, name)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: PlaylistEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result = PlaylistEntity(_tmpId,_tmpName,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAllPlaylists(): Flow<List<PlaylistEntity>> {
    val _sql: String = "SELECT * FROM playlists ORDER BY name ASC"
    return createFlow(__db, false, arrayOf("playlists")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<PlaylistEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PlaylistEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item = PlaylistEntity(_tmpId,_tmpName,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getTracksForPlaylist(playlistId: Long): Flow<List<TrackEntity>> {
    val _sql: String = """
        |
        |        SELECT t.* FROM tracks t 
        |        INNER JOIN playlist_track_cross_ref ref ON t.id = ref.trackId 
        |        WHERE ref.playlistId = ? 
        |        ORDER BY ref.orderPosition ASC
        |    
        """.trimMargin()
    return createFlow(__db, false, arrayOf("tracks", "playlist_track_cross_ref")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, playlistId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfArtist: Int = getColumnIndexOrThrow(_stmt, "artist")
        val _columnIndexOfAlbum: Int = getColumnIndexOrThrow(_stmt, "album")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfDataPath: Int = getColumnIndexOrThrow(_stmt, "dataPath")
        val _columnIndexOfAlbumArtUri: Int = getColumnIndexOrThrow(_stmt, "albumArtUri")
        val _columnIndexOfIsFavorite: Int = getColumnIndexOrThrow(_stmt, "isFavorite")
        val _columnIndexOfPlayCount: Int = getColumnIndexOrThrow(_stmt, "playCount")
        val _columnIndexOfDateAdded: Int = getColumnIndexOrThrow(_stmt, "dateAdded")
        val _columnIndexOfCodec: Int = getColumnIndexOrThrow(_stmt, "codec")
        val _columnIndexOfBitrateKbps: Int = getColumnIndexOrThrow(_stmt, "bitrateKbps")
        val _columnIndexOfSampleRateHz: Int = getColumnIndexOrThrow(_stmt, "sampleRateHz")
        val _columnIndexOfBitsPerSample: Int = getColumnIndexOrThrow(_stmt, "bitsPerSample")
        val _columnIndexOfLyrics: Int = getColumnIndexOrThrow(_stmt, "lyrics")
        val _columnIndexOfFolderPath: Int = getColumnIndexOrThrow(_stmt, "folderPath")
        val _result: MutableList<TrackEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TrackEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpArtist: String
          _tmpArtist = _stmt.getText(_columnIndexOfArtist)
          val _tmpAlbum: String
          _tmpAlbum = _stmt.getText(_columnIndexOfAlbum)
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpDataPath: String
          _tmpDataPath = _stmt.getText(_columnIndexOfDataPath)
          val _tmpAlbumArtUri: String?
          if (_stmt.isNull(_columnIndexOfAlbumArtUri)) {
            _tmpAlbumArtUri = null
          } else {
            _tmpAlbumArtUri = _stmt.getText(_columnIndexOfAlbumArtUri)
          }
          val _tmpIsFavorite: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsFavorite).toInt()
          _tmpIsFavorite = _tmp != 0
          val _tmpPlayCount: Int
          _tmpPlayCount = _stmt.getLong(_columnIndexOfPlayCount).toInt()
          val _tmpDateAdded: Long
          _tmpDateAdded = _stmt.getLong(_columnIndexOfDateAdded)
          val _tmpCodec: String
          _tmpCodec = _stmt.getText(_columnIndexOfCodec)
          val _tmpBitrateKbps: Int
          _tmpBitrateKbps = _stmt.getLong(_columnIndexOfBitrateKbps).toInt()
          val _tmpSampleRateHz: Int
          _tmpSampleRateHz = _stmt.getLong(_columnIndexOfSampleRateHz).toInt()
          val _tmpBitsPerSample: Int
          _tmpBitsPerSample = _stmt.getLong(_columnIndexOfBitsPerSample).toInt()
          val _tmpLyrics: String?
          if (_stmt.isNull(_columnIndexOfLyrics)) {
            _tmpLyrics = null
          } else {
            _tmpLyrics = _stmt.getText(_columnIndexOfLyrics)
          }
          val _tmpFolderPath: String
          _tmpFolderPath = _stmt.getText(_columnIndexOfFolderPath)
          _item =
              TrackEntity(_tmpId,_tmpTitle,_tmpArtist,_tmpAlbum,_tmpDurationMs,_tmpDataPath,_tmpAlbumArtUri,_tmpIsFavorite,_tmpPlayCount,_tmpDateAdded,_tmpCodec,_tmpBitrateKbps,_tmpSampleRateHz,_tmpBitsPerSample,_tmpLyrics,_tmpFolderPath)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteDemoTracks() {
    val _sql: String = "DELETE FROM tracks WHERE dataPath LIKE 'http%' OR id >= 9000"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAllTracks() {
    val _sql: String = "DELETE FROM tracks"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun setFavorite(id: Long, isFav: Boolean) {
    val _sql: String = "UPDATE tracks SET isFavorite = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: Int = if (isFav) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        _argIndex = 2
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun incrementPlayCount(id: Long) {
    val _sql: String = "UPDATE tracks SET playCount = playCount + 1 WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateLyrics(id: Long, lyrics: String) {
    val _sql: String = "UPDATE tracks SET lyrics = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, lyrics)
        _argIndex = 2
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
    val _sql: String = "DELETE FROM playlist_track_cross_ref WHERE playlistId = ? AND trackId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, playlistId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, trackId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
