package o.maranello;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class TestSettings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_settings);
    }

    public void submit(View view) {
        Intent intent = new Intent(this, TestResults.class);
        startActivity(intent);
    }
}
