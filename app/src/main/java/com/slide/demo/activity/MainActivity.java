package com.slide.demo.activity;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.slide.demo.R;
import com.slide.demo.adapter.SlideAdapter;

/**
 * @author ljm
 */
public class MainActivity extends AppCompatActivity {

    private RecyclerView mSlideRv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSlideRv = findViewById(R.id.rv_slide);
        SlideAdapter adapter = new SlideAdapter(this);
        mSlideRv.setAdapter(adapter);
        mSlideRv.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        mSlideRv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.top = 2;
                outRect.bottom = 2;
            }
        });
    }
}
