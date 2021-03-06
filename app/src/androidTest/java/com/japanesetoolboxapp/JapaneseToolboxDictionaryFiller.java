package com.japanesetoolboxapp;


import android.os.Environment;
import android.util.Log;

import com.japanesetoolboxapp.ui.SplashScreenActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class JapaneseToolboxDictionaryFiller {

    private static final int NUMBER_WORDS_TO_SEARCH = 500;
    private static final int TIME_BETWEEN_WORD_SEARCHES = 1000;

    @Rule
    public ActivityTestRule<SplashScreenActivity> mActivityTestRule = new ActivityTestRule<>(SplashScreenActivity.class);

    @Test
    public void JapaneseToolboxDictionaryFillerTest() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<String> words = getFrequentWordsFromFile();
        List<String> wordsWithDeletions = findWords(words);
        updateWordsFile(wordsWithDeletions);
    }

    private List<String> getFrequentWordsFromFile() {
        //Find the directory for the SD Card using the API
        File extStg = Environment.getExternalStorageDirectory();
        File file = new File(extStg,"/download/10000mostCommonJapWords.txt");

        List<String> words = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                words.add(line.trim());
            }
            br.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return words;
    }
    private List<String> findWords(List<String> words) {

        List<String> wordsWithDeletions = new ArrayList<>(words);
        int i = 0;
        while (i< NUMBER_WORDS_TO_SEARCH) {

            if (i%50 == 0) updateWordsFile(wordsWithDeletions);

            TestInputStringAndButton("DICT", wordsWithDeletions.get(0));
            Log.i("DictFiller", "Searched for word No. " + Integer.toString(i) + "/"+ words.size() +" in list: "+ wordsWithDeletions.get(0) + ".");
            wordsWithDeletions.remove(0);

            try {
                Thread.sleep(TIME_BETWEEN_WORD_SEARCHES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            i++;
        }

        return wordsWithDeletions;
    }
    private void updateWordsFile(List<String> words) {
        File extStg = android.os.Environment.getExternalStorageDirectory();
        File dir = new File (extStg.getAbsolutePath() + "/download");
        dir.mkdirs();
        File file = new File(dir, "10000mostCommonJapWords.txt");

        try {
            FileOutputStream f = new FileOutputStream(file);

            //Clear the file
            f.write(("").getBytes());

            //Write the words
            PrintWriter pw = new PrintWriter(f);
            for (String word : words) {
                pw.println(word);
            }
            pw.flush();
            pw.close();
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void TestInputStringAndButton(String button_string, String input) {

        ViewInteraction autoCompleteTextView2 = onView(
                allOf(withId(R.id.query), isDisplayed()));
        autoCompleteTextView2.perform(replaceText(input), closeSoftKeyboard());

        if (button_string.equals("DICT")) {
            ViewInteraction button = onView(
                    allOf(withId(R.id.button_dict), withText("DICT"), isDisplayed()));
            button.perform(click());
        }
    }

}
