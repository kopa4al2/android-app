package com.example.sharedtravel.views;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.sharedtravel.R;
import com.example.sharedtravel.fragments.dialogs.DialogBuilder;

import java.util.ArrayList;
import java.util.HashSet;
//ITS SOME KIND OF MAGIIIC
//NEVER EVER EVER EVER CHANGE ANYTHING HERE!!!!
public class CitiesSpinnerAdapter extends RecyclerView.Adapter<CitiesSpinnerAdapter.SpinnerViewHolder> implements Filterable {

    private boolean allowMultiple;
    private String[] data;
    private String[] dataFiltered;
    private int indexOfLastItemSelected = -1;
    private HashSet<String> selectedTextCities;

    private CitiesSpinner.SpinnerAdapterItemClickedListener listener;

    public CitiesSpinnerAdapter(String[] cities, boolean allowMultiple, CitiesSpinner.SpinnerAdapterItemClickedListener listener) {
        this.data = cities;
        dataFiltered = cities;
        this.listener = listener;
        this.allowMultiple = allowMultiple;
        selectedTextCities = new HashSet<>();
    }


    @NonNull
    @Override
    public SpinnerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final boolean SHOULD_ATTACH_TO_ROOT = false;
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.citites_spinner_single_item, viewGroup, SHOULD_ATTACH_TO_ROOT);
        return new SpinnerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpinnerViewHolder spinnerViewHolder, int i) {
        final TextView cityTextView = spinnerViewHolder.cityTextView;
        if (indexOfLastItemSelected != -1 && !allowMultiple) {
            cityTextView.setBackgroundColor(
                    indexOfLastItemSelected != i ?
                            Color.parseColor("#FFFFFF")
                            :
                            Color.parseColor("#DDDDDD"));
        } else if (allowMultiple) {
            if (!selectedTextCities.contains(dataFiltered[i])) {
                cityTextView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            } else {
                cityTextView.setBackgroundColor(Color.parseColor("#DDDDDD"));
            }
        }

        spinnerViewHolder.cityTextView.setText(dataFiltered[i]);


    }

    @Override
    public int getItemCount() {
        return this.dataFiltered.length;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    dataFiltered = data;
                } else {
                    ArrayList<String> filteredList = new ArrayList<>();
                    for (String city : data) {

                        if (city.toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(city);
                        }
                    }

                    dataFiltered = filteredList.toArray(new String[0]);
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = dataFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                dataFiltered = (String[]) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }


    class SpinnerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView cityTextView;


        SpinnerViewHolder(@NonNull View itemView) {
            super(itemView);
            cityTextView = itemView.findViewById(R.id.tv_single_city);
            cityTextView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            if (selectedTextCities.contains(((TextView) v).getText().toString()))
                return;

            //Can not choose more than 4 intermedaite stops
            if(selectedTextCities.size() >= 4) {
                //Notifty view so it can display error message
                listener.itemClicked(null, null);
                return;
            }

            if (!allowMultiple) {
                indexOfLastItemSelected = getAdapterPosition();
                notifyDataSetChanged();
            } else {
                selectedTextCities.add(((TextView) v).getText().toString());
                notifyDataSetChanged();
            }
            listener.itemClicked(v, textView -> {
                selectedTextCities.remove(textView.getText().toString());
                notifyDataSetChanged();
            });
        }
    }

    public interface RemoveTextViewListener {
        void removeTextView(TextView textView);
    }
}