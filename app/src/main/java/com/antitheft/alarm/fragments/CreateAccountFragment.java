package com.antitheft.alarm.fragments;


import android.view.View;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.antitheft.alarm.activity.MainActivity;
import com.antitheft.alarm.R;
import com.antitheft.alarm.listener.IFragmentInteractionListener;
import com.antitheft.alarm.utils.Const;

import butterknife.BindView;
import butterknife.OnClick;


import static com.antitheft.alarm.utils.Const.INPUT_PASSWORD_ID;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link IFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CreateAccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateAccountFragment extends BaseFragment {

    @BindView(R.id.bnt_continue)
    Button bnt_continue;

    public CreateAccountFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment CreateAccountFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CreateAccountFragment newInstance() {
        return new CreateAccountFragment();
    }

    // TODO: Rename method, update argument and hook method into UI event
    @OnClick(R.id.bnt_continue)
    public void onButtonPressed(View view) {
        goTo(INPUT_PASSWORD_ID);
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_create_account;
    }

    @Override
    public void initData() {

    }

    @Override
    public void initViewEvent() {

    }

    @Override
    public int getFragmentId() {
        return Const.CREATE_ACCOUNT_ID;
    }

    @Override
    public void onBackPressed() {
        parentActivity.finish();
    }
}
