package com.example.ak.project_481;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.ml.vision.label.FirebaseVisionLabel;

import java.util.ArrayList;
import java.util.List;

public class BottomSheetLabelFragment extends BottomSheetDialogFragment {

    private static List<FirebaseVisionLabel> labels = new ArrayList<>();
    private RecyclerView recyclerView;

    public static ItemAdapter getItemAdapter() {
        return itemAdapter;
    }

    public static void setItemAdapter(ItemAdapter itemAdapter) {
        BottomSheetLabelFragment.itemAdapter = itemAdapter;
    }

    private static ItemAdapter itemAdapter ;


    public static List<FirebaseVisionLabel> getLabels() {
        return labels;
    }

    public static void setLabels(List<FirebaseVisionLabel> labels) {
        BottomSheetLabelFragment.labels = labels;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_bottom_sheet, container, false);
        recyclerView = v.findViewById(R.id.rvLabels);
        itemAdapter = new ItemAdapter(getContext(), labels );
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(itemAdapter);
        return v;
    }

    private static void setRecyclerView(){

    }
//    @Override
//    public void setupDialog(Dialog dialog, int style) {
//        super.setupDialog(dialog, style);
//        View v = LayoutInflater.from(getContext()).inflate(R.layout.fragment_bottom_sheet, null);
//    }
}
