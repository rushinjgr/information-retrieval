package hw1;

import java.io.*;
import java.util.*;

public class preproc {

    static final String[] stems = {"ion","ions","ive","ed","ing","ly","s","es"};
    static final String stopWordsURI = "hw1.stopwords.txt";
    public static void main(String[] args) {
        //read file
        String fileURI = args[0];
        StringTokenizer tk = new StringTokenizer(readTxt(fileURI)," :.,:;!?-");
        WordData wordFreq = new WordData();
        ArrayList<String> stopwords = populateStopWords(stopWordsURI);
        String temp = new String();
        Integer count;
        while(tk.hasMoreTokens()){
            temp = tk.nextToken().toLowerCase();
                //if the word is not a stopword, add it to the Hashmap or increment it's count
                if(!(stopwords.contains(temp))){
                    wordFreq.increment(destem(temp));
                }
            }
        writeCount(fileURI.substring(0,fileURI.length()-3) + "counts",wordFreq.data);
    }

    public static void writeCount(String fURI, TreeSet<Word> data ){
        FileWriter fstream = null;
        try {
            fstream = new FileWriter(fURI, false);
            BufferedWriter out = new BufferedWriter(fstream);
            for(Word z: data){
                out.write(z.word + "," + z.count + "\n");
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String destem(String toStem){
        StringBuilder result = new StringBuilder(toStem);
        int stemLength;
        int temp;
        for(String stem: stems){
            stemLength = stem.length();
            //avoid out of bounds error
            temp = ((result.length())-stemLength);;
            if ((temp )>=0){
                if (result.substring(temp, result.length()).compareToIgnoreCase(stem) == 0) {
                    result.delete(temp, result.length());
                }
            }
        }
        return result.toString();
    }
    public static ArrayList<String> populateStopWords(String URI){
        ArrayList<String> result = new ArrayList<String>();
        BufferedReader reader = null;
        String s;
        try {
            reader = new BufferedReader(new FileReader(URI));
            while ((s = reader.readLine()) != null) {
                result.add(s);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String readTxt(String URI) {
        String content = "";
        try {
            content = new Scanner(new File(URI)).useDelimiter("\\Z").next();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    private static class Word implements Comparable{
        Word(String inword, int c){
            word = inword;
            count = c;
        }

        public String word;
        public int count;

        @Override
        public int compareTo(Object o) {
            Word w = (Word) o;
            int temp = this.word.compareToIgnoreCase(w.word);
            if(temp ==0){
                return 0;
            }
            else if(temp < 0){
                return -1;
            }
            else{
                return 1;
            }
        }
    }

    private static class WordData{
        TreeSet<Word> data = new TreeSet<Word>();

        public int getCount(String target) {
            for(Word w: data){
                if(w.word.compareToIgnoreCase(target) == 0){
                    return w.count;
                }
            }
            return 0;
        }

        public int increment(String target){
            boolean added = false;
            for(Word w: data){
                if(w.word.compareToIgnoreCase(target) == 0){
                    w.count++;
                    added = true;
                    return w.count;
                }
            }
            if(!(added)){
                data.add(new Word(target,1));
            }
            return 1;
        }
    }

}