package com.example.culinaryforumapp;

public class Comment {
    public String idComment, generatedCommentId, generatedRecipeId ,sendingTime, userUID, userNick, commentText;

    public Comment() {
    }

    public Comment(String idComment, String generatedCommentId, String generatedRecipeId, String sendingTime, String userUID, String userNick, String commentText) {
        this.idComment = idComment;
        this.generatedCommentId = generatedCommentId;
        this.generatedRecipeId = generatedRecipeId;
        this.sendingTime = sendingTime;
        this.userUID = userUID;
        this.userNick = userNick;
        this.commentText = commentText;
    }
}
