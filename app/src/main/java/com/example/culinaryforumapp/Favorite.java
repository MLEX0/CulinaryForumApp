package com.example.culinaryforumapp;

public class Favorite {
    public String idFavorite, userUID, recipeID, generateFavoritesId;

    public Favorite() {

    }

    public Favorite(String idFavorite, String userUID, String recipeID, String generateFavoritesId) {
        this.idFavorite = idFavorite;
        this.userUID = userUID;
        this.recipeID = recipeID;
        this.generateFavoritesId = generateFavoritesId;
    }
}
