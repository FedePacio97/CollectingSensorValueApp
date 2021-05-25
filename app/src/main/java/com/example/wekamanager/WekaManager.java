package com.example.wekamanager;

import android.util.Log;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.*;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Add;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class WekaManager {

    public static final String[] CLASSES = {
            "upper_body_push_ups", "lower_body_push_ups", "half_top_push_ups", "half_bottom_push_ups",
            "slow_push_ups", "normal_push_ups", "fast_push_ups"
    };

    public static final String[] ATTRIBUTE_NAMES = getAttributeNames();

    public static Classifier buildClassifier(String dataPath) {

        try {
            Log.d("weka", "Building a new classifier...");
            //Load data from file
            DataSource source = new DataSource(dataPath);
            Instances data = source.getDataSet();
            if (data.classIndex() == -1)
                data.setClassIndex(data.numAttributes() - 1);

            // train NaiveBayes
            Classifier classifier = new NaiveBayes();
            classifier.buildClassifier(data);

            // evaluate expected accuracy using 10 fold cross validation
            Classifier evaluator = new NaiveBayes();
            Evaluation eval = new Evaluation(data);
            eval.crossValidateModel(evaluator, data, 10, new Random(1));
            Log.d("weka", "The model just built will have a predicted accuracy of " + eval.pctCorrect() + "%");

            return classifier;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static Classifier loadClassifier(InputStream modelFile) {
        try {
            Log.d("weka", "Loading a classifier model from a file");
            return (NaiveBayes) SerializationHelper.read(modelFile);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /*
    public static boolean classify(Classifier cl, InputStream unlabeledData, String outfilePath) {

        try {

            CSVLoader loader = new CSVLoader();
            loader.setSource(unlabeledData);
            Instances unlabeled = loader.getDataSet();

            // 1. nominal attribute
            Add filter = new Add();
            filter.setAttributeIndex("last");
            filter.setNominalLabels(String.join(",", classes));
            filter.setAttributeName("label");
            filter.setInputFormat(unlabeled);
            unlabeled = Filter.useFilter(unlabeled, filter);


            // set class attribute
            unlabeled.setClassIndex(unlabeled.numAttributes() - 1);

            // create copy
            Instances labeled = new Instances(unlabeled);

            // label instances
            for (int i = 0; i < unlabeled.numInstances(); i++) {
                double clsLabel = cl.classifyInstance(unlabeled.instance(i));
                labeled.instance(i).setClassValue(clsLabel);
            }

            // save labeled data
            BufferedWriter writer = new BufferedWriter(
                    new FileWriter(outfilePath));
            writer.write(labeled.toString());
            writer.newLine();
            writer.flush();
            writer.close();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    */

    public static List<String> classify(Classifier cl, InputStream unlabeledData) {
        try {

            // load unlabeled data
            CSVLoader loader = new CSVLoader();
            loader.setSource(unlabeledData);
            Instances unlabeled = loader.getDataSet();

            unlabeled = addLabel(unlabeled);

            if(unlabeled == null) {
                Log.d("weka", "Error while adding label attribute to dataset");
                return null;
            }

            // set class attribute
            unlabeled.setClassIndex(unlabeled.numAttributes() - 1);

            // create copy
            Instances labeled = new Instances(unlabeled);

            // label instances
            for (int i = 0; i < unlabeled.numInstances(); i++) {
                double clsLabel = cl.classifyInstance(unlabeled.instance(i));
                labeled.instance(i).setClassValue(clsLabel);
            }

            // save labeled data
            List<String> classes = new ArrayList<>();
            for (Instance i : labeled) {
                classes.add(i.classAttribute().value((int) i.classValue()));
            }
            return classes;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String classify(Classifier cl, Instances unlabeled) {
        try {

            Instances labeled = new Instances(unlabeled);

            double clsLabel = cl.classifyInstance(unlabeled.instance(0));
            labeled.instance(0).setClassValue(clsLabel);
            return labeled.classAttribute().value((int) labeled.instance(0).classValue());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String classify(Classifier cl, Double[] samples) {
        Instances inst = buildInstances(samples);
        return classify(cl, inst);
    }

    private static Instances addLabel(Instances unlabeled) {
        try {
            // add class nominal attribute
            Add filter = new Add();
            filter.setAttributeIndex("last");
            filter.setNominalLabels(String.join(",", CLASSES));
            filter.setAttributeName("label");
            filter.setInputFormat(unlabeled);
            unlabeled = Filter.useFilter(unlabeled, filter);
            return unlabeled;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String[] getAttributeNames() {
        final String[] sensors = {"accel", "gyro", "rot"};
        final String[] axes = {"x", "y", "z"};
        final String[] indexes = {"mean", "median", "95", "max", "min", "range", "std_dev", "dom_freq", "dom_freq_2"};
        int len = sensors.length*axes.length*indexes.length;

        String[] attributes = new String[len];
        int index = 0;

        for (String s: sensors) {
            for (String a: axes) {
                for (String i: indexes) {
                    attributes[index] = s + "_" + a + "_" + i;
                    ++index;
                }
            }
        }

        return  attributes;
    }

    public static Instances buildInstances(Double[] values) {

        ArrayList<Attribute> attributes = new ArrayList<>();

        //add all numerical attributes
        for(String name: ATTRIBUTE_NAMES) {
            Attribute att = new Attribute(name);
            attributes.add(att);
        }

        //add label attribute
        Attribute lab = new Attribute("label", Arrays.asList(CLASSES));
        attributes.add(lab);

        Instances res = new Instances("sampleWindow", attributes, 1);

        res.setClassIndex(res.numAttributes() - 1);

        Instance newInstance = new DenseInstance(res.numAttributes());
        for(int i = 0 ; i < res.numAttributes() ; i++) {
            newInstance.setValue(i , values[i]);
        }
        res.add(newInstance);
        return res;
    }

}
