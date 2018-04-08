package com.example.MyVoc;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MyActivity extends Activity {

    private int currentLayout;
    private int position;
    private ArrayList<Group> groups = new ArrayList<>();
    private ArrayList<Word> words = new ArrayList<>();
    private String filesDir = "/MyVoc";
    private String wordFile = "Wordlist.txt";
    //private String vocFile = "voc.txt";
    private Word currentWord;
    private ArrayList<String> currentGroups = new ArrayList<>();
    private ArrayList<Integer> currentGroupNumbers = new ArrayList<>();
    private Sort sort = Sort.ALPHABET;
    private Focus focus = new Focus(Unit.DEFAULT, -1, -1);
    public Column column = Column.BOTH;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivity(R.layout.word_list);
    }

    private void setActivity(int layout) {
        this.setContentView(layout);
        switch (layout) {
            case R.layout.word_list:
                ((EditText) findViewById(R.id.edit_search)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_GO)
                            createNewWord();
                        return false;
                    }
                });
                ((EditText) findViewById(R.id.edit_search)).addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (before != count) {
                            String str = String.valueOf(((EditText) findViewById(R.id.edit_search)).getText());
                            if (str.isEmpty())
                                buildList(true);
                            else {
                                if (str.length() == 1) {
                                    Collections.sort(words, new Comparator<Word>() {
                                        public int compare(Word w1, Word w2) {
                                            return w1.foreignWord.compareTo(w2.foreignWord);
                                        }
                                    });
                                    buildList(false);
                                }
                                search(str);
                            }
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                    return;
                inputWords(findFile(filesDir, wordFile));
                buildList(true);
                break;
            case R.layout.word_info:
                currentGroupNumbers = (ArrayList<Integer>) currentWord.groupNumber.clone();
                ((EditText) findViewById(R.id.edit_foreign_word)).setText(currentWord.foreignWord);
                if (focus.unit == Unit.FOREIGNWORD || focus.unit == Unit.TRANSLATIONS)
                    ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).toggleSoftInput(0, 0);
                if (focus.unit == Unit.FOREIGNWORD)
                    findViewById(R.id.edit_foreign_word).requestFocus();
                switch (currentWord.partOfSpeech) {
                    case UNKNOWN:
                        ((RadioButton) findViewById(R.id.radio_unknown)).toggle();
                        break;
                    case N:
                        ((RadioButton) findViewById(R.id.radio_n)).toggle();
                        break;
                    case V:
                        ((RadioButton) findViewById(R.id.radio_v)).toggle();
                        break;
                    case ADJ:
                        ((RadioButton) findViewById(R.id.radio_adj)).toggle();
                        break;
                    case ADV:
                        ((RadioButton) findViewById(R.id.radio_adv)).toggle();
                        break;
                    case PR:
                        ((RadioButton) findViewById(R.id.radio_pr)).toggle();
                        break;
                }
                ((RadioGroup) findViewById(R.id.group_part_of_speech)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        switch (checkedId) {
                            case R.id.radio_unknown:
                                currentWord.partOfSpeech = PartOfSpeech.UNKNOWN;
                                break;
                            case R.id.radio_n:
                                currentWord.partOfSpeech = PartOfSpeech.N;
                                break;
                            case R.id.radio_v:
                                currentWord.partOfSpeech = PartOfSpeech.V;
                                break;
                            case R.id.radio_adj:
                                currentWord.partOfSpeech = PartOfSpeech.ADJ;
                                break;
                            case R.id.radio_adv:
                                currentWord.partOfSpeech = PartOfSpeech.ADV;
                                break;
                            case R.id.radio_pr:
                                currentWord.partOfSpeech = PartOfSpeech.PR;
                                break;
                        }
                    }
                });
                buildInfo();
                (findViewById(R.id.button_delete)).setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        String search = words.get(position).foreignWord;
                        for (int i = 0; i < words.get(position).groupNumber.size(); i++) {
                            int n = words.get(position).groupNumber.get(i);
                            int m;
                            if (n > -1) {
                                groups.get(i).amount--;
                                for (Word w : words) {
                                    m = w.groupNumber.get(i);
                                    if (m > n)
                                        w.groupNumber.set(i, m - 1);
                                }
                            }
                        }
                        int n = words.get(position).time;
                        for (Word w : words) {
                            if (w.time > n)
                                w.time--;
                        }
                        words.remove(position);
                        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                            return false;
                        outputWords(findFile(filesDir, wordFile));
                        setActivity(R.layout.word_list);
                        search(search);
                        return false;
                    }
                });
                break;
        }
        currentLayout = layout;
    }

    private void createNewWord() {
        if (String.valueOf(((EditText) findViewById(R.id.edit_search)).getText()).isEmpty())
            focus.unit = Unit.FOREIGNWORD;
        else
            focus = new Focus(Unit.TRANSLATIONS, 0, 0);
        ArrayList<Integer> nums = new ArrayList<>();
        for (Group g : groups)
            if (g.last) {
                nums.add(g.amount);
                g.amount++;
            } else
                nums.add(-1);
        words.add(new Word(String.valueOf(((EditText) findViewById(R.id.edit_search)).getText()), words.size(), nums, PartOfSpeech.UNKNOWN, new ArrayList<>()));
        position = words.size() - 1;
        currentWord = words.get(position);
        setActivity(R.layout.word_info);
    }

    @Override
    public void onBackPressed() {
        switch (currentLayout) {
            case R.layout.word_list:
                setActivity(R.layout.main_menu);
                break;
            case R.layout.word_info:
                currentWord.foreignWord = String.valueOf(((EditText) findViewById(R.id.edit_foreign_word)).getText());
                verifyCurrentWords();
                for (int i = 0; i < currentGroupNumbers.size(); i++) {
                    int n = currentWord.groupNumber.get(i);
                    int m = currentGroupNumbers.get(i);
                    if (m != n) {
                        if (n == -1) {
                            int k;
                            for (Word w : words) {
                                k = w.groupNumber.get(i);
                                if (k > n)
                                    w.groupNumber.set(i, k - 1);
                            }
                        }
                        if (n > -1 && m > -1) {
                            int k;
                            for (Word w : words) {
                                k = w.groupNumber.get(i);
                                if (k > m)
                                    w.groupNumber.set(i, k - 1);
                            }
                        }
                    }
                    currentWord.groupNumber.set(i, n);
                }
                words.set(position, currentWord);
                for (int i = 0; i < currentWord.groupNumber.size(); i++)
                    groups.get(i).last = currentWord.groupNumber.get(i) > -1;
                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                    return;
                outputWords(findFile(filesDir, wordFile));
                setActivity(R.layout.word_list);
                search(words.get(position).foreignWord);
                break;
            case R.layout.main_menu:
                finish();
                break;
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_sort:
                switch (sort) {
                    case ALPHABET:
                        sort = Sort.TIME;
                        break;
                    case TIME:
                        sort = Sort.ALPHABET;
                        break;
                }
                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                    return;
                outputWords(findFile(filesDir, wordFile));
                buildList(true);
                break;
            case R.id.button_groups:
                Dialog dialog = new Dialog(this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.voc_groups);
                dialog.show();
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        int n = groups.size();
                        for (int i = 0; i < groups.size(); i++)
                            groups.get(i).name = currentGroups.get(i).trim();
                        for (int i = 0; i < groups.size(); i++)
                            if (groups.get(i).name.isEmpty()) {
                                groups.remove(i);
                                for (Word w : words)
                                    w.groupNumber.remove(i);
                                i--;
                            }
                        for (int i = n; i < currentGroups.size(); i++)
                            if (!currentGroups.get(i).trim().isEmpty()) {
                                groups.add(new Group(currentGroups.get(i).trim(), 0, true));
                                for (Word w : words)
                                    w.groupNumber.add(-1);
                            }
                        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                            return;
                        outputWords(findFile(filesDir, wordFile));
                        setActivity(R.layout.word_list);
                    }
                });
                currentGroups.clear();
                for (Group group : groups)
                    currentGroups.add(group.name);
                ListView listVocGroups = (ListView) dialog.findViewById(R.id.list_voc_groups);
                listVocGroups.setAdapter(new ListVocGroupsAdapter(currentGroups, focus, this));
                listVocGroups.postInvalidate();
                currentLayout = R.layout.voc_groups;
                break;
            case R.id.button_wordlist:
                setActivity(R.layout.word_list);
                break;
            case R.id.button_test:
//                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
//                    return;
//                inputVoc(findFile(filesDir, vocFile));
//                setContentView(R.layout.word_list);
//                buildList();
//                currentLayout = R.layout.word_list;
                break;
            case R.id.button_new:
                createNewWord();
                break;
//            case R.id.button_delete:
//                String search = words.get(position).foreignWord;
//                for (int i = 0; i < words.get(position).groupNumber.size(); i++) {
//                    int n = words.get(position).groupNumber.get(i);
//                    int m;
//                    if (n > -1) {
//                        groups.get(i).amount--;
//                        for (Word w : words) {
//                            m = w.groupNumber.get(i);
//                            if (m > n)
//                                w.groupNumber.set(i, m - 1);
//                        }
//                    }
//                }
//                int n = words.get(position).time;
//                for (Word w : words) {
//                    if (w.time > n)
//                        w.time--;
//                }
//                words.remove(position);
//                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
//                    return;
//                outputWords(findFile(filesDir, wordFile));
//                setActivity(R.layout.word_list);
//                search(search);
//                break;
        }
    }

//    private void inputVoc(File file) {
//        words.clear();
//        groups.clear();
//        try {
//            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "CP1251"));
//            String line = br.readLine();
//            int num = Integer.parseInt(line);
//            for (int i = 0; i < num; i++) {
//                line = br.readLine();
//                groups.add(new Group(line.substring(0, line.indexOf('|')), Integer.valueOf(line.substring(line.indexOf('|') + 1))));
//            }
//            while ((line = br.readLine()) != null) {
//                String fW = line.substring(0, line.indexOf('|'));
//                line = line.substring(line.indexOf('|') + 1);
//                ArrayList<Integer> grN = new ArrayList<>();
//                for (int i = 0; i < num; i++) {
//                    grN.add(Integer.valueOf(line.substring(0, line.indexOf('|'))));
//                    line = line.substring(line.indexOf('|') + 1);
//                }
//                PartOfSpeech pOS = PartOfSpeech.UNKNOWN;;
//                switch (line.substring(0, line.indexOf('|'))) {
//                    case "N":
//                        pOS = PartOfSpeech.N;
//                        break;
//                    case "V":
//                        pOS = PartOfSpeech.V;
//                        break;
//                    case "ADV":
//                        pOS = PartOfSpeech.ADV;
//                        break;
//                    case "ADJ":
//                        pOS = PartOfSpeech.ADJ;
//                        break;
//                    case "ELSE":
//                        pOS = PartOfSpeech.ELSE;
//                        break;
//                }
//                line = line.substring(line.indexOf('|') + 1);
//                ArrayList<String> tr = new ArrayList<>();
//                while (!line.isEmpty()) {
//                    tr.add(line.substring(0, line.indexOf('|')));
//                    line = line.substring(line.indexOf('|') + 1);
//                }
//                words.add(new Word(fW, grN, pOS, tr));
//            }
//            br.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private void inputWords(File file) {
        words.clear();
        groups.clear();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            sort = Sort.values()[Integer.parseInt(br.readLine())];
            String line = br.readLine();
            int num = Integer.parseInt(line);
            for (int i = 0; i < num; i++) {
                line = br.readLine();
                String name = line.substring(0, line.indexOf('|'));
                line = line.substring(line.indexOf('|') + 1);
                int amount = Integer.valueOf(line.substring(0, line.indexOf('|')));
                line = line.substring(line.indexOf('|') + 1);
                boolean last = Boolean.valueOf(line);
                groups.add(new Group(name, amount, last));
            }
            while ((line = br.readLine()) != null) {
                String fW = line.substring(0, line.indexOf('|'));
                line = line.substring(line.indexOf('|') + 1);
                int time = Integer.valueOf(line.substring(0, line.indexOf('|')));
                line = line.substring(line.indexOf('|') + 1);
                ArrayList<Integer> grN = new ArrayList<>();
                for (int i = 0; i < num; i++) {
                    grN.add(Integer.valueOf(line.substring(0, line.indexOf('|'))));
                    line = line.substring(line.indexOf('|') + 1);
                }
                PartOfSpeech pOS = PartOfSpeech.values()[Integer.parseInt(line.substring(0, line.indexOf('|')))];
//                try {
//                    pOS = PartOfSpeech.valueOf(line.substring(0, line.indexOf('|')));
//                } catch (IllegalArgumentException e) {
//                    pOS = PartOfSpeech.UNKNOWN;
//                }
//                pOS = PartOfSpeech.UNKNOWN;
//                switch (line.substring(0, line.indexOf('|'))) {
//                    case "N":
//                        pOS = PartOfSpeech.N;
//                        break;
//                    case "V":
//                        pOS = PartOfSpeech.V;
//                        break;
//                    case "ADV":
//                        pOS = PartOfSpeech.ADV;
//                        break;
//                    case "ADJ":
//                        pOS = PartOfSpeech.ADJ;
//                        break;
//                    case "PR":
//                        pOS = PartOfSpeech.PR;
//                        break;
//                }
                line = line.substring(line.indexOf('|') + 1);
                ArrayList<String> tr = new ArrayList<>();
                while (!line.isEmpty()) {
                    tr.add(line.substring(0, line.indexOf('|')));
                    line = line.substring(line.indexOf('|') + 1);
                }
                words.add(new Word(fW, time, grN, pOS, tr));
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void outputWords(File file) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(String.valueOf(sort.ordinal()));
            bw.newLine();
            bw.write(String.valueOf(groups.size()));
            for (Group group : groups) {
                bw.newLine();
                bw.write(group.name + '|' + group.amount + '|' + group.last);
            }
            for (Word word : words) {
                bw.newLine();
                bw.write(word.foreignWord + '|');
                bw.write(word.time + "|");
                for (int num : word.groupNumber)
                    bw.write(num + "|");
                bw.write(word.partOfSpeech.ordinal() + "|");
//                switch (word.partOfSpeech) {
//                    case UNKNOWN:
//                        bw.write("?|");
//                        break;
//                    case N:
//                        bw.write("N|");
//                        break;
//                    case V:
//                        bw.write("V|");
//                        break;
//                    case ADV:
//                        bw.write("ADV|");
//                        break;
//                    case ADJ:
//                        bw.write("ADJ|");
//                        break;
//                    case PR:
//                        bw.write("PR|");
//                        break;
//                }
                for (String tr : word.translations)
                    bw.write(tr + '|');
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File findFile(String dir, String name) {
        File path = new File((Environment.getExternalStorageDirectory()).getAbsolutePath() + dir);
        path.mkdirs();
        return new File(path, name);
    }

    void listenListWordsClick(int position) {
        this.position = position;
        currentWord = words.get(position);
        focus.unit = Unit.DEFAULT;
        setActivity(R.layout.word_info);
    }

    private void buildInfo() {
        ListView listTranslations = (ListView) findViewById(R.id.list_translations);
        listTranslations.setAdapter(new ListTranslationsAdapter(currentWord.translations, focus, this));
        listTranslations.postInvalidate();
        ListView listGroups = (ListView) findViewById(R.id.list_groups);
        listGroups.setAdapter(new ListGroupsAdapter(groups, currentWord.groupNumber, this));
        listGroups.postInvalidate();
    }

    private void buildList(boolean b) {
        if (b)
            switch (sort) {
                case ALPHABET:
                    Collections.sort(words, new Comparator<Word>() {
                        public int compare(Word w1, Word w2) {
                            return w1.foreignWord.compareTo(w2.foreignWord);
                        }
                    });
                    break;
                case TIME:
                    Collections.sort(words, new Comparator<Word>() {
                        public int compare(Word w1, Word w2) {
                            return w2.time - w1.time;
                        }
                    });
                    break;
            }
        ListView listWords = (ListView) findViewById(R.id.list_words);
        listWords.setAdapter(new ListWordsAdapter(words, this));
        listWords.postInvalidate();
    }

    private void search(String search) {
        if (sort == Sort.ALPHABET) {
            int searchPosition = 0;
            while (searchPosition < words.size() && words.get(searchPosition).foreignWord.compareTo(search) < 0)
                searchPosition++;
            if (searchPosition > 0)
                searchPosition--;
            ((ListView) findViewById(R.id.list_words)).setSelection(searchPosition);
        }
    }

    private void verifyCurrentWords() {
        currentWord.foreignWord = currentWord.foreignWord.trim();
        int i = 0;
        while (i < currentWord.translations.size()) {
            currentWord.translations.set(i, currentWord.translations.get(i).trim());
            if (currentWord.translations.get(i).isEmpty())
                currentWord.translations.remove(i);
            else
                i++;
        }
//
//
//        while (currentWord.foreignWord.length() > 0 && currentWord.foreignWord.charAt(0) == ' ')
//            currentWord.foreignWord = currentWord.foreignWord.substring(1);
//        while (currentWord.foreignWord.length() > 0 && currentWord.foreignWord.charAt(currentWord.foreignWord.length() - 1) == ' ')
//            currentWord.foreignWord = currentWord.foreignWord.substring(0, currentWord.foreignWord.length() - 1);
//        int i = 0;
//        while (i < currentWord.translations.size()) {
//            while (currentWord.translations.get(i).length() > 0 && currentWord.translations.get(i).charAt(0) == ' ')
//                currentWord.translations.set(i, currentWord.translations.get(i).substring(1));
//            while (currentWord.translations.get(i).length() > 0 && currentWord.translations.get(i).charAt(currentWord.translations.get(i).length() - 1) == ' ')
//                currentWord.translations.set(i, currentWord.translations.get(i).substring(0, currentWord.translations.get(i).length() - 1));
//            if (currentWord.translations.get(i).length() == 0)
//                currentWord.translations.remove(i);
//            else
//                i++;
//        }
    }
}