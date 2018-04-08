package com.example.MyVoc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;

class ListWordsAdapter extends ArrayAdapter<Word> {

    private ArrayList<Word> words;
    private MyActivity myActivity;

    ListWordsAdapter(ArrayList<Word> words, MyActivity myActivity) {
        super(myActivity, R.layout.list_words, words);
        this.words = words;
        this.myActivity = myActivity;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = ((LayoutInflater) myActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_words, parent, false);

        if (myActivity.column == Column.BOTH || myActivity.column == Column.LEFT)
            ((TextView) rowView.findViewById(R.id.text_foreign_word)).setText(words.get(position).foreignWord);
        if (myActivity.column == Column.BOTH || myActivity.column == Column.RIGHT)
            ((TextView) rowView.findViewById(R.id.text_native_word)).setText(words.get(position).translations.size() != 0 ? words.get(position).translations.get(0) : "");

        switch (words.get(position).partOfSpeech) {
            case UNKNOWN:
                ((TextView) rowView.findViewById(R.id.text_part_of_speech)).setText("?");
                break;
            case N:
                ((TextView) rowView.findViewById(R.id.text_part_of_speech)).setText("n");
                break;
            case V:
                ((TextView) rowView.findViewById(R.id.text_part_of_speech)).setText("v");
                break;
            case ADJ:
                ((TextView) rowView.findViewById(R.id.text_part_of_speech)).setText("adj");
                break;
            case ADV:
                ((TextView) rowView.findViewById(R.id.text_part_of_speech)).setText("adv");
                break;
            case PR:
                ((TextView) rowView.findViewById(R.id.text_part_of_speech)).setText("pr");
                break;
        }

        (rowView.findViewById(R.id.list_words)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myActivity.listenListWordsClick(position);
            }
        });

        (rowView.findViewById(R.id.text_foreign_word)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myActivity.listenListWordsClick(position);
            }
        });

        (rowView.findViewById(R.id.text_native_word)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myActivity.listenListWordsClick(position);
            }
        });

        (rowView.findViewById(R.id.text_foreign_word)).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                switch (myActivity.column) {
                    case BOTH:
                        myActivity.column = Column.RIGHT;
                        break;
                    case LEFT:
                        myActivity.column = Column.RIGHT;
                        break;
                    case RIGHT:
                        myActivity.column = Column.BOTH;
                        break;
                }
                notifyDataSetInvalidated();
                return true;
            }
        });

        (rowView.findViewById(R.id.text_native_word)).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                switch (myActivity.column) {
                    case BOTH:
                        myActivity.column = Column.LEFT;
                        break;
                    case RIGHT:
                        myActivity.column = Column.LEFT;
                        break;
                    case LEFT:
                        myActivity.column = Column.BOTH;
                        break;
                }
                notifyDataSetInvalidated();
                return true;
            }
        });

        return rowView;
    }
}