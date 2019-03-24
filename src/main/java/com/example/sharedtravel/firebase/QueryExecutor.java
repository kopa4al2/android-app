package com.example.sharedtravel.firebase;

import android.widget.Adapter;
import android.widget.BaseAdapter;

import com.example.sharedtravel.firebase.utils.MultipleQueryCompleteListener;
import com.example.sharedtravel.firebase.utils.QueryCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Observable;

public class QueryExecutor{


    public static void executeQuery(Query query, QueryCompleteListener listener) {
        listener.onStart();
        query.get()
                .addOnSuccessListener(listener::onSuccess)
                .addOnFailureListener(listener::onFail);
    }

    public static void executeQueries(List<Query> queries, MultipleQueryCompleteListener listener) {

        listener.onStart();
        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (Query query : queries) {
            tasks.add(query.get());
        }
        Task<List<QuerySnapshot>> combined = Tasks.whenAllSuccess(tasks);

        combined.addOnSuccessListener(listener::onSuccess)
                .addOnFailureListener(listener::onFail);
    }



}
