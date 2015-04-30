package com.integreight.onesheeld.shields.fragments;

import android.nfc.NdefRecord;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.adapters.NfcNdefRecordsExpandableAdapter;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.NfcShield;
import com.integreight.onesheeld.shields.controller.NfcShield.NFCEventHandler;

import com.integreight.onesheeld.utils.customviews.OneSheeldTextView;

import java.util.ArrayList;


/**
 * Created by Mouso on 3/11/2015.
 */
public class NfcFragment extends ShieldFragmentParent<NfcFragment> {

    ExpandableListView nfcRecords;
    OneSheeldTextView cardDetails,noCard;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.nfc_shield_fragment_view, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        nfcRecords =(ExpandableListView) v.findViewById(R.id.nfc_Records_list);
        cardDetails =(OneSheeldTextView) v.findViewById(R.id.nfc_card_details);
        noCard = (OneSheeldTextView) v.findViewById(R.id.nfc_no_card);

        nfcRecords.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                return true;
            }
        });
    }

    @Override
    public void onStart() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            if (!reInitController())
                return;
        }
        ((NfcShield) getApplication().getRunningShields().get(getControllerTag())).setEventHandler(nfcEventHandler);
        super.onStart();
    }

    @Override
    public void doOnServiceConnected() {
        initializeFirmata();
    }

    private void initializeFirmata() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            getApplication().getRunningShields().put(getControllerTag(), new NfcShield(activity, getControllerTag()));
        }
    }

    @Override
    public void onResume() {
        ((NfcShield) getApplication().getRunningShields().get(getControllerTag())).setEventHandler(nfcEventHandler);
        super.onResume();
        ((NfcShield) getApplication().getRunningShields().get(getControllerTag())).displayData();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    private NFCEventHandler nfcEventHandler = new NFCEventHandler() {
        @Override
        public void ReadNdef(String id,int maxSize,int usedSize,ArrayList<ArrayList<String>> data) {
            //handle data display
            noCard.setVisibility(View.GONE);
            cardDetails.setText("Tag ID :     \t" + id);
            cardDetails.append("\n");
            cardDetails.append("Max Size :\t"+String.valueOf(maxSize)+" bytes ");
            cardDetails.append("\n");
            cardDetails.append("Used Size : "+String.valueOf(usedSize)+" bytes");
            cardDetails.append("\n");
            cardDetails.append("No. of Records : " + String.valueOf(data.size()) + " record(s)");

            if (canChangeUI()) {
                NfcNdefRecordsExpandableAdapter nfcNdefRecordsExpandableAdapter = new NfcNdefRecordsExpandableAdapter(activity,data);
                nfcRecords.setAdapter(nfcNdefRecordsExpandableAdapter);
            }
        }
    };
}