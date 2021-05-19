package com.example.sensortrial;


import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PreprocessingManager {

    public final int WINDOW; // microseconds
    public final int SAMPLING_PERIOD; // microseconds
    public final Context CONTEXT; // microseconds
    // The starting time (nanoseconds) of the last window
    private Long window_start = null;
    private final ArrayList<ArrayList<Double>> accumulators = new ArrayList<>();

    //used to turn a dataset into a supervised one
    private String LABEL;

    //private String filename = "out.csv";
    private String filename = "out.txt";
    boolean written_header = false;

    public PreprocessingManager(Context context, int window, int period, String label) {
        WINDOW = window;
        SAMPLING_PERIOD = period;
        CONTEXT = context;
        /*
        Append all sensor data accumulators. Respectively:
        accumulators[0] = accelerometer-x
        accumulators[1] = accelerometer-y
        accumulators[2] = accelerometer-z
        accumulators[3] = gyroscope-x
        accumulators[4] = gyroscope-y
        accumulators[5] = gyroscope-z
        accumulators[6] = orientation-x
        accumulators[7] = orientation-y
        accumulators[8] = orientation-z
         */
        for (int i = 0; i < 9; ++i) {
            accumulators.add(new ArrayList<Double>());
        }

        LABEL = label;

    }

    private void check_window(long time) {
        if (window_start == null) {
            window_start = time;
            return;
        }

        long delta = window_start + WINDOW * 1000L - time;
        if (delta < 0) {
            window_start = window_start + WINDOW * 1000L;
            update_csv();
            accumulators.clear();
            for (int i = 0; i < 9; ++i) {
                accumulators.add(new ArrayList<Double>());
            }
        }
    }

    private void print_header(){
        String[] ATTRIBUTE_NAMES = {
                "accel_x_mean", "accel_x_median", "accel_x_95", "accel_x_max", "accel_x_min", "accel_x_range", "accel_x_std_dev", "accel_x_rms", "accel_x_dom_freq", "accel_x_dom_freq_2",
                "accel_y_mean", "accel_y_median", "accel_y_95", "accel_y_max", "accel_y_min", "accel_y_range", "accel_y_std_dev", "accel_y_rms", "accel_y_dom_freq", "accel_y_dom_freq_2",
                "accel_z_mean", "accel_z_median", "accel_z_95", "accel_z_max", "accel_z_min", "accel_z_range", "accel_z_std_dev", "accel_z_rms", "accel_z_dom_freq", "accel_z_dom_freq_2",
                "gyro_x_mean", "gyro_x_median", "gyro_x_95", "gyro_x_max", "gyro_x_min", "gyro_x_range", "gyro_x_std_dev", "gyro_x_rms", "gyro_x_dom_freq", "gyro_x_dom_freq_2",
                "gyro_y_mean", "gyro_y_median", "gyro_y_95", "gyro_y_max", "gyro_y_min", "gyro_y_range", "gyro_y_std_dev", "gyro_y_rms", "gyro_y_dom_freq", "gyro_y_dom_freq_2",
                "gyro_z_mean", "gyro_z_median", "gyro_z_95", "gyro_z_max", "gyro_z_min", "gyro_z_range", "gyro_z_std_dev", "gyro_z_rms", "gyro_z_dom_freq", "gyro_z_dom_freq_2",
                "rot_x_mean", "rot_x_median", "rot_x_95", "rot_x_max", "rot_x_min", "rot_x_range", "rot_x_std_dev", "rot_x_rms", "rot_x_dom_freq", "rot_x_dom_freq_2",
                "rot_y_mean", "rot_y_median", "rot_y_95", "rot_y_max", "rot_y_min", "rot_y_range", "rot_y_std_dev", "rot_y_rms", "rot_y_dom_freq", "rot_y_dom_freq_2",
                "rot_z_mean", "rot_z_median", "rot_z_95", "rot_z_max", "rot_z_min", "rot_z_range", "rot_z_std_dev", "rot_z_rms", "rot_z_dom_freq", "rot_z_dom_freq_2" , "label"
        };

        String header = String.join(",", ATTRIBUTE_NAMES);

        try {
            File out = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
            Log.d("path",Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
            FileWriter fileWriter = new FileWriter(out, true);
            PrintWriter pw = new PrintWriter(fileWriter);
            pw.println(header);
            pw.flush();
            //to debug
            Log.d("CSV_header", header);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private Double[] extract_indexes(List<Double> acc) {

        //feature
        double mean = acc.stream()
                .mapToDouble(d -> d)
                .average()
                .orElse(0.0);

        //Double[] sorted = (Double[]) acc.stream().sorted().toArray();

        List<Double> sorted = acc.stream().sorted().collect(Collectors.toList());
        //Log.d("Debug",temp.toString());

        //Double[] sorted = (Double[]) temp.toArray();
        //feature
        double median = sorted.get(sorted.size() / 2);
        int index_95th = (int) Math.floor(sorted.size() * 0.95);
        //feature
        double percentile_95 = sorted.get(index_95th);
        //feature
        double max = acc.stream()
                .max(Double::compare)
                .get();
        //feature
        double min = acc.stream()
                .min(Double::compare)
                .get();
        //feature
        double range = max - min;
        double variance = acc.stream()
                .map(i -> i - mean)
                .map(i -> i * i)
                .mapToDouble(i -> i).average().getAsDouble();
        //feature
        double std_dev = Math.sqrt(variance);
        double square_avg = mean * mean + variance;
        //feature
        double root_mean_square = Math.sqrt(square_avg);

        //feature
        int length = acc.size();
        int nearest_2_pow = (int) Math.ceil(Math.log(length) / Math.log(2));
        Log.d("nearest_2_pow", String.valueOf(nearest_2_pow));
        int target_length = (int) Math.pow(2, nearest_2_pow);
        for (int i = length; i < target_length; i++)
            acc.add(0.0);

        ComplexNumber[] x = new ComplexNumber[target_length];
        // original data
        for (int i = 0; i < target_length; i++) {
            x[i] = new ComplexNumber(acc.get(i), 0);
        }

        // FFT of original data
        ComplexNumber[] y = FFT.fft(x);
        double first_max_modulo = 0, second_max_modulo = 0;
        int index_first_max_frequency = 0, index_second_max_frequency = 0;
        for (int i = 0; i < y.length; i++) {
            if (y[i].mod() > first_max_modulo) {
                second_max_modulo = first_max_modulo;
                first_max_modulo = y[i].mod();
                index_second_max_frequency = index_first_max_frequency;
                index_first_max_frequency = i;
            } else if (y[i].mod() > second_max_modulo) {
                second_max_modulo = y[i].mod();
                index_second_max_frequency = i;
            }
        }

        //features
        double dominant_frequency = index_first_max_frequency, dominant_frequency_2 = index_second_max_frequency;


        return new Double[]{mean, median, percentile_95, max, min, range, std_dev,
                root_mean_square, dominant_frequency, dominant_frequency_2};
    }

    private void update_csv() {
        //File directory = CONTEXT.getFilesDir();
        //File out = new File(directory, "out.csv");
        //File out = new File(CONTEXT.getExternalFilesDir(filepath), filename);
        if(!written_header){
            print_header();
            written_header = true;
        }

        File out = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
        //Log.d("path",Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());

        try (FileWriter fileWriter = new FileWriter(out, true)) {
            PrintWriter pw = new PrintWriter(fileWriter);
            ArrayList<String> row = new ArrayList<>();
            for (ArrayList<Double> acc : accumulators) {
                List<String> temp = Stream.of(extract_indexes(acc))
                        .map(Object::toString)
                        .collect(Collectors.toList());
                row.addAll(temp);
            }

            //add label to turn dataset into supervised one
            row.add(LABEL);

            pw.println(String.join(",", row));
            pw.flush();
            //to debug
            Log.d("CSV", String.join(",", row));

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void log_accelerometer(long time, double x, double y, double z) {
        check_window(time);
        if (time >= window_start) {
            accumulators.get(0).add(x);
            accumulators.get(1).add(y);
            accumulators.get(2).add(z);
        }
    }

    public void log_gyroscope(long time, double x, double y, double z) {
        check_window(time);
        if (time >= window_start) {
            accumulators.get(3).add(x);
            accumulators.get(4).add(y);
            accumulators.get(5).add(z);
        }
    }

    public void log_orientation(long time, double x, double y, double z) {
        check_window(time);
        if (time >= window_start) {
            accumulators.get(6).add(x);
            accumulators.get(7).add(y);
            accumulators.get(8).add(z);
        }
    }
}