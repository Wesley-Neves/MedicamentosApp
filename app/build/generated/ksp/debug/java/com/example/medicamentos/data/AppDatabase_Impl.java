package com.example.medicamentos.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile TreatmentDao _treatmentDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(6) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `treatments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `medicationName` TEXT NOT NULL, `dosage` TEXT NOT NULL, `startDate` INTEGER NOT NULL, `durationInDays` INTEGER NOT NULL, `frequencyPerDay` INTEGER NOT NULL, `startHour` INTEGER NOT NULL, `startMinute` INTEGER NOT NULL, `intervalHours` INTEGER NOT NULL, `daysCompleted` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `daily_doses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `treatmentId` INTEGER NOT NULL, `medicationName` TEXT NOT NULL, `dosage` TEXT NOT NULL, `time` TEXT NOT NULL, `date` TEXT NOT NULL, `status` TEXT NOT NULL, `takenTimestamp` INTEGER, `postponeCount` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '3e490863d8b211960173673b597ff9d1')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `treatments`");
        db.execSQL("DROP TABLE IF EXISTS `daily_doses`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsTreatments = new HashMap<String, TableInfo.Column>(10);
        _columnsTreatments.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTreatments.put("medicationName", new TableInfo.Column("medicationName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTreatments.put("dosage", new TableInfo.Column("dosage", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTreatments.put("startDate", new TableInfo.Column("startDate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTreatments.put("durationInDays", new TableInfo.Column("durationInDays", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTreatments.put("frequencyPerDay", new TableInfo.Column("frequencyPerDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTreatments.put("startHour", new TableInfo.Column("startHour", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTreatments.put("startMinute", new TableInfo.Column("startMinute", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTreatments.put("intervalHours", new TableInfo.Column("intervalHours", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTreatments.put("daysCompleted", new TableInfo.Column("daysCompleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTreatments = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTreatments = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTreatments = new TableInfo("treatments", _columnsTreatments, _foreignKeysTreatments, _indicesTreatments);
        final TableInfo _existingTreatments = TableInfo.read(db, "treatments");
        if (!_infoTreatments.equals(_existingTreatments)) {
          return new RoomOpenHelper.ValidationResult(false, "treatments(com.example.medicamentos.data.Treatment).\n"
                  + " Expected:\n" + _infoTreatments + "\n"
                  + " Found:\n" + _existingTreatments);
        }
        final HashMap<String, TableInfo.Column> _columnsDailyDoses = new HashMap<String, TableInfo.Column>(9);
        _columnsDailyDoses.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyDoses.put("treatmentId", new TableInfo.Column("treatmentId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyDoses.put("medicationName", new TableInfo.Column("medicationName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyDoses.put("dosage", new TableInfo.Column("dosage", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyDoses.put("time", new TableInfo.Column("time", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyDoses.put("date", new TableInfo.Column("date", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyDoses.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyDoses.put("takenTimestamp", new TableInfo.Column("takenTimestamp", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyDoses.put("postponeCount", new TableInfo.Column("postponeCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDailyDoses = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDailyDoses = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDailyDoses = new TableInfo("daily_doses", _columnsDailyDoses, _foreignKeysDailyDoses, _indicesDailyDoses);
        final TableInfo _existingDailyDoses = TableInfo.read(db, "daily_doses");
        if (!_infoDailyDoses.equals(_existingDailyDoses)) {
          return new RoomOpenHelper.ValidationResult(false, "daily_doses(com.example.medicamentos.data.MedicationDose).\n"
                  + " Expected:\n" + _infoDailyDoses + "\n"
                  + " Found:\n" + _existingDailyDoses);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "3e490863d8b211960173673b597ff9d1", "0f6873c890f7f1c6aa8c6baa3f83abca");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "treatments","daily_doses");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `treatments`");
      _db.execSQL("DELETE FROM `daily_doses`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(TreatmentDao.class, TreatmentDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public TreatmentDao treatmentDao() {
    if (_treatmentDao != null) {
      return _treatmentDao;
    } else {
      synchronized(this) {
        if(_treatmentDao == null) {
          _treatmentDao = new TreatmentDao_Impl(this);
        }
        return _treatmentDao;
      }
    }
  }
}
