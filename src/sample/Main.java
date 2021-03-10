package sample;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.awt.*;
import java.io.*;
import java.math.*;

public class Main extends Application {

    private static Map<String, Integer> wordCounts;
    private Map<String, Integer> trainSpamFreq;
    private Map<String, Integer> trainHamFreq;
    private Map<String, Double> trainFreq;
    private Map<String, Double> testFreq;

    private static long szs;
    private static long szh;
    private static boolean spam;
    private static boolean test;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("CSCI 2020 Assignment 1");
        primaryStage.setScene(new Scene(root, 1000, 800));
        primaryStage.show();
    }

    public Main(){
        this(0, 0, false, false);
    }

    public Main (long sizes, long sizeh, boolean spamtest, boolean testtest){
        wordCounts = new TreeMap<>();
        trainSpamFreq = new TreeMap<>(wordCounts);
        trainHamFreq = new TreeMap<>(wordCounts);
        testFreq = new TreeMap<>();
        trainFreq= new TreeMap<>();

        szs=sizes;
        szh=sizeh;
        spam=spamtest;
        test=testtest;
    }

    public static void clearmap (){
        wordCounts.clear();
    }

    public static long getszs (){
        return szs;
    }

    public static long getszh (){
        return szh;
    }

    public static void setszs (long n){
        szs=n;
    }

    public static void setszh (long n){
        szh=n;
    }

    public static boolean getspam (){
        return spam;
    }

    public static boolean gettest (){
        return test;
    }

    public static void setspam (boolean t){
        spam=t;
    }

    public static void settest (boolean t){
        test=t;
    }

    public void parseFile(File file) throws IOException{
        //System.out.println("Starting parsing the file:" + file.getAbsolutePath());
        Set<String> repeats = new HashSet <String>();

        if(file.isDirectory()){
            //parse each file inside the directory
            File[] content = file.listFiles();
            for(File current: content){
                parseFile(current);
            }
        }
        else {	//if the word has been counted in the file, ignore the rest of the word occurances in the file
            Scanner scanner = new Scanner(file);

            while (scanner.hasNext()){
                String word = scanner.next();

                if (isValidWord(word)){
                    word.equalsIgnoreCase(word);

                    repeats.add(word); //adding words to the set called repeats
                    if(wordCounts.containsKey(word)){
                        int previous = wordCounts.get(word);
                        wordCounts.put(word, previous);
                    }
                    else{ //if it's the first occurance, value is set as 1
                        wordCounts.put(word, 1);
                    }

                }
            }
            scanner.close();
        }
        iteration(repeats); //ensures a max of one word is counted per file
    }

    public void iteration(Set<String> repeats){
        Iterator<String> iterator = repeats.iterator();

        while (iterator.hasNext()){
            String key = iterator.next();

            //adds +1 to "wordCount" of words in "repeats"  , set ensures uniqueness
            int previous = wordCounts.get(key);
            wordCounts.put(key, previous+1);
        }
    }

    //calculates P(S|F) frequency for testing
    public void testingfreq(){
        Set<String> keys = trainFreq.keySet();
        Iterator<String> iterator = keys.iterator();

        while (iterator.hasNext()){
            String key = iterator.next();
            double n =0;

            if (test){
                n+= Math.log(1-trainFreq.get(key))-Math.log(trainFreq.get(key));

                testFreq.put(key, 1/(1+Math.pow(Math.E, n)));
            }
        }
    }

    //calculates overall frequency of P(S|W) for training
    public void freq(){
        Set<String> keys = wordCounts.keySet();
        Iterator<String> keyIterator = keys.iterator();

        while(keyIterator.hasNext()){
            String key = keyIterator.next();
            double spamk=0;
            double hamk=0;

            if (trainSpamFreq.containsKey(key))
                spamk= trainSpamFreq.get(key);
            if (trainHamFreq.containsKey(key))
                hamk= trainHamFreq.get(key);

            double index = spamk/(spamk+hamk);
            trainFreq.put(key, index);
        }
    }

    private boolean isValidWord(String word){
        String allLetters = "^[a-zA-Z]+$";
        // returns true if the word is composed by only letters, otherwise returns false;
        return word.matches(allLetters);
    }

    //outputs into given output file in a filename : wordcount format
    public void outputWordCount(int minCount, File output) throws IOException{
        System.out.println("Saving word counts to file:" + output.getAbsolutePath());
        System.out.println("Total words:" + wordCounts.keySet().size());

        if (!output.exists()){
            output.createNewFile();
            if (output.canWrite()){
                PrintWriter fileOutput = new PrintWriter(output);
                freq();
                Set<String> keys = trainFreq.keySet();
                Iterator<String> keyIterator = keys.iterator();

                while(keyIterator.hasNext()){

                    String key = keyIterator.next();
                    double count = wordCounts.get(key);
                    int index=  wordCounts.get(key);
                    double sz=1.0;

                    if (getspam()){
                        sz=getszs();
                        trainSpamFreq.put(key, index);
                    }
                    else{
                        sz=getszh();
                        trainHamFreq.put(key, index);
                    }

                    // testing minimum number of occurances
                    if(count>=minCount && sz>0){
                        fileOutput.println(key + ": " + count/sz);
                    }
                }

                fileOutput.close();
            }
        }
        else{
            System.out.println("Error: the output file already exists: " + output.getAbsolutePath());
        }
    }

    //calculates total file size of the directory folder
    public static long folderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }

    /* //calculates true and false positive values (theoretically)
    public void calculate(File current, Map <String, Double> map) {
        double numTrue = 0;
        double numFalsePos = 0;
        double numTruePos = 0;

        if (current.getParent().contains("ham") && prob < 0.5) {
            numTrue++;
        } else if (current.getParent().contains.equals("spam") && prob > 0.5) {
            numTrue++;
            numTruePos++;
        } else if (current.getParent().contains.equals("ham") && prob > 0.5) {
            numFalsePos++;
        }
    }*/

    public static void main(String[] args) {

        System.out.println("Hello, this program is running!");

        if(args.length < 2){
            System.err.println("Usage: java WordCounter <inputDir> <outfile>");
            System.exit(0);
        }

        File dataDir = new File(args[0]);
        File outFile = new File(args[1]); // File outFile = getDirName(); doesn't work


        Main wordCounter = new Main (0, 0, false, false);

        try{
            if(dataDir.isDirectory()){
                //parse each file inside the directory
                File[] content = dataDir.listFiles();
                for(File curr: content){
                    String name = curr.getName();
                    setspam(true);
                    clearmap();

                    if (name.contains("spam") && curr.isDirectory()){
                        szs= folderSize(curr);
                    }
                    else if (name.contains("ham")&& curr.isDirectory()){
                        szh= folderSize(curr);
                        setspam(false);
                    }

                    for(File current: content){
                        //parseFile(current);
                        wordCounter.parseFile(current);
                    }
                }
                wordCounter.outputWordCount(2, outFile);
            }
        }catch(FileNotFoundException e){
            System.err.println("Invalid input dir: " + dataDir.getAbsolutePath());
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        launch(args);
    }
}
