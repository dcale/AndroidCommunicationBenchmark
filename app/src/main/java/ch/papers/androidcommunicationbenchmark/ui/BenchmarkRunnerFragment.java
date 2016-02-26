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

import java.util.ArrayList;
import java.util.List;

import ch.papers.androidcommunicationbenchmark.R;
import ch.papers.androidcommunicationbenchmark.communication.Client;
import ch.papers.androidcommunicationbenchmark.communication.Server;
import ch.papers.androidcommunicationbenchmark.communication.bluetooth.le.BluetoothLEClient;
import ch.papers.androidcommunicationbenchmark.communication.bluetooth.le.BluetoothLEServer;
import ch.papers.androidcommunicationbenchmark.communication.bluetooth.rfcomm.BluetoothRfcommClient;
import ch.papers.androidcommunicationbenchmark.communication.bluetooth.rfcomm.BluetoothRfcommServer;
import ch.papers.androidcommunicationbenchmark.communication.nfc.beam.NFCServer;
import ch.papers.androidcommunicationbenchmark.communication.nfc.hostemulation.NFCClient;
import ch.papers.androidcommunicationbenchmark.communication.wifi.WifiClient;
import ch.papers.androidcommunicationbenchmark.communication.wifi.WifiServer;
import ch.papers.androidcommunicationbenchmark.models.BenchmarkResult;
import ch.papers.androidcommunicationbenchmark.utils.FixedSizeArrayList;
import ch.papers.androidcommunicationbenchmark.utils.Logger;
import ch.papers.androidcommunicationbenchmark.utils.Logger.OnLogChangedListener;
import ch.papers.objectstorage.UuidObjectStorage;
import ch.papers.objectstorage.listeners.DummyOnResultListener;
import ch.papers.objectstorage.listeners.OnResultListener;

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

    private Button serverButton;
    private Button clientButton;


    private final OnLogChangedListener onLogChangedListener = new OnLogChangedListener(){
        private final int tagSize=20;
        private final List<String> logBuffer = new FixedSizeArrayList<String>(50);

        @Override
        public synchronized void onLogChanged(final String tag, final String log) {
            String displayTag = (tag+"                       ").substring(0,tagSize); //every tag has same length
            this.logBuffer.add(displayTag + log);

            final ScrollView scrollView = (ScrollView) getView().findViewById(R.id.logScrollView);
            final TextView logTextView = (TextView) getView().findViewById(R.id.logTextView);
            logTextView.post(new Runnable() {

                @Override
                public void run() {

                    displayBuffer();
                }

                private void displayBuffer(){
                    String fullLog = "";
                    for(String log : new ArrayList<String>(logBuffer)){
                        fullLog += log+"\n";
                    }
                    logTextView.setText(fullLog);
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
                this.server = new BluetoothLEServer(this.getActivity());
                this.client = new BluetoothLEClient(this.getActivity());
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

        if(this.server.isSupported()){
            this.serverButton.setVisibility(View.VISIBLE);
        } else {
            this.serverButton.setVisibility(View.GONE);
        }

        if(this.client.isSupported()){
            this.clientButton.setVisibility(View.VISIBLE);
        } else {
            this.clientButton.setVisibility(View.GONE);
        }

        Logger.getInstance().registerOnLogChangedListener(onLogChangedListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.benchmark_runner_fragment, container, false);

        serverButton = (Button)view.findViewById(R.id.startServerButton);
        serverButton.setOnClickListener(this.onStartServerButtonClickedListener);

        clientButton = (Button)view.findViewById(R.id.startClientButton);
        clientButton.setOnClickListener(this.onStartClientButtonClickedListener);

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
                client.start(new OnResultListener<BenchmarkResult>() {
                    @Override
                    public void onSuccess(BenchmarkResult result) {
                        UuidObjectStorage.getInstance().addEntry(result, new OnResultListener<BenchmarkResult>() {
                            @Override
                            public void onSuccess(BenchmarkResult result) {
                                UuidObjectStorage.getInstance().commit(DummyOnResultListener.getInstance());
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
                client.stop();
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
