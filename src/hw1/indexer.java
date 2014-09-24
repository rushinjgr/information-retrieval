package hw1;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * Created by Justin Rushin III on 9/22/14.
 */
public class indexer {
    public static void main(String[] args) {
        //TODO add error handling for if no files found
        Index index = new Index();
        File folder = new File("./");
        File[] listOfFiles = folder.listFiles();
        ArrayList<File> targets = new ArrayList<File>();
        String temp;
        for (File file : listOfFiles) {
            if (file.isFile()) {
                temp = file.getName();
                if((temp.substring(temp.lastIndexOf("."),temp.length()).compareToIgnoreCase(".counts"))==0){
                    targets.add(file);
                }
            }
        }
        for(File target : targets){
            try {
                BufferedReader br = new BufferedReader(new FileReader(target));
                String line;
                String name = target.getName();
                name = name.substring(0,name.lastIndexOf("."));
                while ((line = br.readLine()) != null) {
                    line = line.substring(0,line.indexOf(","));
                    index.termFileAdd(line,name);
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        index.writeToFile("inverted.index");
    }

    public static class Term{
        public String term;
        public LinkedList<String> files;
        public Term(String t){
            term = t;
            files = new LinkedList<String>();
        }
    }

    public static class Index{
        public ArrayList<Term> termIndex;

        public Index(){
            termIndex = new ArrayList<Term>();
        }
        public boolean termFileAdd(String str,String fname){
            boolean contained = false;
            Iterator<Term> it = termIndex.iterator();
            Term temp;
            while(!contained && it.hasNext()){
                temp = it.next();
                if(temp.term.compareToIgnoreCase(str)==0){
                    temp.files.add(fname);
                    contained = true;
                }
            }
            if(!contained){
                temp = new Term(str);
                temp.files.add(fname);
                termIndex.add(temp);
                contained = true;
            }
            return contained;
        }

        public void writeToFile(String fname) {
            FileWriter fstream = null;
            try {
                fstream = new FileWriter(fname, false);
                BufferedWriter out = new BufferedWriter(fstream);
                for (Term term : termIndex) {
                    out.write(term.term);
                    for (String fil : term.files) {
                        out.write("," + fil);
                    }
                    out.write("\n");
                }
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
