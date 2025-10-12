package com.example.medicamentos.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomDatabaseKt;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.IllegalStateException;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TreatmentDao_Impl implements TreatmentDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Treatment> __insertionAdapterOfTreatment;

  private final Converters __converters = new Converters();

  private final EntityInsertionAdapter<MedicationDose> __insertionAdapterOfMedicationDose;

  private final EntityDeletionOrUpdateAdapter<Treatment> __updateAdapterOfTreatment;

  private final EntityDeletionOrUpdateAdapter<MedicationDose> __updateAdapterOfMedicationDose;

  private final SharedSQLiteStatement __preparedStmtOfClearAllTreatments;

  private final SharedSQLiteStatement __preparedStmtOfClearAllDoses;

  private final SharedSQLiteStatement __preparedStmtOfDeleteTreatmentById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteDosesByTreatmentId;

  private final SharedSQLiteStatement __preparedStmtOfDeleteDoseById;

  public TreatmentDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTreatment = new EntityInsertionAdapter<Treatment>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `treatments` (`id`,`medicationName`,`dosage`,`startDate`,`durationInDays`,`frequencyPerDay`,`startHour`,`startMinute`,`intervalHours`,`daysCompleted`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Treatment entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getMedicationName());
        statement.bindString(3, entity.getDosage());
        final Long _tmp = __converters.dateToTimestamp(entity.getStartDate());
        if (_tmp == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, _tmp);
        }
        statement.bindLong(5, entity.getDurationInDays());
        statement.bindLong(6, entity.getFrequencyPerDay());
        statement.bindLong(7, entity.getStartHour());
        statement.bindLong(8, entity.getStartMinute());
        statement.bindLong(9, entity.getIntervalHours());
        statement.bindLong(10, entity.getDaysCompleted());
      }
    };
    this.__insertionAdapterOfMedicationDose = new EntityInsertionAdapter<MedicationDose>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `daily_doses` (`id`,`treatmentId`,`medicationName`,`dosage`,`time`,`date`,`status`,`takenTimestamp`,`postponeCount`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MedicationDose entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getTreatmentId());
        statement.bindString(3, entity.getMedicationName());
        statement.bindString(4, entity.getDosage());
        statement.bindString(5, entity.getTime());
        statement.bindString(6, entity.getDate());
        statement.bindString(7, __MedicationStatus_enumToString(entity.getStatus()));
        if (entity.getTakenTimestamp() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getTakenTimestamp());
        }
        statement.bindLong(9, entity.getPostponeCount());
      }
    };
    this.__updateAdapterOfTreatment = new EntityDeletionOrUpdateAdapter<Treatment>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `treatments` SET `id` = ?,`medicationName` = ?,`dosage` = ?,`startDate` = ?,`durationInDays` = ?,`frequencyPerDay` = ?,`startHour` = ?,`startMinute` = ?,`intervalHours` = ?,`daysCompleted` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Treatment entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getMedicationName());
        statement.bindString(3, entity.getDosage());
        final Long _tmp = __converters.dateToTimestamp(entity.getStartDate());
        if (_tmp == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, _tmp);
        }
        statement.bindLong(5, entity.getDurationInDays());
        statement.bindLong(6, entity.getFrequencyPerDay());
        statement.bindLong(7, entity.getStartHour());
        statement.bindLong(8, entity.getStartMinute());
        statement.bindLong(9, entity.getIntervalHours());
        statement.bindLong(10, entity.getDaysCompleted());
        statement.bindLong(11, entity.getId());
      }
    };
    this.__updateAdapterOfMedicationDose = new EntityDeletionOrUpdateAdapter<MedicationDose>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `daily_doses` SET `id` = ?,`treatmentId` = ?,`medicationName` = ?,`dosage` = ?,`time` = ?,`date` = ?,`status` = ?,`takenTimestamp` = ?,`postponeCount` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MedicationDose entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getTreatmentId());
        statement.bindString(3, entity.getMedicationName());
        statement.bindString(4, entity.getDosage());
        statement.bindString(5, entity.getTime());
        statement.bindString(6, entity.getDate());
        statement.bindString(7, __MedicationStatus_enumToString(entity.getStatus()));
        if (entity.getTakenTimestamp() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getTakenTimestamp());
        }
        statement.bindLong(9, entity.getPostponeCount());
        statement.bindLong(10, entity.getId());
      }
    };
    this.__preparedStmtOfClearAllTreatments = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM treatments";
        return _query;
      }
    };
    this.__preparedStmtOfClearAllDoses = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM daily_doses";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteTreatmentById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM treatments WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteDosesByTreatmentId = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM daily_doses WHERE treatmentId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteDoseById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM daily_doses WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertTreatment(final Treatment treatment,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfTreatment.insertAndReturnId(treatment);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertDose(final MedicationDose dose,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfMedicationDose.insertAndReturnId(dose);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateTreatment(final Treatment treatment,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfTreatment.handle(treatment);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateDose(final MedicationDose dose,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfMedicationDose.handle(dose);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAllData(final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> TreatmentDao.DefaultImpls.clearAllData(TreatmentDao_Impl.this, __cont), $completion);
  }

  @Override
  public Object deleteTreatmentAndDoses(final int treatmentId,
      final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> TreatmentDao.DefaultImpls.deleteTreatmentAndDoses(TreatmentDao_Impl.this, treatmentId, __cont), $completion);
  }

  @Override
  public Object clearAllTreatments(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAllTreatments.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearAllTreatments.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAllDoses(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAllDoses.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearAllDoses.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteTreatmentById(final int treatmentId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteTreatmentById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, treatmentId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteTreatmentById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteDosesByTreatmentId(final int treatmentId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteDosesByTreatmentId.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, treatmentId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteDosesByTreatmentId.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteDoseById(final int doseId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteDoseById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, doseId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteDoseById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Treatment>> getAllTreatments() {
    final String _sql = "SELECT * FROM treatments ORDER BY startDate DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"treatments"}, new Callable<List<Treatment>>() {
      @Override
      @NonNull
      public List<Treatment> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMedicationName = CursorUtil.getColumnIndexOrThrow(_cursor, "medicationName");
          final int _cursorIndexOfDosage = CursorUtil.getColumnIndexOrThrow(_cursor, "dosage");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfDurationInDays = CursorUtil.getColumnIndexOrThrow(_cursor, "durationInDays");
          final int _cursorIndexOfFrequencyPerDay = CursorUtil.getColumnIndexOrThrow(_cursor, "frequencyPerDay");
          final int _cursorIndexOfStartHour = CursorUtil.getColumnIndexOrThrow(_cursor, "startHour");
          final int _cursorIndexOfStartMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "startMinute");
          final int _cursorIndexOfIntervalHours = CursorUtil.getColumnIndexOrThrow(_cursor, "intervalHours");
          final int _cursorIndexOfDaysCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "daysCompleted");
          final List<Treatment> _result = new ArrayList<Treatment>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Treatment _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpMedicationName;
            _tmpMedicationName = _cursor.getString(_cursorIndexOfMedicationName);
            final String _tmpDosage;
            _tmpDosage = _cursor.getString(_cursorIndexOfDosage);
            final Date _tmpStartDate;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfStartDate)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfStartDate);
            }
            final Date _tmp_1 = __converters.fromTimestamp(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.util.Date', but it was NULL.");
            } else {
              _tmpStartDate = _tmp_1;
            }
            final int _tmpDurationInDays;
            _tmpDurationInDays = _cursor.getInt(_cursorIndexOfDurationInDays);
            final int _tmpFrequencyPerDay;
            _tmpFrequencyPerDay = _cursor.getInt(_cursorIndexOfFrequencyPerDay);
            final int _tmpStartHour;
            _tmpStartHour = _cursor.getInt(_cursorIndexOfStartHour);
            final int _tmpStartMinute;
            _tmpStartMinute = _cursor.getInt(_cursorIndexOfStartMinute);
            final int _tmpIntervalHours;
            _tmpIntervalHours = _cursor.getInt(_cursorIndexOfIntervalHours);
            final int _tmpDaysCompleted;
            _tmpDaysCompleted = _cursor.getInt(_cursorIndexOfDaysCompleted);
            _item = new Treatment(_tmpId,_tmpMedicationName,_tmpDosage,_tmpStartDate,_tmpDurationInDays,_tmpFrequencyPerDay,_tmpStartHour,_tmpStartMinute,_tmpIntervalHours,_tmpDaysCompleted);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getTreatmentById(final int id, final Continuation<? super Treatment> $completion) {
    final String _sql = "SELECT * FROM treatments WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Treatment>() {
      @Override
      @Nullable
      public Treatment call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMedicationName = CursorUtil.getColumnIndexOrThrow(_cursor, "medicationName");
          final int _cursorIndexOfDosage = CursorUtil.getColumnIndexOrThrow(_cursor, "dosage");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfDurationInDays = CursorUtil.getColumnIndexOrThrow(_cursor, "durationInDays");
          final int _cursorIndexOfFrequencyPerDay = CursorUtil.getColumnIndexOrThrow(_cursor, "frequencyPerDay");
          final int _cursorIndexOfStartHour = CursorUtil.getColumnIndexOrThrow(_cursor, "startHour");
          final int _cursorIndexOfStartMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "startMinute");
          final int _cursorIndexOfIntervalHours = CursorUtil.getColumnIndexOrThrow(_cursor, "intervalHours");
          final int _cursorIndexOfDaysCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "daysCompleted");
          final Treatment _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpMedicationName;
            _tmpMedicationName = _cursor.getString(_cursorIndexOfMedicationName);
            final String _tmpDosage;
            _tmpDosage = _cursor.getString(_cursorIndexOfDosage);
            final Date _tmpStartDate;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfStartDate)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfStartDate);
            }
            final Date _tmp_1 = __converters.fromTimestamp(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.util.Date', but it was NULL.");
            } else {
              _tmpStartDate = _tmp_1;
            }
            final int _tmpDurationInDays;
            _tmpDurationInDays = _cursor.getInt(_cursorIndexOfDurationInDays);
            final int _tmpFrequencyPerDay;
            _tmpFrequencyPerDay = _cursor.getInt(_cursorIndexOfFrequencyPerDay);
            final int _tmpStartHour;
            _tmpStartHour = _cursor.getInt(_cursorIndexOfStartHour);
            final int _tmpStartMinute;
            _tmpStartMinute = _cursor.getInt(_cursorIndexOfStartMinute);
            final int _tmpIntervalHours;
            _tmpIntervalHours = _cursor.getInt(_cursorIndexOfIntervalHours);
            final int _tmpDaysCompleted;
            _tmpDaysCompleted = _cursor.getInt(_cursorIndexOfDaysCompleted);
            _result = new Treatment(_tmpId,_tmpMedicationName,_tmpDosage,_tmpStartDate,_tmpDurationInDays,_tmpFrequencyPerDay,_tmpStartHour,_tmpStartMinute,_tmpIntervalHours,_tmpDaysCompleted);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MedicationDose>> getDosesForDate(final String date) {
    final String _sql = "SELECT * FROM daily_doses WHERE date = ? ORDER BY time ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, date);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"daily_doses"}, new Callable<List<MedicationDose>>() {
      @Override
      @NonNull
      public List<MedicationDose> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTreatmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "treatmentId");
          final int _cursorIndexOfMedicationName = CursorUtil.getColumnIndexOrThrow(_cursor, "medicationName");
          final int _cursorIndexOfDosage = CursorUtil.getColumnIndexOrThrow(_cursor, "dosage");
          final int _cursorIndexOfTime = CursorUtil.getColumnIndexOrThrow(_cursor, "time");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfTakenTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "takenTimestamp");
          final int _cursorIndexOfPostponeCount = CursorUtil.getColumnIndexOrThrow(_cursor, "postponeCount");
          final List<MedicationDose> _result = new ArrayList<MedicationDose>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MedicationDose _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpTreatmentId;
            _tmpTreatmentId = _cursor.getInt(_cursorIndexOfTreatmentId);
            final String _tmpMedicationName;
            _tmpMedicationName = _cursor.getString(_cursorIndexOfMedicationName);
            final String _tmpDosage;
            _tmpDosage = _cursor.getString(_cursorIndexOfDosage);
            final String _tmpTime;
            _tmpTime = _cursor.getString(_cursorIndexOfTime);
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final MedicationStatus _tmpStatus;
            _tmpStatus = __MedicationStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            final Long _tmpTakenTimestamp;
            if (_cursor.isNull(_cursorIndexOfTakenTimestamp)) {
              _tmpTakenTimestamp = null;
            } else {
              _tmpTakenTimestamp = _cursor.getLong(_cursorIndexOfTakenTimestamp);
            }
            final int _tmpPostponeCount;
            _tmpPostponeCount = _cursor.getInt(_cursorIndexOfPostponeCount);
            _item = new MedicationDose(_tmpId,_tmpTreatmentId,_tmpMedicationName,_tmpDosage,_tmpTime,_tmpDate,_tmpStatus,_tmpTakenTimestamp,_tmpPostponeCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object countDosesForDate(final String date,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(id) FROM daily_doses WHERE date = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, date);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getDosesForTreatmentOnDate(final int treatmentId, final String date,
      final Continuation<? super List<MedicationDose>> $completion) {
    final String _sql = "SELECT * FROM daily_doses WHERE treatmentId = ? AND date = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, treatmentId);
    _argIndex = 2;
    _statement.bindString(_argIndex, date);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MedicationDose>>() {
      @Override
      @NonNull
      public List<MedicationDose> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTreatmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "treatmentId");
          final int _cursorIndexOfMedicationName = CursorUtil.getColumnIndexOrThrow(_cursor, "medicationName");
          final int _cursorIndexOfDosage = CursorUtil.getColumnIndexOrThrow(_cursor, "dosage");
          final int _cursorIndexOfTime = CursorUtil.getColumnIndexOrThrow(_cursor, "time");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfTakenTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "takenTimestamp");
          final int _cursorIndexOfPostponeCount = CursorUtil.getColumnIndexOrThrow(_cursor, "postponeCount");
          final List<MedicationDose> _result = new ArrayList<MedicationDose>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MedicationDose _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpTreatmentId;
            _tmpTreatmentId = _cursor.getInt(_cursorIndexOfTreatmentId);
            final String _tmpMedicationName;
            _tmpMedicationName = _cursor.getString(_cursorIndexOfMedicationName);
            final String _tmpDosage;
            _tmpDosage = _cursor.getString(_cursorIndexOfDosage);
            final String _tmpTime;
            _tmpTime = _cursor.getString(_cursorIndexOfTime);
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final MedicationStatus _tmpStatus;
            _tmpStatus = __MedicationStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            final Long _tmpTakenTimestamp;
            if (_cursor.isNull(_cursorIndexOfTakenTimestamp)) {
              _tmpTakenTimestamp = null;
            } else {
              _tmpTakenTimestamp = _cursor.getLong(_cursorIndexOfTakenTimestamp);
            }
            final int _tmpPostponeCount;
            _tmpPostponeCount = _cursor.getInt(_cursorIndexOfPostponeCount);
            _item = new MedicationDose(_tmpId,_tmpTreatmentId,_tmpMedicationName,_tmpDosage,_tmpTime,_tmpDate,_tmpStatus,_tmpTakenTimestamp,_tmpPostponeCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getDoseById(final int doseId,
      final Continuation<? super MedicationDose> $completion) {
    final String _sql = "SELECT * FROM daily_doses WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, doseId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<MedicationDose>() {
      @Override
      @Nullable
      public MedicationDose call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTreatmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "treatmentId");
          final int _cursorIndexOfMedicationName = CursorUtil.getColumnIndexOrThrow(_cursor, "medicationName");
          final int _cursorIndexOfDosage = CursorUtil.getColumnIndexOrThrow(_cursor, "dosage");
          final int _cursorIndexOfTime = CursorUtil.getColumnIndexOrThrow(_cursor, "time");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfTakenTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "takenTimestamp");
          final int _cursorIndexOfPostponeCount = CursorUtil.getColumnIndexOrThrow(_cursor, "postponeCount");
          final MedicationDose _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpTreatmentId;
            _tmpTreatmentId = _cursor.getInt(_cursorIndexOfTreatmentId);
            final String _tmpMedicationName;
            _tmpMedicationName = _cursor.getString(_cursorIndexOfMedicationName);
            final String _tmpDosage;
            _tmpDosage = _cursor.getString(_cursorIndexOfDosage);
            final String _tmpTime;
            _tmpTime = _cursor.getString(_cursorIndexOfTime);
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final MedicationStatus _tmpStatus;
            _tmpStatus = __MedicationStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            final Long _tmpTakenTimestamp;
            if (_cursor.isNull(_cursorIndexOfTakenTimestamp)) {
              _tmpTakenTimestamp = null;
            } else {
              _tmpTakenTimestamp = _cursor.getLong(_cursorIndexOfTakenTimestamp);
            }
            final int _tmpPostponeCount;
            _tmpPostponeCount = _cursor.getInt(_cursorIndexOfPostponeCount);
            _result = new MedicationDose(_tmpId,_tmpTreatmentId,_tmpMedicationName,_tmpDosage,_tmpTime,_tmpDate,_tmpStatus,_tmpTakenTimestamp,_tmpPostponeCount);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MedicationDose>> getPastDosesHistory() {
    final String _sql = "SELECT * FROM daily_doses WHERE status IN ('TAKEN', 'MISSED') ORDER BY date DESC, time DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"daily_doses"}, new Callable<List<MedicationDose>>() {
      @Override
      @NonNull
      public List<MedicationDose> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTreatmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "treatmentId");
          final int _cursorIndexOfMedicationName = CursorUtil.getColumnIndexOrThrow(_cursor, "medicationName");
          final int _cursorIndexOfDosage = CursorUtil.getColumnIndexOrThrow(_cursor, "dosage");
          final int _cursorIndexOfTime = CursorUtil.getColumnIndexOrThrow(_cursor, "time");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfTakenTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "takenTimestamp");
          final int _cursorIndexOfPostponeCount = CursorUtil.getColumnIndexOrThrow(_cursor, "postponeCount");
          final List<MedicationDose> _result = new ArrayList<MedicationDose>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MedicationDose _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpTreatmentId;
            _tmpTreatmentId = _cursor.getInt(_cursorIndexOfTreatmentId);
            final String _tmpMedicationName;
            _tmpMedicationName = _cursor.getString(_cursorIndexOfMedicationName);
            final String _tmpDosage;
            _tmpDosage = _cursor.getString(_cursorIndexOfDosage);
            final String _tmpTime;
            _tmpTime = _cursor.getString(_cursorIndexOfTime);
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final MedicationStatus _tmpStatus;
            _tmpStatus = __MedicationStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            final Long _tmpTakenTimestamp;
            if (_cursor.isNull(_cursorIndexOfTakenTimestamp)) {
              _tmpTakenTimestamp = null;
            } else {
              _tmpTakenTimestamp = _cursor.getLong(_cursorIndexOfTakenTimestamp);
            }
            final int _tmpPostponeCount;
            _tmpPostponeCount = _cursor.getInt(_cursorIndexOfPostponeCount);
            _item = new MedicationDose(_tmpId,_tmpTreatmentId,_tmpMedicationName,_tmpDosage,_tmpTime,_tmpDate,_tmpStatus,_tmpTakenTimestamp,_tmpPostponeCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getPendingDosesForDate(final String date,
      final Continuation<? super List<MedicationDose>> $completion) {
    final String _sql = "SELECT * FROM daily_doses WHERE date = ? AND status = 'PENDING'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, date);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MedicationDose>>() {
      @Override
      @NonNull
      public List<MedicationDose> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTreatmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "treatmentId");
          final int _cursorIndexOfMedicationName = CursorUtil.getColumnIndexOrThrow(_cursor, "medicationName");
          final int _cursorIndexOfDosage = CursorUtil.getColumnIndexOrThrow(_cursor, "dosage");
          final int _cursorIndexOfTime = CursorUtil.getColumnIndexOrThrow(_cursor, "time");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfTakenTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "takenTimestamp");
          final int _cursorIndexOfPostponeCount = CursorUtil.getColumnIndexOrThrow(_cursor, "postponeCount");
          final List<MedicationDose> _result = new ArrayList<MedicationDose>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MedicationDose _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpTreatmentId;
            _tmpTreatmentId = _cursor.getInt(_cursorIndexOfTreatmentId);
            final String _tmpMedicationName;
            _tmpMedicationName = _cursor.getString(_cursorIndexOfMedicationName);
            final String _tmpDosage;
            _tmpDosage = _cursor.getString(_cursorIndexOfDosage);
            final String _tmpTime;
            _tmpTime = _cursor.getString(_cursorIndexOfTime);
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final MedicationStatus _tmpStatus;
            _tmpStatus = __MedicationStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            final Long _tmpTakenTimestamp;
            if (_cursor.isNull(_cursorIndexOfTakenTimestamp)) {
              _tmpTakenTimestamp = null;
            } else {
              _tmpTakenTimestamp = _cursor.getLong(_cursorIndexOfTakenTimestamp);
            }
            final int _tmpPostponeCount;
            _tmpPostponeCount = _cursor.getInt(_cursorIndexOfPostponeCount);
            _item = new MedicationDose(_tmpId,_tmpTreatmentId,_tmpMedicationName,_tmpDosage,_tmpTime,_tmpDate,_tmpStatus,_tmpTakenTimestamp,_tmpPostponeCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private String __MedicationStatus_enumToString(@NonNull final MedicationStatus _value) {
    switch (_value) {
      case PENDING: return "PENDING";
      case TAKEN: return "TAKEN";
      case MISSED: return "MISSED";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private MedicationStatus __MedicationStatus_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "PENDING": return MedicationStatus.PENDING;
      case "TAKEN": return MedicationStatus.TAKEN;
      case "MISSED": return MedicationStatus.MISSED;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
