package com.example.culinaryforumapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RecipeListAdapter extends ArrayAdapter<Recipe> {

    public RecipeListAdapter(@NonNull Context context, ArrayList<Recipe> recipeArrayList) {
        super(context, R.layout.item_list_recipie, (List<Recipe>) recipeArrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Recipe recipe = getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_list_recipie, parent, false);
        }

        TextView recipeName = convertView.findViewById(R.id.recipeName);
        TextView recipeDescription = convertView.findViewById(R.id.recipeDescription);
        TextView recipeCategory = convertView.findViewById(R.id.textViewCategoryRecipe);


        recipeName.setText(recipe.nameRecipe);
        recipeDescription.setText(recipe.descriptionRecipe);
        recipeCategory.setText(recipe.categoryName);

        return convertView;
    }
}




