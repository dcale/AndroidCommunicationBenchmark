package ch.papers.androidcommunicationbenchmark.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import ch.papers.androidcommunicationbenchmark.R;
import ch.papers.androidcommunicationbenchmark.models.BenchmarkResult;
import ch.papers.androidcommunicationbenchmark.utils.Logger;
import ch.papers.objectstorage.UuidObjectStorage;
import ch.papers.objectstorage.listeners.OnResultListener;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 04/12/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class BenchmarkResultListFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.benchmark_list, container, false);
        final ListView listView = (ListView) view.findViewById(R.id.listView);

        UuidObjectStorage.getInstance().<BenchmarkResult>getEntriesAsList(new OnResultListener<List<BenchmarkResult>>() {
            @Override
            public void onSuccess(final List<BenchmarkResult> result) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listView.setAdapter(new BenchmarkResultListAdapter(getActivity(), result));
                    }
                });
            }

            @Override
            public void onError(String message) {
                Logger.getInstance().log("listadapter", "error " + message);
            }
        }, BenchmarkResult.class);
        this.getActivity().setTitle(R.string.list_benchmark);
        return view;
    }


    public static Fragment newInstance() {

        return new BenchmarkResultListFragment();
    }
}
