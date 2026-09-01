package com.example.database

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _playbackDao: Lazy<PlaybackDao> = lazy {
    PlaybackDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1,
        "f78b361270fa8f1445da34875102ddbc", "5f549f039db6d25307f1c78ebd5fa5b6") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `tracks` (`id` INTEGER NOT NULL, `title` TEXT NOT NULL, `artist` TEXT NOT NULL, `album` TEXT NOT NULL, `durationMs` INTEGER NOT NULL, `dataPath` TEXT NOT NULL, `albumArtUri` TEXT, `isFavorite` INTEGER NOT NULL, `playCount` INTEGER NOT NULL, `dateAdded` INTEGER NOT NULL, `codec` TEXT NOT NULL, `bitrateKbps` INTEGER NOT NULL, `sampleRateHz` INTEGER NOT NULL, `bitsPerSample` INTEGER NOT NULL, `lyrics` TEXT, `folderPath` TEXT NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `playlists` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `playlist_track_cross_ref` (`playlistId` INTEGER NOT NULL, `trackId` INTEGER NOT NULL, `orderPosition` INTEGER NOT NULL, PRIMARY KEY(`playlistId`, `trackId`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'f78b361270fa8f1445da34875102ddbc')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `tracks`")
        connection.execSQL("DROP TABLE IF EXISTS `playlists`")
        connection.execSQL("DROP TABLE IF EXISTS `playlist_track_cross_ref`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsTracks: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsTracks.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTracks.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTracks.put("artist", TableInfo.Column("artist", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTracks.put("album", TableInfo.Column("album", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTracks.put("durationMs", TableInfo.Column("durationMs", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTracks.put("dataPath", TableInfo.Column("dataPath", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTracks.put("albumArtUri", TableInfo.Column("albumArtUri", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTracks.put("isFavorite", TableInfo.Column("isFavorite", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTracks.put("playCount", TableInfo.Column("playCount", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTracks.put("dateAdded", TableInfo.Column("dateAdded", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTracks.put("codec", TableInfo.Column("codec", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTracks.put("bitrateKbps", TableInfo.Column("bitrateKbps", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTracks.put("sampleRateHz", TableInfo.Column("sampleRateHz", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTracks.put("bitsPerSample", TableInfo.Column("bitsPerSample", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTracks.put("lyrics", TableInfo.Column("lyrics", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTracks.put("folderPath", TableInfo.Column("folderPath", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysTracks: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesTracks: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoTracks: TableInfo = TableInfo("tracks", _columnsTracks, _foreignKeysTracks,
            _indicesTracks)
        val _existingTracks: TableInfo = read(connection, "tracks")
        if (!_infoTracks.equals(_existingTracks)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |tracks(com.example.database.TrackEntity).
              | Expected:
              |""".trimMargin() + _infoTracks + """
              |
              | Found:
              |""".trimMargin() + _existingTracks)
        }
        val _columnsPlaylists: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsPlaylists.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaylists.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaylists.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysPlaylists: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesPlaylists: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoPlaylists: TableInfo = TableInfo("playlists", _columnsPlaylists,
            _foreignKeysPlaylists, _indicesPlaylists)
        val _existingPlaylists: TableInfo = read(connection, "playlists")
        if (!_infoPlaylists.equals(_existingPlaylists)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |playlists(com.example.database.PlaylistEntity).
              | Expected:
              |""".trimMargin() + _infoPlaylists + """
              |
              | Found:
              |""".trimMargin() + _existingPlaylists)
        }
        val _columnsPlaylistTrackCrossRef: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsPlaylistTrackCrossRef.put("playlistId", TableInfo.Column("playlistId", "INTEGER",
            true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaylistTrackCrossRef.put("trackId", TableInfo.Column("trackId", "INTEGER", true, 2,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaylistTrackCrossRef.put("orderPosition", TableInfo.Column("orderPosition",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysPlaylistTrackCrossRef: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesPlaylistTrackCrossRef: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoPlaylistTrackCrossRef: TableInfo = TableInfo("playlist_track_cross_ref",
            _columnsPlaylistTrackCrossRef, _foreignKeysPlaylistTrackCrossRef,
            _indicesPlaylistTrackCrossRef)
        val _existingPlaylistTrackCrossRef: TableInfo = read(connection, "playlist_track_cross_ref")
        if (!_infoPlaylistTrackCrossRef.equals(_existingPlaylistTrackCrossRef)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |playlist_track_cross_ref(com.example.database.PlaylistTrackCrossRef).
              | Expected:
              |""".trimMargin() + _infoPlaylistTrackCrossRef + """
              |
              | Found:
              |""".trimMargin() + _existingPlaylistTrackCrossRef)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "tracks", "playlists",
        "playlist_track_cross_ref")
  }

  public override fun clearAllTables() {
    super.performClear(false, "tracks", "playlists", "playlist_track_cross_ref")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(PlaybackDao::class, PlaybackDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun playbackDao(): PlaybackDao = _playbackDao.value
}
