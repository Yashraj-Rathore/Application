package com.example.myapplication.ui.gallery;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class galleryViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public galleryViewModel() {
        mText = new MutableLiveData<>();

    }

    public LiveData<String> getText() {return mText;}

}
