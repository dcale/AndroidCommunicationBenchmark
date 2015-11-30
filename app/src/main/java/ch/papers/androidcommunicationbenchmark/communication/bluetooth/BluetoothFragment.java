package ch.papers.androidcommunicationbenchmark.communication.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import ch.papers.androidcommunicationbenchmark.R;
import ch.papers.androidcommunicationbenchmark.communication.Constants;
import ch.papers.androidcommunicationbenchmark.communication.ServerManager;
import ch.papers.androidcommunicationbenchmark.models.BenchmarkResult;
import ch.papers.androidcommunicationbenchmark.utils.objectstorage.UuidObjectStorage;
import ch.papers.androidcommunicationbenchmark.utils.objectstorage.listeners.OnResultListener;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 15/11/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class BluetoothFragment extends Fragment {
    private final BluetoothClient bluetoothClient = new BluetoothClient();
    private final BluetoothServer bluetoothServer = new BluetoothServer();

    private boolean isServerRunning = false;
    private boolean isClientRunning = false;



    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

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
            BluetoothFragment.this.isServerRunning = !BluetoothFragment.this.isServerRunning;
            Button startButton = (Button) v;
            if (isServerRunning) {
                startButton.setText(R.string.stop);
                BluetoothFragment.this.makeDiscoverable();
                ServerManager.getInstance().start(bluetoothServer);
            } else {
                startButton.setText(R.string.start_server);
            }
        }
    };

    private final View.OnClickListener onStartClientButtonClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            BluetoothFragment.this.isClientRunning = !BluetoothFragment.this.isClientRunning;
            Button startButton = (Button) v;
            if (isClientRunning) {
                startButton.setText(R.string.stop);
                BluetoothFragment.this.bluetoothClient.startBenchmark(getActivity(), new OnResultListener<BenchmarkResult>() {
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

    private void makeDiscoverable() {

        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, Constants.DISCOVERABLE_DURATION);
        this.startActivity(discoverableIntent);
    }

    public static BluetoothFragment newInstance() {
        return new BluetoothFragment();
    }

}
