package com.example.wekamanager;

import android.util.Log;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Random;

public class WekaManager {

    public static Classifier getNeuralNetwork(String dataPath) {

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

    public static boolean classify(Classifier cl, String unclassifiedDataPath, String outfilePath) {

        try {

            // load unlabeled data
            Instances unlabeled = new Instances(
                    new BufferedReader(
                            new FileReader(unclassifiedDataPath)));

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

}
