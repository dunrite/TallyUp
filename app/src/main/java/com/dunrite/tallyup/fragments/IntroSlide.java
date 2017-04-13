package com.dunrite.tallyup.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dunrite.tallyup.R;
import com.dunrite.tallyup.activities.IntroActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class IntroSlide extends Fragment {
    @BindView(R.id.sign_in_button)
    Button signInButton;

    private static final String ARG_LAYOUT_RES_ID = "layoutResId";

    public static IntroSlide newInstance(int layoutResId) {
        IntroSlide IntroSlide = new IntroSlide();

        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_RES_ID, layoutResId);
        IntroSlide.setArguments(args);

        return IntroSlide;
    }

    private int layoutResId;

    public IntroSlide() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(ARG_LAYOUT_RES_ID))
            layoutResId = getArguments().getInt(ARG_LAYOUT_RES_ID);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(layoutResId, container, false);

        switch (layoutResId) {
            case R.layout.fragment_intro1:

                break;
            case R.layout.fragment_intro2:

                break;
            case R.layout.fragment_intro3:
                ButterKnife.bind(this, rootView);
                signInButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((IntroActivity)getActivity()).launchGoogleSignInIntent();
                    }
                });
                break;
        }
        return rootView;
    }

}