package com.example.culinaryforumapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Random;


public class AddRecipeActivity extends AppCompatActivity {

    private String CurrentUserUid;

    private TextView userName;
    EditText recipeName;
    EditText recipeDescription;
    EditText thisRecipe;
    Spinner spinnerCategory;

    String[] categoryArray;

    FirebaseDatabase databaseRecipe;
    DatabaseReference RecipeDataBase;
    ArrayAdapter<String> spinnerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);


        Bundle arguments = getIntent().getExtras();

        Init();

        if(arguments != null){
            CurrentUserUid = arguments.getString(Constants.USER_UID);
            //Toast.makeText(AddRecipeActivity.this, CurrentUserUid, Toast.LENGTH_SHORT).show();
        } else {
            finish();
        }

        userName.setText("От имени " + Constants.CURRENT_USER_NICK);
    }

    private void Init(){
        recipeName = findViewById(R.id.editTextRecipeName);
        recipeDescription = findViewById(R.id.editTextRecipeDescription);
        thisRecipe = findViewById(R.id.editTextThisRecipe);
        spinnerCategory = findViewById(R.id.categorySpinner);
        userName = findViewById(R.id.textViewUserName);

        databaseRecipe = FirebaseDatabase.getInstance("https://culinaryforumapp-default-rtdb.europe-west1.firebasedatabase.app");
        RecipeDataBase = databaseRecipe.getReference(Constants.RECIPE_KEY);
        categoryArray = getResources().getStringArray(R.array.category_recipe);
        spinnerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, categoryArray);
        spinnerCategory.setAdapter(spinnerAdapter);
    }

    public void OnClickButtonSave(View view) {

        if(recipeName.getText().toString().isEmpty()) {

            Toast.makeText(AddRecipeActivity.this, "Поле 'Название рецепта' не может быть пустым!",
                    Toast.LENGTH_SHORT).show();
        }
        else if(recipeDescription.getText().toString().isEmpty()) {

            Toast.makeText(AddRecipeActivity.this, "Поле 'Описание рецепта' не может быть пустым!",
                    Toast.LENGTH_SHORT).show();
        } else if(thisRecipe.getText().toString().isEmpty()){

            Toast.makeText(AddRecipeActivity.this, "Поле 'Способ приготовления' не может быть пустым!",
                    Toast.LENGTH_SHORT).show();
        } else {

            Calendar rightNow = Calendar.getInstance();
            // offset to add since we're not UTC
            long offset = rightNow.get(Calendar.ZONE_OFFSET) +
                    rightNow.get(Calendar.DST_OFFSET);
            long sinceMidnight = (rightNow.getTimeInMillis() + offset) %
                    (24 * 60 * 60 * 1000);
            Random rnd = new Random();
            Recipe newRecipe = new Recipe(RecipeDataBase.getKey(), CurrentUserUid,
                    recipeName.getText().toString(), recipeDescription.getText().toString(),
                    thisRecipe.getText().toString(), spinnerCategory.getSelectedItem().toString(),
                    CurrentUserUid.hashCode() + "" + sinceMidnight + "" + rnd.nextInt() + "" + rnd.nextInt());

            RecipeDataBase.push().setValue(newRecipe).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        Toast.makeText(AddRecipeActivity.this, "Рецепт успешно добавлен!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    else {
                        Toast.makeText(AddRecipeActivity.this, "Ошибка добавления. Проверьте ваше подключение к сети", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    public void OnClickCancel(View view) {
        finish();
    }

}
