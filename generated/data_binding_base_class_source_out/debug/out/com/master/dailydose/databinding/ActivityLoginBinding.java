// Generated by view binder compiler. Do not edit!
package com.master.dailydose.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.master.dailydose.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityLoginBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final CardView cardView;

  @NonNull
  public final ImageView imageView3;

  @NonNull
  public final Button login;

  @NonNull
  public final ConstraintLayout main;

  @NonNull
  public final EditText password;

  @NonNull
  public final ProgressBar progress;

  @NonNull
  public final TextView signUp;

  @NonNull
  public final TextView textView;

  @NonNull
  public final TextView textView11;

  @NonNull
  public final EditText userName;

  private ActivityLoginBinding(@NonNull ConstraintLayout rootView, @NonNull CardView cardView,
      @NonNull ImageView imageView3, @NonNull Button login, @NonNull ConstraintLayout main,
      @NonNull EditText password, @NonNull ProgressBar progress, @NonNull TextView signUp,
      @NonNull TextView textView, @NonNull TextView textView11, @NonNull EditText userName) {
    this.rootView = rootView;
    this.cardView = cardView;
    this.imageView3 = imageView3;
    this.login = login;
    this.main = main;
    this.password = password;
    this.progress = progress;
    this.signUp = signUp;
    this.textView = textView;
    this.textView11 = textView11;
    this.userName = userName;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityLoginBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityLoginBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_login, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityLoginBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.cardView;
      CardView cardView = ViewBindings.findChildViewById(rootView, id);
      if (cardView == null) {
        break missingId;
      }

      id = R.id.imageView3;
      ImageView imageView3 = ViewBindings.findChildViewById(rootView, id);
      if (imageView3 == null) {
        break missingId;
      }

      id = R.id.login;
      Button login = ViewBindings.findChildViewById(rootView, id);
      if (login == null) {
        break missingId;
      }

      ConstraintLayout main = (ConstraintLayout) rootView;

      id = R.id.password;
      EditText password = ViewBindings.findChildViewById(rootView, id);
      if (password == null) {
        break missingId;
      }

      id = R.id.progress;
      ProgressBar progress = ViewBindings.findChildViewById(rootView, id);
      if (progress == null) {
        break missingId;
      }

      id = R.id.signUp;
      TextView signUp = ViewBindings.findChildViewById(rootView, id);
      if (signUp == null) {
        break missingId;
      }

      id = R.id.textView;
      TextView textView = ViewBindings.findChildViewById(rootView, id);
      if (textView == null) {
        break missingId;
      }

      id = R.id.textView11;
      TextView textView11 = ViewBindings.findChildViewById(rootView, id);
      if (textView11 == null) {
        break missingId;
      }

      id = R.id.userName;
      EditText userName = ViewBindings.findChildViewById(rootView, id);
      if (userName == null) {
        break missingId;
      }

      return new ActivityLoginBinding((ConstraintLayout) rootView, cardView, imageView3, login,
          main, password, progress, signUp, textView, textView11, userName);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
