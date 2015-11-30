package ch.papers.androidcommunicationbenchmark.communication.wifi;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import ch.papers.androidcommunicationbenchmark.R;
import ch.papers.androidcommunicationbenchmark.communication.ServerManager;
import ch.papers.androidcommunicationbenchmark.models.BenchmarkResult;
import ch.papers.androidcommunicationbenchmark.utils.objectstorage.UuidObjectStorage;
import ch.papers.androidcommunicationbenchmark.utils.objectstorage.listeners.OnResultListener;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 30/11/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class WifiFragment extends Fragment {

    private final WifiClient wifiClient = new WifiClient();
    private final WifiServer wifiServer = new WifiServer();

    private boolean isServerRunning = false;
    private boolean isClientRunning = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bluetooth_fragment, container, false);
        view.findViewById(R.id.startServerButton).setOnClickListener(this.onStartServerButtonClickedListener);
        view.findViewById(R.id.startClientButton).setOnClickListener(this.onStartClientButtonClickedListener);



        return view;
    }

    private final View.OnClickListener onStartServerButtonClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            WifiFragment.this.isServerRunning = !WifiFragment.this.isServerRunning;
            Button startButton = (Button) v;
            if (isServerRunning) {
                startButton.setText(R.string.stop);
                makeDiscoverable();
                ServerManager.getInstance().start(wifiServer);
            } else {
                startButton.setText(R.string.start_server);
            }
        }
    };

    private final View.OnClickListener onStartClientButtonClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            isClientRunning = !isClientRunning;
            Button startButton = (Button) v;
            if (isClientRunning) {
                startButton.setText(R.string.stop);
                wifiClient.startBenchmark(getActivity(), new OnResultListener<BenchmarkResult>() {
                    @Override
                    public void onSuccess(BenchmarkResult result) {
                        UuidObjectStorage.getInstance().addEntry(result, new OnResultListener<BenchmarkResult>() {
                            @Override
                            public void onSuccess(BenchmarkResult result) {
                                Toast.makeText(getContext(),R.string.benchmark_stored, Toast.LENGTH_SHORT);
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
                            }
                        }, BenchmarkResult.class);

                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
                    }
                });
            } else {
                startButton.setText(R.string.start_server);
            }
        }
    };

    private void makeDiscoverable(){
        WifiP2pManager manager = (WifiP2pManager) this.getActivity().getSystemService(Context.WIFI_P2P_SERVICE);
        WifiP2pManager.Channel channel = manager.initialize(this.getActivity(), this.getActivity().getMainLooper(), null);
    }

    public static WifiFragment newInstance() {
        return new WifiFragment();
    }

}
