package com.japanesetoolboxapp.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.japanesetoolboxapp.data.JapaneseToolboxCentralRoomDatabase;
import com.japanesetoolboxapp.data.JapaneseToolboxExtendedRoomDatabase;
import com.japanesetoolboxapp.data.Word;
import com.japanesetoolboxapp.resources.LocaleHelper;
import com.japanesetoolboxapp.resources.Utilities;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class LocalSearchAsyncTask extends AsyncTask<Void, Void, List<Word>> {

    private WeakReference<Context> contextRef;
    private final String mQuery;
    public LocalDictSearchAsyncResponseHandler listener;

    public LocalSearchAsyncTask(Context context, String query, LocalDictSearchAsyncResponseHandler listener) {
        contextRef = new WeakReference<>(context);
        this.mQuery = query;
        this.listener = listener;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }
    @Override
    protected List<Word> doInBackground(Void... voids) {

        List<Word> localMatchingWordsList = new ArrayList<>();
        if (!TextUtils.isEmpty(mQuery)) {
            JapaneseToolboxCentralRoomDatabase japaneseToolboxCentralRoomDatabase = JapaneseToolboxCentralRoomDatabase.getInstance(contextRef.get());
            JapaneseToolboxExtendedRoomDatabase japaneseToolboxExtendedRoomDatabase = JapaneseToolboxExtendedRoomDatabase.getInstance(contextRef.get());
            String language = LocaleHelper.getLanguage(contextRef.get());
            Object[] matchingWordIds = Utilities.getMatchingWordIdsAndDoBasicFiltering(mQuery,
                    japaneseToolboxCentralRoomDatabase, japaneseToolboxExtendedRoomDatabase, language);
            List<Long> matchingWordIdsCentral = (List<Long>) matchingWordIds[0];
            List<Long> matchingWordIdsExtended = (List<Long>) matchingWordIds[1];
            localMatchingWordsList = japaneseToolboxCentralRoomDatabase.getWordListByWordIds(matchingWordIdsCentral);
            localMatchingWordsList.addAll(japaneseToolboxExtendedRoomDatabase.getWordListByWordIds(matchingWordIdsExtended));

        }

        return localMatchingWordsList;
    }

    @Override
    protected void onPostExecute(List<Word> words) {
        super.onPostExecute(words);
        listener.onLocalDictSearchAsyncTaskResultFound(words);
    }

    public interface LocalDictSearchAsyncResponseHandler {
        void onLocalDictSearchAsyncTaskResultFound(List<Word> text);
    }
}
