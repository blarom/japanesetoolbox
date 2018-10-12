package com.japanesetoolboxapp.ui;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.japanesetoolboxapp.R;
import com.japanesetoolboxapp.data.JapaneseToolboxKanjiRoomDatabase;
import com.japanesetoolboxapp.data.KanjiCharacter;
import com.japanesetoolboxapp.resources.MainApplication;
import com.japanesetoolboxapp.resources.Utilities;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class DecomposeKanjiFragment extends Fragment implements LoaderManager.LoaderCallbacks<Object>{


    //region Parameters
    @BindView(R.id.decompositionsHint) TextView mDecompositionsHint;
    @BindView(R.id.decompositionScrollView) ScrollView mDecompositionScrollView;
    @BindView(R.id.overall_block_container) LinearLayout mOverallBlockContainer;
    @BindView(R.id.decompositions_loading_indicator) ProgressBar mProgressBarLoadingIndicator;
    private Unbinder mBinding;
    private static final int ROOM_DB_KANJI_DECOMPOSITION_LOADER = 7524;
    private static final int ROOM_DB_KANJI_CHARACTER_LOADER = 4732;
    private String mInputQuery;
    private List<String[]> mRadicalsOnlyDatabase;
    private boolean mAlreadyGotFirstQuery;
    private Typeface mDroidSansJapaneseTypeface;
    //endregion


    //Lifecycle Functions
    @Override public void onCreate(Bundle savedInstanceState) { //instead of onActivityCreated
        super.onCreate(savedInstanceState);
        getExtras();
    }
    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_decompose_kanji, container, false);

        //setRetainInstance(true);
        if (getContext()==null) return rootView;

        mBinding = ButterKnife.bind(this, rootView);

        //Setting the Typeface
        AssetManager am = getContext().getApplicationContext().getAssets();
        mDroidSansJapaneseTypeface = Typeface.createFromAsset(am, String.format(Locale.JAPAN, "fonts/%s", "DroidSansJapanese.ttf"));

        getDecomposition();

        return rootView;
    }
    @Override public void onDetach() {
        super.onDetach();
        if (getLoaderManager()!=null) getLoaderManager().destroyLoader(ROOM_DB_KANJI_CHARACTER_LOADER);
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        mBinding.unbind();
        if (getActivity()!=null && MainApplication.getRefWatcher(getActivity())!=null) MainApplication.getRefWatcher(getActivity()).watch(this);
    }


    //Functionality Functions
    private void getExtras() {
        if (getArguments()!=null) {
            mInputQuery = getArguments().getString(getString(R.string.user_query_word));
            mRadicalsOnlyDatabase = (List<String[]>) getArguments().getSerializable(getString(R.string.rad_only_database));
        }
    }
    public void getDecomposition() {

        // Check that the input is valid. If not, return no result.

        String text_type = ConvertFragment.getTextType(mInputQuery);

        if (!TextUtils.isEmpty(mInputQuery) && !text_type.equals("latin") && !text_type.equals("number")) {
            mDecompositionsHint.setVisibility(View.GONE);
            startGettingDecompositionAsynchronously(mInputQuery.substring(0,1), 0);
        }
        else {
            mDecompositionsHint.setVisibility(View.VISIBLE);
        }

    }
    private void displayDecomposition(String inputQuery,
                                      final int radical_iteration,
                                      List<List<String>> decomposedKanji,
                                      List<String> currentKanjiDetailedCharacteristics,
                                      List<String> currentKanjiMainRadicalInfo,
                                      int radicalIndex,
                                      int radicalIndexOriginal,
                                      List<String> currentMainRadicalDetailedCharacteristics) {

        //region Setting up the view
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View decompositionContainer = inflater.inflate(R.layout.fragment_decomposition_element, null);
        final TextView kanjiTV = decompositionContainer.findViewById(R.id.decomposition_element_kanji);
        final ImageView structureIV = decompositionContainer.findViewById(R.id.decomposition_element_structure_image);
        final LinearLayout radicalGalleryLL = decompositionContainer.findViewById(R.id.decomposition_element_radical_gallery);
        final TextView structureTV = decompositionContainer.findViewById(R.id.decomposition_element_structure_value);
        final TextView radicalTitleTV = decompositionContainer.findViewById(R.id.decomposition_element_radical_title);
        final TextView radicalTV = decompositionContainer.findViewById(R.id.decomposition_element_radical_value);
        final TextView onReadingTV = decompositionContainer.findViewById(R.id.decomposition_element_on_readings_value);
        final TextView kunReadingTV = decompositionContainer.findViewById(R.id.decomposition_element_kun_readings_value);
        final TextView onMeaningTV = decompositionContainer.findViewById(R.id.decomposition_element_on_meanings_value);
        final TextView kunMeaningTV = decompositionContainer.findViewById(R.id.decomposition_element_kun__meanings_value);
        //endregion


        //region Initialization
        SpannableString clickable_text;
        Spanned text;
        TextView tv;
        String display_text;
        String[] structure_info;

        if (getActivity()!=null) Utilities.hideSoftKeyboard(getActivity());

        Boolean character_not_found_in_KanjiDictDatabase = false;
        if (currentKanjiDetailedCharacteristics.size() == 0) { character_not_found_in_KanjiDictDatabase = true; }
        Boolean character_is_radical_or_kana = false;
        //endregion

        //region Get the radical characteristics
        String[] radical_row = null;
        if (radicalIndex != -1) {
            radical_row = mRadicalsOnlyDatabase.get(radicalIndex);
            character_is_radical_or_kana = true;
        }
        //endregion

        // If the user clicks on a component further up in the overall_block_container chain, remove the following views
        int childCount = mOverallBlockContainer.getChildCount();
        for (int i=radical_iteration;i<childCount;i++) {
            mOverallBlockContainer.removeViewAt(radical_iteration);
        }

        kanjiTV.setText(decomposedKanji.get(0).get(0));
        kanjiTV.setTypeface(mDroidSansJapaneseTypeface);
        kanjiTV.setTextLocale(Locale.JAPAN);
        structure_info = getStructureInfo(decomposedKanji.get(0).get(1));
        structureIV.setBackgroundResource(Integer.parseInt(structure_info[1]));

        //region Radical gallery
        LinearLayout.LayoutParams radical_gallery_layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        radical_gallery_layoutParams.gravity = Gravity.CENTER_VERTICAL|Gravity.START;
        radical_gallery_layoutParams.setMargins(10, 0, 10, 0);
        for (int i = 1; i < decomposedKanji.size(); i++) {

            if (decomposedKanji.get(i).get(0).equals("")) {break;}

            // Add the component to the layout
            tv = new TextView(getContext());
            tv.setLayoutParams(radical_gallery_layoutParams);
            display_text = decomposedKanji.get(i).get(0);
            text = Utilities.fromHtml("<b><font color='#800080'>" + display_text + "</font></b>");
            clickable_text = new SpannableString(text);
            ClickableSpan Radical_Iteration_ClickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {

                    TextView tv = (TextView) textView;
                    String text = tv.getText().toString();
                    Spanned s = (Spanned) tv.getText();
                    int start = s.getSpanStart(this);
                    int end = s.getSpanEnd(this);

                    startGettingDecompositionAsynchronously(text, radical_iteration+1);
                }
                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            };
            clickable_text.setSpan(Radical_Iteration_ClickableSpan, 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            tv.setText(clickable_text);
            tv.setTypeface(mDroidSansJapaneseTypeface);
            tv.setTextLocale(Locale.JAPAN);
            tv.setTextSize(26);
            tv.setPadding(10,0,10,0);
            tv.setMovementMethod(LinkMovementMethod.getInstance());
            radicalGalleryLL.addView(tv);

            if (i < decomposedKanji.size()-1) {
                tv = new TextView(getContext());
                tv.setText("\u00B7");
                tv.setPadding(10,0,10,0);
                tv.setTextSize(30);
                tv.setTypeface(null, Typeface.BOLD);
                radicalGalleryLL.addView(tv);
            }

        }
        //endregion

        structureTV.setText(structure_info[0]);

        //region Get Meanings and Readings
        if (!character_is_radical_or_kana && character_not_found_in_KanjiDictDatabase && currentKanjiMainRadicalInfo.get(0).equals("")) {
            //Display Characteristics: Meaning of non-Japanese or uncommon Japanese character
            radicalTitleTV.setText(R.string.properties_);
            radicalTV.setText(R.string.this_component_is_a_CJK_character);
        }
        else if (!character_is_radical_or_kana && character_not_found_in_KanjiDictDatabase && !currentKanjiMainRadicalInfo.get(0).equals("")) {
            //Display Main Radical Info only (not readings or meanings)
            radicalTitleTV.setText(R.string.radical_);
            radicalTV.setText(currentKanjiMainRadicalInfo.get(0));
        }
        else if (!character_is_radical_or_kana && !character_not_found_in_KanjiDictDatabase) {
            //Display Main Radical Info (if available)
            if (!currentKanjiMainRadicalInfo.get(0).equals("")) {
                radicalTitleTV.setText(getString(R.string.radical_));
                radicalTV.setText(currentKanjiMainRadicalInfo.get(0));
            }
            else {
                radicalTitleTV.setVisibility(View.GONE);
                radicalTV.setVisibility(View.GONE);
            }

            if (currentKanjiDetailedCharacteristics.size() == 0
                    || currentKanjiDetailedCharacteristics.get(0).equals("")) {
                onReadingTV.setText("-");
                onMeaningTV.setText("-");
                kunReadingTV.setText("-");
                kunMeaningTV.setText("-");
            }
            else {
                onReadingTV.setText(currentKanjiDetailedCharacteristics.get(0).equals("")? "-" : currentKanjiDetailedCharacteristics.get(0));
                onMeaningTV.setText(currentKanjiDetailedCharacteristics.get(2).equals("")? "-" : currentKanjiDetailedCharacteristics.get(2));
                kunReadingTV.setText(currentKanjiDetailedCharacteristics.get(1).equals("")? "-" : currentKanjiDetailedCharacteristics.get(1));
                kunMeaningTV.setText(currentKanjiDetailedCharacteristics.get(3).equals("")? "-" : currentKanjiDetailedCharacteristics.get(3));
            }
        }
        else if (character_is_radical_or_kana) {
            //region Display Radical or Kana Meanings/Readings
            String radicalValue;
            if (radical_row[2].equals("Hiragana") || radical_row[2].equals("Katakana")) {
                radicalValue = radical_row[2] + " " + radical_row[3] + ".";
                radicalTV.setText(radicalValue);
            }
            else if (radical_row[2].equals("Special")){
                radicalValue = "Special symbol with meaning '" + radical_row[3] + "'.";
                radicalTV.setText(radicalValue);
            }
            else {
                //region Get the radical characteristics from the RadialsOnlyDatabase
                List<String> parsed_number = Arrays.asList(mRadicalsOnlyDatabase.get(radicalIndex)[2].split(";"));
                String[] main_radical_row = mRadicalsOnlyDatabase.get(radicalIndexOriginal);

                String strokes = " strokes.";
                if (main_radical_row[4].equals("1")) { strokes = " stroke.";}

                radicalValue = "";
                if (parsed_number.size()>1) {
                    switch (parsed_number.get(1)) {
                        case "alt":
                            radicalValue = "\"" + main_radical_row[3] + "\"" + " (Radical No. " + parsed_number.get(0) + "), " + main_radical_row[4] + strokes;
                            break;
                        case "variant":
                            radicalValue = "\"" + main_radical_row[3] + "\" radical variant" + " (Radical No. " + parsed_number.get(0) + ").";
                            break;
                        case "simplification":
                            radicalValue = "\"" + main_radical_row[3] + "\" (Radical No. " + parsed_number.get(0) + " simplification).";
                            break;
                    }
                }
                else {
                    radicalValue = "\""+ main_radical_row[3] + "\""+ " (Radical No. " + parsed_number.get(0) + "), " + main_radical_row[4] + strokes;
                }
                radicalTV.setText(radicalValue);
                //endregion

                //region Get the remaining radical characteristics (readings, meanings) from the KanjiDictDatabase
                if (currentKanjiDetailedCharacteristics.size() == 0) {
                    currentKanjiDetailedCharacteristics = currentMainRadicalDetailedCharacteristics;
                }

                if (currentKanjiDetailedCharacteristics==null
                        || currentKanjiDetailedCharacteristics.size() == 0
                        || currentKanjiDetailedCharacteristics.get(0).equals("")) {
                    onReadingTV.setText("-");
                    onMeaningTV.setText("-");
                    kunReadingTV.setText("-");
                    kunMeaningTV.setText("-");
                }
                else {
                    onReadingTV.setText(currentKanjiDetailedCharacteristics.get(0).equals("")? "-" : currentKanjiDetailedCharacteristics.get(0));
                    onMeaningTV.setText(currentKanjiDetailedCharacteristics.get(2).equals("")? "-" : currentKanjiDetailedCharacteristics.get(2));
                    kunReadingTV.setText(currentKanjiDetailedCharacteristics.get(1).equals("")? "-" : currentKanjiDetailedCharacteristics.get(1));
                    kunMeaningTV.setText(currentKanjiDetailedCharacteristics.get(3).equals("")? "-" : currentKanjiDetailedCharacteristics.get(3));
                }
                //endregion
            }
            //endregion
        }
        //endregion

        mOverallBlockContainer.addView(decompositionContainer);

        mDecompositionScrollView.fullScroll(View.FOCUS_DOWN);
        mDecompositionScrollView.post(new Runnable() {
            @Override
            public void run() {
                mDecompositionScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }
    static public String[] getStructureInfo(String requested_structure) {
        String structureText = "";
        int structureImage = 0;

        char current_char = 'a';
        char last_char = 'a';
        int starting_index = 0;
        if (requested_structure.equals("c")) {
            structureText = "Character is considered a single component.";
            if (requested_structure.equals("c")) { structureText = "Character is one of the 35 basic CJK strokes.";}
            structureImage = R.drawable.colored_structure_1_original;
        }
        else if (requested_structure.equals("refh") || requested_structure.equals("refr")) {
            structureText = "Component is reflected along a vertical axis (horizontal reflection).";
            structureImage = R.drawable.colored_structure_1_reflected_left_right;
        }
        else if (requested_structure.equals("refv")) {
            structureText = "Component is reflected along a horizontal axis (vertical reflection).";
            structureImage = R.drawable.colored_structure_1_reflected_up_down;
        }
        else if (requested_structure.equals("rot")) {
            structureText = "Component is rotated 180 degrees.";
            structureImage = R.drawable.colored_structure_1_rotation_180;
        }
        else if (requested_structure.equals("w") || requested_structure.equals("wa") || requested_structure.equals("wb") || requested_structure.equals("wbl") || requested_structure.equals("wtr")
                || requested_structure.equals("wtl") || requested_structure.equals("wbr")) {
            structureText = "Second component is located within the first and/or the components overlap.";
            structureImage = R.drawable.colored_structure_2_overlapping;
        }
        else if (requested_structure.equals("a2") || requested_structure.equals("a2m") || requested_structure.equals("a2t")) {
            structureText = "Components are arranged from left to right, and may merge together or touch.";
            structureImage = R.drawable.colored_structure_2_left_right;
        }
        else if (requested_structure.equals("d2") || requested_structure.equals("d2m")|| requested_structure.equals("d2t")) {
            structureText = "Components are arranged from top to bottom, and may merge together or touch.";
            structureImage = R.drawable.colored_structure_2_up_down;
        }
        else if (requested_structure.equals("rrefd")) {
            structureText = "Component is repeated below and its repetition is reflected along a vertical axis (horizontal reflection).";
            structureImage = R.drawable.colored_structure_2_up_down_reflection_up_down;
        }
        else if (requested_structure.equals("rrefl")) {
            structureText = "Component is repeated to the left and its repetition is reflected along a vertical axis (horizontal reflection).";
            structureImage = R.drawable.colored_structure_2_left_right_reflection_on_left;
        }
        else if (requested_structure.equals("rrefr")) {
            structureText = "Component is repeated to the right and its repetition is reflected along a vertical axis (horizontal reflection).";
            structureImage = R.drawable.colored_structure_2_left_right_reflection_on_right;
        }
        else if (requested_structure.equals("rrotd")) {
            structureText = "Component is repeated below and its repetition is reflected along a horizontal axis (vertical reflection).";
            structureImage = R.drawable.colored_structure_2_up_down_rotation_180_on_bottom;
        }
        else if (requested_structure.equals("rrotu")) {
            structureText = "Component is repeated below and its repetition is reflected along a vertical axis (horizontal reflection).";
            structureImage = R.drawable.colored_structure_2_up_down_rotation_180_on_top;
        }
        else if (requested_structure.equals("rrotr")) {
            structureText = "Component is repeated to the right and its repetition is rotated 180 degrees.";
            structureImage = R.drawable.colored_structure_2_left_right_rotation_180_on_right;
        }
        else if (requested_structure.equals("a3")) {
            structureText = "Components are arranged from left to right, and may merge together or touch.";
            structureImage = R.drawable.colored_structure_3_left_center_right;
        }
        else if (requested_structure.equals("a4")) {
            structureText = "Components are arranged from left to right, and may merge together or touch.";
            structureImage = R.drawable.colored_structure_4_left_right;
        }
        else if (requested_structure.equals("d3")) {
            structureText = "Components are arranged from top to bottom, and may merge together or touch.";
            structureImage = R.drawable.colored_structure_3_up_center_down;
        }
        else if (requested_structure.equals("d4")) {
            structureText = "Components are arranged from top to bottom, and may merge together or touch.";
            structureImage = R.drawable.colored_structure_4_up_down;
        }
        else if (requested_structure.equals("r3gw")) {
            structureText = "Component is repeated three times in a triangle pointing downwards.";
            structureImage = R.drawable.colored_structure_3_downwards_triangle;
        }
        else if (requested_structure.equals("r3tr")) {
            structureText = "Component is repeated three times in a triangle pointing upwards.";
            structureImage = R.drawable.colored_structure_3_upwards_triangle;
        }
        else if (requested_structure.equals("4sq")) {
            structureText = "Component is arranged in a square";
            structureImage = R.drawable.colored_structure_4_square;
        }
        else if (requested_structure.equals("r4sq")) {
            structureText = "Component is repeated four times in a square or losange";
            structureImage = R.drawable.colored_structure_4_square_repeat;
        }
        else if (requested_structure.equals("r5")) {
            structureText = "Component is repeated five times.";
            structureImage = R.drawable.colored_structure_5_losange;
        }
        else if (requested_structure.equals("s")) {
            structureText = "First component encloses the others.";
            structureImage = R.drawable.colored_structure_2_outlining;
        }
        else if (requested_structure.equals("sb")) {
            structureText = "First component surrounds the others along the bottom.";
            structureImage = R.drawable.colored_structure_2_enclosing_bottom_to_top;
        }
        else if (requested_structure.equals("sbl")) {
            structureText = "First component surrounds the others along the bottom left.";
            structureImage = R.drawable.colored_structure_2_enclosing_bottomleft_to_topright;
        }
        else if (requested_structure.equals("sbr")) {
            structureText = "First component surrounds the others along the bottom right.";
            structureImage = R.drawable.colored_structure_2_enclosing_bottomright_to_topleft;
        }
        else if (requested_structure.equals("sl")) {
            structureText = "First component surrounds the others along the left.";
            structureImage = R.drawable.colored_structure_2_enclosing_left_to_right;
        }
        else if (requested_structure.equals("sr")) {
            structureText = "First component surrounds the others along the right.";
            structureImage = R.drawable.colored_structure_2_enclosing_right_to_left;
        }
        else if (requested_structure.equals("st") || requested_structure.equals("r3st")) {
            structureText = "First component surrounds the others along the top.";
            structureImage = R.drawable.colored_structure_2_enclosing_top_to_bottom;
        }
        else if (requested_structure.equals("stl") || requested_structure.equals("r3stl")) {
            structureText = "First component surrounds the others along the top left.";
            structureImage = R.drawable.colored_structure_2_enclosing_topleft_to_bottomright;
        }
        else if (requested_structure.equals("str") || requested_structure.equals("r3str")) {
            structureText = "First component surrounds the others along the top right.";
            structureImage = R.drawable.colored_structure_2_enclosing_topright_to_bottomleft;
        }


        if (structureText.equals("")) { structureText = "**No descriptor found**"; }

        String[] structure = {structureText,Integer.toString(structureImage)};
        return structure;
    }

    private void startGettingDecompositionAsynchronously(String inputQuery, int radicalIteration) {
        if (getActivity()!=null) {
            LoaderManager loaderManager = getActivity().getSupportLoaderManager();
            Loader<String> roomDbSearchLoader = loaderManager.getLoader(ROOM_DB_KANJI_DECOMPOSITION_LOADER);
            Bundle bundle = new Bundle();
            bundle.putString(getString(R.string.decomposition_query), inputQuery);
            bundle.putInt(getString(R.string.decomposition_radical_iteration),radicalIteration);
            if (roomDbSearchLoader == null) loaderManager.initLoader(ROOM_DB_KANJI_DECOMPOSITION_LOADER, bundle, this);
            else loaderManager.restartLoader(ROOM_DB_KANJI_DECOMPOSITION_LOADER, bundle, this);
        }
    }
    private void showLoadingIndicator() {
        if (mProgressBarLoadingIndicator!=null) mProgressBarLoadingIndicator.setVisibility(View.VISIBLE);
    }
    private void hideLoadingIndicator() {
        if (mProgressBarLoadingIndicator!=null) mProgressBarLoadingIndicator.setVisibility(View.INVISIBLE);
    }

    //Asynchronous methods
    @NonNull @Override public Loader<Object> onCreateLoader(int id, final Bundle args) {

        if (id == ROOM_DB_KANJI_DECOMPOSITION_LOADER) {
            mAlreadyGotFirstQuery = false;
            showLoadingIndicator();
            String firstQuery = args.getString(getString(R.string.decomposition_query));
            int radicalIteration = args.getInt(getString(R.string.decomposition_radical_iteration));
            KanjiCharacterDecompositionAsyncTaskLoader decompositionLoader = new KanjiCharacterDecompositionAsyncTaskLoader(getContext(), firstQuery, radicalIteration, mRadicalsOnlyDatabase);
            return decompositionLoader;
        }
        else return new KanjiCharacterDecompositionAsyncTaskLoader(getContext(), "", 0, null);
    }
    @Override public void onLoadFinished(@NonNull Loader<Object> loader, Object data) {

        if (loader.getId() == ROOM_DB_KANJI_DECOMPOSITION_LOADER && !mAlreadyGotFirstQuery && data!=null) {
            mAlreadyGotFirstQuery = true;

            Object[] dataElements = (Object[]) data;

            // Search for the input in the database and retrieve the result's characteristics
            List<List<String>> decomposedKanji = (List<List<String>>) dataElements[0];
            List<String> currentKanjiDetailedCharacteristics = (List<String>) dataElements[1];
            List<String> currentKanjiMainRadicalInfo = (List<String>) dataElements[2];
            String inputQuery = (String) dataElements[3];
            int radical_iteration = (int) dataElements[4];
            int radicalIndex = (int) dataElements[5];
            int radicalIndexOriginal = (int) dataElements[6];
            List<String> currentMainRadicalDetailedCharacteristics = (List<String>) dataElements[7];
            hideLoadingIndicator();

            displayDecomposition(inputQuery.substring(0,1),
                    radical_iteration,
                    decomposedKanji,
                    currentKanjiDetailedCharacteristics,
                    currentKanjiMainRadicalInfo,
                    radicalIndex,
                    radicalIndexOriginal,
                    currentMainRadicalDetailedCharacteristics);

            if (getLoaderManager()!=null) getLoaderManager().destroyLoader(ROOM_DB_KANJI_DECOMPOSITION_LOADER);
        }
    }
    @Override public void onLoaderReset(@NonNull Loader<Object> loader) {}
    private static class KanjiCharacterDecompositionAsyncTaskLoader extends AsyncTaskLoader<Object> {

        //region Parameters
        private JapaneseToolboxKanjiRoomDatabase mJapaneseToolboxKanjiRoomDatabase;
        private final String inputQuery;
        private KanjiCharacter mCurrentKanjiCharacter;
        private final List<String[]> mRadicalsOnlyDatabase;
        private final int radicalIteration;
        //endregion

        KanjiCharacterDecompositionAsyncTaskLoader(Context context, String inputQuery, int radicalIteration, List<String[]> mRadicalsOnlyDatabase) {
            super(context);
            this.inputQuery = inputQuery;
            this.radicalIteration = radicalIteration;
            this.mRadicalsOnlyDatabase = mRadicalsOnlyDatabase;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        public Object loadInBackground() {

            mJapaneseToolboxKanjiRoomDatabase = JapaneseToolboxKanjiRoomDatabase.getInstance(getContext());

            // Search for the input in the database and retrieve the result's characteristics

            List<List<String>> decomposedKanji = Decomposition(inputQuery.substring(0,1));
            List<String> currentKanjiDetailedCharacteristics = getKanjiDetailedCharacteristics(mCurrentKanjiCharacter);
            List<String> currentKanjiMainRadicalInfo = getKanjiRadicalCharacteristics(mCurrentKanjiCharacter);
            Object[] radicalInfo = getRadicalInfo();

            return new Object[] {
                    decomposedKanji,
                    currentKanjiDetailedCharacteristics,
                    currentKanjiMainRadicalInfo,
                    inputQuery,
                    radicalIteration,
                    radicalInfo[0],
                    radicalInfo[1],
                    radicalInfo[2]
            };
        }

        List<List<String>> Decomposition(String word) {

            String concatenated_input = Utilities.removeSpecialCharacters(word);
            String inputHexIdentifier = Utilities.convertToUTF8Index(concatenated_input).toUpperCase();
            mCurrentKanjiCharacter = mJapaneseToolboxKanjiRoomDatabase.getKanjiCharacterByHexId(inputHexIdentifier);
            //mCurrentKanjiIndex = Collections.binarySearch(mKanjiCharacters, new KanjiCharacter(inputHexIdentifier), KanjiCharacter.hexIdentiferComparatorAscending);

            List<List<String>> decomposedKanji = new ArrayList<>();
            List<String> kanji_and_its_structure = new ArrayList<>();
            List<String> components_and_their_structure = new ArrayList<>();

            //If decompositions don't exist in the database, then this is a basic character
            if (mCurrentKanjiCharacter ==null) {
                kanji_and_its_structure.add(word);
                kanji_and_its_structure.add("c");
                decomposedKanji.add(kanji_and_its_structure);
            }

            //Otherwise, get the decompositions
            else {

                kanji_and_its_structure.add(getStringFromUTF8(mCurrentKanjiCharacter.getHexIdentifier()));
                kanji_and_its_structure.add(mCurrentKanjiCharacter.getStructure());
                decomposedKanji.add(kanji_and_its_structure);

                List<String> parsedComponents = Arrays.asList(mCurrentKanjiCharacter.getComponents().split(";"));

                String current_component;
                List<List<String>> newDecomposition = new ArrayList<>();

                for (int i = 0; i < parsedComponents.size() ; i++) {
                    current_component = parsedComponents.get(i);
                    components_and_their_structure = new ArrayList<>();

                    if (current_component.length()>0) {
                        if ((current_component.charAt(0) == '0' || current_component.charAt(0) == '1' || current_component.charAt(0) == '2' ||
                                current_component.charAt(0) == '3' || current_component.charAt(0) == '4' || current_component.charAt(0) == '5' ||
                                current_component.charAt(0) == '6' || current_component.charAt(0) == '7' || current_component.charAt(0) == '8' ||
                                current_component.charAt(0) == '9')) {

                            newDecomposition = Decomposition(current_component);

                            // Update the component structures to include the master structure
                            for (int j=1;j<newDecomposition.size();j++) { newDecomposition.get(j).set(1,newDecomposition.get(j).get(1));}

                            // Remove the first List<String> from newDecomposition so that only the decomposed components may be added to decomposedKanji
                            newDecomposition.remove(0);
                            decomposedKanji.addAll(newDecomposition);
                        }
                        else {
                            components_and_their_structure.add(current_component);
                            components_and_their_structure.add("");
                            decomposedKanji.add(components_and_their_structure);
                        }
                    }
                }
            }

            return decomposedKanji;
        }
        String getStringFromUTF8(String word) {

            String hex = word.substring(2,word.length());
            ByteBuffer buff = ByteBuffer.allocate(hex.length()/2);
            for (int i = 0; i < hex.length(); i+=2) {
                buff.put((byte)Integer.parseInt(hex.substring(i, i+2), 16));
            }
            buff.rewind();
            Charset cs = Charset.forName("UTF-8");
            CharBuffer cb = cs.decode(buff);
            String string_value_of_hex = cb.toString();

            return string_value_of_hex;
        }
        List<String> getKanjiDetailedCharacteristics(KanjiCharacter kanjiCharacter) {

            List<String> characteristics = new ArrayList<>();
            if (kanjiCharacter ==null || kanjiCharacter.getReadings()==null) return characteristics;

            characteristics = addElementsToCharacteristics(characteristics, kanjiCharacter.getReadings());
            characteristics = addElementsToCharacteristics(characteristics, kanjiCharacter.getMeanings());

            return characteristics;
        }
        List<String> addElementsToCharacteristics(List<String> characteristics, String list) {
            if (list.equals("") || list.equals(";")) {
                characteristics.add("-");
                characteristics.add("-");
            }
            else if (list.substring(0,1).equals(";")) {
                characteristics.add("-");
                characteristics.add(list.substring(1,list.length()));
            }
            else if (list.substring(list.length()-1).equals(";") || !list.contains(";")) {
                characteristics.add(list.substring(0,list.length()-1));
                characteristics.add("-");
            }
            else {
                String[] parsed_list = list.split(";");
                characteristics.add(parsed_list[0]);
                characteristics.add(parsed_list[1].equals("IDEM")? parsed_list[0] : parsed_list[1]);
            }
            return characteristics;
        }
        List<String> getKanjiRadicalCharacteristics(KanjiCharacter kanjiCharacter) {

            List<String> radical_characteristics = new ArrayList<>();

            if (kanjiCharacter ==null || kanjiCharacter.getRadPlusStrokes()==null) {
                radical_characteristics.add("");
            }
            else {
                List<String> parsed_list = Arrays.asList(kanjiCharacter.getRadPlusStrokes().split("\\+"));

                if (parsed_list.size()>1) {
                    if (!parsed_list.get(1).equals("0")) {
                        int radical_index = -1;
                        for (int i = 0; i < mRadicalsOnlyDatabase.size(); i++) {
                            if (parsed_list.get(0).equals(mRadicalsOnlyDatabase.get(i)[2])) {
                                radical_index = i;
                                break;
                            }
                        }
                        String text = "";
                        if (radical_index != -1) {
                            text = "Character's main radical is " +
                                    mRadicalsOnlyDatabase.get(radical_index)[0] +
                                    " (No. " +
                                    parsed_list.get(0) +
                                    ") with " +
                                    parsed_list.get(1) +
                                    " additional strokes.";
                        }
                        radical_characteristics.add(text);
                    }
                    else {radical_characteristics.add("");}
                }
                else {radical_characteristics.add("");}

            }
            return radical_characteristics;
        }
        Object[] getRadicalInfo() {

            int radicalIndex = -1;
            int mainRadicalIndex = 0;
            List<String> currentMainRadicalDetailedCharacteristics = new ArrayList<>();

            //Find the radical index
            for (int i = 0; i< mRadicalsOnlyDatabase.size(); i++) {
                if (inputQuery.equals(mRadicalsOnlyDatabase.get(i)[0])) {
                    radicalIndex = i;
                }
            }

            if (radicalIndex >= 0) {
                List<String> parsed_number = Arrays.asList(mRadicalsOnlyDatabase.get(radicalIndex)[2].split(";"));
                Boolean found_main_radical = false;
                mainRadicalIndex = radicalIndex;

                if (parsed_number.size() > 1) {
                    while (!found_main_radical) {
                        if (mRadicalsOnlyDatabase.get(mainRadicalIndex)[2].contains(";")) {
                            mainRadicalIndex--;
                        } else {
                            found_main_radical = true;
                        }
                    }
                }

                //Get the remaining radical characteristics (readings, meanings) from the KanjiDictDatabase
                String mainRadical = mRadicalsOnlyDatabase.get(mainRadicalIndex)[0];
                String radicalHexIdentifier = Utilities.convertToUTF8Index(mainRadical).toUpperCase();
                KanjiCharacter kanjiCharacter = mJapaneseToolboxKanjiRoomDatabase.getKanjiCharacterByHexId(radicalHexIdentifier);
                currentMainRadicalDetailedCharacteristics = getKanjiDetailedCharacteristics(kanjiCharacter);

            }
            return new Object[]{
                    radicalIndex,
                    mainRadicalIndex,
                    currentMainRadicalDetailedCharacteristics
            };
        }

    }
}
