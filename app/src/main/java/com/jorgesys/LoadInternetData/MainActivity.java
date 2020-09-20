package com.jorgesys.LoadInternetData;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

//https://trantor.is/search/?q=el+principito

public class MainActivity extends AppCompatActivity {
        Button buscar;
        EditText textoBusqueda;

        private RecyclerView recyclerView;
        private ParseAdapter adapter;
        private ArrayList<ParseItem> parseItems = new ArrayList<>();
        private ProgressBar progressBar;

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
        }

        @Override
        protected void onResume() {
            super.onResume();

            progressBar = findViewById(R.id.progressBar);
            recyclerView = findViewById(R.id.recyclerView);

            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new ParseAdapter(parseItems, this);
            recyclerView.setAdapter(adapter);



            textoBusqueda = (EditText)findViewById(R.id.etBuscar);

            String texto = textoBusqueda.getText().toString();
            buscar = (Button)findViewById(R.id.btnBusqueda);

            buscar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //Hide virtual keyboard!
                    hideKeyboardFrom(getApplicationContext(), textoBusqueda);

                    if(TextUtils.isEmpty(textoBusqueda.getText())){
                        Toast.makeText(MainActivity.this, "Debe ingresar el nombre del libro.", Toast.LENGTH_LONG).show();
                        textoBusqueda.setFocusable(true);
                    }else{
                        Content content = new Content();
                        content.execute();
                    }
                }
            });
        }

        private class Content extends AsyncTask<Void,Void, ArrayList<ParseItem>> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressBar.setVisibility(View.VISIBLE);
                progressBar.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
            }

            @Override
            protected void onPostExecute(ArrayList<ParseItem> items) {
                super.onPostExecute(items);
                progressBar.setVisibility(View.GONE);
                progressBar.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_out));
                //Update information
                adapter.updateData(items);
                adapter.notifyDataSetChanged();
            }

            @Override
            protected ArrayList<ParseItem> doInBackground(Void... voids) {
                String url = "https://trantor.is/search/?q="+ textoBusqueda.getText().toString();
                try {
                    Document doc = Jsoup.connect(url).get();

                    Elements data = doc.select("div.span1");
                    int size = data.size();
                    Log.d("doc", "doc: "+doc);
                    Log.d("data", "data: "+data);
                    Log.d("size", ""+size);
                    for (int i = 0; i < size; i++) {
                        String title = data.select("div.span1")
                                .select("img")
                                .eq(i)
                                .attr("alt");
                        String imgUrl = data.select("div.span1")
                                .select("img")
                                .eq(i)
                                .attr("src");

                        String detailUrl = data.select("div.span1")
                                .select("a")
                                .eq(i)
                                .attr("href");

                        //Check title and imgUrl doesn't contain null or empty values
                        if(!isNullorEmpty(title) && !isNullorEmpty(imgUrl)){
                            imgUrl = "https://trantor.is/" + imgUrl; //Add basepath
                            detailUrl =  "https://trantor.is/" + detailUrl; //Add basepath
                            parseItems.add(new ParseItem(imgUrl, title, detailUrl));
                            Log.d("items", "title: " + title + " img: " + imgUrl );
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return parseItems;
            }
        }


        public static void hideKeyboardFrom(Context context, View view) {
            InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        public static boolean isNullorEmpty(String s ) {
          return s == null || s.trim().isEmpty();
        }

    }