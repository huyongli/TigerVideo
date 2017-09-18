package cn.ittiger.player.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import cn.ittiger.player.PlayerManager;

/**
 * @author: ylhu
 * @time: 17-9-18
 */
public class RecyclerViewVideoActivity extends AppCompatActivity {
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.recyclerview_layout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new VideoAdapter(this));
    }

    @Override
    protected void onResume() {

        super.onResume();
        PlayerManager.getInstance().resume();
    }

    @Override
    protected void onPause() {

        super.onPause();
        PlayerManager.getInstance().pause();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        PlayerManager.getInstance().release();
    }
}
