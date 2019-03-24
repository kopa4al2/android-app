package com.example.sharedtravel.fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.sharedtravel.R;
import com.example.sharedtravel.SharedTravelApplication;
import com.example.sharedtravel.firebase.AsyncListener;
import com.example.sharedtravel.firebase.AuthenticationManager;
import com.example.sharedtravel.firebase.IntercityTravelRepository;
import com.example.sharedtravel.firebase.utils.AsyncListenerWithResult;
import com.example.sharedtravel.fragments.dialogs.DialogBuilder;
import com.example.sharedtravel.model.IntercityTravel;
import com.example.sharedtravel.utils.DateFormatUtils;

import java.text.DateFormat;
import java.util.List;

public class MyTravelsFragment extends Fragment implements View.OnClickListener {

    private RecyclerView myTravelsRecyclerView;
    private View loadingView;


    public MyTravelsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_travels_fragment, container, false);
        loadingView = view.findViewById(R.id.loading_overlay);
        myTravelsRecyclerView = view.findViewById(R.id.rv_my_travels);
        myTravelsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        loadingView.setVisibility(View.VISIBLE);
        initMyTravels(loadingView);

        return view;
    }

    private void initMyTravels(View loadingView) {
        fetchTravelsTask travelsTask = new fetchTravelsTask((travels) -> {
            //noinspection unchecked
            MyTravelsAdapter adapter = new MyTravelsAdapter((List<IntercityTravel>) travels);
            myTravelsRecyclerView.setAdapter(adapter);
            loadingView.setVisibility(View.GONE);
        });

        travelsTask.execute();
    }

    @Override
    public void onClick(View v) {
        //DELETE TRAVEL
        IntercityTravel travel = ((IntercityTravel)v.getTag());
        String travelId = ((IntercityTravel)v.getTag()).getTravelID();
        DialogBuilder.showConfirmationDialog(getContext(), getString(R.string.confirm_delete_travel), () -> {
            loadingView.setVisibility(View.VISIBLE);
            new IntercityTravelRepository().deleteTravelById(travelId, () -> {
                DialogBuilder.showSuccessDialog(getContext(), getString(R.string.successfully_deleted_travel));
                new deleteTravelTask(travel, () -> {
                    initMyTravels(loadingView);
                }).execute();
            });
        });
    }

    private static class deleteTravelTask extends AsyncTask<Void,Void,Void> {

        private final IntercityTravel travel;
        private final AsyncListener callback;

        deleteTravelTask(IntercityTravel travel, AsyncListener callback) {
            this.travel = travel;
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            SharedTravelApplication.dbSQL.intercityTravelDAO().delete(travel);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            callback.onSuccess();
            super.onPostExecute(aVoid);
        }
    }

    private static class fetchTravelsTask extends AsyncTask<Void, Void, List<IntercityTravel>> {

        private AsyncListenerWithResult listener;

        fetchTravelsTask(AsyncListenerWithResult listener) {
            this.listener = listener;
        }

        @Override
        protected List<IntercityTravel> doInBackground(Void... voids) {
            return SharedTravelApplication.dbSQL.intercityTravelDAO().getByCreator(
                    AuthenticationManager.getInstance().getLoggedInFirebaseUser().getUid());
        }

        @Override
        protected void onPostExecute(List<IntercityTravel> intercityTravels) {
            listener.onSuccess(intercityTravels);
            super.onPostExecute(intercityTravels);
        }
    }

    private class MyTravelsAdapter extends RecyclerView.Adapter<MyTravelsAdapter.MyTravelsViewHolder> {

        private List<IntercityTravel> travels;

        public MyTravelsAdapter(List<IntercityTravel> travels) {
            this.travels = travels;
        }

        @NonNull
        @Override
        public MyTravelsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v = getLayoutInflater().inflate(R.layout.my_travels_rv_single_row, viewGroup, false);
            return new MyTravelsViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull MyTravelsViewHolder myTravelsViewHolder, int i) {
            myTravelsViewHolder.bind(travels.get(i));
        }

        @Override
        public int getItemCount() {
            return travels.size();
        }

        private class MyTravelsViewHolder extends RecyclerView.ViewHolder {
            private final String FROM_TEXT;
            private final String TO_TEXT;
            private final String CREATION_DATE_TEXT;

            private ImageButton deleteBtn;
            private TextView from;
            private TextView to;
            private TextView creationDate;

            public MyTravelsViewHolder(@NonNull View itemView) {
                super(itemView);
                FROM_TEXT = itemView.getContext().getString(R.string.from);
                TO_TEXT = itemView.getContext().getString(R.string.to);
                CREATION_DATE_TEXT = itemView.getContext().getString(R.string.creation_date);

                deleteBtn = itemView.findViewById(R.id.btn_my_travels_delete_travel);
                from = itemView.findViewById(R.id.tv_start_city_my_travels);
                to = itemView.findViewById(R.id.tv_end_city_my_travels);
                creationDate = itemView.findViewById(R.id.tv_creation_date_my_travels);
            }

            void bind(IntercityTravel travel) {
                String fromText = String.format("%s : %s", FROM_TEXT, travel.getStartCity());
                String toText = String.format("%s : %s", TO_TEXT, travel.getDestinationCity());
                String formattedDate =
                        DateFormatUtils.dateToString(travel.getDateOfCreation(), DateFormat.SHORT)
                                + " " +
                                DateFormatUtils.dateToHourString(travel.getDateOfCreation());
                String dateText = String.format("%s : %s", CREATION_DATE_TEXT, formattedDate);

                from.setText(fromText);
                to.setText(toText);
                creationDate.setText(dateText);
                deleteBtn.setTag(travel);
                deleteBtn.setOnClickListener(MyTravelsFragment.this);
            }
        }
    }


}
