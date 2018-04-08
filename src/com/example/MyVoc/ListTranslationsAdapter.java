package com.example.MyVoc;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

class ListTranslationsAdapter extends ArrayAdapter<String> {

    private ArrayList<String> translations;
    private MyActivity myActivity;
    private Focus focus;

    ListTranslationsAdapter(ArrayList<String> translations, Focus focus, MyActivity myActivity) {
        super(myActivity, R.layout.list_words, translations);
        this.translations = translations;
        this.focus = focus;
        add("");
        this.myActivity = myActivity;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = ((LayoutInflater) myActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_translations, parent, false);

        final EditText editTranslation = (EditText) rowView.findViewById(R.id.edit_translation);
        editTranslation.setText(translations.get(position));

//        if (position == 0)
//            editTranslation.setBackgroundResource(R.drawable.orange_listrect);

        if (focus.unit == Unit.TRANSLATIONS && focus.position == position) {
            //((InputMethodManager) myActivity.getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(0, 0);
            editTranslation.requestFocus(focus.letter);
        }

        editTranslation.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (translations.get(translations.size() - 1).equals("")) && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_NEXT)) {
                    focus = new Focus(Unit.TRANSLATIONS, translations.size() - 1, 0);
                    notifyDataSetInvalidated();
                }
                return false;
            }
        });

        editTranslation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                focus = new Focus(Unit.TRANSLATIONS, position, start);
                translations.set(position, String.valueOf(editTranslation.getText()));
                if (!String.valueOf(editTranslation.getText()).equals("") && position == translations.size() - 1)
                    add("");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        if (position != translations.size() - 1)
            editTranslation.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    String s = translations.get(0);
                    translations.set(0, translations.get(position));
                    translations.set(position, s);
                    notifyDataSetInvalidated();
                    return false;
                }
            });
        return rowView;
    }
}