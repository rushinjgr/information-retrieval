import java.io.*;
import java.util.*;

/**
 * CS 1655 -- ASSIGNMENT 1
 * INFORMATION RETRIEVAL
 * SUBMITTED 9/24/14
 * Created by Justin Rushin III
 */
public class preproc {

    static final String[] stems = {"ions","ion","ive","ed","ing","ly","es","s"}; //stems to remove
    static final String stopWordsURI = "hw1.stopwords.txt"; //location of stopwords file
    public static void main(String[] args) {
        String fileURI = args[0];
        StringTokenizer tk = new StringTokenizer(readTxt(fileURI)," ':.,:;!?-");
        if(tk == null){
            System.out.println("ERROR: File not found");
            return;
        }
        //store the words and their counts in this custom data structure
        WordData wordFreq = new WordData();
        //load the stopwords
        ArrayList<String> stopwords = populateStopWords(stopWordsURI);
        String temp = new String();
        Integer count;
        //use the increment function of our data structure to add/implement each time a word is found
        while(tk.hasMoreTokens()){
            temp = tk.nextToken().toLowerCase();
                //if the word is not a stopword, add it to the Hashmap or increment it's count
                if(!(stopwords.contains(temp))){
                    wordFreq.increment(destem(temp));
                }
            }
        //output in the specified csv format
        writeCount(fileURI.substring(0,fileURI.length()-3) + "counts",wordFreq.data);
    }

    //writes the .counts file
    //accepts file name as input, TreeSet of data items
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

    //input: string to de stem, returns string with stems removed
    public static String destem(String toStem){
        StringBuilder result = new StringBuilder(toStem);
        int stemLength;
        int temp;
        boolean stemmed=false;    //flag so we don't stem twice
        for(String stem: stems){
            stemLength = stem.length();
            //avoid out of bounds error
            temp = ((result.length())-stemLength);;
            if ((temp )>=0 && !stemmed){
                //remove the stem from the string
                if (result.substring(temp, result.length()).compareToIgnoreCase(stem) == 0) {
                    result.delete(temp, result.length());
                    stemmed = true;
                }
            }
        }
        return result.toString();
    }

    //populates stopwords to arraylist, given URI of file containing stopwords
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

    //reads txtfile to string, given URI
    public static String readTxt(String URI) {
        String content = "";
        try {
            content = new Scanner(new File(URI)).useDelimiter("\\Z").next();
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
        return content;
    }

    //tracks a word and how many times it was counted in a file
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

    //tracks all the words/counts in the file
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

        //if the word was already used in this file, increment increments the count
        //otherwise, it adds it to our data structure for storing it
        //this way we don't have to worry about it in higher level methods
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