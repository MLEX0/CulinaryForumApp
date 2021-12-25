package com.example.culinaryforumapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

public class MainFrameActivity extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private FirebaseUser CurrentUser;

    private FirebaseDatabase databaseRecipe;
    private DatabaseReference RecipeDataBase;
    private DatabaseReference UserDataBase;
    private DatabaseReference FavoriteDataBase;


    private BottomNavigationView BottomNav;
    private FloatingActionButton addRecipeFloatingButton;

    private ListView listViewRecipeHome;
    private ListView listViewRecipeProfile;
    private ListView listViewRecipeFavorite;

    private ArrayList<Recipe> listRecipeHome;
    private ArrayList<Recipe> listRecipeProfile;
    private ArrayList<Recipe> listRecipeFavorite;
    private ArrayList<Favorite> listFavoriteUser;
    private RecipeListAdapter recipeAdapterHome;
    private RecipeListAdapter recipeAdapterProfile;
    private RecipeListAdapter recipeAdapterFavorite;


    private ArrayList<User> userArrayList;
    private boolean UserNickIsNull;


    private ConstraintLayout homeLayout;
    private ConstraintLayout favoriteLayout;
    private ConstraintLayout profileLayout;
    private LinearLayout homeSearchMenu;
    private LinearLayout favoriteSearchMenu;
    private ConstraintLayout profileMenu;

    private TextView profilePageNickName;
    private TextView profilePageEmail;

    private EditText searchInHomeText;
    private EditText searchInFavoriteText;

    private String selectedCategoryForFavorite;
    private String selectedCategoryForHome;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_frame);

        Init();

        SetAuthStateListener();

        SetBottomNavigationSelectedListener();

        CurrentUser = mAuth.getCurrentUser();

        //Проверяем авторизирован ли пользователь
        if(CurrentUser != null) {
            Constants.CURRENT_USER_NICK = CurrentUser.getEmail();
            Constants.openRecipe = null;
            //Toast.makeText(MainFrameActivity.this, CurrentUser.getUid(), Toast.LENGTH_SHORT).show();
        } else {
            finish();
        }

        getFavoritesFromDB();

        getDataFromDB();

        setOnClickItem();

        GetUserDataFromDB();

        AddSearchToHome();

        AddSearchToFavorites();

        //Устанавливаем данные пользователя на странице профиля
        profilePageNickName.setText(Constants.CURRENT_USER_NICK);
        profilePageEmail.setText(CurrentUser.getEmail());
    }

    private void SetBottomNavigationSelectedListener() {

        BottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //Постраничная навигация при нажатии на кнопку меню
                switch(item.getItemId()) {
                    case R.id.nav_home:
                        showHomeLayout();
                        break;
                    case R.id.nav_favorites:
                        showFavoriteLayout();
                        recipeAdapterFavorite.notifyDataSetChanged();
                        break;
                    case R.id.nav_profile:
                        showProfileLayout();
                        break;

                }
                return true;
            }
        });
    }

    //Слушатель авторизации пользователя
    private void SetAuthStateListener()
    {
        //При выходе из аккаунта пользователь попадает на страницу авторизации
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user == null)
                {
                    Intent UnAuth = new Intent(MainFrameActivity.this, RegistrationAuthorizationActivity.class);
                    startActivity(UnAuth);
                    finish();
                }
            }
        });
    }

    //Метод инициализации приложения
    private void Init() {


        mAuth = FirebaseAuth.getInstance();

        BottomNav = findViewById(R.id.bottom_navigation);
        addRecipeFloatingButton = findViewById(R.id.floatingActionButtonAddRecipe);

        //Находим лайауты страниц, чтобы потом скрывать их при переходе между 'окнами'
        homeLayout = findViewById(R.id.homeConstraintLayout);
        favoriteLayout = findViewById(R.id.favoritesConstraintLayout);
        profileLayout = findViewById(R.id.profileConstraintLayout);

        //Находим лайауты верхнего меню на страницю, чтобы потом скрывать при переходе между 'окнами'
        homeSearchMenu = findViewById(R.id.LinearLayoutHomeSeacrhMenu);
        favoriteSearchMenu = findViewById(R.id.LinearLayoutFavoriteSeacrhMenu);
        profileMenu = findViewById(R.id.ConstraintLayoutProfileUpMenu);

        //Получаем референсы от всех хранилищ
        databaseRecipe = FirebaseDatabase.getInstance("https://culinaryforumapp-default-rtdb.europe-west1.firebasedatabase.app");
        RecipeDataBase = databaseRecipe.getReference(Constants.RECIPE_KEY);
        UserDataBase = databaseRecipe.getReference(Constants.USER_KEY);
        FavoriteDataBase = databaseRecipe.getReference(Constants.FAVORITE_KEY);

        //Инициализируем адаптеры для ListView
        listViewRecipeHome = findViewById(R.id.listViewRecipeHome);
        listRecipeHome = new ArrayList<Recipe>();
        recipeAdapterHome = new RecipeListAdapter(MainFrameActivity.this, listRecipeHome);
        listViewRecipeHome.setAdapter(recipeAdapterHome);
        listViewRecipeHome.setClickable(true);

        listViewRecipeProfile = findViewById(R.id.ListViewProfileRecipe);
        listRecipeProfile = new ArrayList<Recipe>();
        recipeAdapterProfile = new RecipeListAdapter(MainFrameActivity.this, listRecipeProfile);
        listViewRecipeProfile.setAdapter(recipeAdapterProfile);
        listViewRecipeProfile.setClickable(true);

        listViewRecipeFavorite = findViewById(R.id.listViewRecipeFavorite);
        listRecipeFavorite = new ArrayList<Recipe>();
        recipeAdapterFavorite = new RecipeListAdapter(MainFrameActivity.this, listRecipeFavorite);
        listViewRecipeFavorite.setAdapter(recipeAdapterFavorite);
        listViewRecipeFavorite.setClickable(true);

        listFavoriteUser = new ArrayList<Favorite>();

        //Инициализируем лист для поиска ника пользователя
        userArrayList = new ArrayList<User>();
        UserNickIsNull = true;

        //Находим текстовые поля, в которых должны отображаться данные пользователя
        profilePageNickName = findViewById(R.id.textViewProfilePageNickName);
        profilePageEmail = findViewById(R.id.textViewProfilePageEmail);

        //Находим текстовые поля для поиска
        searchInHomeText = findViewById(R.id.editTextSearchHome);
        searchInFavoriteText = findViewById(R.id.editTextSearchFavorite);
        //Ставим переменные, по которым идёт проверка на категорию товара, на положение без категории
        selectedCategoryForFavorite = "Нет";
        selectedCategoryForHome = "Нет";

    }

    public void getFavoritesFromDB() {
        //Устанавливаем слушатель для изменения избранного
        ValueEventListener vListenerFavorites = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(listFavoriteUser.size() > 0){listFavoriteUser.clear();}
                for(DataSnapshot ds : snapshot.getChildren()){
                    Favorite favorite = ds.getValue(Favorite.class);
                    //Заполняем массив только избранным текущего пользователя
                    if(favorite.userUID.equals(mAuth.getCurrentUser().getUid())) {
                        assert favorite != null;
                        listFavoriteUser.add(favorite);
                    }
                }
                getDataFromDB();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        FavoriteDataBase.addValueEventListener(vListenerFavorites);
    }

    //Метод для отслеживания изменений данных с рецептами
    public void getDataFromDB(){

        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    //Очищаем все массивы
                    if(listRecipeHome.size() > 0){listRecipeHome.clear();}
                    if(listRecipeProfile.size() > 0){listRecipeProfile.clear();}
                    if(listRecipeFavorite.size() > 0){listRecipeFavorite.clear();}
                    //Перебираем данные
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Recipe recipe = ds.getValue(Recipe.class);

                    //Заполняем массив всех рецептов с сортировкой
                    if(selectedCategoryForHome.equals("Нет")) {
                        assert recipe != null;
                        listRecipeHome.add(recipe);
                    }
                    else if(recipe.categoryName.equals(selectedCategoryForHome)) {
                            assert recipe != null;
                            listRecipeHome.add(recipe);
                    }

                    //Заполняем лист с рецептами пользователя
                    if(recipe.creatorUID.hashCode() == CurrentUser.getUid().hashCode())
                    {
                        assert recipe != null;
                        listRecipeProfile.add(recipe);
                    }

                    // перебираем массив с избраннм пользователя если массив с избранным не пустой
                    if(!listFavoriteUser.isEmpty()){
                        for(Favorite favorite : listFavoriteUser) {
                            if(favorite.recipeID.equals(recipe.generateIdRecipe)){
                                //Если есть сортировка, то проверяем по категориям
                                if(selectedCategoryForFavorite.equals("Нет")){
                                    assert recipe != null;
                                    listRecipeFavorite.add(recipe);
                                } else if(recipe.categoryName.equals(selectedCategoryForFavorite)) {
                                    assert recipe != null;
                                    listRecipeFavorite.add(recipe);
                                }

                            }
                        }
                    }
                }
                recipeAdapterHome.notifyDataSetChanged();
                recipeAdapterProfile.notifyDataSetChanged();
                recipeAdapterFavorite.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        //Удаляем и добавляем слушатель, чтобы менять значения при изменении выбранной категории
        RecipeDataBase.removeEventListener(vListener);
        RecipeDataBase.addValueEventListener(vListener);
        recipeAdapterFavorite.notifyDataSetChanged();
    }


    public void GetUserDataFromDB(){
        ValueEventListener userValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Пытаемся найти имя пользователя в БД
                if(UserNickIsNull == true)
                {
                    if(userArrayList.size() > 0)userArrayList.clear();
                    for(DataSnapshot ds2 : dataSnapshot.getChildren()){
                        User user = ds2.getValue(User.class);
                        assert user != null;
                        if(user.Uid.equals(CurrentUser.getUid().toString())){
                            Constants.CURRENT_USER_NICK = user.NickName;
                            Toast.makeText(MainFrameActivity.this, "Добро пожаловать, " +
                                    Constants.CURRENT_USER_NICK, Toast.LENGTH_SHORT).show();
                            profilePageNickName.setText(Constants.CURRENT_USER_NICK);
                        }
                    }
                    UserNickIsNull = false;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        UserDataBase.addValueEventListener(userValueEventListener);
    }

    // Добавляем переход к рецепту при нажатии на него на всех 'окнах'
    private void setOnClickItem(){
        listViewRecipeHome.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Recipe recipe = listRecipeHome.get(position);
                Intent openRecipe = new Intent(MainFrameActivity.this, ViewRecipeActivity.class);
                Constants.openRecipe = recipe;
                startActivity(openRecipe);
                getFavoritesFromDB();
                recipeAdapterFavorite.notifyDataSetChanged();
            }
        });

        listViewRecipeProfile.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Recipe recipe = listRecipeProfile.get(position);
                Intent openRecipe = new Intent(MainFrameActivity.this, ViewRecipeActivity.class);
                Constants.openRecipe = recipe;
                startActivity(openRecipe);
                getFavoritesFromDB();
                recipeAdapterFavorite.notifyDataSetChanged();
            }
        });

        listViewRecipeFavorite.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Recipe recipe = listRecipeFavorite.get(position);
                Intent openRecipe = new Intent(MainFrameActivity.this, ViewRecipeActivity.class);
                Constants.openRecipe = recipe;
                startActivity(openRecipe);
                getFavoritesFromDB();
                recipeAdapterFavorite.notifyDataSetChanged();
            }
        });
    }

    //Методы для переключения меджду лайаутами через меню
    public void showHomeLayout(){
        profileMenu.setVisibility(View.INVISIBLE);
        addRecipeFloatingButton.setVisibility(View.VISIBLE);
        homeLayout.setVisibility(View.VISIBLE);
        homeSearchMenu.setVisibility(View.VISIBLE);
        favoriteLayout.setVisibility(View.INVISIBLE);
        favoriteSearchMenu.setVisibility(View.INVISIBLE);
        profileLayout.setVisibility(View.INVISIBLE);
    }

    public void showFavoriteLayout(){
        profileMenu.setVisibility(View.INVISIBLE);
        addRecipeFloatingButton.setVisibility(View.INVISIBLE);
        homeLayout.setVisibility(View.INVISIBLE);
        homeSearchMenu.setVisibility(View.INVISIBLE);
        favoriteLayout.setVisibility(View.VISIBLE);
        favoriteSearchMenu.setVisibility(View.VISIBLE);
        profileLayout.setVisibility(View.INVISIBLE);
    }

    public void showProfileLayout(){
        profileMenu.setVisibility(View.VISIBLE);
        addRecipeFloatingButton.setVisibility(View.INVISIBLE);
        homeLayout.setVisibility(View.INVISIBLE);
        homeSearchMenu.setVisibility(View.INVISIBLE);
        favoriteLayout.setVisibility(View.INVISIBLE);
        favoriteSearchMenu.setVisibility(View.INVISIBLE);
        profileLayout.setVisibility(View.VISIBLE);
    }


    // Кнопка выхода из приложения
    public void onClickLogout(View view) {
        String title = "Вы уверены?";
        String message = "Выйти из аккаунта?";
        String button1String = "Да";
        String button2String = "Нет";

        AlertDialog.Builder builder = new AlertDialog.Builder(MainFrameActivity.this);
        builder.setTitle(title);  // заголовок
        builder.setMessage(message); // сообщение
        builder.setPositiveButton(button1String, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mAuth.signOut();
                dialog.cancel();
            }
        });
        builder.setNegativeButton(button2String, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.setCancelable(true);
        builder.show();
    }

    //Выбор категории по нажатию на кнопку для вкладки 'Дом'
    public void OnClickSortHome(View view){
        AlertDialog.Builder b = new AlertDialog.Builder(MainFrameActivity.this);
        b.setTitle("Выберите категрию");
        String[] types = {"Нет", "Первые блюда", "Вторые блюда", "Закуски", "Салаты", "Соусы, кремы",
                            "Напитки", "Десерты", "Выпечка","Безглютеновая выпечка",
                            "Торты", "Заготовки на зиму", "Блюда в мультиварке",
                            "Блюда в хлебопечке", "Хлеб", "Тесто", "Блины и оладьи", "Постные блюда", "Полезные мелочи"};
        b.setItems(types, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                selectedCategoryForHome = types[which];
                getDataFromDB();
            }
        });

        b.show();
    }

    //Выбор категории по нажатию на кнопку для вкладки 'Избранное'
    public void OnClickSortFavorites(View view){
        AlertDialog.Builder b = new AlertDialog.Builder(MainFrameActivity.this);
        b.setTitle("Выберите категрию");
        String[] types = {"Нет", "Первые блюда", "Вторые блюда", "Закуски", "Салаты", "Соусы, кремы",
                "Напитки", "Десерты", "Выпечка","Безглютеновая выпечка",
                "Торты", "Заготовки на зиму", "Блюда в мультиварке",
                "Блюда в хлебопечке", "Хлеб", "Тесто", "Блины и оладьи", "Постные блюда", "Полезные мелочи"};
        b.setItems(types, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                selectedCategoryForFavorite = types[which];
                getDataFromDB();
            }
        });

        b.show();
    }

    //Метод поиска во всех рецептах
    private void AddSearchToHome() {
        searchInHomeText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().equals("")) {
                    getDataFromDB();
                }
                else{
                    for(int item = 0; item < listRecipeHome.size(); item++) {
                        if(!(listRecipeHome.get(item).nameRecipe.toLowerCase(Locale.ROOT).contains(charSequence.toString().toLowerCase(Locale.ROOT)))){
                            listRecipeHome.remove(listRecipeHome.get(item));
                        }
                    }
                    recipeAdapterHome.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    //Метод поиска в избранном
    private void AddSearchToFavorites() {
        searchInFavoriteText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().equals("")) {
                    getDataFromDB();
                }
                else{
                    for(int item = 0; item < listRecipeFavorite.size(); item++) {
                        if(!(listRecipeFavorite.get(item).nameRecipe.toLowerCase(Locale.ROOT).contains(charSequence.toString().toLowerCase(Locale.ROOT)))){
                            listRecipeFavorite.remove(listRecipeFavorite.get(item));
                        }
                    }
                    recipeAdapterFavorite.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    //Переход на страницу добавления рецепта при нажатии на кнопку
    public void AddRecipe(View view){
        Intent addRecipe = new Intent(MainFrameActivity.this, AddRecipeActivity.class);
        addRecipe.putExtra(Constants.USER_UID, CurrentUser.getUid());
        startActivity(addRecipe);
    }
}
