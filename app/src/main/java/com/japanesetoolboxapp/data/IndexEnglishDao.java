package com.japanesetoolboxapp.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface IndexEnglishDao {
    //For example: https://github.com/googlesamples/android-architecture-components/blob/master/PersistenceContentProviderSample/app/src/main/java/com/example/android/contentprovidersample/data/CheeseDao.java

    //Return number of LatinIndexes in a table
    @Query("SELECT COUNT(*) FROM " + IndexEnglish.TABLE_NAME)
    int count();

    //Insert IndexEnglish into table
    @Insert
    long insert(IndexEnglish IndexEnglish);

    //Insert multiple LatinIndexes into table
    @Insert
    long[] insertAll(List<IndexEnglish> englishIndices);

    //Get all LatinIndexes in the table
    @Query("SELECT * FROM " + IndexEnglish.TABLE_NAME)
    List<IndexEnglish> getAllLatinIndexes();

    //Get a IndexEnglish by Exact query match
    @Query("SELECT * FROM " + IndexEnglish.TABLE_NAME + " WHERE " + IndexEnglish.COLUMN_ENGLISH + " LIKE :query")
    IndexEnglish getLatinIndexByExactLatinQuery(String query);

    //Get a IndexEnglish list by similar latin index query match - see: https://stackoverflow.com/questions/44234644/android-rooms-search-in-string
    @Query("SELECT * FROM " + IndexEnglish.TABLE_NAME + " WHERE " + IndexEnglish.COLUMN_ENGLISH + " LIKE :query  || '%' ")
    List<IndexEnglish> getLatinIndexByStartingLatinQuery(String query);

    //Delete a IndexEnglish by Latin
    @Query("DELETE FROM " + IndexEnglish.TABLE_NAME + " WHERE " + IndexEnglish.COLUMN_ENGLISH + " = :latin")
    int deleteLatinIndexByLatin(String latin);

    @Delete
    void deleteLatinIndexes(IndexEnglish... englishIndices);

    //Update a IndexEnglish by Id
    @Update
    int update(IndexEnglish IndexEnglish);


}
