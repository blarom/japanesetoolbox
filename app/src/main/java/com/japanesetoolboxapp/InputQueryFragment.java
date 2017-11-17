package com.japanesetoolboxapp;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

public class InputQueryFragment extends Fragment {
    
    // Fragment Functions
		private	String[] output = {"word","","fast"};
 		//public static View GlobalInputQueryFragment;
		String[] queryHistory;
		ArrayList<String> new_queryHistory;
        //View InputQueryFragment;

    // Fragment Lifecycle Functions
	 	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	 		super.onCreateView(inflater, container, savedInstanceState);

            setRetainInstance(true);
            final View InputQueryFragment = inflater.inflate(R.layout.fragment_inputquerymodule, container, false);

            // Resetting the buttons
                final Button button_searchVerb = InputQueryFragment.findViewById(R.id.button_searchVerb);
                button_searchVerb.getBackground().clearColorFilter();
                final Button button_searchWord = InputQueryFragment.findViewById(R.id.button_searchWord);
                button_searchWord.getBackground().clearColorFilter();
                final Button button_choose_Convert = InputQueryFragment.findViewById(R.id.button_convert);
                button_choose_Convert.getBackground().clearColorFilter();
                final Button button_searchTangorin = InputQueryFragment.findViewById(R.id.button_searchTangorin);
                button_searchTangorin.getBackground().clearColorFilter();
                final Button button_searchByRadical = InputQueryFragment.findViewById(R.id.button_searchByRadical);
                button_searchByRadical.getBackground().clearColorFilter();
                final Button button_Decompose = InputQueryFragment.findViewById(R.id.button_Decompose);
                button_Decompose.getBackground().clearColorFilter();

            button_searchByRadical.setEnabled(true);
            button_Decompose.setEnabled(true);

            // Get the input from the user

		 		;// Initialize inputs and prepare them for listeners below
                AutoCompleteTextView queryInit = (AutoCompleteTextView)InputQueryFragment.findViewById(R.id.query);
                queryInit.setText("");

		 		// Restore inputs from the savedInstanceState (if applicable)
                if (queryHistory == null) {
                    queryHistory = new String[7];
                    for (int i=0;i<queryHistory.length;i++) { queryHistory[i] = ""; } //7 elements in the array
                }
                Log.i("Diagnosis Time", "Loaded Search History.");

			 	final AutoCompleteTextView query = queryInit;
			 		
			 	// Populate the history
                query.setOnTouchListener(new View.OnTouchListener(){
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        //query.showDropDown();
                        query.dismissDropDown();
                        return false;
                    }
                    });
			    	
 				// When Enter is clicked, do the actions described in the following function
					query.setOnEditorActionListener( new EditText.OnEditorActionListener() {
							@Override
							public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            String inputWordString = query.getText().toString();

                            DisplayQueryHistory(inputWordString, query);
                            query.dismissDropDown();


                            button_searchVerb.getBackground().clearColorFilter();
                            button_searchWord.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, Color.GREEN));
                            button_choose_Convert.getBackground().clearColorFilter();
                            button_searchTangorin.getBackground().clearColorFilter();
                            button_searchByRadical.getBackground().clearColorFilter();
                            button_Decompose.getBackground().clearColorFilter();

                            if (MainActivity.heap_size_before_decomposition_loader < MainActivity.decomposition_min_heap_size) {
                                button_Decompose.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, Color.RED));
                            }
                            if (MainActivity.heap_size_before_searchbyradical_loader < MainActivity.components_min_heap_size) {
                                button_searchByRadical.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, Color.RED));
                            }

                            onWordEntered_PerformThisFunction(inputWordString);
                        }
                        else {
                            //query.showDropDown();
                        }
                        return true;
				 	} } );
		 			
				// Alternatively, when a button is clicked, do the actions described in the following function
					button_searchVerb.setOnClickListener( new View.OnClickListener() { public void onClick(View v) {
                        //EditText inputVerbObject = (EditText)fragmentView.findViewById(R.id.input_verb);
                        String inputVerbString = query.getText().toString();

                        DisplayQueryHistory(inputVerbString, query);

                        button_searchVerb.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, Color.GREEN));
                        button_searchWord.getBackground().clearColorFilter();
                        button_choose_Convert.getBackground().clearColorFilter();
                        button_searchTangorin.getBackground().clearColorFilter();
                        button_searchByRadical.getBackground().clearColorFilter();
                        button_Decompose.getBackground().clearColorFilter();

                        // Check if the database has finished loading. If not, make the user wait.
                        while(MainActivity.VerbKanjiConjDatabase == null){
                            new CountDownTimer(500, 500) {
                                public void onFinish() {
                                    // When timer is finished
                                    // Execute your code here
                                }

                                public void onTick(long millisUntilFinished) {
                                    // millisUntilFinished    The amount of time until finished.
                                }
                            }.start();
                        }

                        if (MainActivity.heap_size_before_decomposition_loader < MainActivity.decomposition_min_heap_size) {
                            button_Decompose.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, Color.RED));
                        }
                        if (MainActivity.heap_size_before_searchbyradical_loader < MainActivity.components_min_heap_size) {
                            button_searchByRadical.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, Color.RED));
                        }

                        onVerbEntered_PerformThisFunction(inputVerbString);
		 			} } );

 					button_searchWord.setOnClickListener( new View.OnClickListener() { public void onClick(View v) {
                        String inputWordString = query.getText().toString();

                        DisplayQueryHistory(inputWordString, query);

                        button_searchVerb.getBackground().clearColorFilter();
                        button_searchWord.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, Color.GREEN));
                        button_choose_Convert.getBackground().clearColorFilter();
                        button_searchTangorin.getBackground().clearColorFilter();
                        button_searchByRadical.getBackground().clearColorFilter();
                        button_Decompose.getBackground().clearColorFilter();

                        // Check if the database has finished loading. If not, make the user wait.
                        while(MainActivity.VerbKanjiConjDatabase == null){
                            new CountDownTimer(500, 500) {
                                public void onFinish() {
                                    // When timer is finished
                                    // Execute your code here
                                }

                                public void onTick(long millisUntilFinished) {
                                    // millisUntilFinished    The amount of time until finished.
                                }
                            }.start();
                        }

                        if (MainActivity.heap_size_before_decomposition_loader < MainActivity.decomposition_min_heap_size) {
                            button_Decompose.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, Color.RED));
                        }
                        if (MainActivity.heap_size_before_searchbyradical_loader < MainActivity.components_min_heap_size) {
                            button_searchByRadical.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, Color.RED));
                        }

                        onWordEntered_PerformThisFunction(inputWordString);
		 			} } );

                    button_choose_Convert.setOnClickListener( new View.OnClickListener() { public void onClick(View v) {
                        String inputWordString = query.getText().toString();

                        DisplayQueryHistory(inputWordString, query);

                        button_searchVerb.getBackground().clearColorFilter();
                        button_searchWord.getBackground().clearColorFilter();
                        button_choose_Convert.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, Color.GREEN));
                        button_searchTangorin.getBackground().clearColorFilter();
                        button_searchByRadical.getBackground().clearColorFilter();
                        button_Decompose.getBackground().clearColorFilter();

                        if (MainActivity.heap_size_before_decomposition_loader < MainActivity.decomposition_min_heap_size) {
                            button_Decompose.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, Color.RED));
                        }
                        if (MainActivity.heap_size_before_searchbyradical_loader < MainActivity.components_min_heap_size) {
                            button_searchByRadical.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, Color.RED));
                        }

                        onConvertEntered_PerformThisFunction(inputWordString);
                    } } );

		 			button_searchTangorin.setOnClickListener( new View.OnClickListener() { public void onClick(View v) {
                        // Search using Tangorin
                        String queryString = query.getText().toString();

                        DisplayQueryHistory(queryString, query);

                        button_searchVerb.getBackground().clearColorFilter();
                        button_searchWord.getBackground().clearColorFilter();
                        button_choose_Convert.getBackground().clearColorFilter();
                        button_searchTangorin.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, Color.GREEN));
                        button_searchByRadical.getBackground().clearColorFilter();
                        button_Decompose.getBackground().clearColorFilter();

                        if (MainActivity.heap_size_before_decomposition_loader < MainActivity.decomposition_min_heap_size) {
                            button_Decompose.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, Color.RED));
                        }
                        if (MainActivity.heap_size_before_searchbyradical_loader < MainActivity.components_min_heap_size) {
                            button_searchByRadical.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, Color.RED));
                        }

                        String website = "http://www.tangorin.com/general/" + queryString;
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(website));
                        startActivity(intent);
		 			} } );

                    button_searchByRadical.setOnClickListener( new View.OnClickListener() { public void onClick(View v) {
                        // Break up a Kanji to Radicals

                        String inputWordString = query.getText().toString();

                        DisplayQueryHistory(inputWordString, query);

                        button_searchVerb.getBackground().clearColorFilter();
                        button_searchWord.getBackground().clearColorFilter();
                        button_choose_Convert.getBackground().clearColorFilter();
                        button_searchTangorin.getBackground().clearColorFilter();
                        button_searchByRadical.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, Color.GREEN));
                        button_Decompose.getBackground().clearColorFilter();

                        // Check if the database has finished loading. If not, make the user wait.
                        while(MainActivity.RadicalsOnlyDatabase == null && MainActivity.enough_memory_for_heavy_functions) {
                            new CountDownTimer(200000, 10000) {
                                public void onFinish() {
                                }
                                public void onTick(long millisUntilFinished) {
                                    // millisUntilFinished    The amount of time until finished.
                                }
                            }.start();
                            //heap_size = AvailableMemory();
                        }
                        Log.i("Diagnosis Time","Starting radical module.");

                        // If the app memory is too low to load the radicals and decomposition databases, make the searchByRadical and Decompose buttons inactive
                        if (MainActivity.heap_size_before_searchbyradical_loader < MainActivity.components_min_heap_size) {
                            Toast.makeText(getActivity(), "Sorry, your device does not have enough memory to run this function.", Toast.LENGTH_LONG).show();
                            button_searchByRadical.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, Color.RED));
                        }
                        else {
                            button_searchByRadical.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, Color.GREEN));
                            onSearchByRadicalsEntered_PerformThisFunction(inputWordString);
                        }
                    } } );

                    button_Decompose.setOnClickListener( new View.OnClickListener() { public void onClick(View v) {
                        // Break up a Kanji to Radicals

                        String inputWordString = query.getText().toString();

                        DisplayQueryHistory(inputWordString, query);

                        button_searchByRadical.getBackground().clearColorFilter();
                        button_Decompose.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, Color.GREEN));
                        button_searchVerb.getBackground().clearColorFilter();
                        button_searchWord.getBackground().clearColorFilter();
                        button_choose_Convert.getBackground().clearColorFilter();
                        button_searchTangorin.getBackground().clearColorFilter();

                        // Check if the database has finished loading. If not, make the user wait.
                        while(MainActivity.RadicalsOnlyDatabase == null && MainActivity.enough_memory_for_heavy_functions) {
                            new CountDownTimer(200000, 10000) {
                                public void onFinish() {
                                }
                                public void onTick(long millisUntilFinished) {
                                    // millisUntilFinished    The amount of time until finished.
                                }
                            }.start();
                            //heap_size = AvailableMemory();
                        }
                        Log.i("Diagnosis Time","Starting decomposition module.");

                        // If the app memory is too low to load the radicals and decomposition databases, make the searchByRadical and Decompose buttons inactive
                        if (MainActivity.heap_size_before_searchbyradical_loader < MainActivity.components_min_heap_size) {
                            button_searchByRadical.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, Color.RED));
                        }
                        if (MainActivity.heap_size_before_decomposition_loader < MainActivity.decomposition_min_heap_size) {
                            Toast.makeText(getActivity(), "Sorry, your device does not have enough memory to run this function.", Toast.LENGTH_LONG).show();
                            button_Decompose.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, Color.RED));
                        }
                        else {
                            button_Decompose.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, Color.GREEN));
                            onDecomposeEntered_PerformThisFunction(inputWordString);
                        }
                    } } );

					Button button_ClearQuery = InputQueryFragment.findViewById(R.id.choose_ClearQuery);
					button_ClearQuery.setOnClickListener( new View.OnClickListener() { public void onClick(View v) {
		 				//EditText query = (EditText) GlobalInputQueryFragment.findViewById(R.id.query);
		 				query.setText("");
		 			} } );

					Button button_ShowHistory = InputQueryFragment.findViewById(R.id.choose_ShowHistory);
					button_ShowHistory.setOnClickListener( new View.OnClickListener() { public void onClick(View v) {

                        String queryString = query.getText().toString();
                        DisplayQueryHistory(queryString, query);
                        boolean queryHistoryIsEmpty = true;
                        for (String element : queryHistory) {
                            if (!element.equals("")) { queryHistoryIsEmpty = false; }
                        }
						if (!queryHistoryIsEmpty) {query.showDropDown();}
		 			} } );

			    	
	        return InputQueryFragment;
	    }
	 	@Override public void onAttach(Context context) {
	        super.onAttach(context);
            if (context instanceof UserEnteredQueryListener) {
                userEnteredQueryListener = (UserEnteredQueryListener) context;
            } else {
                throw new ClassCastException(context.toString()
                        + " must implement InputQueryFragment.UserEnteredQueryListener");
            }

	    }
	 	@Override public void onSaveInstanceState(Bundle savedInstanceState) {
	 		super.onSaveInstanceState(savedInstanceState);
	 		
	        EditText query = (EditText)getActivity().findViewById(R.id.query);
	        savedInstanceState.putString("query", query.getText().toString());

	        /*RadioButton radio_FastSearch = (RadioButton)GlobalInputQueryFragment.findViewById(R.id.radio_FastSearch);
	        if (radio_FastSearch.isChecked()) { savedInstanceState.putBoolean("radio_FastSearch", true); }
	        else							  { savedInstanceState.putBoolean("radio_FastSearch", false); }*/
	        
	        savedInstanceState.putStringArray("queryHistory", queryHistory);
	 	}
	    private class QueryInputSpinnerAdapter extends ArrayAdapter<String> {
    	// Code adapted from http://mrbool.com/how-to-customize-spinner-in-android/28286
	    	public QueryInputSpinnerAdapter(Context ctx, int txtViewResourceId, List<String> list) {
	    		super(ctx, txtViewResourceId, list);
	    		}
	    	@Override
	    	public View getDropDownView( int position, View cnvtView, ViewGroup prnt) {
	    		return getCustomView(position, cnvtView, prnt);
	    	}
	    	@Override
	    	public View getView(int pos, View cnvtView, ViewGroup prnt) {
	    		return getCustomView(pos, cnvtView, prnt);
	    	}
	    	public View getCustomView(int position, View convertView, ViewGroup parent) {
	    		
	    		LayoutInflater inflater = LayoutInflater.from(getActivity().getBaseContext());
	    		View mySpinner = inflater.inflate(R.layout.custom_queryhistory_spinner, parent, false);
	    		TextView pastquery = (TextView) mySpinner.findViewById(R.id.pastquery);
	    		pastquery.setText(new_queryHistory.get(position));

	    		return mySpinner;
	    	}
	    }
	    public void DisplayQueryHistory(String queryStr, final AutoCompleteTextView query) {
	    							
    		// Implementing a FIFO array for the spinner
    		// Add the entry at the beginning of the stack
			// If the entry is already in the spinner, remove that entry

		    	if (!queryHistory[0].equals(queryStr)) { // if the query hasn't changed, don't change the dropdown list
					String temp;
					int i = queryHistory.length-1;
					while ( i>0 ) {
						temp = queryHistory[i-1];
						queryHistory[i] = temp;
						if (queryHistory[i].equalsIgnoreCase(queryStr)) { queryHistory[i]=""; }
						i--;
					}
					queryHistory[0]=queryStr;
					
					new_queryHistory = new ArrayList<>();
                    for (String aQueryHistory : queryHistory) {
                        if (!aQueryHistory.equals("")) {
                            new_queryHistory.add(aQueryHistory);
                        }
                    }
		    	}

			// Set the dropdown menu to include all past entries
		    	query.setAdapter(new QueryInputSpinnerAdapter(
		    			getActivity().getBaseContext(),
		    			R.layout.custom_queryhistory_spinner,
		    			new_queryHistory));
		    	
		    	//For some reason the following does nothing
		    	query.setOnItemSelectedListener(new OnItemSelectedListener() {
		    		@Override
		    		public void onItemSelected(AdapterView<?> arg0, View arg1,int position, long id) {
		    			String inputWordString = query.getText().toString();
		    			onWordEntered_PerformThisFunction(inputWordString);
		            }
		    		@Override
		    		public void onNothingSelected(AdapterView<?> arg0) { }
		        });
	    }
        public long AvailableMemory() {
            final Runtime runtime = Runtime.getRuntime();
            final long usedMemInMB=(runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
            final long maxHeapSizeInMB=runtime.maxMemory() / 1048576L;
            final long availHeapSizeInMB = maxHeapSizeInMB - usedMemInMB;
            Log.i("Diagnosis Time","Available heap size: " + availHeapSizeInMB);
            return availHeapSizeInMB;
        }
        public void Delay(int milliseconds){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {}
            }, milliseconds);
        }

	// Interface Functions
        private UserEnteredQueryListener userEnteredQueryListener;
	    interface UserEnteredQueryListener {
	    	// Interface used to transfer the verb to VerbModuleFragment or GrammarModuleFragment
	        void OnQueryEnteredSwitchToRelevantFragment(String[] output);
	    }

        public void setNewQuery(String outputFromGrammarModuleFragment) {
            AutoCompleteTextView queryInit = (AutoCompleteTextView) getActivity().findViewById(R.id.query);
            queryInit.setText(outputFromGrammarModuleFragment);
        }

        public void onVerbEntered_PerformThisFunction(String inputVerbString) {

            // Send inputVerbString and SearchType to MainActivity through the interface
            output[0] = "verb";
            output[1] = inputVerbString;
            output[2] = "deep";
            userEnteredQueryListener.OnQueryEnteredSwitchToRelevantFragment(output);
        }
        public void onWordEntered_PerformThisFunction(String inputWordString) {

                /*// Check which Search Type was chosen (Fast or Deep) ************This part is obsolete
                    // Get the ID of the checked radio button in the RadioGroup
                        int selectedId = verbSearchType.getCheckedRadioButtonId();
                    // Return the RadioButton that has the above ID
                        RadioButton chosenVerbSearchType = (RadioButton)fragmentView.findViewById(selectedId);
                    // Define a boolean depending on which radio button was checked
                        boolean checked = chosenVerbSearchType.isChecked();
                    // Define an action depending on the given boolean, ie. depending on the checked RadioButton ID
                        String SearchType = "fast";
                        switch(chosenVerbSearchType.getId()) {
                            case R.id.radio_FastSearch:
                                if (checked) { SearchType = "fast";} break;
                            case R.id.radio_DeepSearch:
                                if (checked) { SearchType = "deep";} break;
                        }*/

            // Send inputWordString and SearchType to MainActivity through the interface
            output[0] = "word";
            output[1] = inputWordString;
            output[2] = "fast";
            userEnteredQueryListener.OnQueryEnteredSwitchToRelevantFragment(output);
        }
        public void onConvertEntered_PerformThisFunction(String inputWordString) {

            // Send inputWordString and SearchType to MainActivity through the interface
            output[0] = "convert";
            output[1] = inputWordString;
            output[2] = "fast";
            userEnteredQueryListener.OnQueryEnteredSwitchToRelevantFragment(output);
        }
        public void onSearchByRadicalsEntered_PerformThisFunction(String inputWordString) {

            // Send inputWordString and SearchType to MainActivity through the interface
            output[0] = "radicals";
            output[1] = inputWordString;
            output[2] = "fast";
            userEnteredQueryListener.OnQueryEnteredSwitchToRelevantFragment(output);
        }
        public void onDecomposeEntered_PerformThisFunction(String inputWordString) {

            // Send inputWordString and SearchType to MainActivity through the interface
            output[0] = "decompose";
            output[1] = inputWordString;
            output[2] = "fast";
            userEnteredQueryListener.OnQueryEnteredSwitchToRelevantFragment(output);
        }

}