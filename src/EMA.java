import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class EMA {
    static char[] C = {'b','c','d','f','g','h','j','k','l','m','n','p','q','r','s','t','v','w','x','y','z'};
    static char[] V = {'a','e','i','o','u'};
    static String idxToChar = "a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,{";
    static String allChars = "abcdefghijklmnopqrstuvwxyz";
    static int total_chars = 26;
    static int vdim = total_chars + 1; // a-z + end
    static int endRand = 7; // how much do you want the end character to be like english's?
    static char stx = '␂'; // start of text
    static char nul = '␀'; // null placeholder for n > 2 ngrams
    static char end = '{'; // end of word
    static double k = 0.01; // small number 0 < k < 1
    static double inacc = -1; // inaccessible index
    static Vector smoothie = new Vector(k);
    static String path = System.getProperty("user.home") + "/Desktop/EMA/lib/";

    static Map<String, Ngram> models = new HashMap<>();

    public static void main(String[] args) throws FileNotFoundException {
        // Scanner sc1 = new Scanner(new File(path + "input.txt"));
        
        // while (sc1.hasNextLine()) {
        //     String line = clean(sc1.nextLine().trim());
        //     if (!"".equals(line)) {
        //         for (String word : line.split(" ")) {
        //             System.out.println(word);
        //         }
        //     }
        // }

        // System.out.println(substitution("i met a traveller from an antique land", "ZXYWVUTSRQMPONLKJIHGFEDCBA"));


        Ngram words = new Ngram(vdim, "words");
        Ngram boys = new Ngram(vdim, "boys");
        Ngram girls = new Ngram(vdim, "girls");
        Ngram names = new Ngram(vdim, "names");
        // Trigram elements = new Trigram(vdim, "elements");

        train(path + "MIT.txt", words);
        train(path + "boys.txt", boys);
        train(path + "girls.txt", girls);
        train(path + "boys.txt", names);
        train(path + "girls.txt", names);
        // train(path + "elements.txt", elements);

        models.put(words.name, words);
        models.put(boys.name, boys);
        models.put(girls.name, girls);
        models.put(names.name, names);
        // models.put(elements.name, elements);

        models.forEach((key, value) -> {
            value.addk();
        });

        models.forEach((key, value) -> {
            value.normalize();
        });

        models.forEach((key, value) -> {
            System.out.println(value.toString());
        });

        // words.bigram_raw.forEach((key, value) -> System.out.println(key + " " + value));
        // output_csv(elements);
        
        // models.forEach((key, value) -> {
        //     output_csv(value);
        // });

        StringBuilder models_names = new StringBuilder();
        models.forEach((key, value) -> models_names.append(value.name + ", "));

        String fourctions = "gmfpc";
        String norm_text = "Most definite features of ";
        String raw_text = "Most common features of ";
        String peek_text = "Most likely characters following the input from ";
        String error = "Features only works for bigram and above.";
        // System.out.println(peek(words.unigram));
        // System.out.println(peek(words.trigram.get("" + stx + 'b')));
        
        Scanner sc = new Scanner(System.in);
        String input = "";
        while (!input.equalsIgnoreCase("quit")) {
            System.out.println("Choose [\'g\'en / \'m\'ax / (\'f\'eatures / \'p\'eek / \'c\'heck)] + [" + models_names.toString() + "('a'll)] + [1, 2, 3, (0)] + [number of times repeated / (first # features / word)]");
            System.out.println("Or: [Solve] + [word] (caesar cipher only)");
            System.out.print("> ");
            input = sc.nextLine();
            String mode = input.substring(0, input.indexOf(" "));
            input = input.substring(input.indexOf(" ") + 1);

            if (fourctions.contains(mode)) {
                String selected = input.substring(0, input.indexOf(" "));
                input = input.substring(input.indexOf(" ") + 1);
                int xgram = Integer.parseInt(input.substring(0, input.indexOf(" ")));
                input = input.substring(input.indexOf(" ") + 1);
                String param = input;

                if (mode.equals("g") || mode.equals("m")) {
                    Ngram model = models.getOrDefault(selected, words);
                    int times = Integer.parseInt(param);
                    boolean max = mode.equals("max") || mode.equals("m");
                    
                    switch (xgram) {
                        case 1 -> {
                            for (int i = 0; i < times; i++) {
                                System.out.println(genUni(model, max));
                            }
                        }
                        case 2 -> {
                            for (int i = 0; i < times; i++) {
                                System.out.println(genBi(model, max));
                            }
                        }
                        default -> {
                            for (int i = 0; i < times; i++) {
                                System.out.println(genTri(model, max));
                            }
                        }
                    }
                } else if (mode.equals("c") || mode.equals("f") || mode.equals("p")) {
                    boolean check = mode.equals("c");
                    boolean feats = mode.equals("f");

                    Map<String, Ngram> selected_models = new HashMap<>();
                    if (selected.equals("a")) {
                        selected_models = models;
                    } else {
                        Ngram model = models.getOrDefault(selected, words);
                        selected_models.put(model.name, model);
                    }

                    if (xgram != 2 && xgram != 3) {
                        if (check) {
                            selected_models.forEach((key, value) -> {
                                System.out.println(value.name + " unigram perplexity: " + checkUni(param, value));
                                // System.out.println("length likelihood (" + word.length() + "): " + value.lengths.get(word.length() - 1) * 100 + "%");
                            });
                        } else if (feats) {
                            System.out.println(error);
                        } else {
                            selected_models.forEach((key, value) -> {
                                System.out.println(peek_text + "unigram \"" + value.name + "\": ");
                                System.out.println(peek(value.unigram));
                            });
                        }
                        System.out.println();
                    }
                    if (xgram != 1 && xgram != 3) {
                        if (check) {
                            selected_models.forEach((key, value) -> {
                                System.out.println(value.name + " bigram perplexity: " + checkBi(param, value));
                                // System.out.println("length likelihood (" + word.length() + "): " + value.lengths.get(word.length() - 1) * 100 + "%");
                            });
                        } else if (feats) {
                            selected_models.forEach((key, value) -> {
                                System.out.println(norm_text + "bigram \"" + value.name + "\": ");
                                System.out.println(find(value, Integer.parseInt(param), 2, false).toString());
                                System.out.println(raw_text + "bigram \"" + value.name + "\": ");
                                System.out.println(find(value, Integer.parseInt(param), 2, true).toString());
                            });
                        } else {
                            selected_models.forEach((key, value) -> {
                                String s = "" + nul + stx + ((param.contains("" + nul) || param.contains("" + stx))? "" : param);
                                System.out.println(peek_text + "bigram \"" + value.name + "\": ");
                                System.out.println(peek(value.bigram.get(s.substring(s.length() - 1))));
                            });
                        }
                        System.out.println();
                    }
                    if (xgram != 1 && xgram != 2) {
                        if (check) {
                            selected_models.forEach((key, value) -> {
                                System.out.println(value.name + " trigram perplexity: " + checkTri(param, value));
                                // System.out.println("length likelihood (" + word.length() + "): " + value.lengths.get(word.length() - 1) * 100 + "%");
                            }); 
                        } else if (feats) {
                            selected_models.forEach((key, value) -> {
                                System.out.println(norm_text + "trigram \"" + value.name + "\": ");
                                System.err.println(find(value, Integer.parseInt(param), 3, false).toString());
                                System.out.println(raw_text + "trigram \"" + value.name + "\": ");
                                System.err.println(find(value, Integer.parseInt(param), 3, true).toString());
                            });
                        } else {
                            selected_models.forEach((key, value) -> {
                                String s = "" + nul + stx + ((param.contains("" + nul) || param.contains("" + stx))? "" : param);
                                System.out.println(peek_text + "trigram \"" + value.name + "\": ");
                                System.out.println(peek(value.trigram.getOrDefault(s.substring(s.length() - 2), new Vector(inacc))));
                            });
                        }
                        System.out.println();
                    }
                    if (check) {
                        selected_models.forEach((key, value) -> {
                            System.out.println(value.name + " length likelihood (" + param.length() + "): " + value.lengths.get(param.length() - 1) * 100 + "%");
                        }); 
                    }
                }
            } else if (mode.equalsIgnoreCase("solve") || mode.equalsIgnoreCase("s")) {
                class Items {
                    String str;
                    int num;
                    double prob;
                    Items(String s, int n, double p) {
                        str = s;
                        num = n;
                        prob = p;
                    }
                }
                List<Items> list = new ArrayList<>();
                for (int i = 1; i <= total_chars; i++) {
                    String c = caesar(input, i);
                    list.add(new Items(c, i, checkTri(c, models.get("words"))));
                }
                list.sort((a, b) -> Double.compare(a.prob, b.prob));
                System.out.println("Decrypted Message: " + list.get(0).str);
                System.out.println("Which Was Encoded With: " + (26 - list.get(0).num));
                System.out.println("Final Perplexity: " + list.get(0).prob);
            } else if (!input.equalsIgnoreCase("quit")) {
                System.out.println("Invalid command, please try again.");
            }
        }
    }

    public static String clean(String text) {
        // Replace anything that is not a letter or whitespace with nothing
        return text.replaceAll("[^a-zA-Z\\s]", "").toLowerCase();
    }

    public static void output_csv(Ngram model) {
        // String unigram_name = path + "csv_" + model.name + "_unigram_sn.csv";
        // File unigram = new File(unigram_name);
        // try {
        //     unigram.createNewFile();
        //     FileWriter writer = new FileWriter(unigram_name);
        //     writer.write(idxToChar + "\n");
        //     writer.write("*," + model.unigram.toString());
        //     writer.close();
        // } catch (IOException e) {}

        // String bigram_name = path + "csv_" + model.name + "_bigram_sn.csv";
        // File bigram = new File(bigram_name);
        // try {
        //     bigram.createNewFile();
        //     FileWriter writer = new FileWriter(bigram_name);
        //     writer.write(idxToChar + "\n");
        //     model.bigram.forEach((key, value) -> {
        //         try {
        //             writer.write(key + "," + value + "\n");
        //         } catch (IOException e) {}
        //     });
        //     writer.close();
        // } catch (IOException e) {}

        String trigram_name = path + "csv_" + model.name + "_trigram_top.csv";
        File trigram = new File(trigram_name);
        try {
            trigram.createNewFile();
            FileWriter writer = new FileWriter(trigram_name);
            writer.write(idxToChar + "\n");
            model.trigram.forEach((key, value) -> {
                try {
                    writer.write(key + "," + value + "\n");
                } catch (IOException e) {}
            });
            writer.close();
        } catch (IOException e) {}
    }

    public static String caesar(String s, int num) {
        s = clean(s).toLowerCase();
        String output = "";
        for (int i = 0; i < s.length(); i++) {
            if (Character.isWhitespace(s.charAt(i))) {
                output += " ";
            } else {
                output += (char) ((s.charAt(i) + num - 'a') % total_chars + 'a');
            }
        }
        return output;
    }

    public static String substitution(String s, String key) {
        s = clean(s).toLowerCase();
        key = key.toLowerCase();
        String output = "";
        for (int i = 0; i < s.length(); i++) {
            if (Character.isWhitespace(s.charAt(i))) {
                output += " ";
            } else {
                output += (char) (key.charAt(s.charAt(i) - 'a'));
            }
        }
        return output;
    }

    public static String peek(Vector vec) {
        class Items {
            char ch;
            double prob;
            Items(char c, double p) {
                ch = c;
                prob = p;
            }
        }
        List<Items> list = new ArrayList<>();
        for (int i = 0; i < total_chars; i++) {
            list.add(new Items((char) (i + 'a'), vec.vals[i]));
        }
        list.sort((a, b) -> Double.compare(b.prob, a.prob));
        String output = "";
        for (int i = 0; i < total_chars; i++) {
            output += list.get(i).ch;
        }
        return output;
    }

    public static String genUni(Ngram model, boolean max) {
        int length = genInt(model.lengths) + 1;
        String word = "";
        if (max) {
            while (word.length() <= length && word.length() < vdim) {
                word += model.unigram.max();
            }
            return word;
        }
        while (word.length() <= length && word.length() < vdim) {
            word += genChar(model.unigram);
        }
        return word;
    }

    public static double checkUni(String word, Ngram model) {
        double sum = 0;
        for (int i = 0; i < word.length(); i++) {
            sum += Math.log(model.unigram.get(word.charAt(i) - 'a'));
        }
        return Math.exp(-1 * sum / word.length());
    }

    public static String genBi(Ngram model, boolean max) {
        int length = genInt(model.lengths) + 1;
        char c = stx;
        String word = "";
        while (word.length() <= length && word.length() < vdim) {
            char to;
            if (max) {
                to = model.bigram.get("" + c).max();
            } else {
                to = genChar(model.bigram.get("" + c));
            }
            word += to;
            c = to;
            if (word.length() == length) {
                // if (ends.get(to - 'a') > Math.random() / endRand) {
                if (model.bigram.get("" + to).get(vdim - 1) > Math.random() / endRand) {
                    return word;
                } else {
                    length++;
                }
            }
        }
        return word;
    }

    public static double checkBi(String word, Ngram model) {
        double sum = Math.log(model.bigram.get("" + stx).get(word.charAt(0) - 'a'));
        for (int i = 0; i < word.length() - 1; i++) {
            sum += Math.log(model.bigram.get("" + word.charAt(i)).get(word.charAt(i + 1) - 'a'));
        }
        return Math.exp(-1 * sum / word.length());
    }

    public static String genTri(Ngram model, boolean max) {
        int length = genInt(model.lengths) + 1;
        String s = "" + nul + stx;
        String word = "";
        while (word.length() <= length && word.length() < vdim) {
            char to;
            if (max) {
                to = model.trigram.getOrDefault(s, model.bigram.get("" + s.charAt(1))).max();
            } else {
                to = genChar(model.trigram.getOrDefault(s, model.bigram.get("" + s.charAt(1))));
            }
            word += to;
            s = "" + s.charAt(1) + to;
            if (word.length() == length) {
                // if (ends.get(to - 'a') > Math.random() / endRand) {
                if (model.bigram.get("" + to).get(vdim - 1) > Math.random() / endRand) {
                    return word;
                } else {
                    length++;
                }
            }
        }
        return word;
    }

    public static double checkTri(String word, Ngram model) {
        String s = "" + nul + stx;
        word += end;
        double sum = Math.log(model.trigram.getOrDefault(s, model.bigram.get("" + s.charAt(1))).get(word.charAt(0) - 'a'));
        for (int i = 0; i < word.length() - 1; i++) {
            s = "" + s.charAt(1) + word.charAt(i);
            sum += Math.log(model.trigram.getOrDefault(s, model.bigram.get("" + s.charAt(1))).get(word.charAt(i + 1) - 'a'));
        }
        return Math.exp(-1 * sum / word.length());
    }

    public static char genChar(Vector vec) {
        double randGoal = Math.random() * vec.total(false);
        double charRand = 0.0;
        int i = 0;
        while (charRand < randGoal && i < vdim) {
            charRand += vec.get(i);
            i++;
        }
        // System.out.println((char) (i + 'a'));
        // if (i == 26) {
        //     System.out.println(randG);
        //     System.out.println(charRand);
        // }
        return (char) (i + 'a' - 1);
    }

    public static int genInt(Vector vec) {
        double randGoal = Math.random();
        double charRand = 0.0;
        int i = 0;
        while (charRand < randGoal && i < 26) {
            charRand += vec.get(i);
            i++;
        }
        return i;
    }

    public static void train(String fileName, Ngram model) throws FileNotFoundException {
        model.directory.add(fileName);
        Scanner sc = new Scanner(new File(fileName));
        
        while (sc.hasNextLine()) {
            String word = "" + nul + stx + clean(sc.nextLine().trim()).toLowerCase() + end;
            if (word.equals("")) {
                break;
            }
            for (int i = 0; i < word.length() - 1; i++) {
                char c = word.charAt(i);
                // System.out.println(c);
                String s = "" + c + word.charAt(i + 1);
                if (model.bigram.get("" + c) != null) {
                    model.bigram.get("" + c).add(word.charAt(i + 1));
                }

                if (i < word.length() - 2) {
                    if (!model.trigram.containsKey(s)) {
                        model.trigram.put(s, new Vector(vdim));
                    }
                    model.trigram.get(s).add(word.charAt(i + 2));
                }
            }
            // System.out.println(word);
            model.lengths.add(word.length() - 4);
            model.ends.add(word.charAt(word.length() - 2));
        }

        double[] vals = new double[vdim];
        for (int i = 0; i < vdim - 1; i++) {
            vals[i] = model.bigram.get("" + (char) ('a' + i)).total();
        }
        model.unigram = new Vector(vals);

        model.tsize = model.bigram.get("" + stx).total();
        model.tcsize = model.unigram.total();
    }

    public static List<String> find(Ngram model, int first, int xgram, boolean raw) {
        if (raw) {
            xgram += 10;
        }
        class Items {
            String str;
            double prob;
            Items(String s, double p) {
                str = s;
                prob = p;
            }
        }
        first = Math.min(first, vdim * vdim);
        List<Items> list = new ArrayList<>();
        List<String> output = new ArrayList<>();
        Map<String, Vector> gram;
        if (xgram == 2) {
            gram = model.bigram;
        } else if (xgram == 3) {
            gram = model.trigram;
        } else if (xgram == 12) {
            gram = model.bigram_raw;
        } else {
            gram = model.trigram_raw;
        }
        for (Map.Entry<String, Vector> entry : gram.entrySet()) {
            String key = entry.getKey();
            Vector value = entry.getValue();
            for (int i = 0; i < value.dims(); i++) {
                char c = (char) (i + 'a');
                list.add(new Items(key + c, value.vals[i]));
            }
        }
        int i = 0;
        list.sort((a, b) -> Double.compare(b.prob, a.prob));
        for (Items item : list) {
            if (raw) {
                output.add(item.str + ": " + (int) (item.prob));
            } else {
                output.add(item.str + ": " + (((int) (item.prob * 1000)) / 10.0) + "%");
            }
            i++;
            if (i >= first) {
                return output;
            }
        }

        return output;
    }

    private static class Ngram {
        public Map<Integer, Map<String, Vector>> ngrams;
        public Map<Integer, Map<String, Vector>> ngrams_raw;

        public Map<String, Vector> quadgram;
        public Map<String, Vector> trigram;
        public Map<String, Vector> bigram;
        public Vector unigram;
        public Vector ends;
        public Vector lengths;
        public String name;
        public List<String> directory;
        public double tsize;
        public double tcsize;

        public Map<String, Vector> quadgram_raw;
        public Map<String, Vector> trigram_raw;
        public Map<String, Vector> bigram_raw;
        
        public Ngram(int size, String name) {
            this.quadgram = new HashMap<>();
            this.trigram = new HashMap<>();
            this.bigram = new HashMap<>();
            this.unigram = new Vector(size);
            this.ends = new Vector(size);
            this.lengths = new Vector(size);
            this.name = name;
            this.directory = new ArrayList<>();
            this.tsize = 1;
            this.tcsize = 1;

            this.quadgram_raw = new HashMap<>();
            this.trigram_raw = new HashMap<>();
            this.bigram_raw = new HashMap<>();

            this.bigram.put("" + stx, new Vector(size));
            for (char c = 'a'; c <= 'z'; c++) {
                this.bigram.put("" + c, new Vector(size));
            }
        }

        public void addk() {
            trigram.forEach((key, value) -> {
                trigram.get(key).add(smoothie, 1);
            });
            bigram.forEach((key, value) -> {
                bigram.get(key).add(smoothie, 1);
            });
            unigram.add(smoothie, 26);
            lengths.add(smoothie, 10);
            ends.add(smoothie, 26);
        }

        public void normalize() {
            for (Map.Entry<String, Vector> entry : trigram.entrySet()) {
                trigram_raw.put(entry.getKey(), new Vector(entry.getValue().vals));
            }
            for (Map.Entry<String, Vector> entry : bigram.entrySet()) {
                bigram_raw.put(entry.getKey(), new Vector(entry.getValue().vals));
            }
            
            trigram.forEach((key, value) -> {
                trigram.get(key).normalize(value.total());
            });
            bigram.forEach((key, value) -> {
                bigram.get(key).normalize(value.total());
            });
            unigram.normalize(tcsize);
            lengths.normalize(tsize);
            ends.normalize(tsize);
        }

        public String toString(){
            return "Model name: " + this.name + "\n" + 
                   "Trained with: " + this.directory + "\n" +
                   "Training word count: " + this.tsize + "\n" + 
                   "Training character count: " + this.tcsize + "\n";
        }
    }

    private static class Vector {
        public double[] vals;
    
        public Vector(int dims) {
            this.vals = new double[dims];
        }

        public Vector(double k) {
            this.vals = new double[vdim];
            for (int i = 0; i < vals.length; i++) {
                vals[i] = k;
            }
            // vals[vdim - 1] = k * (vdim - 2);
        }
        
        public Vector(double[] vals) {
            this.vals = Arrays.copyOf(vals, vals.length);
        }

        // public void temp(double num){
        //     this.vals[this.vals.length - 1] /= num;
        // }
    
        // Post: Returns the total number of dimensions this vector is storing
        public int dims() {
            return this.vals.length;
        }

        public double total() {
            double sum = 0;
            for (double x : this.vals) {
                sum += x;
            }
            return sum;
        }

        public double total(boolean genuine) {
            double sum = 0;
            for (double x : this.vals) {
                sum += x;
            }
            sum -= this.vals[vdim - 1];
            return sum;
        }

        private static double total(double[] v) {
            double sum = 0;
            for (double x : v) {
                sum += x;
            }
            return sum;
        }

        public double get(int i) {
            return this.vals[i];
        }

        public void add(char c) {
            // System.out.println(c);
            this.vals[c - 'a'] += 1.0;
            // return new Vector(this.vals);
        }

        public void add(int i) throws FileNotFoundException {
            if (i >= vdim || i < 0) {
                System.out.println("Pregnant Sonic");
                throw new FileNotFoundException();
            }

            this.vals[i] += 1.0;
            //return new Vector(this.vals);
        }

        private void add(Vector other, int factor) {
            for (int i = 0; i < other.vals.length; i++) {
                this.vals[i] += other.vals[i] * factor;
            }
        }

        public void normalize(double val) {
            for (int i = 0; i < vals.length; i++) {
                vals[i] = vals[i] / val;
            }
            // return new Vector(this.vals);
        }

        public char min(){
            double min = vals[0];
            int index = 0;
            for (int i = 1; i < vals.length - 1; i++) {
                if (vals[i] < min) {
                    min = vals[i];
                    index = i;
                }
            }
            return (char) (index + 'a');
        }

        public char max(){
            double max = vals[0];
            int index = 0;
            for (int i = 1; i < vals.length - 1; i++) {
                if (vals[i] > max) {
                    max = vals[i];
                    index = i;
                }
            }
            return (char) (index + 'a');
        }
    
        // Post: Returns whether or not other equals this
        public boolean equals(Object other) {
            if (other == null || !(other instanceof Vector)) {
                return false;
            } else {
                Vector o = (Vector)other;
                return Arrays.equals(o.vals, this.vals);
            }
        }
    
        // Post: returns a string representation of this vector
        public String toString() {
            StringBuilder output = new StringBuilder();
            for (int i = 0; ; i++) {
                output.append(vals[i]);
                if (i == vals.length - 1)
                    return output.toString();
                output.append(",");
            }
        }
    
        // // Post: Returns the norm / magnitude / euclidean length of this vector
        // public double norm() {
        //     double sum = 0;
        //     for (double x : this.vals) {
        //         sum += Math.pow(x, 2);
        //     }
        //     return Math.sqrt(sum);
        // }
    
        // Pre: Throws IllegalArgumentException if the provided vectors are different length
        // Post: Returns the result of adding the provided two vectors together
        public static Vector add(Vector one, Vector two, int factor) {
            return Vector.linear(one, two, true, factor);
        }
    
        // Pre: Throws IllegalArgumentException if the provided vectors are different length
        // Post: Returns the result of subtracting the provided two vectors
        public static Vector subtract(Vector one, Vector two, int factor) {
            return Vector.linear(one, two, false, factor);
        }
    
        // Pre: Throws IllegalArgumentException if the provided vectors are different length
        // Helper method: does linear addition / subtraction on provided vectors
        private static Vector linear(Vector one, Vector two, boolean add, int factor) {
            lengthCheck(one, two);
            Vector ret = new Vector(one.vals);
            for (int i = 0; i < two.vals.length; i++) {
                ret.vals[i] += (add ? 1 : -1) * two.vals[i] * factor;
            }
            return ret;
        }
    
        // Pre: Throws IllegalArgumentException if the provided vectors are different length
        // Post: Returns the dot product of the two provided vectors
        public static double dot(Vector one, Vector two) {
            lengthCheck(one, two);
            
            double sum = 0;
            for (int i = 0; i < one.vals.length; i++) {
                sum += (one.vals[i] * two.vals[i]);
            }
            return sum;
        }
    
        // // Pre: Throws IllegalArgumentException if the provided vectors are different length
        // // Post: Returns the cosine similarity of the two provided vectors
        // public static double cosine(Vector one, Vector two) {
        //     lengthCheck(one, two);
    
        //     return Vector.dot(one, two) / (one.norm() * two.norm());
        // }
    
        // Helper method: checks that the provided two vectors have the same length
        private static void lengthCheck(Vector one, Vector two) {
            if (one.vals.length != two.vals.length) {
                throw new IllegalArgumentException("Unable to add vectors of different lengths");
            }
        }
    }
}