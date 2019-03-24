package com.example.sharedtravel.views;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.sharedtravel.R;
import com.example.sharedtravel.fragments.dialogs.DialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class CitiesSpinner extends MaterialButton implements View.OnClickListener {

    private final SpinnerDialogFragment dialogFragment = new SpinnerDialogFragment();

    private String tag = "cities_view";

    private Context context;
    private OnItemSelectedListener itemSelectedListener;
    private boolean allowMultiple;


    public CitiesSpinner(Context context) {
        super(context);
        init(context);
    }

    public CitiesSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CitiesSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    private List<String> getSelectedCities() {
        return dialogFragment.getChosenCities();
    }

    public void setAllowMultiple(boolean allowMultiple) {
        this.allowMultiple = allowMultiple;
    }

    public void setItemSelectedListener(CitiesSpinner.OnItemSelectedListener itemSelectedListener) {
        this.itemSelectedListener = itemSelectedListener;
    }

    private void init(Context context) {
        this.allowMultiple = false;
        this.context = context;

        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (dialogFragment.isAdded())
            return;
        FragmentManager manager = ((FragmentActivity) context).getSupportFragmentManager();
        dialogFragment.setCancelable(false);
        dialogFragment.initAndShow(manager, allowMultiple, tag, (confirmBtn) -> {
            if (itemSelectedListener != null) {
                this.itemSelectedListener.selectedItem(getSelectedCities());
            }
            dialogFragment.dismiss();
        });

    }

    public interface OnItemSelectedListener {
        void selectedItem(List<String> item);
    }


    public static class SpinnerDialogFragment extends DialogFragment
            implements CitiesSpinner.SpinnerAdapterItemClickedListener {

        private static final String CHOSEN_CITY_SAVED_INSTANCE = "city";

        //CallbackListener for communication between adapter
        private OnClickListener clickListener;
        private LinearLayout chosenCitiesContainer;

        //CONSTANT DATA LOADED FROM STRING RESOURCE
        private String[] cities;

        private boolean allowMultiple;
        private ArrayList<String> chosenCities;
        private TextView chosenCityTextView;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setStyle(DialogFragment.STYLE_NORMAL, R.style.SpinnerDialogTheme);
        }

        //Start point
        void initAndShow(FragmentManager manager, boolean allowMultiple, String tag, View.OnClickListener clickListener) {

            this.allowMultiple = allowMultiple;
            this.clickListener = clickListener;
            this.show(manager, tag);
        }

        private void initView(@NonNull View view) {
            cities = getResources().getStringArray(R.array.cities);
            chosenCities = new ArrayList<>();
            chosenCitiesContainer = view.findViewById(R.id.chosen_city_container);

            /*Start init {@link RecyclerView} */
            RecyclerView recyclerView = view.findViewById(R.id.rv_cities_spinner);
            RecyclerView.Adapter recyclerViewAdapter = new CitiesSpinnerAdapter(cities, allowMultiple, this);
            recyclerView.setAdapter(recyclerViewAdapter);
            //End init {@Link RecyclerView}

            Button cancelBtn = view.findViewById(R.id.sp_btn_cancel);
            cancelBtn.setOnClickListener((v) -> dismiss());

            Button confirmBtn = view.findViewById(R.id.sp_btn_confirm);
            confirmBtn.setOnClickListener(clickListener);

            chosenCityTextView = view.findViewById(R.id.tv_destination_city);

            EditText searchEditText = view.findViewById(R.id.et_search_spinner);
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    ((CitiesSpinnerAdapter) recyclerViewAdapter).getFilter().filter(s);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

        }


        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            setRetainInstance(true);
            View view = inflater.inflate(R.layout.cities_spinner_layout, container);
            initView(view);
            if (savedInstanceState != null) {
                if (!allowMultiple)
                    chosenCityTextView.setText(savedInstanceState.getString(CHOSEN_CITY_SAVED_INSTANCE));

            }

            return view;
        }

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);
            if (chosenCities != null &&
                    chosenCities.size() > 0) {
                if (!allowMultiple)
                    outState.putString(CHOSEN_CITY_SAVED_INSTANCE, chosenCities.get(0));
            }
        }

        //Listener comes from adapterClass
        @Override
        public void itemClicked(View v, CitiesSpinnerAdapter.RemoveTextViewListener listener) {
            if (!allowMultiple) {
                chosenCities.add(0,
                        ((TextView) v).getText().toString());
                chosenCityTextView.setText(chosenCities.get(0));
                chosenCityTextView.setVisibility(View.VISIBLE);
            } else {
                if(chosenCities.size() >= 4 || v == null || listener == null) {
                    DialogBuilder.showErrorMessage(getContext(), getResources().getString(R.string.too_many_intermediate_stops_error));
                    return;
                }
                chosenCities.add(((TextView) v).getText().toString());

                addTextView(((TextView) v).getText().toString(), listener);

            }
        }

        private void addTextView(String text, CitiesSpinnerAdapter.RemoveTextViewListener listener) {
            TextView newTextView = new TextView(getContext());
            TableRow.LayoutParams layoutParams =  new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0,3,0,3);
            newTextView.setLayoutParams(layoutParams);
            newTextView.setGravity(Gravity.CENTER);
            newTextView.setTextColor(getResources().getColor(R.color.colorOnSurface));
            newTextView.setTextSize(15);
            newTextView.setBackgroundResource(R.drawable.spinner_selected_city);
            newTextView.setCompoundDrawablesWithIntrinsicBounds(
                    null, null, getResources().getDrawable(R.drawable.ic_remove), null);
            newTextView.setText(text);
            newTextView.setVisibility(View.VISIBLE);
            newTextView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.oswald));
            newTextView.setOnClickListener((tv) -> {
                tv.setVisibility(View.GONE);
                chosenCities.remove(((TextView) tv).getText().toString());
                listener.removeTextView((TextView) tv);
            });
            chosenCitiesContainer.addView(newTextView);
        }

        //Returns ArrayList with 1 item if not allow multiple
        ArrayList<String> getChosenCities() {
            return chosenCities;
        }

    }



    public interface SpinnerAdapterItemClickedListener {
        void itemClicked(View view, CitiesSpinnerAdapter.RemoveTextViewListener callback);
    }
}
