import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.StringTokenizer;

import static java.util.Collections.sort;

/**
 * CS 1655 -- ASSIGNMENT 1
 * INFORMATION RETRIEVAL
 * SUBMITTED 9/24/14
 * Created by Justin Rushin III
 */
public class ranker {
    public static void main(String[] args){
        indexer.Index index = new indexer.Index();
        //populate the inverted index
        index = readFromFile("inverted.index", index);
        //determine if the keywords from args are in the index
        int count = 0;
        ArrayList<Keyword> keywords = new ArrayList<Keyword>();
        String temp;
        char[] arr;
        //this is the filename of the output file
        StringBuilder fURI = new StringBuilder();
        fURI.append("output.");

        //process the input search terms, no more than 3
        while((count < 4) && (count < args.length)){
            if(count > 0){
                //separator in the filename
                fURI.append("+");
            }

            //perform the preprocessing on the search terms
            temp = removePunct(args[count].toCharArray());
            temp = preproc.destem(temp.toLowerCase());

            //add the search term to the file
            fURI.append(args[count]);

            //if the search term is still valid after preprocessing, add it
            if(temp.length() >0){
                keywords.add(count,new Keyword(temp));
            }
            count++;
        }


        //find the number of count files in the working directory
        int countFiles = 0;
        File folder = new File("./");
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                temp = file.getName();
                if((temp.substring(temp.lastIndexOf("."),temp.length()).compareToIgnoreCase(".counts"))==0){
                    countFiles++;
                }
            }
        }

        //find out how many times the keywords are in each count file
        ArrayList<String> targetFiles = getKeyFiles(keywords,index);
        for(String target : targetFiles){
            keywords = keywordCount(target,keywords);
        }

        //if a count file from the index was not found, keywords will be null
        //print error msg and exit
        if (keywords == null){
            System.out.println("ERROR: file corrupt/not found");
            return;
        }

        //get the files that contain the relevant keywords
        ArrayList<SearchFile> searchFiles = new ArrayList<SearchFile>();
        for(String filey : targetFiles){
            SearchFile newsf = new SearchFile();
            newsf.fname = filey;
            searchFiles.add(newsf);
        }

        //for each search term
        //calculate the relevance of each file that contains the term
        //add the score for that keyword to the file and accumulate to its total score
        double calc;
        for(Keyword kw: keywords){
            calc = (double) countFiles / (double) kw.frequency;
            calc = (Math.log(calc) / Math.log(2.0));
            for(Keyword.KeyCount kc : kw.keycounts){
                kc.score = (Math.log((double)kc.count)/Math.log(2.0));
                kc.score = (1+ kc.score);
                kc.score = kc.score * calc;
                for(SearchFile filey : searchFiles){
                    if(filey.fname.compareTo(kc.file)==0){
                        filey.relevance+=kc.score;
                    }
                }
            }
        }

        //add the extensions to the file name
        fURI.append(".txt");

        //rank the results and write them to the file
        rankandWriteResults(fURI.toString(), searchFiles);
    }

    //sorts the rank of each file for the queried term(s) and writes the results to the file
    public static void rankandWriteResults(String fURI, ArrayList<SearchFile> searchFiles){
        Collections.sort(searchFiles);
        FileWriter fstream = null;
        try {

            fstream = new FileWriter(fURI, false);
            BufferedWriter out = new BufferedWriter(fstream);
            int rank = 1;
            DecimalFormat fmt = new DecimalFormat("#.###");
            for(SearchFile f : searchFiles){
               out.write(rank + "," + f.fname + "," + fmt.format(f.relevance) + "\n");
               rank++;
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //counts the keywords in a provided file
    //stores the count with the relevant keyword
    public static ArrayList<Keyword> keywordCount(String targetFile, ArrayList<Keyword> keywords){
        FileReader fstream = null;
        StringTokenizer tk;
        try {
            fstream = new FileReader(targetFile + ".counts");
            BufferedReader in = new BufferedReader(fstream);
            String line;
            while((line = in.readLine())!=null){
                for(Keyword k: keywords){
                    tk = new StringTokenizer(line," ,");
                    if(k.keyword.compareTo(tk.nextToken())==0){
                        Keyword.KeyCount current = new Keyword.KeyCount();
                        current.file = targetFile;
                        current.count = Integer.parseInt(tk.nextToken());
                        k.keycounts.add(current);
                        k.frequency++;
                    }
                }
            }
            in.close();
        } catch (IOException e) {
            return null;
        } catch (NullPointerException f){
            return null;
        }

        return keywords;
    }

    //takes the array of keywords and finds the relevant files that contain one or more of the keywords
    public static ArrayList<String> getKeyFiles(ArrayList<Keyword> keywords,indexer.Index index){
        ArrayList<String> keyFiles = new ArrayList<String>();
        for (Keyword k : keywords) {
            for (indexer.Term t : index.termIndex) {
                if(t != null && (k.keyword.compareTo(t.term)==0)){
                    for(String f : t.files){
                        if(!(keyFiles.contains(f))){
                            keyFiles.add(f);
                        }
                    }
                }
            }
        }
        return keyFiles;
    }

    //remove punctuation from a char array and return it as a string
    public static String removePunct(char[] arr){
        StringBuilder result = new StringBuilder();
        for(char c: arr){
            if ((c == ' ') || (c == '.') || (c == ',') || (c == ':') || (c == ';') || (c == '!') || (c == '?') || (c == '-') || (c == '\'')){
                //as per piazza discussion on handling of characters
                result.append(" ");
            }
            else{
               result.append(c);
            }
        }
        return (result.toString());
    }

    //adds a file's information to the provided index
    //essentially the reverse of the store index to file method from the indexer.java
    public static indexer.Index readFromFile(String fname, indexer.Index index){
        FileReader fstream = null;
        StringTokenizer tk;
        String temp;
        try {
            fstream = new FileReader(fname);
            BufferedReader in = new BufferedReader(fstream);
            String line;
            while((line = in.readLine())!=null){
                tk = new StringTokenizer(line," ,");
                temp = tk.nextToken();
                //associate the relevant files with the search term
                while(tk.hasMoreTokens()){
                    index.termFileAdd(temp,tk.nextToken());
                }
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return index;
    }

    //structure for a user inputted keyword
    //allows us to associate the overall frequency across multiple files
    //allows us to associate KeyCount objects
    //KeyCount is child class, stores the count and score for the term for a file
    private static class Keyword{
        public String keyword;
        public int frequency;
        public ArrayList<KeyCount> keycounts;

        public Keyword(String w){
            keyword = w;
            keycounts = new ArrayList<KeyCount>();
            frequency = 0;
        }

        public static class KeyCount{
            String file;
            int count;
            double score;

            public KeyCount(){
                file = null;
                count = 0;
                score = 0.0;
            }
        }

        public String toString(){
            StringBuilder s = new StringBuilder();
            s.append(keyword);
            s.append(" - ");
            s.append("FREQ:");
            s.append(frequency);
            s.append("\n");
            for(KeyCount kc : keycounts){
                s.append("FILE:"+kc.file);
                s.append(" - ");
                s.append("COUNT:"+kc.count);
                s.append(" - ");
                s.append("SCORE:" + kc.score);
                s.append("\n");
            }
            return s.toString();
        }
    }

    //provides string for filename and a double to associate relevance score
    private static class SearchFile implements Comparable{
        String fname;
        Double relevance;

        public SearchFile(){
            fname = "";
            relevance = 0.0;
        }

        public String toString(){
            StringBuilder s = new StringBuilder();
            s.append("NAME:"+fname);
            s.append(" - ");
            s.append("RELEVANCE:"+relevance);
            s.append(" - ");
            s.append("RANK:"+relevance);
            return (s.toString());
        }

        @Override
        public int compareTo(Object o) {
            SearchFile target = (SearchFile) o;
            double us = this.relevance;
            double them = target.relevance;
            if(them > us){
                return 1;
            }
            else if(them == us){
                return 0;
            }
            else {
                return -1;
            }

        }
    }
}
