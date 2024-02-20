package it.uniba.dib.sms2324FF2.therapist.homepage;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import it.uniba.dib.sms2324FF2.R;

public class CustomArrayAdapter extends ArrayAdapter<String> {

    private final Typeface typeface;

    public CustomArrayAdapter(@NonNull Context context, int resource, @NonNull String[] objects) {
        super(context, resource, objects);
        typeface = ResourcesCompat.getFont(context, R.font.bubblegum_sans);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.spinner_dropdown_item, parent, false);
        }

        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(getItem(position));
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setTypeface(typeface);

        return convertView;
    }
}

