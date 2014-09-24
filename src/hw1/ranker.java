package hw1;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.StringTokenizer;

import static java.util.Collections.sort;

/**
 * Created by Justin Rushin III on 9/23/14.
 */
public class ranker {


    public static void main(String[] args){
        indexer.Index index = new indexer.Index();
        index = readFromFile("inverted.index", index);
        //determine if the keywords from args are in the index
        int count = 0;
        ArrayList<Keyword> keywords = new ArrayList<Keyword>();
        String temp;
        char[] arr;
        while((count < 4) && (count < args.length)){
            temp = removePunct(args[count].toCharArray());
            temp = preproc.destem(temp.toLowerCase());
            if(temp.length() >0){
                keywords.add(count,new Keyword(temp));
            }
            count++;
        }

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

        //TODO handle missing files from index gracefully
        ArrayList<String> targetFiles = getKeyFiles(keywords,index);
        for(String target : targetFiles){
            keywords = keywordCount(target,keywords);
        }

        ArrayList<SearchFile> searchFiles = new ArrayList<SearchFile>();
        for(String filey : targetFiles){
            SearchFile newsf = new SearchFile();
            newsf.fname = filey;
            searchFiles.add(newsf);
        }

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
            System.out.println(kw.toString());
        }

        rankandWriteResults(searchFiles);
    }

    public static void rankandWriteResults(ArrayList<SearchFile> searchFiles){
        Collections.sort(searchFiles);
        for(SearchFile f : searchFiles){
            System.out.println(f.fname + " " + f.relevance);
        }
    }

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
            e.printStackTrace();
        }
        return keywords;
    }

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

    public static String removePunct(char[] arr){
        StringBuilder result = new StringBuilder();
        for(char c: arr){
            if ((c == ' ') || (c == '.') || (c == ',') || (c == ':') || (c == ';') || (c == '!') || (c == '?') || (c == '-')){
                //as per piazza discussion on handling of characters
                result.append(" ");
            }
            else{
               result.append(c);
            }
        }
        return (result.toString());
    }

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

    private static class SearchFile implements Comparable{
        String fname;
        Double relevance;
        int rank;

        public SearchFile(){
            fname = "";
            relevance = 0.0;
            rank = 0;
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
