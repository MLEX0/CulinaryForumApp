package com.example.culinaryforumapp;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

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
import java.util.List;

public class CommentListAdapter extends ArrayAdapter<Comment> {
    public CommentListAdapter(@NonNull Context context, ArrayList<Comment> recipeArrayList) {
        super(context, R.layout.item_list_comment, (List<Comment>) recipeArrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Comment comment = getItem(position);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser CurrentUser = mAuth.getCurrentUser();

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_list_comment, parent, false);
        }

        TextView commentAuthorName = convertView.findViewById(R.id.commentAuthorName);
        TextView commentText = convertView.findViewById(R.id.commentText);
        TextView dateOfSend = convertView.findViewById(R.id.dateOfSend);
        Button deleteComment = convertView.findViewById(R.id.buttonDeleteComment);

        //Возможность удаления комментария, если авторизированный пользователь его автор
        if(comment.userUID.equals(CurrentUser.getUid())){
            deleteComment.setVisibility(View.VISIBLE);
            deleteComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String title = "Вы уверены?";
                    String message = "Если вы удалите этот комментарий, его нельзя будет восстановить!";
                    String button1String = "Да";
                    String button2String = "Нет";

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(title);  // заголовок
                    builder.setMessage(message); // сообщение
                    builder.setPositiveButton(button1String, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            deleteComment.setText("Удаление...");
                            deleteComment.setActivated(false);
                            DatabaseReference ref = FirebaseDatabase.getInstance("https://culinaryforumapp-default-rtdb.europe-west1.firebasedatabase.app").getReference();
                            Query commentDelQuery = ref.child(Constants.COMMENT_KEY).orderByChild("generatedCommentId").equalTo(comment.generatedCommentId);
                            commentDelQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                        snapshot.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(getContext(), "Комментарий удалён", Toast.LENGTH_SHORT).show();
                                                    deleteComment.setText("Удалить комментарий");
                                                    deleteComment.setActivated(true);
                                                    deleteComment.setVisibility(View.GONE);
                                                }
                                                else{

                                                    deleteComment.setText("Удалить комментарий");
                                                    deleteComment.setActivated(true);

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
                }
            });
        } else {
            deleteComment.setVisibility(View.GONE);
        }

        commentAuthorName.setText(comment.userNick);
        commentText.setText(comment.commentText);
        dateOfSend.setText(comment.sendingTime);

        return convertView;
    }
}
