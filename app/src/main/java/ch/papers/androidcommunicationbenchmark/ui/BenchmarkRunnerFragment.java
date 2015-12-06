package ch.papers.androidcommunicationbenchmark.ui;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import ch.papers.androidcommunicationbenchmark.R;
import ch.papers.androidcommunicationbenchmark.communication.Client;
import ch.papers.androidcommunicationbenchmark.communication.Server;
import ch.papers.androidcommunicationbenchmark.communication.bluetooth.rfcomm.BluetoothRfcommClient;
import ch.papers.androidcommunicationbenchmark.communication.bluetooth.rfcomm.BluetoothRfcommServer;
import ch.papers.androidcommunicationbenchmark.communication.nfc.beam.NFCServer;
import ch.papers.androidcommunicationbenchmark.communication.nfc.hostemulation.NFCClient;
import ch.papers.androidcommunicationbenchmark.communication.wifi.WifiClient;
import ch.papers.androidcommunicationbenchmark.communication.wifi.WifiServer;
import ch.papers.androidcommunicationbenchmark.models.BenchmarkResult;
import ch.papers.androidcommunicationbenchmark.utils.Logger;
import ch.papers.androidcommunicationbenchmark.utils.Logger.OnLogChangedListener;
import ch.papers.androidcommunicationbenchmark.utils.objectstorage.UuidObjectStorage;
import ch.papers.androidcommunicationbenchmark.utils.objectstorage.listeners.OnResultListener;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 15/11/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class BenchmarkRunnerFragment extends Fragment {
    private Client client;
    private Server server;

    private final static String CONNECTION_TYPE_ARGUMENT = "CONNECTION_TYPE";

    private boolean isServerRunning = false;
    private boolean isClientRunning = false;


    private final OnLogChangedListener onLogChangedListener = new OnLogChangedListener(){
        @Override
        public void onLogChanged(final String tag, final String log) {
            final ScrollView scrollView = (ScrollView) getView().findViewById(R.id.logScrollView);
            final TextView logTextView = (TextView) getView().findViewById(R.id.logTextView);
            logTextView.post(new Runnable() {
                @Override
                public void run() {
                    logTextView.setText(logTextView.getText() + tag + "\t\t" + log + "\n");
                    scrollView.fullScroll(View.FOCUS_DOWN);
                }
            });
        }
    };

    @Override
    public void onStop() {
        super.onStop();
        Logger.getInstance().unregisterOnLogChangedListener(onLogChangedListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        switch (this.getArguments().getInt(CONNECTION_TYPE_ARGUMENT)) {
            case BenchmarkResult.ConnectionTechonology.BLUETOOTH_RFCOMM:
                this.server = new BluetoothRfcommServer(this.getActivity());
                this.client = new BluetoothRfcommClient(this.getActivity());
                this.getActivity().setTitle(R.string.bluetooth_rfcomm_benchmark);
                break;
            case BenchmarkResult.ConnectionTechonology.BLUETOOTH_LE:
                this.server = new BluetoothRfcommServer(this.getActivity());
                this.client = new BluetoothRfcommClient(this.getActivity());
                this.getActivity().setTitle(R.string.bluetooth_le_benchmark);
                break;
            case BenchmarkResult.ConnectionTechonology.WIFI:
                this.server = new WifiServer(this.getActivity());
                this.client = new WifiClient(this.getActivity());
                this.getActivity().setTitle(R.string.wifi_benchmark);
                break;
            case BenchmarkResult.ConnectionTechonology.NFC:
                this.server = new NFCServer(this.getActivity());
                this.client = new NFCClient(this.getActivity());
                this.getActivity().setTitle(R.string.nfc_benchmark);
                break;
        }
        Logger.getInstance().registerOnLogChangedListener(onLogChangedListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.benchmark_runner_fragment, container, false);
        view.findViewById(R.id.startServerButton).setOnClickListener(this.onStartServerButtonClickedListener);
        view.findViewById(R.id.startClientButton).setOnClickListener(this.onStartClientButtonClickedListener);
        return view;
    }

    private final View.OnClickListener onStartServerButtonClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            BenchmarkRunnerFragment.this.isServerRunning = !BenchmarkRunnerFragment.this.isServerRunning;
            Button startButton = (Button) v;
            if (isServerRunning) {
                server.start();
                startButton.setText(R.string.stop_server);
            } else {
                server.stop();
                startButton.setText(R.string.start_server);
            }
        }
    };

    private final View.OnClickListener onStartClientButtonClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            BenchmarkRunnerFragment.this.isClientRunning = !BenchmarkRunnerFragment.this.isClientRunning;
            Button startButton = (Button) v;
            if (isClientRunning) {
                startButton.setText(R.string.stop_client);
                client.startBenchmark(new OnResultListener<BenchmarkResult>() {
                    @Override
                    public void onSuccess(BenchmarkResult result) {
                        UuidObjectStorage.getInstance().addEntry(result, new OnResultListener<BenchmarkResult>() {
                            @Override
                            public void onSuccess(BenchmarkResult result) {
                                Snackbar.make(getView(), R.string.benchmark_stored, Snackbar.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String message) {
                                Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
                            }
                        }, BenchmarkResult.class);

                    }

                    @Override
                    public void onError(String message) {
                        Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
                    }
                });
            } else {
                startButton.setText(R.string.start_client);
            }
        }
    };


    public static Fragment newInstance(int connectionType) {
        Fragment fragment = new BenchmarkRunnerFragment();
        Bundle args = new Bundle();
        args.putInt(CONNECTION_TYPE_ARGUMENT, connectionType);
        fragment.setArguments(args);
        return fragment;
    }
}
