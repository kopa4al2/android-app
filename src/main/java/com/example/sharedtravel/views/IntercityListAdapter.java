package com.example.sharedtravel.views;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sharedtravel.R;
import com.example.sharedtravel.databinding.SingleTravelCardBinding;
import com.example.sharedtravel.firebase.utils.DocumentSnapshotConverter;
import com.example.sharedtravel.fragments.dialogs.DialogBuilder;
import com.example.sharedtravel.handlers.OnTravelCardClickHandler;
import com.example.sharedtravel.model.IntercityTravel;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;

import java.util.List;

public class IntercityListAdapter extends
        FirestorePagingAdapter<IntercityTravel, IntercityListAdapter.IntercityTravelViewHolder> {

    private Context context;
    private View loadingView;

    public IntercityListAdapter(FirestorePagingOptions<IntercityTravel> options,
                                Context context,
                                View loadingView) {
        super(options);
        this.loadingView = loadingView;
        this.context = context;
    }

    @NonNull
    @Override
    public IntercityTravelViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater li = LayoutInflater.from(context);
        SingleTravelCardBinding travelBinding = DataBindingUtil.inflate(li,
                R.layout.single_travel_card,
                viewGroup,
                false);
        return new IntercityTravelViewHolder(travelBinding);
    }

    @Override
    protected void onBindViewHolder(@NonNull IntercityTravelViewHolder holder, int position, @NonNull IntercityTravel model) {
        holder.bind(model);
    }


    @Override
    protected void onLoadingStateChanged(@NonNull LoadingState state) {
        switch (state) {
            case LOADING_INITIAL:
                loadingView.setVisibility(View.VISIBLE);
                break;
            case LOADING_MORE:

                break;
            case LOADED:
                loadingView.setVisibility(View.GONE);
                break;
            case ERROR:
                DialogBuilder.showErrorMessage(context, context.getString(R.string.no_internet));
                break;
        }
    }

    static class IntercityTravelViewHolder extends RecyclerView.ViewHolder {
        private final SingleTravelCardBinding binding;

        IntercityTravelViewHolder(@NonNull SingleTravelCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(IntercityTravel item) {

            binding.setHandler(new OnTravelCardClickHandler());
            binding.setTravel(item);
            binding.executePendingBindings();

        }
    }
}
