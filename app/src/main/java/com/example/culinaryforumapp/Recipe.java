package com.example.culinaryforumapp;

public class Recipe {
        public String idRecipe, creatorUID, generateIdRecipe, nameRecipe, descriptionRecipe, recipePreparationMethod, categoryName;

        public Recipe() {

        }

        public Recipe(String idRecipe, String creatorUID, String nameRecipe, String descriptionRecipe, String recipePreparationMethod, String categoryName, String generateIdRecipe) {

                this.idRecipe = idRecipe;
                this.creatorUID = creatorUID;
                this.nameRecipe = nameRecipe;
                this.descriptionRecipe = descriptionRecipe;
                this.recipePreparationMethod = recipePreparationMethod;
                this.categoryName = categoryName;
                this.generateIdRecipe = generateIdRecipe;
        }
}
