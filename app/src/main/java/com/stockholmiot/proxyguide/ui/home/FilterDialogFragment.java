package com.stockholmiot.proxyguide.ui.home;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import com.google.firebase.firestore.Query;
import com.stockholmiot.proxyguide.R;
import com.stockholmiot.proxyguide.ui.home.models.Filters;

public class FilterDialogFragment extends DialogFragment implements View.OnClickListener {

    public static final String TAG = "FilterDialog";
    private Context mContext;
    private Fragment fragmentShowDialog;

    interface FilterListener {
        void onFilter(Filters filters);
    }

    public FilterDialogFragment() {

    }

    public FilterDialogFragment(Context context) {
        mContext = context;
    }

    public FilterDialogFragment(Context mContext, Fragment fragmentShowDialog) {
        this.mContext = mContext;
        this.fragmentShowDialog = fragmentShowDialog;
    }

    private View mRootView;
    private Spinner mCountrySpinner;
    private Spinner mCitySpinner;
    private Spinner mSortSpinner;

    private FilterListener mFilterListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_filter_dialog, container, false);

        mCountrySpinner = mRootView.findViewById(R.id.spinner_country);
        mCitySpinner = mRootView.findViewById(R.id.spinner_city);

        mRootView.findViewById(R.id.button_search).setOnClickListener(this);
        mRootView.findViewById(R.id.button_cancel).setOnClickListener(this);

        return mRootView;

    }
    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
        if (fragmentShowDialog instanceof FilterListener) {
            mFilterListener = (FilterListener) childFragment;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (fragmentShowDialog instanceof FilterListener) {
            mFilterListener = (FilterListener) fragmentShowDialog;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_search){
            onSearchClicked();
        }else if (v.getId() == R.id.button_cancel){
            onCancelClicked();
        }
        /*switch (v.getId()) {
            case R.id.button_search:
                onSearchClicked();
                break;
            case R.id.button_cancel:
                onCancelClicked();
                break;
        }*/
    }

    public void onSearchClicked() {
        if (mFilterListener != null) {
            mFilterListener.onFilter(getFilters());
        }

        dismiss();
    }

    public void onCancelClicked() {
        dismiss();
    }



    @Nullable
    private String getSelectedCountry() {
        String selected = (String) mCountrySpinner.getSelectedItem();
        if (getString(R.string.value_any_country).equals(selected)) {
            return null;
        } else {
            return selected;
        }
    }

    @Nullable
    private String getSelectedCity() {
        String selected = (String) mCitySpinner.getSelectedItem();
        if (getString(R.string.value_any_city).equals(selected)) {
            return null;
        } else {
            return selected;
        }
    }



    public void resetFilters() {
        if (mRootView != null) {
            mCountrySpinner.setSelection(0);
            mCitySpinner.setSelection(0);
        }
    }

    public Filters getFilters() {
        Filters filters = new Filters();

        if (mRootView != null) {
            filters.setCountry(getSelectedCountry());
            filters.setCity(getSelectedCity());

        }

        return filters;
    }

}