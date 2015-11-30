package ch.papers.androidcommunicationbenchmark.utils;

import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 21/11/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class Logger {
    private static final Logger INSTANCE = new Logger();
    private TextView textView  = null;

    private Logger() {

    }

    public static Logger getInstance() {
        return INSTANCE;
    }

    public void bindTextView(TextView textView){
        this.textView = textView;
    }

    public void log(final String tag, final String message){
        Log.d(tag, message);
        if(textView!=null){
            textView.post(new Runnable() {
                @Override
                public void run() {
                    textView.setText(textView.getText() +tag+": "+ message + "\n");
                }
            });
        }
    }
}
