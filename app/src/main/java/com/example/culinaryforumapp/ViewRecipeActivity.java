package com.example.culinaryforumapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class ViewRecipeActivity extends AppCompatActivity {

    private FirebaseDatabase databaseUser;
    private DatabaseReference UserDataBase;
    private DatabaseReference FavoritesDatabase;
    private DatabaseReference CommentDataBase;

    private TextView viewRecipeName;
    private TextView ViewRecipeDescription;
    private TextView viewRecipeRecipe;
    private TextView viewAuthorName;

    private ImageView imageNotFavorite;
    private ImageView imageIsFavorite;
    private Button buttonDeleteThisRecipe;
    private EditText commentText;

    private ConstraintLayout commentLayout;

    private ListView listViewComments;
    private ArrayList<Comment> listComments;
    private CommentListAdapter commentAdapter;

    private FirebaseAuth mAuth;
    private FirebaseUser CurrentUser;
    private int height;

    private String currentUserNick;

    private String currentFavoriteId;


    private ValueEventListener favoritesValueEventListener;
    private ValueEventListener commentEventListener;
    private ValueEventListener userValueEventListenerRecipe;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_view);

        //Находим все объекты для отображения рецепта
        viewRecipeName = findViewById(R.id.textViewViewActivityRecipeName);
        ViewRecipeDescription = findViewById(R.id.textViewViewActivityRecipeDescription);
        viewRecipeRecipe = findViewById(R.id.textViewViewActivityRecipeRecipe);
        viewAuthorName = findViewById(R.id.textViewViewActivityRecipeAuthorName);
        imageNotFavorite = findViewById(R.id.imageViewAddOnFavorites);
        imageIsFavorite = findViewById(R.id.imageViewButtonDeleteFavorites);
        currentFavoriteId = null;
        buttonDeleteThisRecipe = findViewById(R.id.buttonDeleteThisRecipe);
        commentText = findViewById(R.id.editTextTextPersonName);
        commentLayout = findViewById(R.id.CommentLayout);

        //Инициализируем адаптер комментариев
        listViewComments = findViewById(R.id.ListViewComments);
        listComments = new ArrayList<Comment>();
        commentAdapter = new CommentListAdapter(ViewRecipeActivity.this, listComments);
        listViewComments.setAdapter(commentAdapter);
        listViewComments.setClickable(false);

        //Проверяем есть ли данные с рецептом, который должен быть открыт
        if(Constants.openRecipe == null) {
            finish();
        } else {
            //Инициализируем БД с комментариями и пользователями
            databaseUser = FirebaseDatabase.getInstance("https://culinaryforumapp-default-rtdb.europe-west1.firebasedatabase.app");
            UserDataBase = databaseUser.getReference(Constants.USER_KEY);
            FavoritesDatabase = databaseUser.getReference(Constants.FAVORITE_KEY);
            CommentDataBase = FirebaseDatabase.getInstance("https://culinaryforumapp-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference(Constants.COMMENT_KEY);

            mAuth = FirebaseAuth.getInstance();
            CurrentUser = mAuth.getCurrentUser();

            //Проверяем является ли авторизированнный пользователь владельцем рецепта
            if(CurrentUser.getUid().equals(Constants.openRecipe.creatorUID)) {
                canDelete();
            }

            GetFavoriteDataFromDB();
            GetUserDataFromDB();
            GetCommentFromDB();
        }

        currentUserNick = "Бетатестер";

        viewAuthorName.setText("Автор рецепта: бетатестер");


        viewRecipeName.setText(Constants.openRecipe.nameRecipe);
        ViewRecipeDescription.setText(Constants.openRecipe.descriptionRecipe);
        viewRecipeRecipe.setText(Constants.openRecipe.recipePreparationMethod);


    }

    //Добаляем слушатель для комментариев
    private void GetCommentFromDB(){
        commentEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(listComments.size() > 0){listComments.clear();}
                int i = 1;
                for(DataSnapshot ds : snapshot.getChildren()){

                    Comment comment = ds.getValue(Comment.class);

                    int height = 500;
                    //Заполняем массив комментариев для этого рецепта
                    if(comment.generatedRecipeId.equals(Constants.openRecipe.generateIdRecipe)) {
                        assert comment != null;
                        listComments.add(comment);
                        //Эта чертовщина нужна для изменения размеров ListView комментариев
                        i++;
                        View view = findViewById(R.id.ListViewComments);
                        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                        height = (height * i * 2);
                        layoutParams.height = height;
                        view.setLayoutParams(layoutParams);
                    }
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        CommentDataBase.addValueEventListener(commentEventListener);
    }

    //Метод отправки комментария
    public void OnClickSendComment(View view){
        //Проверяем не пустой ли рецепт
        if(Constants.openRecipe != null) {
            if(!commentText.getText().toString().isEmpty()){

                //Эта штука нужна для получения времени отправки комментария, оно храниться как String
                Calendar rightNow = Calendar.getInstance();
                // offset to add since we're not UTC
                long offset = rightNow.get(Calendar.ZONE_OFFSET) +
                        rightNow.get(Calendar.DST_OFFSET);
                long sinceMidnight = (rightNow.getTimeInMillis() + offset) %
                        (24 * 60 * 60 * 1000);
                String betweenStr;
                if(rightNow.get(Calendar.MINUTE) < 10) {
                    betweenStr = ":0";
                }
                else {
                    betweenStr = ":";
                }

                String DateTime = rightNow.get(Calendar.DATE) + "/" + rightNow.get(Calendar.MONTH) + "/" +
                        rightNow.get(Calendar.YEAR) +" "+ rightNow.get(Calendar.HOUR_OF_DAY) + betweenStr + rightNow.get(Calendar.MINUTE);
                Random rnd = new Random();

                //Формируем новый комментарий для отправки его в БД
                //Также формируем для него уникальный ID для того, чтобы можно было его потом удалить
                Comment newComment = new Comment(CommentDataBase.getKey(),
                        CurrentUser.getUid().hashCode() + "" + Constants.openRecipe.generateIdRecipe.hashCode() + "" + sinceMidnight + "" + rnd.nextInt() + "" + rnd.nextInt() + "",
                        Constants.openRecipe.generateIdRecipe.toString(),
                        DateTime, CurrentUser.getUid().toString(), Constants.CURRENT_USER_NICK.toString(), commentText.getText().toString());

                //Отправляем его в БД и проверяем отправлен ли он
                CommentDataBase.push().setValue(newComment).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(ViewRecipeActivity.this, "Коммент отправлен!", Toast.LENGTH_SHORT).show();
                            commentText.setText("");
                        }
                        else {
                            Toast.makeText(ViewRecipeActivity.this, "Ошибка отправления! Проверьте ваше подключение к сети", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } else {
            finish();
        }
    }

    //Проверяем есть ли этот рецепт в избранном пользователя
    private void GetFavoriteDataFromDB(){
        if(Constants.openRecipe != null){
            favoritesValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot ds : snapshot.getChildren()) {
                        Favorite favorite = ds.getValue(Favorite.class);

                        if(favorite.userUID.equals(CurrentUser.getUid()) &&
                                favorite.recipeID.equals(Constants.openRecipe.generateIdRecipe)){

                            currentFavoriteId = favorite.generateFavoritesId;
                            IsFavorite();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            FavoritesDatabase.addValueEventListener(favoritesValueEventListener);
        } else {
            finish();
        }
    }

    //Получаем никнейм автора рецепта
    private void GetUserDataFromDB() {

        if(Constants.openRecipe != null){
            userValueEventListenerRecipe = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot ds2 : dataSnapshot.getChildren()){
                        User user = ds2.getValue(User.class);

                        if(user.Uid.equals(Constants.openRecipe.creatorUID)) {
                            assert user != null;
                            viewAuthorName.setText("Автор рецепта: " + user.NickName);
                        }
                        if(user.Uid.equals(CurrentUser.getUid())){
                            currentUserNick = user.NickName;
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            UserDataBase.addValueEventListener(userValueEventListenerRecipe);
        } else {
            finish();
        }

    }

    //Изменение состояния кнопки 'Добавить в избранное/Удалить их избранного'
    public void IsFavorite() {
        imageIsFavorite.setVisibility(View.VISIBLE);
        imageNotFavorite.setVisibility(View.INVISIBLE);
    }

    public void NotFavorite() {
        imageIsFavorite.setVisibility(View.INVISIBLE);
        imageNotFavorite.setVisibility(View.VISIBLE);
    }

    //Метод добавления рецепта в избранное
    public void AddInFavorites(View view){
        if(Constants.openRecipe != null) {

            //Получаем дату и время чтобы уникальный ID избранного не повторялся
            Calendar rightNow = Calendar.getInstance();
            // offset to add since we're not UTC
            long offset = rightNow.get(Calendar.ZONE_OFFSET) +
                    rightNow.get(Calendar.DST_OFFSET);
            long sinceMidnight = (rightNow.getTimeInMillis() + offset) %
                    (24 * 60 * 60 * 1000);
            Random rnd = new Random();


            //Формируем запись избранного для отправки её в БД
            //Также формируем для неё уникальный ID для того, чтобы можно было ёё потом удалить
            Favorite favorite = new Favorite(FavoritesDatabase.getKey(), CurrentUser.getUid(),
                    Constants.openRecipe.generateIdRecipe, CurrentUser.getUid().hashCode() + ""
                    + Constants.openRecipe.generateIdRecipe.hashCode() + "" + sinceMidnight + "" + rnd.nextInt() + "" + rnd.nextInt());

            //Отправляем запись в БД
            FavoritesDatabase.push().setValue(favorite).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    IsFavorite();
                    Toast.makeText(ViewRecipeActivity.this, "Рецепт добавлен в избранное", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
          finish();
        }
    }


    public void DeleteFromFavorites(View view) {
        if(Constants.openRecipe != null){

            DatabaseReference ref = FirebaseDatabase.getInstance("https://culinaryforumapp-default-rtdb.europe-west1.firebasedatabase.app").getReference();
            //Формируем запрос для удаления и ищем его по заранее полученному ID
            Query favoritesQuery = ref.child(Constants.FAVORITE_KEY).orderByChild("generateFavoritesId").equalTo(currentFavoriteId);
            favoritesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                        snapshot.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(ViewRecipeActivity.this, "Рецепт удалён из избранного",
                                            Toast.LENGTH_SHORT).show();
                                    NotFavorite();
                                }
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        } else{
          finish();
        }
    }

    //Открываем возможность удалить рецепт
    public void canDelete() {
        buttonDeleteThisRecipe.setVisibility(View.VISIBLE);
    }

    //Метод удаления рецепта
    public void DeleteThisRecipeFromDB(View view){
        if(Constants.openRecipe != null){

            String title = "Вы уверены?";
            String message = "Если вы удалите рецепт, то его уже нельзя будет восстановить, " +
                    "а также все комментарии к нему остануться в вечном забвении";
            String button1String = "Да";
            String button2String = "Нет";

            AlertDialog.Builder builder = new AlertDialog.Builder(ViewRecipeActivity.this);
            builder.setTitle(title);  // заголовок
            builder.setMessage(message); // сообщение
            builder.setPositiveButton(button1String, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    buttonDeleteThisRecipe.setText("Удаление...");
                    buttonDeleteThisRecipe.setActivated(false);
                    //Формируем запрос на удаление рецепта и проверяем его выполнение
                    DatabaseReference ref = FirebaseDatabase.getInstance("https://culinaryforumapp-default-rtdb.europe-west1.firebasedatabase.app").getReference();
                    Query recipeDelQuery = ref.child(Constants.RECIPE_KEY).orderByChild("generateIdRecipe").equalTo(Constants.openRecipe.generateIdRecipe);
                    recipeDelQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                snapshot.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            finish();
                                        }
                                        else{

                                            buttonDeleteThisRecipe.setText("Удалить рецепт");
                                            buttonDeleteThisRecipe.setActivated(true);

                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });
            builder.setNegativeButton(button2String, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            builder.setCancelable(true);
            builder.show();

        } else {
            finish();
        }
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Удаляем слушатели во избежание ошибок
        CommentDataBase.removeEventListener(commentEventListener);
        FavoritesDatabase.removeEventListener(favoritesValueEventListener);
        UserDataBase.removeEventListener(userValueEventListenerRecipe);

        //Обнуляем открытый рецепт
        Constants.openRecipe = null;
    }
}
