package ch.papers.androidcommunicationbenchmark.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ch.papers.androidcommunicationbenchmark.R;
import ch.papers.androidcommunicationbenchmark.models.BenchmarkResult;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 04/12/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class BenchmarkResultListAdapter extends ArrayAdapter<BenchmarkResult> {
    private final static String DATE_FORMAT = "yyyy-MM-dd";

    private final Context context;
    private final List<BenchmarkResult> benchmarkResults;


    public BenchmarkResultListAdapter(Context context, List<BenchmarkResult> benchmarkResults) {
        super(context, R.layout.benchmark_entry, benchmarkResults);
        this.context = context;
        this.benchmarkResults = benchmarkResults;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.benchmark_entry, null);
        }
        BenchmarkResult benchmarkResult = this.benchmarkResults.get(position);
        ImageView typeImageView = (ImageView) v.findViewById(R.id.typeImageView);
        TextView bandWidthTextView = (TextView) v.findViewById(R.id.bandwidthTextView);
        TextView latencyTextView = (TextView) v.findViewById(R.id.latencyTextView);
        TextView dateTextView = (TextView) v.findViewById(R.id.dateTextView);

        latencyTextView.setText(String.format(this.context.getString(R.string.latency_string), benchmarkResult.latency()));
        bandWidthTextView.setText(String.format(this.context.getString(R.string.bandwidth_string), benchmarkResult.bandwidth()));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        dateTextView.setText(simpleDateFormat.format(new Date(benchmarkResult.getTimestamp())));

        switch(benchmarkResult.getConnectionTechnology()){
            case BenchmarkResult.ConnectionTechonology.BLUETOOTH_RFCOMM:
                typeImageView.setImageResource(R.drawable.ic_bluetooth_connected);
                break;
            case BenchmarkResult.ConnectionTechonology.WIFI:
                typeImageView.setImageResource(R.drawable.ic_network_wifi);
                break;
            case BenchmarkResult.ConnectionTechonology.NFC:
                typeImageView.setImageResource(R.drawable.ic_nfc);
                break;
            case BenchmarkResult.ConnectionTechonology.BLUETOOTH_LE:
                typeImageView.setImageResource(R.drawable.ic_bluetooth_searching);
                break;
        }
        return v;
    }
}
