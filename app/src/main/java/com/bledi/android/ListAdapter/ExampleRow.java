package com.bledi.android.ListAdapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bledi.android.twittertest.R;
import com.squareup.picasso.Picasso;

public class ExampleRow {

    // This is a reference to the layout we defined above
    public static final int LAYOUT = R.layout.list_item;

    private final Context context;
    private final TextView textView;
    private final ImageView imageView;
 
    public ExampleRow(Context context, View convertView) {
        this.context = context;
        this.imageView = (ImageView) convertView.findViewById(R.id.imageView);
        this.textView = (TextView) convertView.findViewById(R.id.textView);
    }

    public void bind(ExampleViewModel exampleViewModel) {
        this.textView.setText(exampleViewModel.getText());
        Picasso.with(this.context).load(exampleViewModel.getImageUrl()).into(this.imageView);
    }
}
