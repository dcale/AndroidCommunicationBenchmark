package ch.papers.androidcommunicationbenchmark.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Random;

import ch.papers.androidcommunicationbenchmark.R;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 08/01/16.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class AuthenticationFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = new AuthenticationView(this.getContext(), new byte[]{(byte) new Random().nextInt(), (byte) new Random().nextInt(),
                (byte) new Random().nextInt(), (byte) new Random().nextInt(),
                (byte) new Random().nextInt(), (byte) new Random().nextInt(),
                (byte) new Random().nextInt(), (byte) new Random().nextInt()});

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .detach(AuthenticationFragment.this)
                        .attach(AuthenticationFragment.this)
                        .commit();
            }
        });
        this.getActivity().setTitle(R.string.authview);
        return view;
    }

    public static Fragment newInstance() {
        return new AuthenticationFragment();
    }
}
