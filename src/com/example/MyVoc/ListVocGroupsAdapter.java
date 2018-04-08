package com.example.MyVoc;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

class ListVocGroupsAdapter extends ArrayAdapter<String> {

    private ArrayList<String> groups;
    private MyActivity myActivity;
    private Focus focus;

    ListVocGroupsAdapter(ArrayList<String> groups, Focus focus, MyActivity myActivity) {
        super(myActivity, R.layout.list_voc_groups, groups);
        this.groups = groups;
        this.focus = focus;
        add("");
        this.myActivity = myActivity;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = ((LayoutInflater) myActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_voc_groups, parent, false);

        final EditText editGroup = (EditText) rowView.findViewById(R.id.edit_group);
        editGroup.setText(groups.get(position));

//        if (position == 0)
//            editTranslation.setBackgroundResource(R.drawable.orange_listrect);

        if (focus.unit == Unit.GROUPS && focus.position == position)
            editGroup.requestFocus(focus.letter);

        editGroup.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (groups.get(groups.size() - 1).equals("")) && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_NEXT)) {
                    focus = new Focus(Unit.GROUPS, groups.size() - 1, 0);
                    notifyDataSetInvalidated();
                }
                return false;
            }
        });

        editGroup.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                focus = new Focus(Unit.GROUPS, position, start);
                groups.set(position, String.valueOf(editGroup.getText()));
                if (!String.valueOf(editGroup.getText()).equals("") && position == groups.size() - 1)
                    add("");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

//        if (position != groups.size() - 1)
//            editGroup.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    String s = groups.get(0);
//                    groups.set(0, groups.get(position));
//                    groups.set(position, s);
//                    notifyDataSetInvalidated();
//                    return false;
//                }
//            });
        return rowView;
    }
}
