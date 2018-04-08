package com.example.MyVoc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;

class ListGroupsAdapter extends ArrayAdapter<Group> {

    private ArrayList<Group> groups;
    private ArrayList<Integer> groupNumber;
    private MyActivity myActivity;

    ListGroupsAdapter(ArrayList<Group> groups, ArrayList<Integer> groupNumber, MyActivity myActivity) {
        super(myActivity, R.layout.list_groups, groups);
        this.groups = groups;
        this.groupNumber = groupNumber;
        this.myActivity = myActivity;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = ((LayoutInflater) myActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_groups, parent, false);

        ((CheckBox) rowView.findViewById(R.id.check_group)).setText(groups.get(position).name);
        if (groupNumber.get(position) > -1)
            ((CheckBox) rowView.findViewById(R.id.check_group)).setChecked(true);
        ((CheckBox) rowView.findViewById(R.id.check_group)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    groupNumber.set(position, groups.get(position).amount);
                    groups.get(position).amount++;
                } else {
                    groupNumber.set(position, -1);
                    groups.get(position).amount--;
                }
            }
        });
        return rowView;
    }
}